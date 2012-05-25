package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class ILMBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int ilm;
    private int ilmMask;

    public ILMBreakCondition(int ilm, int ilmMask, BreakTrigger breakTrigger) {
        super(breakTrigger);
        this.ilm = ilm;
        this.ilmMask = ilmMask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (cpuState.getILM() & ilmMask) == ilm;
    }
}
