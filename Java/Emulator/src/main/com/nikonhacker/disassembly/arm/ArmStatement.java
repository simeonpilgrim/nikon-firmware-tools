package com.nikonhacker.disassembly.arm;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.emu.memory.Memory;

import java.util.EnumSet;
import java.util.Set;

/*
 * Statement : an instance of a specific Instruction with specific operands
 */
public class ArmStatement extends Statement {
    ///* output formatting */
    private static String fmt_nxt;
    private static String fmt_imm;
    private static String fmt_and;
    private static String fmt_inc;
    private static String fmt_dec;
    private static String fmt_mem;
    private static String fmt_par;
    private static String fmt_ens;

    /** data read */
    public int[] data = new int[2];

    /** number of used elements in data[]*/
    public int numData;

    /** start of decoded memory block (used only for display in "v"ector format */
    private int memRangeStart = 0;

    /** Interpreted first register operand: Ri (Fr), or rs or fs (Tx) */
    public int decodedRiRsFs;
    /** Interpreted second register operand: Rj (Fr), or rt or ft (Tx) operand */
    public int decodedRjRtFt;

    /**
     * Default decoding upon class loading
     */
    static {
        initFormatChars(EnumSet.noneOf(OutputOption.class));
    }

    public ArmStatement() {
        reset();
    }

    public ArmStatement(int memRangeStart) {
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

    // --------------- This code must be moved to FastMemoryLE class
    public final static int halfwords2word(int half0, int half1) {
        return half0+(half1<<16);
    }

    // ------------------------------
    public void decodeOperands(int pc, Memory memory) {
        switch (((ArmInstruction) getInstruction()).instructionFormat)
        {
/*
            case A:
                ri_rs_fs = 0xF & data[0];
                rj_rt_ft = 0xF & (data[0] >> 4);
                break;
            case B:
                ri_rs_fs = 0xF & data[0];
                imm = 0xFF & (data[0] >> 4);
                immBitWidth = 8;
                break;
            case C:
                ri_rs_fs = 0xF & data[0];
                imm = 0xF & (data[0] >> 4);
                immBitWidth = 4;
                break;
            case D:
                imm = 0xFF & data[0];
                immBitWidth = 8;
                break;
            case E:
                ri_rs_fs = 0xF & data[0];
                break;
            case F:
                imm = 0x7FF & data[0];
                immBitWidth = 11;
                break;
            case Z:
                rj_rt_ft = 0xF & (data[0] >> 4);
                break;
                */
            case W:
                immBitWidth = numData*16;
                if (immBitWidth>16)
                    imm = halfwords2word(data[0], data[1]);
                else
                    imm = data[0];
                break;
        }

    }

    @Override
    public void reset() {
        data[0] = data[1] = 0xDEAD;
        numData = 0;
        immBitWidth = 0;
        ri_rs_fs = CPUState.NOREG;
        rj_rt_ft = CPUState.NOREG;
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
     * Disassemble ArmInstruction for presentation
     * must be called after decodeOperands()
     *
     * @param context
     * @param updateRegisters if true, cpuState registers will be updated during action interpretation.
     * @see ArmInstruction for a description of all possible chars
     */
    @Override
    public void formatOperandsAndComment(StatementContext context, boolean updateRegisters, Set<OutputOption> outputOptions) {

        /* DISPLAY FORMAT processing */

        int tmp;
        int pos;

        boolean writeDirection = false; // for memory operations

        decodedImm = imm;
        decodedRiRsFs = ri_rs_fs;
        decodedRjRtFt = rj_rt_ft;

        StringBuilder operandBuffer = new StringBuilder();
        StringBuilder commentBuffer = new StringBuilder();

        StringBuilder currentBuffer = operandBuffer;

        for (char formatChar : getInstruction().getOperandFormat().toCharArray())
        {
            switch (formatChar)
            {
                case '#':
                    currentBuffer.append(fmt_imm);
                    break;
                case ';':
                    currentBuffer = commentBuffer;
                    break;
                case 'T':
                    currentBuffer.append("INT");
                    break;
                case 'a':
                    pos = immBitWidth;
                    while (pos >= 8){
                        pos -= 8;
                        currentBuffer.append(Format.asAscii(decodedImm >> pos));
                    }
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
                case 'u':
                    /* unsigned constant */
                    currentBuffer.append(Format.asHexInBitsLength((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x"), decodedImm, immBitWidth));
                    break;
                case 'v':
                    /* vector */
                    currentBuffer.append((outputOptions.contains(OutputOption.DOLLAR)?"$":"0x") + Format.asHex(0xFF - (0xFF & ((context.cpuState.pc - memRangeStart) / 4)), 1));
                    break;
                default:
                    currentBuffer.append(formatChar);
                    break;
            }
        }

        setOperandString(operandBuffer.toString());

        setCommentString(commentBuffer.toString());


        /* ACTION processing */

        int r = ArmCPUState.NOREG;

        for (char s : instruction.getAction().toCharArray())
        {
            switch (s)
            {
                default:
                    System.err.println("bad action '" + s + "' in " + instruction + " at " + Format.asHex(context.cpuState.pc, 8));
                    break;
            }
        }

        // ARM has no delay slot
        setDelaySlotType(Instruction.DelaySlotType.NONE);
        /* LINE BREAKS and INDENT (IF instruction) processing */

        // Retrieve stored type to print this instruction
        // TODO

        // Store the one of this instruction for printing next one
        // TODO


/*
        if (we are in IF instruction) {
            // Current instruction has no delay slot
            setMustInsertLineBreak(context.isLineBreakRequested() || newIsBreak);
            // Clear break request for next one
            context.setLineBreakRequest(false);
        }
        else {
            // Current instruction has a delay slot
            // Don't break now
            setMustInsertLineBreak(false);
            // Request a break after the next instruction if needed (current instruction is IF)
            context.setLineBreakRequest(newIsBreak);
        }
        */
    }


    public long getBinaryStatement() {
        long out = 0;
        for (int i = 0; i < numData; ++i) {
            out |= (data[i] << (i * 16));
        }
        return out;
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
        ArmInstruction instruction = ArmInstructionSet.instructionMap[data[0]];

        if (instruction == null) {
            setInstruction(ArmInstructionSet.opData[RangeType.Width.MD_WORD.getIndex()]);
        }
        else {
            setInstruction(instruction);
        }
    }

    /* check if NOPs: valid code but does nothing */
    public boolean isPotentialStuffing() {
        return numData == 1 && (
                   data[0] == 0xBF00 /* NOP 16-bit thumb */
// TODO NOP 32-bit thumb ?
                || data[0] == 0x0000 /* 0x0000 stuffing */ );
    }
}
