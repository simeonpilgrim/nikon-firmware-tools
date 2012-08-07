package com.nikonhacker.disassembly.tx;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
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
    ///* disassembly */
    // [Flags]
    public final static int DF_FLOW = 0x01;
    public final static int DF_BREAK = 0x02;
    public final static int DF_DELAY = 0x04;
    public final static int DF_BRANCH = 0x10;
    public final static int DF_JUMP = 0x20;
    public final static int DF_CALL = 0x40;
    public final static int DF_RETURN = 0x80;
    public final static int DF_TO_KEEP = DF_FLOW | DF_BRANCH | DF_JUMP | DF_CALL | DF_RETURN;
    public final static int DF_TO_COPY = DF_DELAY;
    public final static int DF_TO_DELAY = DF_BREAK;

    ///* output formatting */
    public static String fmt_nxt;
    public static String fmt_imm;
    public static String fmt_and;
    public static String fmt_inc;
    public static String fmt_dec;
    public static String fmt_mem;
    public static String fmt_par;
    public static String fmt_ens;

    /** cached CPUState, for CALLs and INTs */
    public TxCPUState cpuState = null;

    /** rs operand */
    public int rs; // as-is from binary code
    public int decodedRs; // interpreted

    /** rt operand */
    public int rt; // as-is from binary code
    public int decodedRt; // interpreted

    /** rd operand */
    public int rd; // as-is from binary code
    public int decodedRd; // interpreted

    /** shift amount operand */
    public int sa; // as-is from binary code
    public int decodedSa; // interpreted

    /** coprocessor operation (not implemented yet in operand parsing, only for display) */
    public int c;

    /** direct operand */
    public int imm; // as-is from binary code
    public int decodedImm; // interpreted


    /** number of significant bits in decodedX (for display only) */
    public int immBitWidth;

    /** flags (for display only) */
    public int flags;

    /** start of decoded memory block (used only for display in "v"ector format */
    public int memRangeStart = 0;

    private int binaryStatement;


    /** This array is used to decode the mfc0 and mtc0 instruction operands
     * Array is indexed by [SEL][number] and returns a register index as defined in TxCPUState
     */
    private static final int[][] CP0_REGISTER_NUMBER_MAP = new int[][]{
            // SEL0
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    TxCPUState.BadVAddr, TxCPUState.Count, -1, TxCPUState.Compare, TxCPUState.Status, TxCPUState.Cause, TxCPUState.EPC, TxCPUState.PRId,
                    TxCPUState.Config, -1, -1, -1, -1, -1, TxCPUState.SSCR, TxCPUState.Debug,
                    TxCPUState.DEPC, -1, -1, -1, -1, -1, TxCPUState.ErrorEPC, TxCPUState.DESAVE
            },
            // SEL1
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    TxCPUState.Config1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            },
            // SEL2
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    TxCPUState.Config2, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            },
            // SEL3
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    TxCPUState.Config3, -1, -1, -1, -1, -1, -1, -1,
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
                    -1, TxCPUState.SSCR, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            },
            // SEL7
            {
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, TxCPUState.IER, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1
            }
    };


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



    public void decodeOperands(int pc, Memory memory) {
        switch (((TxInstruction)instruction).getInstructionFormat())
        {
            case R:
                rs    = (binaryStatement >>> 21) & 0b11111;
                rt    = (binaryStatement >>> 16) & 0b11111;
                rd    = (binaryStatement >>> 11) & 0b11111;
                sa    = (binaryStatement >>>  6) & 0b11111;
                break;
            case I:
            case I_BRANCH:
                rs    = (binaryStatement >>> 21) & 0b11111;
                rt    = (binaryStatement >>> 16) & 0b11111;
                imm   =  binaryStatement         & 0xFFFF;
                immBitWidth = 16;
                break;
            case J:
                imm   =  binaryStatement         & 0x03FFFFFF;
                immBitWidth = 26;
                break;
            case BREAK:
                imm   = (binaryStatement >>> 6)  & 0x000FFFFF;
                immBitWidth = 20;
                break;
            case TRAP:
                rs    = (binaryStatement >>> 21) & 0b11111;
                rt    = (binaryStatement >>> 16) & 0b11111;
                imm   = (binaryStatement >>>  6) & 0x3FF;
                immBitWidth = 10;
                break;
            case CP:
                rt    = (binaryStatement >>> 16) & 0b11111;
                rd    = CP0_REGISTER_NUMBER_MAP[binaryStatement & 0b111][(binaryStatement >>> 11) & 0b11111];
                break;
        }
    }

    public void reset() {
        flags = 0;
        immBitWidth = 0;
        c = 0;
        rs = CPUState.NOREG;
        rt = CPUState.NOREG;
        rd = CPUState.NOREG;
        sa = CPUState.NOREG;
        imm = CPUState.NOREG;
        operandString = null;
        commentString = null;
    }

    public void getNextStatement(Memory memory, int address)
    {
        binaryStatement = memory.loadInstruction32(address);
    }

    /**
     * Disassemble TxInstruction for presentation
     * must be called after decodeOperands()
     * @param cpuState This stores CPU state.
     * @param updateRegisters if true, cpuState registers will be updated during action interpretation.
     */
    public void formatOperandsAndComment(TxCPUState cpuState, boolean updateRegisters, Set<OutputOption> outputOptions) {
        int tmp;
        int pos;

        decodedRs = rs;
        decodedRt = rt;
        decodedRd = rd;
        decodedSa = sa;
        decodedImm = imm;

        flags = cpuState.flags;
        cpuState.flags = 0;

        StringBuilder operandBuffer = new StringBuilder();
        StringBuilder commentBuffer = new StringBuilder();

        StringBuilder currentBuffer = operandBuffer;

        for (char formatChar : ((TxInstruction)instruction).displayFormat.toCharArray())
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
                case ';':
                    currentBuffer = commentBuffer;
                    break;
                case '@':
                    currentBuffer.append(fmt_mem);
                    break;
                case '2':
                    decodedImm <<= 1;
                    immBitWidth += 1;
                    break;
                case '4':
                    decodedImm <<= 2;
                    immBitWidth += 2;
                    break;
//                case 'A':
//                    currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.AC]);
//                    break;
//                case 'C':
//                    currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.CCR]);
//                    break;
//                case 'F':
//                    currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.FP]);
//                    break;
                case 'J':
                    if (cpuState.isRegisterDefined(decodedRt))
                    {
                        decodedImm = cpuState.getReg(decodedRt);
                        immBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        immBitWidth = 0;
                    }
                    break;
                case 'I':
                    if (cpuState.isRegisterDefined(decodedRs))
                    {
                        decodedImm = cpuState.getReg(decodedRs);
                        immBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        immBitWidth = 0;
                    }
                    break;
                case 'K':
                    if (cpuState.isRegisterDefined(decodedRd))
                    {
                        decodedImm = cpuState.getReg(decodedRd);
                        immBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        immBitWidth = 0;
                    }
                    break;
                case 'M':
                    currentBuffer.append("ILM");
                    break;
//                case 'P':
//                    currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.PS]);
//                    break;
//                case 'S':
//                    currentBuffer.append(TxCPUState.REG_LABEL[TxCPUState.SP]);
//                    break;
                case 'T':
                    currentBuffer.append("INT");
                    break;
                case 'X':
                case 'Y':
                    throw new RuntimeException("no more X or Y : operand parsing is now done in decodeOperands()");
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
                    currentBuffer.append(decodedImm);
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
                    currentBuffer.append(TxCPUState.REG_LABEL[decodedRs]);
                    break;
                case 'j':
                    currentBuffer.append(TxCPUState.REG_LABEL[decodedRt]);
                    break;
                case 'k':
                    currentBuffer.append(TxCPUState.REG_LABEL[decodedRd]);
                    break;
                case 'l':
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedSa, 5));
                    break;
                case 'g':
                    // todo : special register
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
                    /* relative */
                    // TODO +4 or +2 according to ISA mode 32 or 16
                    decodedImm = cpuState.pc + 4 + BinaryArithmetics.signExtend(immBitWidth, decodedImm);
                    immBitWidth = 32;
                    break;
                case 's':
                    /* signed constant */
                    if (BinaryArithmetics.IsNeg(immBitWidth, decodedImm))
                    {
                        /* avoid "a+-b" : remove the last "+" so that output is "a-b" */
                        if (outputOptions.contains(OutputOption.CSTYLE) && (currentBuffer.charAt(currentBuffer.length() - 1) == '+')) {
                            currentBuffer.delete(currentBuffer.length() - 1, currentBuffer.length() - 1);
                        }
                        currentBuffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), BinaryArithmetics.NEG(immBitWidth, decodedImm), immBitWidth));
                    }
                    else
                    {
                        currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, immBitWidth - 1));
                    }
                    break;
                case 'u':
                    /* unsigned constant */
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
                    currentBuffer.append(fmt_par);
                    boolean first = true;
                    for (int i = 0; i < 8; ++i)
                    {
                        if ((decodedImm & (1 << i)) != 0)
                        {
                            if (first)
                                first = false;
                            else
                                currentBuffer.append(",");

                            if ((decodedImm & 0x100) != 0)
                                currentBuffer.append(TxCPUState.REG_LABEL[c + 7 - i]);
                            else
                                currentBuffer.append(TxCPUState.REG_LABEL[c + i]);
                        }
                    }
                    currentBuffer.append(fmt_ens);
                    break;
                default:
                    currentBuffer.append(formatChar);
                    break;
            }
        }


//        int r = TxCPUState.NOREG;
//        int dflags = 0;
//        for (char s : instruction.action.toCharArray())
//        {
//            switch (s)
//            {
//                case '!':
//                    /* jump */
//                    dflags |= DF_FLOW | DF_BREAK | DF_BRANCH;
//                    break;
//                case '?':
//                    /* branch */
//                    dflags |= DF_FLOW | DF_BRANCH;
//                    break;
//                case '(':
//                    /* call */
//                    dflags |= DF_FLOW | DF_CALL;
//                    //System.err.println("CALL {0:X8} {1:x8}", displayx, displayRegisterBuffer.pc);
//                    break;
//                case ')':
//                    /* return */
//                    dflags |= DF_FLOW | DF_BREAK | DF_CALL;
//                    break;
//                case '_':
//                    /* delay */
//                    dflags |= DF_DELAY;
//                    break;
//                case 'i':
//                    r = decodedRs;
//                    break;
//                case 'j':
//                    r = decodedRt;
//                    break;
//                case 'w':
//                    if (updateRegisters) {
//                        cpuState.setRegisterUndefined(r);
//                    }
//                    break;
//                case 'v':
//                    if (updateRegisters && cpuState.registerExists(r))
//                    {
//                        cpuState.setRegisterDefined(r);
//                        cpuState.setReg(r, decodedImm);
//                    }
//                    break;
//                case 'x':
//                    r = TxCPUState.NOREG;
//                    break;
//                default:
//                    System.err.println("bad action '" + s + "' at " + Format.asHex(cpuState.pc, 8));
//                    break;
//            }
//        }
//
//        flags |= dflags & DF_TO_KEEP;
//        cpuState.flags |= dflags & DF_TO_COPY;
//        if ((dflags & DF_DELAY) != 0)
//            cpuState.flags |= dflags & DF_TO_DELAY;
//        else
//            flags |= dflags & DF_TO_DELAY;

        /*XXX*/
        operandString = operandBuffer.toString();

        commentString = commentBuffer.toString();
    }


    /**
     * Simple and fast version used by realtime disassembly trace
     */
    public String toString() {
        String out = Format.asHex(binaryStatement, 8);

        if ((flags & DF_DELAY) != 0) {
            out += "               " + StringUtils.rightPad(((TxInstruction)instruction).name, 6) + " " + getOperandString();
        }
        else {
            out += "              " + StringUtils.rightPad(((TxInstruction)instruction).name, 7) + " " + getOperandString();
        }

        if (StringUtils.isNotBlank(commentString)) {
            out += StringUtils.leftPad("; " + commentString, 22);
        }
        out += "\n";
        if ((flags & DF_BREAK) != 0) {
            out += "\n";
        }
        return out;
    }


    /**
     * Full fledged version used for offline disassembly
     * @param options
     * @return
     */
    public String toString(Set<OutputOption> options) {
        String out = "";
        if (options.contains(OutputOption.HEXCODE)) {
            out += Format.asHex(binaryStatement, 8);
        }

        if (options.contains(OutputOption.BLANKS)) {
            out += "        ";
        }


        if (instruction != null) {
            if ((flags & DF_DELAY) != 0) {
                out += "  " + StringUtils.rightPad(((TxInstruction)instruction).name, 6) + " " + operandString;
            }
            else {
                out += " " + StringUtils.rightPad(((TxInstruction)instruction).name, 7) + " " + operandString;
            }
        }
        else {
            out += " (?)     " + operandString;
        }
        
        if (StringUtils.isNotBlank(commentString)) {
            out += StringUtils.leftPad("; " + commentString, 22);
        }
        out += "\n";
        if ((flags & DF_BREAK) != 0) {
            out += "\n";
        }
        return out;
    }
}
