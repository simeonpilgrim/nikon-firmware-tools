package com.nikonhacker.emu.peripherials.clock.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.clock.ClockGenerator;

/**
 * In Tx CPU, the Clock Generator can be configured specifically, and its output is used by peripherials
 * See HW spec, section 5
 */
public class TxClockGenerator implements ClockGenerator {

    private static final int F_OSC_HZ = 80_000_000; // High speed oscillator, 80MHz
    private static final int FS_HZ    = 1_000_000; // Low speed oscillator, say 1MHz

    private int sysCr0;
    private int pllSel;

    private int gearDivider = 1;
    private int prescaler;
    private int prescalerDivider = 2;






    // TODO don't store individual fields ! Store the register value and add getters for fields




    private boolean fpSel;
    private int     scoSel;
    private int     nmiFlg;
    private int rstFlg = 0b0001; // Power-on reset by default

    public TxClockGenerator() {
    }

    /**
     * 5.5 Prescaler Clock Controller
     * Each internal I/O (TMRB0-11, SIO0-2, HSIO0-2 and SBI0) has a prescaler for dividing a clock. The clock φT0
     * to be input to each prescaler is obtained by selecting the "fperiph" clock at the SYSCR1<FPSEL> and then dividing
     * the clock according to the setting of SYSCR1<PRCK2:0>. After the controller is reset, fperiph/2 is selected as φT0.
     */

    public byte getSysCr0() {
        return (byte) sysCr0;
    }

    public void setSysCr0(byte sysCr0) {
        this.sysCr0 = sysCr0 & 0b111;
        switch (this.sysCr0) {
            case 0b000: gearDivider = 1; break;
            case 0b100: gearDivider = 2; break;
            case 0b101: gearDivider = 4; break;
            case 0b110: gearDivider = 8; break;
            case 0b111: gearDivider = 16; break;
            default: throw new RuntimeException("Unrecognized gear in register SYSCR0 : 0b" + Format.asHex(this.sysCr0, 8));
        }
    }

    public byte getSysCr1() {
        return (byte) ((fpSel?0b10000:0) | prescaler) ;
    }

    public void setSysCr1(byte sysCr1) {
        fpSel = ((sysCr1 & 0b10000) != 0);
        prescaler = sysCr1 & 0b111;
        switch (prescaler) {
            case 0b000: prescalerDivider = 2; break;
            case 0b001: prescalerDivider = 4; break;
            case 0b010: prescalerDivider = 8; break;
            case 0b011: prescalerDivider = 16; break;
            case 0b100: prescalerDivider = 32; break;
            default: throw new RuntimeException("Unrecognized prescaler in register SYSCR1 : 0b" + Format.asHex(prescaler, 8));
        }
    }

    public byte getSysCr2() {
        return (byte) scoSel;
    }

    public void setSysCr2(byte sysCr2) {
        scoSel = sysCr2 & 0b11;
    }

    public void setSysCr(int value) {
        setSysCr0((byte) (value & 0xFF));
        setSysCr1((byte) ((value >> 8) & 0xFF));
        setSysCr2((byte) ((value >> 16) & 0xFF));
    }

    public int getPllSel() {
        return pllSel;
    }

    public void setPllSel(int pllsel) {
        boolean wasPllselSet = isPllselSet();
        this.pllSel = pllsel & 0x1;
        if (isPllselSet() != wasPllselSet) {
            // TODO
            //notifyFrequencyChange();
        }
    }

    public boolean isPllselSet() {
        return pllSel != 0;
    }

    public int readAndClearNmiFlag() {
        int value = nmiFlg;
        nmiFlg = 0;
        return value;
    }

    public int getNmiFlg() {
        return nmiFlg;
    }

    public void setNmiFlg(int nmiFlg) {
        this.nmiFlg = nmiFlg;
    }

    public int getRstFlg() {
        return rstFlg;
    }

    public void setRstFlg(int rstFlg) {
        this.rstFlg = rstFlg;
    }


    public int getT0Hz() {
        return getFPeriphHz() / prescalerDivider;
    }

    public int getFcHz() {
        if (isPllselSet()) {
            // TODO
            return F_OSC_HZ;
        }
        else {
            return F_OSC_HZ;
        }
    }

    public int getFPeriphHz() {
        if (fpSel) {
            return F_OSC_HZ;
        }
        else {
            return getFGear();
        }
    }

    public int getFsysHz() {
        // TODO
        return F_OSC_HZ;
    }

    private int getFGear() {
        return F_OSC_HZ / gearDivider;
    }
}
