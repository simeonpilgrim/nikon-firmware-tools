package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

import com.nikonhacker.emu.memory.listener.tx.TxIoListener;

public class TxIoPinSerialClockHandler implements TxIoPinHandler {
    private int serialInterfaceNumber;

    public TxIoPinSerialClockHandler(int serialInterfaceNumber) {
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String toString() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HSerial Clock " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "Serial Clock " + serialInterfaceNumber;
        }
    }
}
