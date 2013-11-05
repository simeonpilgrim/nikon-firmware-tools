package com.nikonhacker.emu.peripherials.adConverter.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.CycleCounterListener;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.peripherials.adConverter.AdUnit;
import com.nikonhacker.emu.peripherials.adConverter.AdValueProvider;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;

public class TxAdUnit implements AdUnit, CycleCounterListener {
    private static final int CLK_ADCLK_MASK     = 0b00000111;
    private static final int CLK_TSH_MASK       = 0b11110000;

    private static final int MOD0_ADS_MASK      = 0b00000001;
    private static final int MOD0_SCAN_MASK     = 0b00000010;
    private static final int MOD0_REPEAT_MASK   = 0b00000100;
    private static final int MOD0_ITM_MASK      = 0b00011000;

    private static final int MOD1_ADCH_MASK     = 0b00001111;
    private static final int MOD1_ADSCN_MASK    = 0b00100000;

    private static final int MOD2_HPADCH_MASK   = 0b00001111;
    private static final int MOD2_HPADCE_MASK   = 0b00100000;

    private static final int MOD3_5_ADOBSV_MASK = 0b00000001;
    private static final int MOD3_5_REGSA_MASK  = 0b00011110;
    private static final int MOD3_5_ADOBICA_MASK= 0b00100000;

    private static final int MOD4_ADHTG_MASK    = 0b00010000;
    private static final int MOD4_ADHS_MASK     = 0b00100000;
    private static final int MOD4_HADHTG_MASK   = 0b01000000;
    private static final int MOD4_HADHS_MASK    = 0b10000000;

    private static final int REGSP = 8;

    private static final int NORMAL_PRIORITY = 0;
    private static final int TOP_PRIORITY = 1;

    private final char unitName;
    private final int numChannels;
    private Emulator emulator;
    private TxInterruptController interruptController;
    private AdValueProvider provider;

    private int clk;

    private int mod0;
    private int mod1;
    private int mod2;
    private int mod3;
    private int mod4;
    private int mod5;

    /**
     * reg array contains REG0 > REG3 (units A,B) or REG0 > REG7 (unit C)
     * + REGSP at position 8
     */
    private int reg[];

    /**
     * This is the comparison value corresponding to COMREG0
     */
    private int comparisonValue0;
    /**
     * This is the comparison value corresponding to COMREG1
     */
    private int comparisonValue1;

    private boolean isEoc[] = new boolean[2];
    private boolean isBusy[] = new boolean[2];

    private int firstScanChannel, lastScanChannel, currentScanChannel;

    private long conversionIntervalCycles;
    private int numCycles = 0;

    private int conversionNumber;
    private int conversionInterruptInterval;

    public TxAdUnit(char unitName, int numChannels, Emulator emulator, TxInterruptController interruptController, AdValueProvider provider) {
        this.unitName = unitName;
        this.numChannels = numChannels;
        this.emulator = emulator;
        this.interruptController = interruptController;
        this.provider = provider;
        reset();
    }

    private void reset() {
        emulator.removeCycleCounterListener(this);
        numCycles = 0;
        reg = new int[9]; // 0..3 or 0..7 + 8 for SP
        isEoc[0] = false;
        isEoc[1] = false;
        isBusy[0] = false;
        isBusy[1] = false;
    }

    public char getUnitName() {
        return unitName;
    }

    public int getNumChannels() {
        return numChannels;
    }

    public int getClk() {
        return clk;
    }

    public int getClkAdclk() {
        return clk & CLK_ADCLK_MASK;
    }

    public int getSampleHoldTimeInConversionClock() {
        switch ((clk & CLK_TSH_MASK) >> 4) {
            case 0b1000: return 8;
            case 0b1001: return 16;
            case 0b1010: return 24;
            case 0b1011: return 32;
            case 0b0011: return 64;
            case 0b1100: return 128;
            case 0b1101: return 512;
            default: throw new RuntimeException(toString() + " Error: invalid tSH value : 0b" + Format.asBinary((clk & CLK_TSH_MASK) >> 4, 4));
        }
    }

    public void setClk(int clk) {
        this.clk = clk;
    }

    public int getMod0() {
        int value = (isEoc[NORMAL_PRIORITY] ? 0b10000000 : 0) | (isBusy[NORMAL_PRIORITY] ? 0b01000000 : 0) | (mod0 & 0b00011110);
        // <EOCF> is cleared to "0" upon read
        isEoc[NORMAL_PRIORITY] = false;
        return value;
    }

    private boolean isMod0Ads() {
        return (mod0 & MOD0_ADS_MASK) != 0;
    }

    public boolean isMod0Scan() {
        return (mod0 & MOD0_SCAN_MASK) != 0;
    }

    public boolean isMod0Repeat() {
        return (mod0 & MOD0_REPEAT_MASK) != 0;
    }

    private int getMod0Itm() {
        return mod0 & MOD0_ITM_MASK;
    }

    public void setMod0(int mod0) {
        this.mod0 = mod0 & 0b00011111;
        if (isMod0Ads()) {
            startConversion(NORMAL_PRIORITY);
        }
    }

    public int getMod1() {
        return mod1;
    }

    public int getMod1Adch() {
        return mod1 & MOD1_ADCH_MASK;
    }

    public boolean isMod1Adscn() {
        return (mod1 & MOD1_ADSCN_MASK) != 0;
    }

    public void setMod1(int mod1) {
        this.mod1 = mod1;
        // TODO I2AD Idle mode
    }

    public int getMod2() {
        int value = (isEoc[TOP_PRIORITY] ? 0b10000000 : 0) | (isBusy[TOP_PRIORITY] ? 0b01000000 : 0) | (mod2 & 0b00111111);
        // The EOCFHP Flag is cleared upon read.
        isEoc[TOP_PRIORITY] = false;
        return value;
    }

    private int getMod2Hpadch() {
        return mod2 & MOD2_HPADCH_MASK;
    }

    private boolean isMod2Hpadce() {
        return (mod2 & MOD2_HPADCE_MASK) != 0;
    }

    public void setMod2(int mod2) {
        this.mod2 = mod2 & 0b0011111;
        if (isMod2Hpadce()) {
            // Start top priority conversion
            startConversion(TOP_PRIORITY);
        }
    }

    public int getMod3() {
        return mod3 & 0b00111111;
    }

    private boolean isMod3Adobsv() {
        return (mod3 & MOD3_5_ADOBSV_MASK) != 0;
    }

    private int getMod3Regsa() {
        return mod3 & MOD3_5_REGSA_MASK;
    }

    private boolean isMod3Adobic() {
        return (mod3 & MOD3_5_ADOBICA_MASK) != 0;
    }

    public void setMod3(int mod3) {
        this.mod3 = mod3 & 0b10111111;
    }

    public int getMod4() {
        return mod4 & 0b11110000;
    }

    private boolean isMod4AdhtgSet() {
        return (mod4 & MOD4_ADHTG_MASK) != 0;
    }

    private boolean isMod4AdhsSet() {
        return (mod4 & MOD4_ADHS_MASK) != 0;
    }

    private boolean isMod4HadhtgSet() {
        return (mod4 & MOD4_HADHTG_MASK) != 0;
    }

    private boolean isMod4HadhsSet() {
        return (mod4 & MOD4_HADHS_MASK) != 0;
    }

    public void setMod4(int mod4) {
        // if ADRST goes from 10 to 01, perform a Software reset
        if ((this.mod4 & 0b11) == 0b10 && (mod4 & 0b11)== 0b01) {
            reset();
        }
        this.mod4 = mod4 & 0b11110011;
        // TODO: Hardware triggers
        if (isMod4AdhtgSet()) {
            System.err.println(toString() + " Error: Hardware trigger by " + (isMod4AdhsSet()?"timer":"external TRG") + " not implemented");
        }
        if (isMod4HadhtgSet()) {
            System.err.println(toString() + " Error: Top priority hardware trigger by " + (isMod4HadhsSet()?"timer":"external TRG") + " not implemented");
        }
    }

    public int getMod5() {
        return mod5 & 0b00111111;
    }

    private boolean isMod5Adobsv() {
        return (mod5 & MOD3_5_ADOBSV_MASK) != 0;
    }

    private int getMod5Regsa() {
        return mod5 & MOD3_5_REGSA_MASK;
    }

    private boolean isMod5Adobic() {
        return (mod5 & MOD3_5_ADOBICA_MASK) != 0;
    }


    public void setMod5(int mod5) {
        this.mod5 = mod5 & 0b10111111;
    }

    public int getReg(int regNumber) {
        return reg[regNumber];
    }

    public void setReg(int regNumber, int reg) {
        this.reg[regNumber] = reg;
    }

    public int getRegSp() {
        return reg[REGSP];
    }

    public void setRegSp(int regSp) {
        this.reg[REGSP] = regSp;
    }

    public int getComReg0() {
        // Because storage registers assigned to perform the A/D monitor function are usually not read by software,
        // overrun flag <OVRn> is always set and the conversion result storage flag <ADnRRF> is also set
        return comparisonValue0 << 6 & 0b11;
    }

    public void setComReg0(int comReg0) {
        this.comparisonValue0 = comReg0 >> 6;
    }

    public int getComReg1() {
        // Because storage registers assigned to perform the A/D monitor function are usually not read by software,
        // overrun flag <OVRn> is always set and the conversion result storage flag <ADnRRF> is also set
        return comparisonValue1 << 6 & 0b11;
    }

    public void setComReg1(int comReg1) {
        this.comparisonValue1 = comReg1 >> 6;
    }


    private void startConversion(int priorityType) {
        if (!isBusy[priorityType]) {
            // Preparation
            isBusy[priorityType] = true;
            isEoc[priorityType] = false;
            // top priority is always fixed
            // normal priority can be fixed or scan
            if (priorityType == NORMAL_PRIORITY) {
                if (isMod0Scan()) {
                    firstScanChannel = 0;
                    lastScanChannel = getMod1Adch();
                    if ((unitName == 'C') && !isMod1Adscn() && lastScanChannel > 3) {
                        // If 4 channel scan, change first channel to 4
                        // Spec is not very clear for ADMOD1:ADSCN. It says setting it to 1 is "prohibited"...
                        // But then it gives meanings for both 0 and 1. I guess for units A and B it doesn't matter, and
                        // the "AN0" etc. should read "ANn0" in both columns
                        // For unit C, it only changes the start channel from 0 to 4.
                        firstScanChannel = 4;
                    }
                }
                else {
                    // fixed
                    firstScanChannel = getMod1Adch();
                    lastScanChannel = firstScanChannel;
                }
                currentScanChannel = firstScanChannel;
                if (isMod0Repeat()) {
                    conversionNumber = 0;
                    if (!isMod0Scan()) {
                        // We're in fixed repeat. Determine requested Interrupt interval
                        switch (getMod0Itm()) {
                            case 0b00:
                                conversionInterruptInterval = 1;
                                break;
                            case 0b01:
                                conversionInterruptInterval = 4; break;
                            case 0b10:
                                if (unitName != 'C') {
                                    throw new RuntimeException(toString() + " Error: setting MOD0:ITM to " + getMod0Itm() + " is invalid for unit " + unitName);
                                }
                                conversionInterruptInterval = 8; break;
                            default:
                                throw new RuntimeException(toString() + " Error: setting MOD0:ITM to " + getMod0Itm() + " is invalid");
                        }
                    }
                    else {
                        // Scan repeat
                        conversionInterruptInterval = 1;
                    }
                }
            }
            // determine conversion interval
            // TODO: this is plain wrong:
            conversionIntervalCycles = getClkAdclk();
            // register timer (add is protected against multiple adds of the same listener)
            emulator.addCycleCounterListener(this);
        }
    }


    @Override
    public boolean onCycleCountChange(long oldCount, int increment) {
        numCycles += increment;
        if (numCycles < conversionIntervalCycles) {
            return true;
        }
        else {
            boolean continueNotifying = true;
            numCycles -= conversionIntervalCycles;
            if (isBusy[TOP_PRIORITY]) {
                // top-priority conversion is handled first
                // TODO: normally, a full conversion cycle should pass before this one starts
                // top priority is always single fixed
                setConvertedValue(REGSP, provider.getAnalogValue(unitName, getMod2Hpadch()) & 0x3FF);
                isEoc[TOP_PRIORITY] = true;
                isBusy[TOP_PRIORITY] = false;
                requestAdTopPriorityCompleteInterrupt();
                if (!isBusy[NORMAL_PRIORITY]) {
                    continueNotifying = false;
                }
            }
            else {
                // Hopefully this reflects timings from the table at p 17-20 of the spec:
                // "Relationships between A/D Conversion Modes, Interrupt Generation Timings and Flag Operations"
                // And assignments described in tables at page 17-22 of the spec
                if (currentScanChannel < lastScanChannel) {
                    // Scan still in progress
                    // target register is according to scanned channel
                    setConvertedValue(currentScanChannel, provider.getAnalogValue(unitName, currentScanChannel) & 0x3FF);
                    currentScanChannel++;
                    // Continue scanning
                }
                else {
                    // Fixed, or channel scan complete
                    if (isMod0Repeat()) {
                        // Repeat mode
                        if (!isMod0Scan()) {
                            // Repeat fixed mode
                            // target register is according to number of conversions performed
                            setConvertedValue(conversionNumber, provider.getAnalogValue(unitName, currentScanChannel) & 0x3FF);
                            conversionNumber++;
                            if (conversionNumber == conversionInterruptInterval) {
                                requestAdCompleteInterrupt();
                                // <EOCF> is set with the same timing as this interrupt INTAD is generated
                                // ADnMOD <EOCF> is set to "1."
                                isEoc[NORMAL_PRIORITY] = true;
                                // ADnMOD0 <ADBF> is not cleared to "0." It remains at "1."

                                conversionNumber = 0;
                            }
                        }
                        else {
                            // Repeat scan mode, scan complete.
                            // target register is according to scanned channel
                            setConvertedValue(currentScanChannel, provider.getAnalogValue(unitName, currentScanChannel) & 0x3FF);
                            requestAdCompleteInterrupt();
                            // ADnMOD <EOCF> is set to "1."
                            isEoc[NORMAL_PRIORITY] = true;
                            // ADnMOD0 <ADBF> is not cleared to "0." It remains at "1."
                        }
                        // Start over
                        currentScanChannel = firstScanChannel;
                    }
                    else {
                        // Single fixed mode, or single channel scan complete
                        // target register is according to scanned channel
                        setConvertedValue(currentScanChannel, provider.getAnalogValue(unitName, currentScanChannel) & 0x3FF);
                        isEoc[NORMAL_PRIORITY] = true;
                        isBusy[NORMAL_PRIORITY] = false;
                        requestAdCompleteInterrupt();
                        if (!isBusy[TOP_PRIORITY]) {
                            continueNotifying = false;
                        }
                    }
                }
            }
            return continueNotifying;
        }
    }

    private void setConvertedValue(int regNumber, int value) {
        setReg(regNumber, (value << 6) | 0b1); // TODO OVR=overrun ?
        // Monitor 1
        if (isMod3Adobsv() && regNumber == normalizeRegsa(getMod3Regsa())) {
            compareAndInterrupt(value, comparisonValue0, isMod3Adobic());
        }
        if (isMod5Adobsv() && regNumber == normalizeRegsa(getMod5Regsa())) {
            compareAndInterrupt(value, comparisonValue1, isMod5Adobic());
        }
    }

    private int normalizeRegsa(int regsa) {
        return (regsa < 8)?regsa:8;
    }

    private void compareAndInterrupt(int value, int comparisonValue, boolean isAdobic) {
        if (isAdobic) {
            if (value > comparisonValue) {
                requestAdMonitoringInterrupt();
            }
        }
        else {
            if (value < comparisonValue) {
                requestAdMonitoringInterrupt();
            }
        }
    }

    private boolean requestAdCompleteInterrupt() {
        return interruptController.request(TxInterruptController.INTADA + (unitName - 'A'));
    }

    private boolean requestAdTopPriorityCompleteInterrupt() {
        return interruptController.request(TxInterruptController.INTADHPA + 2 * (unitName - 'A'));
    }

    private boolean requestAdMonitoringInterrupt() {
        return interruptController.request(TxInterruptController.INTADMA + 2 * (unitName - 'A'));
    }

    @Override
    public String toString() {
        return "A/D Unit " + unitName;
    }
}
