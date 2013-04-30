package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinCaptureOutputHandler implements TxIoPinHandler {
    private int timerNumber;

    public TxIoPinCaptureOutputHandler(int timerNumber) {
        this.timerNumber = timerNumber;
    }

    @Override
    public String toString() {
        return "Capture and trigger " + timerNumber + " output";
    }

    @Override
    public String getPinName() {
        return "TC" + timerNumber + "OUT";
    }
}
