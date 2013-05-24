package com.nikonhacker.emu.peripherials.ioPort.util;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

/** This basic implementation is a 2-pin component with no connection in between */
public abstract class Abstract2PinComponent {
    protected final String name;
    protected       Pin    pin1;
    protected       Pin    pin2;

    public Abstract2PinComponent(final String name) {
        this.name = name;
        pin1 = new Pin(name + ".pin1");
        pin2 = new Pin(name + ".pin2");
    }

    public String getName() {
        return name;
    }

    /**
     * Inserts this wire between the given pin and the pin originally connected to it
     * @param pin
     */
    public void insertAtPin(Pin pin) {
        Pin otherPin = pin.getConnectedPin();
        System.out.println("Connecting " + pin + " to " + pin1);
        Pin.interconnect(pin, pin1);
        System.out.println("Connecting " + pin2 + " to " + otherPin);
        Pin.interconnect(pin2, otherPin);
    }

    /**
     * Remove this component, reconnecting the two original pins directly
     */
    public void remove() {
        Pin.interconnect(pin1.getConnectedPin(), pin2.getConnectedPin());
    }

}
