package com.nikonhacker.emu.peripherials.ioPort.util;

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
}
