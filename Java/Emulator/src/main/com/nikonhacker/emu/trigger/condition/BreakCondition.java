package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

public interface BreakCondition {
    BreakTrigger getBreakTrigger();

    boolean matches(CPUState cpuState, Memory memory);
}
