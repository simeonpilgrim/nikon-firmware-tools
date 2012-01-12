package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class RegisterEqualityBreakCondition implements BreakCondition {
    int regNumber;
    int value;

    public RegisterEqualityBreakCondition(int regNumber, int value) {
        this.regNumber = regNumber;
        this.value = value;
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        return cpuState.getReg(regNumber) == value;
    }
}
