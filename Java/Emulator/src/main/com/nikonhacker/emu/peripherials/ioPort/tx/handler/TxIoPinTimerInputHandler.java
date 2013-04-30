package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinTimerInputHandler implements TxIoPinHandler {
    private int timerNumber;
    private int inputNumber;

    public TxIoPinTimerInputHandler(int timerNumber, int inputNumber) {
        this.timerNumber = timerNumber;
        this.inputNumber = inputNumber;
    }

    public TxIoPinTimerInputHandler(int timerNumber) {
        this.timerNumber = timerNumber;
    }

    @Override
    public String toString() {
        return "Timer " + timerNumber + " input " + inputNumber;
    }

    @Override
    public String getPinName() {
        return "TB" + timerNumber + "IN" + (inputNumber == -1?"":(""+inputNumber));
    }
}
