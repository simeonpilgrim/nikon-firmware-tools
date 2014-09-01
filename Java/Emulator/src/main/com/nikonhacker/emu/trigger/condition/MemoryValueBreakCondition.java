package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class MemoryValueBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int address = 0;
    private int mask = 0xFFFFFFFF;
    private boolean isChangeDetection = true;
    private boolean negate = false;
    private int value = 0;

    public MemoryValueBreakCondition(BreakTrigger breakTrigger) {
        super(breakTrigger);
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public boolean isChangeDetection() {
        return isChangeDetection;
    }

    public void setChangeDetection(boolean changeDetection) {
        isChangeDetection = changeDetection;
    }

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value & mask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        final int currentValue = memory.load32(address) & mask;
        final boolean matches = negate ^ (currentValue == value);
        if (matches && isChangeDetection) {
            // Re-arm trigger so that if will fire when value changes from the current one
            negate = true;
            value = currentValue;
        }
        return matches;
    }

    @Override
    public String toString() {
        return "@(0x" + Format.asHex(address, 8) + ") & 0x" + Format.asHex(mask, 8) + (isChangeDetection ? " changes" : ((negate?" != ":" == ") + "0x" + Format.asHex(value, 8)));
    }
}
