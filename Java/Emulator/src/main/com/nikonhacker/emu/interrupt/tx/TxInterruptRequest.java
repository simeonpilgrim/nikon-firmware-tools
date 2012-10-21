package com.nikonhacker.emu.interrupt.tx;

import com.nikonhacker.emu.interrupt.InterruptRequest;

public class TxInterruptRequest extends InterruptRequest {

    /** the type of this interrupt/exception */
    Type type = Type.UNDEFINED;

    /** only meaningful for software/hardware interrupts */
    private int level = 0;

    /** only meaningful for software/hardware interrupts */
    private int interruptNumber;

    /** only meaningful for some interrupts */
    private int code = 0;

    /** only meaningful for coprocessor unusable exceptions */
    protected int coprocessorNumber;

    /** only meaningful for address error exceptions */
    protected int badVAddr;

    /**
     * Create request of a custom type (not to be used for Hardware interrupt as they also need an interrupt number)
     * @param type
     */
    public TxInterruptRequest(Type type) {
        super();
        this.type = type;
    }

    /**
     * Create custom requests
     * @param type
     * @param interruptNumber only meaningful for HW interrupt
     * @param level only meaningful for HW interrupt
     */
    public TxInterruptRequest(Type type, int interruptNumber, int level) {
        super();
        this.type = type;
        this.interruptNumber = interruptNumber;
        this.level = level;
    }

    public Type getType() {
        return type;
    }


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getInterruptNumber() {
        return interruptNumber;
    }

    public void setInterruptNumber(int interruptNumber) {
        this.interruptNumber = interruptNumber;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


    public int getCoprocessorNumber() {
        return coprocessorNumber;
    }


    public int getBadVAddr() {
        return badVAddr;
    }

    /**
     * Returns an absolute priority for this request, for comparison
     * Lower number = higher priority
     * Priority is first based on the type, then, for interrupts, on level (7 is the highest), and finally on interrupt number (lower had more priority
     * See table 6.2 at section 6.1.3.2
     * @return
     */
    public int getPriority() {
        return -(getType().getPriority() << 4) - getLevel();
    }
}
