package com.nikonhacker.emu.trigger.condition.fr;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.AbstractLoggingBreakCondition;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

public class SCRBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int scr;
    private int scrMask;

    public SCRBreakCondition(int scr, int scrMask, BreakTrigger breakTrigger) {
        super(breakTrigger);
        this.scr = scr;
        this.scrMask = scrMask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (((FrCPUState)cpuState).getSCR() & scrMask) == scr;
    }
}
