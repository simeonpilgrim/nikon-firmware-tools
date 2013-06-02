package com.nikonhacker.emu.peripherials.ioPort.util;

/**
 * This component, when inserted at pin A, acts as a source that always outputs the value given in the constructor
 * The pin formerly attached to A is attached to pin 2 of this component, but is left dangling
 */
public class FixedSourceComponent extends Abstract2PinComponent {
    public FixedSourceComponent(int overridingValue, String name) {
        super(name);
        // Replace pin 1 by a FixedSourcePin
        pin1 = new FixedSourcePin("Forced_" + overridingValue, overridingValue, this);
    }

}
