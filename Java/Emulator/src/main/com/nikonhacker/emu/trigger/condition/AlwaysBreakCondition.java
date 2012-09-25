package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class AlwaysBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    public AlwaysBreakCondition() {
        super(null);
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return true;
    }
}
