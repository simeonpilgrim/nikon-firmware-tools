package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

import com.nikonhacker.Format;

public class TxIoPinInterruptHandler implements TxIoPinHandler {
    private int interruptNumber;

    public TxIoPinInterruptHandler(int interruptNumber) {
        this.interruptNumber = interruptNumber;
    }

    @Override
    public String toString() {
        return "Interrupt 0x" + Format.asHex(interruptNumber, 1);
    }

    @Override
    public String getPinName() {
        return "INT" + Format.asHex(interruptNumber, 1);
    }
}
