// Vincent Musso (vjm13)
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

int streq(const char* a, const char* b) {
	return strcmp(a, b) == 0;
}

int is_redirect_input(char* token) {  		//test for input redirect
	return streq(token, "<");
}

int is_redirect_output(char* token) { 		//test for output redirect
	return streq(token, ">");
}

void tokenize(char* input, char** tokens, int num_tokens) {  	//divide user input into tokens spaced by space, tab, or newline
	const char* delim = " \t\n";
	char* token = strtok(input, delim);
	int i;
	for(i = 0; token != NULL; i++) {
		tokens[i] = token;
		token = strtok(NULL, delim);
	}
	tokens[i] = NULL;			//tack on null value so execvp can find arguments
}

void exit_shell(char** tokens) {   	//determine exit code
	if(tokens[1] == NULL) {
		exit(0);
	} else {
		int code = atoi(tokens[1]); 
		exit(code);
	}
}

int check_for_redirect(char** tokens, int* new_in, int* new_out) {  	//loops through input tokens for any redirects
	int i = 0;
	int count = 0;
	while(tokens[i] != NULL) {  		//find end of tokens
		i++;
	}

	for(i -= 1; i > 0; i--) {			//decrement i to get last non-null index
		if(count > 2) {
			return -1;				//only 2 redirect options, so > 2 redirects means one is duplicated
		}							//might not be required given the checks below, but implemented as a safety net

		if(is_redirect_input(tokens[i])) {
			if(*new_in != 0) {
				return -1;			//cannot duplicate redirects, so if new_in or new_out are already set, then return
			}
			count++;
			*new_in = i;			//set equal to the index so as to preserve it after setting the token to null//
			tokens[i] = NULL;		//so that execvp can find arguments

		} else if(is_redirect_output(tokens[i])) {
			if(*new_out != 0) {
				return -1;
			}
			count++;
			*new_out = i;
			tokens[i] = NULL;
		}
	}
	return count != 0;				//so that we get 0 for no redirects, 1 for redirects (and -1 for too many, from earlier)
}

int open_redirects(FILE* new_input, FILE* new_output, char** tokens, int make_new_in, int make_new_out) {	//create redirect files
	if(make_new_in > 0) {
		new_input = freopen(tokens[make_new_in+1], "r", stdin);				//for both, file to be created/used is one index ahead
		if(new_input == NULL) {
			return 0;
		}
	} 
	if(make_new_out > 0) {
		new_output = freopen(tokens[make_new_out+1], "w", stdout);
		if(new_output == NULL) {
			return 0;
		}
	}
	return 1;
}

int main(int argc, char** argv) {
	signal(SIGINT, SIG_IGN);

	while(1) {
		char* input_buffer = calloc(300, 1);
		printf("myshell> ");
		fgets(input_buffer, 300, stdin);

		int num_tokens = (strlen(input_buffer) / 2) + 1;
		char** tokens = calloc(sizeof(char*), num_tokens);
		tokenize(input_buffer, tokens, num_tokens);

		if(tokens[0] == NULL) {
			free(input_buffer);
			free(tokens);
			continue;
			
		} else if(streq(tokens[0], "exit")) {
			exit_shell(tokens);

		} else if(streq(tokens[0], "cd")) {
			chdir(tokens[1]);
			free(input_buffer);
			free(tokens);
			continue;				//command has finished running, so we want to skip the rest of our checks
		} 

		int make_new_out = 0;
		int make_new_in = 0;
		FILE* new_input = NULL;
		FILE* new_output = NULL;
		int redirect = check_for_redirect(tokens, &make_new_in, &make_new_out);

		if(redirect == -1) {
			printf("Error: cannot redirect same input/output more than once\n");
			free(input_buffer);
			free(tokens);
			continue;
		}

		if(fork() == 0) {
			signal(SIGINT, SIG_DFL);
			if(redirect) {
				int redirect_success = open_redirects(new_input, new_output, tokens, make_new_in, make_new_out);
				if(!redirect_success) {
					printf("Error: could not open file for redirection\n");
					free(input_buffer);
					free(tokens);
					continue;
				}
			}

			execvp(tokens[0], &tokens[0]);

			if(errno) {
				perror("Error");
			}
			exit(1);
		} else {
			int status;
			int childpid = waitpid(-1, &status, 0);

			if(childpid == -1) {
				perror("Error");

			} else if(WIFSIGNALED(status)) {
				printf("\nTerminated due to signal: %s\n", strsignal(status));
			} 
		}
		free(input_buffer);
		free(tokens);
	}
	return 0;
}