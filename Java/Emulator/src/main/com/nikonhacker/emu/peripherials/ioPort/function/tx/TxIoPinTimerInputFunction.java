package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinTimerInputFunction extends AbstractInputPinFunction implements PinFunction {
    private int timerNumber;
    private int inputNumber;

    public TxIoPinTimerInputFunction(int timerNumber, int inputNumber) {
        this.timerNumber = timerNumber;
        this.inputNumber = inputNumber;
    }

    public TxIoPinTimerInputFunction(int timerNumber) {
        this.timerNumber = timerNumber;
    }

    @Override
    public String getFullName() {
        return "Timer " + timerNumber + " input " + inputNumber;
    }

    @Override
    public String getShortName() {
        return "TB" + timerNumber + "IN" + (inputNumber == -1?"":(""+inputNumber));
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
