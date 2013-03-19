package com.nikonhacker.disassembly.tx;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Set;

/*
 * Statement : an instance of a specific Instruction with specific operands
 */
public class TxStatement extends Statement {
    public static final EnumSet<Instruction.FlowType> BREAK_FLOW_TYPES = EnumSet.of(Instruction.FlowType.JMP, Instruction.FlowType.RET);
    ///* output formatting */
    private static String fmt_nxt;
    private static String fmt_imm;
    private static String fmt_and;
    private static String fmt_inc;
    private static String fmt_dec;
    private static String fmt_mem;
    private static String fmt_par;
    private static String fmt_ens;

    /** rs or fs operand */
    public int rs_fs; // as-is from binary code
    private int decodedRsFs; // interpreted

    /** rt or ft operand */
    public int rt_ft; // as-is from binary code
    private int decodedRtFt; // interpreted

    /** rd of fd operand */
    public int rd_fd; // as-is from binary code
    private int decodedRdFd; // interpreted

    /** sa (shift amount) of cc (CP1 condition code) operand */
    // TODO use imm instead ??
    // TODO what does decodedSa mean ??
    public int sa_cc; // as-is from binary code
    private int decodedSaCc; // interpreted

    /** coprocessor operation (not implemented yet in operand parsing, only for display) */
    private int c;


    /** number of significant bits in imm */
    public int immBitWidth;

    /** number of significant bits in decodedImm */
    private int decodedImmBitWidth;

    /** start of decoded memory block (used only for display in "v"ector format */
    private int memRangeStart = 0;

    private int binaryStatement;
    private int numBytes;

    /**
     * Default decoding upon class loading
     */
    static {
        initFormatChars(EnumSet.noneOf(OutputOption.class));
    }

    public TxStatement() {
        reset();
    }

    public TxStatement(int memRangeStart) {
        this.memRangeStart = memRangeStart;
        reset();
    }

    public int getBinaryStatement() {
        return binaryStatement;
    }

    public void setBinaryStatement(int numBytes, int binaryStatement) {
        this.numBytes = numBytes;
        this.binaryStatement = binaryStatement;
    }

    public static void initFormatChars(Set<OutputOption> outputOptions) {
        fmt_nxt = ",";
        fmt_par = "(";
        fmt_ens = ")";

        if (outputOptions.contains(OutputOption.CSTYLE)) {
            fmt_imm = "";
            fmt_and = "+";
            fmt_inc = "++";
            fmt_dec = "--";
            fmt_mem = "*";
        }
        else {
            fmt_imm = "#";
            fmt_and = ",";
            fmt_inc = "+";
            fmt_dec = "-";
            fmt_mem = "@";
        }
    }



    public void decode32BitOperands() {
        switch (((TxInstruction)instruction).getInstructionFormat32())
        {
            case R:
                rs_fs = (binaryStatement >>> 21) & 0b11111; // rs
                rt_ft = (binaryStatement >>> 16) & 0b11111; // rt
                rd_fd = (binaryStatement >>> 11) & 0b11111; // rd
                sa_cc = (binaryStatement >>>  6) & 0b11111; // sa
                break;
            case I:
            case I_BRANCH:
                rs_fs = (binaryStatement >>> 21) & 0b11111; // rs or base
                rt_ft = (binaryStatement >>> 16) & 0b11111; // rt
                imm   =  binaryStatement         & 0xFFFF;
                immBitWidth = 16;
                break;
            case J:
                imm   =  binaryStatement         & 0x03FFFFFF;
                immBitWidth = 26;
                break;
            case BREAK:
                imm   = (binaryStatement >>>  6) & 0x000FFFFF;
                immBitWidth = 20;
                break;
            case TRAP:
                rs_fs = (binaryStatement >>> 21) & 0b11111; // rs
                rt_ft = (binaryStatement >>> 16) & 0b11111; // rt
                imm   = (binaryStatement >>>  6) & 0x3FF;
                immBitWidth = 10;
                break;
            // IN CPxx formats, rs, rt and rd are mapped or shifted to target the registers belonging to the correct CP
            case CP0:
                rt_ft = (binaryStatement >>> 16) & 0b11111; // rt
                rd_fd = TxCPUState.CP0_REGISTER_MAP[binaryStatement & 0b111][(binaryStatement >>> 11) & 0b11111];
                break;
            case CP1_R1: // Coprocessor 1 with registers, version 1 (rt)
                rt_ft = (binaryStatement >>> 16) & 0b11111; // rt
                rs_fs = TxCPUState.CP1_F0 + ((binaryStatement >>> 11) & 0b11111); //fs
                break;
            case CP1_R2: // Coprocessor 1 with registers, version 2 (ft)
                //fmt   = (binaryStatement >>> 21) & 0b11111;
                rt_ft = TxCPUState.CP1_F0 + ((binaryStatement >>> 16) & 0b11111); // ft
                rs_fs = TxCPUState.CP1_F0 + ((binaryStatement >>> 11) & 0b11111); // fs
                rd_fd = TxCPUState.CP1_F0 + ((binaryStatement >>>  6) & 0b11111); // fd
                break;
            case CP1_CR1: // Coprocessor 1 with Condition Register
                rt_ft = (binaryStatement >>> 16) & 0b11111; // rt
                rs_fs = TxCPUState.CP1_REGISTER_NUMBER_MAP[(binaryStatement >>> 11) & 0b11111];
                break;
            case CP1_CC_BRANCH:
                sa_cc = (binaryStatement >>> 18) & 0b111; // cc
                imm   =  binaryStatement         & 0xFFFF;
                immBitWidth = 16;
                break;
            case CP1_I:
                rs_fs = (binaryStatement >>> 21) & 0b11111;
                rt_ft = TxCPUState.CP1_F0 + ((binaryStatement >>> 16) & 0b11111); // ft
                imm   =  binaryStatement         & 0xFFFF;
                immBitWidth = 16;
                break;
            case CP1_R_CC: // Coprocessor 1 with Register and Condition Code
                //fmt   = (binaryStatement >>> 21) & 0b11111;
                rt_ft = TxCPUState.CP1_F0 + ((binaryStatement >>> 16) & 0b11111); //ft
                rs_fs = TxCPUState.CP1_F0 + ((binaryStatement >>> 11) & 0b11111); //fs
                sa_cc = (binaryStatement >>>  8) & 0b111; //cc
                break;

        }
    }

    public void decode16BitOperands(int pc) {
        if (numBytes == 2) {
            // Not EXTENDed
            switch (((TxInstruction)instruction).getInstructionFormat16())
            {
                case I:
                    imm   =  binaryStatement  & 0b11111111111;
                    immBitWidth = 11;
                    break;
                case RI:
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rs_fs = rt_ft;
                    imm   =  binaryStatement  & 0b11111111;
                    immBitWidth = 8;
                    break;
                case RR:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    rd_fd = rs_fs;
                    break;
                case RRI:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    imm   =  binaryStatement  & 0b11111;
                    rd_fd = rt_ft; // trick to use RRI for MULT, SLLV, etc. to avoid a specific encoding
                    immBitWidth = 5;
                    break;
                case RRR1:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    rd_fd = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 2) & 0b111]; // rz
                    break;
                case RRR2:
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // ry
                    imm = (binaryStatement >>> 2) & 0b11111; // imm or sa
                    immBitWidth = 5;
                    break;
                case RRR3:
                    // TODO Check cp0rt32 -> cp0 reg number mapping
                    rt_ft = TxCPUState.CP0_REGISTER_MAP_16B[(binaryStatement >>> 2) & 0b11111]; // cp0rt32
                    imm   = TxCPUState.XIMM3_MAP[(binaryStatement >>> 8) & 0b111];
                    immBitWidth = 4;
                    break;
                case RRIA:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    imm   =  binaryStatement  & 0b1111;
                    immBitWidth = 4;
                    break;
                case SHIFT1:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    sa_cc = (binaryStatement >>> 2) & 0b111; // sa
                    if (sa_cc == 0) sa_cc = 8; // special convention as a value of 0 is meaningless
                    rd_fd = rs_fs;
                    break;
                case SHIFT2:
                    // TODO Check cp0rt32 -> cp0 reg number mapping
                    rd_fd = TxCPUState.CP0_REGISTER_MAP_16B[(binaryStatement >>> 3) & 0b11111]; // cp0rt32
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111];
                    break;
                case I8MOVFP:
                    rd_fd = TxCPUState.FP;
                    rs_fs = (binaryStatement >>> 5) & 0b11111;
                    break;
                case I8MOVR32:
                    rs_fs = binaryStatement & 0b11111;
                    rd_fd = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111];
                    break;
                case I8MOV32R:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[binaryStatement & 0b111];
                    // r32 bit encoding is --------21043--- . 43 must not move and 210 is shifted right to reorder 43210
                    rd_fd = binaryStatement & 0b11000 | (binaryStatement >> 5) & 0b00111;
                    break;
                case I8SVRS:
                    imm   = binaryStatement & 0b1111; // framesize
                    immBitWidth = 4;
                    sa_cc = (binaryStatement >>> 4) & 0b111; // ra|s0|s1
                    break;
                case FPB_SPB:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = rs_fs; // TODO useful ?
                    imm   =  binaryStatement & 0b1111111;
                    immBitWidth = 7;
                    rd_fd = rt_ft;
                    break;
                case FPH_SPH:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = rs_fs; // TODO useful ?
                    imm   =  (binaryStatement >>> 1) & 0b111111;
                    immBitWidth = 6;
                    rd_fd = rt_ft;
                    break;
                case SPC_BIT:
                    sa_cc = (binaryStatement >>> 5) & 0b111; // pos3
                    rs_fs = 0b11; // base = fp
                    imm   =  binaryStatement  & 0b11111;
                    immBitWidth = 5;
                    break;
                case BREAK:
                    imm   = (binaryStatement >>>  5) & 0b111111;
                    immBitWidth = 6;
                    break;
                case W:
                    break;
                default:
                    throw new RuntimeException("Decoding of format " + ((TxInstruction)instruction).getInstructionFormat16() + " is not implemented at 0x" + Format.asHex(pc, 8));
            }
        }
        else {
            // EXTENDed versions
            switch (((TxInstruction)instruction).getInstructionFormat16())
            {
                case I:
                    imm   =   ((binaryStatement >> (16-11)) & 0b11111000_00000000)
                            | ((binaryStatement >> (21- 5)) & 0b00000111_11100000)
                            | ((binaryStatement           ) & 0b00000000_00011111);
                    immBitWidth = 16;
                    break;
                case RI:
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rs_fs = rt_ft;
                    imm   =   ((binaryStatement >> (16-11)) & 0b11111000_00000000)
                            | ((binaryStatement >> (21- 5)) & 0b00000111_11100000)
                            | ((binaryStatement           ) & 0b00000000_00011111);
                    immBitWidth = 16;
                    break;
                case RRI:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    imm   =   ((binaryStatement >> (16-11)) & 0b11111000_00000000)
                            | ((binaryStatement >> (21- 5)) & 0b00000111_11100000)
                            | ((binaryStatement           ) & 0b00000000_00011111);
                    immBitWidth = 16;
                    break;
                case RRR1:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    rd_fd = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 2) & 0b111]; // rz
                    throw new RuntimeException("RRR1 EXTEND not implemented");
                    //break;
                case RRR3:
                    // TODO Check cp0rt32 -> cp0 reg number mapping
                    rt_ft = TxCPUState.CP0_REGISTER_MAP_16B[(binaryStatement >>> 2) & 0b11111]; // cp0rt32
                    imm   = TxCPUState.XIMM3_MAP[(binaryStatement >>> 8) & 0b111];
                    immBitWidth = 4;
                    break;
                case RRIA:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    imm   =   ((binaryStatement >> (16-11)) & 0b01111000_00000000)
                            | ((binaryStatement >> (20- 4)) & 0b00000111_11110000)
                            | ((binaryStatement           ) & 0b00000000_00001111);
                    immBitWidth = 15;
                    break;
                case SHIFT1:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    sa_cc = (binaryStatement >>> 22) & 0b11111; // sa
                    rd_fd = rs_fs;
                    break;
                case I8SVRS:
                    imm   =   ((binaryStatement >> (20- 4)) & 0b11110000)
                            | ( binaryStatement             & 0b00001111); // framesize
                    immBitWidth = 8;
                    sa_cc =   ((binaryStatement >> (24- 7)) & 0b11_10000000)
                            | ((binaryStatement >> (16- 3)) & 0b00_01111000)
                            | ((binaryStatement >> 4)       & 0b00_00000111); // xsregs|aregs|ra|s0|s1
                    break;
                case FPB_SPB:
                case FPH_SPH:
                    // same as RI in 32bit
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = rs_fs; // TODO useful ?
                    imm   =   ((binaryStatement >> (16-11)) & 0b11111000_00000000)
                            | ((binaryStatement >> (21- 5)) & 0b00000111_11100000)
                            | ((binaryStatement           ) & 0b00000000_00011111);
                    immBitWidth = 16;
                    break;
                case SPC_BIT:
                    sa_cc = (binaryStatement >>> 5) & 0b111; // pos3
                    rs_fs = (binaryStatement >>> 19) & 0b11; // base
                    imm   =   ((binaryStatement >> (16-11)) & 0b00111000_00000000)
                            | ((binaryStatement >> (21- 5)) & 0b00000111_11100000)
                            | ((binaryStatement           ) & 0b00000000_00011111);
                    immBitWidth = 14;
                    break;
                case JAL_JALX:
                    imm   =   ((binaryStatement << (21-16)) & 0b00000011_11100000_00000000_00000000)
                            | ((binaryStatement >> (21-16)) & 0b00000000_00011111_00000000_00000000)
                            | ( binaryStatement             & 0b00000000_00000000_11111111_11111111);
                    immBitWidth = 26;
                    break;
                case RR_BS1F_BFINS:
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // ry
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // rx
                    sa_cc = (binaryStatement >>> 16) & 0b11111; // bit1
                    imm = (binaryStatement >>> 21) & 0b11111; // bit2
                    immBitWidth = 5;
                    break;
                case RR_MIN_MAX:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>>16) & 0b111]; // ry
                    rd_fd = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rz
                    break;
                case W:
                    break;
                default:
                    throw new RuntimeException("EXTENDed decoding of format " + ((TxInstruction)instruction).getInstructionFormat16() + " is not implemented at 0x" + Format.asHex(pc, 8));
            }
        }
    }

    public void reset() {
        immBitWidth = 0;
        c = 0;
        rs_fs = CPUState.NOREG;
        rt_ft = CPUState.NOREG;
        rd_fd = CPUState.NOREG;
        sa_cc = CPUState.NOREG;
        imm = CPUState.NOREG;
        setOperandString(null);
        setCommentString(null);
    }

    /**
     * Disassemble TxInstruction for presentation
     * must be called after decodeOperands()
     *
     * @param context
     * @param updateRegisters if true, cpuState registers will be updated during action interpretation.
     * @see TxInstruction for a description of all possible chars
     */
    public void formatOperandsAndComment(StatementContext context, boolean updateRegisters, Set<OutputOption> outputOptions) throws DisassemblyException {

        /* DISPLAY FORMAT processing */
        decodedRsFs = rs_fs;
        decodedRtFt = rt_ft;
        decodedRdFd = rd_fd;
        decodedSaCc = sa_cc;
        decodedImm = imm;
        decodedImmBitWidth = immBitWidth;

        setOperandString(format(context, outputOptions, instruction.getOperandFormat()));
        setCommentString(format(context, outputOptions, instruction.getCommentFormat()));

        /* ACTION processing */

        int currentlySelectedRegisterNumber = TxCPUState.NOREG;

        for (char actionChar : instruction.getAction().toCharArray())
        {
            switch (actionChar)
            {
//                case 'A':
//                    r = TxCPUState.AC;
//                    break;
//                case 'C':
//                    r = TxCPUState.CCR;
//                    break;
//                case 'F':
//                    r = TxCPUState.FP;
//                    break;
//                case 'P':
//                    r = TxCPUState.PS;
//                    break;
//                case 'S':
//                    r = TxCPUState.SP;
//                    break;
                case 'i':
                    // Select Rs
                    currentlySelectedRegisterNumber = decodedRsFs;
                    break;
                case 'j':
                    // Select Ry
                    currentlySelectedRegisterNumber = decodedRtFt;
                    break;
                case 'k':
                    // Select Rd
                    currentlySelectedRegisterNumber = decodedRdFd;
                    break;
                case 'w':
                    // Declare the selected register as "undefined"
                    if (updateRegisters) {
                        context.cpuState.setRegisterUndefined(currentlySelectedRegisterNumber);
                    }
                    break;
                case 'v':
                    // Set the selected register to the immediate operand value
                    if (updateRegisters && context.cpuState.registerExists(currentlySelectedRegisterNumber)) {
                        context.cpuState.setRegisterDefined(currentlySelectedRegisterNumber);
                        context.cpuState.setReg(currentlySelectedRegisterNumber, decodedImm);
                    }
                    break;
                case 'V':
                    // Set the selected register to the immediate operand value shifted left by 16
                    if (updateRegisters && context.cpuState.registerExists(currentlySelectedRegisterNumber)) {
                        context.cpuState.setRegisterDefined(currentlySelectedRegisterNumber);
                        context.cpuState.setReg(currentlySelectedRegisterNumber, decodedImm << 16);
                    }
                    break;
                case '+':
                    // Add the immediate operand to the selected register
                    if (updateRegisters && context.cpuState.registerExists(currentlySelectedRegisterNumber)) {
                        // TODO : this doesn't handle "addiu $a0, $a1, 2" : would execute $a0 += 2
                        context.cpuState.setReg(currentlySelectedRegisterNumber, context.cpuState.getReg(currentlySelectedRegisterNumber) + (decodedImm << 16 >> 16));
                    }
                    break;
                case '|':
                    // Or the immediate operand with the selected register
                    if (updateRegisters && context.cpuState.registerExists(currentlySelectedRegisterNumber)) {
                        context.cpuState.setReg(currentlySelectedRegisterNumber, context.cpuState.getReg(currentlySelectedRegisterNumber) | decodedImm);
                    }
                    break;
                case 'x':
                    // Unselect register
                    currentlySelectedRegisterNumber = TxCPUState.NOREG;
                    break;
                default:
                    System.err.println("bad action '" + actionChar + "' in " + instruction + " at " + Format.asHex(context.cpuState.pc, 8));
                    break;
            }
        }


        /* LINE BREAKS and INDENT (delay slot) processing */

        // Retrieve stored delay slot type to print this instruction
        setDelaySlotType(context.getStoredDelaySlotType());
        // Store the one of this instruction for printing next one
        context.setStoredDelaySlotType(instruction.getDelaySlotType());


        boolean newIsBreak = BREAK_FLOW_TYPES.contains(instruction.getFlowType());

        if (instruction.getDelaySlotType() == Instruction.DelaySlotType.NONE) {
            // Current instruction has no delay slot
            // Break if requested by current instruction (JMP, RET) or if we're in the delay slot of the previous one
            setMustInsertLineBreak(context.isLineBreakRequested() || newIsBreak);
            // Clear break request for next one
            context.setLineBreakRequest(false);
        }
        else {
            // Current instruction has a delay slot
            // Don't break now
            setMustInsertLineBreak(false);
            // Request a break after the next instruction if needed (current instruction is a JMP or RET)
            context.setLineBreakRequest(newIsBreak);
        }

    }

    private String format(StatementContext context, Set<OutputOption> outputOptions, String formatString) throws DisassemblyException {
        int pos;
        int tmp;

        int offset = 0;
        boolean isOptionalExpression = false; // sections between square brackets are "optional"

        StringBuilder buffer = new StringBuilder();
        StringBuilder tmpBuffer = null;

        // See TxInstruction constructor for meaning of chars
        for (char formatChar : formatString.toCharArray())
        {
            switch (formatChar)
            {
                case '#':
                    buffer.append(fmt_imm);
                    break;
                case '&':
                    buffer.append(fmt_and);
                    break;
                case '(':
                    buffer.append(fmt_par);
                    break;
                case ')':
                    buffer.append(fmt_ens);
                    break;
                case '+':
                    buffer.append(fmt_inc);
                    break;
                case ',':
                    buffer.append(fmt_nxt);
                    break;
                case '-':
                    buffer.append(fmt_dec);
                    break;
                case '@':
                    buffer.append(fmt_mem);
                    break;
                case '[':
                    // Start of bracket. Store currentBuffer for later and start own buffer
                    tmpBuffer = buffer;
                    buffer = new StringBuilder();
                    isOptionalExpression = true;
                    break;
                case ']':
                    // Analyse result of string between brackets and compare it to what was in the buffer before
                    if (!stripped(tmpBuffer).equals(stripped(buffer))) {
                        // If different, add it
                        tmpBuffer.append(buffer);
                    }
                    // Then revert to normal mode
                    buffer = tmpBuffer;
                    isOptionalExpression = false;
                    break;

                case '2':
                    decodedImm <<= 1;
                    decodedImmBitWidth += 1;
                    break;
                case '4':
                    decodedImm <<= 2;
                    decodedImmBitWidth += 2;
                    break;
                case '8':
                    decodedImm <<= 3;
                    decodedImmBitWidth += 3;
                    break;

                case 'A':
                    buffer.append(TxCPUState.registerLabels[TxCPUState.RA]);
                    break;
                case 'F':
                    buffer.append(TxCPUState.registerLabels[TxCPUState.FP]);
                    break;
                case 'P':
                    buffer.append("pc");
                    break;
                case 'S':
                    buffer.append(TxCPUState.registerLabels[TxCPUState.SP]);
                    break;

                case 'B': // Bit operations such as bext, bins, etc (using SPECIAL_BIT encoding), have an offset on a varying base register.
                          // 'B' prints this full expression, such as "0x12(SP)"
                    switch (rs_fs) {
                        case 0b00:
                            if (BinaryArithmetics.isNegative(decodedImmBitWidth, decodedImm)) {
                                buffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR) ? "$" : "0x"), BinaryArithmetics.neg(decodedImmBitWidth, decodedImm), decodedImmBitWidth));
                            }
                            else {
                                buffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, decodedImmBitWidth - 1));
                            }
                            buffer.append("(" + TxCPUState.registerLabels[0] + ")");
                            break;
                        case 0b01:
                            buffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, decodedImmBitWidth));
                            buffer.append("(" + TxCPUState.registerLabels[TxCPUState.GP] + ")");
                            break;
                        case 0b10:
                            buffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, decodedImmBitWidth));
                            buffer.append("(" + TxCPUState.registerLabels[TxCPUState.SP] + ")");
                            break;
                        case 0b11:
                            buffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, decodedImmBitWidth));
                            buffer.append("(" + TxCPUState.registerLabels[TxCPUState.FP] + ")");
                            break;
                        default:
                            throw new DisassemblyException("Unrecognized base for Bit operation : " + rs_fs);

                    }
                    break;

                case 'I':
                    if (context.cpuState.isRegisterDefined(decodedRsFs))
                    {
                        decodedImm = context.cpuState.getReg(decodedRsFs);
                        decodedImmBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        decodedImmBitWidth = 0;
                    }
                    break;
                case 'J':
                    if (context.cpuState.isRegisterDefined(decodedRtFt))
                    {
                        decodedImm = context.cpuState.getReg(decodedRtFt);
                        decodedImmBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        decodedImmBitWidth = 0;
                    }
                    break;
                case 'K':
                    if (context.cpuState.isRegisterDefined(decodedRdFd))
                    {
                        decodedImm = context.cpuState.getReg(decodedRdFd);
                        decodedImmBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        decodedImmBitWidth = 0;
                    }
                    break;

                case 'a':
                    pos = decodedImmBitWidth;
                    while (pos >= 8){
                        pos -= 8;
                        buffer.append(Format.asAscii(decodedImm >> pos));
                    }
                    break;
                case 'b':
                    /* shift2 */
                    decodedImm += 16;
                    decodedImmBitWidth += 1;
                    break;
                case 'c':
                    /* coprocessor operation */
                    buffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(c, 2));
                    break;
                case 'd':
                    /* unsigned decimal */
                    if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedImm == 0)) buffer.append(decodedImm);
                    break;
                case 'f':
                    pos = decodedImmBitWidth >> 1;

                    tmp = (int)(((1L << pos) - 1) & (decodedImm >> pos));
                    int tmq = (int)(((1L << pos) - 1) & decodedImm);
                    if (tmq != 0)
                        buffer.append(((double)tmp) / tmq);
                    else
                        buffer.append("NaN");

                    break;

                case 'i':
                    if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedRsFs == 0)) buffer.append(TxCPUState.registerLabels[decodedRsFs]);
                    break;
                case 'j':
                    if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedRtFt == 0)) buffer.append(TxCPUState.registerLabels[decodedRtFt]);
                    break;
                case 'k':
                    try {
                        if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedRdFd == 0)) buffer.append(TxCPUState.registerLabels[decodedRdFd]);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 'l':
                    if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedSaCc == 0)) buffer.append(decodedSaCc);
                    break;

                case 'n':
                    /* negative constant */
                    //opnd.append(hexPrefix + Format.asHexInBitsLength(dp.displayx, dp.w + 1));
                    buffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << (decodedImmBitWidth + 1)) - 1) & BinaryArithmetics.neg(decodedImmBitWidth, (1 << (decodedImmBitWidth)) | decodedImm), decodedImmBitWidth + 1));
                    break;
                case 'p':
                    /* pair */
                    pos = decodedImmBitWidth >> 1;
                    buffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << pos) - 1) & (decodedImm >> pos), pos));
                    buffer.append(fmt_nxt);
                    buffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << pos) - 1) & decodedImm, pos));
                    break;
                case 'q':
                    /* rational */
                    pos = decodedImmBitWidth >> 1;
                    buffer.append(((1L << pos) - 1) & (decodedImm >> pos));
                    buffer.append("/");
                    buffer.append(((1L << pos) - 1) & decodedImm);
                    break;
                case 'r':
                    /* relative to PC */
                    offset = context.cpuState.pc + numBytes;
                    break;
                case 'R':
                    /* relative to PC & 0xF0000000 */
                    offset = context.cpuState.pc & 0xF0000000;
                    break;
                case 's':
                    /* signed constant */
                    if (offset != 0) {
                        // relative signed means immediate must be sign-extended before appending, then printed unsigned
                        decodedImm = offset + BinaryArithmetics.signExtend(decodedImmBitWidth, decodedImm);
                        decodedImmBitWidth = 32;
                        buffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, decodedImmBitWidth));
                    }
                    else {
                        if (BinaryArithmetics.isNegative(decodedImmBitWidth, decodedImm)) {
                            /* avoid "a+-b" : remove the last "+" so that output is "a-b" */
                            if (outputOptions.contains(OutputOption.CSTYLE) && (buffer.charAt(buffer.length() - 1) == '+')) {
                                buffer.delete(buffer.length() - 1, buffer.length() - 1);
                            }
                            buffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), BinaryArithmetics.neg(decodedImmBitWidth, decodedImm), decodedImmBitWidth));
                        }
                        else {
                            buffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, decodedImmBitWidth - 1));
                        }
                    }
                    break;
                case 'u':
                    /* unsigned constant */
                    if (offset != 0) {
                        decodedImm = offset + decodedImm;
                        decodedImmBitWidth = 32;
                    }
                    buffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, decodedImmBitWidth));
                    break;
                case 'v':
                    /* vector */
                    buffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(0xFF - (0xFF & ((context.cpuState.pc - memRangeStart) / 4)), 1));
                    break;
                case 'x':
                    decodedImm |= 0x100;
                    break;
                case 'y':
                    c += 8;
                    // goto case 'z'; /*FALLTHROUGH*/
                case 'z':
                    /* register list */
                    if ((sa_cc & 0b100) != 0) { // RA
                        buffer.append(TxCPUState.registerLabels[TxCPUState.RA] + ",");
                    }
                    if ((sa_cc & 0b010) != 0) { // S0
                        buffer.append(TxCPUState.registerLabels[TxCPUState.S0] + ",");
                    }
                    if ((sa_cc & 0b001) != 0) { // S1
                        buffer.append(TxCPUState.registerLabels[TxCPUState.S1] + ",");
                    }

                    int xsregs = (binaryStatement >> 24) & 0b111;
                    if (xsregs > 0) {
                        buffer.append(TxCPUState.registerLabels[18]);
                        int lastReg = Math.min(xsregs + 17, 23);
                        if (lastReg >= 18) {
                            buffer.append("-" + TxCPUState.registerLabels[lastReg]);
                        }
                        buffer.append(",");
                        if (xsregs == 7) {
                            buffer.append(TxCPUState.registerLabels[30] + ",");
                        }
                    }

                    switch ((binaryStatement >> 16) & 0b1111) {
                        case 0b0001:buffer.append("[" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b0010:buffer.append("[" + TxCPUState.registerLabels[6] + "-" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b0011:buffer.append("[" + TxCPUState.registerLabels[5] + "-" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b1011:buffer.append("[" + TxCPUState.registerLabels[4] + "-" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b0100:buffer.append(TxCPUState.registerLabels[4]);break;
                        case 0b0101:buffer.append(TxCPUState.registerLabels[4] + ",[" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b0110:buffer.append(TxCPUState.registerLabels[4] + ",[" + TxCPUState.registerLabels[6]+ "-" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b0111:buffer.append(TxCPUState.registerLabels[4] + ",[" + TxCPUState.registerLabels[5]+ "-" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b1000:buffer.append(TxCPUState.registerLabels[4] + "-" + TxCPUState.registerLabels[5] + ", ");break;
                        case 0b1001:buffer.append(TxCPUState.registerLabels[4] + "-" + TxCPUState.registerLabels[5] + ",[" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b1010:buffer.append(TxCPUState.registerLabels[4] + "-" + TxCPUState.registerLabels[5] + ",[" + TxCPUState.registerLabels[6] + "-" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b1100:buffer.append(TxCPUState.registerLabels[4] + "-" + TxCPUState.registerLabels[6] + ", ");break;
                        case 0b1101:buffer.append(TxCPUState.registerLabels[4] + "-" + TxCPUState.registerLabels[6] + ",[" + TxCPUState.registerLabels[7] + "], ");break;
                        case 0b1110:buffer.append(TxCPUState.registerLabels[4] + "-" + TxCPUState.registerLabels[7] + ", ");break;
                    }

                    if (isExtended() && imm == 0) {
                        buffer.append(" 0x" + Format.asHex(128, 2));
                    }
                    else {
                        buffer.append(" 0x" + Format.asHex((imm << 3), 2));
                    }
                    break;
                default:
                    buffer.append(formatChar);
                    break;
            }
        }

        return buffer.toString();
    }

    /** Remove blanks and comas */
    private String stripped(StringBuilder buffer) {
        return StringUtils.replace(StringUtils.replace(buffer.toString(), " ", ""), ",", "");
    }

    public String getFormattedBinaryStatement() {
        if (numBytes == 4)
        {
            // 32b, or extended 16b
            return Format.asHex(binaryStatement, 8);
        }
        else {
            // 16b
            return Format.asHex(binaryStatement, 4) + "    ";
        }
    }

    @Override
    public int getNumBytes() {
        return numBytes;
    }

    public boolean isExtended() {
        return (numBytes == 4);
    }

    public void fill16bInstruction(int binaryStatement, int pc, Memory memory) throws DisassemblyException {
        // In 16-bit ISA, all instructions are on 16-bits, except EXTENDed instructions and JAL/JALX.
        // Handle these 3 cases, based on the 5 MSBs of the 16 bits read:
        switch (binaryStatement & 0b11111000_00000000) {
            case 0b11110000_00000000:
                // This is the EXTEND prefix. Get real instruction
                int realBinaryStatement = memory.loadInstruction16(pc + 2);
                setBinaryStatement(4, (binaryStatement << 16) | realBinaryStatement);

                // Now most of the instructions can be determined based only on the lower 16bits, except two cases: Min/Max and Bs1f/Bfins:
                switch (realBinaryStatement & 0b11111000_00011111) {
                    case 0b11101000_00000101:
                        // Weird min/max encoding : they are both extended instructions with the same lower 16b pattern
                        setInstruction(TxInstructionSet.getMinMaxInstructionForStatement(getBinaryStatement()));
                        break;
                    case 0b11101000_00000111:
                        // Weird Bs1f/Bfins encoding : they are both extended instructions with the same lower 16b pattern
                        setInstruction(TxInstructionSet.getBs1fBfinsInstructionForStatement(getBinaryStatement()));
                        break;
                    default:
                        // Normal case for EXTENDed instructions. Decode based on lower 16 bits
                        setInstruction(TxInstructionSet.getExtendedInstructionFor16BitStatement(realBinaryStatement));
                }
                break;
            case 0b00011000_00000000:
                // This is the JAL/JALX prefix.
                int fullStatement = (binaryStatement << 16) | memory.loadInstruction16(pc + 2);
                setBinaryStatement(4, fullStatement);

                try {
                    setInstruction(TxInstructionSet.getJalInstructionForStatement(fullStatement));
                } catch (DisassemblyException e) {
                    System.err.println("Could not decode statement 0x" + Format.asHex(getBinaryStatement(), 4) + " at 0x" + Format.asHex(pc, 8) + ": " + e.getClass().getName());
                }
                break;
            default:
                // Normal non-EXTENDed 16-bit instructions
                setBinaryStatement(2, binaryStatement);
                setInstruction(TxInstructionSet.getInstructionFor16BitStatement(binaryStatement));
                break;
        }
    }

    public void fill32bInstruction(int binaryStatement32) throws DisassemblyException {
        setBinaryStatement(4, binaryStatement32);
        setInstruction(TxInstructionSet.getInstructionFor32BitStatement(binaryStatement32));
    }

    public boolean isPotentialStuffing() {
        return      (numBytes == 2 && getBinaryStatement() == 0xFFFF) /* TX 0xFFFF stuffing in 16-bit ISA mode*/
                ||  (numBytes == 4 && getBinaryStatement() == 0x00000000) /* TX NOP stuffing in 32-bit ISA mode*/
                ;
    }
}
