package com.nikonhacker.emu.peripherials.serialInterface;

public class SerialWire implements SerialDevice {
    private String wireName;
    private SerialDevice realDevice;

    public SerialWire(String wireName, SerialDevice realDevice) {
        this.wireName = wireName;
        this.realDevice = realDevice;
    }

    @Override
    public void write(Integer value) {
        realDevice.write(value);
    }

    @Override
    public void connectSerialDevice(SerialDevice connectedDevice) {
        realDevice.connectSerialDevice(connectedDevice);
    }

    @Override
    public void disconnectSerialDevice() {
        realDevice.connectSerialDevice(new DummySerialDevice());
    }

    @Override
    public SerialDevice getConnectedSerialDevice() {
        return realDevice;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int nbBits) {
    }

    public String getWireName() {
        return wireName;
    }
}
