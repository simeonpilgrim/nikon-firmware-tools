package com.nikonhacker.disassembly.tx;

import com.nikonhacker.disassembly.CodeStructure;

public class TxCodeStructure extends CodeStructure {

    public TxCodeStructure(int address) {
        super(address);
    }

    public String[] getRegisterLabels() {
        return TxCPUState.REG_LABEL;
    }
}