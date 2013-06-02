package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractOutputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinClockFunction  extends AbstractOutputPinFunction implements PinFunction {

    public TxIoPinClockFunction() {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
    }

    @Override
    public String getFullName() {
        return componentName + " Clock";
    }

    @Override
    public String getShortName() {
        return "SCOUT";
    }

    @Override
    public Integer getValue(int defaultOutputValue) {
        if (IoPort.DEBUG) System.out.println("TxIoPinClockFunction.getValue not implemented for pin " + getShortName());
        return null;
    }
}
