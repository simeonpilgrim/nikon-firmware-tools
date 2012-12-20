package com.nikonhacker.emu.peripherials.programmableTimer;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.CpuPowerModeChangeListener;
import com.nikonhacker.emu.clock.TxClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.TxInterruptController;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TxTimer extends ProgrammableTimer implements CpuPowerModeChangeListener {
    private TxCPUState cpuState;
    private TxClockGenerator clockGenerator;

    int currentValue = 0;

    // Registers
    int cr;
    int mod;
    int ffcr;
    int st;
    int im;
    int rg0;
    int rg1;
    int cp0;
    int cp1;

    // Flip-flop output
    boolean ff0 = false; // undefined in fact

    private TimerTask timerTask = null;
    private long intervalNanoseconds = 1000000000L; // in ns/Timertick. For example, intervalNanoseconds=1000000000 ns/Timertick means f = 1Hz
    private static final int MAX_COUNTER_VALUE = (1 << 16) - 1;
    private boolean operateInIdle = true;
    private boolean operate;

    public TxTimer(int timerNumber, TxCPUState cpuState, TxClockGenerator clockGenerator, TxInterruptController interruptController) {
        super(timerNumber, interruptController);
        this.cpuState = cpuState;
        this.clockGenerator = clockGenerator;
        cpuState.addCpuPowerModeChangeListener(this);
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
                executorService.shutdownNow();
            }
            // Create a new scheduler
            executorService = Executors.newSingleThreadScheduledExecutor();

            // If a run was requested before enabling the timer, or this timer was just temporarily disabled
            if (timerTask != null) {
                // restart it
                executorService.scheduleAtFixedRate(timerTask, 0, intervalNanoseconds, TimeUnit.NANOSECONDS);
            }
        }
        else {
            // disable
            if (executorService != null) {
                // Shut down
                executorService.shutdownNow();
                // Destroy it
                executorService = null;
            }
        }
    }

    public int getRun() {
        return (timerTask != null)?0x1:0x0 ; // | prescaler?0x4:0x0
    }

    /**
     * Stop or starts the timer.
     * Technically, this creates or destroys the TimerTask
     * @param run
     */
    public void setRun(int run) {
        boolean countEnabled = (run & 0x1) != 0;
        boolean prescalerEnabled = (run & 0x4) != 0;

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
                    if (active & operate) {
                        boolean interruptCondition = false;

                        currentValue += scale;

                        // Comparator 0
                        if (rg0 > 0 && (currentValue / scale == rg0 / scale)) {
                            // CP0 matches
                            if ((im & 0b001) == 0) {
                                st |= 0b001;
                                interruptCondition = true;
                            }
                            if ((ffcr & 0b00000100) != 0) {
                                toggleFf0();
                            }
                        }

                        // Comparator 1
                        if (rg1 > 0 && (currentValue / scale == rg1 / scale)) {
                            // CP1 matches
                            if ((im & 0b010) == 0) {
                                st |= 0b010;
                                interruptCondition = true;
                            }
                            if ((ffcr & 0b00001000) != 0) {
                                toggleFf0();
                            }
                            if (getModCle()) {
                                currentValue -= rg1;
                            }
                        }

                        // Wrap at 16bit
                        if (currentValue > MAX_COUNTER_VALUE) {
                            // overflow
                            if ((im & 0b100) == 0) {
                                st |= 0b100;
                                interruptCondition = true;
                            }
                            currentValue -= MAX_COUNTER_VALUE;
                        }
                        if (interruptCondition) {
                            interruptController.request(TxInterruptController.INTTB0 + timerNumber);
                        }
                    }
                }
            };

            if (executorService == null) {
                System.out.println("Start requested on timer " + timerNumber + " but its TB" + Format.asHex(timerNumber, 1) + "EN register is 0. Postponing...");
            }
            else {
                executorService.scheduleAtFixedRate(timerTask, 0, intervalNanoseconds, TimeUnit.NANOSECONDS);
            }
        }
        else {
            currentValue = 0;
            if (executorService != null) {
                // Stop it (and prepare a new one, because it cannot be restarted)
                executorService.shutdownNow();
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
        if ((cr & 0b10000000) != 0) /* WBF */ {
            throw new RuntimeException("Attempt to configure Timer #" + timerNumber + " with double buffering. This is not supported for now.");
        }
        if ((cr & 0b100000) != 0) /* SYNC */ {
            throw new RuntimeException("Attempt to configure Timer #" + timerNumber + " in synchronization mode. This is not supported for now.");
        }
        operateInIdle = (cr & 0b1000) != 0;
        updateOperate();
    }

    public int getMod() {
        return mod;
    }

    public boolean getModCp0() {
        return (mod & 0b100000) != 0;
    }

    public int getModCpm() {
        return (mod & 0b11000) >> 3;
    }

    public boolean getModCle() {
        return (mod & 0b100) != 0;
    }

    public int getModClk() {
        return mod & 0b11;
    }

    public int getDivider() {
        switch (getModClk()) {
            case 0b00:
                System.out.println("Timer " + timerNumber + " should tick according to external pin. Using T0 frequency...");
                return 1;  // Incorrect, but we have no actual pin connected, so...
            case 0b01: return 2;  // T1  = T0/2
            case 0b10: return 8;  // T4  = T0/8
            case 0b11: return 32; // T16 = T0/32
        }
        throw new RuntimeException("Unknown ModClk : 0b" + Format.asBinary(getModClk(), 2) + "for Timer " + timerNumber);
    }

    public void setMod(int mod) {
        if ((mod & 0b11000) != 0) {
            // TODO latching according to TBnMOD<TBnCPM1:0>. See section 11.3.5
            throw new RuntimeException("Latching not implemented");
        }
        this.mod = mod;
    }

    public int getFfcr() {
        return ffcr | 0b11000011;
    }

    public void setFfcr(int ffcr) {
        switch (ffcr & 0b11) {
            case 0b00: toggleFf0(); break;
            case 0b01: ff0 = true; break;
            case 0b10: ff0 = false; break;
            // case 0b11 : no change
        }
        this.ffcr = ffcr;
    }

    private void toggleFf0() {
        ff0 = !ff0;
    }

    public int getSt() {
        int value = st;
        st = 0;
        return value;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int getIm() {
        return im;
    }

    public void setIm(int im) {
        this.im = im;
    }

    public int getUc() {
        return currentValue;
    }

    public void setUc(int uc) {
        this.currentValue = uc;
    }

    public int getRg0() {
        return rg0;
    }

    public void setRg0(int rg0) {
        this.rg0 = rg0;
    }

    public int getRg1() {
        return rg1;
    }

    public void setRg1(int rg1) {
        this.rg1 = rg1;
    }

    public int getCp0() {
        return cp0;
    }

    public void setCp0(int cp0) {
        this.cp0 = cp0;
    }

    public int getCp1() {
        return cp1;
    }

    public void setCp1(int cp1) {
        this.cp1 = cp1;
    }

    public boolean getFf0() {
        return ff0;
    }

    @Override
    public void onCpuPowerModeChange(TxCPUState.PowerMode powerMode) {
        updateOperate();
    }

    private void updateOperate() {
        operate = operateInIdle | (cpuState.getPowerMode()== TxCPUState.PowerMode.RUN);
    }

    @Override
    public String toString() {
        int requestLevel = ((TxInterruptController) interruptController).getRequestLevel(TxInterruptController.INTTB0 + timerNumber);
        return "Timer " + Format.asHex(timerNumber, 1) + " : TB" + Format.asHex(timerNumber, 1) + "EN=0x" + Format.asHex(getEn(), 2)
                + ", TB" + Format.asHex(timerNumber, 1) + "RUN=0x" + Format.asHex(getRun(), 2)
                + ", level=0b" + Format.asBinary(requestLevel, 3) + (requestLevel==0?" (interrupt disabled)":" (interrupt enabled)")
                + ", RG0=" + rg0 + ", RG1=" + rg1
                + ", value=" + currentValue;
    }

}
