package com.nikonhacker.disassembly.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.Register32;

import java.util.Set;

public class TxCPUState extends CPUState {
    /** Register names (first array is "standard", second array is "alternate") */
    final static String[][] REG_LABEL = new String[][]{
            {
                    /* standard registers names (default) */
                    "r0",       "r1",       "r2",       "r3",
                    "r4",       "r5",       "r6",       "r7",
                    "r8",       "r9",       "r10",      "r11",
                    "r12",      "r13",      "r14",      "r15",

                    "r16",      "r17",      "r18",      "r19",
                    "r20",      "r21",      "r22",      "r23",
                    "r24",      "r25",      "r26",      "r27",
                    "r28",      "r29",      "r30",      "r31",

                    "hi",       "lo",

                    // CP0 Registers
                    "Config",   "Config1", "Config2",   "Config3",
                    "BadVAddr", "Count",   "Compare",   "Status",
                    "Cause",    "EPC",     "ErrorEPC",  "PRId",
                    "IER",      "SSCR",    "Debug",     "DEPC",
                    "DESAVE",

                    // CP1 Registers
                    "$f0",  "$f1",  "$f2",  "$f3",
                    "$f4",  "$f5",  "$f6",  "$f7",
                    "$f8",  "$f9",  "$f10", "$f11",
                    "$f12", "$f13", "$f14", "$f15",
                    "$f16", "$f17", "$f18", "$f19",
                    "$f20", "$f21", "$f22", "$f23",
                    "$f24", "$f25", "$f26", "$f27",
                    "$f28", "$f29", "$f30", "$f31",

                    // CP1 Control Registers
                    "FIR", "FCCR", "FEXR", "FENR", "FCSR"
            },
            {
                    /* alternate registers names */
                    "$zero",    "$at",      "$v0",      "$v1",
                    "$a0",      "$a1",      "$a2",      "$a3",
                    "$t0",      "$t1",      "$t2",      "$t3",
                    "$t4",      "$t5",      "$t6",      "$t7",

                    "$s0",      "$s1",      "$s2",      "$s3",
                    "$s4",      "$s5",      "$s6",      "$s7",
                    "$t8",      "$t9",      "$k0",      "$k1",
                    "$gp",      "$sp",      "$fp",      "$ra",

                    "hi",       "lo",

                    // CP0 Registers
                    "Config",   "Config1", "Config2",   "Config3",
                    "BadVAddr", "Count",   "Compare",   "Status",
                    "Cause",    "EPC",     "ErrorEPC",  "PRId",
                    "IER",      "SSCR",    "Debug",     "DEPC",
                    "DESAVE",

                    // CP1 Registers
                    "$f0",  "$f1",  "$f2",  "$f3",
                    "$f4",  "$f5",  "$f6",  "$f7",
                    "$f8",  "$f9",  "$f10", "$f11",
                    "$f12", "$f13", "$f14", "$f15",
                    "$f16", "$f17", "$f18", "$f19",
                    "$f20", "$f21", "$f22", "$f23",
                    "$f24", "$f25", "$f26", "$f27",
                    "$f28", "$f29", "$f30", "$f31",

                    // CP1 Control Registers
                    "FIR", "FCCR", "FEXR", "FENR", "FCSR"
            }
    };

    public final static int S0 = 16;
    public final static int S1 = 17;
    public final static int T8 = 24;
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

    public final static int CP1_F0 = 51;

    public final static int FIR = 67;
    public final static int FCCR = 68;
    public final static int FEXR = 69;
    public final static int FENR = 70;
    public final static int FCSR = 71;

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

    /** This array is used to decode the mfc0 and mtc0 instruction operands
     * Array is indexed by [SEL][number] and returns a register index as defined in TxCPUState
     */
    static final int[][] CP0_REGISTER_MAP = new int[][]{
            // SEL0
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    BadVAddr, Count, -1, Compare, Status, Cause, EPC, PRId,
                    Config, -1, -1, -1, -1, -1, SSCR, Debug,
                    DEPC, -1, -1, -1, -1, -1, ErrorEPC, DESAVE
            },
            // SEL1
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    Config1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            },
            // SEL2
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    Config2, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            },
            // SEL3
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    Config3, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            },
            // SEL4
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            },
            // SEL5
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            },
            // SEL6
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, SSCR, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            },
            // SEL7
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, IER, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            }
    };

    // TODO cp0rs32 - unknown mapping ! Putting dummy values in to test
    static final int[] CP0_REGISTER_MAP_16B = new int[]{
             0, 1, 2, 3, 4, 5, 6, 7,
             8, 9,10,11,12,13,14,15,
            16,17,18,19,20,21,22,23,
            24,25,26,27,28,29,30,31
    };

    static final int[] CP1_REGISTER_NUMBER_MAP = new int[]{
            FIR, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, FCCR, FEXR, -1, FENR, -1, -1, FCSR
    };

    static final int[] REGISTER_MAP_16B = new int[] {
            16, 17, 2, 3, 4, 5, 6, 7
    };

    static final int[] XIMM3_MAP = new int[] {
            +1, +2, +4, +8, -1, -2, -4, -8
    };

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


    public int getCp1CrReg(int regNumber) {
        switch(regNumber) {
            case FIR:
                //       0000Impl06LW3PDSProcesidRevision
                return 0b00000000000100010000000000000000;
            case FCSR:
                return getReg(FCSR);
            case FCCR:
                return ((getReg(FCSR) & 0b11111110000000000000000000000000) >> 24) | ((getReg(FCSR) & 0b00000000100000000000000000000000) >> 23);
            case FEXR:
                return getReg(FCSR) & 0b00000000000000111111000001111100;
            case FENR:
                return (getReg(FCSR) & 0b00000000000000000000111110000011) | ((getReg(FCSR) & 0b00000001000000000000000000000000) >> 22);
        }
        throw new RuntimeException("Unknown CP1 register number " + regNumber);
    }

    public void setCp1CrReg(int regNumber, int value) {
        switch(regNumber) {
            case FIR:
                throw new RuntimeException("Cannot write to read-only CP1 Control register FIR");
            case FCSR:
                setReg(FCSR, value);
                break;
            case FCCR:
                setReg(FCSR, (getReg(FCSR) & 0b00000001011111111111111111111111) | ((value & 0b11111110) << 24) | ((value & 0b1) << 23));
                break;
            case FEXR:
                setReg(FCSR, (getReg(FCSR) & 0b11111111111111000000111110000011) | (value & 0b00000000000000111111000001111100));
                break;
            case FENR:
                setReg(FCSR, (getReg(FCSR) & 0b11111111111111111111000001111000) | (value & 0b00000000000000000000111110000011) | ((value & 0b100) << 22));
                break;
            default:
                throw new RuntimeException("Unknown CP1 register number " + regNumber);
        }
    }

    public enum PowerMode {
        RUN,
        HALT,
        DOZE
    }


    private Register32[][] shadowRegisterSets;

    private int activeRegisterSet;

    private PowerMode powerMode;

    public boolean is16bitIsaMode = false;

    // The 8 condition flags will be stored in bits 0-7 for flags 0-7.
    private Register32 cp1Condition = new Register32(0);
    private int numCp1ConditionFlags = 8;


    /**
     * Constructor
     */
    public TxCPUState() {
        reset();
    }

    /**
     * Constructor
     * @param startPc initial value for the Program Counter, including ISA mode as LSB
     */
    public TxCPUState(int startPc) {
        reset();
        setPc(startPc);
    }

    /**
     * Retrieves the PC value as defined by the specification, including the ISA mode as LSB
     * Technically, this combines the pc (address) int field and the is16bitIsaMode boolean field
     * @return
     */
    public int getPc() {
        return pc | (is16bitIsaMode?1:0);
    }

    /**
     * Sets the PC value as defined by the specification, including the ISA mode as LSB
     * Technically, the value is split between the pc (address) int field and the is16bitIsaMode boolean field
     * @return
     */
    public void setPc(int pc) {
        this.is16bitIsaMode = ((pc & 1) == 1);
        this.pc = pc & 0xFFFFFFFE;
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

    public String toString() {
        String registers = "";
        for (int i = 0; i < regValue.length; i++) {
            registers += registerLabels[i] + "=0x" + Format.asHex(getReg(i), 8) + "\n";
        }
        registers = registers.trim() + "]";
        return "CPUState : " +
                "pc=0x" + Format.asHex(pc, 8) +
                ", rvalid=0b" + Long.toString(regValidityBitmap, 2) +
                ", reg=" + registers +
                '}';
    }

    public void reset() {
        shadowRegisterSets = new Register32[8][registerLabels.length];
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
        for (int i = 0; i < regValue.length; i++) {
            cloneCpuState.regValue[i] = new Register32(regValue[i].getValue());
        }
        cloneCpuState.regValidityBitmap = regValidityBitmap;
        cloneCpuState.pc = pc;
        return cloneCpuState;
    }


    /**
     *  Sets the value of the FPU register given to the value given.
     *   @param reg Register to set the value of.
     *   @param val The desired float value for the register.
     **/
    public void setRegisterToFloat(int reg, float val){
        regValue[reg].setValue(Float.floatToRawIntBits(val));
    }

    /**
     *  Sets the value of the FPU register given to the 32-bit
     *  pattern given by the int parameter.
     *   @param reg Register to set the value of.
     *   @param val The desired int bit pattern for the register.
     **/
    public void setRegisterToInt(int reg, int val){
        regValue[reg].setValue(val);
    }

    /**
     *  Sets the value of the FPU register given to the double value given.  The register
     *  must be even-numbered, and the low order 32 bits are placed in it.  The high order
     *  32 bits are placed in the (odd numbered) register that follows it.
     *   @param reg Register to set the value of.
     *   @param val The desired double value for the register.
     *   @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public void setRegisterPairToDouble(int reg, double val) throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        long bits = Double.doubleToRawLongBits(val);
        regValue[reg+1].setValue(Format.highOrderLongToInt(bits));  // high order 32 bits
        regValue[reg].setValue(Format.lowOrderLongToInt(bits)); // low order 32 bits
    }

    /**
     *  Sets the value of the FPU register pair given to the long value containing 64 bit pattern
     *  given.  The register
     *  must be even-numbered, and the low order 32 bits from the long are placed in it.  The high order
     *  32 bits from the long are placed in the (odd numbered) register that follows it.
     *   @param reg Register to set the value of.  Must be even register of even/odd pair.
     *   @param val The desired double value for the register.
     *   @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public void setRegisterPairToLong(int reg, long val)
            throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        regValue[reg+1].setValue(Format.highOrderLongToInt(val));  // high order 32 bits
        regValue[reg].setValue(Format.lowOrderLongToInt(val)); // low order 32 bits
    }


    /**
     *  Gets the float value stored in the given FPU register.
     *   @param reg Register to get the value of.
     *   @return The  float value stored by that register.
     **/

    public float getFloatFromRegister(int reg){
        return Float.intBitsToFloat(regValue[reg].getValue());
    }

    /**
     *  Gets the double value stored in the given FPU register.  The register
     *  must be even-numbered.
     *   @param reg Register to get the value of. Must be even number of even/odd pair.
     *   @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public double getDoubleFromRegisterPair(int reg) throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        return Double.longBitsToDouble(Format.twoIntsToLong(regValue[reg + 1].getValue(), regValue[reg].getValue()));
    }

    /**
     *  Gets a long representing the double value stored in the given double
     *  precision FPU register.
     *  The register must be even-numbered.
     *   @param reg Register to get the value of. Must be even number of even/odd pair.
     *   @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public long getLongFromRegisterPair(int reg) throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        return Format.twoIntsToLong(regValue[reg + 1].getValue(), regValue[reg].getValue());
    }


    /**
     *  Set condition flag to 1 (true).
     *
     *  @param flag condition flag number (0-7)
     *  @return previous flag setting (0 or 1)
     */
    public void setConditionFlag(int flag) {
        // TODO check
        // cp1Condition.setValue(Format.setBit(cp1Condition.getValue(), flag));
        throw new RuntimeException("Unimplemented");
    }

    /**
     *  Set condition flag to 0 (false).
     *
     *  @param flag condition flag number (0-7)
     *  @return previous flag setting (0 or 1)
     */
    public void clearConditionFlag(int flag) {
        // TODO check
        // cp1Condition.setValue(Format.clearBit(cp1Condition.getValue(), flag));
        throw new RuntimeException("Unimplemented");
    }


    /**
     *  Get value of specified condition flag (0-7).
     *
     *  @param flag condition flag number (0-7)
     *  @return 0 if condition is false, 1 if condition is true
     */
    public int getConditionFlag(int flag) {
        // TODO check
        // return Format.bitValue(cp1Condition.getValue(), flag);
        throw new RuntimeException("Unimplemented");
    }


    /**
     *  Get array of condition flags (0-7).
     *
     *  @return array of int condition flags
     */
    public int getConditionFlags() {
        // TODO check
        // return cp1Condition.getValue();
        throw new RuntimeException("Unimplemented");
    }


    /**
     *  Clear all condition flags (0-7).
     *
     */
    public void clearConditionFlags() {
        // TODO check
        // cp1Condition.setValue(0);  // sets all 32 bits to 0.
        throw new RuntimeException("Unimplemented");
    }

    /**
     *  Set all condition flags (0-7).
     *
     */
    public void setConditionFlags() {
        // TODO check
        // cp1Condition.setValue(-1);  // sets all 32 bits to 1.
        throw new RuntimeException("Unimplemented");
    }

    /**
     *  Get count of condition flags.
     *
     *  @return number of condition flags
     */
    public int getConditionFlagCount() {
        // TODO check
        // return numCp1ConditionFlags;
        throw new RuntimeException("Unimplemented");
    }

}
