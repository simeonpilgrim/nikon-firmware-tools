package com.nikonhacker.emu.peripherials.ioPort.util;

import java.io.PrintWriter;

/**
 * This class implements a bidirectional logging component (to a printWriter).
 */
public class PrintWriterLoggerIoWire extends Abstract2PinComponent {
    public PrintWriterLoggerIoWire(final String name, PrintWriter printWriter) {
        super(name);
        // Replace Pins 1 & 2 to forward values to each other
        pin1 = new PrintWriterLoggerForwardingPin(name + " " + pin1.getConnectedPin() + "=>" + pin2.getConnectedPin(), printWriter);
        pin2 = new PrintWriterLoggerForwardingPin(name + " " + pin2.getConnectedPin() + "=>" + pin1.getConnectedPin(), printWriter);
        ((PrintWriterLoggerForwardingPin) pin1).setTargetPin(pin2);
        ((PrintWriterLoggerForwardingPin) pin2).setTargetPin(pin1);
    }

    private static class PrintWriterLoggerForwardingPin extends ForwardingPin {
        private final PrintWriter printWriter;

        public PrintWriterLoggerForwardingPin(String name, PrintWriter printWriter) {
            super(name);
            this.printWriter = printWriter;
        }

        @Override
        public void setInputValue(int value) {
            printWriter.write(this.getConnectedPin() + " sends " + value + " to " + targetPin.getConnectedPin());
            super.setInputValue(value);
        }
    }

}
