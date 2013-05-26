package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinInterruptFunction extends AbstractInputPinFunction implements PinFunction {
    private InterruptController interruptController;
    private int interruptNumber;

    public TxIoPinInterruptFunction(InterruptController interruptController, int interruptNumber) {
        this.interruptController = interruptController;
        this.interruptNumber = interruptNumber;
    }

    @Override
    public String getFullName() {
        return "Interrupt 0x" + Format.asHex(interruptNumber, 1) + " (" + getShortName() + ")";
    }

    @Override
    public String getShortName() {
        return TxInterruptController.hardwareInterruptDescription[interruptNumber].symbolicName;
    }

    @Override
    public void setValue(int value) {
        //Test according to edge detection
        int interruptActiveState = ((TxInterruptController) interruptController).getRequestActiveState(interruptNumber);

        // Note that setValue() is only called in case of actual value change, so no risk of triggering multiple times on Level (hi, lo)
        // TODO: test of changed value could be done here instead of at the higher level, at the cost of a slightly lower performance.
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
    }

}
