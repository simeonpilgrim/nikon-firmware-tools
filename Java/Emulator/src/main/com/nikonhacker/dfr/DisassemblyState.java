package com.nikonhacker.dfr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

public class DisassemblyState {
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
    /** decoded opcode */
    public OpCode opcode = null;

    /** data read */
    public int[] data = new int[3];

    /** number of used elements in data[]*/
    public int n;

    /** Ri/Rs operand */
    public int i;

    /** Rj operand */
    public int j;

    /** coprocessor operation (not implemented yet in operand parsing, only for display) */
    public int c;

    /** constant operand */
    public int x;


    /** number of significant bits in x (for display only) */
    public int w;

    /** flags (for display only) */
    public int flags;

    /** formatted operand list */
    public String operands;

    /** optional comment */
    public String comment;
    
    /** register buffers (for display only) */
    public Integer registerBuffer[] = new Integer[49];

    /** start of decoded memory block (used only for display in "v"ector format */
    public int memRangeStart = 0;

    public DisassemblyState() {
        reset();
    }

    public DisassemblyState(int memRangeStart) {
        this.memRangeStart = memRangeStart;
        reset();
    }

    public void decodeInstructionOperands(CPUState cpuState, Memory memory) {
        switch (opcode.instructionFormat)
        {
            case OpCode.FORMAT_A:
                i = 0xF & data[0];
                j = 0xF & (data[0] >> 4);
                break;
            case OpCode.FORMAT_B:
                i = 0xF & data[0];
                x = 0xFF & (data[0] >> 4);
                w = 8;
                break;
            case OpCode.FORMAT_C:
                i = 0xF & data[0];
                x = 0xF & (data[0] >> 4);
                w = 4;
                break;
            case OpCode.FORMAT_D:
                x = 0xFF & data[0];
                w = 8;
                break;
            case OpCode.FORMAT_E:
                i = 0xF & data[0];
                break;
            case OpCode.FORMAT_F:
                x = 0x7FF & data[0];
                w = 11;
                break;
            case OpCode.FORMAT_Z:
                j = 0xF & (data[0] >> 4);
                break;
            case OpCode.FORMAT_W:
                x = data[0];
                w = 16;
                break;
        }

        for (int ii = 0; ii < opcode.numberExtraXWords; ii++) {
            getNextInstruction(memory, cpuState.pc);
            x = (x << 16) + data[n - 1];
            w += 16;
        }

        for (int ii = 0; ii < opcode.numberExtraYWords; ii++) {
            /* coprocessor extension word */
            getNextInstruction(memory, cpuState.pc);
            int tmp = data[n - 1];
            x = i;
            w = 4;
            c = 0xFF & (tmp >> 8);
            j = 0x0F & (tmp >> 4);
            i = 0x0F & (tmp);
        }
    }

    public void reset() {
        flags = 0;
        data[0] = data[1] = data[2] = 0xDEAD;
        n = 0;
        w = 0;
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
     *                        must be true in disassembler mode and false in emulator mode
     */
    public void formatOperandsAndComment(CPUState cpuState, boolean updateRegisters, Set<OutputOption> outputOptions) {
        int tmp;
        int pos;
        int displayX = x;
        int displayI = i;
        int displayJ = j;

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
                    currentBuffer.append(Dfr.fmt_imm);
                    break;
                case '&':
                    currentBuffer.append(Dfr.fmt_and);
                    break;
                case '(':
                    currentBuffer.append(Dfr.fmt_par);
                    break;
                case ')':
                    currentBuffer.append(Dfr.fmt_ens);
                    break;
                case '+':
                    currentBuffer.append(Dfr.fmt_inc);
                    break;
                case ',':
                    currentBuffer.append(Dfr.fmt_nxt);
                    break;
                case '-':
                    currentBuffer.append(Dfr.fmt_dec);
                    break;
                case ';':
                    currentBuffer = commentBuffer;
                    break;
                case '@':
                    currentBuffer.append(Dfr.fmt_mem);
                    break;
                case '2':
                    displayX <<= 1;
                    w += 1;
                    break;
                case '4':
                    displayX <<= 2;
                    w += 2;
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
                    if (cpuState.isRegisterValid(displayJ))
                    {
                        displayX = cpuState.getReg(displayJ);
                        w = 32;
                    }
                    else
                    {
                        displayX = 0;
                        w = 0;
                    }
                    break;
                case 'I':
                    if (cpuState.isRegisterValid(displayI))
                    {
                        displayX = cpuState.getReg(displayI);
                        w = 32;
                    }
                    else
                    {
                        displayX = 0;
                        w = 0;
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
                case 'X':
                case 'Y':
                    throw new RuntimeException("no more X or Y : operand parsing is now done in decodeInstructionOperands()");
                case 'a':
                    pos = w;
                    while (pos >= 8){
                        pos -= 8;
                        currentBuffer.append(Format.asAscii(displayX >> pos));
                    }
                    break;
                case 'b':
                    /* shift2 */
                    displayX += 16;
                    w += 1;
                    break;
                case 'c':
                    /* coprocessor operation */
                    currentBuffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(c, 2));
                    break;
                case 'd':
                    /* unsigned decimal */
                    currentBuffer.append(displayX);
                    break;
                case 'f':
                    pos = w >> 1;

                    tmp = (int)(((1L << pos) - 1) & (displayX >> pos));
                    int tmq = (int)(((1L << pos) - 1) & displayX);
                    if (tmq != 0)
                        currentBuffer.append(((double)tmp) / tmq);
                    else
                        currentBuffer.append("NaN");

                    break;
                case 'g':
                    displayI += CPUState.DEDICATED_REG_OFFSET;
                    currentBuffer.append(CPUState.REG_LABEL[displayI]);
                    break;
                case 'h':
                    displayJ += CPUState.DEDICATED_REG_OFFSET;
                    currentBuffer.append(CPUState.REG_LABEL[displayJ]);
                    break;
                case 'i':
                    currentBuffer.append(CPUState.REG_LABEL[displayI]);
                    break;
                case 'j':
                    currentBuffer.append(CPUState.REG_LABEL[displayJ]);
                    break;
                case 'k':
                    displayI += CPUState.COPROCESSOR_REG_OFFSET;
                    currentBuffer.append(displayI);
                    break;
                case 'l':
                    displayJ += CPUState.COPROCESSOR_REG_OFFSET;
                    currentBuffer.append(displayJ);
                    break;
                case 'n':
                    /* negative constant */
                    //opnd.append(hexPrefix + Format.asHexInBitsLength(dp.displayx, dp.w + 1));
                    currentBuffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << (w + 1)) - 1) & Dfr.NEG(w, (1 << (w)) | displayX), w + 1));
                    break;
                case 'p':
                    /* pair */
                    pos = w >> 1;
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << pos) - 1) & (displayX >> pos), pos));
                    currentBuffer.append(Dfr.fmt_nxt);
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), ((1 << pos) - 1) & displayX, pos));
                    break;
                case 'q':
                    /* rational */
                    pos = w >> 1;
                    currentBuffer.append(((1L << pos) - 1) & (displayX >> pos));
                    currentBuffer.append("/");
                    currentBuffer.append(((1L << pos) - 1) & displayX);
                    break;
                case 'r':
                    /* relative */
                    displayX = cpuState.pc + 2 + Dfr.signExtend(w, displayX);
                    w = 32;
                    break;
                case 's':
                    /* signed constant */
                    if (Dfr.IsNeg(w, displayX))
                    {
                        /* avoid "a+-b" : remove the last "+" so that output is "a-b" */
                        if (outputOptions.contains(OutputOption.CSTYLE) && (currentBuffer.charAt(currentBuffer.length() - 1) == '+')) {
                            currentBuffer.delete(currentBuffer.length() - 1, currentBuffer.length() - 1);
                        }
                        currentBuffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), Dfr.NEG(w, displayX), w));
                    }
                    else
                    {
                        currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), displayX, w - 1));
                    }
                    break;
                case 'u':
                    /* unsigned constant */
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), displayX, w));
                    break;
                case 'v':
                    /* vector */
                    currentBuffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(0xFF - (0xFF & ((cpuState.pc - memRangeStart) / 4)), 1));
                    break;
                case 'x':
                    displayX |= 0x100;
                    break;
                case 'y':
                    c += 8;
                    // goto case 'z'; /*FALLTHROUGH*/
                case 'z':
                    /* register list */
                    currentBuffer.append(Dfr.fmt_par);
                    boolean first = true;
                    for (int i = 0; i < 8; ++i)
                    {
                        if ((displayX & (1 << i)) != 0)
                        {
                            if (first)
                                first = false;
                            else
                                currentBuffer.append(",");

                            if ((displayX & 0x100) != 0)
                                currentBuffer.append(CPUState.REG_LABEL[c + 7 - i]);
                            else
                                currentBuffer.append(CPUState.REG_LABEL[c + i]);
                        }
                    }
                    currentBuffer.append(Dfr.fmt_ens);
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
                    r = displayI;
                    break;
                case 'j':
                    r = displayJ;
                    break;
                case 'w':
                    if (updateRegisters) {
                        cpuState.setRegisterInvalid(r);
                    }
                    break;
                case 'v':
                    if (updateRegisters && cpuState.isOkRegisterNumber(r))
                    {
                        cpuState.setRegisterValid(r);
                        cpuState.setReg(r, displayX);
                    }
                    break;
                case 'x':
                    r = CPUState.NOREG;
                    break;
                default:
                    Dfr.error("bad action '" + s + "'");
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

    public String toString() {
        String out = "";

        for (int i = 0; i < 3; ++i) {
            if (i < n) {
                out += " " + Format.asHex(data[i], 4);
            }
            else {
                out += "     ";
            }
        }
        if (opcode != null) {
            if ((flags & DF_DELAY) != 0) {
                out += "                " + StringUtils.rightPad(opcode.name, 6) + " " + operands;
            }
            else {
                out += "               " + StringUtils.rightPad(opcode.name, 7) + " " + operands;
            }
        }
        else {
            out += "               (no opcode)" + operands;
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

}
