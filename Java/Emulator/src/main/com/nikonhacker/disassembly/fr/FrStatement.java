package com.nikonhacker.disassembly.fr;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.Instruction;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.Statement;
import com.nikonhacker.emu.memory.Memory;

import java.util.EnumSet;
import java.util.Set;

/*
 * Statement : an instance of a specific Instruction with specific operands
 */
public class FrStatement extends Statement {
    ///* output formatting */
    public static String fmt_nxt;
    public static String fmt_imm;
    public static String fmt_and;
    public static String fmt_inc;
    public static String fmt_dec;
    public static String fmt_mem;
    public static String fmt_par;
    public static String fmt_ens;

    /** data read */
    public int[] data = new int[3];

    /** number of used elements in data[]*/
    public int n;

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

    /** start of decoded memory block (used only for display in "v"ector format */
    public int memRangeStart = 0;

    /**
     * Default decoding upon class loading
     */
    static {
        initFormatChars(EnumSet.noneOf(OutputOption.class));
    }

    public FrStatement() {
        reset();
    }

    public FrStatement(int memRangeStart) {
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

    public void decodeOperands(int pc, Memory memory) {
        switch (((FrInstruction) getInstruction()).instructionFormat)
        {
            case FrInstructionSet.FORMAT_A:
                i = 0xF & data[0];
                j = 0xF & (data[0] >> 4);
                break;
            case FrInstructionSet.FORMAT_B:
                i = 0xF & data[0];
                x = 0xFF & (data[0] >> 4);
                xBitWidth = 8;
                break;
            case FrInstructionSet.FORMAT_C:
                i = 0xF & data[0];
                x = 0xF & (data[0] >> 4);
                xBitWidth = 4;
                break;
            case FrInstructionSet.FORMAT_D:
                x = 0xFF & data[0];
                xBitWidth = 8;
                break;
            case FrInstructionSet.FORMAT_E:
                i = 0xF & data[0];
                break;
            case FrInstructionSet.FORMAT_F:
                x = 0x7FF & data[0];
                xBitWidth = 11;
                break;
            case FrInstructionSet.FORMAT_Z:
                j = 0xF & (data[0] >> 4);
                break;
            case FrInstructionSet.FORMAT_W:
                x = data[0];
                xBitWidth = 16;
                break;
        }

        for (int ii = 0; ii < ((FrInstruction) getInstruction()).numberExtraXWords; ii++) {
            getNextStatement(memory, pc);
            x = (x << 16) + data[n - 1];
            xBitWidth += 16;
        }

        for (int ii = 0; ii < ((FrInstruction) getInstruction()).numberExtraYWords; ii++) {
            /* coprocessor extension word */
            getNextStatement(memory, pc);
            int tmp = data[n - 1];
            x = i;
            xBitWidth = 4;
            c = 0xFF & (tmp >> 8);
            j = 0x0F & (tmp >> 4);
            i = 0x0F & (tmp);
        }
    }

    public void reset() {
        data[0] = data[1] = data[2] = 0xDEAD;
        n = 0;
        xBitWidth = 0;
        c = 0;
        i = CPUState.NOREG;
        j = CPUState.NOREG;
        x = 0;
        setOperandString(null);
        setCommentString(null);
    }

    public void getNextData(Memory memory, int address)
    {
        data[n] = memory.loadUnsigned16(address + 2 * n);
        n++;
    }

    public void getNextStatement(Memory memory, int address)
    {
        data[n] = memory.loadInstruction16(address + 2 * n);
        n++;
    }

    /**
     * Disassemble FrInstruction for presentation
     * must be called after decodeOperands()
     * @param cpuState This stores CPU state.
     * @param updateRegisters if true, cpuState registers will be updated during action interpretation.
     */
    public void formatOperandsAndComment(CPUState cpuState, boolean updateRegisters, Set<OutputOption> outputOptions) {

        /* DISPLAY FORMAT processing */

        int tmp;
        int pos;

        decodedX = x;
        decodedI = i;
        decodedJ = j;

        StringBuilder operandBuffer = new StringBuilder();
        StringBuilder commentBuffer = new StringBuilder();

        StringBuilder currentBuffer = operandBuffer;

        for (char formatChar : getInstruction().getDisplayFormat().toCharArray())
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
                    currentBuffer.append(FrCPUState.REG_LABEL[FrCPUState.AC]);
                    break;
                case 'C':
                    currentBuffer.append(FrCPUState.REG_LABEL[FrCPUState.CCR]);
                    break;
                case 'F':
                    currentBuffer.append(FrCPUState.REG_LABEL[FrCPUState.FP]);
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
                    currentBuffer.append(FrCPUState.REG_LABEL[FrCPUState.PS]);
                    break;
                case 'S':
                    currentBuffer.append(FrCPUState.REG_LABEL[FrCPUState.SP]);
                    break;
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
                case 'g':
                    decodedI += FrCPUState.DEDICATED_REG_OFFSET;
                    currentBuffer.append(FrCPUState.REG_LABEL[decodedI]);
                    break;
                case 'h':
                    decodedJ += FrCPUState.DEDICATED_REG_OFFSET;
                    currentBuffer.append(FrCPUState.REG_LABEL[decodedJ]);
                    break;
                case 'i':
                    currentBuffer.append(FrCPUState.REG_LABEL[decodedI]);
                    break;
                case 'j':
                    currentBuffer.append(FrCPUState.REG_LABEL[decodedJ]);
                    break;
                case 'k':
                    decodedI += FrCPUState.COPROCESSOR_REG_OFFSET;
                    currentBuffer.append(decodedI);
                    break;
                case 'l':
                    decodedJ += FrCPUState.COPROCESSOR_REG_OFFSET;
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
                                currentBuffer.append(FrCPUState.REG_LABEL[c + 7 - i]);
                            else
                                currentBuffer.append(FrCPUState.REG_LABEL[c + i]);
                        }
                    }
                    currentBuffer.append(fmt_ens);
                    break;
                default:
                    currentBuffer.append(formatChar);
                    break;
            }
        }

        setOperandString(operandBuffer.toString());

        setCommentString(commentBuffer.toString());


        /* ACTION processing */

        int r = FrCPUState.NOREG;

        for (char s : instruction.getAction().toCharArray())
        {
            switch (s)
            {
                case 'A':
                    r = FrCPUState.AC;
                    break;
                case 'C':
                    r = FrCPUState.CCR;
                    break;
                case 'F':
                    r = FrCPUState.FP;
                    break;
                case 'P':
                    r = FrCPUState.PS;
                    break;
                case 'S':
                    r = FrCPUState.SP;
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
                    if (updateRegisters && cpuState.registerExists(r)) {
                        cpuState.setRegisterDefined(r);
                        cpuState.setReg(r, decodedX);
                    }
                    break;
                case 'x':
                    r = FrCPUState.NOREG;
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
