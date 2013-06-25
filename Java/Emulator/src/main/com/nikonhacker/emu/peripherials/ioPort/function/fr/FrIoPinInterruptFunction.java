package com.nikonhacker.emu.peripherials.ioPort.function.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinInterruptFunction;

public class FrIoPinInterruptFunction extends IoPinInterruptFunction {

    public FrIoPinInterruptFunction(InterruptController interruptController, int interruptNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_FR], interruptController, interruptNumber);
    }

    @Override
    public String getShortName() {
        return componentName + " " + FrInterruptController.getInterruptShortName(interruptNumber);
    }

    @Override
    public void setValue(int value) {
        // Hardcoded falling edge mode
        if (value == 0) {
            interruptController.request(interruptNumber);
        }
    }
}
