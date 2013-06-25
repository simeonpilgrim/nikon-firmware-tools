package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinCaptureInputFunction extends AbstractInputPinFunction implements PinFunction {
    private int timerNumber;

    public TxIoPinCaptureInputFunction(int timerNumber) {
        this.timerNumber = timerNumber;
    }

    @Override
    public String getFullName() {
        return "Capture and trigger " + timerNumber + " input";
    }

    @Override
    public String getShortName() {
        return "TC" + timerNumber + "IN";
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }
}
