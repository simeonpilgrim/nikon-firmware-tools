package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinBusFunction extends AbstractInputPinFunction implements PinFunction {

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
        if (IoPort.DEBUG) System.out.println("TxIoPinBusFunction.setValue not implemented for pin " + getShortName());
    }
}
