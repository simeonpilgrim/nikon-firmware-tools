package com.nikonhacker.emu.peripherials.ioPort.function;

public class InputPinFunction extends AbstractInputPinFunction implements PinFunction {
    private int value;

    @Override
    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String getShortName() {
        return "IN";
    }

    @Override
    public String getFullName() {
        return "Input Pin";
    }
}
