package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractOutputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinSbiOutFunction extends AbstractOutputPinFunction implements PinFunction {

    public TxIoPinSbiOutFunction() {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
    }

    @Override
    public String getFullName() {
        return componentName + " SBI Out";
    }

    @Override
    public String getShortName() {
        return "SO/SDA";
    }

    @Override
    public Integer getValue(int defaultOutputValue) {
        System.out.println("TxIoPinSbiOutFunction.getValue not implemented for pin " + getShortName());
        return null;
    }
}
