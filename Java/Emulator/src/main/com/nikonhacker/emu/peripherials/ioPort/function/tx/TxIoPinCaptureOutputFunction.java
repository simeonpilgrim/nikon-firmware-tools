package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractOutputPinFunction;

public class TxIoPinCaptureOutputFunction extends AbstractOutputPinFunction{
    private int timerNumber;

    public TxIoPinCaptureOutputFunction(int timerNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.timerNumber = timerNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " Capture and trigger " + timerNumber + " output";
    }

    @Override
    public String getShortName() {
        return "TC" + timerNumber + "OUT";
    }

    @Override
    public Integer getValue(Integer defaultOutputValue) {
        if (logPinMessages) System.out.println("TxIoPinCaptureOutputFunction.getValue not implemented for pin " + getShortName());
        return null;
    }
}
