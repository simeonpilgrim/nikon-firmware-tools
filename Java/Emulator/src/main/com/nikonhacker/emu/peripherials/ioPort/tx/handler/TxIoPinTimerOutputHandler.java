package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

import com.nikonhacker.Format;

public class TxIoPinTimerOutputHandler implements TxIoPinHandler {
    private int timerNumber;

    public TxIoPinTimerOutputHandler(int timerNumber) {
        this.timerNumber = timerNumber;
    }

    @Override
    public String toString() {
        return "Timer " + Format.asHex(timerNumber,1) + " output";
    }

    @Override
    public String getPinName() {
        return "TB" + Format.asHex(timerNumber,1) + "OUT";
    }
}
