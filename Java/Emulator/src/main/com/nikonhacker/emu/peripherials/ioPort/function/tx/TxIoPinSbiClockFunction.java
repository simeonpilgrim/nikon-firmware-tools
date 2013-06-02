package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractOutputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinSbiClockFunction extends AbstractOutputPinFunction implements PinFunction {

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
    public Integer getValue(int defaultOutputValue) {
        if (IoPort.DEBUG) System.out.println("TxIoPinSbiClockFunction.getValue not implemented for pin " + getShortName());
        return null;
    }
}
