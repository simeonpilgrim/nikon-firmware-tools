package com.nikonhacker.emu.peripherials.keyCircuit.tx;

import com.nikonhacker.emu.peripherials.keyCircuit.Key;

public class TxKey implements Key {
    private int keyNum;
    
    private int kwupst = 0x20;
    private int state;
    private int latch;
    private TxKeyCircuit  keyCircuit;

    public TxKey(int keyNum, TxKeyCircuit keyCircuit) {
        this.keyNum = keyNum;
        this.keyCircuit = keyCircuit;
    }

    /**
     * Set interrupt latch register if event match
     */
    private void setLatch(int value) {
        switch (kwupst&0x70) {
            case 0x00: // "L"
            case 0x20: // falling edge
                if (value==0)
                    latch = 1;
                break;
            case 0x10: // "H"
            case 0x30: // rising edge
                if (value==1)
                    latch = 1;
                break;
            case 0x40: // both edges
                latch = 1;
        }
        // notify always
        if ((kwupst & 1) != 0) {
            if (latch != 0)
                keyCircuit.requestInterrupt(keyNum);
            else
                keyCircuit.removeInterrupt(keyNum);
        }
    }

    /**
     * Set Key pin state and check for interrupt event
     */
    public void setValue(int value) {
        synchronized (this) {
            setLatch(value);
            state = value;
        }
    }

    /**
     * Return actual Key pin state
     */
    public int getValue() {
        synchronized (this) {
            return state;
        }
    }
    
    /**
     * Return latched interrupt value; clear latch bits if event is not active anymore
     */
    public int getLatch() {
        synchronized (this) {
            int oldLatch = latch;
             // don't clear bits if input level still not withdrawn
            switch (kwupst&0x70) {
                case 0: // "L"
                    if (state!=0)
                        latch = 0;
                    break;
                case 0x10: // "H"
                    if (state==1)
                        break;
                default:
                    latch = 0;
            }
            if ((kwupst & 1) != 0) {
                // clear interrupt
                if (oldLatch != latch)
                    keyCircuit.removeInterrupt(keyNum);
            }
            return oldLatch;
        }
    }

    /**
     * Get interrupt event type
     */
    public int getKWUPST() {
        return kwupst;
    }

    /**
     * Set interrupt event type and update interrupt latch
     */
    public void setKWUPST(int value) {
        if ((value & 1) == 0) {
            // remove to be sure
            keyCircuit.removeInterrupt(keyNum);
        }
        if ((value & 0x70) > 0x40) {
            throw new RuntimeException("KEY invalid active state");
        }
        synchronized (this) {
            // ignore not used bits
            kwupst = value & 0xF1;
            setLatch(state);
        }
    }
}
