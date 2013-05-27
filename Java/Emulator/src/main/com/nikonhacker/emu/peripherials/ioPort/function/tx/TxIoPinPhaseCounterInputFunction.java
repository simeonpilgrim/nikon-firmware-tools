package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinPhaseCounterInputFunction extends AbstractInputPinFunction implements PinFunction {
    private final int phaseCounterNumber;
    private final int inputNumber;

    public TxIoPinPhaseCounterInputFunction(int phaseCounterNumber, int inputNumber) {
        this.phaseCounterNumber = phaseCounterNumber;
        this.inputNumber = inputNumber;
    }

    @Override
    public String getFullName() {
        return "Phase Counter " + phaseCounterNumber + " input " + inputNumber;
    }

    @Override
    public String getShortName() {
        return "PHC" + phaseCounterNumber + "IN" + inputNumber;
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
