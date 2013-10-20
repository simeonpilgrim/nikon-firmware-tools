package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.Format;

/**
 * Generic serial test device
 */
public class TestDevice extends SerialDevice {
    private final String deviceName;

    public TestDevice(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public void write(Integer value) {
        System.out.println("        " + deviceName + " receives " + ((value == null)?"null":("0x" + Format.asHex(value, 2))));
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        System.out.println("TestDevice.onBitNumberChange(" + serialDevice + ", " + numBits + ");");
    }

    public void sendBytes() {
        send(0x01);
        send(0x02);
        send(0x03);
        send(0x04);
    }

    private void send(int value) {
        System.out.println(deviceName + " writes 0x" + Format.asHex(value, 2));
        targetDevice.write(value);
    }

    @Override
    public String toString() {
        return deviceName;
    }
}
