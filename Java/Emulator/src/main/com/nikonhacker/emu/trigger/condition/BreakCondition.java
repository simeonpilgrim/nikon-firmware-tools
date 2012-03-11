package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public interface BreakCondition {
    BreakTrigger getBreakTrigger();

    boolean matches(CPUState cpuState, Memory memory);
}
