package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class ILMBreakCondition implements BreakCondition {
    private int ilm;
    private int ilmMask;
    private BreakTrigger breakTrigger;

    public ILMBreakCondition(int ilm, int ilmMask, BreakTrigger breakTrigger) {
        this.ilm = ilm;
        this.ilmMask = ilmMask;
        this.breakTrigger = breakTrigger;
    }

    public BreakTrigger getBreakTrigger() {
        return breakTrigger;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (cpuState.getILM() & ilmMask) == ilm;
    }
}
