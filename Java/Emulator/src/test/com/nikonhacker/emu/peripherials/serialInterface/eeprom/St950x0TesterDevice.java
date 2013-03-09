package com.nikonhacker.emu.peripherials.serialInterface.eeprom;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.DummySerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;

public class St950x0TesterDevice implements SerialDevice {
    private final String deviceName;
    private SerialDevice eeprom;

    public St950x0TesterDevice(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public void write(Integer value) {
        System.err.println("        " + deviceName + " receives 0x" + Format.asHex(value, 2));
    }

    @Override
    public void connectSerialDevice(SerialDevice connectedDevice) {
        this.eeprom = connectedDevice;
    }

    @Override
    public void disconnectSerialDevice() {
        this.eeprom = new DummySerialDevice();
    }

    @Override
    public SerialDevice getConnectedSerialDevice() {
        return eeprom;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int nbBits) {
        System.out.println("TestDevice.onBitNumberChange(" + serialDevice + ", " + nbBits + ");");
    }

    public void doSendRdsr() {
        write(St950x0.RDSR);
        write(St950x0.DUMMY_BYTE);
    }

    public void doSendWrite() {
        write(St950x0.WRITE0);
        write(0x12);
        write(0x34);
        write(0x56);
    }

    public void doSendRead() {
        write(St950x0.READ0);
        write(0x13);
        write(St950x0.DUMMY_BYTE);
    }

    public void doSendWren() {
        write(St950x0.WREN);
    }

    private void write(int value) {
        System.err.println(deviceName + " writes 0x" + Format.asHex(value, 2));
        eeprom.write(value);
    }

}
