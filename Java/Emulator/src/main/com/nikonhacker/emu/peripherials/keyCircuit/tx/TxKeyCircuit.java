package com.nikonhacker.emu.peripherials.keyCircuit.tx;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.keyCircuit.KeyCircuit;

public class TxKeyCircuit implements KeyCircuit {

    public TxKey keys[] = new TxKey[32];
    private int kwupcnt;
    private int kwupclr;
    
    private InterruptController interruptController;
    private int requestedInts;

    public TxKeyCircuit(InterruptController interruptController) {
        this.interruptController = interruptController;
        for (int i = 0; i < 32; i++) {
            keys[i] = new TxKey(i, this);
        }
    }

    /**
     Update bits in KWUPINT register from start to (end-1) keys and return latch
    */
    private int updateKWUPINT(int start, int end) {
        int kwupint=0;
        
        end--;
        for (; end>=start; end--) {
            kwupint <<= 1;
            if (keys[end].getLatch()!=0)
                kwupint |= 1;
        }
        return (kwupint << start);
    }

    /**
     Return actual state of all keys
    */
    public int getPKEY() {
        int pkey=0;
        
        for (int i=31; i>=0; i--) {
            pkey <<= 1;
            if (keys[i].getValue()!=0)
                pkey |= 1;
        }
        return pkey;
    }
    
    /**
     Return actual state of all keys
    */
    public int getKWUPINT() {
        return updateKWUPINT(0, 32);
    }

    /**
     Return KWUPINT register
    */
    public byte getKWUPINTn(int n) {
        int StartBit = (3-n)*8;
        return (byte)(updateKWUPINT(StartBit, StartBit+8) >> StartBit);
    }

    public int getKWUPCNT() {
        return kwupcnt;
    }

    public int getKWUPCLR() {
        // always 0
        return 0;
    }

    public void setKWUPCNT(int value) {
        // ignore not used bits
        kwupcnt = value & 0x3C;
    }

    /**
    Clear all interrupt requests
    */
    public void setKWUPCLR(int value) {
        if (value == 0b1010) {
            // reading KWPUINT clears also interrupt request if possible
            getKWUPINT();
        }
    }
    
    public boolean requestInterrupt (int keyNum) {
        synchronized (this) {
            requestedInts |= (1<<keyNum);
            return interruptController.request(TxInterruptController.KWUP);
        }
    }

    public void removeInterrupt (int keyNum) {
        synchronized (this) {
            requestedInts &= (~(1<<keyNum));
            if (requestedInts == 0) {
                interruptController.removeRequest(TxInterruptController.KWUP);
            }
        }
    }
}
