package com.nikonhacker.emu.peripherials.ioPort.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.memory.listener.fr.ExpeedPinIoListener;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.fr.FrIoPinInputFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.fr.FrIoPinInterruptFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.fr.FrIoPinOutputFunction;

public class FrIoPort extends IoPort {

    private byte functionRegister;
    private byte valueLatch;

    public FrIoPort(int portNumber, InterruptController interruptController, boolean logPinMessages) {
        super(Constants.CHIP_FR, portNumber, interruptController, logPinMessages);
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            inputFunctions[bitNumber] = new FrIoPinInputFunction(getShortName() + bitNumber);
            inputFunctions[bitNumber].setLogPinMessages(logPinMessages);
            outputFunctions[bitNumber] = new FrIoPinOutputFunction(getShortName() + bitNumber);
            outputFunctions[bitNumber].setLogPinMessages(logPinMessages);
        }
    }

    @Override
    public void setValue(byte newValue) {
        // latch value
        valueLatch = newValue;
        super.setValue(newValue);
    }

    public byte getFunctionRegister() {
        return functionRegister;
    }

    public void setFunctionRegister(byte functionRegister) {
        this.functionRegister = functionRegister;

        setDirectionOutput(functionRegister);
        setInputEnabled((byte) ((~functionRegister) & 0xFF) );

        for (int pinNumber = 0; pinNumber < 8; pinNumber++) {
            if ( (functionRegister & (1 << pinNumber))!=0) {
                pins[pinNumber].setFunction(outputFunctions[pinNumber]);
            } else {
                pins[pinNumber].setFunction(inputFunctions[pinNumber]);
            }
        }
        // retry, because firmware set value and afterwards function
        super.setValue(valueLatch);
    }

    public static IoPort[] setupPorts(InterruptController interruptController, boolean logPinMessages) {
        FrIoPort[] ioPorts = new FrIoPort[ExpeedPinIoListener.NUM_PORT];

        int portNumber;

        // Initialize everything as disabled by default
        for (portNumber = 0; portNumber < ioPorts.length; portNumber++) {
            ioPorts[portNumber] = new FrIoPort(portNumber, interruptController, logPinMessages);
        }

        // D5100 specific: Override Port7.pin6 as external interrupt trigger channel 6 (interrupt 0x16)
        ioPorts[IoPort.PORT_7].inputFunctions[6] = new FrIoPinInterruptFunction(interruptController, 6);

        // default all inputs
        for (portNumber = 0; portNumber < ioPorts.length; portNumber++) {
            ioPorts[portNumber].setFunctionRegister((byte)0);
        }
        return ioPorts;
    }
}
