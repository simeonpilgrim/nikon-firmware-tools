package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.ioPort.function.OutputPinFunction;

public class TxIoPinSerialClockFunction extends OutputPinFunction {
    private int serialInterfaceNumber;

    public TxIoPinSerialClockFunction(int serialInterfaceNumber) {
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String getFullName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HSerial Clock " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "Serial Clock " + serialInterfaceNumber;
        }
    }

    @Override
    public String getShortName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HSCLK" + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "SCLK" + serialInterfaceNumber;
        }
    }
}
