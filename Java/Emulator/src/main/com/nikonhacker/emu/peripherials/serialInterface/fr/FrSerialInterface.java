package com.nikonhacker.emu.peripherials.serialInterface.fr;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Behaviour based on spec in http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-10147-2E.pdf
 */
public class FrSerialInterface extends SerialInterface {
    /**
     * FIFO1. null if disabled
     */
    private Queue<Integer> fifo1 = null;

    /**
     * FIFO2. null if disabled
     */
    private Queue<Integer> fifo2 = null;

    private int baudRateGenerator;

    private int scrIbcr;
    private int smr;
    private int ssr;
    private int escrIbsr;
    private int tdr, rdr;
    private int bgr1;
    private int bgr0;
    private int ismk;
    private int isba;
    private int fcr1 = 0x4;
    private int fcr0;
    private int fbyte2 = 0x8;
    private int fbyte1 = 0x8;
    private int readPointer;
    private int savedReadPointer;
    private int fifoIdleCounter = 0; // TODO implement counter increment and detection

    private int rxInterruptNumber, txInterruptNumber;


    public FrSerialInterface(int serialInterfaceNumber, InterruptController interruptController, int baseInterruptNumber, Emulator emulator, boolean logSerialMessages) {
        super(serialInterfaceNumber, interruptController, emulator, logSerialMessages);
//        First, interrupt numbers were automatic but they are now custom
//        rxInterruptNumber = InterruptController.SERIAL_IF_RX_0_REQUEST_NR + this.serialInterfaceNumber * 3;
//        txInterruptNumber = InterruptController.SERIAL_IF_RX_0_REQUEST_NR + 1 + this.serialInterfaceNumber * 3;
//        txInterruptNumber = baseInterruptNumber + 1;
        rxInterruptNumber = baseInterruptNumber;
        txInterruptNumber = baseInterruptNumber;
    }

    public void setScrIbcr(int scrIbcr) {
        if ((scrIbcr & 0x80) != 0) {
            clearViaUpcl();
        }
        this.scrIbcr = scrIbcr;
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
        baudRateGenerator = bgr1 << 8 + bgr0;
        ssr = 0x3; // ORE-RDRF-TDRE-TBI=0011
    }

    public int getScrIbcr() {
        return scrIbcr & 0x7F;
    }

    public void setSmr(int smr) {
        int md = (smr & 0xE0) >> 5;
        if (md != 2) {
            throw new RuntimeException("Serial Interface " + serialInterfaceNumber + ": cannot be configured in mode MD=0b" + Format.asBinary(md, 3) + ".\nOnly CSIO mode (0b010) is supported by emulator");
        }
        if ((smr & 0x10) != 0) {
            throw new RuntimeException("Serial Interface " + serialInterfaceNumber + ": reserved bit 4 of SMR must be 0");
        }

        this.smr = smr;
    }

    public int getSmr() {
        return smr & 0xEF;
    }

    public void setSsr(int ssr) {
        // only highest bit is taken into account
        if ((ssr & 0x80) != 0) {
            // Clear ORE flag
            this.ssr = this.ssr & 0xF7;
        }
    }

    public int getSsr() {
        return ssr & 0x0F;
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

    public int getEscrIbsr() {
        return escrIbsr & 0x7F;
    }

    // TRANSMISSION LOGIC

    /**
     * Sets the data to be transmitted via Serial port
     * This can only be called by the CPU
     * @param tdr
     */
    public void setTdr(int tdr) {
        Queue<Integer> txFifo;
        if ((fcr1 & 0x1) == 0) {
            txFifo = fifo1;
        }
        else {
            txFifo = fifo2;
        }

        if (txFifo == null) {
            // Not using FIFO for reception
            // Clear SSR:TDRE to indicate that TDR contains valid data
            ssr = ssr & 0xFD;

            // TODO test SCR:TXE

            this.tdr = mask(tdr);
        }
        else {
            txFifo.add(mask(tdr));
            if ((fcr1 & 0x1) == 0) {
                fbyte1 = txFifo.size();
            }
            else {
                fbyte2 = txFifo.size();
            }
        }
        super.valueReady(read());
    }

    /**
     * Gets the data transmitted via Serial port
     * This can only be called by external software to simulate data reading by another device
     * @return 5 to 9 bits integer corresponding to a single value read by a device from this serial port
     */
    public Integer read() {
        Queue<Integer> txFifo;
        if ((fcr1 & 0x1) == 0) {
            txFifo = fifo1;
        }
        else {
            txFifo = fifo2;
        }

        if (txFifo == null) {
            // Set SSR:TDRE to indicate that TDR no longer contains any valid data
            ssr = ssr | 0x2;

            // Request TX interrupt if enabled (SCR:TIE)
            if ((scrIbcr & 0x08) != 0) {
                interruptController.request(txInterruptNumber);
            }

            // Request Bus idle TX interrupt if enabled (SCR:TBIE)
            if ((scrIbcr & 0x04) != 0) {
                interruptController.request(txInterruptNumber);
            }

            return tdr;
        }
        else {
            Integer value = txFifo.poll();

            if (txFifo.isEmpty()) {
                // Set FCR1:FDRQ
                fcr1 = fcr1 | 0x4;

                // Set SSR:TDRE to indicate that TDR no longer contains any valid data
                ssr = ssr | 0x2;

                // Request Fifo TX interrupt if enabled (FCR1:FTIE)
                if ((fcr1 & 0x04) != 0) {
                    interruptController.request(txInterruptNumber);
                }
            }

            return value;
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
            if (logSerialMessages) System.out.println("FrSerialInterface.write(null)");
        }
        else {
            Queue<Integer> rxFifo;
            int fbyte;
            if ((fcr1 & 0x1) == 0) {
                rxFifo = fifo2;
                fbyte = fbyte2;
            }
            else {
                rxFifo = fifo1;
                fbyte = fbyte1;
            }

            if (rxFifo == null) {
                // Not using FIFO for reception
                if ((ssr & 0x4) != 0) {
                    // There was already a value pending : OverRun Error

                    // Set SSR:ORE
                    ssr = ssr | 0x8;

                    // Request RX interrupt, if enabled (SCR:RIE)
                    if ((scrIbcr & 0x10) != 0) {
                        interruptController.request(rxInterruptNumber);
                    }
                }
                else {
                    rdr = mask(value);

                    // Set SSR:RDRF
                    ssr = ssr | 0x4;

                    // Request RX interrupt, if enabled (SCR:RIE)
                    if ((scrIbcr & 0x10) != 0) {
                        interruptController.request(rxInterruptNumber);
                    }
                }
            }
            else {
                // Using FIFO for reception
                if (rxFifo.size() == 16) {
                    // FIFO is already full : OverRun Error

                    // Set SSR:ORE
                    ssr = ssr | 0x8;

                    // "If this flag is set during the use of the reception FIFO, the reception
                    // FIFO enable bit will be cleared". So :

                    // Determine which fifo is the reception one
                    if (rxFifo == fifo1) {
                        fcr0 = fcr0 & 0xFE; // Reset FCR0:FE1
                        fifo1 = null; // Disable fifo1
                    }
                    else {
                        fcr0 = fcr0 & 0xFD; // Reset FCR0:FE2
                        fifo2 = null; // Disable fifo2
                    }

                    // Request RX interrupt, if enabled (SCR:RIE)
                    if ((scrIbcr & 0x10) != 0) {
                        interruptController.request(rxInterruptNumber);
                    }
                }
                else  {
                    // not full yet. Add the value
                    rxFifo.add(mask(value));

                    if (rxFifo.size() == fbyte) {
                        // Check that it wasn't signalled as full yet
                        // because the "== fbyte" condition could match multiple times when FIFO is read and written concurrently
                        if ((ssr & 0x4) == 0) {

                            // Signal RDR Full
                            // Set SSR:RDRF
                            ssr = ssr | 0x4;

                            // Request RX interrupt, if enabled (SCR:RIE)
                            if ((scrIbcr & 0x10) != 0) {
                                interruptController.request(rxInterruptNumber);
                            }
                        }
                    }
                    else {
                        fifoIdleCounter = 0;
                    }
                }
            }
        }
    }

    /**
     * Gets the data received via Serial port
     * This can only be called by the CPU
     * @return rdr
     */
    public int getRdr() {
        Queue<Integer> rxFifo;
        if ((fcr1 & 0x1) == 0) {
            rxFifo = fifo2;
        }
        else {
            rxFifo = fifo1;
        }

        if (rxFifo == null) {
            // Clear SSR:RDRF
            ssr = ssr & 0xEF;
            return rdr;
        }
        else {
            if (rxFifo.isEmpty()) {
                // throw exception ??
                if (logSerialMessages) System.err.println("Attempt to read from empty FIFO");
                return -1;
            }
            else {
                int value = rxFifo.poll();
                if (rxFifo.isEmpty()) {
                    // Clear SSR:RDRF
                    ssr = ssr & 0xEF;
                }
                return value;
            }
        }
    }

    public void setBgr1(int bgr1) {
        this.bgr1 = bgr1;
        baudRateGenerator = bgr1 << 8 + bgr0;
    }

    public int getBgr1() {
        return bgr1;
    }

    public void setBgr0(int bgr0) {
        this.bgr0 = bgr0;
        baudRateGenerator = bgr1 << 8 + bgr0;
    }

    public int getBgr0() {
        return bgr0;
    }

    public void setIsmk(int ismk) {
        this.ismk = ismk;
    }

    public int getIsmk() {
        return ismk;
    }

    public void setIsba(int isba) {
        this.isba = isba;
    }

    public int getIsba() {
        return isba;
    }

    public void setFcr1(int fcr1) {
        if ((fcr1 & 0xC) != 0) {
            if (logSerialMessages) System.out.println("Error: attempt to write 0b11 to reserved bits 14-15 of FCR !");
        }

        this.fcr1 = fcr1;
    }

    public int getFcr1() {
        return fcr1 & 0x1F;
    }

    public void setFcr0(int fcr0) {
        // FLST is ignored upon write

        // FLD
        if ((fcr0 & 0x20) != 0) {
            readPointer = savedReadPointer;
        }

        // FSET
        if ((fcr0 & 0x10) != 0) {
            savedReadPointer = readPointer;
        }

        // FCL2
        if ((fcr0 & 0x08) != 0) {
            fifo2 = new LinkedList<Integer>();
        }

        // FCL1
        if ((fcr0 & 0x04) != 0) {
            fifo1 = new LinkedList<Integer>();
        }

        // FE2
        if ((fcr0 & 0x02) != 0) {
            fifo2 = new LinkedList<Integer>();
            if ((fcr1 & 0x1) == 0) {
                // fifo2 is the rx fifo
                // start idle detection counter
                // TODO
            }
        }
        else {
            fifo2 = null;
            if ((fcr1 & 0x1) == 0) {
                // fifo2 is the rx fifo
                // reset counter upon de-activation
                fifoIdleCounter = 0;
            }
        }

        // FE1
        if ((fcr0 & 0x01) != 0) {
            fifo1 = new LinkedList<Integer>();
            if ((fcr1 & 0x1) != 0) {
                // fifo1 is the rx fifo
                // start idle detection counter
                // TODO
            }
        }
        else {
            fifo1 = null;
            if ((fcr1 & 0x1) != 0) {
                // fifo1 is the rx fifo
                // reset counter upon de-activation
                fifoIdleCounter = 0;
            }
        }
        this.fcr0 = fcr0;
    }

    public int getFcr0() {
        return fcr0 & 0x63; // 0b01100011
    }

    public void setFbyte2(int fbyte2) {
        this.fbyte2 = fbyte2;
    }

    public int getFbyte2() {
        return fifo2.size();
    }

    public void setFbyte1(int fbyte1) {
        this.fbyte1 = fbyte1;
    }

    public int getFbyte1() {
        return fifo1.size();
    }

    private int mask(int value) {
        return value & ((1 << getNumBits()) - 1);
    }

    public int getNumBits() {
        switch (escrIbsr & 0x3) {
            case 0: // 8-bit
                return 8;
            case 1: // 5-bit
                return 5;
            case 2: // 6-bit
                return 6;
            case 3: // 7-bit
                return 7;
            case 4: // 9-bit
                return 9;
            default:
                if (logSerialMessages) System.err.println("Error: Invalid ESCR value: " + (escrIbsr & 0x3));
                return 8;
        }
    }

    public String getName() {
        return Constants.CHIP_LABEL[Constants.CHIP_FR] + " Serial #" + serialInterfaceNumber;
    }
}
