package com.nikonhacker.disassembly.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.DisassemblyException;
import com.nikonhacker.disassembly.Instruction;
import com.nikonhacker.emu.EmulationException;
import com.nikonhacker.emu.memory.Memory;

/*
Copyright (c) 2003-2010,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
*/

/**
 * The list of TxInstruction objects, each of which represents a TX19a MIPS32 instruction.
 * This is based mainly on
 *
 * @author original: Pete Sanderson and Ken Vollmar
 * @version August 2003-5
 */

public class TxInstructionSet
{

    // ----------------------------- 16 bits ------------------------------------

    /**
     * All 16bit variations of opcode and arguments
     */
    public static TxInstruction[] opcode16Map = new TxInstruction[0x10000];
    public static TxInstruction[] extendedOpcode16Map = new TxInstruction[0x10000];

    /**
     * Instruction types (formats)
     */
    public enum InstructionFormat16 {
        /** Layout of type I instructions is as follows         : <pre>[ op  |    imm    ]</pre> */
        I,

        /** Layout of type RI instructions is as follows        : <pre>[ op  |rx |  imm   ]</pre> */
        RI,

        /** Layout of type RR instructions is as follows        : <pre>[ RR  |rx |ry |  F  ]</pre> */
        RR,

        /** Layout of type RRI instructions is as follows       : <pre>[ op  |rx |ry | imm ]</pre> */
        RRI,

        /** Layout of type RRR1 instructions is as follows      : <pre>[ RRR |rx |ry |rz |F ]</pre> */
        RRR1,

        /** Layout of type RRR2 instructions is as follows      : <pre>[ op  |ry |F| imm |F ]</pre> */
        RRR2,

        /** Layout of type RRR3 instructions is as follows      : <pre>[ op  |imm|F|cpr32|F ]</pre> */
        RRR3,

        /** Layout of type RRR4 instructions is as follows      : <pre>[ op  |rx |F|00000|F ]</pre> */
        RRR4,

        /** Layout of type RRIA instructions is as follows      : <pre>[RRI-A|rx |ry |F|imm ]</pre> */
        RRIA,

        /** Layout of type SHIFT1 instructions is as follows    : <pre>[SHIFT|rx |ry |SA |F ]</pre> */
        SHIFT1,

        /** Layout of type SHIFT2 instructions is as follows    : <pre>[ op  |rxy|cpr32| F ]</pre> */
        SHIFT2,

        /** Layout of type I8 instructions is as follows        : <pre>[ I8  | F |        ]</pre> */
        I8,

        /** Layout of type I8MOVR32 instructions is as follows  : <pre>[ I8  | F |ry |r3240]</pre> */
        I8MOVR32,

        /** Layout of type I8MOV32R instructions is as follows  : <pre>[ I8  | F |R32A4|rz ]</pre> */
        I8MOV32R,

        /** Layout of type I8SVRS instructions is as follows    : <pre>[ I8  | F  |r|s|s|imm ]</pre> */
        I8SVRS,

        /** Layout of type FPB_SPB instructions is as follows   : <pre>[ op  |rx |F|  imm  ]</pre> */
        FPB_SPB,

        /** Layout of type FP_SP_H instructions is as follows   : <pre>[ op  |rx |F| imm  |F]</pre> */
        FP_SP_H,

        /** Layout of type SWFP_LWFP instructions is as follows : <pre>[ op  | F |ry | imm ]</pre> */
        SWFP_LWFP,

        /** Layout of type SPC_BIT instructions is as follows   : <pre>[ op  | F |ps3| imm ]</pre> */
        SPC_BIT,

        /** Layout of type SPC_BAL instructions is as follows   : <pre>[ op  | F |  imm   ]</pre> */
        SPC_BAL,

        /** Layout of type RRR_INT instructions is as follows   : <pre>[ op  |00|F |0000|F ]</pre> */
        RRR_INT,

        /** Layout for data reading is as follows               : <pre>[      imm       ]</pre> */
        W,

        /** Layout for data reading is as follows               : <pre>[ op  | imm  |  F  ]</pre> */
        BREAK
    }

    // TODO "EXTEND"


    // ----------------------------- 32 bits ------------------------------------

    /**
     * Instruction types (formats)
     */
    public enum InstructionFormat32 {
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
        CP1_R_CC,

        /** Layout for data reading is as follows       : <pre>[              imm               ]</pre> */
        W
    }

    public static final TxInstruction dummyInstruction = new TxInstruction("unk", "u", "", "unk",
            "UNKnown instruction",
            InstructionFormat32.W,
            InstructionFormat16.W, "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                }
            });
    public static final TxInstruction addInstruction = new TxInstruction("add", "k, [i, ]j", "kw", "add $t1,$t2,$t3",
            "ADDition with overflow: set $t1 to ($t2 plus $t3)",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 100000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int add1 = cpuState.getReg(statement.rs_fs);
                    int add2 = cpuState.getReg(statement.rt_ft);
                    int sum = add1 + add2;
                    // overflow on A+B detected when A and B have same sign and A+B has other sign.
                    if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    cpuState.setReg(statement.rd_fd, sum);
                }
            });
    public static final TxInstruction subInstruction = new TxInstruction("sub", "k, [i, ]j", "kw", "sub $t1,$t2,$t3",
            "SUBtraction with overflow: set $t1 to ($t2 minus $t3)",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 100010",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int sub1 = cpuState.getReg(statement.rs_fs);
                    int sub2 = cpuState.getReg(statement.rt_ft);
                    int dif = sub1 - sub2;
                    // overflow on A-B detected when A and B have opposite signs and A-B has B's sign
                    if ((sub1 >= 0 && sub2 < 0 && dif < 0) || (sub1 < 0 && sub2 >= 0 && dif >= 0)) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    cpuState.setReg(statement.rd_fd, dif);
                }
            });
    public static final TxInstruction addiInstruction = new TxInstruction("addi", "j, [i, ]s", "j+", "addi $t1,$t2,-100",
            "ADDition Immediate with overflow: set $t1 to ($t2 plus signed 16-bit immediate)",
            InstructionFormat32.I,
            null, "001000 sssss fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int add1 = cpuState.getReg(statement.rs_fs);
                    int add2 = statement.imm << 16 >> 16;
                    int sum = add1 + add2;
                    // overflow on A+B detected when A and B have same sign and A+B has other sign.
                    if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    cpuState.setReg(statement.rt_ft, sum);
                }
            });
    public static final TxInstruction adduInstruction = new TxInstruction("addu", "k, [i, ]j", "kw", "addu $t1,$t2,$t3",
            "ADDition Unsigned without overflow: set $t1 to ($t2 plus $t3), no overflow",
            InstructionFormat32.R, InstructionFormat16.RRR1,
            "000000 sssss ttttt fffff 00000 100001",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rs_fs) + cpuState.getReg(statement.rt_ft));
                }
            });
    // alternative if rt=r0
    public static final TxInstruction moveAdduInstruction = new TxInstruction("move", "k, i", "kw", "move $t1,$t2",
            "MOVE (formally an ADDU with rt=r0): set $t1 to $t2, no overflow",
            InstructionFormat32.R,
            null, "000000 sssss 00000 fffff 00000 100001",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rs_fs));
                }
            });
    public static final TxInstruction subuInstruction = new TxInstruction("subu", "k, [i, ]j", "kw", "subu $t1,$t2,$t3",
            "SUBtraction Unsigned without overflow: set $t1 to ($t2 minus $t3), no overflow",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 100011",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rs_fs) - cpuState.getReg(statement.rt_ft));
                }
            });
    public static final TxInstruction addiuInstruction = new TxInstruction("addiu", "j, [i, ]s", "j+", "addiu $t1,$t2,-100",
            "ADDition Immediate 'Unsigned' without overflow: set $t1 to ($t2 plus signed 16-bit immediate), no overflow",
            InstructionFormat32.I,
            InstructionFormat16.RRIA, "001001 sssss fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int shift = 32 - statement.immBitWidth;
                    cpuState.setReg(statement.rt_ft, cpuState.getReg(statement.rs_fs) + (statement.imm << shift >> shift));
                }
            });
    // alternative if rs=r0
    public static final TxInstruction liAddiuInstruction = new TxInstruction("li", "j, s", "jv", "li $t1,-100",
            "Load Immediate (formally an ADDIU with rs = r0): set $t1 to signed 16-bit immediate, no overflow",
            InstructionFormat32.I,
            null, "001001 00000 fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, statement.imm << 16 >> 16);
                }
            });
    public static final TxInstruction multInstruction = new TxInstruction("mult", "[k, ]i, j", "kw", "mult $t1,$t2",
            "MULTiplication: Set HI to high-order 32 bits, LO (and Rs) to low-order 32 bits of the product of $t1 and $t2",
            InstructionFormat32.R,
            null, "000000 fffff sssss 00000 00000 011000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (long) cpuState.getReg(statement.rs_fs) * (long) cpuState.getReg(statement.rt_ft);
                    cpuState.setReg(TxCPUState.HI, (int) (product >> 32));
                    int lo = (int) ((product << 32) >> 32);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd_fd, lo);
                }
            });
    public static final TxInstruction multuInstruction = new TxInstruction("multu", "[k, ]i, j", "kw", "multu $t1,$t2",
            "MULTiplication Unsigned: Set HI to high-order 32 bits, LO (and Rs) to low-order 32 bits of the product of unsigned $t1 and $t2",
            InstructionFormat32.R,
            null, "000000 fffff sssss 00000 00000 011001",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (((long) cpuState.getReg(statement.rs_fs)) << 32 >>> 32)
                            * (((long) cpuState.getReg(statement.rt_ft)) << 32 >>> 32);
                    cpuState.setReg(TxCPUState.HI, (int) (product >> 32));
                    int lo = (int) ((product << 32) >> 32);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd_fd, lo);
                }
            });
    public static final TxInstruction mulInstruction = new TxInstruction("mul", "k, i, j", "kw", "mul $t1,$t2,$t3",
            "MULtiplication without overflow: Set $t1 to low-order 32 bits of the product of $t2 and $t3",
            InstructionFormat32.R,
            null, "011100 sssss ttttt fffff 00000 000010",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (long) cpuState.getReg(statement.rs_fs) * (long) cpuState.getReg(statement.rt_ft);
                    cpuState.setReg(statement.rd_fd, (int) ((product << 32) >> 32));
                }
            });
    public static final TxInstruction maddInstruction = new TxInstruction("madd", "[k, ]i, j", "kw", "madd $t1,$t2",
            "Multiply ADD: Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product",
            InstructionFormat32.R,
            null, "011100 fffff sssss 00000 00000 000000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (long) cpuState.getReg(statement.rs_fs) * (long) cpuState.getReg(statement.rt_ft);
                    long contentsHiLo = Format.twoIntsToLong(cpuState.getReg(TxCPUState.HI), cpuState.getReg(TxCPUState.LO));
                    long sum = contentsHiLo + product;
                    cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(sum));
                    int lo = Format.lowOrderLongToInt(sum);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd_fd, lo);
                }
            });
    public static final TxInstruction madduInstruction = new TxInstruction("maddu", "[k, ]i, j", "kw", "maddu $t1,$t2",
            "Multiply ADD Unsigned: Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product, unsigned",
            InstructionFormat32.R,
            null, "011100 fffff sssss 00000 00000 000001",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (((long) cpuState.getReg(statement.rs_fs)) << 32 >>> 32)
                            * (((long) cpuState.getReg(statement.rt_ft)) << 32 >>> 32);
                    long contentsHiLo = Format.twoIntsToLong(cpuState.getReg(TxCPUState.HI), cpuState.getReg(TxCPUState.LO));
                    long sum = contentsHiLo + product;
                    cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(sum));
                    int lo = Format.lowOrderLongToInt(sum);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd_fd, lo);
                }
            });
    public static final TxInstruction msubInstruction = new TxInstruction("msub", "[k, ]i, j", "kw", "msub $t1,$t2",
            "Multiply SUBtract: Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decrement LO by low-order 32 bits of product",
            InstructionFormat32.R,
            null, "011100 fffff sssss 00000 00000 000100",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (long) cpuState.getReg(statement.rs_fs) * (long) cpuState.getReg(statement.rt_ft);
                    long contentsHiLo = Format.twoIntsToLong(cpuState.getReg(TxCPUState.HI), cpuState.getReg(TxCPUState.LO));
                    long diff = contentsHiLo - product;
                    cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(diff));
                    int lo = Format.lowOrderLongToInt(diff);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd_fd, lo);
                }
            });
    public static final TxInstruction msubuInstruction = new TxInstruction("msubu", "[k, ]i, j", "kw", "msubu $t1,$t2",
            "Multiply SUBtract Unsigned: Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decement LO by low-order 32 bits of product, unsigned",
            InstructionFormat32.R,
            null, "011100 fffff sssss 00000 00000 000101",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (((long) cpuState.getReg(statement.rs_fs)) << 32 >>> 32)
                            * (((long) cpuState.getReg(statement.rt_ft)) << 32 >>> 32);
                    long contentsHiLo = Format.twoIntsToLong(cpuState.getReg(TxCPUState.HI), cpuState.getReg(TxCPUState.LO));
                    long diff = contentsHiLo - product;
                    cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(diff));
                    int lo = Format.lowOrderLongToInt(diff);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd_fd, lo);
                }
            });
    public static final TxInstruction divInstruction = new TxInstruction("div", "i, j", "iw", "div $t1,$t2",
            "DIVision with overflow: Divide $t1 by $t2 then set LO to quotient and HI to remainder",
            InstructionFormat32.R, InstructionFormat16.RR,
            "000000 fffff sssss 00000 00000 011010",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt_ft) == 0) {
                        // Note: no exceptions, and undefined results for zero divide
                        return;
                    }
                    cpuState.setReg(TxCPUState.HI, cpuState.getReg(statement.rs_fs) % cpuState.getReg(statement.rt_ft));
                    cpuState.setReg(TxCPUState.LO, cpuState.getReg(statement.rs_fs) / cpuState.getReg(statement.rt_ft));
                }
            });
    public static final TxInstruction divuInstruction = new TxInstruction("divu", "i, j", "iw", "divu $t1,$t2",
            "DIVision Unsigned without overflow: Divide unsigned $t1 by $t2 then set LO to quotient and HI to remainder",
            InstructionFormat32.R, InstructionFormat16.RR,
            "000000 fffff sssss 00000 00000 011011",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt_ft) == 0) {
                        // Note: no exceptions, and undefined results for zero divide
                        return;
                    }
                    long oper1 = ((long) cpuState.getReg(statement.rs_fs)) << 32 >>> 32;
                    long oper2 = ((long) cpuState.getReg(statement.rt_ft)) << 32 >>> 32;
                    cpuState.setReg(TxCPUState.HI, (int) (((oper1 % oper2) << 32) >> 32));
                    cpuState.setReg(TxCPUState.LO, (int) (((oper1 / oper2) << 32) >> 32));
                }
            });
    public static final TxInstruction mfhiInstruction = new TxInstruction("mfhi", "k", "kw", "mfhi $t1",
            "Move From HI register: Set $t1 to contents of HI (see multiply and divide operations)",
            InstructionFormat32.R,
            null, "000000 00000 00000 fffff 00000 010000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(TxCPUState.HI));
                }
            });
    public static final TxInstruction mfloInstruction = new TxInstruction("mflo", "k", "kw", "mflo $t1",
            "Move From LO register: Set $t1 to contents of LO (see multiply and divide operations)",
            InstructionFormat32.R,
            null, "000000 00000 00000 fffff 00000 010010",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(TxCPUState.LO));
                }
            });
    public static final TxInstruction mthiInstruction = new TxInstruction("mthi", "i", "", "mthi $t1",
            "Move To HI registerr: Set HI to contents of $t1 (see multiply and divide operations)",
            InstructionFormat32.R,
            null, "000000 fffff 00000 00000 00000 010001",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.HI, cpuState.getReg(statement.rs_fs));
                }
            });
    public static final TxInstruction mtloInstruction = new TxInstruction("mtlo", "i", "", "mtlo $t1",
            "Move To LO register: Set LO to contents of $t1 (see multiply and divide operations)",
            InstructionFormat32.R,
            null, "000000 fffff 00000 00000 00000 010011",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.LO, cpuState.getReg(statement.rs_fs));
                }
            });
    public static final TxInstruction andInstruction = new TxInstruction("and", "k, [i, ]j", "kw", "and $t1,$t2,$t3",
            "bitwise AND: Set $t1 to bitwise AND of $t2 and $t3",
            InstructionFormat32.R, InstructionFormat16.RR,
            "000000 sssss ttttt fffff 00000 100100",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rs_fs) & cpuState.getReg(statement.rt_ft));
                }
            });
    public static final TxInstruction orInstruction = new TxInstruction("or", "k, [i, ]j", "kw", "or $t1,$t2,$t3",
            "bitwise OR: Set $t1 to bitwise OR of $t2 and $t3",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 100101",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rs_fs) | cpuState.getReg(statement.rt_ft));
                }
            });
    // alternative if rs=r0
    public static final TxInstruction moveOrInstruction = new TxInstruction("move", "k, j", "kw", "move $t1,$t3",
            "MOVE (formally an OR with rs=r0): Set $t1 to $t3",
            InstructionFormat32.R,
            null, "000000 00000 ttttt fffff 00000 100101",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rt_ft));
                }
            });
    public static final TxInstruction andiInstruction = new TxInstruction("andi", "j, [i, ]s", "jw", "andi $t1,$t2,100",
            "bitwise AND Immediate: Set $t1 to bitwise AND of $t2 and zero-extended 16-bit immediate",
            InstructionFormat32.I, InstructionFormat16.RI,
            "001100 sssss fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                    cpuState.setReg(statement.rt_ft, cpuState.getReg(statement.rs_fs) & (statement.imm & 0x0000FFFF));
                }
            });
    public static final TxInstruction oriInstruction = new TxInstruction("ori", "j, [i, ]s", "jw", "ori $t1,$t2,100",
            "bitwise OR Immediate: Set $t1 to bitwise OR of $t2 and zero-extended 16-bit immediate",
            InstructionFormat32.I,
            null, "001101 sssss fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                    cpuState.setReg(statement.rt_ft, cpuState.getReg(statement.rs_fs) | (statement.imm & 0x0000FFFF));
                }
            });
    // alternative if rs=r0
    public static final TxInstruction liOriInstruction = new TxInstruction("li", "j, s", "jv", "li $t1,100",
            "Load Immediate (formally an ORI with rs=r0): Set $t1 to zero-extended 16-bit immediate",
            InstructionFormat32.I,
            null, "001101 00000 fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                    cpuState.setReg(statement.rt_ft, statement.imm & 0x0000FFFF);
                }
            });
    public static final TxInstruction norInstruction = new TxInstruction("nor", "k, [i, ]j", "kw", "nor $t1,$t2,$t3",
            "bitwise NOR: Set $t1 to bitwise NOR of $t2 and $t3",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 100111",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, ~(cpuState.getReg(statement.rs_fs) | cpuState.getReg(statement.rt_ft)));
                }
            });
    public static final TxInstruction xorInstruction = new TxInstruction("xor", "k, i, j", "kw", "xor $t1,$t2,$t3",
            "bitwise XOR (exclusive OR): Set $t1 to bitwise XOR of $t2 and $t3",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 100110",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rs_fs) ^ cpuState.getReg(statement.rt_ft));
                }
            });
    public static final TxInstruction xoriInstruction = new TxInstruction("xori", "j, [i, ]s", "jw", "xori $t1,$t2,100",
            "bitwise XOR Immediate: Set $t1 to bitwise XOR of $t2 and zero-extended 16-bit immediate",
            InstructionFormat32.I,
            null, "001110 sssss fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                    cpuState.setReg(statement.rt_ft, cpuState.getReg(statement.rs_fs) ^ (statement.imm & 0x0000FFFF));
                }
            });
    public static final TxInstruction sllInstruction = new TxInstruction("sll", "k, [j, ]l", "kw", "sll $t1,$t2,10",
            "Shift Left Logical: Set $t1 to result of shifting $t2 left by number of bits specified by immediate",
            InstructionFormat32.R,
            null, "000000 00000 sssss fffff ttttt 000000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rt_ft) << statement.sa_cc);
                }
            });
    // alternate SLL with 0 registers
    public static final TxInstruction nopInstruction = new TxInstruction("nop", "", "", "nop",
            "NOP (formally a useless SLL): Do nothing",
            InstructionFormat32.R,
            null, "000000 00000 00000 00000 00000 000000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // nop
                }
            });
    public static final TxInstruction sllvInstruction = new TxInstruction("sllv", "k, [j, ]i", "kw", "sllv $t1,$t2,$t3",
            "Shift Left Logical Variable: Set $t1 to result of shifting $t2 left by number of bits specified by value in low-order 5 bits of $t3",
            InstructionFormat32.R,
            null, "000000 ttttt sssss fffff 00000 000100",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // Mask all but low 5 bits of register containing shift amount.
                    cpuState.setReg(statement.rd_fd,
                            cpuState.getReg(statement.rt_ft) << (cpuState.getReg(statement.rs_fs) & 0b11111));
                }
            });
    public static final TxInstruction srlInstruction = new TxInstruction("srl", "k, [j, ]l", "kw", "srl $t1,$t2,10",
            "Shift Right Logical: Set $t1 to result of shifting $t2 right by number of bits specified by immediate",
            InstructionFormat32.R,
            null, "000000 00000 sssss fffff ttttt 000010",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // must zero-fill, so use ">>>" instead of ">>".
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rt_ft) >>> statement.sa_cc);
                }
            });
    public static final TxInstruction sraInstruction = new TxInstruction("sra", "k, [j, ]l", "kw", "sra $t1,$t2,10",
            "Shift Right Arithmetic: Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by immediate",
            InstructionFormat32.R,
            null, "000000 00000 sssss fffff ttttt 000011",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // must sign-fill, so use ">>".
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rt_ft) >> statement.sa_cc);
                }
            });
    public static final TxInstruction sravInstruction = new TxInstruction("srav", "k, [j, ]i", "kw", "srav $t1,$t2,$t3",
            "Shift Right Arithmetic Variable: Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3",
            InstructionFormat32.R,
            null, "000000 ttttt sssss fffff 00000 000111",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // Mask all but low 5 bits of register containing shift amount. Use ">>" to sign-fill.
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rt_ft) >> (cpuState.getReg(statement.rs_fs) & 0b11111));
                }
            });
    public static final TxInstruction srlvInstruction = new TxInstruction("srlv", "k, [j, ]i", "kw", "srlv $t1,$t2,$t3",
            "Shift Right Logical Variable: Set $t1 to result of shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3",
            InstructionFormat32.R,
            null, "000000 ttttt sssss fffff 00000 000110",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // Mask all but low 5 bits of register containing shift amount. Use ">>>" to zero-fill.
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rt_ft) >>> (cpuState.getReg(statement.rs_fs) & 0b11111));
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lwInstruction = new TxInstruction("lw", "j, s(i)", "jw", "lw $t1,-100($t2)",
            "Load Word: Set $t1 to contents of effective memory word address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            "100011 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // Changed from MARS : Added sign extension to offset
                    cpuState.setReg(statement.rt_ft, memory.load32(cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                }
            });
    public static final TxInstruction lwlInstruction = new TxInstruction("lwl", "j, s(i)", "jw", "lwl $t1,-100($t2)",
            "Load Word Left: Load from 1 to 4 bytes left-justified into $t1, starting with effective memory byte address and continuing through the low-order byte of its word",
            InstructionFormat32.I,
            null, "100010 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    int address = cpuState.getReg(statement.rs_fs) + statement.imm;
                    int result = cpuState.getReg(statement.rt_ft);
                    for (int i = 0; i <= address % 4; i++) {
                        result = Format.setByte(result, 3 - i, memory.loadUnsigned8(address - i));
                    }
                    cpuState.setReg(statement.rt_ft, result);
                }
            });
    public static final TxInstruction lwrInstruction = new TxInstruction("lwr", "j, s(i)", "jw", "lwr $t1,-100($t2)",
            "Load Word Right: Load from 1 to 4 bytes right-justified into $t1, starting with effective memory byte address and continuing through the high-order byte of its word",
            InstructionFormat32.I,
            null, "100110 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    int address = cpuState.getReg(statement.rs_fs) + statement.imm;
                    int result = cpuState.getReg(statement.rt_ft);
                    for (int i = 0; i <= 3 - (address % 4); i++) {
                        result = Format.setByte(result, i, memory.loadUnsigned8(address + i));
                    }
                    cpuState.setReg(statement.rt_ft, result);
                }
            });
    public static final TxInstruction swInstruction = new TxInstruction("sw", "j, s(i)", "", "sw $t1,-100($t2)",
            "Store Word: Store contents of $t1 into effective memory word address",
            InstructionFormat32.I,
            null, "101011 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    memory.store32(cpuState.getReg(statement.rs_fs) + statement.imm, cpuState.getReg(statement.rt_ft));
                }
            });
    public static final TxInstruction swlInstruction = new TxInstruction("swl", "j, s(i)", "", "swl $t1,-100($t2)",
            "Store Word Left: Store high-order 1 to 4 bytes of $t1 into memory, starting with effective byte address and continuing through the low-order byte of its word",
            InstructionFormat32.I,
            null, "101010 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    int address = cpuState.getReg(statement.rs_fs) + statement.imm;
                    int source = cpuState.getReg(statement.rt_ft);
                    for (int i = 0; i <= address % 4; i++) {
                        memory.store8(address - i, Format.getByte(source, 3 - i));
                    }
                }
            });
    public static final TxInstruction swrInstruction = new TxInstruction("swr", "j, s(i)", "", "swr $t1,-100($t2)",
            "Store Word Right: Store low-order 1 to 4 bytes of $t1 into memory, starting with high-order byte of word containing effective byte address and continuing through that byte address",
            InstructionFormat32.I,
            null, "101110 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    int address = cpuState.getReg(statement.rs_fs) + statement.imm;
                    int source = cpuState.getReg(statement.rt_ft);
                    for (int i = 0; i <= 3 - (address % 4); i++) {
                        memory.store8(address + i, Format.getByte(source, i));
                    }
                }
            });
    public static final TxInstruction luiInstruction = new TxInstruction("lui", "j, u", "jV", "lui $t1,100", // todo disassemble with << 16
            "Load Upper Immediate: Set high-order 16 bits of $t1 to 16-bit immediate and low-order 16 bits to 0",
            InstructionFormat32.I,
            null, "001111 00000 fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, statement.imm << 16);
                }
            });
    // TODO: delay slot work
    public static final TxInstruction beqInstruction = new TxInstruction("beq", "i, j, 4ru", "", "beq $t1,$t2,label",
            "Branch if EQual: Branch to statement at label's address if $t1 and $t2 are equal",
            InstructionFormat32.I_BRANCH,
            null, "000100 fffff sssss tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) == cpuState.getReg(statement.rt_ft)) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // alternative if rt=r0
    // TODO: delay slot work
    public static final TxInstruction beqzInstruction = new TxInstruction("beqz", "i, 4ru", "", "beqz $t1,label",
            "Branch if EQual Zero: Branch to statement at label's address if $t1 is zero",
            InstructionFormat32.I_BRANCH,
            null, "000100 fffff 00000 tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) == 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction beqlInstruction = new TxInstruction("beql", "i, j, 4ru", "", "beql $t1,$t2,label",
            "Branch if EQual (Likely): Branch to statement at label's address if $t1 and $t2 are equal",
            InstructionFormat32.I_BRANCH,
            null, "010100 fffff sssss tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) == cpuState.getReg(statement.rt_ft)) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // alternative if rt=r0
    // TODO: delay slot work
    public static final TxInstruction beqzlInstruction = new TxInstruction("beqzl", "i, 4ru", "", "beqzl $t1,label",
            "Branch if EQual Zero (Likely): Branch to statement at label's address if $t1 is zero",
            InstructionFormat32.I_BRANCH,
            null, "010100 fffff 00000 tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) == 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bneInstruction = new TxInstruction("bne", "i, j, 4ru", "", "bne $t1,$t2,label",
            "Branch if Not Equal: Branch to statement at label's address if $t1 and $t2 are not equal",
            InstructionFormat32.I_BRANCH,
            null, "000101 fffff sssss tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) != cpuState.getReg(statement.rt_ft)) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // alternative if rt=r0
    // TODO: delay slot work
    public static final TxInstruction bnezInstruction = new TxInstruction("bnez", "i, 4ru", "", "bnez $t1,label",
            "Branch if Not Equal Zero: Branch to statement at label's address if $t1 is not zero",
            InstructionFormat32.I_BRANCH,
            null, "000101 fffff 00000 tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) != 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bnelInstruction = new TxInstruction("bnel", "i, j, 4ru", "", "bnel $t1,$t2,label",
            "Branch if Not Equal (Likely): Branch to statement at label's address if $t1 and $t2 are not equal",
            InstructionFormat32.I_BRANCH,
            null, "010101 fffff sssss tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) != cpuState.getReg(statement.rt_ft)) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // alternative if rt=r0
    // TODO: delay slot work
    public static final TxInstruction bnezlInstruction = new TxInstruction("bnezl", "i, 4ru", "", "bnezl $t1,label",
            "Branch if Not Equal Zero (Likely): Branch to statement at label's address if $t1 is not zero",
            InstructionFormat32.I_BRANCH,
            null, "010101 fffff 00000 tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) != 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bgezInstruction = new TxInstruction("bgez", "i, 4rs", "", "bgez $t1,label",
            "Branch if Greater than or Equal to Zero: Branch to statement at label's address if $t1 is greater than or equal to zero",
            InstructionFormat32.I_BRANCH,
            null, "000001 fffff 00001 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) >= 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bgezlInstruction = new TxInstruction("bgezl", "i, 4rs", "", "bgezl $t1,label",
            "Branch if Greater than or Equal to Zero (Likely): Branch to statement at label's address if $t1 is greater than or equal to zero",
            InstructionFormat32.I_BRANCH,
            null, "000001 fffff 00011 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) >= 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bgezalInstruction = new TxInstruction("bgezal", "i, 4rs", "", "bgezal $t1,label",
            "Branch if Greater then or Equal to Zero And Link: If $t1 is greater than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            InstructionFormat32.I_BRANCH,
            null, "000001 fffff 10001 ssssssssssssssss",
            Instruction.FlowType.CALL, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) >= 0) {
                        cpuState.setReg(TxCPUState.RA, cpuState.pc + 8);
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bgezallInstruction = new TxInstruction("bgezall", "i, 4rs", "", "bgezall $t1,label",
            "Branch if Greater then or Equal to Zero And Link (Likely): If $t1 is greater than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            InstructionFormat32.I_BRANCH,
            null, "000001 fffff 10011 ssssssssssssssss",
            Instruction.FlowType.CALL, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) >= 0) {
                        cpuState.setReg(TxCPUState.RA, cpuState.pc + 8);
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bgtzInstruction = new TxInstruction("bgtz", "i, 4rs", "", "bgtz $t1,label",
            "Branch if Greater Than Zero: Branch to statement at label's address if $t1 is greater than zero",
            InstructionFormat32.I_BRANCH,
            null, "000111 fffff 00000 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) > 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bgtzlInstruction = new TxInstruction("bgtzl", "i, 4rs", "", "bgtzl $t1,label",
            "Branch if Greater Than Zero (Likely): Branch to statement at label's address if $t1 is greater than zero",
            InstructionFormat32.I_BRANCH,
            null, "010111 fffff 00000 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) > 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction blezInstruction = new TxInstruction("blez", "i, 4rs", "", "blez $t1,label",
            "Branch if Less than or Equal to Zero: Branch to statement at label's address if $t1 is less than or equal to zero",
            InstructionFormat32.I_BRANCH,
            null, "000110 fffff 00000 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) <= 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction blezlInstruction = new TxInstruction("blezl", "i, 4rs", "", "blezl $t1,label",
            "Branch if Less than or Equal to Zero (Likely): Branch to statement at label's address if $t1 is less than or equal to zero",
            InstructionFormat32.I_BRANCH,
            null, "010110 fffff 00000 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) <= 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bltzInstruction = new TxInstruction("bltz", "i, 4rs", "", "bltz $t1,label",
            "Branch if Less Than Zero: Branch to statement at label's address if $t1 is less than zero",
            InstructionFormat32.I_BRANCH,
            null, "000001 fffff 00000 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) < 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bltzlInstruction = new TxInstruction("bltzl", "i, 4rs", "", "bltzl $t1,label",
            "Branch if Less Than Zero (Likely): Branch to statement at label's address if $t1 is less than zero",
            InstructionFormat32.I_BRANCH,
            null, "000001 fffff 00010 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) < 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bltzalInstruction = new TxInstruction("bltzal", "i, 4rs", "", "bltzal $t1,label",
            "Branch if Less Than Zero And Link: If $t1 is less than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            InstructionFormat32.I_BRANCH,
            null, "000001 fffff 10000 ssssssssssssssss",
            Instruction.FlowType.CALL, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) < 0) {
                        cpuState.setReg(TxCPUState.RA, cpuState.pc + 8); // the "and link" part
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    // TODO: delay slot work
    public static final TxInstruction bltzallInstruction = new TxInstruction("bltzall", "i, 4rs", "", "bltzall $t1,label",
            "Branch if Less Than Zero And Link (Likely): If $t1 is less than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            InstructionFormat32.I_BRANCH,
            null, "000001 fffff 10010 ssssssssssssssss",
            Instruction.FlowType.CALL, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) < 0) {
                        cpuState.setReg(TxCPUState.RA, cpuState.pc + 8); // the "and link" part
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    public static final TxInstruction sltInstruction = new TxInstruction("slt", "k, i, j", "kw", "slt $t1,$t2,$t3",
            "Set on Less Than: If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 101010",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, (cpuState.getReg(statement.rs_fs) < cpuState.getReg(statement.rt_ft)) ? 1 : 0);
                }
            });
    public static final TxInstruction sltuInstruction = new TxInstruction("sltu", "k, i, j", "kw", "sltu $t1,$t2,$t3",
            "Set on Less Than Unsigned: If $t2 is less than $t3 using unsigned comparision, then set $t1 to 1 else set $t1 to 0",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 101011",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs_fs);
                    int second = cpuState.getReg(statement.rt_ft);
                    if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                        cpuState.setReg(statement.rd_fd, (first < second) ? 1 : 0);
                    } else {
                        cpuState.setReg(statement.rd_fd, (first >= 0) ? 1 : 0);
                    }
                }
            });
    public static final TxInstruction sltiInstruction = new TxInstruction("slti", "j, i, s", "jw", "slti $t1,$t2,-100",
            "Set Less Than Immediate: If $t2 is less than sign-extended 16-bit immediate, then set $t1 to 1 else set $t1 to 0",
            InstructionFormat32.I,
            null, "001010 sssss fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // 16 bit immediate value in statement.imm is sign-extended
                    cpuState.setReg(statement.rt_ft, (cpuState.getReg(statement.rs_fs) < (statement.imm << 16 >> 16)) ? 1 : 0);
                }
            });
    public static final TxInstruction sltiuInstruction = new TxInstruction("sltiu", "j, i, imm", "jw", "sltiu $t1,$t2,-100",
            "Set Less Than Immediate Unsigned: If $t2 is less than  sign-extended 16-bit immediate using unsigned comparison, then set $t1 to 1 else set $t1 to 0",
            InstructionFormat32.I,
            null, "001011 sssss fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs_fs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                        cpuState.setReg(statement.rt_ft, (first < second) ? 1 : 0);
                    } else {
                        cpuState.setReg(statement.rt_ft, (first >= 0) ? 1 : 0);
                    }
                }
            });
    public static final TxInstruction movnInstruction = new TxInstruction("movn", "k, i, j", "kw", "movn $t1,$t2,$t3",
            "MOVe conditional on Non zero: Set $t1 to $t2 if $t3 is not zero",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 001011",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt_ft) != 0) {
                        cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rs_fs));
                    }
                }
            });
    public static final TxInstruction movzInstruction = new TxInstruction("movz", "k, i, j", "kw", "movz $t1,$t2,$t3",
            "MOVe conditional on Zero: Set $t1 to $t2 if $t3 is zero",
            InstructionFormat32.R,
            null, "000000 sssss ttttt fffff 00000 001010",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt_ft) == 0) {
                        cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rs_fs));
                    }
                }
            });
    public static final TxInstruction breakInstruction = new TxInstruction("break", "u", "", "break 100",
            "Break execution with code: Terminate program execution with specified exception code",
            InstructionFormat32.BREAK, InstructionFormat16.BREAK,
            "000000 ffffffffffffffffffff 001101",
            Instruction.FlowType.INT, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // so will just halt execution with a message.
                    throw new TxEmulationException(statement, "break instruction executed; code = " +
                            statement.imm + ".", Exceptions.BREAKPOINT_EXCEPTION);
                }
            });
    // TODO: delay slot work
    public static final TxInstruction jInstruction = new TxInstruction("j", "4Ru", "", "j target",
            "Jump unconditionally: Jump to statement at target address",
            InstructionFormat32.J,
            null, "000010 ffffffffffffffffffffffffff",
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.pc = (cpuState.pc & 0xF0000000) | (statement.imm << 2);
                }
            });
    // TODO: delay slot work
    // TODO handle ISA mode switch
    public static final TxInstruction jrInstruction = new TxInstruction("jr", "i;Iu", "", "jr $t1",
            "Jump Register unconditionally: Jump to statement whose address is in $t1",
            InstructionFormat32.R, InstructionFormat16.RI,
            "000000 fffff 00000 00000 00000 001000",
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.pc = cpuState.getReg(statement.rs_fs);
                }
            });
    // alternative if rs=ra
    // TODO: delay slot work
    // TODO handle ISA mode switch
    public static final TxInstruction retInstruction = new TxInstruction("ret", "", "", "ret",
            "RETurn (formally a JR to $ra): Return to calling statement",
            InstructionFormat32.R,
            null, "000000 fffff 00000 00000 00000 001000",
            Instruction.FlowType.RET, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.pc = cpuState.getReg(statement.rs_fs);
                }
            });
    // TODO: delay slot work
    public static final TxInstruction jalInstruction = new TxInstruction("jal", "4Ru", "", "jal target", // TODO put address in comment
            "Jump And Link: Set $ra to Program Counter (return address) then jump to statement at target address",
            InstructionFormat32.J,
            null, "000011 ffffffffffffffffffffffffff",
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.RA, cpuState.pc + 8);
                    cpuState.pc = (cpuState.pc & 0xF0000000) | (statement.imm << 2);
                }
            });
    // TODO: delay slot work
    // TODO handle ISA mode switch
    public static final TxInstruction jalxInstruction = new TxInstruction("jalx", "4Ru", "", "jalx target", // TODO put address in comment
            "Jump And Link eXchanging isa mode: Set $ra to Program Counter (return address) then jump to statement at target address, toggling ISA mode",
            InstructionFormat32.J,
            null, "011101 ffffffffffffffffffffffffff",
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.RA, cpuState.pc + 8);
                    cpuState.pc = (cpuState.pc & 0xF0000000) | (statement.imm << 2);
                }
            });
    // TODO: delay slot work
    // TODO handle ISA mode switch
    public static final TxInstruction jalrInstruction = new TxInstruction("jalr", "(k,) i;Iu", "", "jalr $t1,$t2", // TODO omit rd if rd=$ra
            "Jump And Link Register: Set $t1 to Program Counter (return address) then jump to statement whose address is in $t2",
            InstructionFormat32.R,
            null, "000000 sssss 00000 fffff 00000 001001",
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.pc + 8);
                    cpuState.pc = cpuState.getReg(statement.rs_fs);
                }
            });
    public static final TxInstruction cloInstruction = new TxInstruction("clo", "k, i", "kw", "clo $t1,$t2",
            "Count number of Leading Ones: Set $t1 to the count of leading one bits in $t2 starting at most significant bit position",
            InstructionFormat32.R,
            null, "011100 sssss 00000 fffff 00000 100001",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int value = cpuState.getReg(statement.rs_fs);
                    int leadingOnes = 0;
                    int bitPosition = 31;
                    while (Format.bitValue(value, bitPosition) == 1 && bitPosition >= 0) {
                        leadingOnes++;
                        bitPosition--;
                    }
                    cpuState.setReg(statement.rd_fd, leadingOnes);
                }
            });
    public static final TxInstruction clzInstruction = new TxInstruction("clz", "k, i", "kw", "clz $t1,$t2",
            "Count number of Leading Zeroes: Set $t1 to the count of leading zero bits in $t2 starting at most significant bit positio",
            InstructionFormat32.R,
            null, "011100 sssss 00000 fffff 00000 100000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int value = cpuState.getReg(statement.rs_fs);
                    int leadingZeros = 0;
                    int bitPosition = 31;
                    while (Format.bitValue(value, bitPosition) == 0 && bitPosition >= 0) {
                        leadingZeros++;
                        bitPosition--;
                    }
                    cpuState.setReg(statement.rd_fd, leadingZeros);
                }
            });

    public static final TxInstruction mfc0Instruction = new TxInstruction("mfc0", "j, k", "jw", "mfc0 $t1,$8",
            "Move From Coprocessor 0: Set $t1 to the value stored in Coprocessor 0 register $8",
            InstructionFormat32.CP0,
            null, "010000 00000 fffff sssss 00000000 eee",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, cpuState.getReg(statement.rd_fd));
                }
            });

    public static final TxInstruction mtc0Instruction = new TxInstruction("mtc0", "j, k", "", "mtc0 $t1,$8",
            "Move To Coprocessor 0: Set Coprocessor 0 register $8 to value stored in $t1",
            InstructionFormat32.CP0,
            null, "010000 00100 fffff sssss 00000000 eee",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd_fd, cpuState.getReg(statement.rt_ft)) ;
                }
            });

    /////////////////////// CP1 and Floating Point Instructions Start Here ////////////////
    public static final TxInstruction mfc1Instruction = new TxInstruction("mfc1", "j, i", "jw", "mfc1 $t1,$8",
            "Move From Coprocessor 1: Set $t1 to the value stored in Coprocessor 1 register $8",
            InstructionFormat32.CP1_R1,
            null, "010000 00000 fffff sssss 00000000 000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, cpuState.getReg(statement.rs_fs/*fs*/));
                }
            });

    public static final TxInstruction mtc1Instruction = new TxInstruction("mtc1", "j, i", "", "mtc1 $t1,$8",
            "Move To Coprocessor 1: Set Coprocessor 1 register $8 to value stored in $t1",
            InstructionFormat32.CP1_R1,
            null, "010000 00100 fffff sssss 00000000 000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rs_fs/*fs*/, cpuState.getReg(statement.rt_ft)) ;
                }
            });

    public static final TxInstruction cfc1Instruction = new TxInstruction("cfc1", "j, i", "jw", "cfc1 $t1,$8",
            "move Control From Coprocessor 1: Set $t1 to the value stored in coprocessor 1 control register $8",
            InstructionFormat32.CP1_CR1,
            null, "010000 00000 fffff sssss 00000000 000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, cpuState.getCp1CrReg(statement.rs_fs/*cr#*/));
                }
            });

    public static final TxInstruction ctc1Instruction = new TxInstruction("ctc1", "j, i", "", "ctc1 $t1,$8",
            "move Control To Coprocessor 1: Set coprocessor 1 control register $8 to value stored in $t1",
            InstructionFormat32.CP1_CR1,
            null, "010000 00100 fffff sssss 00000000 000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setCp1CrReg(statement.rs_fs/*cr#*/, cpuState.getReg(statement.rt_ft)) ;
                }
            });


    public static final TxInstruction lwc1Instruction = new TxInstruction("lwc1", "j, s(i)", "jw", "lwc1 $f1,-100($t2)",
            "Load Word into Coprocessor 1 (FPU): Set $f1 to 32-bit value from effective memory word address",
            InstructionFormat32.CP1_I,
            null, "110001 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    cpuState.setReg(statement.rt_ft, memory.load32(cpuState.getReg(statement.rs_fs) + statement.imm));
                }
            });

    public static final TxInstruction swc1Instruction = new TxInstruction("swc1", "j, s(i)", "", "swc1 $f1,-100($t2)",
            "Store Word from Coprocessor 1 (FPU): Store 32 bit value in $f1 to effective memory word address",
            InstructionFormat32.CP1_I,
            null, "111001 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    memory.store32(cpuState.getReg(statement.rs_fs) + statement.imm, cpuState.getReg(statement.rt_ft));
                }
            });

    public static final TxInstruction addSInstruction = new TxInstruction("add.s", "k, [i, ]j", "kw", "add.s $f0,$f1,$f3",
            "floating point ADDition Single precision: Set $f0 to single-precision floating point value of $f1 plus $f3",
            InstructionFormat32.CP1_R2,
            null, "010001 10000 ttttt sssss fffff 000000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        float add1 = Float.intBitsToFloat(cpuState.getReg(statement.rs_fs));
                        float add2 = Float.intBitsToFloat(cpuState.getReg(statement.rt_ft));
                        float sum = add1 + add2;
                        // overflow detected when sum is positive or negative infinity.
                        /*
                        if (sum == Float.NEGATIVE_INFINITY || sum == Float.POSITIVE_INFINITY) {
                          throw new ProcessingException(statement,"arithmetic overflow");
                        }
                        */
                        cpuState.setReg(statement.rd_fd, Float.floatToIntBits(sum));
                    }
                }
            }
    );
    public static final TxInstruction subSInstruction = new TxInstruction("sub.s", "k, [i, ]j", "kw", "sub.s $f0,$f1,$f3",
            "floating point SUBtraction Single precision: Set $f0 to single-precision floating point value of $f1  minus $f3",
            InstructionFormat32.CP1_R2,
            null, "010001 10000 ttttt sssss fffff 000001",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        float sub1 = Float.intBitsToFloat(cpuState.getReg(statement.rs_fs));
                        float sub2 = Float.intBitsToFloat(cpuState.getReg(statement.rt_ft));
                        float diff = sub1 - sub2;
                        cpuState.setReg(statement.rd_fd, Float.floatToIntBits(diff));
                    }
                }
            }
    );
    public static final TxInstruction mulSInstruction = new TxInstruction("mul.s", "k, [i, ]j", "kw", "mul.s $f0,$f1,$f3",
            "floating point MULtiplication Single precision: Set $f0 to single-precision floating point value of $f1 times $f3",
            InstructionFormat32.CP1_R2,
            null, "010001 10000 ttttt sssss fffff 000010",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        float mul1 = Float.intBitsToFloat(cpuState.getReg(statement.rs_fs));
                        float mul2 = Float.intBitsToFloat(cpuState.getReg(statement.rt_ft));
                        float prod = mul1 * mul2;
                        cpuState.setReg(statement.rd_fd, Float.floatToIntBits(prod));
                    }
                }
            });
    public static final TxInstruction divSInstruction = new TxInstruction("div.s", "k, [i, ]j", "kw", "div.s $f0,$f1,$f3",
            "floating point DIVision Single precision: Set $f0 to single-precision floating point value of $f1 divided by $f3",
            InstructionFormat32.CP1_R2,
            null, "010001 10000 ttttt sssss fffff 000011",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        float div1 = Float.intBitsToFloat(cpuState.getReg(statement.rs_fs));
                        float div2 = Float.intBitsToFloat(cpuState.getReg(statement.rt_ft));
                        float quot = div1 / div2;
                        cpuState.setReg(statement.rd_fd, Float.floatToIntBits(quot));
                    }
                }
            });

    public static final TxInstruction bc1fInstruction = new TxInstruction("bc1f", "[l, ]4ru", "", "bc1f 1,label",
            "Branch if specified fp condition of Coprocessor 1 flag False (BC1F, not BCLF): If Coprocessor 1 condition flag specified by immediate is false (zero) then branch to statement at label's address",
            InstructionFormat32.CP1_CC_BRANCH,
            null, "010001 01000 fff 00 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        if (cpuState.getConditionFlag(statement.sa_cc) == 0) {
                            cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                        }
                    }
                }
            });
    public static final TxInstruction bc1tInstruction = new TxInstruction("bc1t", "[l, ]4ru", "", "bc1t 1,label",
            "Branch if specified fp condition flag of Coprocessor 1 flag True (BC1T, not BCLT): If Coprocessor 1 condition flag specified by immediate is true (one) then branch to statement at label's address",
            InstructionFormat32.CP1_CC_BRANCH,
            null, "010001 01000 fff 01 ssssssssssssssss",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        if (cpuState.getConditionFlag(statement.sa_cc) == 1) {
                            cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                        }
                    }
                }
            });

    public static final TxInstruction cvtSWInstruction = new TxInstruction("cvt.s.w", "k, i", "kw", "cvt.s.w $f0,$f1",
            "ConVerT to Single precision from Word: Set $f0 to single precision equivalent of 32-bit integer value in $f2",
            InstructionFormat32.CP1_R2,
            null, "010001 10100 00000 sssss fffff 100000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        // convert integer to single (interpret $f1 value as int?)
                        cpuState.setReg(statement.rd_fd, Float.floatToIntBits((float) cpuState.getReg(statement.rs_fs)));
                    }
                }
            });
    public static final TxInstruction cvtWSInstruction = new TxInstruction("cvt.w.s", "k, i", "kw", "cvt.w.s $f0,$f1",
            "ConVerT to Word from Single precision: Set $f0 to 32-bit integer equivalent of single precision value in $f1",
            InstructionFormat32.CP1_R2,
            null, "010001 10000 00000 sssss fffff 100100",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        // convert single precision in $f1 to integer stored in $f0
                        cpuState.setReg(statement.rd_fd, (int) Float.intBitsToFloat(cpuState.getReg(statement.rs_fs)));
                    }
                }
            });


    public static final TxInstruction cEqSInstruction = new TxInstruction("c.eq.s", "[l, ]i, j", "", "c.eq.s 1,$f0,$f1",
            "Compare EQual Single precision: If $f0 is equal to $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false",
            InstructionFormat32.CP1_R_CC,
            null, "010001 10000 ttttt sssss fff 00 110010",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        float op1 = Float.intBitsToFloat(cpuState.getReg(statement.rs_fs));
                        float op2 = Float.intBitsToFloat(cpuState.getReg(statement.rt_ft));
                        if (op1 == op2)
                            cpuState.setConditionFlag(statement.sa_cc);
                        else
                            cpuState.clearConditionFlag(statement.sa_cc);
                    }
                }
            });
    public static final TxInstruction cLeSInstruction = new TxInstruction("c.le.s", "[l, ]i, j", "", "c.le.s 1,$f0,$f1",
            "Compare Less or Equal Single precision: If $f0 is less than or equal to $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false",
            InstructionFormat32.CP1_R_CC,
            null, "010001 10000 ttttt sssss fff 00 111110",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        float op1 = Float.intBitsToFloat(cpuState.getReg(statement.rs_fs));
                        float op2 = Float.intBitsToFloat(cpuState.getReg(statement.rt_ft));
                        if (op1 <= op2)
                            cpuState.setConditionFlag(statement.sa_cc);
                        else
                            cpuState.clearConditionFlag(statement.sa_cc);
                    }
                }
            });
    public static final TxInstruction cLtSInstruction = new TxInstruction("c.lt.s", "[l, ]i, j", "", "c.lt.s 1,$f0,$f1",
            "Compare Less Than Single precision: If $f0 is less than $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false",
            InstructionFormat32.CP1_R_CC,
            null, "010001 10000 ttttt sssss fff 00 111100",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    {
                        float op1 = Float.intBitsToFloat(cpuState.getReg(statement.rs_fs));
                        float op2 = Float.intBitsToFloat(cpuState.getReg(statement.rt_ft));
                        if (op1 < op2)
                            cpuState.setConditionFlag(statement.sa_cc);
                        else
                            cpuState.clearConditionFlag(statement.sa_cc);
                    }
                }
            });

    // TRAP instructions
    public static final TxInstruction teqInstruction = new TxInstruction("teq", "i, j, u", "", "teq $t1,$t2,$t3",
            "Trap if EQual: Trap with code $t3 if $t1 is equal to $t2",
            InstructionFormat32.TRAP,
            null, "000000 fffff sssss 00000 00000 110100",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) == cpuState.getReg(statement.rt_ft)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction teqiInstruction = new TxInstruction("teqi", "i, s", "", "teqi $t1,-100",
            "Trap if EQual to Immediate: Trap if $t1 is equal to sign-extended 16 bit immediate",
            InstructionFormat32.I,
            null, "000001 fffff 01100 ssssssssssssssss",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) == (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tneInstruction = new TxInstruction("tne", "i, j, u", "", "tne $t1,$t2",
            "Trap if Not Equal: Trap if $t1 is not equal to $t2",
            InstructionFormat32.TRAP,
            null, "000000 fffff sssss 00000 00000 110110",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) != cpuState.getReg(statement.rt_ft)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tneiInstruction = new TxInstruction("tnei", "i, s", "", "tnei $t1,-100",
            "Trap if Not Equal to Immediate: Trap if $t1 is not equal to sign-extended 16 bit immediate",
            InstructionFormat32.I,
            null, "000001 fffff 01110 ssssssssssssssss",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) != (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tgeInstruction = new TxInstruction("tge", "i, j, u", "", "tge $t1,$t2",
            "Trap if Greater or Equal: Trap if $t1 is greater than or equal to $t2",
            InstructionFormat32.TRAP,
            null, "000000 fffff sssss 00000 00000 110000",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) >= cpuState.getReg(statement.rt_ft)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tgeuInstruction = new TxInstruction("tgeu", "i, j, u", "", "tgeu $t1,$t2",
            "Trap if Greater or Equal Unsigned: Trap if $t1 is greater than or equal to $t2 using unsigned comparision",
            InstructionFormat32.TRAP,
            null, "000000 fffff sssss 00000 00000 110001",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs_fs);
                    int second = cpuState.getReg(statement.rt_ft);
                    // if signs same, do straight compare; if signs differ & first negative then first greater else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first >= second) : (first < 0)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tgeiInstruction = new TxInstruction("tgei", "i, s", "", "tgei $t1,-100",
            "Trap if Greater than or Equal to Immediate: Trap if $t1 greater than or equal to sign-extended 16 bit immediate",
            InstructionFormat32.I,
            null, "000001 fffff 01000 ssssssssssssssss",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) >= (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tgeiuInstruction = new TxInstruction("tgeiu", "i, s", "", "tgeiu $t1,-100",
            "Trap if Greater or Equal to Immediate unsigned: Trap if $t1 greater than or equal to sign-extended 16 bit immediate, unsigned comparison",
            InstructionFormat32.I,
            null, "000001 fffff 01001 ssssssssssssssss",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs_fs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    // if signs same, do straight compare; if signs differ & first negative then first greater else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first >= second) : (first < 0)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tltInstruction = new TxInstruction("tlt", "i, j, u", "", "tlt $t1,$t2",
            "Trap if Less Than: Trap if $t1 less than $t2",
            InstructionFormat32.TRAP,
            null, "000000 fffff sssss 00000 00000 110010",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) < cpuState.getReg(statement.rt_ft)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tltuInstruction = new TxInstruction("tltu", "i, j, u", "", "tltu $t1,$t2",
            "Trap if Less Than Unsigned: Trap if $t1 less than $t2, unsigned comparison",
            InstructionFormat32.TRAP,
            null, "000000 fffff sssss 00000 00000 110011",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs_fs);
                    int second = cpuState.getReg(statement.rt_ft);
                    // if signs same, do straight compare; if signs differ & first positive then first is less else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first < second) : (first >= 0)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tltiInstruction = new TxInstruction("tlti", "i, s", "", "tlti $t1,-100",
            "Trap if Less Than Immediate: Trap if $t1 less than sign-extended 16-bit immediate",
            InstructionFormat32.I,
            null, "000001 fffff 01010 ssssssssssssssss",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) < (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tltiuInstruction = new TxInstruction("tltiu", "i, s", "", "tltiu $t1,-100",
            "Trap if Less Than Immediate Uunsigned: Trap if $t1 less than sign-extended 16-bit immediate, unsigned comparison",
            InstructionFormat32.I,
            null, "000001 fffff 01011 ssssssssssssssss",
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs_fs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    // if signs same, do straight compare; if signs differ & first positive then first is less else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first < second) : (first >= 0)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });

    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lbInstruction = new TxInstruction("lb", "j, s(i)", "jw", "lb $t1,-100($t2)",
            "Load Byte: Set $t1 to signed 8-bit value from effective memory byte address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            "100000 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // offset is sign-extended and halfword value is loaded signed
                    cpuState.setReg(statement.rt_ft, memory.loadSigned8(cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lbuInstruction = new TxInstruction("lbu", "j, s(i)", "jw", "lbu $t1,-100($t2)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            "100100 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned8(cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lhInstruction = new TxInstruction("lh", "j, s(i)", "jw", "lh $t1,-100($t2)",
            "Load Halfword: Set $t1 to signed 16-bit value from effective memory halfword address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            "100001 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // offset is sign-extended and halfword value is loaded signed
                    cpuState.setReg(statement.rt_ft, memory.loadSigned16(cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lhuInstruction = new TxInstruction("lhu", "j, s(i)", "jw", "lhu $t1,-100($t2)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            "100101 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // offset is sign-extended and halfword value is loaded unsigned
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned16(cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                }
            });
    public static final TxInstruction sbInstruction = new TxInstruction("sb", "j, s(i)", "jw", "sb $t1,-100($t2)",
            "Store Byte: Store the low-order 8 bits of $t1 into the effective memory byte address",
            InstructionFormat32.I,
            null, "101000 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    memory.store8(
                            cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16),
                            cpuState.getReg(statement.rt_ft) & 0x000000ff);
                }
            });
    public static final TxInstruction shInstruction = new TxInstruction("sh", "j, s(i)", "jw", "sh $t1,-100($t2)",
            "Store Halfword: Store the low-order 16 bits of $t1 into the effective memory halfword address",
            InstructionFormat32.I,
            null, "101001 ttttt fffff ssssssssssssssss",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    memory.store16(
                            cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16),
                            cpuState.getReg(statement.rt_ft) & 0x0000ffff);
                }
            });
    public static final TxInstruction syncInstruction = new TxInstruction("sync", "", "", "sync",
            "SYNC: Wait for all operations to complete",
            InstructionFormat32.I,
            null, "000000 00000000000000000000 001111",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    /* nop. Simulator does not have any pipeline */
                }
            });
    public static final TxInstruction waitInstruction = new TxInstruction("wait", "", "", "wait",
            "WAIT: put the processor in stand-by",
            InstructionFormat32.I,
            null, "010000 1 0000000000000000000 100000",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (Format.bitValue(cpuState.getReg(TxCPUState.Status), TxCPUState.Status_RP_bit) == 1) {
                        cpuState.setPowerMode(TxCPUState.PowerMode.DOZE);
                    }
                    else {
                        cpuState.setPowerMode(TxCPUState.PowerMode.HALT);
                    }
                }
            });

    public static final TxInstruction eretInstruction = new TxInstruction("eret", "", "", "eret",
            "Exception RETurn: Set Program Counter to Coprocessor 0 EPC register value, clear Coprocessor Status exception level bit",
            InstructionFormat32.R,
            null, "010000 1 0000000000000000000 011000",
            Instruction.FlowType.RET, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // set EXL bit (bit 1) in Status register to 0 and set PC to EPC
                    cpuState.setReg(TxCPUState.Status, Format.clearBit(cpuState.getReg(TxCPUState.Status), TxCPUState.Status_EXL_bit));
                    cpuState.pc = cpuState.getReg(TxCPUState.EPC);
                }
            });




    /* ***************************************************************************************************
     * 16-bit specific instructions, or instructions with a different behaviour from the 32-bit equivalent
     * *************************************************************************************************** */


    public static final TxInstruction ac0iuInstruction = new TxInstruction("ac0iu", "", "", "ac0iu",
            "Add Coprocessor 0 Immediate Unsigned",
            null, InstructionFormat16.RRR3,
            "",
            Instruction.FlowType.NONE , false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, cpuState.getReg(statement.rt_ft) + statement.imm);
                }
            });

    public static final TxInstruction adjfpInstruction = new TxInstruction("addiu", "F, 4s" /* TODO shift of not according to EXTEND */, "" /* TODO action */, "addiu fp, -100",
            "ADD Immediate Unsigned to FP",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (statement.immBitWidth == 8) {
                        // If not EXTENDed, "The 8-bit immediate is shifted left by two bits and sign-extended"
                        cpuState.setReg(TxCPUState.FP, cpuState.getReg(TxCPUState.FP) + (statement.imm << 24 >> 22));
                    }
                    else {
                        // "When EXTENDed, the immediate operand is not shifted at all"
                        cpuState.setReg(TxCPUState.FP, cpuState.getReg(TxCPUState.FP) + (statement.imm << 16 >> 16));
                    }
                }
            });

    public static final TxInstruction addiu8Instruction = new TxInstruction("addiu", "i, s", "i+", "addiu $t1,-100",
            "ADDition Immediate 'Unsigned' without overflow: add signed 16-bit immediate to $t1, no overflow",
            null, InstructionFormat16.RI,
            "001001 sssss fffff tttttttttttttttt",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int shift = 32 - statement.immBitWidth;
                    cpuState.setReg(statement.rt_ft, cpuState.getReg(statement.rs_fs) + (statement.imm << shift >> shift));
                }
            });

    public static final TxInstruction addiupcInstruction = new TxInstruction("addiu", "i, P, 4s", "" /* TODO action */, "addiu r3, pc, 16",
            "ADD Immediate Unsigned with PC",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int basePc = cpuState.pc; // TODO: if in delay slot of JAL or JALX, should be the upper halfword of the JAL or JALX instruction
                    if (statement.immBitWidth == 8) {
                        // If not EXTENDed, "The 8-bit immediate is shifted left by two bits, zero-extended and added"
                        cpuState.setReg(statement.rt_ft, (basePc & 0xFFFFFFFC) + (statement.imm << 2));
                    }
                    else {
                        // "When EXTENDed, the immediate operand is not shifted at all"
                        cpuState.setReg(statement.rt_ft, (basePc & 0xFFFFFFFC) + (statement.imm << 16 >> 16));
                    }
                }
            });

    public static final TxInstruction addiuspInstruction = new TxInstruction("addiu", "i, S, 4s", "" /* TODO action */, "addiu r3, sp, 16",
            "ADD Immediate Unsigned with SP",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (statement.immBitWidth == 8) {
                        // If not EXTENDed, "The 8-bit immediate is shifted left by two bits, zero-extended and added"
                        cpuState.setReg(statement.rt_ft, cpuState.getReg(TxCPUState.SP) + (statement.imm << 2));
                    }
                    else {
                        // "When EXTENDed, the immediate operand is not shifted at all"
                        cpuState.setReg(statement.rt_ft, cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 16));
                    }
                }
            });

    public static final TxInstruction adjspInstruction = new TxInstruction("addiu", "S, 4s", "" /* TODO action */, "addiu sp, 16",
            "ADD Immediate Unsigned with SP",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (statement.immBitWidth == 8) {
                        // If not EXTENDed, "The 8-bit immediate is shifted left by three bits and sign-extended"
                        cpuState.setReg(TxCPUState.SP, cpuState.getReg(TxCPUState.SP) + (statement.imm << 24 >> 21));
                    }
                    else {
                        // "When EXTENDed, the immediate operand is not shifted at all"
                        cpuState.setReg(TxCPUState.SP, cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 16));
                    }
                }
            });

    public static final TxInstruction bInstruction = new TxInstruction("b", "2ru", "", "b 100",
            "unconditional Branch: branch to target address",
            null, InstructionFormat16.I,
            "",
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int shift = 32 - statement.immBitWidth;
                    cpuState.pc += statement.imm << shift >> (shift-1);
                }
            });

    public static final TxInstruction balInstruction = new TxInstruction("bal", "2ru", "", "bal 100",
            "unconditional Branch And Link: branch to target address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.RA, cpuState.pc + 5); // TODO check
                    int shift = 32 - statement.immBitWidth;
                    cpuState.pc += statement.imm << shift >> (shift-1);
                }
            });

    public static final TxInstruction bclrInstruction = new TxInstruction("bclr", "u(i), l", "", "bclr 4(sp), 7",
            "Bit CLeaR: clear given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int address = statement.imm;
                    switch (statement.rs_fs) {
                        //case 0b00: address += 0; break; // useless
                        case 0b01: address += cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address += cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address += cpuState.getReg(TxCPUState.FP); break;
                    }
                    memory.store8(address, memory.loadUnsigned8(address) & (~(1 << statement.sa_cc)));
                }
            });

    // TODO: delay slot work
    public static final TxInstruction beqz1Instruction = new TxInstruction("beqz", "i, 2ru", "", "beqz $t1,label",
            "Branch if EQual Zero: Branch to statement at label's address if $t1 is zero",
            null, InstructionFormat16.RI,
            "000100 fffff 00000 tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) == 0) {
                        int shift = 32 - statement.immBitWidth;
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << shift >> (shift-1)); // sign extend and x4
                    }
                }
            });

    public static final TxInstruction bextInstruction = new TxInstruction("bext", "u(i), l", "", "bext 4(sp), 7",
            "Bit EXTract: extract given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int address = statement.imm;
                    switch (statement.rs_fs) {
                        //case 0b00: address += 0; break; // useless
                        case 0b01: address += cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address += cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address += cpuState.getReg(TxCPUState.FP); break;
                    }
                    cpuState.setReg(24 /*t8*/, ((memory.loadUnsigned8(address) & (1 << statement.sa_cc)) == 0)?0:1);
                }
            });

    public static final TxInstruction binsInstruction = new TxInstruction("bins", "u(i), l", "", "bins 4(sp), 7",
            "Bit INSert: insert given bit at memory address",
            null, InstructionFormat16.SPC_BIT,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int address = statement.imm;
                    switch (statement.rs_fs) {
                        //case 0b00: address += 0; break; // useless
                        case 0b01: address += cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address += cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address += cpuState.getReg(TxCPUState.FP); break;
                    }
                    if ((cpuState.getReg(24 /*t8*/) & 1) == 0) {
                        // Clear bit
                        memory.store8(address, memory.loadUnsigned8(address) & (~(1 << statement.sa_cc)));
                    }
                    else {
                        // set bit
                        memory.store8(address, memory.loadUnsigned8(address) | (~(1 << statement.sa_cc)));
                    }
                }
            });

    // TODO: delay slot work
    public static final TxInstruction bnez1Instruction = new TxInstruction("bnez", "i, 2ru", "", "bnez $t1,label",
            "Branch if Not Equal Zero: Branch to statement at label's address if $t1 is not zero",
            null, InstructionFormat16.RI,
            "000101 fffff 00000 tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs_fs) != 0) {
                        int shift = 32 - statement.immBitWidth;
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << shift >> (shift-1)); // sign extend and x4
                    }
                }
            });

    public static final TxInstruction bsetInstruction = new TxInstruction("bset", "u(i), l", "", "bset 4(sp), 7",
            "Bit SET: set given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int address = statement.imm;
                    switch (statement.rs_fs) {
                        //case 0b00: address += 0; break; // useless
                        case 0b01: address += cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address += cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address += cpuState.getReg(TxCPUState.FP); break;
                    }
                    memory.store8(address, memory.loadUnsigned8(address) | (1 << statement.sa_cc));
                }
            });

    // TODO: delay slot work
    public static final TxInstruction bteqzInstruction = new TxInstruction("bteqz", "2ru", "", "bteqz label",
            "Branch if T8 EQual Zero: Branch to statement at label's address if $t1 is not zero",
            null, InstructionFormat16.RI,
            "000101 fffff 00000 tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(24 /*t8*/) == 0) {
                        int shift = 32 - statement.immBitWidth;
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << shift >> (shift-1)); // sign extend and x4
                    }
                }
            });

    // TODO: delay slot work
    public static final TxInstruction btnezInstruction = new TxInstruction("btnez", "2ru", "", "btnez label",
            "Branch if T8 Not Equal Zero: Branch to statement at label's address if $t1 is not zero",
            null, InstructionFormat16.RI,
            "000101 fffff 00000 tttttttttttttttt",
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(24 /*t8*/) != 0) {
                        int shift = 32 - statement.immBitWidth;
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << shift >> (shift-1)); // sign extend and x4
                    }
                }
            });

    public static final TxInstruction btstInstruction = new TxInstruction("btst", "u(i), l", "", "btst 4(sp), 7",
            "Bit TeST: extract given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int address = statement.imm;
                    switch (statement.rs_fs) {
                        //case 0b00: address += 0; break; // useless
                        case 0b01: address += cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address += cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address += cpuState.getReg(TxCPUState.FP); break;
                    }
                    cpuState.setReg(24 /*t8*/, ((memory.loadUnsigned8(address) & (1 << statement.sa_cc)) == 0)?1:0); // !bext
                }
            });

    public static final TxInstruction cmpInstruction = new TxInstruction("cmp", "i, j", "", "cmp $t1, $t2",
            "CoMPare: set t8 to 0 if registers are equal",
            null, InstructionFormat16.RR,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(24 /*t8*/, cpuState.getReg(statement.rs_fs) ^ cpuState.getReg(statement.rt_ft));
                }
            });

    public static final TxInstruction cmpiInstruction = new TxInstruction("cmpi", "i, u", "", "cmpi $t1, 15",
            "CoMPare Immediate: set t8 to 0 if register equals given value",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(24 /*t8*/, cpuState.getReg(statement.rs_fs) ^ statement.imm);
                }
            });

    public static final TxInstruction diInstruction = new TxInstruction("di", "", "", "di",
            "Disable Interrupt: clears the IE bit of the status register",
            null, InstructionFormat16.I,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.Status, Format.clearBit(cpuState.getReg(TxCPUState.Status), TxCPUState.Status_IE_bit));
                }
            });

    public static final TxInstruction diveInstruction = new TxInstruction("dive", "i, j", "iw", "dive $t1,$t2",
            "DIVision with Exception: Divide $t1 by $t2 then set LO to quotient and HI to remainder",
            InstructionFormat32.R, InstructionFormat16.RR,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt_ft) == 0) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.DIVIDE_BY_ZERO_EXCEPTION);
                    }
                    cpuState.setReg(TxCPUState.HI, cpuState.getReg(statement.rs_fs) % cpuState.getReg(statement.rt_ft));
                    cpuState.setReg(TxCPUState.LO, cpuState.getReg(statement.rs_fs) / cpuState.getReg(statement.rt_ft));
                }
            });

    public static final TxInstruction diveuInstruction = new TxInstruction("divu", "i, j", "iw", "divu $t1,$t2",
            "DIVision with Exception Unsigned: Divide unsigned $t1 by $t2 then set LO to quotient and HI to remainder",
            null, InstructionFormat16.RR,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt_ft) == 0) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.DIVIDE_BY_ZERO_EXCEPTION);
                    }
                    long oper1 = ((long) cpuState.getReg(statement.rs_fs)) << 32 >>> 32;
                    long oper2 = ((long) cpuState.getReg(statement.rt_ft)) << 32 >>> 32;
                    cpuState.setReg(TxCPUState.HI, (int) (((oper1 % oper2) << 32) >> 32));
                    cpuState.setReg(TxCPUState.LO, (int) (((oper1 / oper2) << 32) >> 32));
                }
            });

    public static final TxInstruction eiInstruction = new TxInstruction("ei", "", "", "ei",
            "Enable Interrupt: sets the IE bit of the status register",
            null, InstructionFormat16.I,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.Status, Format.setBit(cpuState.getReg(TxCPUState.Status), TxCPUState.Status_IE_bit));
                }
            });

    // TODO: delay slot work
    // TODO handle ISA mode switch
    public static final TxInstruction jalr16Instruction = new TxInstruction("jalr", "i;Iu", "", "jalr $t2",
            "Jump And Link Register: Set $ra to Program Counter (return address) then jump to statement whose address is in $t2",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.RA, cpuState.pc + 5); // TODO 5 ??
                    cpuState.pc = cpuState.getReg(statement.rs_fs);
                }
            });

    // TODO handle ISA mode switch
    public static final TxInstruction jalrcInstruction = new TxInstruction("jalrc", "i;Iu", "", "jalrc $t2",
            "Jump And Link Register Compact: Set $ra to Program Counter (return address) then jump to statement whose address is in $t2",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.RA, cpuState.pc + 3); // TODO 3 ??
                    cpuState.pc = cpuState.getReg(statement.rs_fs);
                }
            });

    // TODO: delay slot work
    // TODO handle ISA mode switch
    public static final TxInstruction jrraInstruction = new TxInstruction("jr", "A", "", "jr $ra",
            "Jump Register RA unconditionally: Jump to statement whose address is in $ra",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.pc = cpuState.getReg(TxCPUState.RA);
                }
            });

    // TODO handle ISA mode switch
    public static final TxInstruction jrcraInstruction = new TxInstruction("jrc", "A", "", "jrc $ra",
            "Jump Register RA unconditionally Compact: Jump to statement whose address is in $ra",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.pc = cpuState.getReg(TxCPUState.RA);
                }
            });

    // TODO handle ISA mode switch
    public static final TxInstruction jrcInstruction = new TxInstruction("jrc", "i;Iu", "", "jrc $t2",
            "Jump Register unconditionally Compact: Jump to statement whose address is in $t2",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.pc = cpuState.getReg(statement.rs_fs);
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of lbInstruction: does not sign-extend offset
     */
    public static final TxInstruction lb16Instruction = new TxInstruction("lb", "j, u(i)", "jw", "lb $t1,-100($t2)",
            "Load Byte: Set $t1 to signed 8-bit value from effective memory byte address",
            null, InstructionFormat16.RRI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadSigned8(cpuState.getReg(statement.rs_fs) + statement.imm));
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of lbuInstruction: does not sign-extend offset
     */
    public static final TxInstruction lbu16Instruction = new TxInstruction("lbu", "j, u(i)", "jw", "lbu $t1,-100($t2)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.RRI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned8(cpuState.getReg(statement.rs_fs) + statement.imm));
                }
            });

    /**
     * EXTENDed 16-bit ISA version of lbufpInstruction: sign-extends offset
     */
    public static final TxInstruction lbufpInstruction = new TxInstruction("lbu", "j, s(F)", "jw", "lbu $t1,-100($fp)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned8(cpuState.getReg(TxCPUState.FP) + statement.imm << 16 >> 16));
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lbufpInstruction: does not sign-extend offset
     */
    public static final TxInstruction lbufp16Instruction = new TxInstruction("lbu", "j, u(F)", "jw", "lbu $t1,-100($fp)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned8(cpuState.getReg(TxCPUState.FP) + statement.imm));
                }
            });


    /**
     * EXTENDed 16-bit ISA version of lbuspInstruction: sign-extends offset
     */
    public static final TxInstruction lbuspInstruction = new TxInstruction("lbu", "j, s(S)", "jw", "lbu $t1,-100($sp)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned8(cpuState.getReg(TxCPUState.SP) + statement.imm << 16 >> 16));
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lbuspInstruction: does not sign-extend offset
     */
    public static final TxInstruction lbusp16Instruction = new TxInstruction("lbu", "j, u(S)", "jw", "lbu $t1,-100($sp)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned8(cpuState.getReg(TxCPUState.SP) + statement.imm));
                }
            });


    /**
     * non-EXTENDed 16-bit ISA version of lhInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction lh16Instruction = new TxInstruction("lh", "j, 2u(i)", "jw", "lh $t1,-100($t2)",
            "Load Halfword: Set $t1 to signed 16-bit value from effective memory halfword address",
            null, InstructionFormat16.RRI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadSigned16(cpuState.getReg(statement.rs_fs) + statement.imm << 1));
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of lhuInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction lhu16Instruction = new TxInstruction("lhu", "j, 2u(i)", "jw", "lhu $t1,-100($t2)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.RRI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned16(cpuState.getReg(statement.rs_fs) + statement.imm << 1));
                }
            });

    /**
     * EXTENDed 16-bit ISA version of lhufpInstruction: sign-extends offset and multiplies it by 2
     */
    public static final TxInstruction lhufpInstruction = new TxInstruction("lhu", "j, 2s(F)", "jw", "lhu $t1,-100($fp)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned16(cpuState.getReg(TxCPUState.FP) + statement.imm << 16 >> 15));
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lhufpInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction lhufp16Instruction = new TxInstruction("lhu", "j, 2u(F)", "jw", "lhu $t1,-100($fp)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned16(cpuState.getReg(TxCPUState.FP) + statement.imm << 1));
                }
            });


    /**
     * EXTENDed 16-bit ISA version of lhuspInstruction: sign-extends offset and multiplies it by 2
     */
    public static final TxInstruction lhuspInstruction = new TxInstruction("lhu", "j, 2s(S)", "jw", "lhu $t1,-100($sp)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned16(cpuState.getReg(TxCPUState.SP) + statement.imm << 16 >> 15));
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lhuspInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction lhusp16Instruction = new TxInstruction("lhu", "j, 2u(S)", "jw", "lhu $t1,-100($sp)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.loadUnsigned16(cpuState.getReg(TxCPUState.SP) + statement.imm << 1));
                }
            });

/* lhu > lw */

    /**
     * non-EXTENDed 16-bit ISA version of lwInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction lw16Instruction = new TxInstruction("lw", "j, 4u(i)", "jw", "lw $t1,-100($t2)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RRI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.load32(cpuState.getReg(statement.rs_fs) + statement.imm << 2));
                }
            });

    /**
     * EXTENDed 16-bit ISA version of lwfpInstruction: sign-extends offset
     */
    public static final TxInstruction lwfpInstruction = new TxInstruction("lw", "j, s(F)", "jw", "lw $t1,-100($fp)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RRI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.load32(cpuState.getReg(TxCPUState.FP) + statement.imm << 16 >> 16));
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lwfpInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction lwfp16Instruction = new TxInstruction("lw", "j, 4u(F)", "jw", "lw $t1,-100($fp)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RRI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.load32(cpuState.getReg(TxCPUState.FP) + statement.imm << 2));
                }
            });


    /**
     * EXTENDed 16-bit ISA version of lwpcInstruction: sign-extends offset
     */
    public static final TxInstruction lwpcInstruction = new TxInstruction("lw", "j, s(P)", "jw", "lw $t1,-100($pc)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // TODO mask PC
                    cpuState.setReg(statement.rt_ft, memory.load32(cpuState.pc + statement.imm << 16 >> 16));
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lwpcInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction lwpc16Instruction = new TxInstruction("lw", "j, 4u(P)", "jw", "lw $t1,-100($pc)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // TODO mask PC
                    cpuState.setReg(statement.rt_ft, memory.load32(cpuState.pc + statement.imm << 2));
                }
            });

    /**
     * EXTENDed 16-bit ISA version of lwspInstruction: sign-extends offset
     */
    public static final TxInstruction lwspInstruction = new TxInstruction("lw", "j, s(S)", "jw", "lw $t1,-100($sp)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.load32(cpuState.getReg(TxCPUState.SP) + statement.imm << 16 >> 16));
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lwspInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction lwsp16Instruction = new TxInstruction("lw", "j, 4u(S)", "jw", "lw $t1,-100($sp)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RI,
            "",
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt_ft, memory.load32(cpuState.getReg(TxCPUState.SP) + statement.imm << 2));
                }
            });





//    private SyscallLoader syscallLoader;
//
//    void populate() {
//        instructionList.add(
//                new TxInstruction("syscall",
//                        "Issue a system call: Execute the system call specified by value in $v0",
//                        TxInstruction.Format.R,
//                        "000000 00000 00000 00000 00000 001100",
//                        new SimulationCode()
//                        {
//                            public void simulate(TxStatement statement) throws EmulationException
//                            {
//                                findAndSimulateSyscall(RegisterFile.getValue(2),statement);
//                            }
//                        }));

//        ////////////// READ PSEUDO-INSTRUCTION SPECS FROM DATA FILE AND ADD //////////////////////
//        addPseudoInstructions();

//        ////////////// GET AND CREATE LIST OF SYSCALL FUNCTION OBJECTS ////////////////////
//        syscallLoader = new SyscallLoader();
//        syscallLoader.loadSyscalls();

//        // Initialization step.  Create token list for each instruction example.  This is
//        // used by parser to determine user program correct syntax.
//        for (int i = 0; i < instructionList.size(); i++)
//        {
//            TxInstruction inst = (TxInstruction) instructionList.get(i);
//            inst.createExampleTokenList();
//        }
//
//    }

//    /*
//         * Method to find and invoke a syscall given its service number.  Each syscall
//         * function is represented by an object in an array list.  Each object is of
//         * a class that implements Syscall or extends AbstractSyscall.
//         */
//
//    private void findAndSimulateSyscall(int number, TxStatement statement)
//            throws EmulationException {
//        Syscall service = syscallLoader.findSyscall(number);
//        if (service != null) {
//            service.simulate(statement);
//            return;
//        }
//        throw new TxEmulationException(statement,
//                "invalid or unimplemented syscall service: " +
//                        number + " ", Exceptions.SYSCALL_EXCEPTION);
//    }



    public static TxInstruction getInstructionFor16BitStatement(int binStatement) throws DisassemblyException {
        return opcode16Map[binStatement];
    }

    public static TxInstruction getExtendedInstructionFor16BitStatement(int binStatement) throws DisassemblyException {
        return extendedOpcode16Map[binStatement];
    }


    public static TxInstruction getInstructionFor32BitStatement(int binStatement) throws DisassemblyException {
        return opcode32Resolver.resolve(binStatement);
    }


    private abstract static class InstructionResolver {
        public abstract TxInstruction resolve(int binStatement) throws ReservedInstructionException;
    }

    public static class DirectInstructionResolver extends InstructionResolver {
        private TxInstruction instruction;

        public DirectInstructionResolver(TxInstruction instruction) {
            this.instruction = instruction;
        }

        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return instruction;
        }
    }


    /* Advanced resolvers to simplify instructions */

    static InstructionResolver sllOrNopResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (binStatement == 0) {
                return nopInstruction;
            }
            else {
                return sllInstruction;
            }

        }
    };

    static InstructionResolver jrOrRetResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 21) & 0b11111) == 0b11111) { // rs == ra
                return retInstruction;
            }
            else {
                return jrInstruction;
            }

        }
    };

    static InstructionResolver adduOrMoveResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 16) & 0b11111) == 0) { // rt == 0
                return moveAdduInstruction;
            }
            else {
                return adduInstruction;
            }

        }
    };

    static InstructionResolver orOrMoveResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 21) & 0b11111) == 0) { // rs == 0
                return moveOrInstruction;
            }
            else {
                return orInstruction;
            }

        }
    };

    static InstructionResolver addiuOrLiResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 21) & 0b11111) == 0) { // rs == 0
                return liAddiuInstruction;
            }
            else {
                return addiuInstruction;
            }

        }
    };

    static InstructionResolver oriOrLiResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 21) & 0b11111) == 0) { // rs == 0
                return liOriInstruction;
            }
            else {
                return oriInstruction;
            }

        }
    };

    static InstructionResolver beqOrBeqzResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 16) & 0b11111) == 0) {
                return beqzInstruction;
            }
            else {
                return beqInstruction;
            }

        }
    };

    static InstructionResolver bneOrBnezResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 16) & 0b11111) == 0) {
                return bnezInstruction;
            }
            else {
                return bneInstruction;
            }

        }
    };

    static InstructionResolver beqlOrBeqzlResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 16) & 0b11111) == 0) {
                return beqzlInstruction;
            }
            else {
                return beqlInstruction;
            }

        }
    };

    static InstructionResolver bnelOrBnezlResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 16) & 0b11111) == 0) {
                return bnezlInstruction;
            }
            else {
                return bnelInstruction;
            }

        }
    };

    /* Exception resolvers */

    static InstructionResolver starResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            throw new ReservedInstructionException();
        }
    };

    static InstructionResolver betaResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            throw new ReservedInstructionException();
        }
    };

    static InstructionResolver thetaResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            // Formally, See section 3.5:
            // If the corresponding CU bit in the Status register is cleared, a Coprocessor Unusable exception is taken.
            // If the CU bit is set, a Reserved Instruction exception is taken.
            throw new ReservedInstructionException();
        }
    };

    static InstructionResolver unimplementedResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            throw new ReservedInstructionException("Not yet implemented");
        }
    };

    /* Standard resolvers */

    static InstructionResolver[] opcodeResolvers;
    static InstructionResolver[] specialFunctionResolvers;
    static InstructionResolver[] regImmRtResolvers;
    static InstructionResolver[] special2FunctionResolvers;
    static InstructionResolver[] cop0RsResolvers;
    static InstructionResolver[] cop0CoFunctionResolvers;
    static InstructionResolver[] cop1RsResolvers;
    static InstructionResolver[] cop1SFunctionResolvers;
    static InstructionResolver[] cop1WLFunctionResolvers;
    static InstructionResolver bc1fResolver;
    static InstructionResolver bc1tResolver;


    private static InstructionResolver opcode32Resolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return opcodeResolvers[binStatement >>> 26].resolve(binStatement);
        }
    };

    private static InstructionResolver specialFunctionResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return specialFunctionResolvers[binStatement & 0b111111].resolve(binStatement);
        }
    };

    private static InstructionResolver regImmRtResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return regImmRtResolvers[(binStatement >>> 16) & 0b11111].resolve(binStatement);
        }
    };

    private static InstructionResolver special2FunctionResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return special2FunctionResolvers[binStatement & 0b111111].resolve(binStatement);
        }
    };

    private static InstructionResolver cop0RsResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return cop0RsResolvers[(binStatement >>> 21) & 0b11111].resolve(binStatement);
        }
    };

    private static InstructionResolver cop1RsResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return cop1RsResolvers[(binStatement >>> 21) & 0b11111].resolve(binStatement);
        }
    };

    private static InstructionResolver cop0CoFunctionResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return cop0CoFunctionResolvers[binStatement & 0b111111].resolve(binStatement);
        }
    };

    private static InstructionResolver cop1SFunctionResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return cop1SFunctionResolvers[binStatement & 0b111111].resolve(binStatement);
        }
    };

    private static InstructionResolver cop1WLFunctionResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return cop1WLFunctionResolvers[binStatement & 0b111111].resolve(binStatement);
        }
    };

    private static InstructionResolver bc1TFResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if ((binStatement & 0x10000) == 0) {
                return bc1fResolver.resolve(binStatement);
            }
            else {
                return bc1tResolver.resolve(binStatement);
            }
        }
    };


    static {
        // ----------------- 16-bits -----------------

        // These are rewrites of the Toshiba architecture document, appendix F

        // Fill with dummy instruction by default
        for (int i = 0; i < opcode16Map.length; i++) {
            opcode16Map[i] = dummyInstruction;
            extendedOpcode16Map[i] = dummyInstruction;
        }

        // Now patch with defined 16-bit instruction and operand combinations
        //                map                , encoding          , mask              , instruction
        expandInstruction(opcode16Map,         0b1110000000000000, 0b1111100010000011, ac0iuInstruction);

        expandInstruction(opcode16Map,         0b0110011000000000, 0b1111111100000000, adjfpInstruction);
        expandInstruction(extendedOpcode16Map, 0b0110011000000000, 0b1111111111100000, adjfpInstruction);

        expandInstruction(opcode16Map,         0b0100100000000000, 0b1111100000000000, addiu8Instruction);
        expandInstruction(extendedOpcode16Map, 0b0100100000000000, 0b1111100011100000, addiu8Instruction);

        expandInstruction(opcode16Map,         0b0000100000000000, 0b1111100000000000, addiupcInstruction);
        expandInstruction(extendedOpcode16Map, 0b0000100000000000, 0b1111100011100000, addiupcInstruction);

        expandInstruction(opcode16Map,         0b0000000000000000, 0b1111100000000000, addiuspInstruction);
        expandInstruction(extendedOpcode16Map, 0b0000000000000000, 0b1111100011100000, addiuspInstruction);

        expandInstruction(opcode16Map,         0b0100000000000000, 0b1111100000010000, addiuInstruction);
        expandInstruction(extendedOpcode16Map, 0b0100000000000000, 0b1111100000010000, addiuInstruction);

        expandInstruction(opcode16Map,         0b0110001100000000, 0b1111111100000000, adjspInstruction);
        expandInstruction(extendedOpcode16Map, 0b0110001100000000, 0b1111111111100000, adjspInstruction);

/*
        expandInstruction(extendedOpcode16Map, 0b0100100001000000, 0b1111100011100000, addmiuInstruction); // incl with $r0
*/

        expandInstruction(opcode16Map,         0b1110000000000001, 0b1111100000000011, adduInstruction);

        expandInstruction(opcode16Map,         0b1110100000001100, 0b1111100000011111, andInstruction);

        expandInstruction(extendedOpcode16Map, 0b0100100010000000, 0b1111100011100000, andiInstruction);

        expandInstruction(opcode16Map,         0b0001000000000000, 0b1111100000000000, bInstruction);
        expandInstruction(extendedOpcode16Map, 0b0001000000000000, 0b1111111111100000, bInstruction);

        expandInstruction(opcode16Map,         0b1111110000000000, 0b1111111100000000, balInstruction);
        expandInstruction(extendedOpcode16Map, 0b1111110000000000, 0b1111111111100000, balInstruction);

        expandInstruction(extendedOpcode16Map, 0b1111100100000000, 0b1111111100000000, bclrInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b1111100100000000, 0b1111111100000000, bclrInstruction);

        expandInstruction(opcode16Map,         0b0010000000000000, 0b1111100000000000, beqz1Instruction);
        expandInstruction(extendedOpcode16Map, 0b0010000000000000, 0b1111100011100000, beqz1Instruction);

        expandInstruction(extendedOpcode16Map, 0b1111110100000000, 0b1111111100000000, bextInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b1111110100000000, 0b1111111100000000, bextInstruction);

/*
        expandInstruction(extendedOpcode16Map, 0b1110100000000111, 0b1111100000011111, bfinsInstruction);
*/
        expandInstruction(extendedOpcode16Map, 0b1111101100000000, 0b1111111100000000, binsInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b1111101100000000, 0b1111111100000000, binsInstruction);

        expandInstruction(opcode16Map,         0b0010100000000000, 0b1111100000000000, bnez1Instruction);
        expandInstruction(extendedOpcode16Map, 0b0010100000000000, 0b1111100011100000, bnez1Instruction);

        expandInstruction(opcode16Map,         0b1110100000000101, 0b1111100000011111, breakInstruction);
/*
        expandInstruction(extendedOpcode16Map, 0b1110100000000111, 0b1111100000011111, bs1fInstruction);
*/
        expandInstruction(extendedOpcode16Map, 0b1111101000000000, 0b1111111100000000, bsetInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b1111101000000000, 0b1111111100000000, bsetInstruction);

        expandInstruction(opcode16Map,         0b0110000000000000, 0b1111111100000000, bteqzInstruction);
        expandInstruction(extendedOpcode16Map, 0b0110000000000000, 0b1111111111100000, bteqzInstruction);

        expandInstruction(opcode16Map,         0b0110000100000000, 0b1111111100000000, btnezInstruction);
        expandInstruction(extendedOpcode16Map, 0b0110000100000000, 0b1111111111100000, btnezInstruction);

        expandInstruction(extendedOpcode16Map, 0b1111100000000000, 0b1111111100000000, btstInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b1111100000000000, 0b1111111100000000, btstInstruction);

        expandInstruction(opcode16Map,         0b1110100000001010, 0b1111100000011111, cmpInstruction);

        expandInstruction(opcode16Map,         0b0111000000000000, 0b1111100000000000, cmpiInstruction);
        expandInstruction(extendedOpcode16Map, 0b0111000000000000, 0b1111100011100000, cmpiInstruction);

/*
        expandInstruction(extendedOpcode16Map, 0b1110100000011111, 0b1111111111111111, deretInstruction); // + constraints on extended part
*/

        expandInstruction(opcode16Map,         0b1110000010000000, 0b1111111111111111, diInstruction);

        expandInstruction(opcode16Map,         0b1110100000011010, 0b1111100000011111, divInstruction);

        expandInstruction(opcode16Map,         0b1110100000011110, 0b1111100000011111, diveInstruction);

        expandInstruction(opcode16Map,         0b1110100000011111, 0b1111100000011111, diveuInstruction);

        expandInstruction(opcode16Map,         0b1110100000011011, 0b1111100000011111, divuInstruction);

        expandInstruction(opcode16Map,         0b1110000110000000, 0b1111111111111111, eiInstruction);

        expandInstruction(extendedOpcode16Map, 0b1110100000011000, 0b1111111111111111, eretInstruction); // + constraints on extended part ?
/*
        TODO JAL;
*/
        expandInstruction(opcode16Map,         0b1110100001000000, 0b1111100011111111, jalr16Instruction);

        expandInstruction(opcode16Map,         0b1110100011000000, 0b1111100011111111, jalrcInstruction);
/*
        TODO JALX;
*/

        expandInstruction(opcode16Map,         0b1110100000000000, 0b1111100011111111, jrInstruction);

        expandInstruction(opcode16Map,         0b1110100000100000, 0b1111111111111111, jrraInstruction);

        expandInstruction(opcode16Map,         0b1110100010100000, 0b1111111111111111, jrcraInstruction);

        expandInstruction(opcode16Map,         0b1110100010000000, 0b1111100011111111, jrcInstruction);

        expandInstruction(opcode16Map,         0b1000000000000000, 0b1111100000000000, lb16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1000000000000000, 0b1111100000000000, lbInstruction);

        expandInstruction(opcode16Map,         0b1010000000000000, 0b1111100000000000, lbu16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1010000000000000, 0b1111100000000000, lbuInstruction);

        expandInstruction(opcode16Map,         0b0011100000000000, 0b1111100010000000, lbufp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b0011100000000000, 0b1111100011100000, lbufpInstruction);

        expandInstruction(opcode16Map,         0b0111100000000000, 0b1111100010000000, lbusp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b0111100000000000, 0b1111100011100000, lbuspInstruction);

        expandInstruction(opcode16Map,         0b1000100000000000, 0b1111100000000000, lh16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1000100000000000, 0b1111100000000000, lhInstruction);

        expandInstruction(opcode16Map,         0b1010100000000000, 0b1111100000000000, lhu16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1010100000000000, 0b1111100000000000, lhuInstruction);

        expandInstruction(opcode16Map,         0b1011100000000001, 0b1111100010000001, lhufp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1011100000000001, 0b1111100011100001, lhufpInstruction);

        expandInstruction(opcode16Map,         0b1011100000000000, 0b1111100010000001, lhusp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1011100000000000, 0b1111100011100001, lhuspInstruction);

/*
        expandInstruction(opcode16Map,         0b0110100000000000, 0b1111100000000000, li Instruction);
        expandInstruction(extendedOpcode16Map, 0b0110100000000000, 0b1111100011100000, li Instruction);

        expandInstruction(extendedOpcode16Map, 0b0100100011100000, 0b1111100011100000, lui Instruction);
*/
        expandInstruction(opcode16Map,         0b1001100000000000, 0b1111100000000000, lw16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1001100000000000, 0b1111100000000000, lwInstruction);

        expandInstruction(opcode16Map,         0b1111111000000000, 0b1111111100000000, lwfp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1111111000000000, 0b1111111100000000, lwfpInstruction);

        expandInstruction(opcode16Map,         0b1001000000000000, 0b1111100000000000, lwsp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1001000000000000, 0b1111100011100000, lwspInstruction);

        expandInstruction(opcode16Map,         0b1011000000000000, 0b1111100000000000, lwpc16Instruction);
        expandInstruction(extendedOpcode16Map, 0b1011000000000000, 0b1111100011100000, lwpcInstruction);


/*
        expandInstruction(opcode16Map,         0b1110100000010110, 0b1111100000011111, madd Instruction);

        expandInstruction(opcode16Map,         0b1110100000010111, 0b1111100000011111, maddu Instruction);

        expandInstruction(extendedOpcode16Map, 0b1110100000000101, 0b1111100000011111, max Instruction); // !! + constraints on extended part

        expandInstruction(opcode16Map,         0b0011000000000001, 0b1111100000000111, mfc0 Instruction);

        expandInstruction(opcode16Map,         0b1110100000010000, 0b1111100011111111, mfhi Instruction);

        expandInstruction(opcode16Map,         0b1110100000010010, 0b1111100011111111, mflo Instruction);

        expandInstruction(extendedOpcode16Map, 0b1110100000000101, 0b1111100000011111, min Instruction); // !! + constraints on extended part

        expandInstruction(opcode16Map,         0b1110110000001000, 0b1111110000011111, move1 Instruction);

        expandInstruction(opcode16Map,         0b0110011100000000, 0b1111111100000000, move2 Instruction);

        expandInstruction(opcode16Map,         0b0110010100000000, 0b1111111100000000, move3 Instruction);

        expandInstruction(opcode16Map,         0b0011000000000101, 0b1111100000000111, mtc0 Instruction);

        expandInstruction(opcode16Map,         0b1110000000000010, 0b1111100011111111, mthi Instruction);

        expandInstruction(opcode16Map,         0b1110000010000010, 0b1111100011111111, mtlo Instruction);

        expandInstruction(opcode16Map,         0b1110100000011100, 0b1111100000011111, mult1 Instruction);

        expandInstruction(opcode16Map,         0b1110100000011000, 0b1111100000011111, mult2 Instruction);

        expandInstruction(opcode16Map,         0b1110100000011001, 0b1111100000011111, multu1 Instruction);

        expandInstruction(opcode16Map,         0b1110100000011101, 0b1111100000011111, multu2 Instruction);

        expandInstruction(opcode16Map,         0b1110100000001011, 0b1111100000011111, negInstruction);

        expandInstruction(opcode16Map,         0b1110100000001111, 0b1111100000011111, notInstruction);

        expandInstruction(opcode16Map,         0b1110100000011101, 0b1111100000011111, or Instruction);

        expandInstruction(extendedOpcode16Map, 0b0100100010100000, 0b1111100011100000, ori Instruction);

        expandInstruction(opcode16Map,         0b0110010000000000, 0b1111111110000000, restore1Instruction);

        expandInstruction(extendedOpcode16Map, 0b0110010000000000, 0b1111111110000000, restore2Instruction);

        expandInstruction(opcode16Map,         0b1110100000010100, 0b1111100000011111, saddInstruction);

        expandInstruction(opcode16Map,         0b0110010010000000, 0b1111111110000000, save1Instruction);

        expandInstruction(extendedOpcode16Map, 0b0110010010000000, 0b1111111110000000, save2Instruction);

        expandInstruction(opcode16Map,         0b1100000000000000, 0b1111100000000000, sb Instruction);
        expandInstruction(extendedOpcode16Map, 0b1100000000000000, 0b1111100000000000, sb Instruction);

        expandInstruction(opcode16Map,         0b0011100010000000, 0b1111100010000000, sb Instruction);
        expandInstruction(extendedOpcode16Map, 0b0011100010000000, 0b1111100011100000, sb Instruction);

        expandInstruction(opcode16Map,         0b0111100010000000, 0b1111100010000000, sb Instruction);
        expandInstruction(extendedOpcode16Map, 0b0111100010000000, 0b1111100011100000, sb Instruction);

        expandInstruction(opcode16Map,         0b1110100000000001, 0b1111100000011111, sdbbpInstruction);

        expandInstruction(opcode16Map,         0b1110100010010001, 0b1111100011111111, sebInstruction);

        expandInstruction(opcode16Map,         0b1110100010110001, 0b1111100011111111, sehInstruction);

        expandInstruction(opcode16Map,         0b1100100000000000, 0b1111100000000000, sh Instruction);
        expandInstruction(extendedOpcode16Map, 0b1100100000000000, 0b1111100000000000, sh Instruction);

        expandInstruction(opcode16Map,         0b1011100010000001, 0b1111100010000001, sh Instruction);
        expandInstruction(extendedOpcode16Map, 0b1011100010000001, 0b1111100011100001, sh Instruction);

        expandInstruction(opcode16Map,         0b1011100010000000, 0b1111100010000001, sh Instruction);
        expandInstruction(extendedOpcode16Map, 0b1011100010000000, 0b1111100011100001, sh Instruction);

        expandInstruction(opcode16Map,         0b0011000000000000, 0b1111100000000011, sll Instruction);
        expandInstruction(extendedOpcode16Map, 0b0011000000000000, 0b1111100000011111, sll Instruction);

        expandInstruction(opcode16Map,         0b1110000010000000, 0b1111100010000011, sll Instruction);

        expandInstruction(opcode16Map,         0b1110100000000100, 0b1111100000011111, sllv Instruction);

        expandInstruction(opcode16Map,         0b1110100000000010, 0b1111100000011111, slt Instruction);

        expandInstruction(opcode16Map,         0b0101000000000000, 0b1111100000000000, slti Instruction);
        expandInstruction(extendedOpcode16Map, 0b0101000000000000, 0b1111100011100000, slti Instruction);

        expandInstruction(opcode16Map,         0b0101100000000000, 0b1111100000000000, sltiu Instruction);
        expandInstruction(extendedOpcode16Map, 0b0101100000000000, 0b1111100011100000, sltiu Instruction);

        expandInstruction(opcode16Map,         0b1110100000000011, 0b1111100000011111, sltu Instruction);

        expandInstruction(opcode16Map,         0b0011000000000011, 0b1111100000000011, sra1 Instruction);
        expandInstruction(extendedOpcode16Map, 0b0011000000000011, 0b1111100000011111, sra1 Instruction);

        expandInstruction(opcode16Map,         0b1110000000000010, 0b1111100010000011, sra2 Instruction);

        expandInstruction(opcode16Map,         0b1110100000000111, 0b1111100000011111, srav Instruction);

        expandInstruction(opcode16Map,         0b0011000000000010, 0b1111100000000011, srl1 Instruction);
        expandInstruction(extendedOpcode16Map, 0b0011000000000010, 0b1111100000011111, srl1 Instruction);

        expandInstruction(opcode16Map,         0b1110000010000010, 0b1111100010000011, srl2 Instruction);

        expandInstruction(opcode16Map,         0b1110100000000110, 0b1111100000011111, srlv Instruction);

        expandInstruction(opcode16Map,         0b1110100000010101, 0b1111100000011111, ssub Instruction);

        expandInstruction(opcode16Map,         0b1110000000000011, 0b1111100000000011, subu Instruction);

        expandInstruction(opcode16Map,         0b0110001000000000, 0b1111111100000000, sw1 Instruction);
        expandInstruction(extendedOpcode16Map, 0b0110001000000000, 0b1111111111100000, sw1 Instruction);

        expandInstruction(opcode16Map,         0b1111111100000000, 0b1111111100000000, sw2 Instruction);
        expandInstruction(extendedOpcode16Map, 0b1111111100000000, 0b1111111100000000, sw2 Instruction);

        expandInstruction(opcode16Map,         0b1101000000000000, 0b1111100000000000, sw3 Instruction);
        expandInstruction(extendedOpcode16Map, 0b1101000000000000, 0b1111100011100000, sw3 Instruction);

        expandInstruction(opcode16Map,         0b1101100000000000, 0b1111100000000000, sw4 Instruction);
        expandInstruction(extendedOpcode16Map, 0b1101100000000000, 0b1111100000000000, sw4 Instruction);

        expandInstruction(extendedOpcode16Map, 0b1110100000001111, 0b1111111111111111, sync Instruction);

        expandInstruction(extendedOpcode16Map, 0b1110100000001100, 0b1111100000011111, syscall Instruction);

        expandInstruction(extendedOpcode16Map, 0b1110100000000000, 0b1111111111111111, wait Instruction);

        expandInstruction(opcode16Map,         0b1110100000001110, 0b1111100000011111, xor Instruction);

        expandInstruction(extendedOpcode16Map, 0b0100100011000000, 0b1111100011100000, xori Instruction);

        expandInstruction(opcode16Map,         0b1110100000010001, 0b1111100011111111, zeb Instruction);

        expandInstruction(opcode16Map,         0b1110100000110001, 0b1111100011111111, zeh Instruction);
*/

        // ----------------- 32-bits -----------------

        // These are rewrites of the Toshiba architecture document, appendix E

        // Encoding of the Opcode Field
        opcodeResolvers = new InstructionResolver[64];

        opcodeResolvers[0b000000] = specialFunctionResolver;
        opcodeResolvers[0b000001] = regImmRtResolver;
        opcodeResolvers[0b000010] = new DirectInstructionResolver(jInstruction);
        opcodeResolvers[0b000011] = new DirectInstructionResolver(jalInstruction);
        opcodeResolvers[0b000100] = new DirectInstructionResolver(beqInstruction);
        if (true/*OptionAltInstructions*/) {
            opcodeResolvers[0b000100] = beqOrBeqzResolver;
        }
        opcodeResolvers[0b000101] = new DirectInstructionResolver(bneInstruction);
        if (true/*OptionAltInstructions*/) {
            opcodeResolvers[0b000101] = bneOrBnezResolver;
        }
        opcodeResolvers[0b000110] = new DirectInstructionResolver(blezInstruction);
        opcodeResolvers[0b000111] = new DirectInstructionResolver(bgtzInstruction);

        opcodeResolvers[0b001000] = new DirectInstructionResolver(addiInstruction);
        opcodeResolvers[0b001001] = new DirectInstructionResolver(addiuInstruction);
        if (true/*OptionAltInstructions*/) {
            opcodeResolvers[0b001001] = addiuOrLiResolver;
        }
        opcodeResolvers[0b001010] = new DirectInstructionResolver(sltiInstruction);
        opcodeResolvers[0b001011] = new DirectInstructionResolver(sltiuInstruction);
        opcodeResolvers[0b001100] = new DirectInstructionResolver(andiInstruction);
        opcodeResolvers[0b001101] = new DirectInstructionResolver(oriInstruction);
        if (true/*OptionAltInstructions*/) {
            opcodeResolvers[0b001101] = oriOrLiResolver;
        }
        opcodeResolvers[0b001110] = new DirectInstructionResolver(xoriInstruction);
        opcodeResolvers[0b001111] = new DirectInstructionResolver(luiInstruction);

        opcodeResolvers[0b010000] = cop0RsResolver;
        opcodeResolvers[0b010001] = cop1RsResolver;
        opcodeResolvers[0b010010] = /*COP2*/ thetaResolver;
        opcodeResolvers[0b010011] = /*COP3*/ thetaResolver;
        opcodeResolvers[0b010100] = new DirectInstructionResolver(beqlInstruction);
        if (true/*OptionAltInstructions*/) {
            opcodeResolvers[0b010100] = beqlOrBeqzlResolver;
        }
        opcodeResolvers[0b010101] = new DirectInstructionResolver(bnelInstruction);
        if (true/*OptionAltInstructions*/) {
            opcodeResolvers[0b010101] = bnelOrBnezlResolver;
        }
        opcodeResolvers[0b010110] = new DirectInstructionResolver(blezlInstruction);
        opcodeResolvers[0b010111] = new DirectInstructionResolver(bgtzlInstruction);

        opcodeResolvers[0b011000] = betaResolver;
        opcodeResolvers[0b011001] = betaResolver;
        opcodeResolvers[0b011010] = betaResolver;
        opcodeResolvers[0b011011] = betaResolver;
        opcodeResolvers[0b011100] = special2FunctionResolver;
        opcodeResolvers[0b011101] = unimplementedResolver; // new DirectInstructionResolver(jalxInstruction);
        opcodeResolvers[0b011110] = betaResolver;
        opcodeResolvers[0b011111] = starResolver;

        opcodeResolvers[0b100000] = new DirectInstructionResolver(lbInstruction);
        opcodeResolvers[0b100001] = new DirectInstructionResolver(lhInstruction);
        opcodeResolvers[0b100010] = new DirectInstructionResolver(lwlInstruction);
        opcodeResolvers[0b100011] = new DirectInstructionResolver(lwInstruction);
        opcodeResolvers[0b100100] = new DirectInstructionResolver(lbuInstruction);
        opcodeResolvers[0b100101] = new DirectInstructionResolver(lhuInstruction);
        opcodeResolvers[0b100110] = new DirectInstructionResolver(lwrInstruction);
        opcodeResolvers[0b100111] = betaResolver;

        opcodeResolvers[0b101000] = new DirectInstructionResolver(sbInstruction);
        opcodeResolvers[0b101001] = new DirectInstructionResolver(shInstruction);
        opcodeResolvers[0b101010] = new DirectInstructionResolver(swlInstruction);
        opcodeResolvers[0b101011] = new DirectInstructionResolver(swInstruction);
        opcodeResolvers[0b101100] = betaResolver;
        opcodeResolvers[0b101101] = betaResolver;
        opcodeResolvers[0b101110] = new DirectInstructionResolver(swrInstruction);
        opcodeResolvers[0b101111] = /*CACHE*/ betaResolver;

        opcodeResolvers[0b110000] = /*LL*/ betaResolver;
        opcodeResolvers[0b110001] = new DirectInstructionResolver(lwc1Instruction);
        opcodeResolvers[0b110010] = /*LWC2*/ betaResolver;
        opcodeResolvers[0b110011] = /*PREF*/ betaResolver;
        opcodeResolvers[0b110100] = betaResolver;
        opcodeResolvers[0b110101] = /*LDC1*/ betaResolver;
        opcodeResolvers[0b110110] = /*LDC2*/ betaResolver;
        opcodeResolvers[0b110111] = betaResolver;

        opcodeResolvers[0b111000] = /*SC*/ betaResolver;
        opcodeResolvers[0b111001] = new DirectInstructionResolver(swc1Instruction);
        opcodeResolvers[0b111010] = /*SWC2*/ betaResolver;
        opcodeResolvers[0b111011] = starResolver;
        opcodeResolvers[0b111100] = betaResolver;
        opcodeResolvers[0b111101] = /*SDC1*/ betaResolver;
        opcodeResolvers[0b111110] = /*SDC2*/ betaResolver;
        opcodeResolvers[0b111111] = betaResolver;

        // SPECIAL Opcode Encoding of Function Field
        specialFunctionResolvers = new InstructionResolver[64];

        specialFunctionResolvers[0b000000] = new DirectInstructionResolver(sllInstruction);
        if (true/*OptionAltInstructions*/) {
            specialFunctionResolvers[0b000000] = sllOrNopResolver;
        }
        specialFunctionResolvers[0b000001] = betaResolver;
        specialFunctionResolvers[0b000010] = new DirectInstructionResolver(srlInstruction);
        specialFunctionResolvers[0b000011] = new DirectInstructionResolver(sraInstruction);
        specialFunctionResolvers[0b000100] = new DirectInstructionResolver(sllvInstruction);
        specialFunctionResolvers[0b000101] = starResolver;
        specialFunctionResolvers[0b000110] = new DirectInstructionResolver(srlvInstruction);
        specialFunctionResolvers[0b000111] = new DirectInstructionResolver(sravInstruction);

        specialFunctionResolvers[0b001000] = new DirectInstructionResolver(jrInstruction);
        if (true/*OptionAltInstructions*/) {
            specialFunctionResolvers[0b001000] = jrOrRetResolver;
        }
        specialFunctionResolvers[0b001001] = new DirectInstructionResolver(jalrInstruction);
        specialFunctionResolvers[0b001010] = new DirectInstructionResolver(movzInstruction);
        specialFunctionResolvers[0b001011] = new DirectInstructionResolver(movnInstruction);
        specialFunctionResolvers[0b001100] = unimplementedResolver; // new DirectInstructionResolver(syscallInstruction);
        specialFunctionResolvers[0b001101] = new DirectInstructionResolver(breakInstruction);
        specialFunctionResolvers[0b001110] = starResolver;
        specialFunctionResolvers[0b001111] = new DirectInstructionResolver(syncInstruction);

        specialFunctionResolvers[0b010000] = new DirectInstructionResolver(mfhiInstruction);
        specialFunctionResolvers[0b010001] = new DirectInstructionResolver(mthiInstruction);
        specialFunctionResolvers[0b010010] = new DirectInstructionResolver(mfloInstruction);
        specialFunctionResolvers[0b010011] = new DirectInstructionResolver(mtloInstruction);
        specialFunctionResolvers[0b010100] = betaResolver;
        specialFunctionResolvers[0b010101] = starResolver;
        specialFunctionResolvers[0b010110] = betaResolver;
        specialFunctionResolvers[0b010111] = betaResolver;

        specialFunctionResolvers[0b011000] = new DirectInstructionResolver(multInstruction);
        specialFunctionResolvers[0b011001] = new DirectInstructionResolver(multuInstruction);
        specialFunctionResolvers[0b011010] = new DirectInstructionResolver(divInstruction);
        specialFunctionResolvers[0b011011] = new DirectInstructionResolver(divuInstruction);
        specialFunctionResolvers[0b011100] = betaResolver;
        specialFunctionResolvers[0b011101] = betaResolver;
        specialFunctionResolvers[0b011110] = betaResolver;
        specialFunctionResolvers[0b011111] = betaResolver;

        specialFunctionResolvers[0b100000] = new DirectInstructionResolver(addInstruction);
        specialFunctionResolvers[0b100001] = new DirectInstructionResolver(adduInstruction);
        if (true/*OptionAltInstructions*/) {
            specialFunctionResolvers[0b100001] = adduOrMoveResolver;
        }
        specialFunctionResolvers[0b100010] = new DirectInstructionResolver(subInstruction);
        specialFunctionResolvers[0b100011] = new DirectInstructionResolver(subuInstruction);
        specialFunctionResolvers[0b100100] = new DirectInstructionResolver(andInstruction);
        specialFunctionResolvers[0b100101] = new DirectInstructionResolver(orInstruction);
        if (true/*OptionAltInstructions*/) {
            specialFunctionResolvers[0b100101] = orOrMoveResolver;
        }
        specialFunctionResolvers[0b100110] = new DirectInstructionResolver(xorInstruction);
        specialFunctionResolvers[0b100111] = new DirectInstructionResolver(norInstruction);

        specialFunctionResolvers[0b101000] = starResolver;
        specialFunctionResolvers[0b101001] = starResolver;
        specialFunctionResolvers[0b101010] = new DirectInstructionResolver(sltInstruction);
        specialFunctionResolvers[0b101011] = new DirectInstructionResolver(sltuInstruction);
        specialFunctionResolvers[0b101100] = betaResolver;
        specialFunctionResolvers[0b101101] = betaResolver;
        specialFunctionResolvers[0b101110] = betaResolver;
        specialFunctionResolvers[0b101111] = betaResolver;

        specialFunctionResolvers[0b110000] = new DirectInstructionResolver(tgeInstruction);
        specialFunctionResolvers[0b110001] = new DirectInstructionResolver(tgeuInstruction);
        specialFunctionResolvers[0b110010] = new DirectInstructionResolver(tltInstruction);
        specialFunctionResolvers[0b110011] = new DirectInstructionResolver(tltuInstruction);
        specialFunctionResolvers[0b110100] = new DirectInstructionResolver(teqInstruction);
        specialFunctionResolvers[0b110101] = starResolver;
        specialFunctionResolvers[0b110110] = new DirectInstructionResolver(tneInstruction);
        specialFunctionResolvers[0b110111] = starResolver;

        specialFunctionResolvers[0b111000] = betaResolver;
        specialFunctionResolvers[0b111001] = starResolver;
        specialFunctionResolvers[0b111010] = betaResolver;
        specialFunctionResolvers[0b111011] = betaResolver;
        specialFunctionResolvers[0b111100] = betaResolver;
        specialFunctionResolvers[0b111101] = starResolver;
        specialFunctionResolvers[0b111110] = betaResolver;
        specialFunctionResolvers[0b111111] = betaResolver;

        // REGIMM Encoding of rt Field
        regImmRtResolvers = new InstructionResolver[32];

        regImmRtResolvers[0b00000] = new DirectInstructionResolver(bltzInstruction);
        regImmRtResolvers[0b00001] = new DirectInstructionResolver(bgezInstruction);
        regImmRtResolvers[0b00010] = new DirectInstructionResolver(bltzlInstruction);
        regImmRtResolvers[0b00011] = new DirectInstructionResolver(bgezlInstruction);
        regImmRtResolvers[0b00100] = starResolver;
        regImmRtResolvers[0b00101] = starResolver;
        regImmRtResolvers[0b00110] = starResolver;
        regImmRtResolvers[0b00111] = starResolver;

        regImmRtResolvers[0b01000] = new DirectInstructionResolver(tgeiInstruction);
        regImmRtResolvers[0b01001] = new DirectInstructionResolver(tgeiuInstruction);
        regImmRtResolvers[0b01010] = new DirectInstructionResolver(tltiInstruction);
        regImmRtResolvers[0b01011] = new DirectInstructionResolver(tltiuInstruction);
        regImmRtResolvers[0b01100] = new DirectInstructionResolver(teqiInstruction);
        regImmRtResolvers[0b01101] = starResolver;
        regImmRtResolvers[0b01110] = new DirectInstructionResolver(tneiInstruction);
        regImmRtResolvers[0b01111] = starResolver;

        regImmRtResolvers[0b10000] = new DirectInstructionResolver(bltzalInstruction);
        regImmRtResolvers[0b10001] = new DirectInstructionResolver(bgezalInstruction);
        regImmRtResolvers[0b10010] = new DirectInstructionResolver(bltzallInstruction);
        regImmRtResolvers[0b10011] = new DirectInstructionResolver(bgezallInstruction);
        regImmRtResolvers[0b10100] = starResolver;
        regImmRtResolvers[0b10101] = starResolver;
        regImmRtResolvers[0b10110] = starResolver;
        regImmRtResolvers[0b10111] = starResolver;

        regImmRtResolvers[0b11000] = starResolver;
        regImmRtResolvers[0b11001] = starResolver;
        regImmRtResolvers[0b11010] = starResolver;
        regImmRtResolvers[0b11011] = starResolver;
        regImmRtResolvers[0b11100] = starResolver;
        regImmRtResolvers[0b11101] = starResolver;
        regImmRtResolvers[0b11110] = starResolver;
        regImmRtResolvers[0b11111] = starResolver;

        // SPECIAL2 Encoding of Function Field
        special2FunctionResolvers = new InstructionResolver[64];

        special2FunctionResolvers[0b000000] = new DirectInstructionResolver(maddInstruction);
        special2FunctionResolvers[0b000001] = new DirectInstructionResolver(madduInstruction);
        special2FunctionResolvers[0b000010] = new DirectInstructionResolver(mulInstruction);
        special2FunctionResolvers[0b000011] = thetaResolver;
        special2FunctionResolvers[0b000100] = new DirectInstructionResolver(msubInstruction);
        special2FunctionResolvers[0b000101] = new DirectInstructionResolver(msubuInstruction);
        special2FunctionResolvers[0b000110] = thetaResolver;
        special2FunctionResolvers[0b000111] = thetaResolver;

        special2FunctionResolvers[0b001000] = thetaResolver;
        special2FunctionResolvers[0b001001] = thetaResolver;
        special2FunctionResolvers[0b001010] = thetaResolver;
        special2FunctionResolvers[0b001011] = thetaResolver;
        special2FunctionResolvers[0b001100] = thetaResolver;
        special2FunctionResolvers[0b001101] = thetaResolver;
        special2FunctionResolvers[0b001110] = thetaResolver;
        special2FunctionResolvers[0b001111] = thetaResolver;

        special2FunctionResolvers[0b010000] = thetaResolver;
        special2FunctionResolvers[0b010001] = thetaResolver;
        special2FunctionResolvers[0b010010] = thetaResolver;
        special2FunctionResolvers[0b010011] = thetaResolver;
        special2FunctionResolvers[0b010100] = thetaResolver;
        special2FunctionResolvers[0b010101] = thetaResolver;
        special2FunctionResolvers[0b010110] = thetaResolver;
        special2FunctionResolvers[0b010111] = thetaResolver;

        special2FunctionResolvers[0b011000] = thetaResolver;
        special2FunctionResolvers[0b011001] = thetaResolver;
        special2FunctionResolvers[0b011010] = thetaResolver;
        special2FunctionResolvers[0b011011] = thetaResolver;
        special2FunctionResolvers[0b011100] = thetaResolver;
        special2FunctionResolvers[0b011101] = thetaResolver;
        special2FunctionResolvers[0b011110] = thetaResolver;
        special2FunctionResolvers[0b011111] = thetaResolver;

        special2FunctionResolvers[0b100000] = new DirectInstructionResolver(clzInstruction);
        special2FunctionResolvers[0b100001] = new DirectInstructionResolver(cloInstruction);
        special2FunctionResolvers[0b100010] = thetaResolver;
        special2FunctionResolvers[0b100011] = thetaResolver;
        special2FunctionResolvers[0b100100] = betaResolver;
        special2FunctionResolvers[0b100101] = betaResolver;
        special2FunctionResolvers[0b100110] = thetaResolver;
        special2FunctionResolvers[0b100111] = thetaResolver;

        special2FunctionResolvers[0b101000] = thetaResolver;
        special2FunctionResolvers[0b101001] = thetaResolver;
        special2FunctionResolvers[0b101010] = thetaResolver;
        special2FunctionResolvers[0b101011] = thetaResolver;
        special2FunctionResolvers[0b101100] = thetaResolver;
        special2FunctionResolvers[0b101101] = thetaResolver;
        special2FunctionResolvers[0b101110] = thetaResolver;
        special2FunctionResolvers[0b101111] = thetaResolver;

        special2FunctionResolvers[0b110000] = thetaResolver;
        special2FunctionResolvers[0b110001] = thetaResolver;
        special2FunctionResolvers[0b110010] = thetaResolver;
        special2FunctionResolvers[0b110011] = thetaResolver;
        special2FunctionResolvers[0b110100] = thetaResolver;
        special2FunctionResolvers[0b110101] = thetaResolver;
        special2FunctionResolvers[0b110110] = thetaResolver;
        special2FunctionResolvers[0b110111] = thetaResolver;

        special2FunctionResolvers[0b111000] = thetaResolver;
        special2FunctionResolvers[0b111001] = thetaResolver;
        special2FunctionResolvers[0b111010] = thetaResolver;
        special2FunctionResolvers[0b111011] = thetaResolver;
        special2FunctionResolvers[0b111100] = thetaResolver;
        special2FunctionResolvers[0b111101] = thetaResolver;
        special2FunctionResolvers[0b111110] = thetaResolver;
        special2FunctionResolvers[0b111111] = unimplementedResolver; // new DirectInstructionResolver(sdbbpInstruction); /* if EJTAG */

        // COP0 Encoding of rs Field
        cop0RsResolvers = new InstructionResolver[32];

        cop0RsResolvers[0b00000] = new DirectInstructionResolver(mfc0Instruction);
        cop0RsResolvers[0b00001] = betaResolver;
        cop0RsResolvers[0b00010] = starResolver;
        cop0RsResolvers[0b00011] = starResolver;
        cop0RsResolvers[0b00100] = new DirectInstructionResolver(mtc0Instruction);
        cop0RsResolvers[0b00101] = betaResolver;
        cop0RsResolvers[0b00110] = starResolver;
        cop0RsResolvers[0b00111] = starResolver;

        cop0RsResolvers[0b01000] = starResolver;
        cop0RsResolvers[0b01001] = starResolver;
        cop0RsResolvers[0b01010] = starResolver;
        cop0RsResolvers[0b01011] = starResolver;
        cop0RsResolvers[0b01100] = starResolver;
        cop0RsResolvers[0b01101] = starResolver;
        cop0RsResolvers[0b01110] = starResolver;
        cop0RsResolvers[0b01111] = starResolver;

        cop0RsResolvers[0b10000] = cop0CoFunctionResolver;
        cop0RsResolvers[0b10001] = cop0CoFunctionResolver;
        cop0RsResolvers[0b10010] = cop0CoFunctionResolver;
        cop0RsResolvers[0b10011] = cop0CoFunctionResolver;
        cop0RsResolvers[0b10100] = cop0CoFunctionResolver;
        cop0RsResolvers[0b10101] = cop0CoFunctionResolver;
        cop0RsResolvers[0b10110] = cop0CoFunctionResolver;
        cop0RsResolvers[0b10111] = cop0CoFunctionResolver;

        cop0RsResolvers[0b11000] = cop0CoFunctionResolver;
        cop0RsResolvers[0b11001] = cop0CoFunctionResolver;
        cop0RsResolvers[0b11010] = cop0CoFunctionResolver;
        cop0RsResolvers[0b11011] = cop0CoFunctionResolver;
        cop0RsResolvers[0b11100] = cop0CoFunctionResolver;
        cop0RsResolvers[0b11101] = cop0CoFunctionResolver;
        cop0RsResolvers[0b11110] = cop0CoFunctionResolver;
        cop0RsResolvers[0b11111] = cop0CoFunctionResolver;

        // MIPS32 COP0 Encoding of Function Field When rs = CO
        cop0CoFunctionResolvers = new InstructionResolver[64];

        cop0CoFunctionResolvers[0b000000] = starResolver;
        cop0CoFunctionResolvers[0b000001] = /* tlbp */ starResolver;
        cop0CoFunctionResolvers[0b000010] = /* tlbwi */ starResolver;
        cop0CoFunctionResolvers[0b000011] = starResolver;
        cop0CoFunctionResolvers[0b000100] = starResolver;
        cop0CoFunctionResolvers[0b000101] = starResolver;
        cop0CoFunctionResolvers[0b000110] = /* tlbwr */ starResolver;
        cop0CoFunctionResolvers[0b000111] = starResolver;

        cop0CoFunctionResolvers[0b001000] = /* tlbp */ starResolver;
        cop0CoFunctionResolvers[0b001001] = starResolver;
        cop0CoFunctionResolvers[0b001010] = starResolver;
        cop0CoFunctionResolvers[0b001011] = starResolver;
        cop0CoFunctionResolvers[0b001100] = starResolver;
        cop0CoFunctionResolvers[0b001101] = starResolver;
        cop0CoFunctionResolvers[0b001110] = starResolver;
        cop0CoFunctionResolvers[0b001111] = starResolver;

        cop0CoFunctionResolvers[0b010000] = starResolver;
        cop0CoFunctionResolvers[0b010001] = starResolver;
        cop0CoFunctionResolvers[0b010010] = starResolver;
        cop0CoFunctionResolvers[0b010011] = starResolver;
        cop0CoFunctionResolvers[0b010100] = starResolver;
        cop0CoFunctionResolvers[0b010101] = starResolver;
        cop0CoFunctionResolvers[0b010110] = starResolver;
        cop0CoFunctionResolvers[0b010111] = starResolver;

        cop0CoFunctionResolvers[0b011000] = new DirectInstructionResolver(eretInstruction);
        cop0CoFunctionResolvers[0b011001] = starResolver;
        cop0CoFunctionResolvers[0b011010] = starResolver;
        cop0CoFunctionResolvers[0b011011] = starResolver;
        cop0CoFunctionResolvers[0b011100] = starResolver;
        cop0CoFunctionResolvers[0b011101] = starResolver;
        cop0CoFunctionResolvers[0b011110] = starResolver;
        cop0CoFunctionResolvers[0b011111] = unimplementedResolver; // new DirectInstructionResolver(deretInstruction);/* if EJTAG */

        cop0CoFunctionResolvers[0b100000] = new DirectInstructionResolver(waitInstruction);
        cop0CoFunctionResolvers[0b100001] = starResolver;
        cop0CoFunctionResolvers[0b100010] = starResolver;
        cop0CoFunctionResolvers[0b100011] = starResolver;
        cop0CoFunctionResolvers[0b100100] = starResolver;
        cop0CoFunctionResolvers[0b100101] = starResolver;
        cop0CoFunctionResolvers[0b100110] = starResolver;
        cop0CoFunctionResolvers[0b100111] = starResolver;

        cop0CoFunctionResolvers[0b101000] = starResolver;
        cop0CoFunctionResolvers[0b101001] = starResolver;
        cop0CoFunctionResolvers[0b101010] = starResolver;
        cop0CoFunctionResolvers[0b101011] = starResolver;
        cop0CoFunctionResolvers[0b101100] = starResolver;
        cop0CoFunctionResolvers[0b101101] = starResolver;
        cop0CoFunctionResolvers[0b101110] = starResolver;
        cop0CoFunctionResolvers[0b101111] = starResolver;

        cop0CoFunctionResolvers[0b110000] = starResolver;
        cop0CoFunctionResolvers[0b110001] = starResolver;
        cop0CoFunctionResolvers[0b110010] = starResolver;
        cop0CoFunctionResolvers[0b110011] = starResolver;
        cop0CoFunctionResolvers[0b110100] = starResolver;
        cop0CoFunctionResolvers[0b110101] = starResolver;
        cop0CoFunctionResolvers[0b110110] = starResolver;
        cop0CoFunctionResolvers[0b110111] = starResolver;

        cop0CoFunctionResolvers[0b111000] = starResolver;
        cop0CoFunctionResolvers[0b111001] = starResolver;
        cop0CoFunctionResolvers[0b111010] = starResolver;
        cop0CoFunctionResolvers[0b111011] = starResolver;
        cop0CoFunctionResolvers[0b111111] = starResolver;
        cop0CoFunctionResolvers[0b111100] = starResolver;
        cop0CoFunctionResolvers[0b111101] = starResolver;
        cop0CoFunctionResolvers[0b111110] = starResolver;

        // COP1 Encoding of rs Field
        cop1RsResolvers = new InstructionResolver[32];

        cop1RsResolvers[0b00000] = new DirectInstructionResolver(mfc1Instruction);
        cop1RsResolvers[0b00001] = betaResolver;
        cop1RsResolvers[0b00010] = new DirectInstructionResolver(cfc1Instruction);
        cop1RsResolvers[0b00011] = /*MIPS32 MFHC1*/starResolver;
        cop1RsResolvers[0b00100] = new DirectInstructionResolver(mtc1Instruction);
        cop1RsResolvers[0b00101] = betaResolver;
        cop1RsResolvers[0b00110] = new DirectInstructionResolver(ctc1Instruction);
        cop1RsResolvers[0b00111] = /*MIPS32 MTHC1*/starResolver;

        cop1RsResolvers[0b01000] = bc1TFResolver;
        cop1RsResolvers[0b01001] = /*BC1ANY2*/starResolver;
        cop1RsResolvers[0b01010] = /*BC1ANY4*/starResolver;
        cop1RsResolvers[0b01011] = starResolver;
        cop1RsResolvers[0b01100] = starResolver;
        cop1RsResolvers[0b01101] = starResolver;
        cop1RsResolvers[0b01110] = starResolver;
        cop1RsResolvers[0b01111] = starResolver;

        cop1RsResolvers[0b10000] = cop1SFunctionResolver;
        cop1RsResolvers[0b10001] = unimplementedResolver;
        cop1RsResolvers[0b10010] = starResolver;
        cop1RsResolvers[0b10011] = starResolver;
        cop1RsResolvers[0b10100] = cop1WLFunctionResolver;
        cop1RsResolvers[0b10101] = cop1WLFunctionResolver;
        cop1RsResolvers[0b10110] = unimplementedResolver;
        cop1RsResolvers[0b10111] = starResolver;

        cop1RsResolvers[0b11000] = starResolver;
        cop1RsResolvers[0b11001] = starResolver;
        cop1RsResolvers[0b11010] = starResolver;
        cop1RsResolvers[0b11011] = starResolver;
        cop1RsResolvers[0b11100] = starResolver;
        cop1RsResolvers[0b11101] = starResolver;
        cop1RsResolvers[0b11110] = starResolver;
        cop1RsResolvers[0b11111] = starResolver;

        // MIPS32 COP1 Encoding of Function Field When rs = S
        cop1SFunctionResolvers = new InstructionResolver[64];

        cop1SFunctionResolvers[0b000000] = new DirectInstructionResolver(addSInstruction);
        cop1SFunctionResolvers[0b000001] = new DirectInstructionResolver(subSInstruction);
        cop1SFunctionResolvers[0b000010] = new DirectInstructionResolver(mulSInstruction);
        cop1SFunctionResolvers[0b000011] = new DirectInstructionResolver(divSInstruction);
        cop1SFunctionResolvers[0b000100] = unimplementedResolver;//new DirectInstructionResolver(SqrtSInstruction);
        cop1SFunctionResolvers[0b000101] = unimplementedResolver;//new DirectInstructionResolver(AbsSInstruction);
        cop1SFunctionResolvers[0b000110] = unimplementedResolver;//new DirectInstructionResolver(MovSInstruction);
        cop1SFunctionResolvers[0b000111] = unimplementedResolver;//new DirectInstructionResolver(NegSInstruction);

        cop1SFunctionResolvers[0b001000] = unimplementedResolver;//new DirectInstructionResolver(roundS_lInstruction)
        cop1SFunctionResolvers[0b001001] = unimplementedResolver;//new DirectInstructionResolver(truncS_lInstruction)
        cop1SFunctionResolvers[0b001010] = unimplementedResolver;//new DirectInstructionResolver(ceilS_lInstruction)
        cop1SFunctionResolvers[0b001011] = unimplementedResolver;//new DirectInstructionResolver(floorS_lInstruction)
        cop1SFunctionResolvers[0b001100] = unimplementedResolver;//new DirectInstructionResolver(roundS_wInstruction)
        cop1SFunctionResolvers[0b001101] = unimplementedResolver;//new DirectInstructionResolver(truncS_wInstruction)
        cop1SFunctionResolvers[0b001110] = unimplementedResolver;//new DirectInstructionResolver(ceilS_wInstruction)
        cop1SFunctionResolvers[0b001111] = unimplementedResolver;//new DirectInstructionResolver(floorS_wInstruction)

        cop1SFunctionResolvers[0b010000] = starResolver;
        cop1SFunctionResolvers[0b010001] = unimplementedResolver;//new DirectInstructionResolver(movcfSInstruction)
        cop1SFunctionResolvers[0b010010] = unimplementedResolver;//new DirectInstructionResolver(movzSInstruction)
        cop1SFunctionResolvers[0b010011] = unimplementedResolver;//new DirectInstructionResolver(movnSInstruction)
        cop1SFunctionResolvers[0b010100] = starResolver;
        cop1SFunctionResolvers[0b010101] = unimplementedResolver;//new DirectInstructionResolver(recipSInstruction)
        cop1SFunctionResolvers[0b010110] = unimplementedResolver;//new DirectInstructionResolver(rsqrtSInstruction)
        cop1SFunctionResolvers[0b010111] = starResolver;

        cop1SFunctionResolvers[0b011000] = starResolver;
        cop1SFunctionResolvers[0b011001] = starResolver;
        cop1SFunctionResolvers[0b011010] = starResolver;
        cop1SFunctionResolvers[0b011011] = starResolver;
        cop1SFunctionResolvers[0b011100] = unimplementedResolver;//new DirectInstructionResolver(recip2SInstruction)
        cop1SFunctionResolvers[0b011101] = unimplementedResolver;//new DirectInstructionResolver(recip1SInstruction)
        cop1SFunctionResolvers[0b011110] = unimplementedResolver;//new DirectInstructionResolver(rsqrt1SInstruction)
        cop1SFunctionResolvers[0b011111] = unimplementedResolver;//new DirectInstructionResolver(rsqrt2SInstruction)

        cop1SFunctionResolvers[0b100000] = starResolver;
        cop1SFunctionResolvers[0b100001] = unimplementedResolver;//new DirectInstructionResolver(cvtSDInstruction);
        cop1SFunctionResolvers[0b100010] = starResolver;
        cop1SFunctionResolvers[0b100011] = starResolver;
        cop1SFunctionResolvers[0b100100] = new DirectInstructionResolver(cvtWSInstruction);
        cop1SFunctionResolvers[0b100101] = unimplementedResolver;//new DirectInstructionResolver(cvtSLInstruction);
        cop1SFunctionResolvers[0b100110] = unimplementedResolver;//new DirectInstructionResolver(cvtSPsInstruction);
        cop1SFunctionResolvers[0b100111] = starResolver;

        cop1SFunctionResolvers[0b101000] = starResolver;
        cop1SFunctionResolvers[0b101001] = starResolver;
        cop1SFunctionResolvers[0b101010] = starResolver;
        cop1SFunctionResolvers[0b101011] = starResolver;
        cop1SFunctionResolvers[0b101100] = starResolver;
        cop1SFunctionResolvers[0b101101] = starResolver;
        cop1SFunctionResolvers[0b101110] = starResolver;
        cop1SFunctionResolvers[0b101111] = starResolver;

        cop1SFunctionResolvers[0b110000] = unimplementedResolver;//new DirectInstructionResolver(cFSInstruction);
        cop1SFunctionResolvers[0b110001] = unimplementedResolver;//new DirectInstructionResolver(cUnSInstruction);
        cop1SFunctionResolvers[0b110010] = new DirectInstructionResolver(cEqSInstruction);
        cop1SFunctionResolvers[0b110011] = unimplementedResolver;//new DirectInstructionResolver(cUeqSInstruction);
        cop1SFunctionResolvers[0b110100] = unimplementedResolver;//new DirectInstructionResolver(cSOltInstruction);
        cop1SFunctionResolvers[0b110101] = unimplementedResolver;//new DirectInstructionResolver(cSUltInstruction);
        cop1SFunctionResolvers[0b110110] = unimplementedResolver;//new DirectInstructionResolver(cSOleInstruction);
        cop1SFunctionResolvers[0b110111] = unimplementedResolver;//new DirectInstructionResolver(cSUleInstruction);

        cop1SFunctionResolvers[0b111000] = unimplementedResolver;//new DirectInstructionResolver(cSfSInstruction);
        cop1SFunctionResolvers[0b111001] = unimplementedResolver;//new DirectInstructionResolver(cNgleSInstruction);
        cop1SFunctionResolvers[0b111010] = unimplementedResolver;//new DirectInstructionResolver(cSeqSInstruction);
        cop1SFunctionResolvers[0b111011] = unimplementedResolver;//new DirectInstructionResolver(cNglSInstruction);
        cop1SFunctionResolvers[0b111100] = new DirectInstructionResolver(cLtSInstruction);
        cop1SFunctionResolvers[0b111101] = unimplementedResolver;//new DirectInstructionResolver(cNgeSInstruction);
        cop1SFunctionResolvers[0b111110] = new DirectInstructionResolver(cLeSInstruction);
        cop1SFunctionResolvers[0b111111] = unimplementedResolver;//new DirectInstructionResolver(cNgtSInstruction);


        // MIPS32 COP1 Encoding of Function Field When rs = W or L
        cop1WLFunctionResolvers = new InstructionResolver[64];

        cop1WLFunctionResolvers[0b000000] = starResolver;
        cop1WLFunctionResolvers[0b000001] = starResolver;
        cop1WLFunctionResolvers[0b000010] = starResolver;
        cop1WLFunctionResolvers[0b000011] = starResolver;
        cop1WLFunctionResolvers[0b000100] = starResolver;
        cop1WLFunctionResolvers[0b000101] = starResolver;
        cop1WLFunctionResolvers[0b000110] = starResolver;
        cop1WLFunctionResolvers[0b000111] = starResolver;

        cop1WLFunctionResolvers[0b001000] = starResolver;
        cop1WLFunctionResolvers[0b001001] = starResolver;
        cop1WLFunctionResolvers[0b001010] = starResolver;
        cop1WLFunctionResolvers[0b001011] = starResolver;
        cop1WLFunctionResolvers[0b001100] = starResolver;
        cop1WLFunctionResolvers[0b001101] = starResolver;
        cop1WLFunctionResolvers[0b001110] = starResolver;
        cop1WLFunctionResolvers[0b001111] = starResolver;

        cop1WLFunctionResolvers[0b010000] = starResolver;
        cop1WLFunctionResolvers[0b010001] = starResolver;
        cop1WLFunctionResolvers[0b010010] = starResolver;
        cop1WLFunctionResolvers[0b010011] = starResolver;
        cop1WLFunctionResolvers[0b010100] = starResolver;
        cop1WLFunctionResolvers[0b010101] = starResolver;
        cop1WLFunctionResolvers[0b010110] = starResolver;
        cop1WLFunctionResolvers[0b010111] = starResolver;

        cop1WLFunctionResolvers[0b011000] = starResolver;
        cop1WLFunctionResolvers[0b011001] = starResolver;
        cop1WLFunctionResolvers[0b011010] = starResolver;
        cop1WLFunctionResolvers[0b011011] = starResolver;
        cop1WLFunctionResolvers[0b011100] = starResolver;
        cop1WLFunctionResolvers[0b011101] = starResolver;
        cop1WLFunctionResolvers[0b011110] = starResolver;
        cop1WLFunctionResolvers[0b011111] = starResolver;

        cop1WLFunctionResolvers[0b100000] = new DirectInstructionResolver(cvtSWInstruction);
        cop1WLFunctionResolvers[0b100001] = unimplementedResolver;//new DirectInstructionResolver(cop1CvtDInstruction);
        cop1WLFunctionResolvers[0b100010] = starResolver;
        cop1WLFunctionResolvers[0b100011] = starResolver;
        cop1WLFunctionResolvers[0b100100] = starResolver;
        cop1WLFunctionResolvers[0b100101] = starResolver;
        cop1WLFunctionResolvers[0b100110] = unimplementedResolver;//new DirectInstructionResolver(cop1CvtPsPwInstruction);
        cop1WLFunctionResolvers[0b100111] = starResolver;

        cop1WLFunctionResolvers[0b101000] = starResolver;
        cop1WLFunctionResolvers[0b101001] = starResolver;
        cop1WLFunctionResolvers[0b101010] = starResolver;
        cop1WLFunctionResolvers[0b101011] = starResolver;
        cop1WLFunctionResolvers[0b101100] = starResolver;
        cop1WLFunctionResolvers[0b101101] = starResolver;
        cop1WLFunctionResolvers[0b101110] = starResolver;
        cop1WLFunctionResolvers[0b101111] = starResolver;

        cop1WLFunctionResolvers[0b110000] = starResolver;
        cop1WLFunctionResolvers[0b110001] = starResolver;
        cop1WLFunctionResolvers[0b110010] = starResolver;
        cop1WLFunctionResolvers[0b110011] = starResolver;
        cop1WLFunctionResolvers[0b110100] = starResolver;
        cop1WLFunctionResolvers[0b110101] = starResolver;
        cop1WLFunctionResolvers[0b110110] = starResolver;
        cop1WLFunctionResolvers[0b110111] = starResolver;

        cop1WLFunctionResolvers[0b111000] = starResolver;
        cop1WLFunctionResolvers[0b111001] = starResolver;
        cop1WLFunctionResolvers[0b111010] = starResolver;
        cop1WLFunctionResolvers[0b111011] = starResolver;
        cop1WLFunctionResolvers[0b111111] = starResolver;
        cop1WLFunctionResolvers[0b111100] = starResolver;
        cop1WLFunctionResolvers[0b111101] = starResolver;
        cop1WLFunctionResolvers[0b111110] = starResolver;

        bc1fResolver = new DirectInstructionResolver(bc1fInstruction);
        bc1tResolver = new DirectInstructionResolver(bc1tInstruction);
    }

    private static void expandInstruction(TxInstruction[] map, int encoding, int mask, TxInstruction instruction) {
        fillMap(map, encoding, mask, instruction, 0, 0);
    }

    private static void fillMap(TxInstruction[] map, int encoding, int mask, TxInstruction instruction, int value, int numBitsFilled) {
        if (numBitsFilled == 16) {
            map[value] = instruction;
        }
        else {
            int minValue, maxValue;
            if (((mask >> (15-numBitsFilled)) & 1) == 0) {
                // This is not a significant bit. Give it both values
                minValue = 0;
                maxValue = 1;
            }
            else {
                minValue = maxValue = ((encoding >> (15-numBitsFilled)) & 1);
            }
            for (int bitValue = minValue; bitValue <= maxValue; bitValue++) {
                fillMap(map, encoding, mask, instruction, value | (bitValue << (15-numBitsFilled)), numBitsFilled + 1);
            }
        }
    }


}