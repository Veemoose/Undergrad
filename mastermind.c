
////////////////////////////////////////////////////////////////////////////////////////////////

// By: Vincent Musso
// Text-based implementation of Mastermind: https://en.wikipedia.org/wiki/Mastermind_(board_game)

////////////////////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

int streq(const char* a, const char* b) {
	return strcmp(a, b) == 0;
}

void get_line(char* input, int size) {
	fgets(input, size, stdin);
	int len = strlen(input);
	input[len-1] = '\0';
}

int random_int(int start, int end) {
	return rand() % end + start;
}

int retrieve_guess(char* input, int attemptNumber) {
	printf("Guess #%d: ", attemptNumber);
	get_line(input, 50);
	int length = strlen(input);
	return length;
}

int string_is_valid(char* input) {
	return strspn(input, "rRgGbByYpPoO") == 4;
}

char upperCharExchange(int color) {
	switch(color) {
		case 1: return 'R'; break;
		case 2: return 'G'; break;
		case 3: return 'B'; break;
		case 4: return 'Y'; break;
		case 5: return 'P'; break;
		default: return 'O'; break;
	}
}

char lowerCharExchange(int color) {
	switch(color) {
		case 1: return 'r'; break;
		case 2: return 'g'; break;
		case 3: return 'b'; break;
		case 4: return 'y'; break;
		case 5: return 'p'; break;
		default: return 'o'; break;
	}
}

int main() {
	srand((unsigned int)time(NULL));
	char input[10];
	char upperSolutionChars[4];
	char lowerSolutionChars[4];
	int solution[4];
	int playAgain = 0;
	char* yes = "y";
	char* Yes = "Y";
	char* no = "n";
	char* No = "N";
	
	do {
		int attemptCount = 10;
		printf("To choose your difficulty, type the number of different colors to use (min 3, max 6): ");
		get_line(input, 50);
		int numOfColors = atoi(input);

		while(numOfColors < 3 || numOfColors > 6) {
			printf("Please enter a number that is within the range [3, 6]: ");
			get_line(input, 50);
			numOfColors = atoi(input);
		}

		for(int i = 0; i < 4; i++) {
			solution[i] = random_int(1, numOfColors);
		}

		printf("You'll have 10 attempts to guess the sequence. The sequence is 4 colors long, with repeats allowed.\n");

		while(attemptCount >= 1) {
			int rightColorWrongSpot = 0;
			int rightColorRightSpot = 0;
			int length = retrieve_guess(input, (11 - attemptCount));

			while(length != 4) {
				printf("Your guess must be 4 characters long\n");
				length = retrieve_guess(input, (11 - attemptCount));
			}

			while(string_is_valid(input) == 0) {
				printf("Your guess must only contain upper or lowercase letters representative of the possible colors\n ([R]ed, [G]reen, [B]lue, [Y]ellow, [P]urple, [O]range");
				length = retrieve_guess(input, (11 - attemptCount));
			}

			for(int i = 0; i < length; i++) {
				upperSolutionChars[i] = upperCharExchange(solution[i]);
			}

			for(int i = 0; i < length; i++) {
				lowerSolutionChars[i] = lowerCharExchange(solution[i]);
			}

			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					if((upperSolutionChars[i] == input[j]) || (lowerSolutionChars[i] == input[j])) {
						if(i == j) {
							rightColorRightSpot += 1;
						} else if(i != j) {
							rightColorWrongSpot +=1;
						}
					}
				}
			}

			if(rightColorRightSpot == 4) {
				printf("Congratulations! You guessed the code! Play again? [Y]es or [N]o");
				while(1) {
					get_line(input, 50);
					if(streq(input, yes) || streq(input, Yes)) {
						playAgain = 1;
						break;
					} else if(streq(input, no) || streq(input, No)) {
						printf("\nThanks for playing! Goodbye!");
						exit(0);
					}
				}
				break;
			}

			printf("\n%d colors are correct and in the right spot. %d colors are correct but in the wrong spot.\n", rightColorRightSpot, rightColorWrongSpot);
			attemptCount -= 1;
		}

		if(attemptCount < 1) {
			printf("\nSorry, you've run out of attempts. Here was the solution:");
			for(int i = 0; i < 4; i++) {
				printf("%c", upperSolutionChars[i]);
			}
			printf("\nBetter luck next time! Play again? [Y]es or [N]o");
			get_line(input, 50);
			if(streq(input, yes) || streq(input, Yes)) {
				playAgain = 1;
			} else if(streq(input, no) || streq(input, No)) {
				printf("\nThanks for playing! Goodbye!");
				exit(0);
			}
		}
	} while(playAgain);

	
	return 0;
}