package com.nikonhacker.emu.interrupt.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.interrupt.InterruptRequest;

public class TxInterruptRequest extends InterruptRequest {

    /** the type of this interrupt/exception */
    private Type type = Type.UNDEFINED;

    /** only meaningful for software/hardware interrupts */
    private int level = 0;

    /** only meaningful for some interrupts - TODO never set for now */
    private int code = 0;

    /** only meaningful for coprocessor unusable exceptions */
    protected int coprocessorNumber;

    /** only meaningful for address error exceptions */
    protected int badVAddr;

    /**
     * Create request of a custom type (not to be used for Hardware interrupt as they also require an interrupt number)
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

    /**
     * Interrupt level
     * @return higher number means higher priority
     */
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
     * Returns an absolute priority for this request, for comparison
     * Priority is based on :
     * 1) the type - See table 6.2 at section 6.1.3.2
     * 2) then, for interrupts, on level ("7" is the highest)
     * 3) finally on interrupt number (lower interrupt number means more priority)
     * See section 6.5.1.6
     * @return a Lower number for higher priority
     */
    public int getPriority() {
        return -(getType().getPriority() << 8) - (getLevel() << 4) + interruptNumber;
    }

    @Override
    public String toString() {
        return type + (type==Type.HARDWARE_INTERRUPT?(" #" + interruptNumber + " (0x" + Format.asHex(interruptNumber, 2) + ")"):"");
    }


}
