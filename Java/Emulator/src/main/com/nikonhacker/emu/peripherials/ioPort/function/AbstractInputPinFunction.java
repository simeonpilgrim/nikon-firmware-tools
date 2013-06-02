package com.nikonhacker.emu.peripherials.ioPort.function;

public abstract class AbstractInputPinFunction implements PinFunction {

    protected String componentName;

    protected AbstractInputPinFunction(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Delegates to that function the responsibility to handle an input.
     */
    public abstract void setValue(int value);
}
