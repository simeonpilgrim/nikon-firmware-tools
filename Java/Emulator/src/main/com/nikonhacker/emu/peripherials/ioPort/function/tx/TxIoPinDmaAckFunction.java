package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinDmaAckFunction extends AbstractInputPinFunction implements PinFunction {
    private int channelNumber;

    public TxIoPinDmaAckFunction(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    @Override
    public String getFullName() {
        return "DMA Ack (channel " + channelNumber + ")";
    }

    @Override
    public String getShortName() {
        return "DACK" + channelNumber;
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
