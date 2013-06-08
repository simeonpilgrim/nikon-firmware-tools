package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinSbiInFunction extends AbstractInputPinFunction {

    public TxIoPinSbiInFunction() {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
    }

    @Override
    public String getFullName() {
        return componentName + " SBI In";
    }

    @Override
    public String getShortName() {
        return "SI/SCL";
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages) System.out.println("TxIoPinSbiInFunction.setValue not implemented for pin " + getShortName());
    }

}
