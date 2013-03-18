package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinChipSelectHandler implements TxIoPinHandler {
    private int blockNumber;

    public TxIoPinChipSelectHandler(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    @Override
    public String toString() {
        return "Chip Select (block space " + blockNumber + ")";
    }

    @Override
    public String getPinName() {
        return "CS" + blockNumber;
    }
}
