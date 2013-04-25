package com.nikonhacker.emu.peripherials.serialInterface.util;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;

import java.io.PrintWriter;

public class PrintWriterLoggerSerialWire extends SerialWire implements SerialDevice {
    private PrintWriter printWriter;
    private int mask = 0xFF; // 8 bits by default

    public PrintWriterLoggerSerialWire(String wireName, SerialDevice realTargetDevice, PrintWriter printWriter) {
        super(wireName, realTargetDevice);
        this.printWriter = printWriter;
    }

    @Override
    public void write(Integer value) {
        printWriter.write(((value == null)?"x":Format.asHex(value & mask, 2)) + " ");
        super.write(value);
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        mask = (1 << numBits) - 1;
        super.onBitNumberChange(serialDevice, numBits);
    }

}
