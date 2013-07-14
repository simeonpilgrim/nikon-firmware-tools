package com.nikonhacker.disassembly.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.Register32;
import com.nikonhacker.disassembly.WriteListenerRegister32;
import com.nikonhacker.emu.CpuPowerModeChangeListener;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.tx.TxInterruptRequest;
import com.nikonhacker.emu.interrupt.tx.Type;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnusedDeclaration")
public class TxCPUState extends CPUState {

    public static final int RESET_ADDRESS = 0xBFC00000;

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

    public final static int AT = 1;
    public final static int V0 = 2;
    public final static int V1 = 3;
    public final static int A0 = 4;
    public final static int A1 = 5;
    public final static int A2 = 6;
    public final static int A3 = 7;
    public final static int T0 = 8;
    public final static int T1 = 9;
    public final static int T2 = 10;
    public final static int T3 = 11;
    public final static int T4 = 12;
    public final static int T5 = 13;
    public final static int T6 = 14;
    public final static int T7 = 15;
    public final static int S0 = 16;
    public final static int S1 = 17;
    public final static int S2 = 18;
    public final static int S3 = 19;
    public final static int S4 = 20;
    public final static int S5 = 21;
    public final static int S6 = 22;
    public final static int S7 = 23;
    public final static int T8 = 24;
    public final static int T9 = 25;
    public final static int K0 = 26;
    public final static int K1 = 27;
    public final static int GP = 28;
    public final static int SP = 29;
    public final static int FP = 30;
    public final static int RA = 31;

    public final static int HI = 32;
    public final static int LO = 33;

    // CP0

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

    // CP1

    public final static int CP1_F0 = 51;

    public final static int FIR = 67;
    public final static int FCCR = 68;
    public final static int FEXR = 69;
    public final static int FENR = 70;
    public final static int FCSR = 71;


    // CP0 register fields
    // Status
    public final static int Status_CU_pos       = 28;
    public final static int Status_CU_mask      = 0b11110000_00000000_00000000_00000000;
    public final static int Status_RP_pos       = 27;
    public final static int Status_FR_pos       = 26;
    public final static int Status_RE_pos       = 25;
    public final static int Status_MX_pos       = 24;
    public final static int Status_PX_pos       = 23;
    public final static int Status_BEV_pos      = 22;
    public final static int Status_NMI_pos      = 19;
    public final static int Status_Impl_pos     = 16;
    public final static int Status_Impl_mask    = 0b00000000_00000011_00000000_00000000;
    public final static int Status_IM_pos       = 8;
    public final static int Status_IM_mask      = 0b00000000_00000000_11111111_00000000;
    public final static int Status_KX_pos       = 7;
    public final static int Status_SX_pos       = 6;
    public final static int Status_UX_pos       = 5;
    public final static int Status_UM_pos       = 4;
    public final static int Status_R0_pos       = 3;
    public final static int Status_ERL_pos      = 2;
    public final static int Status_EXL_pos      = 1;
    public final static int Status_IE_pos       = 0;
    // Cause
    public final static int Cause_BD_pos        = 31;
    public final static int Cause_CE_pos        = 28;
    public final static int Cause_CE_mask       = 0b00110000_00000000_00000000_00000000;
    public final static int Cause_IV_pos        = 23;
    public final static int Cause_WP_pos        = 22;
    public final static int Cause_IP_pos        = 8;
    public final static int Cause_IP_mask       = 0b00000000_00000000_11111111_00000000;
    public final static int Cause_ExcCode_pos   = 2;
    public final static int Cause_ExcCode_mask  = 0b00000000_00000000_00000000_01111100;
    // SSCR
    public final static int Sscr_SSD_pos        = 31;
    public final static int Sscr_PSS_pos        = 8;
    public final static int Sscr_PSS_mask       = 0b00000000_00000000_00001111_00000000;
    public final static int Sscr_CSS_pos        = 0;
    public final static int Sscr_CSS_mask       = 0b00000000_00000000_00000000_00000111;

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

    /**
     * In 16-bit ISA mode, only 8 registers (3 bits) are available in generic instructions
     * Here is the mapping of those 3-bit values to the actual register numbers
     */
    static final int[] REGISTER_MAP_16B = new int[] {
            16, 17, 2, 3, 4, 5, 6, 7
    };

    static final int[] XIMM3_MAP = new int[] {
            +1, +2, +4, +8, -1, -2, -4, -8
    };

    public static String[] registerLabels;
    private List<CpuPowerModeChangeListener> cpuPowerModeChangeListeners = new ArrayList<CpuPowerModeChangeListener>();

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

    public enum PowerMode {
        RUN,
        HALT,
        DOZE
    }

    // Fields

    private Register32[][] shadowRegisterSets;

    private PowerMode powerMode;

    public boolean is16bitIsaMode = false;

    TxInterruptController interruptController = null;

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

    @Override
    public int getResetAddress() {
        return RESET_ADDRESS;
    }

    /**
     * Retrieves the PC value as defined by the specification, including the ISA mode as LSB.
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


    @Override
    public int getSp() {
        return getReg(SP);
    }

    public int getShadowReg(int registerSet, int registerNumber) {
        return shadowRegisterSets[registerSet][registerNumber].getValue();
    }

    public void reset() {
        powerMode = PowerMode.RUN;
        shadowRegisterSets = new Register32[8][registerLabels.length];
        regValue = shadowRegisterSets[0];

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

        // patch exceptions: r26-27-28 are common to all sets
        for (int registerSet = 1; registerSet < 8; registerSet++) {
            //noinspection ManualArrayCopy
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

        // Finally, create special registers with listeners:

        // Status register (in all register sets) is special because it can trigger a software interrupt
        shadowRegisterSets[0][Status] = new WriteListenerRegister32(new WriteListenerRegister32.WriteListener() {
            @Override
            public void afterWrite(int newValue) {
                checkSoftwareInterruptGeneration();
            }
        });

        // Cause register (in all register sets) is special because it can trigger a software interrupt
        shadowRegisterSets[0][Cause] = new WriteListenerRegister32(new WriteListenerRegister32.WriteListener() {
            @Override
            public void afterWrite(int newValue) {
                checkSoftwareInterruptGeneration();
            }
        });

        // IER register (in all register sets) is special because it toggles the IE bit
        shadowRegisterSets[0][IER] = new WriteListenerRegister32(new WriteListenerRegister32.WriteListener() {
            @Override
            public void afterWrite(int newValue) {
                if (newValue == 0) {
                    clearStatusIE();
                }
                else {
                    setStatusIE();
                }
            }
        });

        // SSCR register (in all register sets) is special because it switches the current register set
        shadowRegisterSets[0][SSCR] = new WriteListenerRegister32(new WriteListenerRegister32.WriteListener() {
            @Override
            public void afterWrite(int newValue) {
                if (isSscrSSDSet()) {
                    /* SSD = Shadow Set Disable.
                     * When the SSD bit is set, the Shadow Register Set is not updated by any interruptions,
                     * only shadow set 0 is accessible, and the value of the CSS field is ignored.
                     */
                    regValue = shadowRegisterSets[0];
                }
                else {
                    /* Otherwise, switch to corresponding register set. */
                    regValue = shadowRegisterSets[newValue & Sscr_CSS_mask];
                }
            }
        });

        // And point to them in all register sets
        for (int registerSet = 1; registerSet < 8; registerSet++) {
            shadowRegisterSets[registerSet][Status] = shadowRegisterSets[0][Status];
            shadowRegisterSets[registerSet][Cause] = shadowRegisterSets[0][Cause];
            shadowRegisterSets[registerSet][IER] = shadowRegisterSets[0][IER];
            shadowRegisterSets[registerSet][SSCR] = shadowRegisterSets[0][SSCR];
        }

        regValidityBitmap = 0;

        setStatusBEV();
        setStatusERL();
        setReg(PRId, 0x00074000);
        setSscrSSD();
        setReg(Debug, 0x00010000);
        setPc(RESET_ADDRESS);
    }

    @Override
    public void clear() {
        for (int registerSet = 0; registerSet < 8; registerSet++) {
            regValue = shadowRegisterSets[registerSet];
            for (int i = 0; i < regValue.length; i++) {
                setReg(i, 0);
            }
        }
        regValue = shadowRegisterSets[0];

        regValidityBitmap = 0;
    }


    public TxCPUState createCopy() {
        TxCPUState cloneCpuState = new TxCPUState();
        for (int i = 0; i < regValue.length; i++) {
            cloneCpuState.regValue[i] = new Register32(regValue[i].getValue());
        }
        cloneCpuState.regValidityBitmap = regValidityBitmap;
        cloneCpuState.pc = pc;
        return cloneCpuState;
    }


    public PowerMode getPowerMode() {
        return powerMode;
    }

    public void setPowerMode(PowerMode powerMode) {
        // TODO actually halt or restart processor
        for (CpuPowerModeChangeListener cpuPowerModeChangeListener : cpuPowerModeChangeListeners) {
            cpuPowerModeChangeListener.onCpuPowerModeChange(powerMode);
        }
        this.powerMode = powerMode;
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

    public int getCp1CrReg(int regNumber) {
        switch(regNumber) {
            case FIR:
                //       0000Impl06LW3PDSProcesidRevision
                return 0b00000000_00010001_00000000_00000000;
            case FCSR:
                return getReg(FCSR);
            case FCCR:
                return ((getReg(FCSR) & 0b11111110_00000000_00000000_00000000) >> 24) | ((getReg(FCSR) & 0b00000000_10000000_00000000_00000000) >> 23);
            case FEXR:
                return getReg(FCSR)   & 0b00000000_00000011_11110000_01111100;
            case FENR:
                return (getReg(FCSR)  & 0b00000000_00000000_00001111_10000011) | ((getReg(FCSR) & 0b00000001_00000000_00000000_00000000) >> 22);
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
                setReg(FCSR, (getReg(FCSR) & 0b00000001_01111111_11111111_11111111) | ((value & 0b11111110) << 24) | ((value & 0b1) << 23));
                break;
            case FEXR:
                setReg(FCSR, (getReg(FCSR) & 0b11111111_11111100_00001111_10000011) | (value & 0b00000000_00000011_11110000_01111100));
                break;
            case FENR:
                setReg(FCSR, (getReg(FCSR) & 0b11111111_11111111_11110000_01111000) | (value & 0b00000000_00000000_00001111_10000011) | ((value & 0b100) << 22));
                break;
            default:
                throw new RuntimeException("Unknown CP1 register number " + regNumber);
        }
    }

    // -------------------------  FIELD ACCESSORS  ---------------------------

    // Status

    public int getStatusCU() {
        return (getReg(Status) & Status_CU_mask) >>> Status_CU_pos;
    }

    public void setStatusCU(int statusCU) {
        setReg(Status, (getReg(Status) & ~Status_CU_mask) | (statusCU << Status_CU_pos));
    }


    public boolean isStatusRPSet() {
        return Format.isBitSet(getReg(Status), Status_RP_pos);
    }

    public void setStatusRP() {
        setReg(Status, Format.setBit(getReg(Status), Status_RP_pos));
    }

    public void clearStatusRP() {
        setReg(Status, Format.clearBit(getReg(Status), Status_RP_pos));
    }


    public boolean isStatusFRSet() {
        return Format.isBitSet(getReg(Status), Status_FR_pos);
    }

    public void setStatusFR() {
        setReg(Status, Format.setBit(getReg(Status), Status_FR_pos));
    }

    public void clearStatusFR() {
        setReg(Status, Format.clearBit(getReg(Status), Status_FR_pos));
    }


    public boolean isStatusRESet() {
        return Format.isBitSet(getReg(Status), Status_RE_pos);
    }

    public void setStatusRE() {
        setReg(Status, Format.setBit(getReg(Status), Status_RE_pos));
    }

    public void clearStatusRE() {
        setReg(Status, Format.clearBit(getReg(Status), Status_RE_pos));
    }


    public boolean isStatusMXSet() {
        return Format.isBitSet(getReg(Status), Status_MX_pos);
    }

    public void setStatusMX() {
        setReg(Status, Format.setBit(getReg(Status), Status_MX_pos));
    }

    public void clearStatusMX() {
        setReg(Status, Format.clearBit(getReg(Status), Status_MX_pos));
    }


    public boolean isStatusPXSet() {
        return Format.isBitSet(getReg(Status), Status_PX_pos);
    }

    public void setStatusPX() {
        setReg(Status, Format.setBit(getReg(Status), Status_PX_pos));
    }

    public void clearStatusPX() {
        setReg(Status, Format.clearBit(getReg(Status), Status_PX_pos));
    }


    public boolean isStatusBEVSet() {
        return Format.isBitSet(getReg(Status), Status_BEV_pos);
    }

    public void setStatusBEV() {
        setReg(Status, Format.setBit(getReg(Status), Status_BEV_pos));
    }

    public void clearStatusBEV() {
        setReg(Status, Format.clearBit(getReg(Status), Status_BEV_pos));
    }


    public boolean isStatusNMISet() {
        return Format.isBitSet(getReg(Status), Status_NMI_pos);
    }

    public void setStatusNMI() {
        setReg(Status, Format.setBit(getReg(Status), Status_NMI_pos));
    }

    public void clearStatusNMI() {
        setReg(Status, Format.clearBit(getReg(Status), Status_NMI_pos));
    }


    public int getStatusImpl() {
        return (getReg(Status) & Status_Impl_mask) >>> Status_Impl_pos;
    }

    public void setStatusImpl(int statusImpl) {
        setReg(Status, (getReg(Status) & ~Status_Impl_mask) | (statusImpl << Status_Impl_pos));
    }


    public int getStatusIM() {
        return (getReg(Status) & Status_IM_mask) >>> Status_IM_pos;
    }

    public void setStatusIM(int statusIM) {
        setReg(Status, (getReg(Status) & ~Status_IM_mask) | (statusIM << Status_IM_pos));
    }


    public boolean isStatusKXSet() {
        return Format.isBitSet(getReg(Status), Status_KX_pos);
    }

    public void setStatusKX() {
        setReg(Status, Format.setBit(getReg(Status), Status_KX_pos));
    }

    public void clearStatusKX() {
        setReg(Status, Format.clearBit(getReg(Status), Status_KX_pos));
    }


    public boolean isStatusSXSet() {
        return Format.isBitSet(getReg(Status), Status_SX_pos);
    }

    public void setStatusSX() {
        setReg(Status, Format.setBit(getReg(Status), Status_SX_pos));
    }

    public void clearStatusSX() {
        setReg(Status, Format.clearBit(getReg(Status), Status_SX_pos));
    }


    public boolean isStatusUXSet() {
        return Format.isBitSet(getReg(Status), Status_UX_pos);
    }

    public void setStatusUX() {
        setReg(Status, Format.setBit(getReg(Status), Status_UX_pos));
    }

    public void clearStatusUX() {
        setReg(Status, Format.clearBit(getReg(Status), Status_UX_pos));
    }


    public boolean isStatusUMSet() {
        return Format.isBitSet(getReg(Status), Status_UM_pos);
    }

    public void setStatusUM() {
        setReg(Status, Format.setBit(getReg(Status), Status_UM_pos));
    }

    public void clearStatusUM() {
        setReg(Status, Format.clearBit(getReg(Status), Status_UM_pos));
    }


    public boolean isStatusR0Set() {
        return Format.isBitSet(getReg(Status), Status_R0_pos);
    }

    public void setStatusR0() {
        setReg(Status, Format.setBit(getReg(Status), Status_R0_pos));
    }

    public void clearStatusR0() {
        setReg(Status, Format.clearBit(getReg(Status), Status_R0_pos));
    }


    public boolean isStatusERLSet() {
        return Format.isBitSet(getReg(Status), Status_ERL_pos);
    }

    public void setStatusERL() {
        setReg(Status, Format.setBit(getReg(Status), Status_ERL_pos));
    }

    public void clearStatusERL() {
        setReg(Status, Format.clearBit(getReg(Status), Status_ERL_pos));
    }


    public boolean isStatusEXLSet() {
        return Format.isBitSet(getReg(Status), Status_EXL_pos);
    }

    public void setStatusEXL() {
        setReg(Status, Format.setBit(getReg(Status), Status_EXL_pos));
    }

    public void clearStatusEXL() {
        setReg(Status, Format.clearBit(getReg(Status), Status_EXL_pos));
    }


    public boolean isStatusIESet() {
        return Format.isBitSet(getReg(Status), Status_IE_pos);
    }

    public void setStatusIE() {
        setReg(Status, Format.setBit(getReg(Status), Status_IE_pos));
    }

    public void clearStatusIE() {
        setReg(Status, Format.clearBit(getReg(Status), Status_IE_pos));
    }


    // Cause

    public boolean isCauseBDSet() {
        return Format.isBitSet(getReg(Cause), Cause_BD_pos);
    }

    public void setCauseBD() {
        setReg(Cause, Format.setBit(getReg(Cause), Cause_BD_pos));
    }

    public void clearCauseBD() {
        setReg(Cause, Format.clearBit(getReg(Cause), Cause_BD_pos));
    }


    public int getCauseCE() {
        return (getReg(Cause) & Cause_CE_mask) >>> Cause_CE_pos;
    }

    public void setCauseCE(int statusCE) {
        setReg(Cause, (getReg(Cause) & ~Cause_CE_mask) | (statusCE << Cause_CE_pos));
    }


    public boolean isCauseIVSet() {
        return Format.isBitSet(getReg(Cause), Cause_IV_pos);
    }

    public void setCauseIV() {
        setReg(Cause, Format.setBit(getReg(Cause), Cause_IV_pos));
    }

    public void clearCauseIV() {
        setReg(Cause, Format.clearBit(getReg(Cause), Cause_IV_pos));
    }


    public boolean isCauseWPSet() {
        return Format.isBitSet(getReg(Cause), Cause_WP_pos);
    }

    public void setCauseWP() {
        setReg(Cause, Format.setBit(getReg(Cause), Cause_WP_pos));
    }

    public void clearCauseWP() {
        setReg(Cause, Format.clearBit(getReg(Cause), Cause_WP_pos));
    }


    public int getCauseIP() {
        return (getReg(Cause) & Cause_IP_mask) >>> Cause_IP_pos;
    }

    public void setCauseIP(int statusIP) {
        setReg(Cause, (getReg(Cause) & ~Cause_IP_mask) | (statusIP << Cause_IP_pos));
    }


    public int getCauseExcCode() {
        return (getReg(Cause) & Cause_ExcCode_mask) >>> Cause_ExcCode_pos;
    }

    public void setCauseExcCode(int statusExcCode) {
        setReg(Cause, (getReg(Cause) & ~Cause_ExcCode_mask) | (statusExcCode << Cause_ExcCode_pos));
    }


    // SSCR
    /** Retrieves the SSD (Shadow Set Disable) bit of SSCR.*/
    public boolean isSscrSSDSet() {
        return Format.isBitSet(getReg(SSCR), Sscr_SSD_pos);
    }

    /** This method sets the SSD bit of SSCR.
     * This will result in switching to register set 0 via the WriteListener on this register
     */
    public void setSscrSSD() {
        setReg(SSCR, Format.setBit(getReg(SSCR), Sscr_SSD_pos));
    }

    /** This method clears the SSD bit of SSCR.
     * This will result in switching to register set according to current CSS value via the WriteListener on this register
     */
    public void clearSscrSSD() {
        setReg(SSCR, Format.clearBit(getReg(SSCR), Sscr_SSD_pos));
    }


    /** This method returns the previous shadow register set */
    public int getSscrPSS() {
        return (getReg(SSCR) & Sscr_PSS_mask) >>> Sscr_PSS_pos;
    }

    /** This method sets the previous shadow register set */
    public void setSscrPSS(int statusPSS) {
        setReg(SSCR, (getReg(SSCR) & ~Sscr_PSS_mask) | (statusPSS << Sscr_PSS_pos));
    }


    /** This method returns the current shadow register set */
    public int getSscrCSS() {
        return (getReg(SSCR) & Sscr_CSS_mask) /*>>> Sscr_CSS_pos = 0*/;
    }

    /** This method sets the CSS field of SSCR.
     * This will result in switching to the corresponding register set via the WriteListener on this register
     */
    public void setSscrCSS(int css) {
        setReg(SSCR, (getReg(SSCR) & ~Sscr_CSS_mask) | (css /*<< Sscr_CSS_pos = 0*/));
    }

    /** This method moves CSS to PSS and switches to the given CSS */
    public void pushSscrCssIfSwitchingEnabled(int css) {
        if (!isSscrSSDSet()) {
            setSscrPSS(getSscrCSS());
            setSscrCSS(css);
        }
    }

    /** This method moves back CSS from PSS and switches back to the old CSS */
    public void popSscrCssIfSwitchingEnabled() {
        if (!isSscrSSDSet()) {
            setSscrCSS(getSscrPSS());
        }
    }


    // -------------------------  Interrupt related methods  ---------------------------

    /**
     * This is needed because setting CPU registers can cause a software interrupt
     * @param interruptController
     */
    public void setInterruptController(TxInterruptController interruptController) {
        this.interruptController = interruptController;
    }
    /**
     * if Status<IM>[1:0] == 1 and Cause<IP>[1:0] and Status<IE> == 1, generate a software interrupt request
     */
    private void checkSoftwareInterruptGeneration() {
        if (((getStatusIM() & 0b11) == 1) && ((getCauseIP() & 0b11) == 1) & (isStatusIESet())) {
            if (interruptController != null) {
                interruptController.request(new TxInterruptRequest(Type.SOFTWARE_INTERRUPT));
            }
        }
    }

    @Override
    public boolean accepts(InterruptRequest interruptRequest) {
        if (interruptController == null) {
            System.out.println("TxCPUState.accepts() called while no InterruptController was defined");
            return false;
        }
        // Note: this could be optimized by directly masking IE/ERL/EXL here instead of using methods that call a method that does bitshifting
        return  getPowerMode() == PowerMode.RUN
                && isStatusIESet() // TBC IE is a filter for acceptance, not generation
                && !isStatusERLSet()
                && !isStatusEXLSet()
                // Cfr last paragraph of section 6.5.1.6 :
                // "7" is the highest priority level.
                // 000 means all interrupts enabled.
                && ((TxInterruptRequest)interruptRequest).getLevel() > interruptController.getIlevCmask();
    }

    public void addCpuPowerModeChangeListener(CpuPowerModeChangeListener cpuPowerModeChangeListener) {
        cpuPowerModeChangeListeners.add(cpuPowerModeChangeListener);
    }


    public String toString() {
        String registers = "";
        for (int i = 0; i < regValue.length; i++) {
            registers += registerLabels[i] + "=0x" + Format.asHex(getReg(i), 8) + "\n";
        }
        registers = registers.trim() + "]";
        return "TxCPUState : " +
                "pc=0x" + Format.asHex(pc, 8) +
                ", rvalid=0b" + Long.toString(regValidityBitmap, 2) +
                ", reg=" + registers +
                '}';
    }

}
