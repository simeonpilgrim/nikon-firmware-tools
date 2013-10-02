package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinCpuSignalFunction extends AbstractInputPinFunction {
    private int signalNumber;

    public TxIoPinCpuSignalFunction(int signalNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.signalNumber = signalNumber;
    }

    @Override
    public String getFullName() {
        return componentName + " Cpu Signal (" + getShortName() + ")";
    }

    @Override
    public String getShortName() {
        switch (signalNumber) {
            case 7:
                return "ALE";
            case 6:
                return "R/!W";
            case 5:
                return "BUSAK";
            case 4:
                return "BUSRQ";
            case 3:
                return "RDY";
            case 2:
                return "!HWR";
            case 1:
                return "!WR";
            case 0:
                return "!RD";
        }
        return "(err)";
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages) System.out.println("TxIoPinCpuSignalFunction.setValue not implemented for pin " + getShortName());
    }

}
