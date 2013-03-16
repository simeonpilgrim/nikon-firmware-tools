package com.nikonhacker.emu.peripherials.serialInterface.bga56pin;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.AbstractSpiDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;

public class Bga56PinSerialDevice extends AbstractSpiDevice {
    private int mask = 0xFF; // 8 bits by default

    private SerialDevice connectedDevice;

    @Override
    public void write(Integer value) {
        System.out.println("Unknown component received 0x" + Format.asHex(value & mask, 2));
    }

    @Override
    public void connectSerialDevice(SerialDevice connectedDevice) {
        this.connectedDevice = connectedDevice;
    }

    @Override
    public void disconnectSerialDevice() {
        this.connectedDevice = null;
    }

    @Override
    public SerialDevice getConnectedSerialDevice() {
        return connectedDevice;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int nbBits) {
        mask = (1 << nbBits) - 1;
    }
}
