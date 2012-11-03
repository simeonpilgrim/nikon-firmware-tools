package com.nikonhacker.disassembly;

import com.nikonhacker.emu.interrupt.InterruptRequest;

public abstract class CPUState {

    public final static int NOREG = -1;

    /**
     * Program Counter.
     * This is the actual address of the instruction being executed,
     * so the LSB is always 0 in this field (no matter the ISA mode for TX CPUs)
     */
    public int pc;

    /**
     * Register values
     */
    protected Register32[] regValue;

    /** Used for disassembly formatting */
    protected long regValidityBitmap = 0;

    /**
     * Tests if such a register number exists
     * @param regNumber
     * @return
     */
    public boolean registerExists(int regNumber) {
        return (regNumber >= 0) && (regNumber < regValue.length);
    }

    /**
     * Tests if the given register number is defined in the current disassembly context
     * @param regNumber
     * @return
     */
    public boolean isRegisterDefined(int regNumber) {
        return registerExists(regNumber) && ((regValidityBitmap & (1 << regNumber)) != 0);
    }

    /**
     * Declares the given register number as defined in the current disassembly context
     * @param regNumber
     */
    public void setRegisterDefined(int regNumber) {
        if (registerExists(regNumber))
            regValidityBitmap |= (1L << regNumber);
    }

    /**
     * Declares the given register number as undefined in the current disassembly context
     * @param regNumber
     */
    public void setRegisterUndefined(int regNumber) {
        if (registerExists(regNumber))
            regValidityBitmap &= (~(1L << regNumber));
    }

    /**
     * Declares all register numbers as defined in the current disassembly context
     */
    public void setAllRegistersDefined() {
        regValidityBitmap = -1L;
    }

    public void setReg(int registerNumber, int newValue) {
        regValue[registerNumber].setValue(newValue);
    }

    public int getReg(int registerNumber) {
        return regValue[registerNumber].getValue();
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public abstract void reset();

    public abstract void clear();

    public abstract boolean accepts(InterruptRequest interruptRequest);

    public abstract int getResetAddress();
}
