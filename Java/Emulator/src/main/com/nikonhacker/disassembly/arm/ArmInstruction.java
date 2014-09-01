package com.nikonhacker.disassembly.arm;

import com.nikonhacker.disassembly.Instruction;
import com.nikonhacker.disassembly.SimulationCode;

public class ArmInstruction extends Instruction {

    public ArmInstructionSet.InstructionFormat instructionFormat;

    /**
     * Creates a new FrInstruction
     * @param instructionFormat pattern that specifies how the instruction word should be split in parts
     * @param numberExtraXWords number of extra 16-bit words to be interpreted as x operand
     * @param numberExtraYWords number of extra 16-bit words to be interpreted as y operand (for coprocessor operations)
     * @param name the symbolic name
     * @param displayFormat a string specifying how to format operands and comment. It is a list of characters among :
     * @param flowType
     * @param isConditional
     * @param hasDelaySlot
     * @param simulationCode
     */
    public ArmInstruction(ArmInstructionSet.InstructionFormat instructionFormat, String name, String displayFormat, String action, FlowType flowType, boolean isConditional, SimulationCode simulationCode) {
        super(name, displayFormat, null, null, action, flowType, isConditional, DelaySlotType.NONE, simulationCode);
        this.instructionFormat = instructionFormat;
    }

    @Override
    public String toString() {
        return getName();
    }
}
