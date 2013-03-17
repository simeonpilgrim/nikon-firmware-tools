package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinTimerOutputHandler implements TxIoPinHandler {
    private int timerNumber;
    private int outputNumber;

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
}
