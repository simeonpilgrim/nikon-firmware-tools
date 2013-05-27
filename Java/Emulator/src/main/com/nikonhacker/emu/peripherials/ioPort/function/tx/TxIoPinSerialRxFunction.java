package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinSerialRxFunction extends AbstractInputPinFunction implements PinFunction {
    private int serialInterfaceNumber;

    public TxIoPinSerialRxFunction(int serialInterfaceNumber) {
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String getFullName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HSerial Rx " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "Serial Rx " + serialInterfaceNumber;
        }
    }

    @Override
    public String getShortName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HRXD" + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "RXD" + serialInterfaceNumber;
        }
    }

    @Override
    public void setValue(int value) {
        System.err.println(toString() + " - Setting value is not implemented");
    }

}
