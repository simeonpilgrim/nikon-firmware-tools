package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinInterruptFunction;

public class TxIoPinInterruptFunction extends IoPinInterruptFunction {
    private int previousValue = -1;

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
        if (previousValue != value) {
            //Test according to configured edge detection
            int interruptActiveState = ((TxInterruptController) interruptController).getRequestActiveState(interruptNumber);

            // TODO: if active high/low, it means it should be triggered upon interrupt enable, even if we have no edge at that time.
            if (interruptActiveState == TxInterruptController.ACTIVE_HIGH || interruptActiveState == TxInterruptController.ACTIVE_RISING) {
                if (value != 0) {
                    interruptController.request(interruptNumber);
                }
            }
            else {
                if (value == 0) {
                    interruptController.request(interruptNumber);
                }
            }
            previousValue = value;
        }
    }
}
