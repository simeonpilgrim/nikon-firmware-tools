package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinADTriggerSyncFunction extends AbstractInputPinFunction implements PinFunction {
    @Override
    public String getFullName() {
        return "A/D Trigger Sync";
    }

    @Override
    public String getShortName() {
        return "ADTRGSNC";
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }
}
