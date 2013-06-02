package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractOutputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinTimerOutputFunction extends AbstractOutputPinFunction implements PinFunction {
    private int timerNumber;

    public TxIoPinTimerOutputFunction(int timerNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.timerNumber = timerNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " Timer " + Format.asHex(timerNumber,1) + " output";
    }

    @Override
    public String getShortName() {
        return "TB" + Format.asHex(timerNumber,1) + "OUT";
    }

    @Override
    public Integer getValue(int defaultOutputValue) {
        System.out.println("TxIoPinTimerOutputFunction.getValue not implemented for pin " + getShortName());
        return null;
    }
}
