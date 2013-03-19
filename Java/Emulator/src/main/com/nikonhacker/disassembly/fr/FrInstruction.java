package com.nikonhacker.disassembly.fr;


import com.nikonhacker.disassembly.Instruction;

public class FrInstruction extends Instruction {


    public int encoding;
    public int mask;
    public FrInstructionSet.InstructionFormat instructionFormat;
    public int numberExtraXWords;
    public int numberExtraYWords;

    /**
     * Creates a new FrInstruction
     * @param encoding the value of the word instruction that matches this opcode if all parameters being 0
     * @param mask the part of the "encoding" that contains the actual instruction, the zeroes being used by operands
     * @param instructionFormat pattern that specifies how the instruction word should be split in parts
     * @param numberExtraXWords number of extra 16-bit words to be interpreted as x operand
     * @param numberExtraYWords number of extra 16-bit words to be interpreted as y operand (for coprocessor operations)
     * @param name the symbolic name
     * @param displayFormat a string specifying how to format operands and comment. It is a list of characters among :<br/>
<pre>
; separates the operand part and the comment part<br/>
<br/>
2 : immediate operand (x) must be multiplied by 2 (e.g. address of 16-bit data)<br/>
4 : immediate operand (x) must be multiplied by 4 (e.g. address of 32-bit data)<br/>
r : immediate operand (x) is a relative address<br/>
I : x is loaded from Ri if valid (0 otherwise) (?)<br/>
J : x is loaded from Rj if valid (0 otherwise) (?)<br/>
b : shift2 (?)<br/>
x : set bit #8 of x to indicate that register bitmap (x) used by this operation must be reversed
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
i : name of register pointed by i<br/>
j : name of register pointed by j<br/>
g : name of dedicated register pointed by i<br/>
h : name of dedicated register pointed by j<br/>
k : name of coprocessor register pointed by i<br/>
l : name of coprocessor register pointed by j<br/>
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
'x': current register is undefined<br/>
     * @param flowType
     * @param isConditional
     * @param hasDelaySlot
     */
    public FrInstruction(int encoding, int mask, FrInstructionSet.InstructionFormat instructionFormat, int numberExtraXWords, int numberExtraYWords, String name, String displayFormat, String action, FlowType flowType, boolean isConditional, boolean hasDelaySlot)
    {
        super(name, displayFormat, null, action, flowType, isConditional, hasDelaySlot?DelaySlotType.NORMAL:DelaySlotType.NONE);
        this.encoding = encoding;
        this.mask = mask;
        this.instructionFormat = instructionFormat;
        this.numberExtraXWords = numberExtraXWords;
        this.numberExtraYWords = numberExtraYWords;
    }

    @Override
    public String toString() {
        return getName() + "(0x" + Integer.toHexString(encoding) + ")";
    }
}
