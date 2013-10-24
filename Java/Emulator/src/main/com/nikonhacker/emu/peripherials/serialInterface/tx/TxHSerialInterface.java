package com.nikonhacker.emu.peripherials.serialInterface.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.Prefs;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.clock.tx.TxClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;

public class TxHSerialInterface extends TxSerialInterface {
    private static final int HSERIAL_RX_FIFO_SIZE = 32;

    // Note: Most of the following registers are larger in HSIO than SIO

    private static final int BRCR_BRS_MASK = 0b00111111;

    private static final int RST_RLVL_MASK = 0b00111111;
    private static final int RST_ROR_MASK  = 0b10000000;

    private static final int TST_TLVL_MASK = 0b00000111;
    private static final int TST_TUR_MASK  = 0b10000000;

    private static final int RFC_RIL_MASK  = 0b00011111;

    private static final int TFC_TIL_MASK  = 0b00011111;


    public TxHSerialInterface(int serialInterfaceNumber, Platform platform, boolean logSerialMessages, Prefs prefs) {
        super(serialInterfaceNumber, platform, logSerialMessages, prefs);
    }

    @Override
    public int getRst() {
        int rlvl;
        if (rxFifo.size() < 32) {
            rlvl = rxFifo.size() & RST_RLVL_MASK;// RLVL is on 6 bytes
        }
        else {
            // but 32 is coded as 0b000000. See 15.3.1.11
            rlvl = 0;
        }
        return rst | rlvl;
    }

    /**
     * Overridden because BRS is larger in HSIO than SIO
     */
    @Override
    public int getBrcrBrs() {
        return (brcr & BRCR_BRS_MASK);
    }

    /**
     * Overridden for 1..64 instead of 1..16
     */
    @Override
    public int getDivideRatio() {
        int brs = getBrcrBrs();
        if (brs == 0b000000) {
            return 64;
        }
        else {
            return brs;
        }
    }

    @Override
    public String getName() {
        return Constants.CHIP_LABEL[Constants.CHIP_TX] + " HSerial #" + serialInterfaceNumber;
    }

    /**
     * Compute Rx FIFO Fill Level to generate interrupts
     * Overridden because it is much larger on hi-speed than normal serial interfaces
     */
    @Override
    protected void computeRxFillLevel() {
        rxInterruptFillLevel = rfc & RFC_RIL_MASK; // in Hi-speed, RIL size is independent of Half/Full duplex
        if (rxInterruptFillLevel == 0) {
            // Special case
            rxInterruptFillLevel = 32;
        }
    }

    /**
     * Compute Tx FIFO Fill Level to generate interrupts
     * Overridden because it is much larger on hi-speed than normal serial interfaces
     */
    @Override
    protected void computeTxFillLevel() {
        // According to the specification, TIL is on 6 bits, independent of Half/Full duplex
        // However, the code writes 0xA0 to tfc for 2 bytes sent, which would mean 32 (not really meaningul).
        // Moreover, RIL is 5 bits, and the example at page 15-14, it says "HSC0TFC <5:0> = 00000" with 5 zeroes...
        // So let's consider it is 5 bits
        txInterruptFillLevel = tfc & TFC_TIL_MASK; // in Hi-speed, TIL size is independent of Half/Full duplex
        // TODO: Although, note that the updated japanese spec is fixed with "HSC0TFC <5:0> = 000000" with 6 zeroes, so I don't know...

        if (txInterruptFillLevel > 32) {
            throw new RuntimeException(getName() + " error : HSC0TFC<TIL5:1> is more than 32 (" + txInterruptFillLevel + ")");
        }
    }

    @Override
    protected int getMaxFifoSize() {
        if (getMod1Fdpx() == 0b11) {
            return HSERIAL_RX_FIFO_SIZE / 2;
        }
        else {
            return HSERIAL_RX_FIFO_SIZE;
        }
    }


    @Override
    protected int getRxInterruptNumber() {
        return TxInterruptController.HINTRX0 + 2 * serialInterfaceNumber;
    }

    @Override
    protected int getTxInterruptNumber() {
        return TxInterruptController.HINTTX0 + 2 * serialInterfaceNumber;
    }

    // Clock computation
    // See block diagram and details at section 15.2

    /**
     * Compute frequency
     * It has a few differences in comments compared to super.getFrequencyHz()
     * @return HSIO_CLK
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
                case 0b00: // Timer TB3OUT (from TMRB3)
                    // Note diagram says TB8OUT, but diagram in updated japanese spec is fixed: it is TB3OUT
                    return 0; // TODO
                case 0b01: // Baud rate generator
                    return getBaudrate();
                case 0b10: // Internal fSYS / 2 clock
                    // Note: diagram links fSys undivided in HSIO unlike in SIO, but diagram in updated japanese spec is fixed: it is fSys/2
                    return ((TxClockGenerator)platform.getClockGenerator()).getFsysHz() / 2;
                case 0b11: // External clock (HSCLK0 input)
                    return 0; // TODO Clock input
                default:
                    throw new RuntimeException("Error: TxHSerialInterface.getClockHz");
            }
        }
    }

    /**
     * This has the same logic as super.getBaudrate(), but the source is fSys, and logs are adapted
     * @return
     */
    @Override
    public int getBaudrate() {
        double divider;
        int n = getDivideRatio();
        if (isBrcrBraddeSet()) {
            // N + ((16 - K) / 16) division
            if (isIoMode()) {
                // I/O interface mode
                throw new RuntimeException("Error: TxHSerialInterface.getBaudrate: BRADDE cannot be enabled in I/O mode");
            }
            if (n == 1 || n == 16) {
                throw new RuntimeException("Error: TxHSerialInterface.getBaudrate: BRADDE cannot be enabled with N=" + n);
            }
            int k = getBraddBrk();
            if (k == 0) {
                throw new RuntimeException("Error: TxHSerialInterface.getBaudrate: K=0");
            }
            divider = n + (16-k)/16.0;
        }
        else {
            // N division
            divider = n;
        }
        return (int) (((TxClockGenerator)platform.getClockGenerator()).getFsysHz() / divider);
    }

}
