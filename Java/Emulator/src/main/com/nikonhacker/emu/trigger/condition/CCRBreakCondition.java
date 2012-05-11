package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class CCRBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int ccr;
    private int ccrMask;

    public CCRBreakCondition(int ccr, int ccrMask, BreakTrigger breakTrigger) {
        super(breakTrigger);
        this.ccr = ccr;
        this.ccrMask = ccrMask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (cpuState.getCCR() & ccrMask) == ccr;
    }
}
