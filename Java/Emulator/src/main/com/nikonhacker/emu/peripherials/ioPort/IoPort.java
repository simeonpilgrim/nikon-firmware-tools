package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public abstract class IoPort {

    /** This is the number of this port */
    protected int portNumber;

    /** InterruptController is passed to constructor to be able to actually trigger requests */
    protected InterruptController interruptController;

    protected VariableFunctionPin[] pins;

    public IoPort(int portNumber, InterruptController interruptController) {
        this.portNumber = portNumber;
        this.interruptController = interruptController;
        pins = new VariableFunctionPin[8];
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            pins[bitNumber] = new VariableFunctionPin(getShortName() + bitNumber);
        }
    }

    private String getPortCharacter() {
        return ((portNumber < 10)?String.valueOf(portNumber):String.valueOf((char)('A' - 10 + portNumber)));  // 0-9 then A-Z
    }

    protected String getShortName() {
        return "P" + getPortCharacter();
    }

    protected String getFullName() {
        return "IoPort " + getPortCharacter();
    }

    public VariableFunctionPin getPin(int pinNumber) {
        return pins[pinNumber];
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
