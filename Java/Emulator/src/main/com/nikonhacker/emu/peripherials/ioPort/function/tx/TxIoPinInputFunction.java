package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinInputFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinInputFunction extends IoPinInputFunction implements PinFunction {
    public TxIoPinInputFunction(String pinName) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX], pinName);
    }
}
