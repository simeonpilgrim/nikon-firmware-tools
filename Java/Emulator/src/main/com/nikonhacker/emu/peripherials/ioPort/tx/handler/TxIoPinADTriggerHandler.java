package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinADTriggerHandler implements TxIoPinHandler {
    private char unit;

    public TxIoPinADTriggerHandler(char unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "A/D Trigger (unit " + unit + ")";
    }

    public String getPinName() {
        return "ADTRG" + unit;
    }
}
