package com.nikonhacker.emu.peripherials.ioPort.util;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

/**
 * This is a pin that always returns a fixed value when read.
 */
public class FixedSourcePin extends Pin {
    private int forcedOutputValue;
    private Abstract2PinComponent component;

    /**
     * @param name a symbolic name
     * @param forcedOutputValue the value to return
     * @param component the component this pin belongs to
     */
    public FixedSourcePin(String name, int forcedOutputValue, Abstract2PinComponent component) {
        super(name);
        this.forcedOutputValue = forcedOutputValue;
        this.component = component;
    }

    @Override
    public Integer getOutputValue() {
        return forcedOutputValue;
    }

    public Abstract2PinComponent getComponent() {
        return component;
    }

    @Override
    public void setConnectedPin(Pin connectedPin) {
        // Statically link pin
        super.setConnectedPin(connectedPin);
        // Simulate an edge to reach the newly affected value
        connectedPin.setInputValue(forcedOutputValue);
    }
}
