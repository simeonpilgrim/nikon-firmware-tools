package com.nikonhacker.emu.peripherials.ioPort.function.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinOutputFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class FrIoPinOutputFunction extends IoPinOutputFunction implements PinFunction {
    public FrIoPinOutputFunction(String pinName) {
        super(Constants.CHIP_LABEL[Constants.CHIP_FR], pinName);
    }
}
