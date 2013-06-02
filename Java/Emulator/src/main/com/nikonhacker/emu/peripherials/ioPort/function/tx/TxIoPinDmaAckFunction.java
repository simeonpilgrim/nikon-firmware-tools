package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinDmaAckFunction extends AbstractInputPinFunction implements PinFunction {
    private int channelNumber;

    public TxIoPinDmaAckFunction(int channelNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.channelNumber = channelNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " DMA Ack (channel " + channelNumber + ")";
    }

    @Override
    public String getShortName() {
        return "DACK" + channelNumber;
    }

    @Override
    public void setValue(int value) {
        if (IoPort.DEBUG) System.out.println("TxIoPinDmaAckFunction.setValue not implemented for pin " + getShortName());
    }

}
