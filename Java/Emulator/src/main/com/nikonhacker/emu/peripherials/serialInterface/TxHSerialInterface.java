package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public class TxHSerialInterface extends TxSerialInterface {
    public TxHSerialInterface(int serialInterfaceNumber, InterruptController interruptController, int baseInterruptNumber) {
        super(serialInterfaceNumber, interruptController, baseInterruptNumber);
    }

    @Override
    public String getName() {
        return "HSerial #" + serialInterfaceNumber;
    }
}
