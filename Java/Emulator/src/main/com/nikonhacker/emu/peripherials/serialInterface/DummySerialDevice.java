package com.nikonhacker.emu.peripherials.serialInterface;

public class DummySerialDevice extends SerialDevice {
    @Override
    public void write(Integer value) {
        // ignore
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        // ignore
    }

    @Override
    public String toString() {
        return "Dummy Serial Device";
    }
}
