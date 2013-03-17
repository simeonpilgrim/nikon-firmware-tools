package com.nikonhacker.emu.peripherials.ioPort.tx.handler;

import com.nikonhacker.emu.memory.listener.tx.TxIoListener;

public class TxIoPinSerialTxHandler implements TxIoPinHandler {
    private int serialInterfaceNumber;

    public TxIoPinSerialTxHandler(int serialInterfaceNumber) {
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String toString() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HSerial Tx " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "Serial Tx " + serialInterfaceNumber;
        }
    }
}
