package com.nikonhacker.emu.peripherials.ioPort.util;

import com.nikonhacker.emu.peripherials.ioPort.IoPortValueChangeListener;

/**
 * This class implements a bidirectional logging component (to a custom listener).
 */
public class ValueChangeListenerIoWire extends Abstract2PinComponent {
    public ValueChangeListenerIoWire(final String name, IoPortValueChangeListener ioPortValueChangeListener) {
        super(name);
        // Replace Pins 1 & 2 to forward values to each other
        pin1 = new ValueChangeListenerForwardingPin(name + ".pin1", ioPortValueChangeListener);
        pin2 = new ValueChangeListenerForwardingPin(name + ".pin2", ioPortValueChangeListener);
        ((ValueChangeListenerForwardingPin) pin1).setTargetPin(pin2);
        ((ValueChangeListenerForwardingPin) pin2).setTargetPin(pin1);
    }

    static class ValueChangeListenerForwardingPin extends ForwardingPin {
        private IoPortValueChangeListener ioPortValueChangeListener;

        public ValueChangeListenerForwardingPin(String name, IoPortValueChangeListener ioPortValueChangeListener) {
            super(name);
            this.ioPortValueChangeListener = ioPortValueChangeListener;
        }

        @Override
        public void setInputValue(int value) {
            ioPortValueChangeListener.onValueChange(value);
            super.setInputValue(value);
        }
    }
}
