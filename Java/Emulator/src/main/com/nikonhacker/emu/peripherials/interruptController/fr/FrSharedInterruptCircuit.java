package com.nikonhacker.emu.peripherials.interruptController.fr;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.SharedInterruptCircuit;

public class FrSharedInterruptCircuit implements SharedInterruptCircuit {
    private InterruptController interruptController;

    /*
      there is one function in code that is manipulating register 0x6B000030 and 
      then setting ICR 0x1F priority in register 0x45F for INT 0x2F. This and information
      about usage of 0x6B00008X registers in interrupt handlers bring to conclusion
      that first shared interrupt is 0x1B
     */
    public static final int SHARED_INTERRUPT_BASE_NR = 0x1B;
    
    private static final int[] SHARED_INTERRUPTS = {
        0,   /* 0x1B */
        1,   /* 0x1C */
        2,   /* 0x1D */
        3,   /* 0x1E */
        -1,  /* 0x1F */
        -1,  /* 0x20 */
        -1,  /* 0x21 */
        -1,  /* 0x22 */
        -1,  /* 0x23 */
        4,   /* 0x24 */
        -1,  /* 0x25 */
        -1,  /* 0x26 */
        -1,  /* 0x27 */
        5,   /* 0x28 */
        6,   /* 0x29 */
        7,   /* 0x2A */
        8,   /* 0x2B */
        9,   /* 0x2C */
        10,   /* 0x2D */
        11,   /* 0x2E */
        12,   /* 0x2F */
        13,   /* 0x30 */
        -1,  /* 0x31 */
        14    /* 0x32 */
        };
    
    private int[] sourceStatus = new int[15];
    private int[] sourceConfig = new int[15];

    public FrSharedInterruptCircuit(InterruptController interruptController) {
        this.interruptController = interruptController;
    }
    
    public int getStatusReg(int num) {
        return sourceStatus[num];
    }
    
    public void setConfigReg(int num, int value) {
        sourceConfig[num] = value;
        // TODO
        // coderat: it is not clear what a purpose of this register is
        // it seems to be not disable/enable, because it is only set for some
        // interrupts and not for all that are expected.
    }

    public int getConfigReg(int num) {
        return sourceConfig[num];
    }

    public boolean request(int interruptNumber, int sourceNumber) {
        int idx = interruptNumber - SHARED_INTERRUPT_BASE_NR;
        
        sourceNumber &= 0x1F;
        if (idx >= 0 && idx < SHARED_INTERRUPTS.length) {
            if (SHARED_INTERRUPTS[idx] != -1) {
                idx = SHARED_INTERRUPTS[idx];
                synchronized (sourceStatus) {
                    sourceStatus[idx] |= (1 << sourceNumber);
                    return interruptController.request(interruptNumber);
                }
            }
        }
        throw new RuntimeException("Wrong shared interrupt number");
    }
    
    public void removeRequest(int interruptNumber, int sourceNumber) {
        int idx = interruptNumber - SHARED_INTERRUPT_BASE_NR;

        sourceNumber &= 0x1F;
        if (idx >= 0 && idx < SHARED_INTERRUPTS.length) {
            if (SHARED_INTERRUPTS[idx] != -1) {
                idx = SHARED_INTERRUPTS[idx];
                
                synchronized (sourceStatus) {
                    int mask = 1 << sourceNumber;
                    
                    if ((sourceStatus[idx] & mask) != 0) {
                        sourceStatus[idx] ^= mask;
                        if (sourceStatus[idx] == 0) {
                            interruptController.removeRequest(interruptNumber);
                        }
                    }
                }
                return;
            }
        }
        throw new RuntimeException("Wrong shared interrupt number");
    }
}
