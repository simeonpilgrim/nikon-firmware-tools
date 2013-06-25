package com.nikonhacker.emu.peripherials.serialInterface.eeprom;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.AbstractSerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;

public class St950x0TesterDevice extends AbstractSerialDevice {
    private final String       deviceName;

    public St950x0TesterDevice(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public void write(Integer value) {
        System.err.println("        " + deviceName + " receives 0x" + Format.asHex(value, 2));
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        System.out.println("TestDevice.onBitNumberChange(" + serialDevice + ", " + numBits + ");");
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
        targetDevice.write(value);
    }

}
