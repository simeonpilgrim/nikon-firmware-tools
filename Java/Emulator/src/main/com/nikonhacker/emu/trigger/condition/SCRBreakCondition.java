package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class SCRBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int scr;
    private int scrMask;

    public SCRBreakCondition(int scr, int scrMask, BreakTrigger breakTrigger) {
        super(breakTrigger);
        this.scr = scr;
        this.scrMask = scrMask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (cpuState.getSCR() & scrMask) == scr;
    }
}
