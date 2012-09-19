package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.disassembly.fr.FrCPUState;
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

    public boolean matches(FrCPUState cpuState, Memory memory) {
        return (cpuState.getSCR() & scrMask) == scr;
    }
}
