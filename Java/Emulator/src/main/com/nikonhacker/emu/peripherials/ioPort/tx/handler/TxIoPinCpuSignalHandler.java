package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

public class TxIoPinCpuSignalHandler implements TxIoPinHandler {
    private int signalNumber;

    public TxIoPinCpuSignalHandler(int signalNumber) {
        this.signalNumber = signalNumber;
    }

    @Override
    public String toString() {
        return "Cpu Signal";
    }

    @Override
    public String getPinName() {
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
}
