package com.nikonhacker.emu.peripherials.ioPort.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.memory.listener.fr.ExpeedPinIoListener;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.fr.FrIoPinInputFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.fr.FrIoPinInterruptFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.fr.FrIoPinOutputFunction;

public class FrIoPort extends IoPort {

    public FrIoPort(int portNumber, InterruptController interruptController, boolean logPinMessages) {
        super(Constants.CHIP_FR, portNumber, interruptController, logPinMessages);
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            inputFunctions[bitNumber] = new FrIoPinInputFunction(getShortName() + bitNumber);
            inputFunctions[bitNumber].setLogPinMessages(logPinMessages);
            outputFunctions[bitNumber] = new FrIoPinOutputFunction(getShortName() + bitNumber);
            outputFunctions[bitNumber].setLogPinMessages(logPinMessages);
        }
    }

    public static IoPort[] setupPorts(InterruptController interruptController, boolean logPinMessages) {
        FrIoPort[] ioPorts = new FrIoPort[ExpeedPinIoListener.NUM_PORT];
        for (int portNumber = 0; portNumber < ioPorts.length; portNumber++) {
            ioPorts[portNumber] = new FrIoPort(portNumber, interruptController, logPinMessages);
            ioPorts[portNumber].setInputEnabled((byte) 0x00);
        }

        // Configure port 0 for output (we know for sure P05 aka 0x50000100.bit5 is output for serial. No idea for the rest)
        ioPorts[IoPort.PORT_0].setDirection((byte) 0xFF);
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            ioPorts[IoPort.PORT_0].getPin(bitNumber).setFunction(ioPorts[IoPort.PORT_0].outputFunctions[bitNumber]);
        }

        // Configure port 7 for input (we know for sure P76 aka 0x50000107.bit6 is input for serial. No idea for the rest)
        ioPorts[IoPort.PORT_7].setDirection((byte) 0x00);
        ioPorts[IoPort.PORT_7].setInputEnabled((byte) 0xFF);
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            ioPorts[IoPort.PORT_7].getPin(bitNumber).setFunction(ioPorts[IoPort.PORT_7].inputFunctions[bitNumber]);
        }
        // Override Port7.pin6 as external interrupt trigger channel 6 (interrupt 0x16)
        ioPorts[IoPort.PORT_7].getPin(6).setFunction(new FrIoPinInterruptFunction(interruptController, 6));

        return ioPorts;
    }
}
