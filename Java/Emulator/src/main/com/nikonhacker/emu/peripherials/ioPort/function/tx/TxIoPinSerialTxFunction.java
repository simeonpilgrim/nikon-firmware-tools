package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractOutputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

public class TxIoPinSerialTxFunction extends AbstractOutputPinFunction implements PinFunction {
    private int serialInterfaceNumber;

    public TxIoPinSerialTxFunction(int serialInterfaceNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String getFullName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return componentName + " HSerial Tx " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return componentName + " Serial Tx " + serialInterfaceNumber;
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

    @Override
    public Integer getValue(int defaultOutputValue) {
        if (IoPort.DEBUG) System.out.println("TxIoPinSerialTxFunction.getValue not implemented for pin " + getShortName());
        return null;
    }
}
