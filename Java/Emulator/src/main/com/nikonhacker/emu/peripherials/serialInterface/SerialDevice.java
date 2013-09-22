package com.nikonhacker.emu.peripherials.serialInterface;

public abstract class SerialDevice {
    protected SerialDevice sourceDevice;
    protected SerialDevice targetDevice;

    protected boolean logSerialMessages = true;

    public void setLogSerialMessages(boolean logSerialMessages) {
        this.logSerialMessages = logSerialMessages;
    }

    public void connectTargetDevice(SerialDevice targetDevice) {
        this.setTargetDevice(targetDevice);
        targetDevice.setSourceDevice(this);
    }

    public SerialDevice getTargetDevice() {
        return targetDevice;
    }

    public void setTargetDevice(SerialDevice targetDevice) {
        this.targetDevice = targetDevice;
    }

    public SerialDevice getSourceDevice() {
        return sourceDevice;
    }

    public void setSourceDevice(SerialDevice sourceDevice) {
        this.sourceDevice = sourceDevice;
    }

    public static void interConnectSerialDevices(SerialDevice device1, SerialDevice device2) {
        device1.connectTargetDevice(device2);
        device2.connectTargetDevice(device1);
    }

    public abstract void onBitNumberChange(SerialDevice serialDevice, int numBits);

    public abstract void write(Integer value);
}
