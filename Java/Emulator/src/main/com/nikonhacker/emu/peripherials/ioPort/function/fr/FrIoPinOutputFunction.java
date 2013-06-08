package com.nikonhacker.emu.peripherials.ioPort.function.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinOutputFunction;

public class FrIoPinOutputFunction extends IoPinOutputFunction {
    public FrIoPinOutputFunction(String pinName) {
        super(Constants.CHIP_LABEL[Constants.CHIP_FR], pinName);
    }
}
