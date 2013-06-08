package com.nikonhacker.emu.peripherials.serialInterface;

public abstract class AbstractSerialDevice implements SerialDevice {
    protected SerialDevice sourceDevice;
    protected SerialDevice targetDevice;

    protected boolean logSerialMessages = true;

    public void setLogSerialMessages(boolean logSerialMessages) {
        this.logSerialMessages = logSerialMessages;
    }

    @Override
    public void connectTargetDevice(SerialDevice targetDevice) {
        this.setTargetDevice(targetDevice);
        targetDevice.setSourceDevice(this);
    }

    @Override
    public SerialDevice getTargetDevice() {
        return targetDevice;
    }

    @Override
    public void setTargetDevice(SerialDevice targetDevice) {
        this.targetDevice = targetDevice;
    }

    @Override
    public SerialDevice getSourceDevice() {
        return sourceDevice;
    }

    @Override
    public void setSourceDevice(SerialDevice sourceDevice) {
        this.sourceDevice = sourceDevice;
    }
}
