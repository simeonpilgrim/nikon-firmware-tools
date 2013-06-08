package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinPhaseCounterInputFunction extends AbstractInputPinFunction {
    private final int phaseCounterNumber;
    private final int inputNumber;

    public TxIoPinPhaseCounterInputFunction(int phaseCounterNumber, int inputNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.phaseCounterNumber = phaseCounterNumber;
        this.inputNumber = inputNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " Phase Counter " + phaseCounterNumber + " input " + inputNumber;
    }

    @Override
    public String getShortName() {
        return "PHC" + phaseCounterNumber + "IN" + inputNumber;
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages) System.out.println("TxIoPinPhaseCounterInputFunction.setValue not implemented for pin " + getShortName());
    }

}
