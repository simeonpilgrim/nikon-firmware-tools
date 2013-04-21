package com.nikonhacker.emu.peripherials.programmableTimer.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.CpuPowerModeChangeListener;
import com.nikonhacker.emu.clock.tx.TxClockGenerator;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.TimerCycleCounterListener;

import java.util.TimerTask;
import java.util.concurrent.Executors;

public class TxInputCaptureTimer extends ProgrammableTimer implements CpuPowerModeChangeListener {
    private TxCPUState cpuState;
    private TxClockGenerator clockGenerator;

    private boolean previousMsbSet = false;

    // Registers
    /** Timer run */
    private int run;

    /** Timer configuration register */
    private int cr;

    /** Software capture value */
    private int tbtCap = 0;

    /** External event capture value */
    private int[] tcCap = new int[TxIoListener.NUM_CAPTURE_CHANNEL];

    /** Capture register configuration */
    private int[] capCr = new int[TxIoListener.NUM_CAPTURE_CHANNEL];

    /** Compare values */
    private int[] tcCmp = new int[TxIoListener.NUM_COMPARE_CHANNEL];

    /** Compare values double buffer */
    private int[] tcCmpBuf = new int[TxIoListener.NUM_COMPARE_CHANNEL];

    /** Compare control register */
    private int[] cmpCtl = new int[TxIoListener.NUM_COMPARE_CHANNEL];

    // Flip-flop output
    private boolean ff[] = new boolean[TxIoListener.NUM_COMPARE_CHANNEL];

    private boolean operateInIdle = true;
    private boolean operate;

    public TxInputCaptureTimer(TxCPUState cpuState, TxClockGenerator clockGenerator, TxInterruptController interruptController, TimerCycleCounterListener cycleCounterListener) {
        super(0, interruptController, cycleCounterListener);
        this.cpuState = cpuState;
        this.clockGenerator = clockGenerator;
        this.currentValue = 0;
        cpuState.addCpuPowerModeChangeListener(this);
        for (int compareChannel = 0; compareChannel < TxIoListener.NUM_COMPARE_CHANNEL; compareChannel++) {
            cmpCtl[compareChannel] = 0b00110000;
        }
    }


    public int getEn() {
        return (executorService == null)?0:0x80;
    }

   /**
     * Enable or disable timer.
     * Technically, this creates or destroys the ExecutorService.
     * @param en
     */
    public void setEn(int en) {
        if ((en & 0x80) != 0) {
            // enable
            if (executorService != null) {
                // It is a reconfiguration
                unscheduleTask();
            }
            // Create a new scheduler
            executorService = Executors.newSingleThreadScheduledExecutor();

            // If a run was requested before enabling the timer, or this timer was just temporarily disabled
            if (timerTask != null) {
                // restart it
                scheduleTask();
            }
        }
        else {
            // disable
            if (executorService != null) {
                // Shut down
                unscheduleTask();
                // Destroy it
                executorService = null;
            }
        }
    }

    public int getTbtCap() {
        return tbtCap;
    }

    public void setTbtCap(int tbtCap) {
        this.tbtCap = tbtCap;
    }

    public int getTcCap(int captureChannel) {
        return tcCap[captureChannel];
    }

    public void captureValue(int captureChannel) {
        this.tcCap[captureChannel] = currentValue;
        interruptController.request(TxInterruptController.INTCAP0 + captureChannel);
    }

    public int getCapCr(int captureChannel) {
        return capCr[captureChannel];
    }

    public void setCapCr(int captureChannel, int capCr) {
        this.capCr[captureChannel] = capCr;
        // Noise reduction is meaningless in emulation
        // int eg = capCr & 0b11;
        // TODO: Enable capture should get hold of the corresponding pin
    }

    public int getCmpCtl(int compareChannel) {
        return cmpCtl[compareChannel];
    }

    private boolean isCmpCtlCmpRdEn(int compareChannel) {
        return (cmpCtl[compareChannel] & 0b10) != 0;
    }

    private int getCmpCtlFfc(int compareChannel) {
        return (cmpCtl[compareChannel] & 0b00110000) >> 4;
    }

    public void setCmpCtl(int compareChannel, int cmpCtl) {
        this.cmpCtl[compareChannel] = cmpCtl;
        switch (getCmpCtlFfc(compareChannel)) {
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

    public int getTcCmp(int compareChannel) {
        return tcCmp[compareChannel];
    }

    public void setTcCmp(int compareChannel, int tcCmp) {
        if (isCmpCtlCmpRdEn(compareChannel)) {
            // Double buffering
            this.tcCmpBuf[compareChannel] = tcCmp;
        }
        else {
            // No double buffering
            this.tcCmp[compareChannel] = tcCmp;
        }
    }

    public int getRun() {
        return run;
    }

    /**
     * Stop or starts the timer.
     * Technically, this creates or destroys the TimerTask
     * @param run
     */
    public void setRun(int run) {
        this.run = run;
        boolean countEnabled     = (run & 0b00000001) != 0;
        boolean prescalerEnabled = (run & 0b00000010) != 0;
        operateInIdle            = (run & 0b01000000) != 0;

        if ((run & 0b00001000) != 0) {
            tbtCap = currentValue;
        }

        if (countEnabled && prescalerEnabled) {
            scale = 1;
            intervalNanoseconds = 1000000000L /*ns/s*/ * getDivider() /*T0tick/timertick*/ / clockGenerator.getT0Frequency() /* T0tick/s */ ;

            if (intervalNanoseconds < MIN_EMULATOR_INTERVAL_NANOSECONDS) {
                /* unsustainable frequency */
                scale = (int) Math.ceil((double)MIN_EMULATOR_INTERVAL_NANOSECONDS / intervalNanoseconds);
                intervalNanoseconds *= scale;
            }
            // Prepare task
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (active && operate) {

                        currentValue += scale;

                        // Comparator 1
                        for (int compareChannel = 0; compareChannel < TxIoListener.NUM_COMPARE_CHANNEL; compareChannel++) {
                            if (((cmpCtl[compareChannel] & 0b1) != 0) && (currentValue / scale == tcCmp[compareChannel] / scale)) {
                                // match
                                interruptController.request(TxInterruptController.INTCMP0 + compareChannel);
                                // Spec Block diagram also indicates INTCAP0 in this block. Typo I guess...
                                if ((cmpCtl[compareChannel] & 0b01000000) != 0) {
                                    toggleFf(compareChannel);
                                }
                                // TODO set output pin TCCOUT0 + comparechannel = 1;
                                if (isCmpCtlCmpRdEn(compareChannel)) {
                                    // Double buffering
                                    tcCmp[compareChannel] = tcCmpBuf[compareChannel];
                                }
                            }
                        }

                        // Detect overflow at 32bit
                        boolean msbSet = (currentValue & 0x80000000) != 0;
                        if (!msbSet && previousMsbSet) {
                            // overflow : interrupt
                            interruptController.request(TxInterruptController.INTTBT);
                        }
                        previousMsbSet = msbSet;
                    }
                }
            };

            if (executorService == null) {
                System.out.println("Start requested on capture timer but its TCEN register is 0. Postponing...");
            }
            else {
                scheduleTask();
            }
        }
        else {
            currentValue = 0;
            if (executorService != null) {
                // Stop it (and prepare a new one, because it cannot be restarted)
                unscheduleTask();
                executorService = Executors.newSingleThreadScheduledExecutor();
            }
            timerTask = null;
        }
    }

    public int getCr() {
        return cr;
    }

    public void setCr(int cr) {
        this.cr = cr;
        // Noise reduction is meaningless for an emulator
        // source clock is handled in getDivider()
        updateOperate();
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
                System.out.println("Capture Timer should tick according to external pin TBTIN. Using T0/512 frequency...");
                return 512;  // Incorrect, but we have no actual pin connected, so...
        }
        throw new RuntimeException("Unknown TbtClk : 0b" + Format.asBinary(getTbtClk(), 4) + "for Capture Timer");
    }

    private int getTbtClk() {
        return cr & 0b00001111;
    }

//    public int getFfcr() {
//        return ffcr | 0b11000011;
//    }
//
//    public void setFfcr(int ffcr) {
//        switch (ffcr & 0b11) {
//            case 0b00: toggleFf0(); break;
//            case 0b01: ff0 = true; break;
//            case 0b10: ff0 = false; break;
//            // case 0b11 : no change
//        }
//        this.ffcr = ffcr;
//    }

    private void toggleFf(int compareChannel) {
        ff[compareChannel] = !ff[compareChannel];
    }

    public boolean getFf(int compareChannel) {
        return ff[compareChannel];
    }

    @Override
    public void onCpuPowerModeChange(TxCPUState.PowerMode powerMode) {
        updateOperate();
    }

    private void updateOperate() {
        operate = operateInIdle || (cpuState.getPowerMode()== TxCPUState.PowerMode.RUN);
    }

    @Override
    public String toString() {
        int requestLevel = ((TxInterruptController) interruptController).getRequestLevel(TxInterruptController.INTTBT);
        return "Capture Timer : TCEN=0x" + Format.asHex(getEn(), 2)
                + ", TBTRUN=0x" + Format.asHex(getRun(), 2)
                + ", TBTCR=0x" + Format.asHex(getCr(), 2)
                + ", level=0b" + Format.asBinary(requestLevel, 3) + (requestLevel==0?" (interrupt disabled)":" (interrupt enabled)")
                + ", value=" + currentValue;
    }

}
