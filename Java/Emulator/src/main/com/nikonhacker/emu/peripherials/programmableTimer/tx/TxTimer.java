package com.nikonhacker.emu.peripherials.programmableTimer.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.CpuPowerModeChangeListener;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.clock.tx.TxClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;

/**
 * This implements "16-bit Timer/Event Counters (TMRBs)" according to section 11 of the hardware specification
 */
public class TxTimer extends ProgrammableTimer implements CpuPowerModeChangeListener {
    private static final int MAX_COUNTER_VALUE = 0xFFFF;

    public static final int TBEN_TBEN_MASK = 0b10000000;

    public static final int TBRUN_TBRUN_MASK  = 0b00000001;
    public static final int TBRUN_TBPRUN_MASK = 0b00000100;

    public static final int TBCR_I2TB_MASK   = 0b00001000;
    public static final int TBCR_TBSYNC_MASK = 0b00100000;
    public static final int TBCR_TBWBF_MASK  = 0b10000000;

    public static final int TBMOD_TBCLK_MASK = 0b00000011;
    public static final int TBMOD_TBCLE_MASK = 0b00000100;
    public static final int TBMOD_TBCPM_MASK = 0b00011000;
    public static final int TBMOD_TBCP0_MASK = 0b00100000;

    public static final int TBFFCR_TBFF0C_MASK = 0b00000011;
    public static final int TBFFCR_TBE0T1_MASK = 0b00000100;
    public static final int TBFFCR_TBE1T1_MASK = 0b00001000;
    public static final int TBFFCR_TBC0T1_MASK = 0b00010000;
    public static final int TBFFCR_TBC1T1_MASK = 0b00100000;

    public static final int TBST_INTTBn0_MASK  = 0b00000001;
    public static final int TBST_INTTBn1_MASK  = 0b00000010;
    public static final int TBST_INTTB0Fn_MASK = 0b00000100;

    public static final int TBIM_TBIMn0_MASK  = 0b00000001;
    public static final int TBIM_TBIMn1_MASK  = 0b00000010;
    public static final int TBIM_TBIM0Fn_MASK = 0b00000100;

    public static final int TBCLK_PIN_DRIVEN = 0b00;

    // Registers

    /** Timer enable */
    private int tben = 0;

    /** Timer run */
    private int tbrun = 0;

    /** Timer configuration register */
    private int tbcr   = 0;
    private int tbmod  = 0b00100000;
    private int tbffcr = 0b11000011;
    private int tbst   = 0;
    private int tbim   = 0;

    /** Compare values */
    private int tbrg0  = 0;
    private int tbrg1  = 0;

    /** Compare values double buffer*/
    private int tbrg0buf = 0;
    private int tbrg1buf = 0;

    /** Software capture values */
    private int tbcp0;
    private int tbcp1;

    /** Flip-flop output */
    private boolean ff0;

    /** Local variable to cache if timer must run or not */
    private boolean operate = false;

    public TxTimer(int timerNumber, Platform platform) {
        super(timerNumber, platform);
        this.currentValue = 0;
        ((TxCPUState) platform.getCpuState()).addCpuPowerModeChangeListener(this);
    }


    public int getTben() {
        return tben;
    }

    /**
     * Enable or disable timer.
     * This registers/unregisters the timer to the MasterClock
     * @param tben
     */
    public void setTben(int tben) {
        boolean wasEnabled = isTbenTbenSet();

        this.tben = tben;

        enabled = isTbenTbenSet();

        if (wasEnabled && !enabled) {
            unRegister();
        }

        if (!wasEnabled && enabled) {
            // Register unless it is pin-driven
            if (getTbmodTbclk() != TBCLK_PIN_DRIVEN) {
                register();
            }
        }
    }

    private boolean isTbenTbenSet() {
        return (tben & TBEN_TBEN_MASK) != 0;
    }


    public int getTbrun() {
        return tbrun;
    }

    /**
     * Stop or starts the timer.
     * @param tbrun
     */
    public void setTbrun(int tbrun) {
        this.tbrun = tbrun;

        // if counter is enabled and prescaler is enabled
        if (isTbrunTbrunSet() && isTbrunTbprunSet()) {
            // Check that clock is not external
            // TODO why check this ?
            System.err.println(getName() + " is started while configured as pin-driven");
        }
        else {
            // Spec says "Timer Run/Stop Control - 0: Stop & clear - 1: Count"
            // It seems to also apply to TBPRUN...
            currentValue = 0;
        }
        updateOperate();
    }

    private boolean isTbrunTbprunSet() {
        return (tbrun & TBRUN_TBPRUN_MASK) != 0;
    }

    private boolean isTbrunTbrunSet() {
        return (tbrun & TBRUN_TBRUN_MASK) != 0;
    }


    public int getTbcr() {
        return tbcr;
    }

    public void setTbcr(int tbcr) {
        this.tbcr = tbcr;
        if (isTbcrTbsyncSet()) {
            throw new RuntimeException("Attempt to configure " + getName() + " in synchronization mode. This is not supported for now.");
        }
        updateOperate();
    }

    private boolean isTbcrI2tbSet() {
        return (tbcr & TBCR_I2TB_MASK) != 0;
    }

    private boolean isTbcrTbsyncSet() {
        return (tbcr & TBCR_TBSYNC_MASK) != 0;
    }

    private boolean isTbcrTbwbfSet() {
        return (tbcr & TBCR_TBWBF_MASK) != 0;
    }

    public int getTbmod() {
        return tbmod;
    }

    public void setTbmod(int tbmod) {
        int oldTbclk = getTbmodTbclk();
        this.tbmod = tbmod;
        int newTbclk = getTbmodTbclk();

        if (oldTbclk != newTbclk) {
            if (enabled) {
                if ((oldTbclk != TBCLK_PIN_DRIVEN) && (newTbclk == TBCLK_PIN_DRIVEN)) {
                    // from clock to pin => unregister
                    unRegister();
                }
                if ((oldTbclk != TBCLK_PIN_DRIVEN) && (newTbclk != TBCLK_PIN_DRIVEN)) {
                    // from clock to aanother clock => recompute frequencies
                    updateFrequency();
                }
                if ((oldTbclk == TBCLK_PIN_DRIVEN) && (newTbclk != TBCLK_PIN_DRIVEN)) {
                    // from pin to clock => register
                    register();
                }
            }
        }

        if (isTbmodTbcp0Set()) {
            // Software capture request
            performCapture0();
        }
        if (getTbmodTbcpm() == 0b11) {
            // latching according to TBnMOD<TBnCPM1:0>. See section 11.3.5
            throw new RuntimeException(getName() + ": Latching on TBnOUT (cascading) not implemented");
        }
        // Spec says TBnCLK "**Clears** and controls the TMRBn up-counter."
        currentValue = 0;
    }

    public boolean isTbmodTbcp0Set() {
        return (tbmod & TBMOD_TBCP0_MASK) != 0;
    }

    public int getTbmodTbcpm() {
        return (tbmod & TBMOD_TBCPM_MASK) >> 3;
    }

    public boolean isTbmodTbcleSet() {
        return (tbmod & TBMOD_TBCLE_MASK) != 0;
    }

    public int getTbmodTbclk() {
        return tbmod & TBMOD_TBCLK_MASK;
    }

    public int getDivider() {
        switch (getTbmodTbclk()) {
            case 0b01: return 2;  // T1  = T0/2
            case 0b10: return 8;  // T4  = T0/8
            case 0b11: return 32; // T16 = T0/32
            default:
                throw new RuntimeException(getName() + " should tick according to external pin TB" + Format.asHex(timerNumber, 1) + "IN0. getDivider() should not be called !");
        }
    }


    public int getTbffcr() {
        return tbffcr | 0b11000011;
    }

    public void setTbffcr(int tbffcr) {
        this.tbffcr = tbffcr;
        switch (getTbffcrTbff0c()) {
            case 0b00: toggleFf0(); break;
            case 0b01: ff0 = true; break;
            case 0b10: ff0 = false; break;
            // case 0b11 : no change
        }
    }

    private int getTbffcrTbff0c() {
        return tbffcr & TBFFCR_TBFF0C_MASK;
    }

    private boolean isTbffcrTbE0t1Set() {
        return (tbffcr & TBFFCR_TBE0T1_MASK) != 0;
    }

    private boolean isTbffcrTbE1t1Set() {
        return (tbffcr & TBFFCR_TBE1T1_MASK) != 0;
    }

    private boolean isTbffcrTbc0t1Set() {
        return (tbffcr & TBFFCR_TBC0T1_MASK) != 0;
    }

    private boolean isTbffcrTbc1t1Set() {
        return (tbffcr & TBFFCR_TBC1T1_MASK) != 0;
    }



    public int getTbst() {
        int value = tbst;
        tbst = 0;
        return value;
    }

    public void setTbst(int tbst) {
        throw new RuntimeException(getName() + ": Cannot write to TB" + Format.asHex(timerNumber, 1) + "ST register");
    }


    public int getTbim() {
        return tbim;
    }

    public void setTbim(int tbim) {
        this.tbim = tbim;
    }

    private boolean isTbimTbimn0Set() {
        return (tbim & TBIM_TBIMn0_MASK) != 0;
    }

    private boolean isTbimTbimn1Set() {
        return (tbim & TBIM_TBIMn1_MASK) != 0;
    }

    private boolean isTbimTbim0fnSet() {
        return (tbim & TBIM_TBIM0Fn_MASK) != 0;
    }

    public int getTbuc() {
        return currentValue;
    }

    public void setTbuc(int tbuc) {
        this.currentValue = tbuc;
    }

    private void toggleFf0() {
        ff0 = !ff0;
    }

    // TODO use this
    private boolean getFf0() {
        return ff0;
    }

    // TODO
    // "The value of TB0FF0 can be output to the timer output pin, TB0OUT (shared with P54).
    // To enable timer output, the port 5 related registers P5CR and P5FC must be programmed beforehand."
    public boolean isFf0() {
        return ff0;
    }

    public int getTbrg0() {
        return tbrg0;
    }

    public void setTbrg0(int tbrg0) {
        // "If double buffering is enabled, data is transferred from register buffer 0 to the TB0RG0/1
        // timer register when there is a match between UC0 and TB0RG0/1."
        if (isTbcrTbwbfSet()) {
            // Double buffering
            this.tbrg0buf = tbrg0;
        }
        else {
            // No double buffering
            this.tbrg0 = tbrg0;
        }
    }

    public int getTbrg1() {
        return tbrg1;
    }

    public void setTbrg1(int tbrg1) {
        this.tbrg1 = tbrg1;
    }

    public int getTbcp0() {
        return tbcp0;
    }

    public void setTbcp0(int tbcp0) {
        this.tbcp0 = tbcp0;
    }

    public void performCapture0() {
        setTbcp0(currentValue);
        if (isTbffcrTbc0t1Set()) {
            toggleFf0();
        }
    }

    public int getTbcp1() {
        return tbcp1;
    }

    public void setTbcp1(int tbcp1) {
        this.tbcp1 = tbcp1;
    }

    public void performCapture1() {
        setTbcp1(currentValue);
        if (isTbffcrTbc1t1Set()) {
            toggleFf0();
        }
    }

    @Override
    public void onCpuPowerModeChange(TxCPUState.PowerMode powerMode) {
        updateOperate();
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        updateOperate();
    }

    private void updateOperate() {
        // Operate if all of the conditions below are true:
        // 1. active is true (timers not disabled by UI)
        // 2. either the processor is in RUN state, or the timer is configured to run in idle mode.
        // 3. the timer runs
        // 4. the prescaler runs (as we don't support external pin trigger)
        operate = active
                && ((((TxCPUState) platform.getCpuState()).getPowerMode() == TxCPUState.PowerMode.RUN) || isTbcrI2tbSet())
                && isTbrunTbrunSet()
                && isTbrunTbprunSet();
    }


    @Override
    public int getChip() {
        return Constants.CHIP_TX;
    }

    @Override
    public int getFrequencyHz() {
        return ((TxClockGenerator)platform.getClockGenerator()).getFt0Hz() / getDivider();
    }

    @Override
    public Object onClockTick() {
//        System.out.println(getName() + (operate?" operates":" doesn't operate"));
        if (operate) {
            boolean interruptCondition = false;
            currentValue++;
            // Comparator 0
            if (currentValue == tbrg0) {
                // CP0 matches
                tbst |= TBST_INTTBn0_MASK;
                if (!isTbimTbimn0Set()) {
                    interruptCondition = true;
                }
                // Toggle if requested
                if (isTbffcrTbE0t1Set()) {
                    toggleFf0();
                }
                // Load buffered value if double buffering
                if (isTbcrTbwbfSet()) {
                    tbrg0 = tbrg0buf;
                }
            }

            // Comparator 1
            if (currentValue == tbrg1) {
                // CP1 matches
                tbst |= TBST_INTTBn1_MASK;
                if (!isTbimTbimn1Set()) {
                    interruptCondition = true;
                }
                // Toggle if requested
                if (isTbffcrTbE1t1Set()) {
                    toggleFf0();
                }
                // Clear if requested (CP1 only)
                if (isTbmodTbcleSet()) {
                    // “1”: Clears up-counter if there is a match with timer register 1 (TBnRG1).
                    currentValue = 0;
                }
                // Load buffered value if double buffering
                if (isTbcrTbwbfSet()) {
                    tbrg1 = tbrg1buf;
                }
            }

            // Detect overflow at 16bit
            if (currentValue > MAX_COUNTER_VALUE) {
                // overflow
                tbst |= TBST_INTTB0Fn_MASK;
                if (!isTbimTbim0fnSet()) {
                    interruptCondition = true;
                }
                currentValue = 0;
            }
            if (interruptCondition) {
                platform.getInterruptController().request(TxInterruptController.INTTB0 + timerNumber);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        int requestLevel = ((TxInterruptController) platform.getInterruptController()).getRequestLevel(TxInterruptController.INTTB0 + timerNumber);
        return getName() + " @" + getFrequencyString() + ": TB" + Format.asHex(timerNumber, 1) + "EN=0x" + Format.asHex(getTben(), 2)
                + ", RUN=0x" + Format.asHex(getTbrun(), 2)
                + ", RG0=" + tbrg0 + "d, RG1=" + tbrg1 + "d"
                + ", level=0b" + Format.asBinary(requestLevel, 3) + (requestLevel == 0 ? " (interrupt disabled)" : " (interrupt enabled)")
                + ", value=" + currentValue + "d";
    }

    @Override
    protected String getName() {
        return Constants.CHIP_LABEL[Constants.CHIP_TX] + " Timer " + Format.asHex(timerNumber, 1);
    }
}
