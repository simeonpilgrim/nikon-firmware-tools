package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

import java.util.Deque;
import java.util.LinkedList;

public class TxSerialInterface extends SerialInterface {
    /**
     * Rx FIFO
     */
    private Deque<Integer> rxFifo = new LinkedList<Integer>();

    /**
     * Tx FIFO.
     */
    private Deque<Integer> txFifo = new LinkedList<Integer>();

    private int en; // Enable register
    private int buf; // TX/RX buffer register
    private int cr; // Control register
    private int mod0; // Mode control register 0
    private int mod1; // Mode control register 1
    private int mod2; // Mode control register 2
    private int brcr; // Baud rate generator control register
    private int bradd; // Baud rate generator control register 2
    private int rfc; // Receive FIFO control register
    private int tfc; // Transmit FIFO control register
    private int rst; // Receive FIFO status register
    private int tst; // Transmit FIFO status register
    private int fcnf; // FIFO configuration register

    public TxSerialInterface(int serialInterfaceNumber, InterruptController interruptController) {
        super(serialInterfaceNumber, interruptController);
    }

    public int getEn() {
        return en;
    }

    public void setEn(int en) {
        this.en = en & 0x1;
    }

    public int getBuf() {
        Integer poll = rxFifo.poll();
        if (poll == null) {
            System.err.println(getName() + " - Attempt to read from empty buffer");
            return 0;
        }
        return poll;
    }

    public void setBuf(int buf) {
        txFifo.add(buf);
    }

    public int getCr() {
        return cr;
    }

    public void setCr(int cr) {
        this.cr = cr;
    }

    public int getMod0() {
        return mod0;
    }

    public void setMod0(int mod0) {
        this.mod0 = mod0;
    }

    public int getMod1() {
        return mod1;
    }

    public void setMod1(int mod1) {
        this.mod1 = mod1;
    }

    public int getMod2() {
        return mod2;
    }

    public void setMod2(int mod2) {
        this.mod2 = mod2;
    }

    public int getBrcr() {
        return brcr;
    }

    public void setBrcr(int brcr) {
        this.brcr = brcr;
    }

    public int getBradd() {
        return bradd;
    }

    public void setBradd(int bradd) {
        this.bradd = bradd;
    }

    public int getRfc() {
        return rfc;
    }

    public void setRfc(int rfc) {
        this.rfc = rfc;
    }

    public int getTfc() {
        return tfc;
    }

    public void setTfc(int tfc) {
        this.tfc = tfc;
    }

    public int getRst() {
        return rst;
    }

    public void setRst(int rst) {
        this.rst = rst;
    }

    public int getTst() {
        return tst;
    }

    public void setTst(int tst) {
        this.tst = tst;
    }

    public int getFcnf() {
        return fcnf;
    }

    public void setFcnf(int fcnf) {
        this.fcnf = fcnf;
    }

    // TRANSMISSION LOGIC

    /**
     * Gets the data transmitted via Serial port
     * This can only be called by external software to simulate data reading by another device
     * @return 5 to 9 bits integer corresponding to a single value read by a device from this serial port
     */
    @Override
    public Integer read() {
        return txFifo.poll();
    }

    // RECEPTION LOGIC

    /**
     * Sets the data received via Serial port
     * This can only be called by external software to simulate data writing by another device
     * @param value 5 to 9 bits integer corresponding to a single value written by a device to this serial port
     */
    @Override
    public void write(int value) {
        rxFifo.add(value);
    }

    @Override
    public int getNbBits() {
        return 8;  //TODO
    }

}
