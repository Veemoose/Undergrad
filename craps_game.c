// Vincent Musso (vjm13)
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int streq(const char* a, const char* b) {
	return strcmp(a, b) == 0;
}

int get_input(char* input, int size) {
	fgets(input, size, stdin);
	int len = strlen(input);
	if(len - 1 == 0) {
		return 0;
	}
	input[len - 1] = '\0';
	return 1;
}

void wait_for_input(char* input, int size, char* message) {
	int answer = 0;
	while(!answer) {
		answer = get_input(input, size);
		if(!answer) {
			printf("%s", message);
		}
	}
}

int check_initial_input(char* response) {
	if(streq(response, "play")) {
		return 1;
	} else if(streq(response, "quit")) {
		return 2;
	} else {
		return 0;
	}
}

int check_subsequent_inputs(char* response) {
	if(streq(response, "yes")) {
		return 1;
	} else if(streq(response, "no")) {
		return 2;
	} else {
		return 0;
	}
}

int check_first_roll(int roll, int* point) {
	int success = 0;

	switch(roll) {
		case 2:
			break;
		case 3:
			break;
		case 12:
			break;
		case 7:
			success = 1;
			break;
		case 11:
			success = 1;
			break;
		default:
			*point = roll;
			success = 2;
			break;
	}
	return success;
}

int play_again_query() {
	printf("Would you like to play again? ");

	char response[50];
	int received = 0;
	while(received == 0) {
		wait_for_input(response, 50, "Please give a response: ");
		received = check_subsequent_inputs(response);
		if(received == 0) {
			printf("Enter 'yes' to play again or 'no' to quit: ");
		}
	}
	return received;
}

void roll_die(FILE* rand_source, unsigned char* first_die, unsigned char* second_die) {
	size_t read = fread(first_die, 1, 1, rand_source);
	if(read != 1) {
		printf("Error reading from file\nExiting");
		exit(1);
	}
	*first_die = (*first_die % 6) + 1;

	read = fread(second_die, 1, 1, rand_source);
	if(read != 1) {
		printf("Error reading from file\nExiting");
		exit(1);
	}
	*second_die = (*second_die % 6) + 1;
}

int enter_second_stage(FILE* rand_source, int point) {
	int roll = 0;

	unsigned char first_die;
	unsigned char second_die;

	roll_die(rand_source, &first_die, &second_die);
	roll = first_die + second_die;

	while(roll != point && roll != 7) {
		printf("You rolled %d (%d and %d)\n", roll, first_die, second_die);
		//sleep(1);
		printf("Next roll...\n");
		sleep(1);
		roll_die(rand_source, &first_die, &second_die);
		roll = first_die + second_die;
	}

	if(roll == 7) {
		printf("You rolled %d! (%d and %d)\n", roll, first_die, second_die);
		printf("Sorry, you lose...\n");
		return play_again_query();

	} else if (roll == point) {
		printf("You rolled %d! (%d and %d)\n", roll, first_die, second_die);
		printf("Congratulations, you won!\n");
		return play_again_query();
	}
	return 3;   // This return should never be reached, since we do not leave the loop until
				// one of the above statements is satisfied.
}

int play_the_game(FILE* rand_source) {
	int point = 0;
	int success = 0;

	unsigned char first_die; 
	unsigned char second_die;

	roll_die(rand_source, &first_die, &second_die);

	int roll = first_die + second_die;
	printf("You rolled %d (%d and %d)\n", roll, first_die, second_die);

	success = check_first_roll(roll, &point);

	if(success == 0) {
		printf("Sorry, you lost...\n");
		return play_again_query();

	} else if(success == 1) {
		printf("Congratulations, you won!\n");
		return play_again_query();

	} else {
		printf("%d is now the point\n", roll);
		sleep(1);
		printf("Starting second round...\n");
		sleep(1);
		return enter_second_stage(rand_source, roll);
	}
}

int main(int argc, char** argv) {

	if(argc < 2) {
		printf("You have to specify a file\n");
		exit(1);
	}
	FILE* rand_source = fopen(argv[1], "rb");
	if(rand_source == NULL) {
		printf("There was a problem opening the file\n");
		exit(1);
	}

	printf("Oh Craps! It's time to play a game!\nWelcome! What's your name? ");
	char name[100];
	wait_for_input(name, 100, "You have to enter a name, silly: ");

	printf("Hi %s, would you like to play or quit? ", name);
	char response[50];
	int mode = 0;
	while(mode == 0) {
		wait_for_input(response, 50, "That wasn't exactly a response: ");
		mode = check_initial_input(response);
		if(mode == 0) {
			printf("Enter 'play' to play the game or 'quit' to exit: ");
		}
	}

	while(mode == 1) {
		mode = play_the_game(rand_source);
	}

	if(mode == 3) {												
		printf("Uh, something really weird happened.\n");		// As stated in enter_second_stage(), 
		printf("This message shouldn't appear.\n");			    // these lines of could should never execute.
		printf("Time to bail...\n");							// These are merely put here as a precaution.
		exit(1);												
	}
	printf("See ya next time!\n");

	return 0;
}