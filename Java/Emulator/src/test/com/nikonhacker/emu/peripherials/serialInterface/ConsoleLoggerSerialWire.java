package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.util.SerialWire;

public class ConsoleLoggerSerialWire extends SerialWire implements SerialDevice {
    private int mask = 0xFF; // 8 bits by default

    public ConsoleLoggerSerialWire(String wireName, SerialDevice realTargetDevice) {
        super(wireName, realTargetDevice);
    }

    @Override
    public void write(Integer value) {
        System.out.println("    " + getWireName() + ": " + ((value == null)?"null":("0x" + Format.asHex(value & mask, 2))));
        super.write(value);
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int nbBits) {
        mask = (1 << nbBits) - 1;
        super.onBitNumberChange(serialDevice, nbBits);
    }
}
