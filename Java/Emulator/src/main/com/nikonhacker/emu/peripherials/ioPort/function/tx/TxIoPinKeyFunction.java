package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinKeyFunction extends AbstractInputPinFunction implements PinFunction {
    private int keyNumber;

    public TxIoPinKeyFunction(int keyNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.keyNumber = keyNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " Key " + keyNumber;
    }

    @Override
    public String getShortName() {
        return "KEY" + ((keyNumber<10)?"0":"") + keyNumber;
    }

    @Override
    public void setValue(int value) {
        if (IoPort.DEBUG) System.out.println("TxIoPinKeyFunction.setValue not implemented for pin " + getShortName());
    }

}
