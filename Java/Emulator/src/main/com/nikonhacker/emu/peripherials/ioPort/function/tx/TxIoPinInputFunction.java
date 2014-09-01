package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.IoPinInputFunction;

public class TxIoPinInputFunction extends IoPinInputFunction {
    private AbstractInputPinFunction couple;

    public TxIoPinInputFunction(String pinName) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX], pinName);
    }

    @Override
    public void setValue(int value) {
        // coderat: mode dial (PF4-7) is set as "port", but polled in PKEY
        // possible according to datasheet
        if (couple!=null)
            couple.setValue(value);
    }

    public final void setCouple(AbstractInputPinFunction couple) {
        this.couple = couple;
    }
}
