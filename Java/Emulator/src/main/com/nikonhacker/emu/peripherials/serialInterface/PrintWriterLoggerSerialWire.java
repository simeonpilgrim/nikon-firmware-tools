package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.Format;

import java.io.PrintWriter;

public class PrintWriterLoggerSerialWire extends SerialWire implements SerialDevice {
    private PrintWriter printWriter;

    public PrintWriterLoggerSerialWire(String wireName, SerialDevice realTargetDevice, PrintWriter printWriter) {
        super(wireName, realTargetDevice);
        this.printWriter = printWriter;
    }

    @Override
    public void write(Integer value) {
        printWriter.write(((value == null)?"x":Format.asHex(value, 2)) + " ");
        super.write(value);
    }
}
