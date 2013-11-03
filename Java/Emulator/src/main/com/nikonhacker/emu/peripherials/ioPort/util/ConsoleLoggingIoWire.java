package com.nikonhacker.emu.peripherials.ioPort.util;

import java.io.PrintWriter;

/**
 * This class implements a bidirectional logging component (to stdout).
 */
public class ConsoleLoggingIoWire extends PrintWriterLoggerIoWire {
    public ConsoleLoggingIoWire(final String name) {
        super(name, new PrintWriter(System.out));
    }
}
