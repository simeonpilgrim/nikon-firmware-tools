package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinPhaseCounterInputHandler implements TxIoPinHandler {
    private final int phaseCounterNumber;
    private final int inputNumber;

    public TxIoPinPhaseCounterInputHandler(int phaseCounterNumber, int inputNumber) {
        this.phaseCounterNumber = phaseCounterNumber;
        this.inputNumber = inputNumber;
    }

    @Override
    public String toString() {
        return "Phase Counter " + phaseCounterNumber + " input " + inputNumber;
    }
}
