package com.nikonhacker.emu.peripherials.ioPort.function;

public abstract class AbstractInputPinFunction implements PinFunction {

    protected String componentName;
    protected boolean logPinMessages = true;

    protected AbstractInputPinFunction(String componentName) {
        this.componentName = componentName;
    }

    public void setLogPinMessages(boolean logPinMessages) {
        this.logPinMessages = logPinMessages;
    }

    /**
     * Delegates to that function the responsibility to handle an input.
     */
    public abstract void setValue(int value);
}
