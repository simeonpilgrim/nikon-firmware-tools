package com.nikonhacker.emu.peripherials.ioPort.function.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinInputFunction;

public class FrIoPinInputFunction extends IoPinInputFunction {
    public FrIoPinInputFunction(String pinName) {
        super(Constants.CHIP_LABEL[Constants.CHIP_FR], pinName);
    }
}
