package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinDmaReqFunction extends AbstractInputPinFunction {
    private int channelNumber;

    public TxIoPinDmaReqFunction(int channelNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.channelNumber = channelNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " DMA Req (channel " + channelNumber + ")";
    }

    @Override
    public String getShortName() {
        return "DREQ" + channelNumber;
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages) System.out.println("TxIoPinDmaReqFunction.setValue not implemented for pin " + getShortName());
    }

}
