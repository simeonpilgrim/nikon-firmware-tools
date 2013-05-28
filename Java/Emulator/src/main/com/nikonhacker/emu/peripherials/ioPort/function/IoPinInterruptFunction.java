package com.nikonhacker.emu.peripherials.ioPort.function;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public abstract class IoPinInterruptFunction extends AbstractInputPinFunction implements PinFunction {
    protected InterruptController interruptController;
    protected int                 interruptNumber;

    public IoPinInterruptFunction(InterruptController interruptController, int interruptNumber) {
        this.interruptController = interruptController;
        this.interruptNumber = interruptNumber;
    }

    @Override
    public String getFullName() {
        return "Interrupt 0x" + Format.asHex(interruptNumber, 1) + " (" + getShortName() + ")";
    }

    @Override
    public abstract String getShortName();

    @Override
    public abstract void setValue(int value);
}
