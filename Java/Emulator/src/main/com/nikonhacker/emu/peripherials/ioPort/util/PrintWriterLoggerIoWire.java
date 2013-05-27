package com.nikonhacker.emu.peripherials.ioPort.util;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

import java.io.PrintWriter;

/**
 * This class implements a bidirectional logging component.
 */
public class PrintWriterLoggerIoWire extends Abstract2PinComponent {
    private final PrintWriter printWriter;

    public PrintWriterLoggerIoWire(final String name, PrintWriter printWriter) {
        super(name);
        this.printWriter = printWriter;
        // Replace Pins 1 & 2 to forward values to each other
        pin1 = new ForwardingPin(name + " " + pin1.getConnectedPin() + "=>" + pin2.getConnectedPin());
        pin2 = new ForwardingPin(name + " " + pin2.getConnectedPin() + "=>" + pin1.getConnectedPin());
        ((ForwardingPin) pin1).setTargetPin(pin2);
        ((ForwardingPin) pin2).setTargetPin(pin1);
    }

    private class ForwardingPin extends Pin {
        private Pin targetPin;

        public ForwardingPin(String name) {
            super(name);
        }

        @Override
        public void setInputValue(int value) {
            printWriter.write(this.getConnectedPin() + " sends " + value + " to " + targetPin.getConnectedPin());
            super.setInputValue(value);
            targetPin.getConnectedPin().setInputValue(value);
        }

        public void setTargetPin(Pin targetPin) {
            this.targetPin = targetPin;
        }
    }

}
