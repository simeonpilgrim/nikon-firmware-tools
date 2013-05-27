package com.nikonhacker.emu.peripherials.ioPort.util;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

/**
 * This class implements a component letting data only flow from first component (passed to insertAtPin) to the connected component.
 * No information goes back the other way
 */
public class Diode extends Abstract2PinComponent {

    public Diode(final String name) {
        super(name);
        // Replace Pin 1 to forward values to pin2
        pin1 = new ForwardingPin(name + " " + pin1.getConnectedPin() + "=>" + pin2.getConnectedPin());
        ((ForwardingPin) pin1).setTargetPin(pin2);
    }

    private class ForwardingPin extends Pin {
        private Pin targetPin;

        public ForwardingPin(String name) {
            super(name);
        }

        @Override
        public void setInputValue(int value) {
            super.setInputValue(value);
            targetPin.getConnectedPin().setInputValue(value);
        }

        public void setTargetPin(Pin targetPin) {
            this.targetPin = targetPin;
        }
    }
}
