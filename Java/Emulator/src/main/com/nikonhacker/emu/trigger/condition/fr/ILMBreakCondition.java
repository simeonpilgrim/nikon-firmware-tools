package com.nikonhacker.emu.trigger.condition.fr;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.AbstractLoggingBreakCondition;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

public class ILMBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    private int ilm;
    private int ilmMask;

    public ILMBreakCondition(int ilm, int ilmMask, BreakTrigger breakTrigger) {
        super(breakTrigger);
        this.ilm = ilm;
        this.ilmMask = ilmMask;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return (((FrCPUState)cpuState).getILM() & ilmMask) == ilm;
    }
}
