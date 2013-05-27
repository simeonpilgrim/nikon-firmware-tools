package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinDmaAckHandler implements TxIoPinHandler {
    private int channelNumber;

    public TxIoPinDmaAckHandler(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    @Override
    public String toString() {
        return "DMA Ack (channel " + channelNumber + ")";
    }

    @Override
    public String getPinName() {
        return "DACK" + channelNumber;
    }
}
