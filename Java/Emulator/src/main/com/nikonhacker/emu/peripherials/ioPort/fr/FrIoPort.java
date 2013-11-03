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
        // Initialize everything as disabled by default
        for (int portNumber = 0; portNumber < ioPorts.length; portNumber++) {
            ioPorts[portNumber] = new FrIoPort(portNumber, interruptController, logPinMessages);
            ioPorts[portNumber].setInputEnabled((byte) 0x00);
        }

        // Configure port
        // - P07 aka 0x50000100_0x80 is output (eg set @001089B2 and cleared @001089E0). It could be LCD power on
        // - P05 aka 0x50000100.bit5 is output for serial.
        // - P04 is output (eg set @001E7F56 and cleared @001E7334)
        // - P03 = LCD rotated around the horizontal axis (1 when lcd is ready to be put in storage position)
        // - P02 is input (eg tested @001F655C)
        // - P00 = LCD fully open (0 when fully open)
        // No idea for the rest
        // Let's assume 4 hi bits OUT, and 4 lo bits IN
        ioPorts[IoPort.PORT_0].setDirectionOutput((byte) 0xF0);
        ioPorts[IoPort.PORT_0].setInputEnabled((byte) 0x0F);
        for (int bitNumber = 0; bitNumber < 4; bitNumber++) {
            ioPorts[IoPort.PORT_0].getPin(bitNumber).setFunction(ioPorts[IoPort.PORT_0].inputFunctions[bitNumber]);
        }
        for (int bitNumber = 4; bitNumber < 8; bitNumber++) {
            ioPorts[IoPort.PORT_0].getPin(bitNumber).setFunction(ioPorts[IoPort.PORT_0].outputFunctions[bitNumber]);
        }

        // Configure port 7 for input
        // - P76 aka 0x50000107.bit6 is input for serial.
        // - P75 aka 0x50000107.bit5 is input (eg tested @00113D54)
        // No idea for the rest
        ioPorts[IoPort.PORT_7].setDirectionOutput((byte) 0x00);
        ioPorts[IoPort.PORT_7].setInputEnabled((byte) 0xFF);
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            ioPorts[IoPort.PORT_7].getPin(bitNumber).setFunction(ioPorts[IoPort.PORT_7].inputFunctions[bitNumber]);
        }
        // Override Port7.pin6 as external interrupt trigger channel 6 (interrupt 0x16)
        ioPorts[IoPort.PORT_7].getPin(6).setFunction(new FrIoPinInterruptFunction(interruptController, 6));

        // P84 = LCD closed (0 when closed)
        ioPorts[IoPort.PORT_8].setInputEnabled((byte) 0xFF);
        // Does not work When the LCD turns off => P05 and PD1 go to 0, and PC3 goes to 1
        ioPorts[IoPort.PORT_C].setDirectionOutput((byte) 0xFF);
        ioPorts[IoPort.PORT_D].setDirectionOutput((byte) 0xFF);
        return ioPorts;
    }
}
