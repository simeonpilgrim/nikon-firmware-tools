package com.nikonhacker.emu.interrupt.tx;

import com.nikonhacker.emu.interrupt.InterruptRequest;

public class TxInterruptRequest implements InterruptRequest {

    /** the type of this interrupt/exception */
    Type type = Type.UNDEFINED;

    /** only meaningful for software/hardware interrupts */
    int level = 0;

    /** only meaningful for some interrupts */
    int code = 0;

    /** only meaningful for coprocessor unusable exceptions */
    protected int coprocessorNumber;

    /** only meaningful for address error exceptions */
    protected int badVAddr;

    public TxInterruptRequest(Type type) {
        super();
        this.type = type;
    }

    public TxInterruptRequest(Type type, int level) {
        super();
        this.type = type;
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
     * Priority is first based on the type, then, for interrupts, on level (7 is the highest)
     * See table 6.2 at section 6.1.3.2
     * @return
     */
    public int getPriority() {
        return (getType().getPriority() << 4) + getLevel();
    }

    @Override
    public int compareTo(Object o) {
        TxInterruptRequest other = (TxInterruptRequest) o;
        return other.getPriority() - getPriority();
    }
}
