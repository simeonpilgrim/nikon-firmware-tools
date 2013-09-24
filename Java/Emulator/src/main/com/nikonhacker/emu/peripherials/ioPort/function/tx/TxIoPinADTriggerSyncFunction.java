package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinADTriggerSyncFunction extends AbstractInputPinFunction {

    public TxIoPinADTriggerSyncFunction() {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
    }

    @Override
    public String getFullName() {
        return componentName + " A/D Trigger Sync";
    }

    @Override
    public String getShortName() {
        return "ADTRGSNC";
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages)  System.out.println("TxIoPinADTriggerSyncFunction.setValue not implemented for pin " + getShortName());
    }
}
