package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.Format;

public class ConsoleLoggerSerialWire extends SerialWire implements SerialDevice {
    public ConsoleLoggerSerialWire(String wireName, SerialDevice realTargetDevice) {
        super(wireName, realTargetDevice);
    }

    @Override
    public void write(Integer value) {
        System.out.println("    " + getWireName() + ":" + ((value == null)?"null":(" 0x" + Format.asHex(value, 2))));
        super.write(value);
    }
}
