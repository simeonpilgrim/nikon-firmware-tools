package com.nikonhacker.emu.peripherials.serialInterface;

public class NullSerialDevice implements SerialDevice {
    public void onValueReady(SerialInterface serialInterface) {
        // useless read to empty SerialInterface's output
        serialInterface.read();
    }

    public void onBitNumberChange(SerialInterface serialInterface, int nbBits) {
        // ignored
    }
}
