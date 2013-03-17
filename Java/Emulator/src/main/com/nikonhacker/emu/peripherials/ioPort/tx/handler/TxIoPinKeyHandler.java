package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinKeyHandler implements TxIoPinHandler {
    private int keyNumber;

    public TxIoPinKeyHandler(int keyNumber) {
        this.keyNumber = keyNumber;
    }

    @Override
    public String toString() {
        return "Key " + keyNumber;
    }
}
