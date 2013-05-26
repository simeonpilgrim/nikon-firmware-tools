package com.nikonhacker.emu.peripherials.ioPort.fr;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.memory.listener.fr.ExpeedPinIoListener;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;

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

        return ioPorts;
    }
}
