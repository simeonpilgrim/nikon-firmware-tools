package com.nikonhacker.disassembly.arm;

import com.nikonhacker.disassembly.CodeStructure;

public class ArmCodeStructure extends CodeStructure {

    public ArmCodeStructure(int address) {
        super(address);
    }

    public String[] getRegisterLabels() {
        return ArmCPUState.registerLabels;
    }
}