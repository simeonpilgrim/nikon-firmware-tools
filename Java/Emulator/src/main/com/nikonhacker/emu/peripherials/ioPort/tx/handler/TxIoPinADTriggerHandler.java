package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinADTriggerHandler implements TxIoPinHandler {
    private int unit;

    public TxIoPinADTriggerHandler(int unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "A/D Trigger (unit " + (char)('A' - 1 + unit) + ")";
    }
}
