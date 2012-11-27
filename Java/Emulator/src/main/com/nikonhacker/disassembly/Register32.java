package com.nikonhacker.disassembly;

public class Register32 {
    // todo registers should hold their name and number
    private int value;

    public Register32() {
    }

    public Register32(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "0x" + Integer.toHexString(value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
