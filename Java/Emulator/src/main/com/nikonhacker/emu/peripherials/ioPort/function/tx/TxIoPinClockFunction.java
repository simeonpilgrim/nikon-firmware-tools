package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.OutputPinFunction;

public class TxIoPinClockFunction extends OutputPinFunction {
    @Override
    public String getFullName() {
        return "Clock";
    }

    @Override
    public String getShortName() {
        return "SCOUT";
    }

}
