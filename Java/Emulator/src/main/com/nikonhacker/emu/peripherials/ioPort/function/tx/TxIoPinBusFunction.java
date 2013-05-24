package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinBusFunction extends AbstractInputPinFunction implements PinFunction {
    @Override
    public String getFullName() {
        return "Bus";
    }

    public String getShortName() {
        return "BUS";
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }
}
