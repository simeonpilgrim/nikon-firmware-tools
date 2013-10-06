package com.nikonhacker.emu.peripherials.programmableTimer;

import com.nikonhacker.emu.Clockable;
import com.nikonhacker.emu.Platform;

public abstract class ProgrammableTimer implements Clockable {

    /** Lower boundary of sustainable interval between emulator scheduler ticks */
    @Deprecated
    public final static int MIN_EMULATOR_INTERVAL_NANOSECONDS = 50000; //50 microseconds interval = max frequency of 20kHz

    /** Platform is passed to constructor to be able to use interrupt controller, master clock, etc. */
    protected Platform platform;

    /** This is the number of this timer */
    protected int timerNumber;

    /** This is the current value of this timer */
    protected int currentValue;

    /**
     * This is the actual emulated state of the timer
     */
    protected boolean enabled = false;

    /**
     * This is an emulator setting allowing to pause timers even though their emulated state is enabled.
     * The timer is still registered, but each execution does nothing
     */
    protected boolean active = false;

    /**
     * The scale compensates for the fact that emulated clocks cannot run faster than MAX_EMULATOR_FREQUENCY
     * So emulated timers running faster are triggered at a (emulated frequency / scale)
     * And at each trigger, the reload counter is inc/decremented by (scale)
     */
    @Deprecated
    protected int scale;

    @Deprecated
    protected long intervalNanoseconds = 1000000000L; // in ns/Timertick. For example, intervalNanoseconds=1000000000 ns/Timertick means f = 1Hz

    public ProgrammableTimer(int timerNumber, Platform platform) {
        this.timerNumber = timerNumber;
        this.platform = platform;
    }

    public int getTimerNumber() {
        return timerNumber;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    protected void register() {
        platform.getMasterClock().add(this);
    }

    protected void updateFrequency() {
        platform.getMasterClock().requestResheduling();
    }

    protected void unRegister() {
        platform.getMasterClock().remove(this);
    }

    @Override
    public String toString() {
        return "ProgrammableTimer #" + timerNumber + (active?" (active)":" (inactive) @" + getFrequencyHz() + "Hz");
    }

    protected abstract String getName();

    protected String getFrequencyString() {
        String frequencyHz;
        try {
            frequencyHz = getFrequencyHz() + "Hz";
        }
        catch (Exception e) {
            frequencyHz = "?Hz";
        }
        return frequencyHz;
    }
}
