package com.nikonhacker.emu.peripherials.ioPort.util;

/**
 * This component, when inserted at pin A, acts as a source that always outputs the value given in the constructor
 * The pin formerly attached to A is attached to pin 2 of this component, but is left dangling
 * Although, keeping this pin 2 allows to retain what was previously connected, and restore it if this "override" if removed
 */
public class FixedSourceComponent extends Abstract2PinComponent {
    public FixedSourceComponent(int overridingValue, String name, boolean logPinMessages) {
        super(name);
        setLogPinMessages(logPinMessages);
        // Replace pin 1 by a FixedSourcePin
        pin1 = new FixedSourcePin("Forced_" + overridingValue, overridingValue, this);
    }

}
