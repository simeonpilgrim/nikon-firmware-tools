package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinKeyFunction extends AbstractInputPinFunction implements PinFunction {
    private int keyNumber;

    public TxIoPinKeyFunction(int keyNumber) {
        this.keyNumber = keyNumber;
    }

    @Override
    public String getFullName() {
        return "Key " + keyNumber;
    }

    @Override
    public String getShortName() {
        return "KEY" + ((keyNumber<10)?"0":"") + keyNumber;
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
