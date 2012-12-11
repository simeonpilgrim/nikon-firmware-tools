package com.nikonhacker.emu.peripherials.ioPins;

public class IoPins {
    boolean values[];

    boolean getValue(int pinNumber) {
        return values[pinNumber];
    }

    void setValue(int pinNumber, boolean value) {
        values[pinNumber] = value;
    }
}
