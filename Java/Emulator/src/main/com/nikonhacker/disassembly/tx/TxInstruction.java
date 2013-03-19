package com.nikonhacker.disassembly.tx;


import com.nikonhacker.disassembly.Instruction;

public class TxInstruction extends Instruction {

    private TxInstructionSet.InstructionFormat32 instructionFormat32;
    private TxInstructionSet.InstructionFormat16 instructionFormat16;
    private SimulationCode simulationCode;

    /**
     * Creates a new TxInstruction
     * @param name the symbolic name
     * @param operandFormat
     * @param commentFormat
     * @param action a string specifying how to interpret the instruction. It is a list of characters among :<br/>
* <pre>
'F': current register is FP<br/>
'S': current register is SP<br/>
'i': current register is Rs<br/>
'j': current register is Rt<br/>
'k': current register is Rd<br/>
'w': current register is marked invalid<br/>
'v': current register is marked valid and loaded with the given value<br/>
'V': current register is marked valid and set to the given value shifted left by 16 positions<br/>
'+': current register is incremented by given value<br/>
'|': current register is or'ed with given value<br/>
'x': current register is undefined<br/>
     * @param instructionFormat32 pattern that specifies how the instruction word should be split in parts
     * @param instructionFormat16
     * @param flowType
     * @param isConditional
     * @param delaySlotType
     */
    public TxInstruction(String name, String operandFormat, String commentFormat, String action, String sampleUse, String description,
                         TxInstructionSet.InstructionFormat32 instructionFormat32,
                         TxInstructionSet.InstructionFormat16 instructionFormat16,
                         FlowType flowType, boolean isConditional, DelaySlotType delaySlotType, SimulationCode simulationCode) {
        super(name, operandFormat, commentFormat, action, flowType, isConditional, delaySlotType);
        this.instructionFormat32 = instructionFormat32;
        this.instructionFormat16 = instructionFormat16;
        this.simulationCode = simulationCode;
    }

    public TxInstructionSet.InstructionFormat32 getInstructionFormat32() {
        return instructionFormat32;
    }

    public TxInstructionSet.InstructionFormat16 getInstructionFormat16() {
        return instructionFormat16;
    }

    public SimulationCode getSimulationCode() {
        return simulationCode;
    }

    @Override
    public String toString() {
        return getName();
    }
}
