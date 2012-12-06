package com.nikonhacker.emu.interrupt.tx;

public enum Type {
    RESET_EXCEPTION(16, false),
    SINGLE_STEP_EXCEPTION(15, false),
    NMI(14, false),
    HARDWARE_INTERRUPT(13, true),
    SOFTWARE_INTERRUPT(12, true),
    INSTRUCTION_ADDRESS_ERROR_EXCEPTION(11, false),
    INSTRUCTION_BUS_ERROR_EXCEPTION(10, false),
    DEBUG_BREAKPOINT_EXCEPTION(9, false),
    COPROCESSOR_UNUSABLE_EXCEPTION(8, false),
    RESERVED_INSTRUCTION_EXCEPTION(7, false),
    INTEGER_OVERFLOW_EXCEPTION(6, false),
    TRAP_EXCEPTION(5, false),
    SYSTEM_CALL_EXCEPTION(4, false),
    BREAKPOINT_EXCEPTION(3, false),
    DATA_ADDRESS_ERROR_EXCEPTION(2, false),
    DATA_BUS_ERROR_EXCEPTION(1, false),
    UNDEFINED(0, true);

    int priority;
    boolean maskable;

    Type(int priority, boolean maskable) {
        this.priority = priority;
        this.maskable = maskable;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isMaskable() {
        return maskable;
    }

    /**
     * According to spec convention, maskable => interrupt, non-maskable => exception
     * @return
     */
    public boolean isException() {
        return !maskable;
    }

    /**
     * According to spec convention, maskable => interrupt, non-maskable => exception
     * @return
     */
    public boolean isInterrupt() {
        return maskable;
    }
}
