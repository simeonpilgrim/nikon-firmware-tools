package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.OutputPinFunction;

public class TxIoPinSbiOutFunction extends OutputPinFunction {
    @Override
    public String getFullName() {
        return "SBI Out";
    }

    @Override
    public String getShortName() {
        return "SO/SDA";
    }

}
