package com.nikonhacker.emu.peripherials.serialInterface.tx;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public class TxHSerialInterface extends TxSerialInterface {
    public TxHSerialInterface(int serialInterfaceNumber, InterruptController interruptController) {
        super(serialInterfaceNumber, interruptController);
    }

    @Override
    public String getName() {
        return "HSerial #" + serialInterfaceNumber;
    }
}
