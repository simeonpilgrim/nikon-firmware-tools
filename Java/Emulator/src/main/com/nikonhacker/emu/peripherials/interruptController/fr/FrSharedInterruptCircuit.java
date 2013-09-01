package com.nikonhacker.emu.peripherials.interruptController.fr;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.SharedInterruptCircuit;

public class FrSharedInterruptCircuit implements SharedInterruptCircuit {
    private InterruptController interruptController;

    public static final int SHARED_INTERRUPT_BASE_NR = 0x1C;
    
    private static final int[] SHARED_INTERRUPTS = {
        0,   /* 0x1C */
        1,   /* 0x1D */
        2,   /* 0x1E */
        -1,  /* 0x1F */
        -1,  /* 0x20 */
        -1,  /* 0x21 */
        -1,  /* 0x22 */
        -1,  /* 0x23 */
        3,   /* 0x24 */
        -1,  /* 0x25 */
        -1,  /* 0x26 */
        -1,  /* 0x27 */
        4,   /* 0x28 */
        5,   /* 0x29 */
        6,   /* 0x2A */
        7,   /* 0x2B */
        8,   /* 0x2C */
        9,   /* 0x2D */
        10,   /* 0x2E */
        11,   /* 0x2F */
        12,   /* 0x30 */
        -1,  /* 0x31 */
        13    /* 0x32 */
        };
    
    private int[] sourceStatus = new int[14];

    public FrSharedInterruptCircuit(InterruptController interruptController) {
        this.interruptController = interruptController;
    }
    
    public int getStatusReg(int num) {
        return sourceStatus[num];
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
