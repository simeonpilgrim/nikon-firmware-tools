package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinCaptureInputHandler implements TxIoPinHandler {
    private int timerNumber;

    public TxIoPinCaptureInputHandler(int timerNumber) {
        this.timerNumber = timerNumber;
    }

    @Override
    public String toString() {
        return "Capture and trigger " + timerNumber + " input";
    }

    @Override
    public String getPinName() {
        return "TC" + timerNumber + "IN";
    }
}
