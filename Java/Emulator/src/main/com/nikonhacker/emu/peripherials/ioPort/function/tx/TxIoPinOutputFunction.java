package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinOutputFunction;

public class TxIoPinOutputFunction extends IoPinOutputFunction {
    public TxIoPinOutputFunction(String pinName) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX], pinName);
    }
}
