package com.nikonhacker.emu.peripherials.serialInterface.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.CycleCounterListener;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Behaviour is Based on Toshiba documentation TMP19A44F10XBG_TMP19A44FEXBG_en_datasheet_100401.pdf
 */
public class TxSerialInterface extends SerialInterface implements CycleCounterListener {
    private static final int SERIAL_RX_FIFO_SIZE = 4;
    private static final int CYCLES_PER_BYTE = 10;

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

    protected int en; // Enable register
    protected int cr; // Control register
    protected int mod0; // Mode control register 0
    protected int mod1; // Mode control register 1
    protected int mod2 = 0b10000000; // Mode control register 2
    protected int brcr; // Baud rate generator control register
    protected int bradd; // Baud rate generator control register 2
    protected int rfc; // Receive FIFO control register
    protected int tfc; // Transmit FIFO control register
    protected int rst; // Receive FIFO status register
    protected int tst = 0b10000000; // Transmit FIFO status register
    protected int fcnf; // FIFO configuration register

    private Integer cycleCounter = 0;
    private Integer delayedValue;

    public TxSerialInterface(int serialInterfaceNumber, InterruptController interruptController, Emulator emulator, boolean logSerialMessages) {
        super(serialInterfaceNumber, interruptController, emulator, logSerialMessages);
    }

    public int getEn() {
        return en;
    }

    public void setEn(int en) {
//        System.out.println(getName() + ".setEn(0x" + Format.asHex(en, 8) + ")");
        this.en = en & 0x1;
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
                // Insert delay of a few CPU cycles.
                emulator.addCycleCounterListener(this);
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
//        if (getName().equals("HSerial #2") && cr == 0x14) {
//            throw new RuntimeException("Setting HSC2CR to " + Format.asHex(cr,8)) ;
//        }
//        System.out.println(getName() + ".setCr(0x" + Format.asHex(cr, 8) + ")");

        // RB8 and error flags are not writable
        this.cr = (this.cr & 0b10011100) | (cr & 0b01100011);
    }

    public int getMod0() {
        return mod0;
    }

    public void setMod0(int mod0) {
//        System.out.println(getName() + ".setMod0(0x" + Format.asHex(mod0, 8) + ")");
        this.mod0 = mod0;
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
            // Signal if there are values waiting
            if (getNbTxValuesWaiting() > 0) {
                // Insert delay of a few CPU cycles.
                emulator.addCycleCounterListener(this);
            }
        }
    }

    public int getMod2() {
        return mod2;
    }

    public void setMod2(int mod2) {
//        System.out.println(getName() + ".setMod2(0x" + Format.asHex(mod2, 8) + ")");

        // if SWRST goes from 10 to 01, perform a Software reset
        if ((this.mod2 & 0b11) == 0b10 && (mod2 & 0b11)== 0b01) {
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
        this.bradd = bradd & 0b00001111;
    }

    public int getRfc() {
        return rfc & 0b01011111;
    }

    public void setRfc(int rfc) {
        if ((rfc & 0b10000000) != 0) { // RFCS
            rxFifo = new LinkedList<Integer>();
        }
        // TODO RFIS
        this.rfc = rfc;
        computeRxFillLevel();
    }

    private boolean isRfcRfisSet() {
        return (rfc & 0b01000000) != 0;
    }


    public int getTfc() {
        return tfc & 0b01111111;
    }

    public void setTfc(int tfc) {
        this.tfc = tfc;
        if ((tfc & 0b10000000) != 0) { // TFCS
            txFifo = new LinkedList<Integer>();
        }
        // TODO TFIS
        computeTxFillLevel();
    }

    private boolean isTfcTfisSet() {
        return (tfc & 0b01000000) != 0;
    }


    public int getRst() {
        return rst | (rxFifo.size() & 0b0000_0111);
    }

    /**
     * Clear RST Reception OverRun flag
     */
    protected void clearRstRor() {
        rst = rst & 0b0111_1111;
    }

    /**
     * Set RST Reception OverRun flag
     */
    protected void setRstRor() {
        rst = rst | 0b1000_0000;
    }



    public void setRst(int rst) {
        throw new RuntimeException(getName() + " RST register should not be written");
    }

    public int getTst() {
        return tst;
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
        return (en != 0);
    }

    /*
     * @return true if I/O interface mode is in SCLK input clock mode
     */
    protected boolean isCrIocSet() {
        return (cr & 0b00000001) != 0;
    }

    /**
     * Set CR Overrun error flag
     */
    protected void setCrOerr() {
        cr = cr | 0b00010000;
    }

    /**
     * Clear CR Overrun error flag
     */
    protected void clearCrOerr() {
        cr = cr & 0b11101111;
    }

    /**
     * Set CR Parity/Underrun error flag
     */
    protected void setCrPerr() {
        cr = cr | 0b00001000;
    }

    /**
     * Clear CR Parity/Underrun error flag
     */
    protected void clearCrPerr() {
        cr = cr & 0b11110111;
    }

    /**
     * Set CR Framing error flag
     */
    protected void setCrFerr() {
        cr = cr | 0b00000100;
    }

    /**
     * Clear CR Framing error flag
     */
    protected void clearCrFerr() {
        cr = cr & 0b11111011;
    }


    protected boolean isMod0RxeSet() {
        return (mod0 & 0b00100000) != 0;
    }

    protected void clearMod0Rxe() {
        mod0 = mod0 & 0b11011111;
    }

    protected int getMod0Sm() {
        return (mod0 >> 2) & 0b11;
    }

    protected boolean isMod1TxeSet() {
        return (mod1 & 0b00010000) != 0;
    }

    protected void clearMod1Txe() {
        mod1 = mod1 & 0b11101111;
    }

    protected int getMod1Fdpx() {
        return (mod1 >> 5) & 0b11;
    }

    protected boolean isMod1FdpxRxSet() {
        return (mod1 & 0b00100000) != 0;
    }

    protected boolean isMod1FdpxTxSet() {
        return (mod1 & 0b01000000) != 0;
    }


    /**
     * Check if MOD2 Transmit Buffer Empty flag is set
     */
    protected boolean isMod2TbempSet() {
        return (mod2 & 0b10000000) != 0;
    }

    /**
     * Set MOD2 Transmit Buffer Empty flag
     */
    protected void setMod2Tbemp() {
        mod2 = mod2 | 0b10000000;
    }

    /**
     * Clear MOD2 Transmit Buffer Empty flag
     */
    protected void clearMod2Tbemp() {
        mod2 = mod2 & 0b01111111;
    }

    /**
     * Check if MOD2 Receive Buffer Full flag is set
     */
    protected boolean isMod2RbfllSet() {
        return (mod2 & 0b01000000) != 0;
    }

    /**
     * Set MOD2 Receive Buffer Full flag
     */
    protected void setMod2Rbfll() {
        mod2 = mod2 | 0b01000000;
    }

    /**
     * Clear MOD2 Receive Buffer Full flag
     */
    protected void clearMod2Rbfll() {
        mod2 = mod2 & 0b10111111;
    }

    /**
     * Check if MOD2 Tx Run flag is set
     */
    protected boolean isMod2TxrunSet() {
        return (mod2 & 0b00100000) != 0;
    }

    /**
     * Set MOD2 Tx Run flag
     */
    protected void setMod2Txrun() {
        mod2 = mod2 | 0b00100000;
    }

    /**
     * Clear MOD2 Tx Run flag
     */
    protected void clearMod2Txrun() {
        mod2 = mod2 & 0b11011111;
    }



    protected boolean isFcnfRfstSet() {
        return (fcnf & 0b00010000) != 0;
    }

    protected boolean isFcnfTfieSet() {
        return (fcnf & 0b00001000) != 0;
    }

    protected boolean isFcnfRfieSet() {
        return (fcnf & 0b00000100) != 0;
    }

    protected boolean isFcnfRxtxcntSet() {
        return (fcnf & 0b00000010) != 0;
    }

    protected boolean isFcnfCnfgSet() {
        return (fcnf & 0b00000001) != 0;
    }


    /**
     * Compute Rx FIFO Fill Level to generate interrupts
     * Note: overridden in TxHSerialInterface
     */
    protected void computeRxFillLevel() {
        if (getMod1Fdpx() == 0b11) {
            // Full duplex
            rxInterruptFillLevel = rfc & 0b1;
            if (rxInterruptFillLevel == 0) {
                // Special case
                rxInterruptFillLevel = 2;
            }
        }
        else {
            // Half duplex
            rxInterruptFillLevel = rfc & 0b11;
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
        if (getMod1Fdpx() == 0b11) {
            // Full duplex
            txInterruptFillLevel = tfc & 0b1;
        }
        else {
            // Half duplex
            txInterruptFillLevel = tfc & 0b11;
        }
    }



    protected int getMaxFifoSize() {
        if (getMod1Fdpx() == 0b11) {
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
                interruptController.request(getTxInterruptNumber());
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
                    if (isMod1FdpxTxSet()) {
                        interruptController.request(getTxInterruptNumber());
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
                    interruptController.request(getRxInterruptNumber());
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
                        interruptController.request(getRxInterruptNumber());
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
    /**
     * The goal of this is to delay the actual write to the other device of CYCLES_PER_BYTE cycles
     * The TX empty interrupt occurs at half way
     */
    public boolean onCycleCountChange(long oldCount, int increment) {
        boolean remainRegistered = true;
        if (cycleCounter > CYCLES_PER_BYTE/2 && delayedValue == null) {
            delayedValue = read();
        }
        cycleCounter += increment;
        if (cycleCounter >= CYCLES_PER_BYTE) {
            if (delayedValue != null) {
                super.valueReady(delayedValue);
            }
            else {
                // End of transmission - unregister
                remainRegistered = false;
            }
            delayedValue = null;
            cycleCounter = 0;
        }
        return remainRegistered;
    }
}
