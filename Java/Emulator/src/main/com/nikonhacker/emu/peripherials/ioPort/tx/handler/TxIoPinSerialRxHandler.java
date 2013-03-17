package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

import com.nikonhacker.emu.memory.listener.tx.TxIoListener;

public class TxIoPinSerialRxHandler implements TxIoPinHandler {
    private int serialInterfaceNumber;

    public TxIoPinSerialRxHandler(int serialInterfaceNumber) {
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String toString() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HSerial Rx " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "Serial Rx " + serialInterfaceNumber;
        }
    }
}
