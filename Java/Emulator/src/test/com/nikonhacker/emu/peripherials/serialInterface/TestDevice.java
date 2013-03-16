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
        System.out.println("        " + deviceName + " receives " + ((value == null)?"null":("0x" + Format.asHex(value, 2))));
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
        send(0x01);
        send(0x02);
        send(0x03);
        send(0x04);
    }

    private void send(int value) {
        System.out.println(deviceName + " writes 0x" + Format.asHex(value, 2));
        connectedDevice.write(value);
    }

    @Override
    public String toString() {
        return deviceName;
    }
}
