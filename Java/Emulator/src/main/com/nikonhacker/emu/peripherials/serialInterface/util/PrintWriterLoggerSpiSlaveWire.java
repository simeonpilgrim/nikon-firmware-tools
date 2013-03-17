package com.nikonhacker.emu.peripherials.serialInterface.util;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SpiSlaveDevice;

import java.io.PrintWriter;

public class PrintWriterLoggerSpiSlaveWire extends SpiSlaveWire implements SerialDevice {
    private PrintWriter printWriter;
    private int mask = 0xFF; // 8 bits by default

    public PrintWriterLoggerSpiSlaveWire(String wireName, SpiSlaveDevice realTargetDevice, PrintWriter printWriter) {
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
