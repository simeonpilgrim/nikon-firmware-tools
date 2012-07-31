package com.nikonhacker.disassembly;

import com.nikonhacker.Format;

public abstract class CPUState {
    public int flags = 0;
    protected long regValidityBitmap = 0;
    /**
     * Program Counter
     */
    public int pc;
    /**
     * Register values
     */
    protected Register32[] regValue = new Register32[50];

    /** registers names */
    public abstract String[] getRegisterLabels();

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
        regValue[registerNumber].value = newValue;
    }

    public int getReg(int registerNumber) {
        return regValue[registerNumber].value;
    }

    protected static class Register32 {
        protected int value;

        public Register32(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "0x" + Integer.toHexString(value);
        }
    }
}
