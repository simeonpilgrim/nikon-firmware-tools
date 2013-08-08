package com.nikonhacker.emu.peripherials.clock.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.clock.ClockGenerator;

/**
 * In Tx CPU, the Clock Generator can be configured specifically, and its output is used by peripherials
 * See HW spec, section 5
 */
public class TxClockGenerator extends ClockGenerator {

    /**
     * High speed oscillator in the range 8-10MHz (see section 5.1.1)
     * Let's take 10MHz
     */
    private static final int F_HI_SPEED_OSC_HZ = 10_000_000;

    /**
     *  Low speed oscillator in the range 30-34kHz (see section 5.1.1)
     *  Let's take 30kHz
     */
    private static final int F_LO_SPEED_OSC_HZ = 30_000;


    // Registers

    private int syscr  = 0b00000000_00000001_00000000_00000000;
    private int osccr  = 0b00000000_00000000_00000001_00010000;
    private int stbycr = 0b00000000_00000000_00000001_00000011;
    private int pllsel = 0;
    private int cksel  = 0;
    private int rstflg = 0b00000000_00000000_00000000_00000001; // Power-on reset by default

    // Computed frequencies
    private int foscHz;
    private int fpllHz;
    private int fcHz;
    private int fsHz;
    private int fgearHz;
    private int fperiphHz;
    private int fsysHz;
    private int ft0Hz;
    private int fscOutHz;
    private int fCpuHz;

    public TxClockGenerator() {
        super();
    }

    @Override
    public void setPlatform(Platform platform) {
        super.setPlatform(platform);

        computeFrequencies();
    }

    public int getSyscr() {
        return syscr;
    }

    public void setSyscr(int syscr) {
        this.syscr = syscr;

        computeFrequencies();
    }

    public byte getSyscr0() {
        return (byte) (syscr & 0b00000111);
    }

    public void setSyscr0(byte syscr0) {
        syscr = (syscr & 0xFFFF_FF00) | syscr0;

        computeFrequencies();
    }

    public int getSyscrGearDivider() {
        switch (syscr & 0b00000111) {
            case 0b000: return 1;
            case 0b100: return 2;
            case 0b101: return 4;
            case 0b110: return 8;
            case 0b111: return 16;
            default: throw new RuntimeException("Unrecognized gear in register SYSCR0 : 0b" + Format.asBinary(syscr & 0b00000111, 8));
        }
    }

    public byte getSyscr1() {
        return (byte) ((syscr >> 8) & 0xFF);
    }

    public void setSyscr1(byte syscr1) {
        syscr = (syscr & 0xFFFF_00FF) | (syscr1 << 8);

        computeFrequencies();
    }

    public int getSyscrPrescalerDivider() {
        switch ((syscr & 0b00000111_00000000) >> 8) {
            case 0b000: return 2;
            case 0b001: return 4;
            case 0b010: return 8;
            case 0b011: return 16;
            case 0b100: return 32;
            default: throw new RuntimeException("Unrecognized prescaler in register SYSCR1 : 0b" + Format.asBinary((syscr & 0b00000111_00000000) >> 8, 8));
        }
    }

    public boolean isSyscrFpselSet() {
        return (syscr & 0b0001_0000_0000_0000) != 0;
    }

    public byte getSyscr2() {
        return (byte)((syscr >> 16) & 0xFF);
    }

    public void setSyscr2(byte syscr2) {
        syscr = (syscr & 0xFF00_FFFF) | (syscr2 << 16);

        computeFrequencies();

    }

    public int getSyscrScosel() {
        return (syscr & 0b00000011_00000000_00000000) >> 16;
    }


    public int getOsccr() {
        return osccr & 0b00110011_01111101; // always pretend warm-up is completed (WUEF=0)
    }

    public void setOsccr(int osccr) {
        this.osccr = osccr;

        computeFrequencies();
    }

    public byte getOsccr0() {
        return (byte) (osccr & 0b01111101); // always pretend warm-up is completed (WUEF=0)
    }

    public void setOsccr0(byte osccr0) {
        osccr = (osccr & 0xFFFF_FF00) | osccr0;

        computeFrequencies();
    }

    public byte getOsccr1() {
        return (byte) ((osccr >> 8) & 0b00110011);
    }

    public void setOsccr1(byte osccr1) {
        osccr = (osccr & 0xFFFF_00FF) | (osccr1 << 8);

        computeFrequencies();
    }

    public boolean isOsscrPllonSet() {
        return (osccr & 0b00000000_00000100) != 0;
    }

    public boolean isOsccrXenSet() {
        return (osccr & 0b00000001_00000000) != 0;
    }

    public boolean isOsccrXtenSet() {
        return (osccr & 0b00000010_00000000) != 0;
    }

    public int getStbycr() {
        return stbycr & 0b00000011_00000011_00000111;
    }

    public void setStbycr(int stbycr) {
        this.stbycr = stbycr;

        adjustCpuStandbyMode();

        computeFrequencies();
    }

    private void adjustCpuStandbyMode() {
        switch (getStbycrStby()) {
            case 0b001:
                ((TxCPUState)platform.getCpuState()).setPowerMode(TxCPUState.PowerMode.STOP);
            case 0b010:
                ((TxCPUState)platform.getCpuState()).setPowerMode(TxCPUState.PowerMode.SLEEP);
            case 0b011:
                ((TxCPUState)platform.getCpuState()).setPowerMode(TxCPUState.PowerMode.IDLE);
            case 0b101:
                ((TxCPUState)platform.getCpuState()).setPowerMode(TxCPUState.PowerMode.BACKUP_STOP);
            case 0b110:
                ((TxCPUState)platform.getCpuState()).setPowerMode(TxCPUState.PowerMode.BACKUP_SLEEP);
            default:
                throw new RuntimeException("Unrecognized standby mode in register STBYCR : 0b" + Format.asBinary(stbycr & 0b00000111, 8));
        }
    }

    public int getStbycrStby() {
        return stbycr & 0b111;
    }

    public boolean isStbycrRxenSet() {
        return (stbycr & 0b00000001_00000000) != 0;
    }

    public boolean isStbycrRxtenSet() {
        return (stbycr & 0b00000010_00000000) != 0;
    }

    public boolean isStbycrDrveSet() {
        return (stbycr & 0b00000001_00000000_00000000) != 0;
    }

    public boolean isStbycrPtkeepSet() {
        return (stbycr & 0b00000010_00000000_00000000) != 0;
    }

    public int getPllsel() {
        return pllsel & 0b1;
    }

    public void setPllsel(int pllsel) {
        this.pllsel = pllsel;

        computeFrequencies();
    }

    public boolean isPllselSet() {
        return pllsel != 0;
    }


    public int getCksel() {
        return (cksel == 0)?0b00:0b11; // Pretend transitions are executed immediately : SYSCKFLG=SYSCK
    }

    public void setCksel(int cksel) {
        this.cksel = cksel & 0b10;
        computeFrequencies();
    }

    public boolean isCkselSysckSet() {
       return (cksel & 0b10) != 0;
    }



    public int getRstflg() {
        return rstflg;
    }

    public void setRstflg(int rstflg) {
        this.rstflg = rstflg;
    }


    /**
     * Called when a register influencing frequencies has been modified
     * This implements the clock cascade as expressed in figure of section 5.2
     */
    private void computeFrequencies() {
        foscHz = isOsccrXenSet() ? F_HI_SPEED_OSC_HZ : 0;
        fpllHz = isOsscrPllonSet() ? (8 * foscHz) : 0;
        fcHz = isPllselSet() ? fpllHz : foscHz;
        fsHz = isOsccrXtenSet() ? F_LO_SPEED_OSC_HZ : 0;
        fgearHz = fcHz /getSyscrGearDivider();
        fperiphHz = isSyscrFpselSet() ? fcHz : fgearHz;
        fsysHz = isCkselSysckSet() ? fsHz : fgearHz;
        ft0Hz = fperiphHz / getSyscrPrescalerDivider();
        switch (getSyscrScosel()) {
            case 0b00: fscOutHz = fsHz; break;
            case 0b01: fscOutHz = fsysHz /2; break;
            case 0b10: fscOutHz = fsysHz; break;
            case 0b11: fscOutHz = ft0Hz; break;
        }

        switch (((TxCPUState)platform.getCpuState()).getPowerMode()) {
            case STOP:
            case BACKUP_STOP:
                fCpuHz = 0;
                break;
            case SLEEP:
            case BACKUP_SLEEP:
                fCpuHz = fsHz;
                break;
            default: // RUN = NORMAL or SLOW
                fCpuHz = fsysHz;
                break;
        }

        platform.getMasterClock().requestIntervalComputing();
        // System.out.println(toString());
    }

    public int getFoscHz() {
        return foscHz;
    }

    public int getFpllHz() {
        return fpllHz;
    }

    public int getFcHz() {
        return fcHz;
    }

    public int getFsHz() {
        return fsHz;
    }

    public int getFgearHz() {
        return fgearHz;
    }

    public int getFperiphHz() {
        return fperiphHz;
    }

    public int getFsysHz() {
        return fsysHz;
    }

    /**
     * 5.5 Prescaler Clock Controller
     * Each internal I/O (TMRB0-11, SIO0-2, HSIO0-2 and SBI0) has a prescaler for dividing a clock. The clock φT0
     * to be input to each prescaler is obtained by selecting the "fperiph" clock at the SYSCR1<FPSEL> and then dividing
     * the clock according to the setting of SYSCR1<PRCK2:0>. After the controller is reset, fperiph/2 is selected as φT0.
     */
    public int getFt0Hz() {
        return ft0Hz;
    }

    public int getFscOutHz() {
        return fscOutHz;
    }

    public int getfCpuHz() {
        return fCpuHz;
    }

    @Override
    public String toString() {
        return "TxClockGenerator{" +
                "syscr=0x" + Format.asHex(syscr, 8) +
                ", osccr=0x" + Format.asHex(osccr, 8) +
                ", stbycr=0x" + Format.asHex(stbycr, 8) +
                ", pllsel=0x" + Format.asHex(pllsel, 8) +
                ", cksel=0x" + Format.asHex(cksel, 8) +
                ", rstflg=0x" + Format.asHex(rstflg, 8) +
                ", foscHz=" + foscHz +
                ", fpllHz=" + fpllHz +
                ", fcHz=" + fcHz +
                ", fsHz=" + fsHz +
                ", fgearHz=" + fgearHz +
                ", fperiphHz=" + fperiphHz +
                ", fsysHz=" + fsysHz +
                ", ft0Hz=" + ft0Hz +
                ", fscOutHz=" + fscOutHz +
                ", fCpuHz=" + fCpuHz +
                '}';
    }
}
