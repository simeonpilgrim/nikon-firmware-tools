package com.nikonhacker.disassembly.tx;

import com.nikonhacker.disassembly.Register32;

public class NullRegister32 extends Register32 {

    public NullRegister32() {
        super();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int getValue() {
        return 0;
    }

    @Override
    public void setValue(int value) {
        // ignore
    }
}
