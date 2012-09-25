package com.nikonhacker.emu.trigger.condition.fr;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.AbstractLoggingBreakCondition;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

public class CCRBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int ccr;
    private int ccrMask;

    public CCRBreakCondition(int ccr, int ccrMask, BreakTrigger breakTrigger) {
        super(breakTrigger);
        this.ccr = ccr;
        this.ccrMask = ccrMask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (((FrCPUState)cpuState).getCCR() & ccrMask) == ccr;
    }
}
