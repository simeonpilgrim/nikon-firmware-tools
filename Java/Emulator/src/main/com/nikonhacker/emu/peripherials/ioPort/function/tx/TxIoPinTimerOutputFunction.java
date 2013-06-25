package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.ioPort.function.OutputPinFunction;

public class TxIoPinTimerOutputFunction extends OutputPinFunction {
    private int timerNumber;

    public TxIoPinTimerOutputFunction(int timerNumber) {
        this.timerNumber = timerNumber;
    }

    @Override
    public String getFullName() {
        return "Timer " + Format.asHex(timerNumber,1) + " output";
    }

    @Override
    public String getShortName() {
        return "TB" + Format.asHex(timerNumber,1) + "OUT";
    }

    public void setValue(boolean value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
