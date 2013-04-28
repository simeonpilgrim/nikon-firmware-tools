package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinSbiInHandler implements TxIoPinHandler {
    @Override
    public String toString() {
        return "SBI In";
    }

    @Override
    public String getPinName() {
        return "SI/SCL";
    }
}