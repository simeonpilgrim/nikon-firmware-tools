package com.nikonhacker.emu.peripherials.ioPort.util;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

/**
 * This basic implementation is a 2-pin component with no connection in between
 */
public abstract class Abstract2PinComponent {
    protected final String  name;
    protected       Pin     pin1;
    protected       Pin     pin2;
    private         boolean logPinMessages = true;

    public Abstract2PinComponent(final String name) {
        this.name = name;
        pin1 = new Pin(name + ".pin1");
        pin2 = new Pin(name + ".pin2");
    }

    public void setLogPinMessages(boolean logPinMessages) {
        this.logPinMessages = logPinMessages;
    }

    public String getName() {
        return name;
    }

    /**
     * Inserts this component between the given pin and the pin originally connected to it
     * The given pin gets connected to the the pin1 of this component
     * The pin formerly connected to the given pin gets connected to the pin2 of this component
     * @param pin
     */
    public void insertAtPin(Pin pin) {
        Pin otherPin = pin.getConnectedPin();
        Pin.interconnect(pin, pin1);
        Pin.interconnect(pin2, otherPin);
        if (logPinMessages) {
            System.out.println("Connecting " + pin + " to " + pin1);
            System.out.println("Connecting " + pin2 + " to " + otherPin);
        }
    }

    /**
     * Remove this component, reconnecting the two original pins directly
     */
    public void remove() {
        Pin.interconnect(pin1.getConnectedPin(), pin2.getConnectedPin());
    }

    public Pin getPin1() {
        return pin1;
    }

    public Pin getPin2() {
        return pin2;
    }
}
