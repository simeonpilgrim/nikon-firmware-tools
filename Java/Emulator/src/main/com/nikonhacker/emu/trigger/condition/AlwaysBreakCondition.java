package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;

public class AlwaysBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    public AlwaysBreakCondition() {
        super(null);
    }

    public boolean matches(FrCPUState cpuState, Memory memory) {
        return true;
    }
}
