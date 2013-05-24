package com.nikonhacker.emu.peripherials.ioPort.function;

public class OutputPinFunction implements PinFunction {
    @Override
    public String getShortName() {
        return "OUT";
    }

    @Override
    public String getFullName() {
        return "Output Pin";
    }
}
