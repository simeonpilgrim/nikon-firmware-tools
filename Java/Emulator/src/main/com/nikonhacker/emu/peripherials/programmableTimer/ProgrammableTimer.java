package com.nikonhacker.emu.peripherials.programmableTimer;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public abstract class ProgrammableTimer {

    /** This is the number of this timer */
    protected int timerNumber;

    /** InterruptController is passed to constructor to be able to actually trigger requests */
    protected InterruptController interruptController;

    /**
     * This is an emulator setting allowing to pause timers even though their emulated state is enabled.
     * The Java timer continues to run, but each execution does nothing
     */
    protected boolean enabled = false;


    public ProgrammableTimer(int timerNumber, InterruptController interruptController) {
        this.timerNumber = timerNumber;
        this.interruptController = interruptController;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
