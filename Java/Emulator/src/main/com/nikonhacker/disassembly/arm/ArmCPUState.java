package com.nikonhacker.disassembly.arm;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.Register32;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.fr.FrInterruptRequest;

import java.util.Set;

public class ArmCPUState extends CPUState {

    private static final int RESET_ADDRESS = 0x50000000;

    /** Register names (first array is "standard", second array is "alternate") */
    static String[][] REG_LABEL = new String[][]{
            {
                    "R0",       "R1",       "R2",       "R3",
                    "R4",       "R5",       "R6",       "R7",
                    "R8",       "R9",       "R10",      "R11",
                    "R12",      "R13",      "R14",             /* standard names by default */

                    "PSR",      "PSP",      "MSP"
            },
            {
                    "R0",       "R1",       "R2",       "R3",
                    "R4",       "R5",       "R6",       "R7",
                    "R8",       "R9",       "R10",      "R11",
                    "R12",      "SP",       "LR",              /* alternate names */

                    "PSR",      "PSP",      "MSP"
            }
    };

    public final static int DEDICATED_REG_OFFSET =   15;

    public final static int SP = 13;
    public final static int LR = 14;
    public final static int PSR = 15;
    public final static int PSP = 16;
    public final static int MSP = 17;

    public final static int NUM_STD_REGISTERS = MSP + 1;

    /* bits of the PSR register (APSR part) */
    public int N=0;
    public int Z=0;
    public int C=0;
    public int V=0;
    public int Q=0;

    /* bits of the PSR register (IPSR part) */
    private int ISR=0;

    /* bits of the PSR register (EPSR part) */
    private int EPSR=0;


    public static String[] registerLabels;

    /**
     * Init with default names upon class loading
     */
    static {
        registerLabels = new String[REG_LABEL[0].length];
        System.arraycopy(REG_LABEL[0], 0, registerLabels, 0, REG_LABEL[0].length);
    }

    public static void initRegisterLabels(Set<OutputOption> outputOptions) {
        // Patch names if requested
        if (outputOptions.contains(OutputOption.REGISTER)) {
            System.arraycopy(REG_LABEL[1], 0, registerLabels, 0, REG_LABEL[1].length);
        }
        else {
            System.arraycopy(REG_LABEL[0], 0, registerLabels, 0, REG_LABEL[0].length);
        }
    }

    /**
     * Constructor
     */
    public ArmCPUState() {
        reset();
    }

    /**
     * Constructor
     * @param startPc initial value for the Program Counter
     */
    public ArmCPUState(int startPc) {
        reset();
        pc = startPc;
    }

    @Override
    public int getResetAddress() {
        return RESET_ADDRESS;
    }

    @Override
    public void applyRegisterChanges(CPUState newCpuStateValues, CPUState newCpuStateFlags) {
        if (newCpuStateFlags.pc != 0) {
            pc = newCpuStateValues.pc;
        }
        for (int i = 0; i <= ArmCPUState.NUM_STD_REGISTERS; i++) {
            if (newCpuStateFlags.getReg(i) != 0) {
                setReg(i, newCpuStateValues.getReg(i));
            }
        }
    }

    @Override
    public boolean hasAllRegistersZero() {
        if (pc != 0) return false;
        for (int i = 0; i <= ArmCPUState.NUM_STD_REGISTERS; i++) {
            if (getReg(i) != 0) {
                return false;
            }
        }
        return getAPSR() == 0 && getEPSR() == 0 && getIPSR() == 0;
    }

    @Override
    public final int getNumStdRegisters() {
        return NUM_STD_REGISTERS;
    }

    public final int getAPSR() {
        return (N << 4) | (Z << 3) | (C << 2) | (V << 1) | Q;
    }

    public final void setAPSR(int ccr) {
        N = (ccr >>4) & 0x01;
        Z = (ccr >>3) & 0x01;
        C = (ccr >>2) & 0x01;
        V = (ccr >>1) & 0x01;
        Q = ccr & 0x01;
    }

    public void setThreadMode(int t) {
        if (t == 0) {
            regValue[SP] = regValue[MSP];
        }
        else {
            regValue[SP] = regValue[PSP];
        }
    }

    public int getEPSR() {
        return EPSR;
    }

    public void setEPSR(int epsr) {
        EPSR = epsr;
    }


    public final int getIPSR() {
        return ISR;
    }

    public void setIPSR(int isr) {
        ISR = isr;
    }


    public final int getPSR() {
         return (getAPSR() << 27) | (getEPSR() << 8) | getIPSR();
    }

    public final void setPSR(int psr) {
        setAPSR((psr >> 27) & 0x1F);
        setEPSR((psr>>9) & 0x3807F);
        setIPSR(psr & 0xFF);
    }

    @Override
    public int getSp() {
        return getReg(SP);
    }

    @Override
    public void reset() {
        regValue = new Register32[registerLabels.length];
        for (int i = 0; i < regValue.length; i++) {
            regValue[i] = new Register32(0);
        }
        regValue[SP] = regValue[MSP];
        regValidityBitmap = 0;
        setPc(RESET_ADDRESS);
        // read new MSP from +0
        // read new PC from +4
        setReg(LR, 0xFFFFFFFF);
    }

    @Override
    public void clear() {
        pc = 0;
        for (int i = 0; i < regValue.length; i++) {
            regValue[i] = new Register32(0);
        }
        regValue[SP] = regValue[MSP];
        regValidityBitmap = 0;
    }


    @Override
    public boolean accepts(InterruptRequest interruptRequest) {
        // TODO
        return false;
    }

    public ArmCPUState createCopy() {
        ArmCPUState cloneCpuState = new ArmCPUState();
        for (int i = 0; i <= regValue.length; i++) {
            cloneCpuState.setReg(i, getReg(i));
        }
        cloneCpuState.regValidityBitmap = regValidityBitmap;
        cloneCpuState.pc = pc;
        return cloneCpuState;
    }


    public String toString() {
        String registers = "";
        for (int i = 0; i < regValue.length; i++) {
            registers += registerLabels[i] + "=0x" + Format.asHex(getReg(i), 8) + "\n";
        }
        registers = registers.trim() + "]";
        return "ArmCPUState : " +
                "pc=0x" + Format.asHex(pc, 8) +
                ", rvalid=0b" + Long.toString(regValidityBitmap, 2) +
                ", reg=" + registers +
                '}';
    }

}
