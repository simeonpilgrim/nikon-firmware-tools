package com.nikonhacker.emu;

import com.nikonhacker.Constants;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MasterClock implements Runnable {

    public static final long PS_PER_MS = 1_000_000_000;
    public static final long PS_PER_SEC = 1_000_000_000_000L;

    private DecimalFormat milliSecondFormatter = new DecimalFormat("0000.000000000");

    /**
     * All objects to "clock", encapsulated in an internal class to store their counter value and threshold
     */
    private final List<ClockableEntry> entries = new CopyOnWriteArrayList<>();

    private boolean syncPlay = false;

    private boolean running = false;

    /**
     * The total elapsed time since the start of the MasterClock, in picoseconds (e-12)
     * MAX_LONG being (2e63 - 1) = 9.22e18, it will overflow after 9223372 seconds,
     * which is 2562 hours or more than 100 emulated days.
     */
    private long totalElapsedTimePs;

    /**
     * The duration represented by one MasterClock loop, in picoseconds (e-12)
     */
    private long masterClockLoopDurationPs;

    /**
     * A temp flag to indicate that the computing of intervals based on frequencies must be performed again
     * (due to a change in the list of Clockable, or a frequency change)
     */
    private boolean intervalComputingRequested;

    public MasterClock() {
    }

    public boolean isRunning() {
        return running;
    }

    public void requestIntervalComputing() {
        intervalComputingRequested = true;
    }

    /**
     * Add a clockable object.
     * @param clockable the object to wake up repeatedly
     * @param clockableCallbackHandler the object containing methods called on exit or Exception
     */
    public synchronized void add(Clockable clockable, ClockableCallbackHandler clockableCallbackHandler, boolean enabled) {
        //System.err.println("Adding " + clockable.getClass().getSimpleName());
        // Check if already present
        boolean found = false;
        for (ClockableEntry entry : entries) {
            if (entry.clockable == clockable) {
                // make sure it is enabled
                entry.enabled = true;
                found = true;
                break;
            }
        }
        if (!found) {
            entries.add(new ClockableEntry(clockable, clockableCallbackHandler, enabled));
        }
        requestIntervalComputing();
    }

    /**
     * Simpler version
     * @param clockable
     */
    public void add(Clockable clockable) {
        add(clockable, null, true);
    }


    /**
     * Removes a clockable object.
     * @param clockable the object to remove
     */
    public synchronized void remove(Clockable clockable) {
        for (int i = 0; i < entries.size(); i++) {
            ClockableEntry entry = entries.get(i);
            if (entry.clockable == clockable) {
                //System.err.println("Removing " + entry.clockable.getClass().getSimpleName());
                entries.remove(entry);
                break;
            }
        }
        requestIntervalComputing();
    }

    private void computeIntervals() {
        // Determine least common multiple of all frequencies
        long leastCommonMultipleFrequency = 1;

        // Precompute all frequencies
        Map<ClockableEntry,Integer> entryFrequencies = new HashMap<>();
        for (ClockableEntry entry : entries) {
            entryFrequencies.put(entry, entry.clockable.getFrequencyHz());
        }

        // Compute least common multiple frequency
        for (ClockableEntry entry : entries) {
            int frequencyHz = entryFrequencies.get(entry);
            if (frequencyHz > 0) {
                leastCommonMultipleFrequency = lcm(frequencyHz, leastCommonMultipleFrequency);
                entry.isFrequencyZero = false;
            }
            else {
                entry.isFrequencyZero = true;
            }
        }

        // OK. Now set each counter threshold to the value of lcm/freq
        for (ClockableEntry entry : entries) {
            if (!entry.isFrequencyZero) {
                int newThreshold = (int) (leastCommonMultipleFrequency / entryFrequencies.get(entry));
                if (entry.counterThreshold != 0) {
                    // Adjust value according to new threshold
                    entry.counterValue = entry.counterValue * newThreshold / entry.counterThreshold;
                }
                // Set new threshold
                entry.counterThreshold = newThreshold;
            }
        }

        masterClockLoopDurationPs = PS_PER_SEC/leastCommonMultipleFrequency;
/*
        System.err.println("MasterClock reconfigured with one tick=" + masterClockLoopDurationPs + "ps, with the following entries:");
        for (ClockableEntry entry : entries) {
            System.err.println("  " + (entry.enabled ? "ON: " : "OFF:") + entry.clockable.toString() + " @" + entryFrequencies.get(entry) + "Hz, every " + entry.counterThreshold + " ticks");
        }
        System.err.println("---------------------------------------");
*/
    }

    public void setEnabled(Clockable clockable, boolean enabled) {
        for (ClockableEntry candidateEntry : entries) {
            if (candidateEntry.clockable == clockable) {
                //System.err.println(enabled?"Enabling ":"Disabling " + candidateEntry.clockable.getClass().getSimpleName());
                if (!enabled && candidateEntry.clockableCallbackHandler != null) {
                    //System.err.println("Calling onNormalExit() on callback for " + candidateEntry.clockable.getClass().getSimpleName());
                    candidateEntry.clockableCallbackHandler.onNormalExit("Sync stop due to stopping of " + clockable.toString());
                }
                candidateEntry.enabled = enabled;
                if (candidateEntry.clockable instanceof Emulator) {
                    setLinkedEntriesEnabled(candidateEntry.clockable.getChip(), enabled);
                }
                break;
            }
        }
        if (!enabled) {
            // Check if all entries are disabled
            if (allEntriesDisabled()) {
                running = false;
            }
        }
    }

    public void setSyncPlay(boolean syncPlay) {
        this.syncPlay = syncPlay;
    }

    /**
     * This is the normal way to start the MasterClock asynchronously.
     * Does nothing if the clock is not already running.
     */
    public synchronized void start() {
        if (!running) {
            running = true;
            new Thread(this).start();
        }
    }

    public synchronized void stop() {
        //System.err.println("Requesting clock stop");
        running = false;
    }

    /**
     * This is the way to run the clock synchronously. Normally only called internally.
     * Use start() instead to start the clock.
     */
    public void run() {
        //System.err.println("Clock is starting");
        running = true;
        ClockableEntry entryToDisable = null;
        // Infinite loop
        while (running) {
            if (intervalComputingRequested) {
                intervalComputingRequested = false;
                computeIntervals();
            }
            // At each loop turn, check to see if an entry has reached its counter threshold
            for (ClockableEntry currentEntry : entries) {
                // Increment its counter
                currentEntry.counterValue++;
                if (currentEntry.counterValue >= currentEntry.counterThreshold) {
                    // Threshold reached for this entry
                    // System.err.println("Threshold matched for " + currentEntry.clockable.getClass().getSimpleName() + ", which is " + (currentEntry.enabled?"enabled":"disabled"));
                    // Reset Counter
                    currentEntry.counterValue = 0;
                    if (currentEntry.enabled && !currentEntry.isFrequencyZero) {
                        // If it's enabled. Call its onClockTick() method
                        try {
                            Object result = currentEntry.clockable.onClockTick();
                            //System.err.println(currentEntry.clockable.getClass().getSimpleName() + ".onClockTick() returned " + result);
                            if (result != null) {
                                // A non-null result means this entry shouldn't run anymore
                                entryToDisable = currentEntry;
                                // Warn the callback method
                                //System.err.println("Calling onNormalExit() on callback for " + currentEntry.clockable.getClass().getSimpleName());
                                if (currentEntry.clockableCallbackHandler != null) {
                                    currentEntry.clockableCallbackHandler.onNormalExit(result);
                                }
                            }
                        }
                        catch (Exception e) {
                            // In case of exception this entry shouldn't run anymore
                            entryToDisable = currentEntry;
                            // Warn the callback method
                            //System.err.println("Calling onException() on callback for " + currentEntry.clockable.getClass().getSimpleName());
                            if (currentEntry.clockableCallbackHandler != null) {
                                currentEntry.clockableCallbackHandler.onException(e);
                            }
                        }

                        if (entryToDisable != null) {
                            disableEntry(entryToDisable);

                            // Check if all entries are disabled
                            if (allEntriesDisabled()) {
                                // All entries are now disabled. Stop clock
                                // System.err.println("This was the last entry. Requesting clock stop");
                                running = false;
                                break;
                            }

                            // Clear entryToDisable
                            entryToDisable = null;
                        }
                    }
                }
            }
            // Increment elapsed time
            totalElapsedTimePs += masterClockLoopDurationPs;
        }

        // If we got here, one entry was just disabled and caused the clock to stop.
        // Before we exit, let's rotate the list so that when the clock restarts, it resumes exactly where it left off
        // To do so, the entry we just run will be rotated to the end
        while (entries.get(entries.size() - 1) != entryToDisable) Collections.rotate(entries, 1);


        System.err.println("MasterClock reordered:");
        for (ClockableEntry entry : entries) {
            System.err.println("  " + (entry.enabled ? "ON: " : "OFF:") + entry.clockable.toString() + " every " + entry.counterThreshold + " ticks");
        }
        System.err.println("---------------------------------------");


        //System.err.println("Clock is stopped\r\n=======================================================");
    }

    /**
     * Disable the given entry, and if it's an emulator, all timers linked to it,
     * plus, if syncPlay, the other emulator and its timers
     * @param entryToDisable
     */
    private void disableEntry(ClockableEntry entryToDisable) {
        // Actually disable that entry
        //System.err.println("Disabling " + currentEntry.clockable.getClass().getSimpleName());
        entryToDisable.enabled = false;
        if (entryToDisable.clockable instanceof Emulator) {
            setLinkedEntriesEnabled(entryToDisable.clockable.getChip(), false);
            if (syncPlay) {
                // Warn all other emulators that they are forced to stop, and disable them
                for (ClockableEntry candidateEntry : entries) {
                    if (candidateEntry.enabled && candidateEntry.clockable instanceof Emulator) {
                        //System.err.println("Calling onNormalExit() on callback for " + candidateEntry.clockable.getClass().getSimpleName());
                        if (candidateEntry.clockableCallbackHandler != null) {
                            candidateEntry.clockableCallbackHandler.onNormalExit("Sync stop due to " + entryToDisable.clockable.getClass().getSimpleName());
                        }
                        //System.err.println("Disabling " + candidateEntry.clockable.getClass().getSimpleName());
                        candidateEntry.enabled = false;
                        setLinkedEntriesEnabled(candidateEntry.clockable.getChip(), false);
                    }
                }
                // Stop clock
                //System.err.println("Requesting clock stop");
            }
        }
    }

    private void setLinkedEntriesEnabled(int chip, boolean enabled) {
        for (ClockableEntry candidateEntry : entries) {
            if ((candidateEntry.enabled != enabled) && (candidateEntry.clockable.getChip() == chip)) {
                if (!enabled && candidateEntry.clockableCallbackHandler != null) {
                    //System.err.println("Calling onNormalExit() on callback for " + candidateEntry.clockable.getClass().getSimpleName());
                    candidateEntry.clockableCallbackHandler.onNormalExit("Sync stop due to chip " + Constants.CHIP_LABEL[chip] + " stopping.");
                }
                //System.err.println((enabled?"Enabling ":"Disabling ") + candidateEntry.clockable.getClass().getSimpleName());
                candidateEntry.enabled = enabled;
            }
        }
    }

    private boolean allEntriesDisabled() {
        boolean allEntriesDisabled = true;
        for (ClockableEntry clockableEntry : entries) {
            if (clockableEntry.enabled) {
                allEntriesDisabled = false;
                break;
            }
        }
        return allEntriesDisabled;
    }

    public void resetTotalElapsedTimePs() {
        totalElapsedTimePs = 0;
    }


    public long getTotalElapsedTimePs() {
        return totalElapsedTimePs;
    }

    /**
     * This is for tests only
     * @param totalElapsedTimePs
     */
    public void setTotalElapsedTimePsForDebug(long totalElapsedTimePs) {
        this.totalElapsedTimePs = totalElapsedTimePs;
    }

    public String getFormatedTotalElapsedTimeMs() {
        return milliSecondFormatter.format(totalElapsedTimePs/(double)PS_PER_MS) + "ms";
    }


    //
    // Code from http://stackoverflow.com/questions/4201860/how-to-find-gcf-lcm-on-a-set-of-numbers
    // Author Jeffrey Hantin
    //

    /**
     * Greatest common divider
     * @param a
     * @param b
     * @return
     */
    private static long gcd(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    /**
     * Least common multiple
     * A little trickier, but probably the best approach is reduction by the GCD,
     * which can be similarly iterated:
     */
    private static long lcm(long a, long b) {
        return a * (b / gcd(a, b));
    }


    // This is a wrapper for the device, its counter value and its counter threshold
    private static class ClockableEntry {
        Clockable                clockable;
        ClockableCallbackHandler clockableCallbackHandler;
        int counterValue     = 0;
        int counterThreshold = 0;
        boolean enabled;
        boolean isFrequencyZero;

        public ClockableEntry(Clockable clockable, ClockableCallbackHandler clockableCallbackHandler, boolean enabled) {
            this.clockable = clockable;
            this.clockableCallbackHandler = clockableCallbackHandler;
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return "ClockableEntry (" + (enabled ?"ON":"OFF") +") for " + clockable + '}';
        }
    }
}
