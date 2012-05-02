package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.Format;
import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class MemoryValueBreakCondition implements BreakCondition {
    private BreakTrigger breakTrigger = null;
    private int address = 0;
    private int value = 0;
    private int mask = 0xFFFFFFFF;
    private boolean negate = false;

    public MemoryValueBreakCondition() {
    }

    public MemoryValueBreakCondition(int address, int value, int mask, boolean negate, BreakTrigger breakTrigger) {
        this.address = address;
        this.value = value;
        this.mask = mask;
        this.negate = negate;
        this.breakTrigger = breakTrigger;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }

    public BreakTrigger getBreakTrigger() {
        return breakTrigger;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return negate ^ ((memory.load32(address) & mask) == value);
    }

    @Override
    public String toString() {
        return "@(0x" + Format.asHex(address, 8) + ") & 0x" + Format.asHex(mask, 8) + (negate?" != ":" == ") + "0x" + Format.asHex(value, 8);
    }
}
