package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinSbiClockHandler implements TxIoPinHandler {
    @Override
    public String toString() {
        return "SBI Clock";
    }

    @Override
    public String getPinName() {
        return "SCK";
    }
}
