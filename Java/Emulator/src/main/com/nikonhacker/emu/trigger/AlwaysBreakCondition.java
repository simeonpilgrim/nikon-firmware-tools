package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class AlwaysBreakCondition implements BreakCondition {
    public boolean matches(CPUState cpuState, Memory memory) {
        return true;
    }
}
