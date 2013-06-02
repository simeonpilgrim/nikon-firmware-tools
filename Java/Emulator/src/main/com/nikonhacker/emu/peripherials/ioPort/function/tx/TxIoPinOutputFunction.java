package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinOutputFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinOutputFunction extends IoPinOutputFunction implements PinFunction {
    public TxIoPinOutputFunction(String pinName) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX], pinName);
    }
}
