package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

public class RegisterEqualityBreakCondition implements BreakCondition {
    int regNumber;
    int value;
    private BreakTrigger breakTrigger;

    public RegisterEqualityBreakCondition(int regNumber, int value, BreakTrigger breakTrigger) {
        this.regNumber = regNumber;
        this.value = value;
        this.breakTrigger = breakTrigger;
    }

    public BreakTrigger getBreakTrigger() {
        return breakTrigger;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return cpuState.getReg(regNumber) == value;
    }
}
