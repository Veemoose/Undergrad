# Coursework
Small collection of programming assignments/projects 


# Logisim
Software for building logical circuits and simulating behavior. 
  Two files:
    
    logisim-2.7.2-2191_b.jar - Java archive, for running Logisim
    
    Single_Cycle_CPU.circ - file containing all of the circuits created for a simple, single-cycle/single core CPU
    
  Also contains a list of test ROMs to run by loading into the 
  instruction memory on the 'main' circuit in Single_Cycle_CPU.circ
  
  To run rest ROMs, open Single_Cycle_CPU.circ in Logisim, go to 'main,' right click on the instruction memory and select
  'Load image...' Choose a ROM and then use the Simulate tab to initiate simulatin and change clock frequency
  

# Mars: MIPS Assembler/Runtime Simulator
Software for writing, assembling, and running assembly code for MIPS Architecture
  Two files:
    
    Mars_2191_c.jar - Java archive for running Mars (slightly modified from original release, e.g. addition of push and pop instructions, removal of need for '$' before register names
    
    twentyone.asm - assembly file for game of blackjack, one player against the 'dealer,' one round at a time
