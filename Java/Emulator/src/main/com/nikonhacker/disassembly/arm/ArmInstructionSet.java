package com.nikonhacker.disassembly.arm;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.emu.EmulationException;

import java.util.EnumSet;
import java.util.Set;

public class ArmInstructionSet {

    /**
     * These are specific timings used in instruction execution time computation.
     * For the time being, these are considered constants
     */

    public static final SimulationCode xxx = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            // TODO
            context.cpuState.pc += 2;
            context.cycleIncrement = 1;
        }
    };
    /**
     * Instruction types (formats)
     */
    public enum InstructionFormat {
        /** Layout of type A instructions is as follows : <pre>[   op          |  Rj   |  Ri   ]</pre> */
        A,

        /** Layout of type B instructions is as follows : <pre>[   op  |       x       |  Ri   ]</pre> */
        B,

        /** Layout of type C instructions is as follows : <pre>[   op          |   x   |  Ri   ]</pre> */
        C,

        /** Layout of type D instructions is as follows : <pre>[   op          |       x       ]</pre> */
        D,

        /** Layout of type E instructions is as follows : <pre>[   op                  |  Ri   ]</pre> */
        E,

        /** Layout of type F instructions is as follows : <pre>[   op    |     offset / 2      ]</pre> */
        F,

        /** Layout of type Z instructions is as follows : <pre>[   op                          ]</pre> */
        Z,

        /** Layout for data reading is as follows :       <pre>[               x               ]</pre> */
        W
    }

    /**
     * All 16bit variations of opcode and arguments
     */
    public static ArmInstruction[] instructionMap = new ArmInstruction[0x10000];


    /**
     * This is a "catch-all" instruction used as a safety net for unknown instructions
     */
    public static final ArmInstruction defaultInstruction = new ArmInstruction(InstructionFormat.W, "UNK", "", "", Instruction.FlowType.NONE, false, null);

    /**
     * Main instruction map
     * These are the official names from Cortex M-3 technical manual
     */
    private static void addBaseInstructions(Set<OutputOption> options) {
/*                         encode, mask,   new FrInstruction( format             , nX, name,     displayFmt,     action     , Type                     ,isCond, simulationCode) */

        /* LD @(R13,Rj), Ri */
        fillInstructionMap(0x0000, 0xFF00, new ArmInstruction(InstructionFormat.A, "LD",     "@(A&j),i",     "iw"       , Instruction.FlowType.NONE, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // TODO
                //context.cpuState.setReg(statement.ri_rs_fs, context.memory.load32(context.cpuState.getReg(13) + context.cpuState.getReg(statement.rj_rt_ft)));
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));

    }

    /**
     * Fake OPCodes for data reading
     * Array index is a RangeType.Width.index value
     */
    static ArmInstruction[] opData = {
        new ArmInstruction(InstructionFormat.W, "DW",     "u;a",         ""         , Instruction.FlowType.NONE, false, null),
        new ArmInstruction(InstructionFormat.W, "DL",     "u;a",         ""         , Instruction.FlowType.NONE, false, null),
        new ArmInstruction(InstructionFormat.W, "DL",     "u;a",         ""         , Instruction.FlowType.NONE, false, null),
        new ArmInstruction(InstructionFormat.W, "DL",     "u;T #v",      ""         , Instruction.FlowType.NONE, false, null),
        new ArmInstruction(InstructionFormat.W, "DR",     "q;f",         ""         , Instruction.FlowType.NONE, false, null),
    };


    /**
     * Default instruction decoding upon class loading
     */
    static {
        init(EnumSet.noneOf(OutputOption.class));
    }

    /**
     * This method fills the instructionMap array with all possible variants of instruction word so that
     * OPCODE can be looked up by just getting instructionMap[instructionWord]
     */
    public static void init(Set<OutputOption> options) {
        /* opcode decoding */
        // First, fill everything with a default dummy code as a safety net for unknown instructions
        for (int i = 0; i < 0x10000; i++) {
            instructionMap[i] = defaultInstruction;
        }
        // Then overwrite with actual instructions
        addBaseInstructions(options);
        // And optionally replace some opcodes with alternate versions
// TODO
//        if (options.contains(OutputOption.STACK))
//            replaceAltStackInstructions();
    }

    /**
     * This method maps all possible so that all possible values of the variable parts (the 0 bits in the mask)
     * in the destination array point to the given Instruction<br/>
     * e.g. if call is
     * <pre>fillInstructionMap(0x0000, 0xFF00, myInstruction);</pre>
     * then destination[0x00] to destination[0xFF] will all point to myInstruction
     * @param encoding
     * @param instruction
     * @param mask
     */
    private static void fillInstructionMap(int encoding, int mask, ArmInstruction instruction) {
        int n = (~ mask) & 0xFFFF;
        for( int i = 0 ; i <= n ; i++)
        {
            instructionMap[encoding | i] = instruction;
        }
    }

}
