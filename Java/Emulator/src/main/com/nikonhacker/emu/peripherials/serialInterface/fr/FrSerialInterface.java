package com.nikonhacker.emu.peripherials.serialInterface.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Behaviour based on spec in http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-10147-2E.pdf
 */
public class FrSerialInterface extends SerialInterface {
    public static final int FIFO_SIZE = 128; // Spec says 16, but code splits message at 0x80

    public static final int SCR_TXE_MASK  = 0b0000_0001;
    public static final int SCR_RXE_MASK  = 0b0000_0010;
    public static final int SCR_TBIE_MASK = 0b0000_0100;
    public static final int SCR_TIE_MASK  = 0b0000_1000;
    public static final int SCR_RIE_MASK  = 0b0001_0000;
    public static final int SCR_SPI_MASK  = 0b0010_0000;
    public static final int SCR_MS_MASK   = 0b0100_0000;
    public static final int SCR_UPCL_MASK = 0b1000_0000;

    public static final int SSR_TBI_MASK  = 0b0000_0001;
    public static final int SSR_TDRE_MASK = 0b0000_0010;
    public static final int SSR_RDRF_MASK = 0b0000_0100;
    public static final int SSR_ORE_MASK  = 0b0000_1000;
    public static final int SSR_REC_MASK  = 0b1000_0000;

    public static final int SMR_SCKE_MASK = 0b0000_0010;

    public static final int FCR0_FE1_MASK  = 0b0000_0001;
    public static final int FCR0_FE2_MASK  = 0b0000_0010;
    public static final int FCR0_FCL1_MASK = 0b0000_0100;
    public static final int FCR0_FCL2_MASK = 0b0000_1000;
    public static final int FCR0_FSET_MASK = 0b0001_0000;
    public static final int FCR0_FLD_MASK  = 0b0010_0000;
    public static final int FCR0_FLST_MASK = 0b0100_0000;

    public static final int FCR1_FSEL_MASK  = 0b0000_0001;
    public static final int FCR1_FTIE_MASK  = 0b0000_0010;
    public static final int FCR1_FDRQ_MASK  = 0b0000_0100;
    public static final int FCR1_FRIIE_MASK = 0b0000_1000;
    public static final int FCR1_FLSTE_MASK = 0b0001_0000;

    public static final  int ESCR_L2L1L0_MASK = 0b0000_0111;

    // Registers
    private int scrIbcr;
    private int smr;
    private int ssr = 0b0000_0011; // Initial value according to spec
    private int escrIbsr;
    private int tdr, rdr;
    private int bgr1;
    private int bgr0;
    private int ismk;
    private int isba;
    private int fcr1 = 0b0000_0100; // Initial value according to spec
    private int fcr0;
    private int fbyte2 = 0x8; // Initial value according to spec
    private int fbyte1 = 0x8; // Initial value according to spec

    // Note: SSEL89AB/RDRM/TDRM don't seem to be used in the Expeed implementation

    private final Queue<Integer> fifo1Backend = new LinkedList<Integer>();
    private final Queue<Integer> fifo2Backend = new LinkedList<Integer>();

    /**
     * FIFO1. null if disabled, otherwise = fifo1Backend
     */
    private Queue<Integer> fifo1 = null;

    /**
     * FIFO2. null if disabled, otherwise = fifo2Backend
     */
    private Queue<Integer> fifo2 = null;

    private int baudRateGenerator;

    private int fifoIdleCounter = 0; // TODO implement counter increment and detection

    private int rxInterruptNumber, txInterruptNumber;


    public FrSerialInterface(int serialInterfaceNumber, Platform platform, boolean logSerialMessages) {
        super(serialInterfaceNumber, platform, logSerialMessages);
        // This is pure speculation but seems to work for interrupt 5 at least
        rxInterruptNumber = FrInterruptController.SERIAL_IF_RX_REQUEST_NR /*+ this.serialInterfaceNumber * 3*/;
        txInterruptNumber = FrInterruptController.SERIAL_IF_TX_REQUEST_NR /*+ this.serialInterfaceNumber * 3*/;
    }


    public int getScrIbcr() {
        return scrIbcr;
    }

    public void setScrIbcr(int scrIbcr) {
        if ((scrIbcr & SCR_UPCL_MASK) != 0) {
            clearViaUpcl();
        }
        if (isTieOrTbieSetInScr(scrIbcr)) {
            // TODO : we still do not know the interrupt numbers for reception
            throw new RuntimeException("Serial Interface " + serialInterfaceNumber + ": TX interrupt is not implemented yet");
        }
        synchronized (this) {
            // if TX interrupt (TIE or TBIE) becomes disabled, remove the current request, if any
            if (isTieOrTbieSetInScr(this.scrIbcr) && !isTieOrTbieSetInScr(scrIbcr)) {
                platform.getInterruptController().removeRequest(txInterruptNumber);
            }
            // if RX interrupt (RIE) becomes disabled, remove the current request, if any
            if (isRieSetInScr(this.scrIbcr) && !isRieSetInScr(scrIbcr)) {
                platform.getInterruptController().removeRequest(rxInterruptNumber);
            }
            this.scrIbcr = scrIbcr & 0b0111_1111;
        }
    }

    /**
     * Executed when UPCL is set to 1. Cfr http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-10147-2E.pdf
     * This bit is used to initialize the internal state of the CSIO.
     * Setting the bit to "1":
     * - The CSIO will be reset directly (software reset). The register setting, however, will be retained.
     * In this case, communication of the data which is being transmitted or received will be cut off immediately.
     * - The baud rate generator will reload the value set in BGR1/BGR0 registers, and then restart the operation.
     * - All the transmission/reception interrupt sources (TDRE, TBI, RDRF and ORE) will be initialized ("1100B").
     */
    private void clearViaUpcl() {
        updateBaudRateGenerator();
        ssr = 0b0000_0011; // 0-?-?-ORE-RDRF-TDRE-TBI=00000011
    }

    public boolean isScrTxeSet() {
        return (scrIbcr & SCR_TXE_MASK) != 0;
    }

    public boolean isScrRxeSet() {
        return (scrIbcr & SCR_RXE_MASK) != 0;
    }

    private boolean isScrTbieSet() {
        return (scrIbcr & SCR_TBIE_MASK) != 0;
    }

    private boolean isScrTieSet() {
        return (scrIbcr & SCR_TIE_MASK) != 0;
    }

    private boolean isScrRieSet() {
        return (scrIbcr & SCR_RIE_MASK) != 0;
    }

    private boolean isScrMsSet() {
        return (scrIbcr & SCR_MS_MASK) != 0;
    }


    private boolean isRieSetInScr(int scr) {
        return (scr & SCR_RIE_MASK) != 0;
    }

    private boolean isTieOrTbieSetInScr(int scr) {
        return (scr & (SCR_TIE_MASK | SCR_TBIE_MASK)) != 0;
    }


    public int getSmr() {
        return smr;
    }

    public void setSmr(int smr) {
        int md = smr >>> 5;
        if (md != 0b010) {
            throw new RuntimeException("Serial Interface " + serialInterfaceNumber + ": cannot be configured in mode MD=0b" + Format.asBinary(md, 3) + ".\nOnly CSIO mode (0b010) is supported for now");
        }

        this.smr = smr;
    }

    public boolean isSmrSckeSet() {
        return (smr & SMR_SCKE_MASK) != 0;
    }

    private boolean isSlaveAndClockInputEnabled() {
        return isScrMsSet() && !isSmrSckeSet();
    }


    public int getSsr() {
        return ssr & 0b0000_1111;
    }

    public void setSsr(int ssr) {
        // only highest bit is taken into account
        if ((ssr & SSR_REC_MASK) != 0) {
            // Request to clear ORE flag
            synchronized (this) {
                // important to check for, because level-triggered interrupts can be shared
                if (((this.ssr & (SSR_ORE_MASK | SSR_RDRF_MASK)) == 0) && isScrRieSet()) {
                    platform.getInterruptController().removeRequest(rxInterruptNumber);
                }
                clearSsrOre();
            }
        }
    }

    private void clearSsrTbi() {
        ssr = ssr & ~SSR_TBI_MASK;
    }

    private void clearSsrTdre() {
        ssr = ssr & ~SSR_TDRE_MASK;
    }

    private void clearSsrRdrf() {
        ssr = ssr & ~SSR_RDRF_MASK;
    }

    private void clearSsrOre() {
        ssr = ssr & ~SSR_ORE_MASK;
    }

    private void setSsrTbi() {
        ssr = ssr | SSR_TBI_MASK;
    }

    private void setSsrTdre() {
        ssr = ssr | SSR_TDRE_MASK;
    }

    private void setSsrRdrf() {
        ssr = ssr | SSR_RDRF_MASK;
    }

    private void setSsrOre() {
        ssr = ssr | SSR_ORE_MASK;
    }

    private boolean isSsrTdreSet() {
        return (ssr & SSR_TDRE_MASK) != 0;
    }

    private boolean isSsrRdrfSet() {
        return (ssr & SSR_RDRF_MASK) != 0;
    }


    public int getEscrIbsr() {
        return escrIbsr & 0b0001_1111;
    }

    public void setEscrIbsr(int escrIbsr) {
        int oldNbBits = getNumBits();
        this.escrIbsr = escrIbsr;

        // inform emulated connected device that config changed
        int newNbBits = getNumBits();
        if (newNbBits != oldNbBits) {
            super.bitNumberChanged(newNbBits);
        }
    }

    public int getNumBits() {
        switch (escrIbsr & ESCR_L2L1L0_MASK) {
            case 0b000: // 8-bit
                return 8;
            case 0b001: // 5-bit
                return 5;
            case 0b010: // 6-bit
                return 6;
            case 0b011: // 7-bit
                return 7;
            case 0b100: // 9-bit
                return 9;
            default:
                if (logSerialMessages) System.err.println("Error: Invalid ESCR: L2-L1-L0 value=" + (escrIbsr & ESCR_L2L1L0_MASK));
                return 8;
        }
    }


    // TRANSMISSION LOGIC

    /**
     * Sets the data to be transmitted via Serial port
     * This can only be called by the CPU
     * @param tdr
     */
    public void setTdr(int tdr) {
        // clear TBI, because bus is not idle anymore
        clearSsrTbi();

        // If there is still an Tx interrupt being requested, remove it
        if (isTieOrTbieSetInScr(scrIbcr)) {
            platform.getInterruptController().removeRequest(txInterruptNumber);
        }

        Queue<Integer> txFifo = isTxFifo1()?fifo1:fifo2;

        // "Transmission data cannot be written to the transmission data register (TDR)
        // when the transmission data empty flag (SSR:TDRE) is set to "0"
        // and the transmission FIFO is either disabled or full."
        // In other words, for TDR to be written, either SSR:TDRE is 1, or Fifo is enabled and not full

        boolean tdrWasWritten = false;
        if (txFifo == null) {
            // Not using FIFO for transmission

            if (isSsrTdreSet()) {
                this.tdr = mask(tdr);
                tdrWasWritten = true;
            }
            else {
                if (logSerialMessages) System.out.println("setTdr() called while TDR was not empty. Value ignored...");
            }
        }
        else {
            // Using FIFO for transmission
            if (txFifo.size() < FIFO_SIZE) {
                txFifo.add(mask(tdr));
                tdrWasWritten = true;
                if (txFifo.size() == FIFO_SIZE) {
                    // Spec says among "FDRQ reset condition": "When the transmission FIFO is full."
                    clearFcr1Fdrq();
                }
            }
            else {
                if (logSerialMessages) System.out.println("setTdr() called while FIFO was full. Value ignored...");
            }
        }

        // "The transmission data empty flag (SSR:TDRE) is cleared to "0" when transmission data is written to the transmission data register (TDR)"
        clearSsrTdre();

        if (isScrTxeSet()) {
            // TODO transmitting process should only start in this case
        }
        else {
            if (logSerialMessages) System.out.println("setTdr() called while TXE is not set. TXE is ignored for now");
        }

        // In slave mode, we have to wait for a byte to come in and use its clock to transmit
        if (!isSlaveAndClockInputEnabled() && tdrWasWritten) {
            super.valueReady(read());
        }
    }


    /**
     * Gets the data transmitted via Serial port
     * This can only be called by external software to simulate data reading by another device
     * @return 5 to 9 bits integer corresponding to a single value read by a device from this serial port
     */
    public Integer read() {
        Queue<Integer> txFifo = isTxFifo1()?fifo1:fifo2;

        if (!isScrTxeSet()) {
            if (logSerialMessages) System.err.println("FrSerialInterface.read()  was called while Tx is disabled");
            return 0;
        }

        // NOTE: normally, TDR empty is set when transmission starts, while bus idle is set when transmission ends
        // But in this case, it all happens in one call, so both are done at the same time

        if (txFifo == null) {
            // Not using FIFO for transmission

            // Set SSR:TDRE to indicate that TDR no longer contains any valid data
            setSsrTdre();

            // Request TX interrupt if enabled for non-fifo (TIE)
            if (isScrTieSet()) {
                platform.getInterruptController().request(txInterruptNumber);
            }

            signalBusIdle();

            return tdr;
        }
        else {
            // Using FIFO for transmission
            Integer value = txFifo.poll();

            if (txFifo.isEmpty()) {
                // Spec says among "FDRQ setting condition": "The transmission FIFO is empty."
                setFcr1Fdrq();

                // Set SSR:TDRE to indicate that TDR no longer contains any valid data
                setSsrTdre();

                // Request TX interrupt if enabled for Fifo (FTIE)
                if (isFcr1FtieSet()) {
                    platform.getInterruptController().request(txInterruptNumber);
                }

                signalBusIdle();
            }

            return value;
        }
    }

    private void signalBusIdle() {
        // Set SSR:TBI to indicate that bus is idle
        setSsrTbi();

        // Request Bus idle TX interrupt if enabled
        if (isScrTbieSet()) {
            platform.getInterruptController().request(txInterruptNumber);
        }
    }

    // RECEPTION LOGIC

    /**
     * Sets the data received via Serial port
     * This can only be called by external software to simulate data writing by another device
     * @param value integer (5 to 9 bits) corresponding to a single value written by an external device to this serial port
     */
    public void write(Integer value) {
        if (value == null) {
            throw new RuntimeException("FrSerialInterface.write(null)");
        }
        else {
            if (!isScrRxeSet()) {
                if (logSerialMessages) System.err.println("FrSerialInterface.write(0x" + Format.asHex(value, 2) + ") was called while Rx is disabled. Ignored");
            }
            else {
                Queue<Integer> rxFifo;
                int fbyteThreshold;
                if (isRxFifo1()) {
                    rxFifo = fifo1;
                    fbyteThreshold = fbyte1;
                }
                else {
                    rxFifo = fifo2;
                    fbyteThreshold = fbyte2;
                }

                if (rxFifo == null) {
                    // Not using FIFO for reception
                    if (isSsrRdrfSet()) {
                        // There was already a value pending: indicate OverRun Error
                        setSsrOre();

                        // Request RX interrupt, if enabled
                        if (isScrRieSet()) {
                            platform.getInterruptController().request(rxInterruptNumber);
                        }
                    }
                    else {
                        rdr = mask(value);

                        signalRdrFull();
                    }
                }
                else {
                    // Using FIFO for reception
                    if (rxFifo.size() == FIFO_SIZE) {
                        // FIFO is already full: OverRun Error
                        setSsrOre();

                        // Spec says "If this flag is set during the use of the reception FIFO,
                        // the reception FIFO enable bit will be cleared". So :

                        // Determine which fifo is the reception one
                        if (rxFifo == fifo1) {
                            clearFcr0Fe1();
                            fifo1 = null; // Disable fifo1
                        }
                        else {
                            clearFcr0Fe2();
                            fifo2 = null; // Disable fifo2
                        }

                        // "A reception interrupt request is output when the ORE and RIE bits are set to "1".
                        if (isScrRieSet()) {
                            platform.getInterruptController().request(rxInterruptNumber);
                        }
                    }
                    else  {
                        // not full yet. Add the value
                        rxFifo.add(mask(value));

                        if (rxFifo.size() == fbyteThreshold) {
                            // Check that it wasn't signalled as full yet
                            // because the "== fbyte" condition could match multiple times when FIFO is read and written concurrently
                            if (!isSsrRdrfSet()) {
                                signalRdrFull();
                            }
                        }
                        else {
                            fifoIdleCounter = 0;
                        }
                    }
                }
            }
            // In slave mode, we have to send a byte when we receive one, no matter if RXE is enabled or not
            if (isSlaveAndClockInputEnabled()) {
                super.valueReady(read());
            }
        }
    }

    private void signalRdrFull() {
        // Signal RFR full
        setSsrRdrf();

        // Request RX interrupt, if enabled
        if (isScrRieSet()) {
            platform.getInterruptController().request(rxInterruptNumber);
        }
    }

    /**
     * Gets the data received via Serial port
     * This can only be called by the CPU
     * @return rdr
     */
    public int getRdr() {
        Queue<Integer> rxFifo = isRxFifo1()?fifo1:fifo2;

        int value;
        synchronized (this) {
            if (rxFifo == null) {
                // Not using FIFO for reception
                clearSsrRdrf();
                value = rdr;
            }
            else {
                // Using FIFO for reception
                if (rxFifo.isEmpty()) {
                    throw new RuntimeException("FrSerialInterface.getRdr(): Attempt to read from empty FIFO");
//                    if (logSerialMessages) System.err.println("Attempt to read from empty FIFO");
//                    return -1;
                }
                else {
                    value = rxFifo.poll();
                    if (rxFifo.isEmpty()) {
                        clearSsrRdrf();
                    }
                }
            }
            if (((ssr & (SSR_ORE_MASK | SSR_RDRF_MASK)) == 0) && isScrRieSet()) {
                platform.getInterruptController().removeRequest(rxInterruptNumber);
            }
        }
        return value;
    }


    public int getBgr1() {
        return bgr1;
    }

    public void setBgr1(int bgr1) {
        this.bgr1 = bgr1;
        updateBaudRateGenerator();
    }

    public int getBgr0() {
        return bgr0;
    }

    public void setBgr0(int bgr0) {
        this.bgr0 = bgr0;
        updateBaudRateGenerator();
    }

    private void updateBaudRateGenerator() {
        baudRateGenerator = ((bgr1 & 0xFF) << 8) | (bgr0 & 0xFF);
    }


    public int getIsmk() {
        return ismk;
    }

    public void setIsmk(int ismk) {
        this.ismk = ismk;
    }


    public int getIsba() {
        return isba;
    }

    public void setIsba(int isba) {
        this.isba = isba;
    }


    public int getFcr1() {
        // TODO spec says for FDRQ bit: "1" is read by a read modify write (RMW) instruction...
        return fcr1 & 0b0001_1111;
    }

    public void setFcr1(int fcr1) {
        if ((fcr0 & FCR1_FLSTE_MASK) != 0) {
            throw new RuntimeException("FrSerialInterface.setFcr1(): FLSTE retransmission data lost is not implemented");
        }
        this.fcr1 = fcr1;
    }

    private void setFcr1Fdrq() {
        fcr1 = fcr1 | FCR1_FDRQ_MASK;
    }

    private void clearFcr1Fdrq() {
        fcr1 = fcr1 & ~FCR1_FDRQ_MASK;
    }

    private boolean isFcr1FtieSet() {
        return (fcr1 & FCR1_FTIE_MASK) != 0;
    }


    private boolean isRxFifo1() {
        return (fcr1 & FCR1_FSEL_MASK) != 0;
    }

    private boolean isTxFifo1() {
        return (fcr1 & FCR1_FSEL_MASK) == 0;
    }


    public int getFcr0() {
        return fcr0 & 0b0110_0011; // Undef, FSET, and FCL1/2 are always read as 0
    }

    public void setFcr0(int fcr0) {
        // FLST is ignored upon write

        // FLD - Reloads the FIFO pointer.
        if ((fcr0 & FCR0_FLD_MASK) != 0) {
            throw new RuntimeException("FrSerialInterface.setFcr0(): load FIFO read pointers are not implemented");
        }

        // FSET - Saves the FIFO pointer.
        if ((fcr0 & FCR0_FSET_MASK) != 0) {
            throw new RuntimeException("FrSerialInterface.setFcr0(): save FIFO read pointers are not implemented");
        }

        // FCL2 - Fifo2 CLear bit
        if ((fcr0 & FCR0_FCL2_MASK) != 0) {
            fifo2Backend.clear();
            if (isTxFifo1()) {
                // Spec says among "FDRQ setting condition": "Transmission FIFO reset"
                setFcr1Fdrq();
            }
        }

        // FCL1 - Fifo1 CLear bit
        if ((fcr0 & FCR0_FCL1_MASK) != 0) {
            fifo1Backend.clear();
            if (!isTxFifo1()) {
                // Spec says among "FDRQ setting condition": "Transmission FIFO reset"
                setFcr1Fdrq();
            }
        }

        // FE2 - Fifo2 Enable
        if ((fcr0 & FCR0_FE2_MASK) != 0) {
            fifo2 = fifo2Backend;
            if (!isRxFifo1()) {
                // fifo2 is the rx fifo
                // TODO start idle detection counter
                if (logSerialMessages) System.err.println("Start idle detection counter is not implemented 1");
            }
        }
        else {
            fifo2 = null;
            if (!isRxFifo1()) {
                // fifo2 is the rx fifo
                // reset counter upon de-activation
                fifoIdleCounter = 0;
            }
        }

        // FE1 - Fifo1 Enable
        if ((fcr0 & FCR0_FE1_MASK) != 0) {
            fifo1 = fifo1Backend;
            if (isRxFifo1()) {
                // fifo1 is the rx fifo
                // TODO start idle detection counter
                if (logSerialMessages) System.err.println("Start idle detection counter is not implemented 2");
            }
        }
        else {
            fifo1 = null;
            if (isRxFifo1()) {
                // fifo1 is the rx fifo
                // reset counter upon de-activation
                fifoIdleCounter = 0;
            }
        }
        this.fcr0 = fcr0;
    }

    private void clearFcr0Fe2() {
        fcr0 = fcr0 & ~FCR0_FE2_MASK;
    }

    private void clearFcr0Fe1() {
        fcr0 = fcr0 & ~FCR0_FE1_MASK;
    }


    /**
     * Read the number of valid data elements in FIFO1
     */
    public int getFbyte1() {
        return fifo1.size();
    }

    /**
     * Set the number of transfers for FIFO1
     * @param fbyte1
     */
    public void setFbyte1(int fbyte1) {
        this.fbyte1 = fbyte1;
    }

    /**
     * Read the number of valid data elements in FIFO2
     */
    public int getFbyte2() {
        return fifo2.size();
    }

    /**
     * Set the number of transfers for FIFO2
     * @param fbyte2
     */
    public void setFbyte2(int fbyte2) {
        this.fbyte2 = fbyte2;
    }

    private int mask(int value) {
        return value & ((1 << getNumBits()) - 1);
    }

    public String getName() {
        return Constants.CHIP_LABEL[Constants.CHIP_FR] + " Serial #" + serialInterfaceNumber;
    }
}
