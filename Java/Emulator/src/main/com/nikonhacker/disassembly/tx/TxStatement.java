package com.nikonhacker.disassembly.tx;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.Instruction;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.Statement;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Set;

/*
 * Statement : an instance of a specific Instruction with specific operands
 */
public class TxStatement extends Statement {
    ///* output formatting */
    public static String fmt_nxt;
    public static String fmt_imm;
    public static String fmt_and;
    public static String fmt_inc;
    public static String fmt_dec;
    public static String fmt_mem;
    public static String fmt_par;
    public static String fmt_ens;

    /** rs or fs operand */
    public int rs_fs; // as-is from binary code
    public int decodedRsFs; // interpreted

    /** rt or ft operand */
    public int rt_ft; // as-is from binary code
    public int decodedRtFt; // interpreted

    /** rd of fd operand */
    public int rd_fd; // as-is from binary code
    public int decodedRdFd; // interpreted

    /** sa (shift amount) of cc (CP1 condition code) operand */
    // TODO use imm instead ?? what is decodedSa ??
    public int sa_cc; // as-is from binary code
    public int decodedSaCc; // interpreted

    /** coprocessor operation (not implemented yet in operand parsing, only for display) */
    public int c;

    /** direct operand */
    public int imm; // as-is from binary code
    public int decodedImm; // interpreted


    /** number of significant bits in decodedX (for display only) */
    public int immBitWidth;

    /** start of decoded memory block (used only for display in "v"ector format */
    public int memRangeStart = 0;

    private int binaryStatement;

    /**
     * Default decoding upon class loading
     */
    static {
        initFormatChars(EnumSet.noneOf(OutputOption.class));
    }

    public TxStatement(int memRangeStart) {
        this.memRangeStart = memRangeStart;
        reset();
    }

    public int getBinaryStatement() {
        return binaryStatement;
    }

    public void setBinaryStatement(int binaryStatement) {
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



    public void decode32BitOperands(int pc, Memory memory) {
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

    public void decode16BitOperands(int pc, Memory memory) {
        if (!isExtended()) {
            // No EXTEND
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
                    imm   =   ((binaryStatement >> (16-11)) & 0b1111100000000000)
                            | ((binaryStatement >> (21- 5)) & 0b0000011111100000)
                            | ((binaryStatement           ) & 0b0000000000011111);
                    immBitWidth = 16;
                    break;
                case RI:
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rs_fs = rt_ft;
                    imm   =   ((binaryStatement >> (16-11)) & 0b1111100000000000)
                            | ((binaryStatement >> (21- 5)) & 0b0000011111100000)
                            | ((binaryStatement           ) & 0b0000000000011111);
                    immBitWidth = 16;
                    break;
                case RRI:
                    rs_fs = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 8) & 0b111]; // rx
                    rt_ft = TxCPUState.REGISTER_MAP_16B[(binaryStatement >>> 5) & 0b111]; // ry
                    imm   =   ((binaryStatement >> (16-11)) & 0b1111100000000000)
                            | ((binaryStatement >> (21- 5)) & 0b0000011111100000)
                            | ((binaryStatement           ) & 0b0000000000011111);
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
                    imm   =   ((binaryStatement >> (16-11)) & 0b0111100000000000)
                            | ((binaryStatement >> (20- 4)) & 0b0000011111110000)
                            | ((binaryStatement           ) & 0b0000000000001111);
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
                    sa_cc =   ((binaryStatement >> (20- 7)) & 0b1110000000)
                            | ((binaryStatement >> (16- 3)) & 0b0001111000)
                            | ((binaryStatement >> 4)       & 0b0000000111); // xsregs|aregs|ra|s0|s1
                    break;
                case SPC_BIT:
                    sa_cc = (binaryStatement >>> 5) & 0b111; // pos3
                    rs_fs = (binaryStatement >>> 19) & 0b11; // base
                    imm   =   ((binaryStatement >> (16-11)) & 0b0011100000000000)
                            | ((binaryStatement >> (21- 5)) & 0b0000011111100000)
                            | ((binaryStatement           ) & 0b0000000000011111);
                    immBitWidth = 14;
                    break;
                case JAL_JALX:
                    imm   =   ((binaryStatement << (21-16)) & 0b00000011111000000000000000000000)
                            | ((binaryStatement >> (21-16)) & 0b00000000000111110000000000000000)
                            | ( binaryStatement             & 0b00000000000000001111111111111111);
                    immBitWidth = 26;
                    break;
                case RR_BS1F_BFINS:
                    System.out.println(Format.asBinary(binaryStatement, 32) + " " + (((binaryStatement & 0b00000100000000000000000000000000) == 0)?"BFINS":"BS1F"));
                    if (((binaryStatement & 0b00000100000000000000000000000000) != 0)) {
                        System.out.println("BS1f");
                    }
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
     * @param cpuState This stores CPU state.
     * @param updateRegisters if true, cpuState registers will be updated during action interpretation.
     * @see TxInstruction for a description of all possible chars
     */
    public void formatOperandsAndComment(TxCPUState cpuState, boolean updateRegisters, Set<OutputOption> outputOptions) {

        /* DISPLAY FORMAT processing */

        int tmp;
        int pos;

        int offset = 0;

        decodedRsFs = rs_fs;
        decodedRtFt = rt_ft;
        decodedRdFd = rd_fd;
        decodedSaCc = sa_cc;
        decodedImm = imm;

        boolean isOptionalExpression = false; // sections between square brackets are "optional"

        StringBuilder operandBuffer = new StringBuilder();
        StringBuilder commentBuffer = new StringBuilder();

        StringBuilder currentBuffer = operandBuffer;
        StringBuilder tmpBuffer = null;

        // See TxInstruction constructor for meaning of chars
        for (char formatChar : instruction.getDisplayFormat().toCharArray())
        {
            switch (formatChar)
            {
                case '#':
                    currentBuffer.append(fmt_imm);
                    break;
                case '&':
                    currentBuffer.append(fmt_and);
                    break;
                case '(':
                    currentBuffer.append(fmt_par);
                    break;
                case ')':
                    currentBuffer.append(fmt_ens);
                    break;
                case '+':
                    currentBuffer.append(fmt_inc);
                    break;
                case ',':
                    currentBuffer.append(fmt_nxt);
                    break;
                case '-':
                    currentBuffer.append(fmt_dec);
                    break;
                case '@':
                    currentBuffer.append(fmt_mem);
                    break;

                case ';':
                    currentBuffer = commentBuffer;
                    break;
                case '[':
                    // Start of bracket. Store currentBuffer for later and start own buffer
                    tmpBuffer = currentBuffer;
                    currentBuffer = new StringBuilder();
                    isOptionalExpression = true;
                    break;
                case ']':
                    // Analyse result of string between brackets and compare it to what was in the buffer before
                    if (!stripped(tmpBuffer).equals(stripped(currentBuffer))) {
                        // If different, add it
                        tmpBuffer.append(currentBuffer);
                    }
                    // Then revert to normal mode
                    currentBuffer = tmpBuffer;
                    isOptionalExpression = false;
                    break;

                case '2':
                    decodedImm <<= 1;
                    immBitWidth += 1;
                    break;
                case '4':
                    decodedImm <<= 2;
                    immBitWidth += 2;
                    break;

                case 'A':
                    currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.RA]);
                    break;
                case 'F':
                    currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.FP]);
                    break;
                case 'P':
                    currentBuffer.append("pc");
                    break;
                case 'S':
                    currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.SP]);
                    break;

                case 'I':
                    if (cpuState.isRegisterDefined(decodedRsFs))
                    {
                        decodedImm = cpuState.getReg(decodedRsFs);
                        immBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        immBitWidth = 0;
                    }
                    break;
                case 'J':
                    if (cpuState.isRegisterDefined(decodedRtFt))
                    {
                        decodedImm = cpuState.getReg(decodedRtFt);
                        immBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        immBitWidth = 0;
                    }
                    break;
                case 'K':
                    if (cpuState.isRegisterDefined(decodedRdFd))
                    {
                        decodedImm = cpuState.getReg(decodedRdFd);
                        immBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        immBitWidth = 0;
                    }
                    break;

                case 'a':
                    pos = immBitWidth;
                    while (pos >= 8){
                        pos -= 8;
                        currentBuffer.append(Format.asAscii(decodedImm >> pos));
                    }
                    break;
                case 'b':
                    /* shift2 */
                    decodedImm += 16;
                    immBitWidth += 1;
                    break;
                case 'c':
                    /* coprocessor operation */
                    currentBuffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(c, 2));
                    break;
                case 'd':
                    /* unsigned decimal */
                    if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedImm == 0)) currentBuffer.append(decodedImm);
                    break;
                case 'f':
                    pos = immBitWidth >> 1;

                    tmp = (int)(((1L << pos) - 1) & (decodedImm >> pos));
                    int tmq = (int)(((1L << pos) - 1) & decodedImm);
                    if (tmq != 0)
                        currentBuffer.append(((double)tmp) / tmq);
                    else
                        currentBuffer.append("NaN");

                    break;

                case 'i':
                    if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedRsFs == 0)) currentBuffer.append(TxCPUState.REG_LABEL[decodedRsFs]);
                    break;
                case 'j':
                    if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedRtFt == 0)) currentBuffer.append(TxCPUState.REG_LABEL[decodedRtFt]);
                    break;
                case 'k':
                    try {
                        if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedRdFd == 0)) currentBuffer.append(TxCPUState.REG_LABEL[decodedRdFd]);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 'l':
                    if (!(isOptionalExpression && tmpBuffer.length() == 0 && decodedSaCc == 0)) currentBuffer.append(decodedSaCc);
                    break;

                case 'n':
                    /* negative constant */
                    //opnd.append(hexPrefix + Format.asHexInBitsLength(dp.displayx, dp.w + 1));
                    currentBuffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << (immBitWidth + 1)) - 1) & BinaryArithmetics.NEG(immBitWidth, (1 << (immBitWidth)) | decodedImm), immBitWidth + 1));
                    break;
                case 'p':
                    /* pair */
                    pos = immBitWidth >> 1;
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << pos) - 1) & (decodedImm >> pos), pos));
                    currentBuffer.append(fmt_nxt);
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << pos) - 1) & decodedImm, pos));
                    break;
                case 'q':
                    /* rational */
                    pos = immBitWidth >> 1;
                    currentBuffer.append(((1L << pos) - 1) & (decodedImm >> pos));
                    currentBuffer.append("/");
                    currentBuffer.append(((1L << pos) - 1) & decodedImm);
                    break;
                case 'r':
                    /* relative to PC */
                    offset = cpuState.pc + 2 + (isExtended()?2:0);
                    break;
                case 'R':
                    /* relative to PC & 0xF0000000 */
                    offset = cpuState.pc & 0xF0000000;
                    break;
                case 's':
                    /* signed constant */
                    if (offset != 0) {
                        // relative signed means immediate must be sign-extended before appending, then printed unsigned
                        decodedImm = offset + BinaryArithmetics.signExtend(immBitWidth, decodedImm);
                        immBitWidth = 32;
                        currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, immBitWidth));
                    }
                    else {
                        if (BinaryArithmetics.IsNeg(immBitWidth, decodedImm)) {
                            /* avoid "a+-b" : remove the last "+" so that output is "a-b" */
                            if (outputOptions.contains(OutputOption.CSTYLE) && (currentBuffer.charAt(currentBuffer.length() - 1) == '+')) {
                                currentBuffer.delete(currentBuffer.length() - 1, currentBuffer.length() - 1);
                            }
                            currentBuffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), BinaryArithmetics.NEG(immBitWidth, decodedImm), immBitWidth));
                        }
                        else {
                            currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, immBitWidth - 1));
                        }
                    }
                    break;
                case 'u':
                    /* unsigned constant */
                    if (offset != 0) {
                        decodedImm = offset + decodedImm;
                        immBitWidth = 32;
                    }
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, immBitWidth));
                    break;
                case 'v':
                    /* vector */
                    currentBuffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(0xFF - (0xFF & ((cpuState.pc - memRangeStart) / 4)), 1));
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
                        currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.RA] + ",");
                    }
                    if ((sa_cc & 0b010) != 0) { // S0
                        currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.S0] + ",");
                    }
                    if ((sa_cc & 0b001) != 0) { // S1
                        currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.S1] + ",");
                    }

                    int xsregs = (binaryStatement >> 24) & 0b111;
                    if (xsregs > 0) {
                        currentBuffer.append(TxCPUState.REG_LABEL[18]);
                        int lastReg = Math.min(xsregs + 17, 23);
                        if (lastReg >= 18) {
                            currentBuffer.append("-" + TxCPUState.REG_LABEL[lastReg]);
                        }
                        currentBuffer.append(",");
                        if (xsregs == 7) {
                            currentBuffer.append(TxCPUState.REG_LABEL[30] + ",");
                        }
                    }

                    switch ((binaryStatement >> 16) & 0b1111) {
                        case 0b0001:currentBuffer.append("[" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b0010:currentBuffer.append("[" + TxCPUState.REG_LABEL[6] + "-" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b0011:currentBuffer.append("[" + TxCPUState.REG_LABEL[5] + "-" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b1011:currentBuffer.append("[" + TxCPUState.REG_LABEL[4] + "-" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b0100:currentBuffer.append(TxCPUState.REG_LABEL[4]);break;
                        case 0b0101:currentBuffer.append(TxCPUState.REG_LABEL[4] + ",[" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b0110:currentBuffer.append(TxCPUState.REG_LABEL[4] + ",[" + TxCPUState.REG_LABEL[6]+ "-" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b0111:currentBuffer.append(TxCPUState.REG_LABEL[4] + ",[" + TxCPUState.REG_LABEL[5]+ "-" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b1000:currentBuffer.append(TxCPUState.REG_LABEL[4] + "-" + TxCPUState.REG_LABEL[5] + ", ");break;
                        case 0b1001:currentBuffer.append(TxCPUState.REG_LABEL[4] + "-" + TxCPUState.REG_LABEL[5] + ",[" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b1010:currentBuffer.append(TxCPUState.REG_LABEL[4] + "-" + TxCPUState.REG_LABEL[5] + ",[" + TxCPUState.REG_LABEL[6] + "-" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b1100:currentBuffer.append(TxCPUState.REG_LABEL[4] + "-" + TxCPUState.REG_LABEL[6] + ", ");break;
                        case 0b1101:currentBuffer.append(TxCPUState.REG_LABEL[4] + "-" + TxCPUState.REG_LABEL[6] + ",[" + TxCPUState.REG_LABEL[7] + "], ");break;
                        case 0b1110:currentBuffer.append(TxCPUState.REG_LABEL[4] + "-" + TxCPUState.REG_LABEL[7] + ", ");break;
                    }

                    if (isExtended() && imm == 0) {
                        currentBuffer.append(" 0x" + Format.asHex(128, 2));
                    }
                    else {
                        currentBuffer.append(" 0x" + Format.asHex((imm << 3), 2));
                    }
                    break;
                default:
                    currentBuffer.append(formatChar);
                    break;
            }
        }

        setOperandString(operandBuffer.toString());

        setCommentString(commentBuffer.toString());


        /* ACTION processing */

        int r = TxCPUState.NOREG;

        for (char s : instruction.getAction().toCharArray())
        {
            switch (s)
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
                    r = decodedRsFs;
                    break;
                case 'j':
                    r = decodedRtFt;
                    break;
                case 'k':
                    r = decodedRdFd;
                    break;
                case 'w':
                    if (updateRegisters) {
                        cpuState.setRegisterUndefined(r);
                    }
                    break;
                case 'v':
                    if (updateRegisters && cpuState.registerExists(r)) {
                        cpuState.setRegisterDefined(r);
                        cpuState.setReg(r, decodedImm);
                    }
                    break;
                case 'V':
                    if (updateRegisters && cpuState.registerExists(r)) {
                        cpuState.setRegisterDefined(r);
                        cpuState.setReg(r, decodedImm << 16);
                    }
                    break;
                case '+':
                    if (updateRegisters && cpuState.registerExists(r)) {
                        cpuState.setReg(r, cpuState.getReg(r) + (decodedImm << 16 >> 16));
                    }
                    break;
                case 'x':
                    r = TxCPUState.NOREG;
                    break;
                default:
                    System.err.println("bad action '" + s + "' in " + instruction + " at " + Format.asHex(cpuState.pc, 8));
                    break;
            }
        }


        /* LINE BREAKS and INDENT (delay slot) processing */

        // Retrieve stored delay slot type to print this instruction
        setDelaySlotType(cpuState.getStoredDelaySlotType());
        // Store the one of this instruction for printing next one
        cpuState.setStoredDelaySlotType(instruction.getDelaySlotType());


        boolean newIsBreak = EnumSet.of(Instruction.FlowType.JMP, Instruction.FlowType.RET).contains(instruction.getFlowType());

        if (instruction.getDelaySlotType() == Instruction.DelaySlotType.NONE) {
            // Current instruction has no delay slot
            // Break if requested by current instruction (JMP, RET) or if we're in the delay slot of the previous one
            setMustInsertLineBreak(cpuState.isLineBreakRequested() || newIsBreak);
            // Clear break request for next one
            cpuState.setLineBreakRequest(false);
        }
        else {
            // Current instruction has a delay slot
            // Don't break now
            setMustInsertLineBreak(false);
            // Request a break after the next instruction if needed (current instruction is a JMP or RET)
            cpuState.setLineBreakRequest(newIsBreak);
        }

    }

    /** Remove blanks and comas */
    private String stripped(StringBuilder buffer) {
        return StringUtils.replace(StringUtils.replace(buffer.toString(), " ", ""), ",", "");
    }

    protected String formatAsHex() {
        if (isExtended())
        {
            // 32b, or extended 16b
            return Format.asHex(binaryStatement, 8);
        }
        else {
            // 16b
            return Format.asHex(binaryStatement, 4) + "    ";
        }
    }

    public boolean isExtended() {
        return (binaryStatement & 0xFFFF0000) != 0;
    }
}
