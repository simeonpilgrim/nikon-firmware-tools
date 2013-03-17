package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinDmaReqHandler implements TxIoPinHandler {
    private int channelNumber;

    public TxIoPinDmaReqHandler(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    @Override
    public String toString() {
        return "DMA Req (channel " + channelNumber + ")";
    }
}
