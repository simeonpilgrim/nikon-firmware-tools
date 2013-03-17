package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

import com.nikonhacker.emu.memory.listener.tx.TxIoListener;

public class TxIoPinSerialCtsHandler implements TxIoPinHandler {
    private int serialInterfaceNumber;

    public TxIoPinSerialCtsHandler(int serialInterfaceNumber) {
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String toString() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HSerial CTS " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "Serial CTS " + serialInterfaceNumber;
        }
    }
}
