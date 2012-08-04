package com.nikonhacker.disassembly.fr;

import com.nikonhacker.disassembly.CodeStructure;

public class FrCodeStructure extends CodeStructure {

    public FrCodeStructure(int address) {
        super(address);
    }

    public String[] getRegisterLabels() {
        return FrCPUState.REG_LABEL;
    }
}