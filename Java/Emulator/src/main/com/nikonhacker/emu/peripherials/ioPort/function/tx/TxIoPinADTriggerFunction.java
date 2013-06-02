package com.nikonhacker.emu.peripherials.ioPort.function.tx;


import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinADTriggerFunction extends AbstractInputPinFunction implements PinFunction {
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
        if (IoPort.DEBUG) System.out.println("TxIoPinADTriggerFunction.setValue not implemented for pin " + getShortName());
    }
}
