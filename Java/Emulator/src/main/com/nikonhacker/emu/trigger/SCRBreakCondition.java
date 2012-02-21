package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class SCRBreakCondition implements BreakCondition {
    private int scr;
    private int scrMask;
    private BreakTrigger breakTrigger;

    public SCRBreakCondition(int scr, int scrMask, BreakTrigger breakTrigger) {
        this.scr = scr;
        this.scrMask = scrMask;
        this.breakTrigger = breakTrigger;
    }

    public BreakTrigger getBreakTrigger() {
        return breakTrigger;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (cpuState.getSCR() & scrMask) == scr;
    }
}
