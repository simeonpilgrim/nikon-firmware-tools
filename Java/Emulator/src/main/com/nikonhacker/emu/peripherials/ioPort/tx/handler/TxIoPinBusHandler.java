package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinBusHandler implements TxIoPinHandler {
    @Override
    public String toString() {
        return "Bus";
    }

    public String getPinName() {
        return "BUS";
    }
}
