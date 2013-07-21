package com.nikonhacker.emu.peripherials.programmableTimer.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.clock.fr.FrClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.TimerCycleCounterListener;

import java.util.TimerTask;

/**
     Counter use case:
    Starting listener for FR80 range 0x00000100 - 0x0000013F
    0xFFFFFFFF written to 0x00000130               (@0x001017E8)
    0x0613     written to 0x0000013E               (@0x001017F0)
    
    0x00000271 written to 0x00000100               (@0x001017F6)
    0x0273     written to 0x0000010E               (@0x00101800)
    
                read from 0x00000138 : 0x00000000  (@0x00107660)
                ...
                read from 0x00000138 : 0x00000000  (@0x00107660)

 * This is a implementation of a Fujitsu 32-bit Reload Timer
 * Idea comes from Chapter 19 of 91660 Hardware manual (32-bit Free-Run Timer) from CM71-10146-3E.pdf,
 * but registers do not match. So own interpretation was done.
 */
public class FrFreeRunTimer extends ProgrammableTimer {
    /** comparison value */
    private long cpclr = 1;
    
    /*  currentValue (in superclass) is too complex to use */
    private long longCurrentValue = 0;
    /** configuration corresponds to the Reload Timer register TMCSRn */
    private int tccs = 0x40;

    // The following fields are set upon setting configuration
    private int divider = 1;

    private boolean iclr = false;
    private boolean interruptEnabled = false;
    
    public FrFreeRunTimer(int timerNumber, InterruptController interruptController, TimerCycleCounterListener cycleCounterListener) {
        super(timerNumber, interruptController, cycleCounterListener);
    }

    public void setTCCS(int configuration) {
        boolean initScheduler = false;

        int oldDivider = divider;
        switch (configuration & 0xF) {
            case 0x0: divider = 1;  break;
            case 0x1: divider = 2;  break;
            case 0x2: divider = 4;  break;
            case 0x3: divider = 8; break;
            case 0x4: divider = 16; break;
            case 0x5: divider = 32; break;
            case 0x6: divider = 64; break;
            case 0x7: divider = 128; break;
            case 0x8: divider = 256; break;
            default: throw new RuntimeException("Error configuring Free-Run timer " + timerNumber + ": CLK is not supported (TCCS" + timerNumber + "=0b" + Format.asBinary(configuration, 16) + ")");
        }
        if (oldDivider != divider) {
            initScheduler = true;
        }
        // ICRE
        if ((configuration & 0x100) != 0) {
            interruptEnabled = true;
            // TODO : we do not know the interrupt number
            throw new RuntimeException("FreeRun timer interrupt is not implemented");
        }
        // ICLR
        if ((configuration & 0x200) == 0) {
            iclr = false;
            // TODO : clear interrupt request on controller
        }
        // ECKE
        if ((configuration & 0x8000) != 0) {
            // TODO : we do not know external clocks
            throw new RuntimeException("FreeRun timer external clock is not implemented");
        }
        // SCLR
        if ((configuration & 0x10) != 0) {
            longCurrentValue = 0;
        }
        // STOP
        boolean countOperationEnabled = ((configuration & 0x40) == 0);
        
        if (!enabled) {
            if (countOperationEnabled) {
                // Changing to enabled
                initScheduler = true;
            }
        }
        else {
            if (!countOperationEnabled) {
                // Changing to disabled
                unscheduleTask();
                enabled = false;
            }
        }

        // OK. Done parsing configuration.
        if (initScheduler) {
            if (enabled) {
                // It is a reconfiguration
                unscheduleTask();
            }
            // Create a new scheduler
            enabled = true;

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
                        // coderat: this is my guess, timer 0x130 is definetly counting down
                        // TODO to prove this to the end, we must check if usage of timer 0x100 expects
                        // counting up. I didn't find the place yet.
                        if ((tccs & 0x400)!=0) {
                            longCurrentValue -= scale;
                            if (longCurrentValue < 0) {
                                iclr = true;
                                if (interruptEnabled) {
                                    // TODO generate interrupt
                                }
                                longCurrentValue = cpclr;
                            }
                        } else {
                            longCurrentValue += scale;
                            if (longCurrentValue >= cpclr) {
                                iclr = true;
                                if (interruptEnabled) {
                                    // TODO generate interrupt
                                }
                                longCurrentValue = 0;
                            }
                        }
                    }
                }
            };

            scheduleTask();
        }

        tccs = configuration & 0xFDFF;
    }

    /** return status value */
    public int getTCCS() {
        return tccs | ( iclr ? 0x200: 0);
    }

    /** set comparison value */
    public void setCPCLR(int value) {
        cpclr = (value<0 ? (0x100000000L+(long)value) : (long)value);
    }

    /** return comparison value */
    public int getCPCLR() {
        return (int) (cpclr > 0x7FFFFFFF ? (cpclr-0x100000000L) : cpclr);
    }

    /** set new start value */
    public void setTCDT(int value) {
        longCurrentValue = (value<0 ? (0x100000000L+(long)value) : (long)value);
    }

    /** return current value */
    public int getTCDT() {
        return (int)(longCurrentValue > 0x7FFFFFFF ? (longCurrentValue-0x100000000L) : longCurrentValue);
    }

    @Override
    public String toString() {
        return "FreeRun timer #" + timerNumber + " (value=" + currentValue + (interruptEnabled?", interrupt enabled":", interrupt disabled")+ ")";
    }
}
