package com.nikonhacker.emu.peripherials.programmableTimer.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.clock.fr.FrClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;

/**
 * This is a lightweight implementation of a Fujitsu 16-bit Reload Timer
 * Based on section 20 of the 91605 hardware spec
 * http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-10147-2E.pdf
 */
public class FrReloadTimer extends ProgrammableTimer {

    public static final int TMCSR_TRG_MASK  = 0b00000000_00000001;
    public static final int TMCSR_CNTE_MASK = 0b00000000_00000010;
    public static final int TMCSR_UF_MASK   = 0b00000000_00000100;
    public static final int TMCSR_INTE_MASK = 0b00000000_00001000;
    public static final int TMCSR_RELD_MASK = 0b00000000_00010000;
    public static final int TMCSR_CSL_MASK  = 0b00001110_00000000;

    /** Reload Timer register TMRLRAn corresponds to the reloadValue */
    protected int tmrlra;

    /*  currentValue (in superclass) corresponds to the Reload Timer register TMRn */

    /** Reload Timer register TMCSRn is the configuration */
    private int tmcsr;

    /** indicates an underflow has occurred */
    protected boolean isInUnderflowCondition;

    public FrReloadTimer(int timerNumber, Platform platform) {
        super(timerNumber, platform);
    }

    public int getTmrlra() {
        return tmrlra;
    }

    /**
     * Set reload value
     * @param tmrlra
     */
    public void setTmrlra(int tmrlra) {
        this.tmrlra = tmrlra;
    }


    public int getTmr() {
        return currentValue;
    }

    public int getTmcsr() {
        // bits15-14 are reserved and read as 0
        // bit2 is underflow flag
        // bit0 is always read as 0
        return (tmcsr & 0b00111111_11111010) | (isInUnderflowCondition ? TMCSR_UF_MASK : 0);
    }

    /**
     * Set configuration
     * @param tmcsr
     */
    public void setTmcsr(int tmcsr) {
        // TRGM1-0
        if ((tmcsr & TMCSR_TRGM_MASK()) != 0) {
            throw new RuntimeException("Error configuring reload timer " + timerNumber + ": only TRGM0/1=0b00 is supported (TMCSR" + timerNumber + "=0b" + Format.asBinary(tmcsr, 16) + ")");
        }

        // GATE: ignored
        // OUTL: ignored

        // read old values
        int oldDivider = getDivider();
        boolean wasEnabled = isTmcsrCnteSet();

        // store new register
        this.tmcsr = tmcsr;

        // compare values with old, and reconfigure if needed
        int newDivider = getDivider();
        enabled = isTmcsrCnteSet();

        if (wasEnabled & !enabled) {
            // Stopping
            unRegister();
        }
        if (oldDivider != newDivider) {
            updateFrequency();
        }
        if (!wasEnabled && enabled) {
            // Starting
            register();
        }

        // Clear underflow status if requested (UF written as 0)
        if (!isTmcsrUfSet()) {
            isInUnderflowCondition = false;
            removeInterrupt();
        }

        // Reload value if requested
        if (isTmcsrTrgSet()) {
            currentValue = tmrlra;
        }
    }

    private int TMCSR_TRGM_MASK() {
        return 0b00110000_00000000;
    }

    /**
     * @return true if reload value must happen now
     */
    private boolean isTmcsrTrgSet() {
        return (tmcsr & TMCSR_TRG_MASK) != 0;
    }

    /**
     * @return true if counter is enabled
     */
    private boolean isTmcsrCnteSet() {
        return (tmcsr & TMCSR_CNTE_MASK) != 0;
    }

    /**
     * True if underflow status flag is set
     * @return
     */
    private boolean isTmcsrUfSet() {
        return (tmcsr & TMCSR_UF_MASK) != 0;
    }

    /**
     * @return true=must reload, false=one shot
     */
    protected boolean isTmcsrReldSet() {
        return (tmcsr & TMCSR_RELD_MASK) != 0;
    }

    /**
     * @return true if underflow interrupt is enabled
     */
    protected boolean isTmcsrInteSet() {
        return (tmcsr & TMCSR_INTE_MASK) != 0;
    }

    /**
     * Divider
     * @return
     */
    private int getDivider() {
        // CSL2-1-0
        int csl = (tmcsr & TMCSR_CSL_MASK) >> 9;
        switch (csl) {
            case 0x0: return 2;
            case 0x1: return 4;
            case 0x2: return 8;
            case 0x3: return 16;
            case 0x4: return 32;
            case 0x5: return 64;
            default: throw new RuntimeException("Error configuring reload timer " + timerNumber + ": CSL in Event Counter mode is not supported (TMCSR" + timerNumber + "=0b" + Format.asBinary(tmcsr, 16) + ")");
        }
    }

    @Override
    public int getChip() {
        return Constants.CHIP_FR;
    }

    @Override
    public int getFrequencyHz() {
        return ((FrClockGenerator)platform.getClockGenerator()).getPClkFrequency() / getDivider();
    }

    @Override
    public Object onClockTick() throws Exception {
        if (active) {
            currentValue--;
            if (currentValue==0) {
                isInUnderflowCondition = true;
                if (isTmcsrInteSet()) {
                    requestInterrupt();
                }
                if (isTmcsrReldSet()) {
                    currentValue = tmrlra;
                } else {
                    enabled = false;
                    return "DONE";
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getName() + " @" + getFrequencyString() + ": TMR" + Format.asHex(timerNumber, 1)
                + ", TMCSR=0b" + Format.asBinary(tmcsr, 16) + ", TMRLRA=" + tmrlra + "d"
                + (isTmcsrInteSet() ? ", interrupt enabled" : ", interrupt disabled")
                + ", value=" + currentValue + "d";
    }

    @Override
    protected String getName() {
        return Constants.CHIP_LABEL[Constants.CHIP_FR] + " Reload timer #" + timerNumber;
    }

    protected boolean requestInterrupt() {
        return platform.getInterruptController().request(FrInterruptController.RELOAD_TIMER0_INTERRUPT_REQUEST_NR + timerNumber);
    }

    protected void removeInterrupt() {
        platform.getInterruptController().removeRequest(FrInterruptController.RELOAD_TIMER0_INTERRUPT_REQUEST_NR + timerNumber);
    }
//        if (enabled) {
//            // It is a reconfiguration
//            unregister();
//        }
//        // enable
//        enabled = true;
//
//        scale = 1;
//        intervalNanoseconds = 1000000000L /*ns/s*/ * divider /*pclk tick/timer tick*/ / FrClockGenerator.PCLK_FREQUENCY;
//
//        if (intervalNanoseconds < MIN_EMULATOR_INTERVAL_NANOSECONDS) {
//            /* unsustainable frequency */
//            scale = (int) Math.ceil((double)MIN_EMULATOR_INTERVAL_NANOSECONDS / intervalNanoseconds);
//            intervalNanoseconds *= scale;
//        }
//
//        register();

}
