package com.nikonhacker.emu.peripherials.programmableTimer.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.clock.fr.FrClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.TimerCycleCounterListener;

import java.util.TimerTask;
import java.util.concurrent.Executors;

/**
 * This is a lightweight implementation of a Fujitsu 16-bit Reload Timer
 * Based on spec http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-10147-2E.pdf
 */
public class FrReloadTimer extends ProgrammableTimer {

    /** reloadValue corresponds to the Reload Timer register TMRLRAn */
    private int reloadValue;

    /*  currentValue (in superclass) corresponds to the Reload Timer register TMRn */

    /** configuration corresponds to the Reload Timer register TMCSRn */
    private int configuration;

    // The following fields are set upon setting configuration
    private int divider;
    private boolean mustReload; // vs one-shot
    private boolean interruptEnabled; // underflow interrupt enabled
    private boolean isInUnderflowCondition; // indicates an underflow has occurred

    public FrReloadTimer(int timerNumber, InterruptController interruptController, TimerCycleCounterListener cycleCounterListener) {
        super(timerNumber, interruptController, cycleCounterListener);
    }


    public void setReloadValue(int reloadValue) {
        this.reloadValue = reloadValue;
    }

    public void setConfiguration(int configuration) {
        boolean initScheduler = false;
        // Reserved
        if ((configuration & 0xC000) != 0) {
            System.out.println("Warning, trying to configure reload timer " + timerNumber + " with bits 15-14 != 00 (TMSCR" + timerNumber + "=0b" + Format.asBinary(configuration, 16) + ")");
        }

        // TRGM1-0
        if ((configuration & 0x3000) != 0) {
            throw new RuntimeException("Error configuring reload timer " + timerNumber + ": only TRGM0/1=0b00 is supported (TMSCR" + timerNumber + "=0b" + Format.asBinary(configuration, 16) + ")");
        }

        // CSL2-1-0
        int oldDivider = divider;
        int csl = (configuration & 0x0E00) >> 9;
        switch (csl) {
            case 0x0: divider = 2;  break;
            case 0x1: divider = 4;  break;
            case 0x2: divider = 8;  break;
            case 0x3: divider = 16; break;
            case 0x4: divider = 32; break;
            case 0x5: divider = 64; break;
            default: throw new RuntimeException("Error configuring reload timer " + timerNumber + ": CSL in Event Counter mode is not supported (TMSCR" + timerNumber + "=0b" + Format.asBinary(configuration, 16) + ")");
        }
        if (oldDivider != divider) {
            initScheduler = true;
        }

        // GATE: ignored
        // Undefined: ignored
        // OUTL: ignored

        // RELD
        mustReload = (configuration & 0x0010) != 0;

        // INTE
        interruptEnabled = (configuration & 0x0008) != 0;

        // UF
        if ((configuration & 0x0004) == 0) {
            isInUnderflowCondition = false;
        }

        // CNTE
        boolean countOperationEnabled = (configuration & 0x0002) != 0;

        if (executorService == null) {
            if (countOperationEnabled) {
                // Changing to enabled
                initScheduler = true;
            }
        }
        else {
            if (!countOperationEnabled) {
                // Changing to disabled
                unscheduleTask();
                executorService = null;
            }
        }

        //TRG
        if ((configuration & 0x0001) != 0) {
            currentValue = reloadValue;
        }

        // OK. Done parsing configuration.
        if (initScheduler) {
            if (executorService != null) {
                // It is a reconfiguration
                unscheduleTask();
            }
            // Create a new scheduler
            executorService = Executors.newSingleThreadScheduledExecutor();

            scale = 1;
            intervalNanoseconds = 1000000000L /*ns/s*/ * divider /*pclk tick/timer tick*/ / FrClockGenerator.PCLK_FREQUENCY;

            if (intervalNanoseconds < MIN_EMULATOR_INTERVAL_NANOSECONDS) {
                /* unsustainable frequency */
                scale = (int) Math.ceil((double)MIN_EMULATOR_INTERVAL_NANOSECONDS / intervalNanoseconds);
                intervalNanoseconds *= scale;
            }

            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (active) {
                        currentValue -= scale;
                        if (currentValue < 0) {
                            isInUnderflowCondition = true;
                            if (interruptEnabled) {
                                interruptController.request(FrInterruptController.RELOAD_TIMER0_INTERRUPT_REQUEST_NR + timerNumber);
                            }
                            if (mustReload) {
                                currentValue += reloadValue;
                            } else {
                                unscheduleTask();
                                executorService = null;
                            }
                        }
                    }
                }
            };

            scheduleTask();
        }

        this.configuration = configuration;
    }


    public int getReloadValue() {
        return reloadValue;
    }

    public int getConfiguration() {
        return (configuration & 0x3FFA) | (isInUnderflowCondition?4:0);
    }

    @Override
    public String toString() {
        return "Reload timer #" + timerNumber + " (value=" + currentValue + (interruptEnabled?", interrupt enabled":", interrupt disabled")+ ")";
    }
}
