package com.nikonhacker.disassembly.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.Register32;

import java.util.EnumSet;
import java.util.Set;

public class TxCPUState extends CPUState {
    /** registers names */
    public static String[] REG_LABEL = new String[]{
            "r0",       "r1",       "r2",       "r3",   /* standard names by default */
            "r4",       "r5",       "r6",       "r7",
            "r8",       "r9",       "r10",      "r11",
            "r12",      "r13",      "r14",      "r15",

            "r16",      "r17",      "r18",      "r19",
            "r20",      "r21",      "r22",      "r23",
            "r24",      "r25",      "r26",      "r27",
            "r28",      "r29",      "r30",      "r31",

            "hi",       "lo"
    };
    private Register32[][] shadowRegisterSets;

    public int getActiveRegisterSet() {
        return activeRegisterSet;
    }

    public void setActiveRegisterSet(int activeRegisterSet) {
        this.activeRegisterSet = activeRegisterSet;
    }

    private int activeRegisterSet;

    public final static int GP = 28;
    public final static int SP = 29;
    public final static int FP = 30;
    public final static int RA = 31;
    public final static int HI = 32;
    public final static int LO = 33;

    /**
     * Default decoding upon class loading
     */
    static {
        initRegisterLabels(EnumSet.noneOf(OutputOption.class));
    }


    /**
     * Constructor
     */
    public TxCPUState() {
        reset();
    }

    /**
     * Constructor
     * @param startPc initial value for the Program Counter
     */
    public TxCPUState(int startPc) {
        reset();
        pc = startPc;
    }


    public static void initRegisterLabels(Set<OutputOption> outputOptions) {
        // Patch names if requested
        if (outputOptions.contains(OutputOption.REGISTER)) {
            TxCPUState.REG_LABEL[0] = "$zero";
            TxCPUState.REG_LABEL[1] = "$at";
            TxCPUState.REG_LABEL[2] = "$v0";
            TxCPUState.REG_LABEL[3] = "$v1";
            TxCPUState.REG_LABEL[4] = "$a0";
            TxCPUState.REG_LABEL[5] = "$a1";
            TxCPUState.REG_LABEL[6] = "$a2";
            TxCPUState.REG_LABEL[7] = "$a3";
            TxCPUState.REG_LABEL[8] = "$t0";
            TxCPUState.REG_LABEL[9] = "$t1";
            TxCPUState.REG_LABEL[10] = "$t2";
            TxCPUState.REG_LABEL[11] = "$t3";
            TxCPUState.REG_LABEL[12] = "$t4";
            TxCPUState.REG_LABEL[13] = "$t5";
            TxCPUState.REG_LABEL[14] = "$t6";
            TxCPUState.REG_LABEL[15] = "$t7";
            TxCPUState.REG_LABEL[16] = "$s0";
            TxCPUState.REG_LABEL[17] = "$s1";
            TxCPUState.REG_LABEL[18] = "$s2";
            TxCPUState.REG_LABEL[19] = "$s3";
            TxCPUState.REG_LABEL[20] = "$s4";
            TxCPUState.REG_LABEL[21] = "$s5";
            TxCPUState.REG_LABEL[22] = "$s6";
            TxCPUState.REG_LABEL[23] = "$s7";
            TxCPUState.REG_LABEL[24] = "$t8";
            TxCPUState.REG_LABEL[25] = "$t9";
            TxCPUState.REG_LABEL[26] = "$k0";
            TxCPUState.REG_LABEL[27] = "$k1";
            TxCPUState.REG_LABEL[28] = "$gp";
            TxCPUState.REG_LABEL[29] = "$sp";
            TxCPUState.REG_LABEL[30] = "$fp";
            TxCPUState.REG_LABEL[31] = "$ra";
        }
    }

    public String toString() {
        String registers = "";
        for (int i = 0; i < regValue.length; i++) {
            registers += REG_LABEL[i] + "=0x" + Format.asHex(getReg(i), 8) + "\n";
        }
        registers = registers.trim() + "]";
        return "CPUState : " +
                "flags=0b" + Integer.toString(flags,2) +
                ", pc=0x" + Format.asHex(pc, 8) +
                ", rvalid=0b" + Long.toString(regValidityBitmap, 2) +
                ", reg=" + registers +
                '}';
    }

    public void reset() {
        shadowRegisterSets = new Register32[8][REG_LABEL.length];
        activeRegisterSet = 0;
        regValue = shadowRegisterSets[activeRegisterSet];

        Register32 reg0 = new NullRegister32();

        for (int registerSet = 0; registerSet < 8; registerSet++) {
            // register 0 is dummy
            shadowRegisterSets[registerSet][0] = reg0;
            // base layout : all sets have separate register values
            for (int i = 1; i < regValue.length; i++) {
                shadowRegisterSets[registerSet][i] = new Register32(0);
            }
        }

        // patch exceptions: r26-27-28 and hi-lo are common to all sets
        for (int registerSet = 1; registerSet < 8; registerSet++) {
            for (int i = 26; i <= 28; i++) {
                shadowRegisterSets[registerSet][i] = shadowRegisterSets[0][i];
            }
            shadowRegisterSets[registerSet][HI] = shadowRegisterSets[0][HI];
            shadowRegisterSets[registerSet][LO] = shadowRegisterSets[0][LO];
        }

        // other exception: r29 is common to sets 1-7
        for (int registerSet = 2; registerSet < 8; registerSet++) {
            shadowRegisterSets[registerSet][29] = shadowRegisterSets[1][29];
        }

        regValidityBitmap = 0;
    }

    public void clear() {
        for (int i = 0; i < regValue.length; i++) {
            regValue[i] = new Register32(0);
        }
        regValidityBitmap = 0;
    }


    public TxCPUState clone() {
        TxCPUState cloneCpuState = new TxCPUState();
        for (int i = 0; i <= regValue.length; i++) {
            cloneCpuState.regValue[i] = new Register32(regValue[i].getValue());
        }
        cloneCpuState.flags = flags;
        cloneCpuState.regValidityBitmap = regValidityBitmap;
        cloneCpuState.pc = pc;
        return cloneCpuState;
    }
}
