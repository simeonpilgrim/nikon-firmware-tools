package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class BreakPointCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int pc;

    public BreakPointCondition(int pc, BreakTrigger breakTrigger) {
        super(breakTrigger);
        this.pc = pc;
    }

    public boolean matches(FrCPUState cpuState, Memory memory) {
        return cpuState.pc == pc;
    }
}
