package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinCaptureInputFunction extends AbstractInputPinFunction {
    private int timerNumber;

    public TxIoPinCaptureInputFunction(int timerNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.timerNumber = timerNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " Capture and trigger " + timerNumber + " input";
    }

    @Override
    public String getShortName() {
        return "TC" + timerNumber + "IN";
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages) System.out.println("TxIoPinCaptureInputFunction.setValue not implemented for pin " + getShortName());
    }
}
