package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinChipSelectFunction extends AbstractInputPinFunction implements PinFunction {
    private int blockNumber;

    public TxIoPinChipSelectFunction(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    @Override
    public String getFullName() {
        return "Chip Select (block space " + blockNumber + ")";
    }

    @Override
    public String getShortName() {
        return "CS" + blockNumber;
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
