package com.nikonhacker.dfr;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Set;

public class DisassembledInstruction {
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

    /** decoded opcode */
    public OpCode opcode = null;

    /** cached CPUState, for CALLs and INTs */
    public CPUState cpuState = null;

    /** data read */
    public int[] data = new int[3];

    /** number of used elements in data[]*/
    public int n;

    /** Ri/Rs operand */
    public int i; // as-is from instruction
    public int decodedI; // interpreted

    /** Rj operand */
    public int j; // as-is from instruction
    public int decodedJ; // interpreted

    /** coprocessor operation (not implemented yet in operand parsing, only for display) */
    public int c;

    /** direct operand */
    public int x; // as-is from instruction
    public int decodedX; // interpreted


    /** number of significant bits in decodedX (for display only) */
    public int xBitWidth;

    /** flags (for display only) */
    public int flags;

    /** formatted operand list */
    public String operands;

    /** optional comment */
    public String comment;
    
    /** start of decoded memory block (used only for display in "v"ector format */
    public int memRangeStart = 0;

    /**
     * Default instruction decoding upon class loading
     */
    static {
        initFormatChars(EnumSet.noneOf(OutputOption.class));
    }

    public DisassembledInstruction() {
        reset();
    }

    public DisassembledInstruction(int memRangeStart) {
        this.memRangeStart = memRangeStart;
        reset();
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

    public void decodeInstructionOperands(int pc, Memory memory) {
        switch (opcode.instructionFormat)
        {
            case OpCode.FORMAT_A:
                i = 0xF & data[0];
                j = 0xF & (data[0] >> 4);
                break;
            case OpCode.FORMAT_B:
                i = 0xF & data[0];
                x = 0xFF & (data[0] >> 4);
                xBitWidth = 8;
                break;
            case OpCode.FORMAT_C:
                i = 0xF & data[0];
                x = 0xF & (data[0] >> 4);
                xBitWidth = 4;
                break;
            case OpCode.FORMAT_D:
                x = 0xFF & data[0];
                xBitWidth = 8;
                break;
            case OpCode.FORMAT_E:
                i = 0xF & data[0];
                break;
            case OpCode.FORMAT_F:
                x = 0x7FF & data[0];
                xBitWidth = 11;
                break;
            case OpCode.FORMAT_Z:
                j = 0xF & (data[0] >> 4);
                break;
            case OpCode.FORMAT_W:
                x = data[0];
                xBitWidth = 16;
                break;
        }

        for (int ii = 0; ii < opcode.numberExtraXWords; ii++) {
            getNextInstruction(memory, pc);
            x = (x << 16) + data[n - 1];
            xBitWidth += 16;
        }

        for (int ii = 0; ii < opcode.numberExtraYWords; ii++) {
            /* coprocessor extension word */
            getNextInstruction(memory, pc);
            int tmp = data[n - 1];
            x = i;
            xBitWidth = 4;
            c = 0xFF & (tmp >> 8);
            j = 0x0F & (tmp >> 4);
            i = 0x0F & (tmp);
        }
    }

    public void reset() {
        flags = 0;
        data[0] = data[1] = data[2] = 0xDEAD;
        n = 0;
        xBitWidth = 0;
        c = 0;
        i = CPUState.NOREG;
        j = CPUState.NOREG;
        x = 0;
        operands = null;
        comment = null;
    }

    public void getNextData(Memory memory, int address)
    {
        data[n] = memory.loadUnsigned16(address + 2 * n);
        n++;
    }

    public void getNextInstruction(Memory memory, int address)
    {
        data[n] = memory.loadInstruction16(address + 2 * n);
        n++;
    }

    /**
     * Disassemble OpCode for presentation
     * must be called after Dfr.decodeInstructionOperands()
     * @param cpuState This stores CPU state.
     * @param updateRegisters if true, cpuState registers will be updated during action interpretation.
     * @return the direct argument (x), after decoding (shifts, relative, ...)
     */
    public void formatOperandsAndComment(CPUState cpuState, boolean updateRegisters, Set<OutputOption> outputOptions) {
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

        for (char formatChar : opcode.displayFormat.toCharArray())
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
                case 'A':
                    currentBuffer.append(CPUState.REG_LABEL[CPUState.AC]);
                    break;
                case 'C':
                    currentBuffer.append(CPUState.REG_LABEL[CPUState.CCR]);
                    break;
                case 'F':
                    currentBuffer.append(CPUState.REG_LABEL[CPUState.FP]);
                    break;
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
                case 'P':
                    currentBuffer.append(CPUState.REG_LABEL[CPUState.PS]);
                    break;
                case 'S':
                    currentBuffer.append(CPUState.REG_LABEL[CPUState.SP]);
                    break;
                case 'T':
                    currentBuffer.append("INT");
                    break;
                case 'X':
                case 'Y':
                    throw new RuntimeException("no more X or Y : operand parsing is now done in decodeInstructionOperands()");
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
                case 'g':
                    decodedI += CPUState.DEDICATED_REG_OFFSET;
                    currentBuffer.append(CPUState.REG_LABEL[decodedI]);
                    break;
                case 'h':
                    decodedJ += CPUState.DEDICATED_REG_OFFSET;
                    currentBuffer.append(CPUState.REG_LABEL[decodedJ]);
                    break;
                case 'i':
                    currentBuffer.append(CPUState.REG_LABEL[decodedI]);
                    break;
                case 'j':
                    currentBuffer.append(CPUState.REG_LABEL[decodedJ]);
                    break;
                case 'k':
                    decodedI += CPUState.COPROCESSOR_REG_OFFSET;
                    currentBuffer.append(decodedI);
                    break;
                case 'l':
                    decodedJ += CPUState.COPROCESSOR_REG_OFFSET;
                    currentBuffer.append(decodedJ);
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
                                currentBuffer.append(CPUState.REG_LABEL[c + 7 - i]);
                            else
                                currentBuffer.append(CPUState.REG_LABEL[c + i]);
                        }
                    }
                    currentBuffer.append(fmt_ens);
                    break;
                default:
                    currentBuffer.append(formatChar);
                    break;
            }
        }


        int r = CPUState.NOREG;
        int dflags = 0;
        for (char s : opcode.action.toCharArray())
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
                case 'A':
                    r = CPUState.AC;
                    break;
                case 'C':
                    r = CPUState.CCR;
                    break;
                case 'F':
                    r = CPUState.FP;
                    break;
                case 'P':
                    r = CPUState.PS;
                    break;
                case 'S':
                    r = CPUState.SP;
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
                    r = CPUState.NOREG;
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
        operands = operandBuffer.toString();

        comment = commentBuffer.toString();
    }


    /**
     * Simple and fast version used by realtime disassembly trace
     */
    public String toString() {
        String out = formatDataAsHex();

        if ((flags & DF_DELAY) != 0) {
            out += "               " + StringUtils.rightPad(opcode.name, 6) + " " + operands;
        }
        else {
            out += "              " + StringUtils.rightPad(opcode.name, 7) + " " + operands;
        }

        if (StringUtils.isNotBlank(comment)) {
            out += StringUtils.leftPad("; " + comment, 22);
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
            out += formatDataAsHex();
        }

        if (options.contains(OutputOption.BLANKS)) {
            out += "              ";
        }


        if (opcode != null) {
            if ((flags & DF_DELAY) != 0) {
                out += "  " + StringUtils.rightPad(opcode.name, 6) + " " + operands;
            }
            else {
                out += " " + StringUtils.rightPad(opcode.name, 7) + " " + operands;
            }
        }
        else {
            out += " (no opcode)" + operands;
        }
        
//        for (int i = 0; i < 15-operands.length(); i++) {
//            out += " ";
//        }

        if (StringUtils.isNotBlank(comment)) {
            out += StringUtils.leftPad("; " + comment, 22);
        }
        out += "\n";
        if ((flags & DF_BREAK) != 0) {
            out += "\n";
        }
        return out;
    }

    public String formatDataAsHex() {
        String out = "";
        for (int i = 0; i < 3; ++i) {
            if (i < n) {
                out += " " + Format.asHex(data[i], 4);
            }
            else {
                out += "     ";
            }
        }
        return out;
    }

}
