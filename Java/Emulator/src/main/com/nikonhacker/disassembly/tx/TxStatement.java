package com.nikonhacker.disassembly.tx;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
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

    /** decoded instruction */
    public TxInstruction instruction = null;

    /** cached CPUState, for CALLs and INTs */
    public TxCPUState cpuState = null;

    /** Ri/Rs operand */
    public int i; // as-is from binary code
    public int decodedI; // interpreted

    /** Rj operand */
    public int j; // as-is from binary code
    public int decodedJ; // interpreted

    /** coprocessor operation (not implemented yet in operand parsing, only for display) */
    public int c;

    /** direct operand */
    public int x; // as-is from binary code
    public int decodedX; // interpreted


    /** number of significant bits in decodedX (for display only) */
    public int xBitWidth;

    /** flags (for display only) */
    public int flags;

    /** start of decoded memory block (used only for display in "v"ector format */
    public int memRangeStart = 0;

    private int binaryStatement;

    /** Rs, Rt, Rd, shamt for R format. Rs, Rt, Imm for I format. Target for J format */
    int[] operands = new int[4];
    private int numOperands = 0;

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
        switch (instruction.getInstructionFormat())
        {
            case R:
                numOperands=4;
                operands[0] = (binaryStatement >>> 21) & 0b11111;
                operands[1] = (binaryStatement >>> 16) & 0b11111;
                operands[2] = (binaryStatement >>> 11) & 0b11111;
                operands[3] = (binaryStatement >>>  6) & 0b11111;
                break;
            case I:
            case I_BRANCH:
                numOperands = 3;
                operands[0] = (binaryStatement >>> 21) & 0b11111;
                operands[1] = (binaryStatement >>> 16) & 0b11111;
                operands[2] =  binaryStatement         & 0xFFFF;
                break;
            case J:
                numOperands = 1;
                operands[0] =  binaryStatement         & 0x03FFFFFF;
                break;
            case BREAK:
                numOperands = 1;
                operands[0] = (binaryStatement >>> 6)  & 0x000FFFFF;
                break;
            case TRAP:
                numOperands = 3;
                operands[0] = (binaryStatement >>> 21) & 0b11111;
                operands[1] = (binaryStatement >>> 16) & 0b11111;
                operands[2] = (binaryStatement >>>  6) & 0x3FF;
                break;
        }
    }

    public void reset() {
        flags = 0;
        xBitWidth = 0;
        c = 0;
        i = TxCPUState.NOREG;
        j = TxCPUState.NOREG;
        x = 0;
        setOperandString(null);
        setCommentString(null);
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
     * @return the direct argument (x), after decoding (shifts, relative, ...)
     */
    public void formatOperandsAndComment(TxCPUState cpuState, boolean updateRegisters, Set<OutputOption> outputOptions) {
        int tmp;
        int pos;

        decodedX = x;
        decodedI = i;
        decodedJ = j;

        StringBuilder operandBuffer = new StringBuilder();
        StringBuilder commentBuffer = new StringBuilder();

        StringBuilder currentBuffer = operandBuffer;

        flags = cpuState.flags;
        cpuState.flags = 0;

        for (char formatChar : instruction.displayFormat.toCharArray())
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
                    decodedX <<= 1;
                    xBitWidth += 1;
                    break;
                case '4':
                    decodedX <<= 2;
                    xBitWidth += 2;
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
                    if (cpuState.isRegisterDefined(decodedJ))
                    {
                        decodedX = cpuState.getReg(decodedJ);
                        xBitWidth = 32;
                    }
                    else
                    {
                        decodedX = 0;
                        xBitWidth = 0;
                    }
                    break;
                case 'I':
                    if (cpuState.isRegisterDefined(decodedI))
                    {
                        decodedX = cpuState.getReg(decodedI);
                        xBitWidth = 32;
                    }
                    else
                    {
                        decodedX = 0;
                        xBitWidth = 0;
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
                    pos = xBitWidth;
                    while (pos >= 8){
                        pos -= 8;
                        currentBuffer.append(Format.asAscii(decodedX >> pos));
                    }
                    break;
                case 'b':
                    /* shift2 */
                    decodedX += 16;
                    xBitWidth += 1;
                    break;
                case 'c':
                    /* coprocessor operation */
                    currentBuffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(c, 2));
                    break;
                case 'd':
                    /* unsigned decimal */
                    currentBuffer.append(decodedX);
                    break;
                case 'f':
                    pos = xBitWidth >> 1;

                    tmp = (int)(((1L << pos) - 1) & (decodedX >> pos));
                    int tmq = (int)(((1L << pos) - 1) & decodedX);
                    if (tmq != 0)
                        currentBuffer.append(((double)tmp) / tmq);
                    else
                        currentBuffer.append("NaN");

                    break;
                case 'i':
                    currentBuffer.append(TxCPUState.REG_LABEL[decodedI]);
                    break;
                case 'j':
                    currentBuffer.append(TxCPUState.REG_LABEL[decodedJ]);
                    break;
                case 'n':
                    /* negative constant */
                    //opnd.append(hexPrefix + Format.asHexInBitsLength(dp.displayx, dp.w + 1));
                    currentBuffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << (xBitWidth + 1)) - 1) & BinaryArithmetics.NEG(xBitWidth, (1 << (xBitWidth)) | decodedX), xBitWidth + 1));
                    break;
                case 'p':
                    /* pair */
                    pos = xBitWidth >> 1;
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << pos) - 1) & (decodedX >> pos), pos));
                    currentBuffer.append(fmt_nxt);
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << pos) - 1) & decodedX, pos));
                    break;
                case 'q':
                    /* rational */
                    pos = xBitWidth >> 1;
                    currentBuffer.append(((1L << pos) - 1) & (decodedX >> pos));
                    currentBuffer.append("/");
                    currentBuffer.append(((1L << pos) - 1) & decodedX);
                    break;
                case 'r':
                    /* relative */
                    decodedX = cpuState.pc + 2 + BinaryArithmetics.signExtend(xBitWidth, decodedX);
                    xBitWidth = 32;
                    break;
                case 's':
                    /* signed constant */
                    if (BinaryArithmetics.IsNeg(xBitWidth, decodedX))
                    {
                        /* avoid "a+-b" : remove the last "+" so that output is "a-b" */
                        if (outputOptions.contains(OutputOption.CSTYLE) && (currentBuffer.charAt(currentBuffer.length() - 1) == '+')) {
                            currentBuffer.delete(currentBuffer.length() - 1, currentBuffer.length() - 1);
                        }
                        currentBuffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), BinaryArithmetics.NEG(xBitWidth, decodedX), xBitWidth));
                    }
                    else
                    {
                        currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedX, xBitWidth - 1));
                    }
                    break;
                case 'u':
                    /* unsigned constant */
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedX, xBitWidth));
                    break;
                case 'v':
                    /* vector */
                    currentBuffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(0xFF - (0xFF & ((cpuState.pc - memRangeStart) / 4)), 1));
                    break;
                case 'x':
                    decodedX |= 0x100;
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
                        if ((decodedX & (1 << i)) != 0)
                        {
                            if (first)
                                first = false;
                            else
                                currentBuffer.append(",");

                            if ((decodedX & 0x100) != 0)
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


        int r = TxCPUState.NOREG;
        int dflags = 0;
        for (char s : instruction.action.toCharArray())
        {
            switch (s)
            {
                case '!':
                    /* jump */
                    dflags |= DF_FLOW | DF_BREAK | DF_BRANCH;
                    break;
                case '?':
                    /* branch */
                    dflags |= DF_FLOW | DF_BRANCH;
                    break;
                case '(':
                    /* call */
                    dflags |= DF_FLOW | DF_CALL;
                    //System.err.println("CALL {0:X8} {1:x8}", displayx, displayRegisterBuffer.pc);
                    break;
                case ')':
                    /* return */
                    dflags |= DF_FLOW | DF_BREAK | DF_CALL;
                    break;
                case '_':
                    /* delay */
                    dflags |= DF_DELAY;
                    break;
                case 'i':
                    r = decodedI;
                    break;
                case 'j':
                    r = decodedJ;
                    break;
                case 'w':
                    if (updateRegisters) {
                        cpuState.setRegisterUndefined(r);
                    }
                    break;
                case 'v':
                    if (updateRegisters && cpuState.registerExists(r))
                    {
                        cpuState.setRegisterDefined(r);
                        cpuState.setReg(r, decodedX);
                    }
                    break;
                case 'x':
                    r = TxCPUState.NOREG;
                    break;
                default:
                    System.err.println("bad action '" + s + "' at " + Format.asHex(cpuState.pc, 8));
                    break;
            }
        }

        flags |= dflags & DF_TO_KEEP;
        cpuState.flags |= dflags & DF_TO_COPY;
        if ((dflags & DF_DELAY) != 0)
            cpuState.flags |= dflags & DF_TO_DELAY;
        else
            flags |= dflags & DF_TO_DELAY;

        /*XXX*/
        setOperandString(operandBuffer.toString());

        setCommentString(commentBuffer.toString());
    }


    /**
     * Simple and fast version used by realtime disassembly trace
     */
    public String toString() {
        String out = Format.asHex(binaryStatement, 8);

        if ((flags & DF_DELAY) != 0) {
            out += "               " + StringUtils.rightPad(instruction.name, 6) + " " + getOperandString();
        }
        else {
            out += "              " + StringUtils.rightPad(instruction.name, 7) + " " + getOperandString();
        }

        if (StringUtils.isNotBlank(getCommentString())) {
            out += StringUtils.leftPad("; " + getCommentString(), 22);
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
                out += "  " + StringUtils.rightPad(instruction.name, 6) + " " + getOperandString();
            }
            else {
                out += " " + StringUtils.rightPad(instruction.name, 7) + " " + getOperandString();
            }
        }
        else {
            out += " (no instruction)" + getOperandString();
        }
        
        if (StringUtils.isNotBlank(getCommentString())) {
            out += StringUtils.leftPad("; " + getCommentString(), 22);
        }
        out += "\n";
        if ((flags & DF_BREAK) != 0) {
            out += "\n";
        }
        return out;
    }
}
