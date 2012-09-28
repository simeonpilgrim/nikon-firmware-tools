package com.nikonhacker.disassembly.tx;


import com.nikonhacker.disassembly.Instruction;

public class TxInstruction extends Instruction {

    private TxInstructionSet.InstructionFormat32 instructionFormat32;
    private TxInstructionSet.InstructionFormat16 instructionFormat16;
    public SimulationCode simulationCode;

    /**
     * Creates a new TxInstruction
     * @param name the symbolic name
     * @param displayFormat a string specifying how to format operands and comment. It is a list of characters among :<br/>
<pre>
i, j, k are the operands rs(fs), rt(ft) and rd(fd), respectively<br/>
I, J, K are the values of the registers rs(fs), rt(ft) and rd(fd), respectively, before execution<br/>
l (lowercase L) is the value of shamt (shift amount) or cc (CP1 condition code flag)
; separates the operand part and the comment part<br/>
<br/>
2 : immediate operand (x) must be multiplied by 2 (e.g. address of 16-bit data)<br/>
4 : immediate operand (x) must be multiplied by 4 (e.g. address of 32-bit data)<br/>
r : immediate operand (x) is a relative address<br/>
R : immediate operand (x) must be added concatenated to PC & 0xF0000000 <br/>
b : shift2 (?)<br/>
y : add 8 to c to mark that register bitmap (x) used by this operation represents R8-R15 and not R0-R7
<br/>
[] delimits a section that is only printed optionally. If it is at the start of the format string, it is only printed if included operand is "non-zero". If it is not at the start of the format string, it is only printed if different than the section printed before. examples: "[r0]x" prints "x", "[r2]y prints "r2y", "x[y]" prints "xy" and "x[x]" only prints "x". Blanks and commas are ignored<br/>
# ( ) + , - @ are copied as is<br/>
& : outputs a ,<br/>
<br/>
s : outputs x as a signed hex<br/>
u : outputs x as an unsigned hex<br/>
n : outputs x as a negative hex<br/>
d : outputs x as a decimal<br/>
a : outputs x as ASCII chars<br/>
f : outputs x as a float (hi-half as the dividend, low-half as the divider)<br/>
q : outputs x as a ratio : hi-half / low-half<br/>
p : outputs x as a pair of hex values : hi-half, lo-half<br/>
z : outputs x as a bitmap of register IDs (influenced by previous x and y chars)<br/>
<br/>
v : outputs current PC value as a vector id (0xFF being the first of this memory area, going down to 0x00)
c : outputs coprocessor operation (c)<br/>
</pre>
     * @param action a string specifying how to interpret the instruction. It is a list of characters among :<br/>
* <pre>
'A': current register is AC<br/>
'C': current register is CCR<br/>
'F': current register is FP<br/>
'P': current register is PS<br/>
'S': current register is SP<br/>
'i': current register is Ri<br/>
'j': current register is Rj<br/>
'w': current register is marked invalid<br/>
'v': current register is marked valid and loaded with the given value<br/>
'V': current register is marked valid and set to the given value shifted left by 16 positions<br/>
'+': current register is incremented by given value<br/>
'x': current register is undefined<br/>
     * @param instructionFormat32 pattern that specifies how the instruction word should be split in parts
     * @param instructionFormat16
     * @param flowType
     * @param isConditional
     * @param delaySlotType
     */
    public TxInstruction(String name, String displayFormat, String action, String sampleUse, String description,
                         TxInstructionSet.InstructionFormat32 instructionFormat32,
                         TxInstructionSet.InstructionFormat16 instructionFormat16,
                         FlowType flowType, boolean isConditional, DelaySlotType delaySlotType, SimulationCode simulationCode) {
        super(name, displayFormat, action, flowType, isConditional, delaySlotType);
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
