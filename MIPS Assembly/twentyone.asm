# Vincent Musso (vjm13)
.data
	deck: .byte 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11
	player_hand: .byte 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	dealer_hand: .byte 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	dealt_count: .byte 0
	player_score: .byte 0
	dealer_score: .byte 0
	player_card_count: .byte 0
	dealer_card_count: .byte 0
	current_position: .byte 0
	dealer_label: .asciiz "Dealer's hand: "
	dealer_final_score: .asciiz "Dealer's final score: "
	player_label: .asciiz "Player's hand: "
	game_message: .asciiz "What would you like to do? (0 = stand, 1 = hit): "
	player_won: .asciiz "Congratulations, you won!"
	player_lost: .asciiz "Sorry, you lose..."
	tie_message: .asciiz "It's a tie game!"

.text	
#--------------------------------------------------	
#--------------------------------------------------
# Print string
print_string:
	push	ra
	li	v0, 4
	syscall
	pop	ra
	jr	ra
#--------------------------------------------------
#--------------------------------------------------
# Print char
print_char:
	push	ra
	li	v0, 11
	syscall
	pop	ra
	jr	ra
#--------------------------------------------------
#--------------------------------------------------
print_int:
	push	ra
	li	v0, 1
	syscall
	li	a0, ' '
	jal	print_char
	pop	ra
	jr	ra
#--------------------------------------------------
#--------------------------------------------------
# shuffler
shuffle_deck:
	push	ra
	
	li	t0, 51		# i = 51
	li	t1, 1
_shuffle_loop:
	ble	t0, t1, _shuffle_exit	# while( i >= 51 )
	
	li	a0, 0
	add	a1, t0, 1
	li	v0, 42
	syscall			# rand number between 0 and i + 1, exclusive
	move	t2, a0		# rand number now in t2
	
	la	t3, deck
	add	t4, t3, t2	# t4 now contains address of deck[other_slot]
	add	t3, t3, t0	# t3 now contains address of deck[i]. byte array, so index offset is equal to i
	
	lb	t5, (t4)
	lb	t6, (t3)
	
	sb	t6, (t4)
	sb	t5, (t3)	#  swap(deck[i], deck[other_slot]) complete	
			
	sub	t0, t0, 1
	j 	_shuffle_loop
_shuffle_exit:
	pop	ra
	jr	ra	
	
#--------------------------------------------------
#--------------------------------------------------
# Player dealer
deal_card_to_player:
	push	ra
	
	lb	t0, current_position		# t0 contains i
	la	t1, deck
	add	t1, t1, t0			# t1 now contains address of deck[i]
	lb	t2, (t1)			# t2 now contains contents of deck[i]
	
	lb	t3, player_card_count
	la	t4, player_hand	
	add	t4, t4, t3			# t4 now contains address of player_hand[j]
	
	sb	t2, (t4)			# player-hand[j] contains contents of deck[i]
	
	add	t0, t0, 1
	sb	t0, current_position
	add	t3, t3, 1
	sb	t3, player_card_count
	
	lb	t0, dealt_count
	add	t0, t0, 1
	sb	t0, dealt_count
	
	pop	ra
	jr	ra
#--------------------------------------------------
#--------------------------------------------------
# House dealer
deal_card_to_dealer:
	push	ra
	
	lb	t0, dealer_score
	bge	t0, 17, _deal_card_exit
	
	lb	t0, current_position
	la	t1, deck
	add	t1, t1, t0
	lb	t2, (t1)
	
	lb	t3, dealer_card_count
	la	t4, dealer_hand
	add	t4, t4, t3
	
	sb	t2, (t4)
	
	add	t0, t0, 1
	sb	t0, current_position
	add	t3, t3, 1
	sb	t3, dealer_card_count
	
	lb	t0, dealt_count
	add	t0, t0, 1
	sb	t0, dealt_count
_deal_card_exit:	
	pop	ra
	jr	ra
#--------------------------------------------------
#--------------------------------------------------
# Display the two hands
show_hands:
	push	ra
	
	la	a0, dealer_label
	jal	print_string
	
	li	t0, 0
	li	t3, 0	# will use this for total score
_show_dealer_hand_loop:
	la	t1, dealer_hand
	add	t1, t1, t0
	lb	t1, (t1)
	beq	t1, 0, _show_dealer_exit
	
		move	t2, t1
		add	t3, t3, t2	# Keep track of running total
		move	a0, t2
		jal	print_int
	
	add	t0, t0, 1
	j	_show_dealer_hand_loop
_show_dealer_exit:
	sb	t3, dealer_score
	li	a0, '='
	jal	print_char
	li	a0, ' '
	jal	print_char
	move	a0, t3
	sb	t3, dealer_score
	li	v0, 1
	syscall
	li	a0, '\n'
	jal	print_char
#--------------------------------------------------
# Do the same for the player:
	la	a0, player_label
	jal	print_string
	
	li	t0, 0
	li	t3, 0
_show_player_hand_loop:
	la	t1, player_hand
	add	t1, t1, t0
	lb	t1, (t1)
	beq	t1, 0, _show_player_exit
	
		move	t2, t1
		add	t3, t3, t2
		move	a0, t2
		jal	print_int
		
	add	t0, t0, 1
	j	_show_player_hand_loop
_show_player_exit:
	sb	t3, player_score
	li	a0, '='
	jal	print_char
	li	a0, ' '
	jal	print_char
	move	a0, t3
	sb	t3, player_score
	li	v0, 1
	syscall
	li	a0, '\n'
	jal	print_char
	
	pop	ra
	jr	ra
#--------------------------------------------------
#--------------------------------------------------
# Check scores
check_scores:
	push	ra
	
	lb	t0, dealer_score
	lb	t1, player_score
	
	bne	t1, 21, _skip_tie
	bne	t0, 21, _skip_tie
		j	tie_game	# Both scores == 21
_skip_tie:
	beq	t1, 21, player_win	# Player gets 21
	beq	t0, 21,	player_lose	# Dealer gets 21
	bgt	t1, 21, player_lose	# Player exceeds 21
	bgt	t0, 21, player_win	# Dealer exceeds 21
	
	pop	ra
	jr	ra
#--------------------------------------------------
#--------------------------------------------------
player_win:
	la	a0, player_won
	li	v0, 4
	syscall
	li	v0, 10
	syscall
#--------------------------------------------------
#--------------------------------------------------
player_lose:
	la	a0, player_lost
	li	v0, 4
	syscall
	li	v0, 10
	syscall
#--------------------------------------------------
#--------------------------------------------------
tie_game:
	la	a0, tie_message
	li	v0, 4
	syscall
	li	v0, 10
	syscall
#--------------------------------------------------
#--------------------------------------------------
# Reacts to input of hit or stand. Game should end after a stand
take_turns:
	push	ra
	la	a0, game_message
	jal	print_string
	li	v0, 5
	syscall
	move	t5, v0
	beq	t5, 1, _deal_player_card
	j	_stand
	
_deal_player_card:
	jal	deal_card_to_player	
	lb	t6, dealer_score
	blt	t6, 17, _deal_to_house
	j	exit
	
_deal_to_house:
	jal	deal_card_to_dealer
	j	exit

_stand:
	lb	s0, player_score
	lb	s1, dealer_score
_deal_loop:
	bge	s1, 17, _deal_loop_end
		jal	deal_card_to_dealer
		add	s1, s1, t2		# after function return, t2 contains the value of the most recent card drawn from the deck
	j	_deal_loop
_deal_loop_end:
	la	a0, dealer_final_score		#
	jal	print_string			#
	move	a0, s1				# proof that the dealer won/lost
	jal	print_int			#
	li	a0, '\n'
	jal	print_char
	
	bgt	s1, 21, player_win
	beq	s0, s1, tie_game
	bgt	s0, s1, player_win
	blt	s0, s1, player_lose
	j	tie_game
exit:
	pop	ra
	jr	ra
#--------------------------------------------------
#--------------------------------------------------
.globl main
main:
	li	t0, 0
	sb	t0, dealer_score
	sb	t0, player_score
	jal	shuffle_deck
	jal	deal_card_to_player
	jal	deal_card_to_dealer
	jal	deal_card_to_player
	jal	deal_card_to_dealer

_main_loop:
	jal	show_hands
	jal	check_scores
	jal	take_turns
	j	_main_loop