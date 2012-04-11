package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class AlwaysBreakCondition implements BreakCondition {
    public BreakTrigger getBreakTrigger() {
        return null;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return true;
    }
}
