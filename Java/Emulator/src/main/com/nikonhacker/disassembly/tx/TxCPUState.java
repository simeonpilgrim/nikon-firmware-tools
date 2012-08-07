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

            "hi",       "lo",       "Config",   "Config1",
            "Config2",  "Config3",  "BadVAddr", "Count",
            "Compare",  "Status",   "Cause",    "EPC",
            "ErrorEPC", "PRId",     "IER",      "SSCR",

            "Debug",    "DEPC",     "DESAVE"
    };

    public final static int GP = 28;
    public final static int SP = 29;
    public final static int FP = 30;
    public final static int RA = 31;
    public final static int HI = 32;
    public final static int LO = 33;
    public final static int Config = 34;
    public final static int Config1 = 35;
    public final static int Config2 = 36;
    public final static int Config3 = 37;
    public final static int BadVAddr = 38;
    public final static int Count = 39;
    public final static int Compare = 40;
    public final static int Status = 41;
    public final static int Cause = 42;
    public final static int EPC = 43;
    public final static int ErrorEPC = 44;
    public final static int PRId = 45;
    public final static int IER = 46;
    public final static int SSCR = 47;
    public final static int Debug = 48;
    public final static int DEPC = 49;
    public final static int DESAVE = 50;

    public final static int Status_RP_bit = 27;
    public final static int Status_FR_bit = 26;
    public final static int Status_RE_bit = 25;
    public final static int Status_MX_bit = 24;
    public final static int Status_PX_bit = 23;
    public final static int Status_BEV_bit = 22;
    public final static int Status_NMI_bit = 19;
    public final static int Status_KX_bit = 7;
    public final static int Status_SX_bit = 6;
    public final static int Status_UX_bit = 5;
    public final static int Status_UM_bit = 4;
    public final static int Status_R0_bit = 3;
    public final static int Status_ERL_bit = 2;
    public final static int Status_EXL_bit = 1;
    public final static int Status_IE_bit = 0;

    public enum PowerMode {
        RUN,
        HALT,
        DOZE
    }


    private Register32[][] shadowRegisterSets;

    private int activeRegisterSet;

    private PowerMode powerMode;

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


    public int getActiveRegisterSet() {
        return activeRegisterSet;
    }

    public void setActiveRegisterSet(int activeRegisterSet) {
        this.activeRegisterSet = activeRegisterSet;
    }

    public PowerMode getPowerMode() {
        return powerMode;
    }

    public void setPowerMode(PowerMode powerMode) {
        this.powerMode = powerMode;
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

        // All general registers have different values in each set (except 0 is dummy)
        for (int registerSet = 0; registerSet < 8; registerSet++) {
            // register 0 is dummy
            shadowRegisterSets[registerSet][0] = reg0;
            // base layout : all sets have separate register values
            for (int i = 1; i < HI; i++) {
                shadowRegisterSets[registerSet][i] = new Register32(0);
            }
        }

        // patch exceptions: r26-27-28 and all registers starting at HI are common to all sets
        for (int registerSet = 1; registerSet < 8; registerSet++) {
            for (int i = 26; i <= 28; i++) {
                shadowRegisterSets[registerSet][i] = shadowRegisterSets[0][i];
            }
        }

        // other exception: r29 is separate in set 0, but common to sets 1-7
        for (int registerSet = 2; registerSet < 8; registerSet++) {
            shadowRegisterSets[registerSet][29] = shadowRegisterSets[1][29];
        }

        // All registers starting from HI are a single set, so share them
        for (int i = HI; i < regValue.length; i++) {
            Register32 r = new Register32();
            for (int registerSet = 0; registerSet < 8; registerSet++) {
               shadowRegisterSets[registerSet][i] = r;
            }
        }

        regValidityBitmap = 0;
    }

    public void clear() {
        for (int registerSet = 0; registerSet < 8; registerSet++) {
            setActiveRegisterSet(registerSet);
            for (int i = 0; i < regValue.length; i++) {
                setReg(i, 0);
            }
        }
        setActiveRegisterSet(0);

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
