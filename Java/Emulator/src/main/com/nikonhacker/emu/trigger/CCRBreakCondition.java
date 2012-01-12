package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class CCRBreakCondition implements BreakCondition {
    private int ccr;
    private int ccrMask;

    public CCRBreakCondition(int ccr, int ccrMask) {
        this.ccr = ccr;
        this.ccrMask = ccrMask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (cpuState.getCCR() & ccrMask) == ccr;
    }
}
