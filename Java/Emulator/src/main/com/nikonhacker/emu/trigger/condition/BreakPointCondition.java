package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class BreakPointCondition implements BreakCondition {
    private int pc;
    private BreakTrigger breakTrigger;

    public BreakPointCondition(int pc, BreakTrigger breakTrigger) {
        this.pc = pc;
        this.breakTrigger = breakTrigger;
    }

    public BreakTrigger getBreakTrigger() {
        return breakTrigger;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return cpuState.pc == pc;
    }
}
