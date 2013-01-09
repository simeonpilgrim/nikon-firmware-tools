package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

import java.util.Deque;
import java.util.LinkedList;

public class TxSerialInterface extends SerialInterface {
    /**
     * Rx FIFO
     */
    private Deque<Integer> rxFifo = new LinkedList<Integer>();
    private int rxFifoSize = Integer.MAX_VALUE;
    private int rxFillLevel;

    /**
     * Tx FIFO.
     */
    private Deque<Integer> txFifo = new LinkedList<Integer>();
    private int txFifoSize = Integer.MAX_VALUE;

    private int en; // Enable register
    private int cr; // Control register
    private int mod0; // Mode control register 0
    private int mod1; // Mode control register 1
    private int mod2 = 0b10000000; // Mode control register 2
    private int brcr; // Baud rate generator control register
    private int bradd; // Baud rate generator control register 2
    private int rfc; // Receive FIFO control register
    private int tfc; // Transmit FIFO control register
    private int rst; // Receive FIFO status register
    private int tst = 0b10000000; // Transmit FIFO status register
    private int fcnf; // FIFO configuration register

    public TxSerialInterface(int serialInterfaceNumber, InterruptController interruptController) {
        super(serialInterfaceNumber, interruptController);
    }

    public int getEn() {
        return en;
    }

    public void setEn(int en) {
//        System.out.println(getName() + ".setEn(0x" + Format.asHex(en, 8) + ")");
        this.en = en & 0x1;
    }

    public int getBuf() {
//        if (en == 0) {
//            throw new RuntimeException("Attempt to receive data from disabled " + getName());
//        }
        Integer poll = rxFifo.poll();
        if (poll == null) {
//            System.err.println(getName() + " - Attempt to read from empty buffer");
            return 0;
        }
        return poll;
    }

    public void setBuf(int buf) {
//        System.out.println(getName() + ".setBuf(0x" + Format.asHex(buf, 8) + ")");
//        if (en == 0) {
//            throw new RuntimeException("Attempt to transmit data to disabled " + getName());
//        }
        txFifo.add(buf);
    }

    public int getCr() {
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
        this.cr = cr;
    }

    public int getMod0() {
        return mod0;
    }

    public void setMod0(int mod0) {
//        System.out.println(getName() + ".setMod0(0x" + Format.asHex(mod0, 8) + ")");
        this.mod0 = mod0;
    }

    public int getMod1() {
        return mod1;
    }

    public void setMod1(int mod1) {
//        System.out.println(getName() + ".setMod1(0x" + Format.asHex(mod1, 8) + ")");
        this.mod1 = mod1;
        computeFillLevel();
    }

    public int getMod2() {
        return mod2;
    }

    public void setMod2(int mod2) {
//        System.out.println(getName() + ".setMod2(0x" + Format.asHex(mod2, 8) + ")");
        this.mod2 = mod2;
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
        this.bradd = bradd;
    }

    public int getRfc() {
        return rfc & 0b01000011;
    }

    public void setRfc(int rfc) {
        if ((rfc & 0b10000000) != 0) { // RFCS
            rxFifo = new LinkedList<Integer>();
        }
        // TODO RFIS
        this.rfc = rfc;
        computeFillLevel();
    }

    private void computeFillLevel() {
        if (((mod1 >> 5) & 0b11) == 0b11) {
            // Full duplex
            switch (rfc & 0b1) {
                case 0b0:
                    rxFillLevel=2; break;
                case 0b1:
                    rxFillLevel=1; break;
            }
        }
        else {
            switch (rfc & 0b11) {
                case 0b00:
                    rxFillLevel=4; break;
                case 0b01:
                    rxFillLevel=1; break;
                case 0b10:
                    rxFillLevel=2; break;
                case 0b11:
                    rxFillLevel=3; break;
            }
        }
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
        if ((fcnf & 0b00000001) == 0) {
            txFifoSize = 2;
            rxFifoSize = 2;
        }
        else {
            txFifoSize = Integer.MAX_VALUE;
            rxFifoSize = Integer.MAX_VALUE;
        }
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
        if ((en != 0) && (mod1 & 0b00010000) != 0) { // TXE
            return txFifo.poll();
        }
        else {
            if (en == 0) {
                System.out.println(getName() + " is disabled. Returning 0.");
            }
            else {
                System.out.println("TX is disabled on " + getName() + ". Returning 0.");
            }
            return 0;
        }
    }

    // RECEPTION LOGIC

    /**
     * Sets the data received via Serial port
     * This can only be called by external software to simulate data writing by another device
     * @param value 5 to 9 bits integer corresponding to a single value written by a device to this serial port
     */
    @Override
    public void write(int value) {
        if ((en != 0) && (mod0 & 0b00100000) != 0) { // RXE
            rxFifo.add(value);
        }
        else {
            if (en == 0) {
                System.out.println(getName() + " is disabled. Value 0x" + Format.asHex(value, 2) + " is ignored.");
            }
            else {
                System.out.println("RX is disabled on " + getName() + ". Value 0x" + Format.asHex(value, 2) + " is ignored.");
            }
        }
    }

    @Override
    public int getNbBits() {
        return 8;  //TODO
    }

}
