package com.nikonhacker.emu.peripherials.serialInterface.lcd;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SpiSlaveDevice;

/**
 * This driver is used for the LCD of the viewfinder
 * Specification is at http://rohmfs.rohm.com/en/products/databook/datasheet/ic/driver/lcd_segment/bu9795afv-e.pdf
 */
public class LcdDriver extends SpiSlaveDevice {
    private int mask = 0xFF; // 8 bits by default

    private SerialDevice connectedDevice;

    @Override
    public void write(Integer value) {
        if (!selected) {
            throw new RuntimeException("LcdDriver.write(0x" + Format.asHex(value & 0xFF, 2) + ") called while chip is not SELECTed !");
        }
        System.out.println("LCD driver received 0x" + Format.asHex(value & mask, 2));
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
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        mask = (1 << numBits) - 1;
    }

    @Override
    public String toString() {
        return "Viewfinder Lcd Driver";
    }
}
