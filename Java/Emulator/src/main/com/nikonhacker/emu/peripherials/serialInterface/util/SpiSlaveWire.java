package com.nikonhacker.emu.peripherials.serialInterface.util;

import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SpiSlaveDevice;

/**
 * This class is just a "useless" wire between a serial source and a destination
 * This "inserts" in the chain for two-way connection, but only one way can be "sniffed", from "source to target"
 *
 * This class should be used as a base class or template for more creative uses
 */
public class SpiSlaveWire extends SpiSlaveDevice {
    private String wireName;

    public SpiSlaveWire(String wireName, SpiSlaveDevice realTargetDevice) {
        this.wireName = wireName;
        connectTargetDevice(realTargetDevice);
    }

    @Override
    public void write(Integer value) {
        targetDevice.write(value);
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
    }

    public String getWireName() {
        return wireName;
    }

    @Override
    public boolean isSelected() {
        return ((SpiSlaveDevice)targetDevice).isSelected();
    }
}
