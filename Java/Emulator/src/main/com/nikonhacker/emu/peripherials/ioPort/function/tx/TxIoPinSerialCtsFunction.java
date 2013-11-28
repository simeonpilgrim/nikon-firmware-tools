package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TxIoPinSerialCtsFunction extends AbstractInputPinFunction {
    private int serialInterfaceNumber;

    public TxIoPinSerialCtsFunction(int serialInterfaceNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String getFullName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return componentName + " HSerial CTS " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return componentName + " Serial CTS " + serialInterfaceNumber;
        }
    }

    @Override
    public String getShortName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return "HCTS" + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return "CTS" + serialInterfaceNumber;
        }
    }

    @Override
    public void setValue(int value) {
        if (logPinMessages) System.out.println("TxIoPinSerialCtsFunction.setValue not implemented for pin " + getShortName());
    }

}
