package com.nikonhacker.emu.peripherials.clock.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.clock.ClockGenerator;

/**
 * This is based on hardware specification CM71-10147-2E.pdf, sections 7 and 8
 * (Clock Generating and Clock Division Control parts)
 */
public class FrClockGenerator extends ClockGenerator {

    private static final int F_OSC_HZ = 200_000_000; //MHz


    private static final int CSELR_CKS_MASK  = 0b00000011;
    private static final int CSELR_MCEN_MASK = 0b00100000;
    private static final int CSELR_PCEN_MASK = 0b01000000;

    private static final int CMONR_CKM_MASK   = 0b00000011;
    private static final int CMONR_MCRDY_MASK = 0b00100000;
    private static final int CMONR_PCRDY_MASK = 0b01000000;

    private static final int CSTBR_MOSW_MASK = 0b00001111;

    private static final int PLLCR_ODS_MASK = 0b00110000_00000000;
    private static final int PLLCR_PMS_MASK = 0b00001111_00000000;
    private static final int PLLCR_PTS_MASK = 0b00000000_11110000;
    private static final int PLLCR_PDS_MASK = 0b00000000_00001111;


    private static final int DIVR0_DIVB_MASK = 0b11100000;

    private static final int DIVR1_TSTP_MASK = 0b10000000;
    private static final int DIVR1_DIVT_MASK = 0b01110000;

    private static final int DIVR2_DIVP_MASK = 0b11110000;


    // generation registers

    /** Clock source select register */
    private int cselr = CSELR_MCEN_MASK;

    /** Clock source monitor register is faked */
    // private int cmonr;

    /** Clock stabilization time select register */
    private int cstbr;

    /** PLL configuration register */
    private int pllcr = PLLCR_PTS_MASK;


    // division registers

    /** Divide clock configuration register 0 */
    private int divr0;

    /** Divide clock configuration register 1 */
    private int divr1 = 0b00010000;

    /** Divide clock configuration register 2 */
    private int divr2 = 0b00110000;


    // Computed frequencies

    private int fMainClkHz;
    private int fPllClkHz;
    private int fSrcClkHz;
    private int fBClkHz;
    private int fCClkHz;
    private int fHClkHz;
    private int fTClkHz;
    private int fPClkHz;


    public FrClockGenerator() {
        super();
    }

    @Override
    public void setPlatform(Platform platform) {
        super.setPlatform(platform);

        computeFrequencies();
    }


    // generation registers

    public int getCselr() {
        return cselr & (CSELR_CKS_MASK | CSELR_MCEN_MASK | CSELR_PCEN_MASK);
    }

    public void setCselr(int cselr) {
        this.cselr = cselr;
        if (!isCselrMcenSet()) {
            // This bit (PCEN) is changed to "0" when the MCEN bit (MCEN = 0) is specified to stop the oscillation of the main clock (MAINCLK).
            this.cselr = this.cselr & ~CSELR_PCEN_MASK;
        }
        computeFrequencies();
    }

    private int getCselrCks() {
        return cselr & CSELR_CKS_MASK;
    }

    private boolean isCselrMcenSet() {
        return (cselr & CSELR_MCEN_MASK) != 0;
    }

    private boolean isCselrPcenSet() {
        return (cselr & CSELR_PCEN_MASK) != 0;
    }


    public int getCmonr() {
        // Always pretend clocks are ready by returning the requested status
        return getCselr();
    }


    public int getCstbr() {
        return cstbr & CSTBR_MOSW_MASK;
    }

    public void setCstbr(int cstbr) {
        this.cstbr = cstbr;
        computeFrequencies();
    }


    public int getPllcr() {
        return pllcr & (PLLCR_ODS_MASK | PLLCR_PMS_MASK | PLLCR_PTS_MASK | PLLCR_PDS_MASK);
    }

    public void setPllcr(int pllcr) {
        if ((pllcr & PLLCR_PDS_MASK) != 0b0000) {
            throw new RuntimeException("THE PLLCR:PDS bits cannot be set to " + Format.asBinary(pllcr & PLLCR_PDS_MASK, 4) + ". Spec says \"These bits must always be set to 0000\"");
        }
        this.pllcr = pllcr;
        computeFrequencies();
    }

    private int getPllcrOds() {
        return (pllcr & PLLCR_ODS_MASK) >> 12;
    }

    private int getPllcrPms() {
        return (pllcr & PLLCR_PMS_MASK) >> 8;
    }

    private int getPllcrPts() {
        return (pllcr & PLLCR_PTS_MASK) >> 4;
    }

    private int getPllcrPds() {
        return pllcr & PLLCR_PDS_MASK;
    }

    private int getPllClkMultipleRate() {
        return getPllcrPms() + 1;
    }


    // division registers

    public int getDivr0() {
        return divr0 & DIVR0_DIVB_MASK | 0b11; // Last 2 reserved bits are read as "1"
    }

    public void setDivr0(int divr0) {
        this.divr0 = divr0;
        computeFrequencies();
    }

    public int getDivr0Divb() {
        return (divr0 & DIVR0_DIVB_MASK) >> 5;
    }

    public int getBClkDivisionRate() {
        return getDivr0Divb() + 1;
    }


    public int getDivr1() {
        return divr1;
    }

    public void setDivr1(int divr1) {
        this.divr1 = divr1;
        computeFrequencies();
    }

    public boolean isDivr1TstpSet() {
        return (divr1 & DIVR1_TSTP_MASK) != 0;
    }

    public int getDivr1Divt() {
        return (divr1 & DIVR1_DIVT_MASK) >> 4;
    }

    public int getTClkClockDivisionRate() {
        return getDivr1Divt() + 1;
    }


    public int getDivr2() {
        return divr2;
    }

    public void setDivr2(int divr2) {
        this.divr2 = divr2;
        computeFrequencies();
    }

    public int getDivr2Divp() {
        return (divr2 & DIVR2_DIVP_MASK) >> 4;
    }

    public int getPClkClockDivisionRate() {
        return getDivr2Divp() + 1;
    }


    // Resulting frequencies

    private int getMainClkFrequency() {
        return fMainClkHz;
    }

    private int getPllClkFrequency() {
        return fPllClkHz;
    }

    /**
     * The following 2 clocks can be selected for the source clock (SRCCLK):
     * Main clock (MAINCLK)
     * PLL clock (PLLCLK)
     */
    public int getSrcClkFrequency() {
        return fSrcClkHz;
    }

    /**
     * Base clock (BCLK) = Source clock (SRCCLK) divided by a value from 1 to 8
     */
    public int getBClkFrequency() {
        return fBClkHz;
    }

    /**
     * CPU clock (CCLK) = Base clock (BCLK) divided by 1 (undivided)
     */
    public int getCClkFrequency() {
        /** Source clock @132MHz seems to be a frequently documented value */
        //return 132_000_000; // 132MHz
        return fCClkHz;
    }

    /**
     * On-chip bus clock (HCLK) = Base clock (BCLK) divided by 1 (undivided)
     */
    public int getHClkFrequency() {
        return fHClkHz;
    }

    /**
     * External output clock (TCLK) = Base clock (BCLK) divided by a value from 1 to 8
     */
    public int getTClkFrequency() {
        return fTClkHz;
    }

    /**
     * Peripheral clock (PCLK) = Base clock (BCLK) divided by a value from 1 to 16
     */
    public int getPClkFrequency() {
        // PCLK @50MHz was determined based on the system clock ticking every ms
        //return 50_000_000;
        return fPClkHz;
    }

    /**
     * Called when a register influencing frequencies has been modified
     * This implements the clock cascade as expressed in figure of section 5.2
     */
    private void computeFrequencies() {
        fMainClkHz = isCselrMcenSet() ? F_OSC_HZ : 0;
        fPllClkHz = isCselrPcenSet() ? fMainClkHz * getPllClkMultipleRate() : 0;
        switch (getCselrCks()) {
            case 0b00:
            case 0b01:
                fSrcClkHz = fMainClkHz / 2;
                break;
            case 0b10:
                fSrcClkHz = fPllClkHz;
                break;
            default:
                throw new RuntimeException("Value " + getCselrCks() + " is prohibited for CSEL:CKS");
        }
        fBClkHz = fSrcClkHz / getBClkDivisionRate();
        fCClkHz = fBClkHz;
        fHClkHz = fBClkHz;
        fTClkHz = isDivr1TstpSet() ? 0 : fBClkHz / getTClkClockDivisionRate();
        fPClkHz = fBClkHz / getPClkClockDivisionRate();

//        System.out.println("@PC=0x" + Format.asHex(platform.getCpuState().getPc(), 8) + ": CCLK is " + fCClkHz + "Hz and PCLK is " + fPClkHz + "Hz");

        // TODO handle modes (STOP, MAIN_TIMER, SLEEP, DOZE, BUS_SLEEP)
//        switch (((TxCPUState)platform.getCpuState()).getPowerMode()) {
//            case STOP:
//            case BACKUP_STOP:
//                fCpuHz = 0;
//                break;
//            case SLEEP:
//            case BACKUP_SLEEP:
//                fCpuHz = fsHz;
//                break;
//            default: // RUN = NORMAL or SLOW
//                fCpuHz = fsysHz;
//                break;
//        }

        platform.getMasterClock().requestIntervalComputing();
    }

    // TODO is Main timer used outside of stabilization period ?
}
