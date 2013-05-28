package com.nikonhacker.emu.peripherials.ioPort.function.fr;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinInterruptFunction;

public class FrIoPinInterruptFunction extends IoPinInterruptFunction {

    public FrIoPinInterruptFunction(InterruptController interruptController, int interruptNumber) {
        super(interruptController, interruptNumber);
    }

    @Override
    public String getShortName() {
        return FrInterruptController.getInterruptShortName(interruptNumber);
    }

    @Override
    public void setValue(int value) {
        // Hardcoded falling edge mode
        if (value == 0) {
            interruptController.request(interruptNumber);
        }
    }
}
