package com.nikonhacker.emu;

/**
 * This interface allows classes to be notified after each instruction has been simulated.
 */
public interface CycleCounterListener {
    /**
     * This method is called after each instruction
     * @param oldCount the number of cycles executed before the instruction
     * @param increment the number of cycles used by that instruction
     * @return false if this listener does not want to be notified anymore
     */
    public boolean onCycleCountChange(long oldCount, int increment);
}
