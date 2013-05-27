package com.nikonhacker.emu.peripherials.ioPort.function.tx;


import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinADTriggerFunction extends AbstractInputPinFunction implements PinFunction {
    private char unit;

    public TxIoPinADTriggerFunction(char unit) {
        this.unit = unit;
    }

    @Override
    public String getFullName() {
        return "A/D Trigger (unit " + unit + ")";
    }

    @Override
    public String getShortName() {
        return "ADTRG" + unit;
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }
}
