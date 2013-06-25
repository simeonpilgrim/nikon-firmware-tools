package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinSbiInFunction extends AbstractInputPinFunction implements PinFunction {
    @Override
    public String getFullName() {
        return "SBI In";
    }

    @Override
    public String getShortName() {
        return "SI/SCL";
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
