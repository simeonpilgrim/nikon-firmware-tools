package com.nikonhacker.emu.peripherials.programmableTimer;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public abstract class ProgrammableTimer {
    /**
     * This is an emulator setting allowing to pause timers even though their emulated state is enabled
     */
    protected boolean enabled = false;
    /** This is the number of this timer */
    protected int timerNumber;
    /** InterruptController is passed to constructor to be able to actually trigger requests */
    protected InterruptController interruptController;

    public ProgrammableTimer(InterruptController interruptController, int timerNumber) {
        this.interruptController = interruptController;
        this.timerNumber = timerNumber;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
