package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractOutputPinFunction;

public class TxIoPinSerialClockFunction extends AbstractOutputPinFunction {
    private int serialInterfaceNumber;

    public TxIoPinSerialClockFunction(int serialInterfaceNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.serialInterfaceNumber = serialInterfaceNumber;
    }

    @Override
    public String getFullName() {
        if (serialInterfaceNumber >= TxIoListener.NUM_SERIAL_IF) {
            return componentName + " HSerial Clock " + (serialInterfaceNumber - TxIoListener.NUM_SERIAL_IF);
        }
        else {
            return componentName + " Serial Clock " + serialInterfaceNumber;
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

    @Override
    public Integer getValue(Integer defaultOutputValue) {
        if (logPinMessages) System.out.println("TxIoPinSerialClockFunction.getValue not implemented for pin " + getShortName());
        return null;
    }
}
