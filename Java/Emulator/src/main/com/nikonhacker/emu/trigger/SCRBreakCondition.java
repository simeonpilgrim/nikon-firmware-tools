package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class SCRBreakCondition implements BreakCondition {
    private int scr;
    private int scrMask;

    public SCRBreakCondition(int scr, int scrMask) {
        this.scr = scr;
        this.scrMask = scrMask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (cpuState.getSCR() & scrMask) == scr;
    }
}
