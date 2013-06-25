package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinBusFunction extends AbstractInputPinFunction {

    public TxIoPinBusFunction() {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
    }

    @Override
    public String getFullName() {
        return componentName + " Bus";
    }

    @Override
    public String getShortName() {
        return "BUS";
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages) System.out.println("TxIoPinBusFunction.setValue not implemented for pin " + getShortName());
    }
}
