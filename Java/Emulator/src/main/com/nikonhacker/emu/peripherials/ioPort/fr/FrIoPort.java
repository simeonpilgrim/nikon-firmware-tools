package com.nikonhacker.emu.peripherials.ioPort.fr;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.memory.listener.fr.ExpeedPinIoListener;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.fr.FrIoPinInterruptFunction;

public class FrIoPort extends IoPort {
    private Prefs prefs;

    public FrIoPort(int portNumber, InterruptController interruptController, Prefs prefs) {
        super(portNumber, interruptController);
        this.prefs = prefs;
    }

    public static IoPort[] setupPorts(InterruptController interruptController, Prefs prefs) {
        FrIoPort[] ioPorts = new FrIoPort[ExpeedPinIoListener.NUM_PORT];
        for (int i = 0; i < ioPorts.length; i++) {
            ioPorts[i] = new FrIoPort(i, interruptController, prefs);
        }
        // Statically configure port 0 for output (we know for sure bit 5 is output for serial. No idea for the rest)
        ioPorts[0].setDirection((byte) 0xFF);
        // Statically configure port 7 for input (we know for sure bit 6 is input for serial. No idea for the rest)
        ioPorts[7].setDirection((byte) 0x00);

        // Link interrupt 0x16 to Port7.pin6 (aka 0x50000107.bit6)
        ioPorts[7].getPin(6).setFunction(new FrIoPinInterruptFunction(interruptController, 0x16));

        return ioPorts;
    }
}
