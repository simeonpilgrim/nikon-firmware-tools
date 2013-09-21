package com.nikonhacker.emu.peripherials.serialInterface.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.Prefs;
import com.nikonhacker.emu.Clockable;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.clock.tx.TxClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Behaviour is based on Toshiba documentation TMP19A44F10XBG_TMP19A44FEXBG_en_datasheet_100401.pdf
 */
public class TxSerialInterface extends SerialInterface implements Clockable {
    private static final int SERIAL_RX_FIFO_SIZE = 4;

    public static final int EN_SIOE_MASK = 0b00000001;

    private static final int CR_IOC_MASK  = 0b00000001;
    private static final int CR_FERR_MASK = 0b00000100;
    private static final int CR_PERR_MASK = 0b00001000;
    private static final int CR_OERR_MASK = 0b00010000;

    private static final int MOD0_SC_MASK   = 0b00000011;
    private static final int MOD0_SM_MASK   = 0b00001100;
    private static final int MOD0_WU_MASK   = 0b00010000;
    private static final int MOD0_RXE_MASK  = 0b00100000;
    private static final int MOD0_CTSE_MASK = 0b01000000;
    private static final int MOD0_TB8_MASK  = 0b10000000;

    private static final int MOD1_SINT_MASK    = 0b00001110;
    private static final int MOD1_TXE_MASK     = 0b00010000;
    private static final int MOD1_FDPX_MASK    = 0b01100000;
    private static final int MOD1_FDPX_RX_MASK = 0b00100000;
    private static final int MOD1_FDPX_TX_MASK = 0b01000000;

    private static final int BRCR_BRS_MASK    = 0b00001111;
    private static final int BRCR_BRCK_MASK   = 0b00110000;
    private static final int BRCR_BRADDE_MASK = 0b01000000;

    private static final int BRADD_BRK_MASK = 0b00001111;

    private static final int FCNF_CNFG_MASK    = 0b00000001;
    private static final int FCNF_RXTXCNT_MASK = 0b00000010;
    private static final int FCNF_RFIE_MASK    = 0b00000100;
    private static final int FCNF_TFIE_MASK    = 0b00001000;
    private static final int FCNF_RFST_MASK    = 0b00010000;

    private static final int RFC_RIL0_MASK = 0b00000001;
    private static final int RFC_RIL_MASK  = 0b00000011;
    private static final int RFC_RFIS_MASK = 0b01000000;
    private static final int RFC_RFCS_MASK = 0b10000000;

    private static final int TFC_TIL0_MASK = 0b00000001;
    private static final int TFC_TIL_MASK  = 0b00000011;
    private static final int TFC_TFIS_MASK = 0b01000000;
    private static final int TFC_TFCS_MASK = 0b10000000;

    private static final int MOD2_SWRST_MASK = 0b00000011;
    private static final int MOD2_TXRUN_MASK = 0b00100000;
    private static final int MOD2_RBFLL_MASK = 0b01000000;
    private static final int MOD2_TBEMP_MASK = 0b10000000;

    private static final int RST_RLVL_MASK = 0b00000111;
    private static final int RST_ROR_MASK  = 0b10000000;

    private static final int TST_TLVL_MASK = 0b00000111;
    private static final int TST_TUR_MASK  = 0b10000000;

    private static final int BRnADD_BRnK_MASK = 0b00001111;

    /**
     * Rx buffer
     */
    protected int rxBuf;

    /**
     * Rx FIFO
     */
    protected Queue<Integer> rxFifo = new LinkedList<Integer>();
    protected int rxInterruptFillLevel;

    /**
     * Tx buffer
     */
    protected int txBuf;

    /**
     * Tx FIFO.
     */
    protected Queue<Integer> txFifo = new LinkedList<Integer>();
    protected int txInterruptFillLevel;

    private int bitNumberBeingTransferred = 0;

    protected int en; // Enable register
    protected int cr; // Control register
    protected int mod0; // Mode control register 0
    protected int mod1; // Mode control register 1
    protected int mod2 = MOD2_TBEMP_MASK; // Mode control register 2
    protected int brcr; // Baud rate generator control register
    protected int bradd; // Baud rate generator control register 2
    protected int rfc; // Receive FIFO control register
    protected int tfc; // Transmit FIFO control register
    protected int rst; // Receive FIFO status register
    protected int tst = TST_TUR_MASK; // Transmit FIFO status register
    protected int   fcnf; // FIFO configuration register
    private   Prefs prefs;

    public TxSerialInterface(int serialInterfaceNumber, Platform platform, boolean logSerialMessages, Prefs prefs) {
        super(serialInterfaceNumber, platform, logSerialMessages);
        this.prefs = prefs;
    }


    public int getEn() {
        return en;
    }

    public void setEn(int en) {
//        System.out.println(getName() + ".setEn(0x" + Format.asHex(en, 8) + ")");
        this.en = en & EN_SIOE_MASK;
    }


    /**
     * This is the method called by assembly code to read data received via serial port
     */
    public int getBuf() {
//        if (en == 0) {
//            throw new RuntimeException("Attempt to receive data from disabled " + getName());
//        }
        Integer poll;

        if (!isFcnfCnfgSet()) { // FIFO disabled
            poll = rxBuf;
            clearMod2Rbfll();
        }
        else {
            poll = rxFifo.poll();
            clearRstRor();
            // TODO signal if empty ?
        }

        if (poll == null) {
//            System.err.println(getName() + " - Attempt to read from empty buffer");
            return 0;
        }
        return poll;
    }

    /**
     * This is the method called by assembly code to send data via serial port
     * @param buf the value to send
     */
    public void setBuf(int buf) {
//        System.out.println(getName() + ".setBuf(0x" + Format.asHex(buf, 8) + ")");
//        if (en == 0) {
//            throw new RuntimeException("Attempt to transmit data to disabled " + getName());
//        }
        if (isEnSet()) {
            if (!isFcnfCnfgSet()) { // FIFO disabled
                txBuf = buf;
                // TODO if UART mode with parity, set parity
                // TODO if UART mode 9 bits, set bit in MOD0:TB8
                clearMod2Tbemp();
            }
            else {
                txFifo.add(buf);
                // TODO signal if full ?
            }
            if (isMod1TxeSet()) {
                // Buffer was just written, and TX is enabled
                if (!prefs.isSerialTx19FixRequireRxeAndTxe()) {
                    startTransfer();
                }
                else {
                    // If RXE is also enabled (or we're not in full duplex)
                    if (isMod0RxeSet() || !isFullDuplex()) {
                        // Then start transfer
                        startTransfer();
                    }
                }
            }
        }
        else {
            // Used to reset buffer
            txBuf = buf;
        }
    }


    public int getCr() {
        // TODO in UART mode, if parity or 9-bit communication, set corresponding bits according to the value in buf or FIFO head!
        int value = cr;

        // Clear error flags upon reading
        cr = cr & 0b11100011;
        return value;
    }

    public void setCr(int cr) {
//        System.out.println(getName() + ".setCr(0x" + Format.asHex(cr, 8) + ")");

        // RB8 and error flags are not writable
        this.cr = (this.cr & 0b10011100) | (cr & 0b01100011);
    }


    public int getMod0() {
        return mod0;
    }

    public void setMod0(int mod0) {
//        System.out.println(getName() + ".setMod0(0x" + Format.asHex(mod0, 8) + ")");
        boolean previousRxEnabled = isMod0RxeSet();
        this.mod0 = mod0;
        boolean currentRxEnabled = isMod0RxeSet();

        if (prefs.isSerialTx19FixRequireRxeAndTxe()) {
            // If RX has just been enabled, and TX was already enabled (and we're in full duplex, otherwise transfer will already have started)
            if (currentRxEnabled && !previousRxEnabled && isMod1TxeSet() && isFullDuplex()) {
                // Then start now.
                startTransfer();
            }
        }

        if (getMod0Sm() != 0b00) {
            if (logSerialMessages) System.err.println(getName() + " is being configured as UART. Only I/O serial mode is supported for now");
        }
    }


    public int getMod1() {
        return mod1;
    }

    // Note: could be overridden in TxHSerial, because in that case, fill levels are independent of duplex mode, so no need to recompute them
    public void setMod1(int mod1) {
//        System.out.println(getName() + ".setMod1(0x" + Format.asHex(mod1, 8) + ")");
        boolean previousTxEnabled = isMod1TxeSet();
        this.mod1 = mod1;
        boolean currentTxEnabled = isMod1TxeSet();

        // And in case duplex mode changes
        computeRxFillLevel();
        computeTxFillLevel();

        // Check if TXE was just enabled.
        if (currentTxEnabled && !previousTxEnabled) {
            if (!prefs.isSerialTx19FixRequireRxeAndTxe()) {
                startTransfer();
            }
            else {
                // If TX has just been enabled, and either we're in half duplex or we're in full duplex and RX was already enabled
                if (isMod0RxeSet() || !isFullDuplex()) {
                    startTransfer();
                }
            }
        }
    }

    private void startTransfer() {
        // Signal if there are values waiting
        if (getNbTxValuesWaiting() > 0) {
            if (prefs.isSerialTx19FixInsertDelay()) {
                /* TEMPORARY WORKAROUND
                    coderat: found that interrupt_handler_INTTBF takes in 2.40 sometimes 1201 TX instructions
                     happening between setting TXE=1 and RXE=1 will break serial communication. Reasons are unknown.
                     BFC48198 65D9     move    r30, r17      ; 0xFF001806  HSC0MOD1
                     BFC4819A FA80     bset    0x00(r30), 4  ; TXE enable                       ; start of danger zone
                     BFC4819C 65DB     move    r30, r3       ; 0xFF00180D  HSC0MOD0
                     BFC4819E FAA0     bset    0x00(r30), 5  ; RXE enable                       ; end of danger zone

                     So add start delay of at least 1240 instruction @ 40 MHz = 31us
                 */
                int start_delay_bits = getNumBits() + getIntervalTimeInSclk() - ((31 * getFrequencyHz()) / 1000000);

                bitNumberBeingTransferred = ( start_delay_bits < 0 ? start_delay_bits : 0 );
            }
            // Insert delay of a few CPU cycles.
            platform.getMasterClock().add(this);
        }
    }


    public int getMod2() {
        return mod2;
    }

    public void setMod2(int mod2) {
//        System.out.println(getName() + ".setMod2(0x" + Format.asHex(mod2, 8) + ")");

        // if SWRST goes from 10 to 01, perform a Software reset
        if ((this.mod2 & MOD2_SWRST_MASK) == 0b10 && (mod2 & MOD2_SWRST_MASK)== 0b01) {
            clearMod0Rxe();
            clearMod1Txe();
            clearMod2Tbemp();
            clearMod2Rbfll();
            clearMod2Txrun();
            clearCrOerr();
            clearCrPerr();
            clearCrFerr();
        }

        // TBEMP, RBFLL, TXRUN are not writable
        this.mod2 = (this.mod2 & 0b11100000) | (mod2 & 0b00011111);
    }


    public int getBrcr() {
        return brcr;
    }

    public void setBrcr(int brcr) {
//        System.out.println(getName() + ".setBrcr(0x" + Format.asHex(brcr, 8) + ")");
        this.brcr = brcr;
    }


    public int getBradd() {
        return bradd;
    }

    public void setBradd(int bradd) {
//        System.out.println(getName() + ".setBradd(0x" + Format.asHex(bradd, 8) + ")");
        this.bradd = bradd & BRnADD_BRnK_MASK;
    }

    public int getRfc() {
        return rfc & 0b01011111;
    }

    public void setRfc(int rfc) {
        if ((rfc & RFC_RFCS_MASK) != 0) {
            rxFifo = new LinkedList<Integer>();
        }
        // TODO RFIS
        this.rfc = rfc;
        computeRxFillLevel();
    }

    private boolean isRfcRfisSet() {
        return (rfc & RFC_RFIS_MASK) != 0;
    }


    public int getTfc() {
        return tfc & ~TFC_TFCS_MASK;
    }

    public void setTfc(int tfc) {
        this.tfc = tfc;
        if (isTfcTfcsSet()) {
            txFifo = new LinkedList<Integer>();
        }
        // TODO TFIS
        computeTxFillLevel();
    }

    private boolean isTfcTfcsSet() {
        return (tfc & TFC_TFCS_MASK) != 0;
    }

    private boolean isTfcTfisSet() {
        return (tfc & TFC_TFIS_MASK) != 0;
    }


    public int getRst() {
        return rst | (rxFifo.size() & RST_RLVL_MASK);
    }

    /**
     * Clear RST Reception OverRun flag
     */
    protected void clearRstRor() {
        rst = rst & ~RST_ROR_MASK;
    }

    /**
     * Set RST Reception OverRun flag
     */
    protected void setRstRor() {
        rst = rst | RST_ROR_MASK;
    }


    public void setRst(int rst) {
        throw new RuntimeException(getName() + " RST register should not be written");
    }

    public int getTst() {
        throw new RuntimeException(getName() + "(H)SC0TST updating is not implemented !");
        //return tst;
    }

    public void setTst(int tst) {
        throw new RuntimeException(getName() + " TST register should not be written");
    }

    public int getFcnf() {
        return fcnf;
    }

    public void setFcnf(int fcnf) {
        this.fcnf = fcnf;
    }


    // Utility register field accessors

    /**
     * @return true if Enabled
     */
    protected boolean isEnSet() {
        return (en & EN_SIOE_MASK) != 0;
    }


    /*
     * @return true if I/O interface mode is in SCLK input clock mode (slave)
     */
    public boolean isCrIocSet() {
        return (cr & CR_IOC_MASK) != 0;
    }

    /**
     * Set CR Overrun error flag
     */
    protected void setCrOerr() {
        cr = cr | CR_OERR_MASK;
    }

    /**
     * Clear CR Overrun error flag
     */
    protected void clearCrOerr() {
        cr = cr & ~CR_OERR_MASK;
    }

    /**
     * Set CR Parity/Underrun error flag
     */
    protected void setCrPerr() {
        cr = cr | CR_PERR_MASK;
    }

    /**
     * Clear CR Parity/Underrun error flag
     */
    protected void clearCrPerr() {
        cr = cr & ~CR_PERR_MASK;
    }

    /**
     * Set CR Framing error flag
     */
    protected void setCrFerr() {
        cr = cr | CR_FERR_MASK;
    }

    /**
     * Clear CR Framing error flag
     */
    protected void clearCrFerr() {
        cr = cr & ~CR_FERR_MASK;
    }


    protected int getMod0Sc() {
        return mod0 & MOD0_SC_MASK;
    }

    protected int getMod0Sm() {
        return (mod0 & MOD0_SM_MASK) >> 2;
    }

    protected boolean isMod0WuSet() {
        return (mod0 & MOD0_WU_MASK) != 0;
    }

    protected boolean isMod0RxeSet() {
        return (mod0 & MOD0_RXE_MASK) != 0;
    }

    protected void clearMod0Rxe() {
        mod0 = mod0 & ~MOD0_RXE_MASK;
    }

    protected boolean isMod0CtseSet() {
        return (mod0 & MOD0_CTSE_MASK) != 0;
    }

    protected boolean isMod0Tb8Set() {
        return (mod0 & MOD0_TB8_MASK) != 0;
    }

    protected boolean isIoMode() {
        return getMod0Sm() == 0b00;
    }


    protected int getMod1Sint(){
        return (mod1 & MOD1_SINT_MASK) >> 1;
    }

    protected boolean isMod1TxeSet() {
        return (mod1 & MOD1_TXE_MASK) != 0;
    }

    protected void clearMod1Txe() {
        mod1 = mod1 & ~MOD1_TXE_MASK;
    }

    protected int getMod1Fdpx() {
        return (mod1 & MOD1_FDPX_MASK) >> 5;
    }

    protected boolean isMod1FdpxRxSet() {
        return (mod1 & MOD1_FDPX_RX_MASK) != 0;
    }

    protected boolean isMod1FdpxTxSet() {
        return (mod1 & MOD1_FDPX_TX_MASK) != 0;
    }

    private boolean isFullDuplex() {
        return getMod1Fdpx() == 0b11;
    }

    protected int getIntervalTimeInSclk() {
        switch (getMod1Sint()) {
            case 0b000: return 0;
            case 0b001: return 1;
            case 0b010: return 2;
            case 0b011: return 4;
            case 0b100: return 8;
            case 0b101: return 16;
            case 0b110: return 32;
            case 0b111: return 64;
            default:
                throw new RuntimeException("Error in TxSerialInterface.getIntervalTimeInSclk()");
        }
    }


    /**
     * Check if MOD2 Transmit Buffer Empty flag is set
     */
    protected boolean isMod2TbempSet() {
        return (mod2 & MOD2_TBEMP_MASK) != 0;
    }

    /**
     * Set MOD2 Transmit Buffer Empty flag
     */
    protected void setMod2Tbemp() {
        mod2 = mod2 | MOD2_TBEMP_MASK;
    }

    /**
     * Clear MOD2 Transmit Buffer Empty flag
     */
    protected void clearMod2Tbemp() {
        mod2 = mod2 & ~MOD2_TBEMP_MASK;
    }

    /**
     * Check if MOD2 Receive Buffer Full flag is set
     */
    protected boolean isMod2RbfllSet() {
        return (mod2 & MOD2_RBFLL_MASK) != 0;
    }

    /**
     * Set MOD2 Receive Buffer Full flag
     */
    protected void setMod2Rbfll() {
        mod2 = mod2 | MOD2_RBFLL_MASK;
    }

    /**
     * Clear MOD2 Receive Buffer Full flag
     */
    protected void clearMod2Rbfll() {
        mod2 = mod2 & ~MOD2_RBFLL_MASK;
    }

    /**
     * Check if MOD2 Tx Run flag is set
     */
    protected boolean isMod2TxrunSet() {
        return (mod2 & MOD2_TXRUN_MASK) != 0;
    }

    /**
     * Set MOD2 Tx Run flag
     */
    protected void setMod2Txrun() {
        mod2 = mod2 | MOD2_TXRUN_MASK;
    }

    /**
     * Clear MOD2 Tx Run flag
     */
    protected void clearMod2Txrun() {
        mod2 = mod2 & ~MOD2_TXRUN_MASK;
    }


    public int getBrcrBrs() {
        return (brcr & BRCR_BRS_MASK);
    }

    public int getBrcrBrck() {
        return (brcr & BRCR_BRCK_MASK) >> 4;
    }

    public boolean isBrcrBraddeSet() {
        return (brcr & BRCR_BRADDE_MASK) != 0;
    }


    public int getBraddBrk() {
        return (brcr & BRADD_BRK_MASK);
    }


    protected boolean isFcnfRfstSet() {
        return (fcnf & FCNF_RFST_MASK) != 0;
    }

    protected boolean isFcnfTfieSet() {
        return (fcnf & FCNF_TFIE_MASK) != 0;
    }

    protected boolean isFcnfRfieSet() {
        return (fcnf & FCNF_RFIE_MASK) != 0;
    }

    protected boolean isFcnfRxtxcntSet() {
        return (fcnf & FCNF_RXTXCNT_MASK) != 0;
    }

    protected boolean isFcnfCnfgSet() {
        return (fcnf & FCNF_CNFG_MASK) != 0;
    }


    // Clock computation
    // See block diagram and details at section 14.2

    /**
     * Compute frequency
     * @return SIO_CLK
     */
    @Override
    public int getFrequencyHz() {
        if (isIoMode()) {
            // I/O interface mode, clock is specified in the control register SC0CR
            if (isCrIocSet()) {
                // SCLK input
                return 0; // We are slave
            }
            else {
                // SCLK output
                return getBaudrate() / 2;
            }
        }
        else {
            // UART mode, clock is specified in the mode control register0 (SC0MOD0<SC1:0>)
            switch (getMod0Sc()) {
                case 0b00: // Timer TB0OUT (from TMRB0)
                    return 0; // TODO
                case 0b01: // Baud rate generator
                    return getBaudrate();
                case 0b10: // Internal fSYS/2 clock
                    return ((TxClockGenerator)platform.getClockGenerator()).getFsysHz() / 2;
                case 0b11: // External clock (SCLK0 input)
                    return 0; // TODO Clock input
                default:
                    throw new RuntimeException("Error: TxSerialInterface.getClockHz");
            }
        }
    }

    /**
     *  See section 14.4.1 of the specification
     */
    public int getBaudrate() {
        double divider;
        int n = getDivideRatio();
        if (isBrcrBraddeSet()) {
            // N + ((16 - K) / 16) division
            if (isIoMode()) {
                // I/O interface mode
                throw new RuntimeException("Error: TxSerialInterface.getBaudrate: BRADDE cannot be enabled in I/O mode");
            }
            if (n == 1 || n == 16) {
                throw new RuntimeException("Error: TxSerialInterface.getBaudrate: BRADDE cannot be enabled with N=" + n);
            }
            int k = getBraddBrk();
            if (k == 0) {
                throw new RuntimeException("Error: TxSerialInterface.getBaudrate: K=0");
            }
            divider = n + (16-k)/16.0;
        }
        else {
            // N division
            divider = n;
        }
        return (int) (getBaudrateInputClock() / divider);
    }


    /**
     * The baud rate generator uses one of four clocks (?T1, ?T4, ?T16 or ?T64)
     * supplied from the prescaler output clock
     */
    private int getBaudrateInputClock() {
        switch (getBrcrBrck()) {
            case 0b00:
                return ((TxClockGenerator)platform.getClockGenerator()).getFt0Hz()/2; // T1 = T0 / 2
            case 0b01:
                return ((TxClockGenerator)platform.getClockGenerator()).getFt0Hz()/8; // T4 = T0 / 8
            case 0b10:
                return ((TxClockGenerator)platform.getClockGenerator()).getFt0Hz()/32; // T16 = T0 / 32
            case 0b11:
                return ((TxClockGenerator)platform.getClockGenerator()).getFt0Hz()/128; // T64 = T0 / 128
            default:
                throw new RuntimeException("Error: TxSerialInterface.getBaudrate: BRCR:BRCK=" + getBrcrBrck());
        }
    }

    public int getDivideRatio() {
        int brs = getBrcrBrs();
        if (brs == 0b0000) {
            return 16;
        }
        else {
            return brs;
        }
    }

    /**
     * Compute Rx FIFO Fill Level to generate interrupts
     * Note: overridden in TxHSerialInterface
     */
    protected void computeRxFillLevel() {
        if (isFullDuplex()) {
            // Full duplex
            // "RIL1 is ignored if FDPX1:0=11 (full duplex)."
            rxInterruptFillLevel = rfc & RFC_RIL0_MASK;
            if (rxInterruptFillLevel == 0) {
                // Special case
                rxInterruptFillLevel = 2;
            }
        }
        else {
            // Half duplex
            rxInterruptFillLevel = rfc & RFC_RIL_MASK;
            if (rxInterruptFillLevel == 0) {
                // Special case
                rxInterruptFillLevel = 4;
            }
        }
    }

    /**
     * Compute Tx FIFO Fill Level to generate interrupts
     * Note: overridden in TxHSerialInterface
     */
    protected void computeTxFillLevel() {
        if (isFullDuplex()) {
            // Full duplex
            // "TIL1 is ignored if FDPX1:0=11 (full duplex)."
            txInterruptFillLevel = tfc & TFC_TIL0_MASK;
        }
        else {
            // Half duplex
            txInterruptFillLevel = tfc & TFC_TIL_MASK;
        }
    }


    protected int getMaxFifoSize() {
        if (isFullDuplex()) {
            return SERIAL_RX_FIFO_SIZE / 2;
        }
        else {
            return SERIAL_RX_FIFO_SIZE;
        }
    }

    protected int getUsableRxFifoSize() {
        if (isFcnfRfstSet()) {
            return rxInterruptFillLevel;
        }
        else {
            return getMaxFifoSize();
        }
    }


    // TRANSMISSION LOGIC

    /**
     * Gets the data transmitted via Serial port
     * This can only be called by external software to simulate data reading by another device
     * @return 5 to 9 bits integer corresponding to a single value read by a device from this serial port
     */
    @Override
    public Integer read() {
        if (isEnSet() && isMod1TxeSet() && isMod1FdpxTxSet()) {
            return getTxValue();
        }
        else {
            if (!isEnSet()) {
                if (logSerialMessages) System.out.println(getName() + " is disabled. Returning null.");
            }
            else if (!isMod1TxeSet()) {
                if (logSerialMessages) System.out.println("TX is disabled on " + getName() + ". Returning null.");
            }
            else {
                if (logSerialMessages) System.out.println("Duplex mode on " + getName() + " is " + getMod1Fdpx() + ". Returning null.");
            }
            return null;
        }
    }

    private Integer getTxValue() {
        if (!isFcnfCnfgSet()) { // FIFO disabled
            if (isMod2TbempSet()) {
                if (logSerialMessages) System.err.println(getName() + ": TX buffer underrun");
                // There's no data in buffer => Underrun : new data to return. Return null
                if (isCrIocSet()) { // Buffer underrun can normally only happen in SCLK input mode. In SCLK output mode, clock is stopped
                    setCrPerr();
                }
                return null;
            }
            setMod2Tbemp();
            if (isMod1FdpxTxSet()) {
                platform.getInterruptController().request(getTxInterruptNumber());
            }
            // TODO if UART mode 9 bits, also include bit in MOD0:TB8
            return txBuf;
        }
        else {
            if (txFifo.size() == 0) {
                if (logSerialMessages) System.err.println(getName() + ": TX fifo underrun");
//                if (isCrIocSet()) {// Buffer underrun can normally only happen in SCLK input mode. In SCLK output mode, clock is stopped
//                    setCrPerr(); // TODO This is not explicitly specified in case of FIFO. Sounds logical but...
//                }
                return null;
            }
            else {
                Integer value = txFifo.poll();
                if (isTfcTfisSet()?(txFifo.size() <= txInterruptFillLevel):(txFifo.size() == txInterruptFillLevel)) {
                    if (isFcnfTfieSet()) {
                        platform.getInterruptController().request(getTxInterruptNumber());
                    }
                    if (isFcnfRxtxcntSet()) {
                        clearMod1Txe();
                    }
                }
                return value;
            }
        }
    }

    protected int getNbTxValuesWaiting() {
        if (!isFcnfCnfgSet()) { // FIFO disabled
            return isMod2TbempSet()?0:1;
        }
        else {
            return txFifo.size();
        }
    }


    // RECEPTION LOGIC

    /**
     * Sets the data received via Serial port
     * This can only be called by external software to simulate data writing by another device
     * @param value integer (5 to 9 bits) corresponding to a single value written by an external device to this serial port
     */
    @Override
    public void write(Integer value) {
        if (value == null) {
            if (logSerialMessages) System.out.println("TxSerialInterface.write(null)");
        }
        else {
            if (isEnSet() && isMod0RxeSet() && isMod1FdpxRxSet()) {
                queueRxValue(value);
            }
            else {
                if (!isEnSet()) {
                    if (logSerialMessages) System.out.println(getName() + " is disabled. Value 0x" + Format.asHex(value, 2) + " is ignored.");
                }
                else if (!isMod0RxeSet()) {
                    if (logSerialMessages) System.out.println("RX is disabled on " + getName() + ". Value 0x" + Format.asHex(value, 2) + " is ignored.");
                }
                else {
                    if (logSerialMessages) System.out.println("Duplex mode on " + getName() + " is " + getMod1Fdpx() + ". Value 0x" + Format.asHex(value, 2) + " is ignored.");
                }
            }
        }
    }

    private void queueRxValue(int value) {
        // TODO if UART mode with parity, check parity and set CR:PERR accordingly
        if (!isFcnfCnfgSet()) { // FIFO disabled
            if (isMod2RbfllSet()) {
                if (logSerialMessages) System.err.println(getName() + ": RX buffer overrun");
                // There's already an unread data in buffer => Overrun : new data is lost. No change to current buffer
                setCrOerr(); // See 14.2.14 1st section
            }
            else {
                rxBuf = value;
                // TODO if UART mode 9 bits, also set bit in CR:RB8
                setMod2Rbfll();
                if (isMod1FdpxRxSet()) {
                    platform.getInterruptController().request(getRxInterruptNumber());
                }
            }
        }
        else { // FIFO enabled
            if (rxFifo.size() >= getUsableRxFifoSize()) {
                if (logSerialMessages) System.err.println(getName() + ": RX fifo overrun");
                // Fifo is already full => Overrun : new data is lost. No change to current fifo
                setCrOerr(); // See 14.2.14 1st section - CR:OERR seems to apply also to FIFO
                setRstRor(); // This is FIFO specific. I guess it has to be set here...
            }
            else {
                rxFifo.add(value);

                if (isRfcRfisSet()?(rxFifo.size() >= rxInterruptFillLevel):(rxFifo.size() == rxInterruptFillLevel)) {
                    if (isFcnfRfieSet()) {
                        platform.getInterruptController().request(getRxInterruptNumber());
                    }
                    if (isFcnfRxtxcntSet()) {
                        clearMod0Rxe();
                    }
                }
            }
        }
    }

    protected int getRxInterruptNumber() {
        return TxInterruptController.INTRX0 + 2 * serialInterfaceNumber;
    }

    protected int getTxInterruptNumber() {
        return TxInterruptController.INTTX0 + 2 * serialInterfaceNumber;
    }

    @Override
    public int getNumBits() {
        return 8;  //TODO if UART
    }

    public String getName() {
        return Constants.CHIP_LABEL[Constants.CHIP_TX] + " Serial #" + serialInterfaceNumber;
    }

    public String toString() {
        return getName();
    }


    @Override
    public int getChip() {
        return Constants.CHIP_TX;
    }

    /**
     * The goal of this is to delay the actual transfer according to the currently selected baud rate
     */
    @Override
    public Object onClockTick() throws Exception {
        bitNumberBeingTransferred++;
        if (bitNumberBeingTransferred == getNumBits() + getIntervalTimeInSclk() /* TODO + start/stop/parity if UART */) {
            bitNumberBeingTransferred = 0;
            // Transfer one byte
            Integer value = read();
            if (value != null) {
                super.valueReady(value);
                // device may stop transmission automatically if FIFO was used and configured like this
                if (!isMod1TxeSet()) {
                    // unregister
                    platform.getMasterClock().remove(this);
                }
            }
            else {
                // End of transmission - unregister
                platform.getMasterClock().remove(this);
            }
        }
        return null;
    }
}
