package com.nikonhacker.emu.peripherials.programmableTimer.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.CpuPowerModeChangeListener;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.clock.tx.TxClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;

/**
 * This implements "32-bit Input Capture (TMRC)" according to section 12 of the hardware specification
 */
public class TxInputCaptureTimer extends ProgrammableTimer implements CpuPowerModeChangeListener {
    public static final int TCEN_TCEN_MASK = 0b10000000;

    public static final int TBTRUN_TBTRUN_MASK  = 0b00000001;
    public static final int TBTRUN_TBTPRUN_MASK = 0b00000010;
    public static final int TBTRUN_TBTCAP_MASK  = 0b00000100;
    public static final int TBTRUN_I2TBT_MASK   = 0b01000000;

    public static final int TBTCR_TBTCLK_MASK = 0b00001111;

    public static final int CMPCTL_CMPEN_MASK  = 0b00000001;
    public static final int CMPCTL_CMPRDE_MASK = 0b00000010;
    public static final int CMPCTL_TCFFC_MASK  = 0b00110000;
    public static final int CMPCTL_TCFFEN_MASK = 0b01000000;


    private boolean previousMsbSet = false;

    // Registers

    /** Timer enable */
    private int tcen = 0;

    /** Timer run */
    private int tbtrun;

    /** Timer configuration register */
    private int tbtcr;

    /** Software capture value */
    private int tbtcap = 0;

    /** Capture register configuration */
    private int[] capcr = new int[TxIoListener.NUM_CAPTURE_CHANNEL];

    /** External event capture value */
    private int[] tccap = new int[TxIoListener.NUM_CAPTURE_CHANNEL];

    /** Compare control register */
    private int[] cmpctl = new int[TxIoListener.NUM_COMPARE_CHANNEL];

    /** Compare values */
    private int[] tccmp = new int[TxIoListener.NUM_COMPARE_CHANNEL];

    /** Compare values double buffer */
    private int[] tccmpBuf = new int[TxIoListener.NUM_COMPARE_CHANNEL];

    /** Flip-flop output */
    private boolean ff[] = new boolean[TxIoListener.NUM_COMPARE_CHANNEL];

    /** Local variable to cache if timer must run or not */
    private boolean operate = false;

    public TxInputCaptureTimer(Platform platform) {
        super(0, platform);
        this.currentValue = 0;
        ((TxCPUState) platform.getCpuState()).addCpuPowerModeChangeListener(this);
        for (int compareChannel = 0; compareChannel < TxIoListener.NUM_COMPARE_CHANNEL; compareChannel++) {
            cmpctl[compareChannel] = CMPCTL_TCFFC_MASK;
        }
    }

    public int getTcen() {
        return tcen;
    }

    /**
     * Enable or disable timer.
     * This registers/unregisters the timer to the MasterClock
     * @param tcen
     */
    public void setTcen(int tcen) {
        boolean wasEnabled = isTcenTcenSet();

        this.tcen = tcen;

        enabled = isTcenTcenSet();

        if (wasEnabled && !enabled) {
            unRegister();
        }

        if (!wasEnabled && enabled) {
            register();
        }
    }

    public boolean isTcenTcenSet() {
        return (tcen & TCEN_TCEN_MASK) != 0;
    }


    public int getTbtrun() {
        return tbtrun;
    }

    /**
     * Stop or starts the timer.
     * @param tbtrun
     */
    public void setTbtrun(int tbtrun) {
        this.tbtrun = tbtrun;

        if (isTbtrunTbtcapSet()) {
            tbtcap = currentValue;
        }

        // if counter is enabled and prescaler is enabled
        if (!isTbtrunTbtrunSet() || !isTbtrunTbtprunSet()) {
            // Spec says "Timer Run/Stop Control - 0: Stop & clear - 1: Count"
            // It seems to also apply to TBTPRUN...
            currentValue = 0;
        }
        updateOperate();
    }

    private boolean isTbtrunTbtrunSet() {
        return (tbtrun & TBTRUN_TBTRUN_MASK) != 0;
    }

    private boolean isTbtrunTbtprunSet() {
        return (tbtrun & TBTRUN_TBTPRUN_MASK) != 0;
    }

    private boolean isTbtrunTbtcapSet() {
        return (tbtrun & TBTRUN_TBTCAP_MASK) != 0;
    }

    private boolean isTbtrunI2tbtSet() {
        return (tbtrun & TBTRUN_I2TBT_MASK) != 0;
    }


    public int getTbtcr() {
        return tbtcr;
    }

    public void setTbtcr(int tbtcr) {
        int oldDivider = getDivider();
        this.tbtcr = tbtcr;
        int newDivider = getDivider();

        if (oldDivider != newDivider) {
            updateFrequency();
        }

        // Noise reduction is meaningless for an emulator
    }

    private int getTbtClk() {
        return tbtcr & TBTCR_TBTCLK_MASK;
    }

    public int getDivider() {
        switch (getTbtClk()) {
            case 0b0000: return 4;  // T2  = T0/4
            case 0b0001: return 8;  // T4  = T0/8
            case 0b0010: return 16; // T8 = T0/16
            case 0b0011: return 32; // T16 = T0/32
            case 0b0100: return 64; // T32 = T0/64
            case 0b0101: return 128; // T64 = T0/128
            case 0b0110: return 256; // T128 = T0/256
            case 0b0111: return 512; // T256 = T0/512
            case 0b1111:
                throw new RuntimeException(getName() + " should tick according to external pin TBTIN. getDivider() should not be called !");
//                System.out.println("Capture Timer should tick according to external pin TBTIN. Using T0/512 frequency...");
//                return 512;  // Incorrect, but we have no actual pin connected, so...
            default:
                throw new RuntimeException(getName() + ": Unknown TbtClk : 0b" + Format.asBinary(getTbtClk(), 4));
        }
    }


    public int getTbtcap() {
        return tbtcap;
    }

    public void setTbtcap(int tbtcap) {
        this.tbtcap = tbtcap;
    }

    public int getCapcr(int captureChannel) {
        return capcr[captureChannel];
    }

    public void setCapcr(int captureChannel, int capcr) {
        this.capcr[captureChannel] = capcr;
        // TODO: Hardware capture trigger pinfunction according to

        // Noise reduction is meaningless in emulation
    }


    public int getTccap(int captureChannel) {
        return tccap[captureChannel];
    }


    public int getCmpctl(int compareChannel) {
        return cmpctl[compareChannel];
    }

    public void setCmpctl(int compareChannel, int cmpctl) {
        this.cmpctl[compareChannel] = cmpctl;

        // The role of the timer flip-flop is not well described in the Input Capture spec (chap 12).
        // Explanations are more complete in the Timer Counters spec (e.g. section 11.3.7)
        // Hoping that behaviour is the same
        switch (getCmpctlTcffc(compareChannel)) {
            case 0b00:
                toggleFf(compareChannel);
                break;
            case 0b01:
                ff[compareChannel] = true;
                break;
            case 0b10:
                ff[compareChannel] = false;
                break;
            default :
                // don't care
        }
    }

    private boolean isCmpctlCmpenSet(int compareChannel) {
        return (cmpctl[compareChannel] & CMPCTL_CMPEN_MASK) != 0;
    }

    private boolean isCmpctlCmprdeSet(int compareChannel) {
        return (cmpctl[compareChannel] & CMPCTL_CMPRDE_MASK) != 0;
    }

    private int getCmpctlTcffc(int compareChannel) {
        return (cmpctl[compareChannel] & CMPCTL_TCFFC_MASK) >> 4;
    }

    private boolean isCmpctlTcffenSet(int compareChannel) {
        return (cmpctl[compareChannel] & CMPCTL_TCFFEN_MASK) != 0;
    }


    public int getTccmp(int compareChannel) {
        return tccmp[compareChannel];
    }

    public void setTccmp(int compareChannel, int tccmp) {
        if (isCmpctlCmprdeSet(compareChannel)) {
            // Double buffering
            this.tccmpBuf[compareChannel] = tccmp;
        }
        else {
            // No double buffering
            this.tccmp[compareChannel] = tccmp;
        }
    }


    private void toggleFf(int compareChannel) {
        ff[compareChannel] = !ff[compareChannel];
    }

    // TODO use this
    public boolean getFf(int compareChannel) {
        return ff[compareChannel];
    }


    // TODO use this
    public void performCapture(int captureChannel) {
        tccap[captureChannel] = currentValue;
        platform.getInterruptController().request(TxInterruptController.INTCAP0 + captureChannel);
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
                && ((((TxCPUState)platform.getCpuState()).getPowerMode()== TxCPUState.PowerMode.RUN) || isTbtrunI2tbtSet())
                && isTbtrunTbtrunSet()
                && isTbtrunTbtprunSet();
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
        if (operate) {
            currentValue++;

            // Comparators
            for (int compareChannel = 0; compareChannel < TxIoListener.NUM_COMPARE_CHANNEL; compareChannel++) {
                if (isCmpctlCmpenSet(compareChannel)) {
                    if (currentValue == tccmp[compareChannel]) {
                        // Comparator matches
                        platform.getInterruptController().request(TxInterruptController.INTCMP0 + compareChannel);

                        // Spec Block diagram also indicates INTCAP0 in this block. Typo I guess...
                        if (isCmpctlTcffenSet(compareChannel)) {
                            toggleFf(compareChannel);
                        }
                        // TODO set output pin TCCOUT0 + comparechannel = 1;

                        // Load buffered value if double buffering
                        if (isCmpctlCmprdeSet(compareChannel)) {
                            tccmp[compareChannel] = tccmpBuf[compareChannel];
                        }
                    }
                }
            }

            // Detect overflow at 32bit
            if (currentValue == 0) {
                // overflow : interrupt
                platform.getInterruptController().request(TxInterruptController.INTTBT);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        int requestLevel = ((TxInterruptController) platform.getInterruptController()).getRequestLevel(TxInterruptController.INTTBT);
        return getName() + " @" + getFrequencyString() + ": TCEN=0x" + Format.asHex(getTcen(), 2)
                + ", TBTRUN=0x" + Format.asHex(getTbtrun(), 2)
                + ", TBTCR=0x" + Format.asHex(getTbtcr(), 2)
                + ", level=0b" + Format.asBinary(requestLevel, 3) + (requestLevel == 0 ? " (interrupt disabled)" : " (interrupt enabled)")
                + ", value=" + currentValue;
    }

    @Override
    protected String getName() {
        return Constants.CHIP_LABEL[Constants.CHIP_TX] + " Capture Timer";
    }

}
