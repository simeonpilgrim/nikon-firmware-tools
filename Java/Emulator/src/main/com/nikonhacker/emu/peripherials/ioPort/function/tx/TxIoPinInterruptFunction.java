package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinInterruptFunction;

public class TxIoPinInterruptFunction extends IoPinInterruptFunction {

    public TxIoPinInterruptFunction(InterruptController interruptController, int interruptNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX], interruptController, interruptNumber);
    }

    @Override
    public String getFullName() {
        return componentName + " Interrupt " + getShortName();
    }

    @Override
    public String getShortName() {
        return TxInterruptController.hardwareInterruptDescription[interruptNumber].symbolicName;
    }

    @Override
    public void setValue(int value) {
        ((TxInterruptController) interruptController).setInterruptChannelValue(interruptNumber,value);
    }
}
