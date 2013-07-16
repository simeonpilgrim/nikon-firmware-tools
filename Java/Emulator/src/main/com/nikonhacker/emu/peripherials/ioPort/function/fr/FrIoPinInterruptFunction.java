package com.nikonhacker.emu.peripherials.ioPort.function.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinInterruptFunction;

public class FrIoPinInterruptFunction extends IoPinInterruptFunction {

    private int externalInterruptChannel;

    public FrIoPinInterruptFunction(InterruptController interruptController, int externalInterruptChannel) {
        super(Constants.CHIP_LABEL[Constants.CHIP_FR], interruptController,
                FrInterruptController.INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET + externalInterruptChannel);
        this.externalInterruptChannel = externalInterruptChannel;
    }

    @Override
    public String getShortName() {
        return componentName + " " + FrInterruptController.getInterruptShortName(interruptNumber);
    }

    @Override
    public void setValue(int value) {
        ((FrInterruptController)interruptController).setExternalInterruptChannelValue(externalInterruptChannel, value);
    }
}
