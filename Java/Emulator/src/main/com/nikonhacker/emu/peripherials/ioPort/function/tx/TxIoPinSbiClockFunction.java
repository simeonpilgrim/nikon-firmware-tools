package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.OutputPinFunction;

public class TxIoPinSbiClockFunction extends OutputPinFunction {
    @Override
    public String getFullName() {
        return "SBI Clock";
    }

    @Override
    public String getShortName() {
        return "SCK";
    }

}
