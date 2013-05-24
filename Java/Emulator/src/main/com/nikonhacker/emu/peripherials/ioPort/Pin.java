package com.nikonhacker.emu.peripherials.ioPort;

/** Plain pin */
public class Pin {
    /**
     * This is the pin name, not its function
     */
    private String name;

    /**
     * This is the pin connected to this pin, if any
     */
    private Pin connectedPin;

    /**
     * This is the value set by another component, that can be read by the component to which this pin belongs
     */
    private int inputValue;

    /**
     * This is the value set by this component, that can be read by the component whose pin is connected to this one
     */
    private int outputValue;

    /** Static method to connect two pins "both ways" */
    public static void interconnect(Pin pinA, Pin pinB) {
        pinA.setConnectedPin(pinB);
        pinB.setConnectedPin(pinA);
    }

    public Pin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Pin getConnectedPin() {
        return connectedPin;
    }

    public void setConnectedPin(Pin connectedPin) {
        this.connectedPin = connectedPin;
    }

    /** To be called by CPU code */
    public int getInputValue() {
        return inputValue;
    }

    /** To be called by external component */
    public void setInputValue(int value) {
        this.inputValue = value;
    }

    /** To be called by external component */
    public int getOutputValue() {
        return outputValue;
    }

    /** To be called by CPU code */
    public void setOutputValue(int value) {
        this.outputValue = value;
        if (getConnectedPin() == null) {
            System.out.println("No pin is connected to " + name);
        }
        else {
            getConnectedPin().setInputValue(value);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
