package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public class TxSerialInterface extends SerialInterface {
    @Override
    public Integer read() {
        return null;
    }

    @Override
    public void write(int value) {

    }

    @Override
    public int getNbBits() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public TxSerialInterface(int serialInterfaceNumber, InterruptController interruptController, int baseInterruptNumber) {
        super(serialInterfaceNumber, interruptController);
    }
}
