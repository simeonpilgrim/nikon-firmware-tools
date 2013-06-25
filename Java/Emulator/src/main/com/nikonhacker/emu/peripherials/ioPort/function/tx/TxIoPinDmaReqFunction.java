package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinDmaReqFunction extends AbstractInputPinFunction implements PinFunction {
    private int channelNumber;

    public TxIoPinDmaReqFunction(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    @Override
    public String getFullName() {
        return "DMA Req (channel " + channelNumber + ")";
    }

    @Override
    public String getShortName() {
        return "DREQ" + channelNumber;
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
