package com.nikonhacker.emu;

public interface Clockable {

    /**
     * This method return the frequency of this device, in Hertz
     * @return
     */
    public long getFrequencyHz();

    /**
     * This method is called each time the clock ticks
     * @return null if the device still wants to be "clocked", or any object the method wants to return
     */
    public Object onClockTick() throws Exception;
}
