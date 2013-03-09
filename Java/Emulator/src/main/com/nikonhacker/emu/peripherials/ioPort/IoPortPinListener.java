package com.nikonhacker.emu.peripherials.ioPort;

/**
 * This is a listener to a specific port pin value change
 * @see IoPortsListener for global monitoring
 */
public interface IoPortPinListener {
    public void onPinValueChange(boolean newValue);
}
