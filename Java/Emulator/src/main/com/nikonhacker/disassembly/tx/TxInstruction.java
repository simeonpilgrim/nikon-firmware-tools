package com.nikonhacker.disassembly.tx;


import com.nikonhacker.disassembly.Instruction;

public class TxInstruction extends Instruction {

    private Format instructionFormat;
    public int encoding;
    public int mask;
    public int numberExtraXWords;
    public int numberExtraYWords;
    public String name;
    public String displayFormat;
    public String action;

    public TxInstruction(String name, String displayFormat, String sampleUse, String description, Format instructionFormat, String marsOperationMask, SimulationCode simulationCode) {
        super(null, false, false);
        this.name = name;
        this.displayFormat = displayFormat;
        this.instructionFormat = instructionFormat;
    }
    /**
     * Instruction types (formats)
     */
    public enum Format {
        /** Layout of type I instructions is as follows : <pre>[  op  | rs  | rt  |      imm       ]</pre> */
        I,
        /** Layout of type J instructions is as follows : <pre>[  op  |           target           ]</pre> */
        J,
        /** Layout of type R instructions is as follows : <pre>[  op  | rs  | rt  | rd  |shamt| fnct ]</pre> */
        R ,
        /** MARS-defined variant of I layout */
        I_BRANCH,
        /** used for BREAK code */
        BREAK,
        /** used for TRAP code */
        TRAP,
        /** used for move from/to CP0 instructions is as follows : <pre>[  op  |xxxxx| rt  | rd  |00000000| res ]</pre> */
        CP
    }


    /**
     * Creates a new TxInstruction
     * @param encoding the value of the word instruction that matches this opcode/function combination (6 highest bits concatenated with 6 lower bits from the 32 bits statement)
     * @param mask indicates which bits of the "encoding" are significant. the others are either operants, unused, or to be determined later
     * @param instructionFormat pattern that specifies how the instruction word should be split in parts
     * @param numberExtraXWords number of extra 16-bit words to be interpreted as x operand
     * @param numberExtraYWords number of extra 16-bit words to be interpreted as y operand (for coprocessor operations)
     * @param name the symbolic name
     * @param displayFormat a string specifying how to format operands. It is a list of characters among :<br/>
<pre>
i, j, k are the operands rs, rt, and rd, respectively <br/>
I, J, K are the values of the operands rs, rt, rd before execution<br/>
l (lowercase L) is the value of shamt (shift amount)
; separates the operand part and the comment part<br/>
<br/>
2 : constant operand (x) must be multiplied by 2 (e.g. address of 16-bit data)<br/>
4 : constant operand (x) must be multiplied by 4 (e.g. address of 32-bit data)<br/>
r : constant operand (x) is a relative address<br/>
b : shift2 (?)<br/>
y : add 8 to c to mark that register bitmap (x) used by this operation represents R8-R15 and not R0-R7
<br/>
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
A : outputs "AC"<br/>
C : outputs "CCR"<br/>
F : outputs "FP"<br/>
M : outputs "ILM"<br/>
P : outputs "PS"<br/>
S : outputs "SP"<br/>
<br/>
v : outputs current PC value as a vector id (0xFF being the first of this memory area, going down to 0x00)
c : outputs coprocessor operation (c)<br/>
</pre>
     * @param action a string specifying how to interpret the instruction. It is a list of characters among :<br/>
* <pre>
'!': jump<br/>
'?': branch<br/>
'(': call<br/>
')': return<br/>
'_': instruction provides a delay slot<br/>
'A': current register is AC<br/>
'C': current register is CCR<br/>
'F': current register is FP<br/>
'P': current register is PS<br/>
'S': current register is SP<br/>
'i': current register is Ri<br/>
'j': current register is Rj<br/>
'w': current register is marked invalid<br/>
'v': current register is marked valid and loaded with the given value<br/>
'x': current register is undefined<br/>
     * @param flowType
     * @param isConditional
     * @param hasDelaySlot
     */
    public TxInstruction(int encoding, int mask, Format instructionFormat, int numberExtraXWords, int numberExtraYWords, String name, String displayFormat, String action, FlowType flowType, boolean isConditional, boolean hasDelaySlot)
    {
        super(flowType, hasDelaySlot, isConditional);
        this.encoding = encoding;
        this.mask = mask;
        this.instructionFormat = instructionFormat;
        this.numberExtraXWords = numberExtraXWords;
        this.numberExtraYWords = numberExtraYWords;
        this.name = name;
        this.displayFormat = displayFormat;
        this.action = action;
    }

    public Format getInstructionFormat() {
        return instructionFormat;
    }

    @Override
    public String toString() {
        return name + "(0x" + Integer.toHexString(encoding) + ")";
    }
}
