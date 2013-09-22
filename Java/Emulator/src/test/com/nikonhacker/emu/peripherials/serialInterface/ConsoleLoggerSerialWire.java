package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.peripherials.serialInterface.util.PrintWriterLoggerSerialWire;

import java.io.PrintWriter;

public class ConsoleLoggerSerialWire extends PrintWriterLoggerSerialWire {
    public ConsoleLoggerSerialWire(String wireName, SerialDevice realTargetDevice) {
        super(wireName, realTargetDevice, new PrintWriter(System.out));
    }
}
