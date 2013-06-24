package com.nikonhacker.emu;

import java.util.ArrayList;
import java.util.List;

public class MasterClock implements Runnable {

    /**
     * All objects to "clock", encapsulated in an internal class to store their counter value and threshold
     */
    private final List<ClockableEntry> entries = new ArrayList<>();

    private boolean syncPlay = false;

    private boolean running = false;


    public MasterClock() {
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Add a clockable object.
     * This method will cause an exception if called when the clock is running
     * This method should always be followed by a start()
     * @param clockable the object to wake up repeatedly
     * @param clockableCallbackHandler the object containing methods called on exit or Exception
     */
    public synchronized void add(Clockable clockable, ClockableCallbackHandler clockableCallbackHandler, boolean enabled) {
        if (running) throw new RuntimeException("Cannot add devices once the clock is running !");

        if (clockableCallbackHandler == null) {
            clockableCallbackHandler = new ClockableCallbackHandler() {
                @Override
                public void onNormalExit(Object o) {
                    System.out.println("Normal exit with return " + o.toString());
                }

                @Override
                public void onException(Exception e) {
                    System.out.println("Exit with exception " + e.getMessage());
                }
            };
        }

        //System.err.println("Adding " + clockable.getClass().getSimpleName());
        entries.add(new ClockableEntry(clockable, clockableCallbackHandler, enabled));

        // Determine least common multiple of all frequencies
        long leastCommonMultipleFrequency = 1;
        for (ClockableEntry entry : entries) {
            leastCommonMultipleFrequency = lcm(entry.clockable.getFrequencyHz(), leastCommonMultipleFrequency);
        }

        // OK. Now set each counter threshold to the value of lcm/freq
        for (ClockableEntry entry : entries) {
            entry.counterThreshold = (int) (leastCommonMultipleFrequency / entry.clockable.getFrequencyHz());
        }
    }

    /**
     * Removes a clockable object.
     * This method will cause an exception if called when the clock is running
     * @param clockable the object to remove
     */
    public synchronized void remove(Clockable clockable) {
        if (running) throw new RuntimeException("Cannot add devices once the clock is running !");
        for (int i = 0; i < entries.size(); i++) {
            ClockableEntry entry = entries.get(i);
            if (entry.clockable == clockable) {
                //System.err.println("Removing " + entry.clockable.getClass().getSimpleName());
                entries.remove(entry);
                break;
            }
        }
    }

    public void setEnabled(Clockable clockable, boolean enabled) {
        for (ClockableEntry entry : entries) {
            if (entry.clockable == clockable) {
                //System.err.println(enabled?"Enabling ":"Disabling " + entry.clockable.getClass().getSimpleName());
                entry.enabled = enabled;
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
        ClockableEntry entryToStop = null;
        // Infinite loop
        while (running) {
            // At each loop turn, check to see if an entry has reached its counter threshold
            for (ClockableEntry currentEntry : entries) {
                // Increment its counter
                currentEntry.counterValue++;
                if (currentEntry.counterValue == currentEntry.counterThreshold) {
                    // Threshold reached for this entry
                    //System.err.println("Threshold matched for " + currentEntry.clockable.getClass().getSimpleName() + ", which is " + (currentEntry.enabled?"enabled":"disabled"));
                    // Reset Counter
                    currentEntry.counterValue = 0;
                    if (currentEntry.enabled) {
                        // And it's enabled. Call its onClockTick() method
                        try {
                            Object result = currentEntry.clockable.onClockTick();
                            //System.err.println(currentEntry.clockable.getClass().getSimpleName() + ".onClockTick() returned " + result);
                            if (result != null) {
                                // A non-null result means this entry shouldn't run anymore
                                entryToStop = currentEntry;
                                // Warn the callback method
                                //System.err.println("Calling onNormalExit() on callback for " + currentEntry.clockable.getClass().getSimpleName());
                                currentEntry.clockableCallbackHandler.onNormalExit(result);
                            }
                        }
                        catch (Exception e) {
                            // In case of exception this entry shouldn't run anymore
                            entryToStop = currentEntry;
                            // Warn the callback method
                            //System.err.println("Calling onException() on callback for " + currentEntry.clockable.getClass().getSimpleName());
                            currentEntry.clockableCallbackHandler.onException(e);
                        }

                        if (entryToStop != null) {
                            // Actually disable that entry
                            //System.err.println("Disabling " + currentEntry.clockable.getClass().getSimpleName());
                            entryToStop.enabled = false;
                            if (syncPlay) {
                                // Warn all other remaining entries that they are forced to stop, and disable them
                                for (ClockableEntry entryToDisable : entries) {
                                    if (entryToDisable.enabled) {
                                        //System.err.println("Calling onNormalExit() on callback for " + entryToDisable.clockable.getClass().getSimpleName());
                                        entryToDisable.clockableCallbackHandler.onNormalExit("Sync stop due to " + entryToStop.clockable.getClass().getSimpleName());
                                        //System.err.println("Disabling " + entryToDisable.clockable.getClass().getSimpleName());
                                        entryToDisable.enabled = false;
                                    }
                                }
                                // Stop clock
                                //System.err.println("Requesting clock stop");
                                running = false;
                                break;
                            }
                            else {
                                // Check if all entries are disabled
                                if (allEntriesDisabled()) {
                                    // This was the last entry to run. Stop clock
                                    //System.err.println("This was the last entry. Requesting clock stop");
                                    running = false;
                                    break;
                                }
                            }
                            // Reset entryToStop
                            entryToStop = null;
                        }
                    }
                }
            }
        }
        //System.err.println("Clock is stopped\r\n=======================================================");
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
    private class ClockableEntry {
        Clockable                clockable;
        ClockableCallbackHandler clockableCallbackHandler;
        int counterValue     = 0;
        int counterThreshold = 0;
        boolean enabled;

        public ClockableEntry(Clockable clockable, ClockableCallbackHandler clockableCallbackHandler, boolean enabled) {
            this.clockable = clockable;
            this.clockableCallbackHandler = clockableCallbackHandler;
            this.enabled = enabled;
        }
    }
}
