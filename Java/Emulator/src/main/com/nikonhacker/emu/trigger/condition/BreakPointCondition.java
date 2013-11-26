package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class BreakPointCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int pc;

    public BreakPointCondition(int pc, BreakTrigger breakTrigger) {
        super(breakTrigger);
        this.pc = pc;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return cpuState.pc == pc;
    }

    public final int getPc() {
        return pc;
    }
}
