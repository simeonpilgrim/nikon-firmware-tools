package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinInterruptFunction extends AbstractInputPinFunction implements PinFunction {
    private int interruptNumber;

    public TxIoPinInterruptFunction(int interruptNumber) {
        this.interruptNumber = interruptNumber;
    }

    @Override
    public String getFullName() {
        return "Interrupt 0x" + Format.asHex(interruptNumber, 1);
    }

    @Override
    public String getShortName() {
        return "INT" + Format.asHex(interruptNumber, 1);
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
