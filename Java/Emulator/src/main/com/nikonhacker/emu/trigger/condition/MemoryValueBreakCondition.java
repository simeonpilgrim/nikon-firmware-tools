package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class MemoryValueBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int address = 0;
    private int value = 0;
    private int mask = 0xFFFFFFFF;
    private boolean negate = false;

    public MemoryValueBreakCondition(BreakTrigger breakTrigger) {
        super(breakTrigger);
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

    public boolean matches(FrCPUState cpuState, Memory memory) {
        return negate ^ ((memory.load32(address) & mask) == value);
    }

    @Override
    public String toString() {
        return "@(0x" + Format.asHex(address, 8) + ") & 0x" + Format.asHex(mask, 8) + (negate?" != ":" == ") + "0x" + Format.asHex(value, 8);
    }
}
