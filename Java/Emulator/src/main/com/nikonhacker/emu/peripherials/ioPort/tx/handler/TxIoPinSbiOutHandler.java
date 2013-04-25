package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinSbiOutHandler implements TxIoPinHandler {
    @Override
    public String toString() {
        return "SBI Out";
    }

    @Override
    public String getPinName() {
        return "SO/SDA";
    }
}
