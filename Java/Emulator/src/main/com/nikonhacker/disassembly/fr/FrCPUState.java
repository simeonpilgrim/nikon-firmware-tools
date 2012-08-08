package com.nikonhacker.disassembly.fr;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.Register32;
import com.nikonhacker.emu.InterruptRequest;

import java.util.EnumSet;
import java.util.Set;

public class FrCPUState extends CPUState {
    public static String[] REG_LABEL = new String[]{
            "R0",       "R1",       "R2",       "R3",
            "R4",       "R5",       "R6",       "R7",
            "R8",       "R9",       "R10",      "R11",
            "R12",      "R13",      "R14",      "R15",  /* standard names by default */

            "TBR",      "RP",       "SSP",      "USP",
            "MDH",      "MDL",      "D6",       "D7",
            "D8",       "D9",       "D10",      "D11",
            "D12",      "D13",      "D14",      "D15",

            "CR0",      "CR1",      "CR2",      "CR3",
            "CR4",      "CR5",      "CR6",      "CR7",
            "CR8",      "CR9",      "CR10",     "CR11",
            "CR12",     "CR13",     "CR14",     "CR15",

            "PS",       "CCR"
    };
    public final static int DEDICATED_REG_OFFSET =   16;
    public final static int COPROCESSOR_REG_OFFSET = 32;

    public final static int AC = 13;
    public final static int FP = 14;
    public final static int SP = 15;
    public final static int TBR = 16;
    public final static int RP = 17;
    public final static int SSP = 18;
    public final static int USP = 19;
    public final static int MDH = 20;
    public final static int MDL = 21;

    public final static int PS = 48;
    public final static int CCR = 49;

    /* bits of the PS register (ILM part) */
    public int ILM4 = 0;
    public int ILM3 = 1;
    public int ILM2 = 1;
    public int ILM1 = 1;
    public int ILM0 = 1;

    /* bits of the PS register (SCR part) */
    public int D0 = 0; // 1 if the dividend is negative
    public int D1 = 0; // 1 if the dividend and divisor have opposite signs
    public int T = 0;

    /* bits of the PS register (CCR part) */
    private int S=0; // only accessible via setter because it also points R15 to correct stack pointer
    public int I=0;
    public int N=0;
    public int Z=0;
    public int V=0;
    public int C=0;

    /**
     * Default decoding upon class loading
     */
    static {
        initRegisterLabels(EnumSet.noneOf(OutputOption.class));
    }


    /**
     * Constructor
     */
    public FrCPUState() {
        reset();
    }

    /**
     * Constructor
     * @param startPc initial value for the Program Counter
     */
    public FrCPUState(int startPc) {
        reset();
        pc = startPc;
    }

    public static void initRegisterLabels(Set<OutputOption> outputOptions) {
        // Patch names if requested
        if (outputOptions.contains(OutputOption.REGISTER)) {
            REG_LABEL[AC] = "AC";
            REG_LABEL[FP] = "FP";
            REG_LABEL[SP] = "SP";
        }
    }

    /**
     * Returns CCR part of the PS register (built from individual bits)
     * @return CCR
     */
    public int getCCR() {
        return (S << 5) | (I << 4) | (N << 3) | (Z << 2) | (V << 1) | C; 
    }

    /**
     * Sets CCR part of the PS register (splits it into individual bits)
     * @param ccr
     */
    public void setCCR(int ccr) {
        setS((ccr & 0x20) >> 5);
        I = (ccr & 0x10)>> 4;
        N = (ccr & 0x08) >> 3;
        Z = (ccr & 0x04) >> 2;
        V = (ccr & 0x02) >> 1;
        C = ccr & 0x01;
    }

    /**
     * Returns S bit of the PS register (built from individual bits)
     * @return CCR
     */
    public int getS() {
        return S;
    }


    /**
     * Sets S bit of the PS register (switching R15 to USP or SSP behaviour accordingly)
     * @param newS
     */
    public void setS(int newS) {
        S = newS;
        if (S == 0) {
            regValue[15] = regValue[SSP];
        }
        else {
            regValue[15] = regValue[USP];
        }
    }

    /**
     * Returns SCR part of the PS register (built from individual bits)
     * @return SCR
     */
    public int getSCR() {
        return (D1 << 2) | (D0 << 1) | T;
    }

    /**
     * Sets SCR part of the PS register (splits it into individual bits)
     * @param scr
     */
    public void setSCR(int scr) {
        D1 = (scr & 0x04) >> 2;
        D0 = (scr & 0x02) >> 1;
        T = scr & 0x01;
    }


    /**
     * Returns ILM part of the PS register (built from individual bits)
     * @return ILM
     */
    public int getILM() {
        return (ILM4 << 4) |(ILM3 << 3) |(ILM2 << 2) | (ILM1 << 1) | ILM0;
    }

    /**
     * Sets ILM part of the PS register (splits it into individual bits)
     * @param ilm new ILM value. According to the spec : "A limited range of values can be set from programs.
     *            If the original value is in a range of 16 to 31, a value ranging from 16 to 31 can be specified
     *            as a new value. If a value ranging from 0 to 15 is set for an statement, (specified-value + 16)
     *            is transferred when the statement is executed.
     *            If the original value is in a range of 0 to 15, any value ranging from 0 to 31 can be specified.
     * @param isFromPrograms true if the statement is called by a program
     */
    public void setILM(int ilm, boolean isFromPrograms) {
        if (ILM4 == 0 || !isFromPrograms) {
            ILM4 = (ilm & 0x10) >> 4;
        }
        ILM3 = (ilm & 0x08) >> 3;
        ILM2 = (ilm & 0x04) >> 2;
        ILM1 = (ilm & 0x02) >> 1;
        ILM0 = ilm & 0x01;
    }


    /**
     * Rebuilds PS from individual parts
     * @return PS
     */
    public int getPS() {
        /* According to the Fujitsu Spec CM71-00104-3E.pdf, p29 (or progfr-cm71-00101-5e.pdf , p. 19):
         * The read value of reserved bits is always "0". Write values should always be written as "0"
         * So :
         */
         return (getILM() << 16) | (getSCR() << 8) | getCCR();

        /* Although, all examples write the unused bits as "1" and expect to read them back as 1
         * Ex : CM71-00104-3E.pdf, page 321 (sect. 7.119) (or progfr-cm71-00101-5e.pdf, page 158 (sect. 7.62))
         * So :
         */

//        return 0xffe0f8c0 /* 0b 11111111 11100000 11111000 11000000 */
//                | (getILM() << 16) | (getSCR() << 8) | getCCR();
    }

    /**
     * Sets PS by splitting it into its individual parts
     * @param ps new PS value
     * @param isFromPrograms true if the statement is called by a program. See setILM
     */
    public void setPS(int ps, boolean isFromPrograms) {
        setILM((ps >> 16) & 0xFF, isFromPrograms);
        setSCR((ps >> 8) & 0xFF);
        setCCR(ps & 0xFF);
    }

    public void reset() {
        regValue = new Register32[REG_LABEL.length];
        for (int i = 0; i < regValue.length; i++) {
            regValue[i] = new Register32(0);
        }
        regValue[15] = regValue[SSP];
        setILM(0xf, false); // 0b1111
        T = 0;
        I = 0;
        setS(0);
        setReg(TBR, 0x000FFC00);
        setReg(SSP, 0x00000000);
        regValidityBitmap = 0;
    }

    public void clear() {
        for (int i = 0; i < regValue.length; i++) {
            regValue[i] = new Register32(0);
        }
        regValue[15] = regValue[SSP];
        setILM(0, false);
        T = 0;
        I = 0;
        setS(0);
        setReg(TBR, 0);
        setReg(SSP, 0);
        regValidityBitmap = 0;
    }


    public boolean accepts(InterruptRequest interruptRequest) {
        return(
                (I == 1 && interruptRequest.getICR() < getILM())
             || (getILM() > 15 && interruptRequest.isNMI())
              );
    }

    public FrCPUState clone() {
        FrCPUState cloneCpuState = new FrCPUState();
        for (int i = 0; i <= FrCPUState.CCR; i++) {
            cloneCpuState.setReg(i, getReg(i));
        }
        cloneCpuState.flags = flags;
        cloneCpuState.regValidityBitmap = regValidityBitmap;
        cloneCpuState.pc = pc;
        return cloneCpuState;
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

}
