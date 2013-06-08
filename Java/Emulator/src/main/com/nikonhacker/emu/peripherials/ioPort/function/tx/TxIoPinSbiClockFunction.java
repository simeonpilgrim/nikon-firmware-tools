package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractOutputPinFunction;

public class TxIoPinSbiClockFunction extends AbstractOutputPinFunction {

    public TxIoPinSbiClockFunction() {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
    }

    @Override
    public String getFullName() {
        return componentName + " SBI Clock";
    }

    @Override
    public String getShortName() {
        return "SCK";
    }

    @Override
    public Integer getValue(Integer defaultOutputValue) {
        if (logPinMessages) System.out.println("TxIoPinSbiClockFunction.getValue not implemented for pin " + getShortName());
        return null;
    }
}
