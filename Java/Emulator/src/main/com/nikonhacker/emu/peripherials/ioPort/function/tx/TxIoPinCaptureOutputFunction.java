package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.OutputPinFunction;

public class TxIoPinCaptureOutputFunction extends OutputPinFunction  {
    private int timerNumber;

    public TxIoPinCaptureOutputFunction(int timerNumber) {
        this.timerNumber = timerNumber;
    }

    @Override
    public String getFullName() {
        return "Capture and trigger " + timerNumber + " output";
    }

    @Override
    public String getShortName() {
        return "TC" + timerNumber + "OUT";
    }
}
