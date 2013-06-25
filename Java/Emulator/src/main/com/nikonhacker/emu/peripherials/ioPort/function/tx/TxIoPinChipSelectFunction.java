package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinChipSelectFunction extends AbstractInputPinFunction {
    private int blockNumber;

    public TxIoPinChipSelectFunction(int blockNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.blockNumber = blockNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " Chip Select (block space " + blockNumber + ")";
    }

    @Override
    public String getShortName() {
        return "CS" + blockNumber;
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages) System.out.println("TxIoPinChipSelectFunction.setValue not implemented for pin " + getShortName());
    }

}
