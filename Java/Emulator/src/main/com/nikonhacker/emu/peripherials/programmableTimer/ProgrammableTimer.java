package com.nikonhacker.emu.peripherials.programmableTimer;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

import java.util.concurrent.ScheduledExecutorService;

public abstract class ProgrammableTimer {

    /** Lower boundary of sustainable interval between emulator scheduler ticks */
    public final static int MIN_EMULATOR_INTERVAL_NANOSECONDS = 100000; //100 microseconds interval = max frequency of 10kHz

    /** This is the number of this timer */
    protected int timerNumber;

    /** InterruptController is passed to constructor to be able to actually trigger requests */
    protected InterruptController interruptController;

    /**
     * This is an emulator setting allowing to pause timers even though their emulated state is enabled.
     * The Java timer continues to run, but each execution does nothing
     */
    protected boolean enabled = false;

    /** Underlying scheduler */
    ScheduledExecutorService executorService;

    /**
     * The scale compensates for the fact that emulated clocks cannot run faster than MAX_EMULATOR_FREQUENCY
     * So emulated timers running faster are triggered at a (emulated frequency / scale)
     * And at each trigger, the reload counter is inc/decremented by (scale)
     */
    protected int scale;


    public ProgrammableTimer(int timerNumber, InterruptController interruptController) {
        this.timerNumber = timerNumber;
        this.interruptController = interruptController;
    }

    protected ProgrammableTimer() {
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
