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

        /** MARS-defined variant of I layout as follows : <pre>[  op  |base | rt  |     offset     ]</pre> */
        I_BRANCH,

        /** used for BREAK code */
        BREAK,

        /** used for TRAP code */
        TRAP,

        /** Layout used for CP0 instructions as follows : <pre>[  op  |xxxxx| rt  | rd  |00000000|res]</pre> */
        CP0,

        /** Layout used for CP1 instructions as follows : <pre>[  op  |xxxxx| rt  | fs  |00000000000]</pre> */
        CP1_R1,

        /** Layout used for CP1 instructions as follows : <pre>[  op  |xxxxx| rt  | cr  |00000000000]</pre> */
        CP1_CR1,

        /** Layout used for CP1 instructions as follows : <pre>[  op  | fmt | ft  | fs  | fd  |xxxxxx]</pre> */
        CP1_R2,

        /** Layout used for CP1 instructions as follows : <pre>[  op  | rs  | ft  |      imm       ]</pre> */
        CP1_I,

        /** Layout used for CP1 instructions as follows : <pre>[  op  |xxxxx||cc |xx|     offset     ]</pre> */
        CP1_CC_BRANCH,

        /** Layout used for CP1 instructions as follows : <pre>[  op  | fmt | ft  | fs  |cc |0|0|11|cond]</pre> */
        CP1_R_CC

     }

    /**
     * Creates a new TxInstruction
     * @param encoding the value of the word instruction that matches this opcode/function combination (6 highest bits concatenated with 6 lower bits from the 32 bits statement)
     * @param mask indicates which bits of the "encoding" are significant. the others are either operants, unused, or to be determined later
     * @param instructionFormat pattern that specifies how the instruction word should be split in parts
     * @param numberExtraXWords number of extra 16-bit words to be interpreted as x operand
     * @param numberExtraYWords number of extra 16-bit words to be interpreted as y operand (for coprocessor operations)
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
