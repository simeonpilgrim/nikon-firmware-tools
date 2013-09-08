package com.nikonhacker.emu.peripherials.ioPort.function.tx;


import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinADTriggerFunction extends AbstractInputPinFunction {
    private char unit;

    public TxIoPinADTriggerFunction(char unit) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.unit = unit;
    }

    @Override
    public String getFullName() {
        return componentName + " A/D Trigger (unit " + unit + ")";
    }

    @Override
    public String getShortName() {
        return "ADTRG" + unit;
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages) System.out.println("TxIoPinADTriggerFunction.setValue not implemented for pin " + getShortName());
    }
}
