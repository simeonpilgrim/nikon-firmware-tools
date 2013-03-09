package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.Format;

/**
 * Generic serial test device
 */
public class TestDevice implements SerialDevice {
    private final String deviceName;
    private SerialDevice connectedDevice;

    public TestDevice(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public void write(Integer value) {
        System.err.println("        " + deviceName + " receives 0x" + Format.asHex(value,2));
    }

    @Override
    public void connectSerialDevice(SerialDevice connectedDevice) {
        this.connectedDevice = connectedDevice;
    }

    @Override
    public void disconnectSerialDevice() {
        this.connectedDevice = new DummySerialDevice();
    }

    @Override
    public SerialDevice getConnectedSerialDevice() {
        return connectedDevice;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int nbBits) {
        System.out.println("TestDevice.onBitNumberChange(" + serialDevice + ", " + nbBits + ");");
    }

    public void sendBytes() {
        System.err.println(deviceName + " writes 0x01");
        connectedDevice.write(0x01);
        System.err.println(deviceName + " writes 0x02");
        connectedDevice.write(0x02);
        System.err.println(deviceName + " writes 0x03");
        connectedDevice.write(0x03);
        System.err.println(deviceName + " writes 0x04");
        connectedDevice.write(0x04);
    }
}
