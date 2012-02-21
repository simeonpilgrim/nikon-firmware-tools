package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class CCRBreakCondition implements BreakCondition {
    private int ccr;
    private int ccrMask;
    private BreakTrigger breakTrigger;

    public CCRBreakCondition(int ccr, int ccrMask, BreakTrigger breakTrigger) {
        this.ccr = ccr;
        this.ccrMask = ccrMask;
        this.breakTrigger = breakTrigger;
    }

    public BreakTrigger getBreakTrigger() {
        return breakTrigger;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (cpuState.getCCR() & ccrMask) == ccr;
    }
}
