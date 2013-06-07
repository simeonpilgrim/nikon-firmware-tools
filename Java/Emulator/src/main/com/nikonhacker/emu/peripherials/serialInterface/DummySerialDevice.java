package com.nikonhacker.emu.peripherials.serialInterface;

public class DummySerialDevice implements SerialDevice {
    @Override
    public void write(Integer value) {
        // ignore
    }

    @Override
    public void connectSerialDevice(SerialDevice connectedDevice) {
        // ignore
    }

    @Override
    public void disconnectSerialDevice() {
        // ignore
    }

    @Override
    public SerialDevice getConnectedSerialDevice() {
        return null;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        // ignore
    }

    @Override
    public String toString() {
        return "DummySerialDevice";
    }
}
