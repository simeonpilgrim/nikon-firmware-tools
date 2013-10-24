#   Simple FR80 C++ startup with construction of static objectes before call main()
#   and destruction afterwards.
#
    .balign 4
    .text
    .extern main

    .global _start
 _start: 
	# 1. Set up stack frame: don't have libc and loader (assume it is done)

    # 2. Call init standard C library (prepare C++ new/delete)

    # 3. Call constructors
    ldi:32 _init,R12
    call    @R12

    # 4. Prepare argc, argv, envc, envp (all 0) and call main()
    ldi:8 0, R4
    mov R4, R5
    mov R4, R6
    mov R4, R7
    ldi:32 main,R12
	call @R12
	
	# 6. Call exit() to terminate process with exit code: do it myself

	# 6.1 call destructors
    ldi:32 _fini,R12
	call @R12
	
	# 6.2 loop forever
.wait:
	bra .wait
    .end
