package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.keyCircuit.tx.TxKeyCircuit;

public class TxIoPinKeyFunction extends AbstractInputPinFunction {
    private Platform platform;
    private int      keyNumber;
    private int      previousValue = -1;

    public TxIoPinKeyFunction(Platform platform, int keyNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.platform = platform;
        this.keyNumber = keyNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " Key " + keyNumber;
    }

    @Override
    public String getShortName() {
        return "KEY" + ((keyNumber<10)?"0":"") + keyNumber;
    }

    @Override
    public void setValue(int value) {
        if (previousValue != value) {
            ((TxKeyCircuit)platform.getKeyCircuit()).keys[keyNumber].setValue(value);
            previousValue = value;
        }
    }

}
