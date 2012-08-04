package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class RegisterEqualityBreakCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    int regNumber;
    int value;

    public RegisterEqualityBreakCondition(int regNumber, int value, BreakTrigger breakTrigger) {
        super(breakTrigger);
        this.regNumber = regNumber;
        this.value = value;
    }

    public boolean matches(FrCPUState cpuState, Memory memory) {
        return cpuState.getReg(regNumber) == value;
    }
}
