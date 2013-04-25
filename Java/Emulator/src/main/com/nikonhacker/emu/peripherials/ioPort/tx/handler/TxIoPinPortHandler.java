package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinPortHandler implements TxIoPinHandler {
    @Override
    public String toString() {
        return "Port";
    }

    @Override
    public String getPinName() {
        return "PORT";
    }
}
