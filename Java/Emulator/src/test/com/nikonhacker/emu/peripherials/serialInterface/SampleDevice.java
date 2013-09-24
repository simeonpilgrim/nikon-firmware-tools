package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.Format;

/**
 * This is a sample Serial Device
 */
public class SampleDevice extends SpiSlaveDevice {
    private int mask = 0xFF; // 8 bits by default

    private String       name;

    public SampleDevice(String name) {
        this.name = name;
    }

    @Override
    public void write(Integer value) {
        if (!selected) {
            throw new RuntimeException("SampleDevice.write(0x" + Format.asHex(value & 0xFF, 2) + ") called while chip is not SELECTed !");
        }
        System.out.println("SampleDevice received 0x" + Format.asHex(value & mask, 2));
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        mask = (1 << numBits) - 1;
    }

    @Override
    public String toString() {
        return name;
    }
}
