package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinTimerInputFunction extends AbstractInputPinFunction implements PinFunction {
    private int timerNumber;
    private int inputNumber;

    public TxIoPinTimerInputFunction(int timerNumber, int inputNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.timerNumber = timerNumber;
        this.inputNumber = inputNumber;
    }

    public TxIoPinTimerInputFunction(int timerNumber) {
        this(timerNumber, -1);
    }

    @Override
    public String getFullName() {
        return componentName + " Timer " + timerNumber + " input " + inputNumber;
    }

    @Override
    public String getShortName() {
        return "TB" + timerNumber + "IN" + (inputNumber == -1?"":(""+inputNumber));
    }

    @Override
    public void setValue(int value) {
        System.out.println("TxIoPinTimerInputFunction.setValue not implemented for pin " + getShortName());
    }

}
