package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class ILMBreakCondition implements BreakCondition {
    private int ilm;
    private int ilmMask;

    public ILMBreakCondition(int ilm, int ilmMask) {
        this.ilm = ilm;
        this.ilmMask = ilmMask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (cpuState.getILM() & ilmMask) == ilm;
    }
}
