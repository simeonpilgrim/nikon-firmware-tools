package com.nikonhacker.disassembly.fr;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
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
    public int numData;

    /** Ri/Rs operand */
    public int i; // as-is from binary code
    public int decodedI; // interpreted

    /** Rj operand */
    public int j; // as-is from binary code
    public int decodedJ; // interpreted

    /** coprocessor operation (not implemented yet in operand parsing, only for display) */
    public int c;


    /** number of significant bits in decodedX (for display only) */
    public int immBitWidth;

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
            case A:
                i = 0xF & data[0];
                j = 0xF & (data[0] >> 4);
                break;
            case B:
                i = 0xF & data[0];
                imm = 0xFF & (data[0] >> 4);
                immBitWidth = 8;
                break;
            case C:
                i = 0xF & data[0];
                imm = 0xF & (data[0] >> 4);
                immBitWidth = 4;
                break;
            case D:
                imm = 0xFF & data[0];
                immBitWidth = 8;
                break;
            case E:
                i = 0xF & data[0];
                break;
            case F:
                imm = 0x7FF & data[0];
                immBitWidth = 11;
                break;
            case Z:
                j = 0xF & (data[0] >> 4);
                break;
            case W:
                imm = data[0];
                immBitWidth = 16;
                break;
        }

        for (int ii = 0; ii < ((FrInstruction) getInstruction()).numberExtraXWords; ii++) {
            getNextStatement(memory, pc);
            imm = (imm << 16) + data[numData - 1];
            immBitWidth += 16;
        }

        for (int ii = 0; ii < ((FrInstruction) getInstruction()).numberExtraYWords; ii++) {
            /* coprocessor extension word */
            getNextStatement(memory, pc);
            int tmp = data[numData - 1];
            imm = i;
            immBitWidth = 4;
            c = 0xFF & (tmp >> 8);
            j = 0x0F & (tmp >> 4);
            i = 0x0F & (tmp);
        }
    }

    public void reset() {
        data[0] = data[1] = data[2] = 0xDEAD;
        numData = 0;
        immBitWidth = 0;
        c = 0;
        i = CPUState.NOREG;
        j = CPUState.NOREG;
        imm = 0;
        setOperandString(null);
        setCommentString(null);
    }

    public void getNextData(Memory memory, int address)
    {
        data[numData] = memory.loadUnsigned16(address + 2 * numData);
        numData++;
    }

    public void getNextStatement(Memory memory, int address)
    {
        data[numData] = memory.loadInstruction16(address + 2 * numData);
        numData++;
    }

    /**
     * Disassemble FrInstruction for presentation
     * must be called after decodeOperands()
     *
     * @param context
     * @param updateRegisters if true, cpuState registers will be updated during action interpretation.
     * @see FrInstruction for a description of all possible chars
     */
    @Override
    public void formatOperandsAndComment(StatementContext context, boolean updateRegisters, Set<OutputOption> outputOptions) {

        /* DISPLAY FORMAT processing */

        int tmp;
        int pos;

        decodedImm = imm;
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
                    decodedImm <<= 1;
                    immBitWidth += 1;
                    break;
                case '4':
                    decodedImm <<= 2;
                    immBitWidth += 2;
                    break;

                case 'A':
                    currentBuffer.append(FrCPUState.registerLabels[FrCPUState.AC]);
                    break;
                case 'C':
                    currentBuffer.append(FrCPUState.registerLabels[FrCPUState.CCR]);
                    break;
                case 'F':
                    currentBuffer.append(FrCPUState.registerLabels[FrCPUState.FP]);
                    break;
                case 'M':
                    currentBuffer.append("ILM");
                    break;
                case 'P':
                    currentBuffer.append(FrCPUState.registerLabels[FrCPUState.PS]);
                    break;
                case 'S':
                    currentBuffer.append(FrCPUState.registerLabels[FrCPUState.SP]);
                    break;

                case 'I':
                    if (context.cpuState.isRegisterDefined(decodedI))
                    {
                        decodedImm = context.cpuState.getReg(decodedI);
                        immBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        immBitWidth = 0;
                    }
                    break;
                case 'J':
                    if (context.cpuState.isRegisterDefined(decodedJ))
                    {
                        decodedImm = context.cpuState.getReg(decodedJ);
                        immBitWidth = 32;
                    }
                    else
                    {
                        decodedImm = 0;
                        immBitWidth = 0;
                    }
                    break;

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
                case 'g':
                    decodedI += FrCPUState.DEDICATED_REG_OFFSET;
                    currentBuffer.append(FrCPUState.registerLabels[decodedI]);
                    break;
                case 'h':
                    decodedJ += FrCPUState.DEDICATED_REG_OFFSET;
                    currentBuffer.append(FrCPUState.registerLabels[decodedJ]);
                    break;
                case 'i':
                    currentBuffer.append(FrCPUState.registerLabels[decodedI]);
                    break;
                case 'j':
                    currentBuffer.append(FrCPUState.registerLabels[decodedJ]);
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
                    currentBuffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), -BinaryArithmetics.negativeExtend(immBitWidth, decodedImm), immBitWidth + 1));
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
                    decodedImm = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(immBitWidth, decodedImm);
                    immBitWidth = 32;
                    break;
                case 's':
                    /* signed constant */
                    if (BinaryArithmetics.isNegative(immBitWidth, decodedImm))
                    {
                        /* avoid "a+-b" : remove the last "+" so that output is "a-b" */
                        if (outputOptions.contains(OutputOption.CSTYLE) && (currentBuffer.charAt(currentBuffer.length() - 1) == '+')) {
                            currentBuffer.delete(currentBuffer.length() - 1, currentBuffer.length() - 1);
                        }
                        currentBuffer.append(Format.asHexInBitsLength("-" + (outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), BinaryArithmetics.neg(immBitWidth, decodedImm), immBitWidth));
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
                    currentBuffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(0xFF - (0xFF & ((context.cpuState.pc - memRangeStart) / 4)), 1));
                    break;
                case 'x':
                    decodedImm |= 0x100;
                    break;
                case 'y':
                    c += 8; // use high register list
                    // continue with case 'z'
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
                                currentBuffer.append(FrCPUState.registerLabels[c + 7 - i]);
                            else
                                currentBuffer.append(FrCPUState.registerLabels[c + i]);
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
                        context.cpuState.setRegisterUndefined(r);
                    }
                    break;
                case 'v':
                    if (updateRegisters && context.cpuState.registerExists(r)) {
                        context.cpuState.setRegisterDefined(r);
                        context.cpuState.setReg(r, decodedImm);
                    }
                    break;
                case 'x':
                    r = FrCPUState.NOREG;
                    break;
                default:
                    System.err.println("bad action '" + s + "' in " + instruction + " at " + Format.asHex(context.cpuState.pc, 8));
                    break;
            }
        }


        /* LINE BREAKS and INDENT (delay slot) processing */

        // Retrieve stored delay slot type to print this instruction
        setDelaySlotType(context.getStoredDelaySlotType());

        // Store the one of this instruction for printing next one
        context.setStoredDelaySlotType(instruction.getDelaySlotType());


        boolean newIsBreak = EnumSet.of(Instruction.FlowType.JMP, Instruction.FlowType.RET).contains(instruction.getFlowType());

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


    public String getFormattedBinaryStatement() {
        String out = "";
        for (int i = 0; i < 3; ++i) {
            if (i < numData) {
                out += " " + Format.asHex(data[i], 4);
            }
            else {
                out += "     ";
            }
        }
        return out;
    }

    public int getNumBytes() {
        return numData * 2;
    }

    public void fillInstruction() {
        FrInstruction instruction = FrInstructionSet.instructionMap[data[0]];

        if (instruction == null) {
            setInstruction(FrInstructionSet.opData[RangeType.Width.MD_WORD.getIndex()]);
        }
        else {
            setInstruction(instruction);
        }
    }

    public boolean isPotentialStuffing() {
        return numData == 1 && (
                   data[0] == 0x9FA0 /* 0x9FA0 : NOP stuffing */
                || data[0] == 0x0000 /* 0x0000 stuffing */ );
    }
}
