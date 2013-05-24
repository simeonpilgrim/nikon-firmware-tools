package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.ioPort.function.OutputPinFunction;

public class TxIoPinSerialTxFunction extends OutputPinFunction {
    private int serialInterfaceNumber;

    public TxIoPinSerialTxFunction(int serialInterfaceNumber) {
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String getFullName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HSerial Tx " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "Serial Tx " + serialInterfaceNumber;
        }
    }

    @Override
    public String getShortName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HTXD" + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "TXD" + serialInterfaceNumber;
        }
    }
}
