package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinTimerOutputHandler implements TxIoPinHandler {
    private int timerNumber;
    private int outputNumber = -1;

    public TxIoPinTimerOutputHandler(int timerNumber) {
        this.timerNumber = timerNumber;
    }

    public TxIoPinTimerOutputHandler(int timerNumber, int outputNumber) {
        this.timerNumber = timerNumber;
        this.outputNumber = outputNumber;
    }

    @Override
    public String toString() {
        return "Timer " + timerNumber + " output " + outputNumber;
    }

    @Override
    public String getPinName() {
        return "TB" + timerNumber + "OUT" + (outputNumber == -1?"":(""+outputNumber));
    }
}
