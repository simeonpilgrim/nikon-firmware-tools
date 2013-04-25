package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public abstract class IoPort {
    /** This is the number of this timer */
    protected int portNumber;
    /** InterruptController is passed to constructor to be able to actually trigger requests */
    private InterruptController interruptController;

    public IoPort(int portNumber, InterruptController interruptController) {
        this.portNumber = portNumber;
        this.interruptController = interruptController;
    }

    @Override
    public String toString() {
        return "IoPort " + ((portNumber < 10)?String.valueOf(portNumber):String.valueOf((char)('A' - 10 + portNumber))); // 0-9 then A-Z
    }

    public abstract String getPinName(int pinNumber);

    public abstract String getPinDescription(int pinNumber);
}
