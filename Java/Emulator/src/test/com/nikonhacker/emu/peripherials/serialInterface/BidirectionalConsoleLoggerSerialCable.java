package com.nikonhacker.emu.peripherials.serialInterface;

public class BidirectionalConsoleLoggerSerialCable {
    SerialDevice device1;
    SerialDevice device2;

    public BidirectionalConsoleLoggerSerialCable(SerialDevice device1, SerialDevice device2) {
        this.device1 = device1;
        this.device2 = device2;
    }

    public void connect() {
        device1.connectSerialDevice(new SerialWire("From 1 to 2", device2));
        device2.connectSerialDevice(new SerialWire("From 2 to 1", device1));
    }

    public void disconnect() {
        device1.connectSerialDevice(null);
        device2.connectSerialDevice(null);
    }

}
