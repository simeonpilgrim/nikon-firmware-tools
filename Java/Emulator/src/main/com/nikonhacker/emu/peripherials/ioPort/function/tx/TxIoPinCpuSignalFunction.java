package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinCpuSignalFunction extends AbstractInputPinFunction implements PinFunction {
    private int signalNumber;

    public TxIoPinCpuSignalFunction(int signalNumber) {
        this.signalNumber = signalNumber;
    }

    @Override
    public String getFullName() {
        return "Cpu Signal";
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
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
