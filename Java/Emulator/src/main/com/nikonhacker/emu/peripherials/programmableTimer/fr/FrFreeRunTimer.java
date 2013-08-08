package com.nikonhacker.emu.peripherials.programmableTimer.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.clock.fr.FrClockGenerator;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;

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

 * This is an implementation of a Fujitsu 32-bit Free Run Timer
 * Idea comes from Chapter 19 of 91660 Hardware manual (32-bit Free-Run Timer) from CM71-10146-3E.pdf,
 * but register adresses do not match. So own interpretation was done.
 */
public class FrFreeRunTimer extends ProgrammableTimer {
    public static final int TCCS_CLK_MASK           = 0b00000000_00001111;
    public static final int TCCS_SCLR_MASK          = 0b00000000_00010000;
    public static final int TCCS_STOP_MASK          = 0b00000000_01000000;
    public static final int TCCS_ICRE_MASK          = 0b00000001_00000000;
    public static final int TCCS_ICLR_MASK          = 0b00000010_00000000;
    /**
     * coderat: this is my guess, timer 0x130 is definitely counting down
     * TODO to prove this to the end, we must check if usage of timer 0x100 expects
     * TODO counting up. I didn't find the place yet.
     */
    public static final int TCCS_DOWN_MASK_PROBABLY = 0b00000100_00000000;
    public static final int TCCS_ECKE_MASK          = 0b10000000_00000000;

    /** comparison value */
    private int cpclr = 1;

    /** TCCS is the configuration. It corresponds roughly to the Reload Timer register TMCSRn */
    private int tccs = TCCS_STOP_MASK;

    /** indicates if timer has reached comparison value */
    private boolean iclr = false;

    public FrFreeRunTimer(int timerNumber, Platform platform) {
        super(timerNumber, platform);
    }

    /** Return status value */
    public int getTccs() {
        return tccs | ( iclr ? TCCS_ICLR_MASK : 0);
    }

    public void setTccs(int tccs) {
        // read old values
        int oldDivider = getDivider();
        boolean wasEnabled = !isTccsStopSet();

        // store new register
        this.tccs = tccs & ~TCCS_ICLR_MASK; // Ignore ICLR

        // compare values with old, and reconfigure if needed
        int newDivider = getDivider();
        enabled = !isTccsStopSet();

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

        // ICRE
        if (isTccsIcreSet()) {
            // TODO : we do not know the interrupt number
            throw new RuntimeException(getName() + ": interrupt is not implemented");
        }
        // ICLR
        if (!isTccsIclrSet()) {
            iclr = false;
            // TODO : clear interrupt request on controller
        }
        // ECKE
        if (isTccsEckeSet()) {
            // TODO : we do not know external clocks
            throw new RuntimeException(getName() + ": external clock is not implemented");
        }
        // SCLR
        if (isTccsSclrSet()) {
            currentValue = 0;
        }
    }

    private boolean isTccsStopSet() {
        return (tccs & TCCS_STOP_MASK) != 0;
    }

    private boolean isTccsSclrSet() {
        return (tccs & TCCS_SCLR_MASK) != 0;
    }

    private boolean isTccsEckeSet() {
        return (tccs & TCCS_ECKE_MASK) != 0;
    }

    private boolean isTccsIclrSet() {
        return (tccs & TCCS_ICLR_MASK) != 0;
    }

    private boolean isTccsIcreSet() {
        return (tccs & TCCS_ICRE_MASK) != 0;
    }

    private boolean isTccsDownSet() {
        return (tccs & TCCS_DOWN_MASK_PROBABLY) != 0;
    }

    private int getDivider() {
        switch (tccs & TCCS_CLK_MASK) {
            case 0x0: return 1;
            case 0x1: return 2;
            case 0x2: return 4;
            case 0x3: return 8;
            case 0x4: return 16;
            case 0x5: return 32;
            case 0x6: return 64;
            case 0x7: return 128;
            case 0x8: return 256;
            default: throw new RuntimeException("Error configuring " + getName() + ": CLK is not supported (TCCS" + timerNumber + "=0b" + Format.asBinary(tccs, 16) + ")");
        }
    }

    /** set comparison value */
    public void setCpclr(int cpclr) {
        this.cpclr = cpclr;
    }

    /** return comparison value */
    public int getCpclr() {
        return cpclr;
    }

    /** set new start value */
    public void setTcdt(int tcdt) {
        currentValue = tcdt;
    }

    /** return current value */
    public int getTcdt() {
        return currentValue;
    }

    @Override
    public int getChip() {
        return Constants.CHIP_FR;
    }

    @Override
    public int getFrequencyHz() {
        return FrClockGenerator.PCLK_FREQUENCY / getDivider();
    }

    @Override
    public Object onClockTick() throws Exception {
        if (active) {
            if (isTccsDownSet()) {
                currentValue--;
                if (currentValue == 0) {
                    iclr = true;
                    if (isTccsIcreSet()) {
                        // TODO generate interrupt
                    }
                    currentValue = cpclr;
                }
            } else {
                currentValue++;
                if (currentValue == cpclr) {
                    iclr = true;
                    if (isTccsIcreSet()) {
                        // TODO generate interrupt
                    }
                    currentValue = 0;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getName(); // TODO
    }

    @Override
    protected String getName() {
        return Constants.CHIP_LABEL[Constants.CHIP_FR] + " Free Run Timer";
    }

//    if (enabled) {
//        // It is a reconfiguration
//        unRegister();
//    }
//    // Create a new scheduler
//    enabled = true;
//
//    scale = 1;
//    intervalNanoseconds = 1000000000L /*ns/s*/ * divider /*pclk tick/timer tick*/ / FrClockGenerator.PCLK_FREQUENCY;
//
//    if (intervalNanoseconds < MIN_EMULATOR_INTERVAL_NANOSECONDS) {
//                /* unsustainable frequency */
//        scale = (int) Math.ceil((double)MIN_EMULATOR_INTERVAL_NANOSECONDS / intervalNanoseconds);
//        intervalNanoseconds *= scale;
//    }
//
//    register();


}
