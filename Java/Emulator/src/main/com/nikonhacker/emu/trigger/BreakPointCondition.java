package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class BreakPointCondition implements BreakCondition {
    private int pc;

    public BreakPointCondition(int pc) {
        this.pc = pc;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return cpuState.pc == pc;
    }
}
