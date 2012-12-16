package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public class IoPort {
    /** This is the number of this timer */
    protected int portNumber;
    /** InterruptController is passed to constructor to be able to actually trigger requests */
    protected InterruptController interruptController;

    public IoPort(int portNumber, InterruptController interruptController) {
        this.portNumber = portNumber;
        this.interruptController = interruptController;
    }

    @Override
    public String toString() {
        return "IoPort " + ((portNumber < 10)?String.valueOf(portNumber):String.valueOf((char)(55 + portNumber))); // 0-9 then A-Z
    }

}
