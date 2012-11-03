package com.nikonhacker.disassembly.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.DisassemblyException;
import com.nikonhacker.disassembly.Instruction;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.StatementContext;
import com.nikonhacker.emu.EmulationException;

import java.util.EnumSet;
import java.util.Set;

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
     * Fake OPCodes for data reading
     * Array index is a RangeType.Width.index value
     */
    static TxInstruction[] opData = {
            new TxInstruction("dw",  "u;a", "", "", "", null, null, Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE, new SimulationCode() {
                @Override
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    throw new EmulationException("Cannot execute data at 0x" + Format.asHex(context.cpuState.pc, 8));
                }
            }),
            new TxInstruction("dl",  "u;a", "", "", "", null, null, Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE, new SimulationCode() {
                @Override
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    throw new EmulationException("Cannot execute data at 0x" + Format.asHex(context.cpuState.pc, 8));
                }
            }),
            new TxInstruction("dl",  "u;a", "", "", "", null, null, Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE, new SimulationCode() {
                @Override
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    throw new EmulationException("Cannot execute data at 0x" + Format.asHex(context.cpuState.pc, 8));
                }
            }),
            new TxInstruction("dl",  "u;T #v", "", "", "", null, null, Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE, new SimulationCode() {
                @Override
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    throw new EmulationException("Cannot execute data at 0x" + Format.asHex(context.cpuState.pc, 8));
                }
            }),
            new TxInstruction("dr",  "q;f", "", "", "", null, null, Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE, new SimulationCode() {
                @Override
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    throw new EmulationException("Cannot execute data at 0x" + Format.asHex(context.cpuState.pc, 8));
                }
            }),
    };

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

        /** Layout of type I8MOVFP instructions is as follows   : <pre>[ op  |1| r32 |  F  ]</pre> */
        I8MOVFP,

        /** Layout of type I8MOVR32 instructions is as follows  : <pre>[ I8  | F |ry |r3240]</pre> */
        I8MOVR32,

        /** Layout of type I8MOV32R instructions is as follows  : <pre>[ I8  | F |R32A4|rz ]</pre> */
        I8MOV32R,

        /** Layout of type I8SVRS instructions is as follows    : <pre>[ I8  | F  |r|s|s|imm ]</pre> */
        I8SVRS,

        /** Layout of type FPB_SPB instructions is as follows   : <pre>[ op  |rx |F|  imm  ]</pre> */
        FPB_SPB,

        /** Layout of type FP_SP_H instructions is as follows   : <pre>[ op  |rx |F| imm  |F]</pre> */
        FPH_SPH,

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
        BREAK,

        /** Layout for JAL/JALX (extended only) :<pre>[ op  |x| tar | tar |      tar       ]</pre> */
        JAL_JALX,

        /** Layout for BFINS (extended only)    :<pre>[ ext |0|bit2 |bit1 | op  |ry |rx |  F  ]</pre> */
        RR_BS1F_BFINS,

        /** Layout for MIN/MAX (extended only)  :<pre>[ ext |M|0000000|ry | op  |rz |rx |  F  ]</pre> */
        RR_MIN_MAX
    }



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

    public static final TxInstruction unknownInstruction = new TxInstruction("unk", "", "", "unk",
            "UNKnown instruction",
            InstructionFormat32.W, InstructionFormat16.W,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    throw new TxEmulationException("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 4) + " at 0x" + Format.asHex(context.cpuState.pc, 8) + ": ReservedInstructionException");
                }
            });
    public static final TxInstruction addInstruction = new TxInstruction("add", "k, [i, ]j", "kw", "add $t1,$t2,$t3",
            "ADDition with overflow: set $t1 to ($t2 plus $t3)",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int add1 = context.cpuState.getReg(statement.rs_fs);
                    int add2 = context.cpuState.getReg(statement.rt_ft);
                    int sum = add1 + add2;
                    // overflow on A+B detected when A and B have same sign and A+B has other sign.
                    if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    context.cpuState.setReg(statement.rd_fd, sum);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction subInstruction = new TxInstruction("sub", "k, [i, ]j", "kw", "sub $t1,$t2,$t3",
            "SUBtraction with overflow: set $t1 to ($t2 minus $t3)",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int sub1 = context.cpuState.getReg(statement.rs_fs);
                    int sub2 = context.cpuState.getReg(statement.rt_ft);
                    int dif = sub1 - sub2;
                    // overflow on A-B detected when A and B have opposite signs and A-B has B's sign
                    if ((sub1 >= 0 && sub2 < 0 && dif < 0) || (sub1 < 0 && sub2 >= 0 && dif >= 0)) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    context.cpuState.setReg(statement.rd_fd, dif);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction addiInstruction = new TxInstruction("addi", "j, [i, ]s", "j+", "addi $t1,$t2,-100",
            "ADDition Immediate with overflow: set $t1 to ($t2 plus signed 16-bit immediate)",
            InstructionFormat32.I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int add1 = context.cpuState.getReg(statement.rs_fs);
                    int add2 = statement.imm << 16 >> 16;
                    int sum = add1 + add2;
                    // overflow on A+B detected when A and B have same sign and A+B has other sign.
                    if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    context.cpuState.setReg(statement.rt_ft, sum);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction adduInstruction = new TxInstruction("addu", "k, [i, ]j", "kw", "addu $t1,$t2,$t3",
            "ADDition Unsigned without overflow: set $t1 to ($t2 plus $t3), no overflow",
            InstructionFormat32.R, InstructionFormat16.RRR1,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs) + context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    // alternative if rt=r0
    public static final TxInstruction moveAdduInstruction = new TxInstruction("move", "k, i;Iu", "kw", "move $t1,$t2",
            "MOVE (formally an ADDU with rt=r0): set $t1 to $t2, no overflow",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction subuInstruction = new TxInstruction("subu", "k, [i, ]j", "kw", "subu $t1,$t2,$t3",
            "SUBtraction Unsigned without overflow: set $t1 to ($t2 minus $t3), no overflow",
            InstructionFormat32.R, InstructionFormat16.RRR1,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs) - context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction addiuInstruction = new TxInstruction("addiu", "j, [i, ]s", "j+", "addiu $t1,$t2,-100",
            "ADDition Immediate 'Unsigned' without overflow: set $t1 to ($t2 plus signed 16-bit immediate), no overflow",
            InstructionFormat32.I, InstructionFormat16.RRIA,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int shift = 32 - statement.immBitWidth;
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rs_fs) + (statement.imm << shift >> shift));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    // alternative if rs=r0
    public static final TxInstruction liAddiuInstruction = new TxInstruction("li", "j, s", "jv", "li $t1,-100",
            "Load Immediate (formally an ADDIU with rs = r0): set $t1 to signed 16-bit immediate, no overflow",
            InstructionFormat32.I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, statement.imm << 16 >> 16);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction multInstruction = new TxInstruction("mult", "[k, ]i, j", "kw", "mult $t1,$t2",
            "MULTiplication: Set HI to high-order 32 bits, LO (and Rd) to low-order 32 bits of the product of $t1 and $t2",
            InstructionFormat32.R, InstructionFormat16.RRI /* not RR because rd must be rt here */,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (long) context.cpuState.getReg(statement.rs_fs) * (long) context.cpuState.getReg(statement.rt_ft);
                    context.cpuState.setReg(TxCPUState.HI, (int) (product >> 32));
                    int lo = (int) ((product << 32) >> 32);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.setReg(statement.rd_fd, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction multuInstruction = new TxInstruction("multu", "[k, ]i, j", "kw", "multu $t1,$t2",
            "MULTiplication Unsigned: Set HI to high-order 32 bits, LO (and Rd) to low-order 32 bits of the product of unsigned $t1 and $t2",
            InstructionFormat32.R, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (((long) context.cpuState.getReg(statement.rs_fs)) << 32 >>> 32)
                            * (((long) context.cpuState.getReg(statement.rt_ft)) << 32 >>> 32);
                    context.cpuState.setReg(TxCPUState.HI, (int) (product >> 32));
                    int lo = (int) ((product << 32) >> 32);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.setReg(statement.rd_fd, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction mulInstruction = new TxInstruction("mul", "k, i, j", "kw", "mul $t1,$t2,$t3",
            "MULtiplication without overflow: Set $t1 to low-order 32 bits of the product of $t2 and $t3",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (long) context.cpuState.getReg(statement.rs_fs) * (long) context.cpuState.getReg(statement.rt_ft);
                    context.cpuState.setReg(statement.rd_fd, (int) ((product << 32) >> 32));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction maddInstruction = new TxInstruction("madd", "[k, ]i, j", "kw", "madd $t1,$t2",
            "Multiply ADD: Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (long) context.cpuState.getReg(statement.rs_fs) * (long) context.cpuState.getReg(statement.rt_ft);
                    long contentsHiLo = Format.twoIntsToLong(context.cpuState.getReg(TxCPUState.HI), context.cpuState.getReg(TxCPUState.LO));
                    long sum = contentsHiLo + product;
                    context.cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(sum));
                    int lo = Format.lowOrderLongToInt(sum);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.setReg(statement.rd_fd, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction madduInstruction = new TxInstruction("maddu", "[k, ]i, j", "kw", "maddu $t1,$t2",
            "Multiply ADD Unsigned: Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product, unsigned",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (((long) context.cpuState.getReg(statement.rs_fs)) << 32 >>> 32)
                            * (((long) context.cpuState.getReg(statement.rt_ft)) << 32 >>> 32);
                    long contentsHiLo = Format.twoIntsToLong(context.cpuState.getReg(TxCPUState.HI), context.cpuState.getReg(TxCPUState.LO));
                    long sum = contentsHiLo + product;
                    context.cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(sum));
                    int lo = Format.lowOrderLongToInt(sum);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.setReg(statement.rd_fd, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction msubInstruction = new TxInstruction("msub", "[k, ]i, j", "kw", "msub $t1,$t2",
            "Multiply SUBtract: Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decrement LO by low-order 32 bits of product",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (long) context.cpuState.getReg(statement.rs_fs) * (long) context.cpuState.getReg(statement.rt_ft);
                    long contentsHiLo = Format.twoIntsToLong(context.cpuState.getReg(TxCPUState.HI), context.cpuState.getReg(TxCPUState.LO));
                    long diff = contentsHiLo - product;
                    context.cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(diff));
                    int lo = Format.lowOrderLongToInt(diff);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.setReg(statement.rd_fd, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction msubuInstruction = new TxInstruction("msubu", "[k, ]i, j", "kw", "msubu $t1,$t2",
            "Multiply SUBtract Unsigned: Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decement LO by low-order 32 bits of product, unsigned",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (((long) context.cpuState.getReg(statement.rs_fs)) << 32 >>> 32)
                            * (((long) context.cpuState.getReg(statement.rt_ft)) << 32 >>> 32);
                    long contentsHiLo = Format.twoIntsToLong(context.cpuState.getReg(TxCPUState.HI), context.cpuState.getReg(TxCPUState.LO));
                    long diff = contentsHiLo - product;
                    context.cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(diff));
                    int lo = Format.lowOrderLongToInt(diff);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.setReg(statement.rd_fd, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction divInstruction = new TxInstruction("div", "i, j", "iw", "div $t1,$t2",
            "DIVision with overflow: Divide $t1 by $t2 then set LO to quotient and HI to remainder",
            InstructionFormat32.R, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rt_ft) == 0) {
                        // Note: no exceptions, and undefined results for zero divide
                    }
                    else {
                        context.cpuState.setReg(TxCPUState.HI, context.cpuState.getReg(statement.rs_fs) % context.cpuState.getReg(statement.rt_ft));
                        context.cpuState.setReg(TxCPUState.LO, context.cpuState.getReg(statement.rs_fs) / context.cpuState.getReg(statement.rt_ft));
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction divuInstruction = new TxInstruction("divu", "i, j", "iw", "divu $t1,$t2",
            "DIVision Unsigned without overflow: Divide unsigned $t1 by $t2 then set LO to quotient and HI to remainder",
            InstructionFormat32.R, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rt_ft) == 0) {
                        // Note: no exceptions, and undefined results for zero divide
                    }
                    else {
                        long oper1 = ((long) context.cpuState.getReg(statement.rs_fs)) << 32 >>> 32;
                        long oper2 = ((long) context.cpuState.getReg(statement.rt_ft)) << 32 >>> 32;
                        context.cpuState.setReg(TxCPUState.HI, (int) (((oper1 % oper2) << 32) >> 32));
                        context.cpuState.setReg(TxCPUState.LO, (int) (((oper1 / oper2) << 32) >> 32));
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction mfhiInstruction = new TxInstruction("mfhi", "k", "kw", "mfhi $t1",
            "Move From HI register: Set $t1 to contents of HI (see multiply and divide operations)",
            InstructionFormat32.R, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(TxCPUState.HI));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction mfloInstruction = new TxInstruction("mflo", "k", "kw", "mflo $t1",
            "Move From LO register: Set $t1 to contents of LO (see multiply and divide operations)",
            InstructionFormat32.R, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(TxCPUState.LO));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction mthiInstruction = new TxInstruction("mthi", "i;Iu", "", "mthi $t1",
            "Move To HI registerr: Set HI to contents of $t1 (see multiply and divide operations)",
            InstructionFormat32.R, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(TxCPUState.HI, context.cpuState.getReg(statement.rs_fs));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction mtloInstruction = new TxInstruction("mtlo", "i:Iu", "", "mtlo $t1",
            "Move To LO register: Set LO to contents of $t1 (see multiply and divide operations)",
            InstructionFormat32.R, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(TxCPUState.LO, context.cpuState.getReg(statement.rs_fs));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction andInstruction = new TxInstruction("and", "k, [i, ]j", "kw", "and $t1,$t2,$t3",
            "bitwise AND: Set $t1 to bitwise AND of $t2 and $t3",
            InstructionFormat32.R, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs) & context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction orInstruction = new TxInstruction("or", "k, [i, ]j", "kw", "or $t1,$t2,$t3",
            "bitwise OR: Set $t1 to bitwise OR of $t2 and $t3",
            InstructionFormat32.R, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs) | context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    // alternative if rs=r0
    public static final TxInstruction moveOrInstruction = new TxInstruction("move", "k, j;Ju", "kw", "move $t1,$t3",
            "MOVE (formally an OR with rs=r0): Set $t1 to $t3",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction andiInstruction = new TxInstruction("andi", "j, [i, ]u", "jw", "andi $t1,$t2,100",
            "bitwise AND Immediate: Set $t1 to bitwise AND of $t2 and zero-extended 16-bit immediate",
            InstructionFormat32.I, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rs_fs) & statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction oriInstruction = new TxInstruction("ori", "j, [i, ]u", "j|", "ori $t1,$t2,100",
            "bitwise OR Immediate: Set $t1 to bitwise OR of $t2 and zero-extended 16-bit immediate",
            InstructionFormat32.I, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rs_fs) | statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    // alternative if rs=r0
    public static final TxInstruction liOriInstruction = new TxInstruction("li", "j, s", "jv", "li $t1,100",
            "Load Immediate (formally an ORI with rs=r0): Set $t1 to zero-extended 16-bit immediate",
            InstructionFormat32.I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction norInstruction = new TxInstruction("nor", "k, [i, ]j", "kw", "nor $t1,$t2,$t3",
            "bitwise NOR: Set $t1 to bitwise NOR of $t2 and $t3",
            InstructionFormat32.R,
            null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, ~(context.cpuState.getReg(statement.rs_fs) | context.cpuState.getReg(statement.rt_ft)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction xorInstruction = new TxInstruction("xor", "k, i, j", "kw", "xor $t1,$t2,$t3",
            "bitwise XOR (exclusive OR): Set $t1 to bitwise XOR of $t2 and $t3",
            InstructionFormat32.R, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs) ^ context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction xoriInstruction = new TxInstruction("xori", "j, [i, ]u", "jw", "xori $t1,$t2,100",
            "bitwise XOR Immediate: Set $t1 to bitwise XOR of $t2 and zero-extended 16-bit immediate",
            InstructionFormat32.I,  InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rs_fs) ^ statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction sllInstruction = new TxInstruction("sll", "k, [j, ]l", "kw", "sll $t1,$t2,10",
            "Shift Left Logical: Set $t1 to result of shifting $t2 left by number of bits specified by immediate",
            InstructionFormat32.R, InstructionFormat16.SHIFT1,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rt_ft) << statement.sa_cc);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    // alternate 32-bit SLL with 0 registers, or alternate 16-bit move to $zero
    public static final TxInstruction nopInstruction = new TxInstruction("nop", "", "", "nop",
            "NOP (formally a useless 32b SLL or 16b MOVE): Do nothing",
            InstructionFormat32.R, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // nop
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction sllvInstruction = new TxInstruction("sllv", "k, [j, ]i", "kw", "sllv $t1,$t2,$t3",
            "Shift Left Logical Variable: Set $t1 to result of shifting $t2 left by number of bits specified by value in low-order 5 bits of $t3",
            InstructionFormat32.R, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // Mask all but low 5 bits of register containing shift amount.
                    context.cpuState.setReg(statement.rd_fd,
                            (context.cpuState.getReg(statement.rt_ft) << (context.cpuState.getReg(statement.rs_fs)) & 0b11111));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction srlInstruction = new TxInstruction("srl", "k, [j, ]l", "kw", "srl $t1,$t2,10",
            "Shift Right Logical: Set $t1 to result of shifting $t2 right by number of bits specified by immediate",
            InstructionFormat32.R, InstructionFormat16.SHIFT1,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // must zero-fill, so use ">>>" instead of ">>".
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rt_ft) >>> statement.sa_cc);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction sraInstruction = new TxInstruction("sra", "k, [j, ]l", "kw", "sra $t1,$t2,10",
            "Shift Right Arithmetic: Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by immediate",
            InstructionFormat32.R, InstructionFormat16.SHIFT1,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // must sign-fill, so use ">>".
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rt_ft) >> statement.sa_cc);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction sravInstruction = new TxInstruction("srav", "k, [j, ]i", "kw", "srav $t1,$t2,$t3",
            "Shift Right Arithmetic Variable: Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3",
            InstructionFormat32.R, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // Mask all but low 5 bits of register containing shift amount. Use ">>" to sign-fill.
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rt_ft) >> (context.cpuState.getReg(statement.rs_fs) & 0b11111));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction srlvInstruction = new TxInstruction("srlv", "k, [j, ]i", "kw", "srlv $t1,$t2,$t3",
            "Shift Right Logical Variable: Set $t1 to result of shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3",
            InstructionFormat32.R, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // Mask all but low 5 bits of register containing shift amount. Use ">>>" to zero-fill.
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rt_ft) >>> (context.cpuState.getReg(statement.rs_fs) & 0b11111));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lwInstruction = new TxInstruction("lw", "j, s(i)", "jw", "lw $t1,-100($t2)",
            "Load Word: Set $t1 to contents of effective memory word address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // Changed from MARS : Added sign extension to offset
                    context.cpuState.setReg(statement.rt_ft, context.memory.load32(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction lwlInstruction = new TxInstruction("lwl", "j, s(i)", "jw", "lwl $t1,-100($t2)",
            "Load Word Left: Load from 1 to 4 bytes left-justified into $t1, starting with effective memory byte address and continuing through the low-order byte of its word",
            InstructionFormat32.I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16);
                    int result = context.cpuState.getReg(statement.rt_ft);
                    for (int i = 0; i <= address % 4; i++) {
                        result = Format.setByte(result, 3 - i, context.memory.loadUnsigned8(address - i));
                    }
                    context.cpuState.setReg(statement.rt_ft, result);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction lwrInstruction = new TxInstruction("lwr", "j, s(i)", "jw", "lwr $t1,-100($t2)",
            "Load Word Right: Load from 1 to 4 bytes right-justified into $t1, starting with effective memory byte address and continuing through the high-order byte of its word",
            InstructionFormat32.I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16);
                    int result = context.cpuState.getReg(statement.rt_ft);
                    for (int i = 0; i <= 3 - (address % 4); i++) {
                        result = Format.setByte(result, i, context.memory.loadUnsigned8(address + i));
                    }
                    context.cpuState.setReg(statement.rt_ft, result);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction swInstruction = new TxInstruction("sw", "j, s(i);Ju", "", "sw $t1,-100($t2)",
            "Store Word: Store contents of $t1 into effective memory word address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store32(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16), context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction swlInstruction = new TxInstruction("swl", "j, s(i)", "", "swl $t1,-100($t2)",
            "Store Word Left: Store high-order 1 to 4 bytes of $t1 into memory, starting with effective byte address and continuing through the low-order byte of its word",
            InstructionFormat32.I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16);
                    int source = context.cpuState.getReg(statement.rt_ft);
                    for (int i = 0; i <= address % 4; i++) {
                        context.memory.store8(address - i, Format.getByte(source, 3 - i));
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction swrInstruction = new TxInstruction("swr", "j, s(i)", "", "swr $t1,-100($t2)",
            "Store Word Right: Store low-order 1 to 4 bytes of $t1 into memory, starting with high-order byte of word containing effective byte address and continuing through that byte address",
            InstructionFormat32.I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16);
                    int source = context.cpuState.getReg(statement.rt_ft);
                    for (int i = 0; i <= 3 - (address % 4); i++) {
                        context.memory.store8(address + i, Format.getByte(source, i));
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction luiInstruction = new TxInstruction("lui", "j, u", "jV", "lui $t1,100",
            "Load Upper Immediate: Set high-order 16 bits of $t1 to 16-bit immediate and low-order 16 bits to 0",
            InstructionFormat32.I, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, statement.imm << 16);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction beqInstruction = new TxInstruction("beq", "i, j, 4rs", "", "beq $t1,$t2,label",
            "Branch if EQual: Branch to statement at label's address if $t1 and $t2 are equal",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) == context.cpuState.getReg(statement.rt_ft)) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    // alternative if rt=r0
    public static final TxInstruction beqzInstruction = new TxInstruction("beqz", "i, 4rs", "", "beqz $t1,label",
            "Branch if EQual Zero: Branch to statement at label's address if $t1 is zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) == 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction beqlInstruction = new TxInstruction("beql", "i, j, 4rs", "", "beql $t1,$t2,label",
            "Branch if EQual (Likely): Branch to statement at label's address if $t1 and $t2 are equal",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) == context.cpuState.getReg(statement.rt_ft)) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    // alternative if rt=r0
    public static final TxInstruction beqzlInstruction = new TxInstruction("beqzl", "i, 4rs", "", "beqzl $t1,label",
            "Branch if EQual Zero (Likely): Branch to statement at label's address if $t1 is zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) == 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    public static final TxInstruction bneInstruction = new TxInstruction("bne", "i, j, 4rs", "", "bne $t1,$t2,label",
            "Branch if Not Equal: Branch to statement at label's address if $t1 and $t2 are not equal",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) != context.cpuState.getReg(statement.rt_ft)) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    // alternative if rt=r0
    public static final TxInstruction bnezInstruction = new TxInstruction("bnez", "i, 4rs", "", "bnez $t1,label",
            "Branch if Not Equal Zero: Branch to statement at label's address if $t1 is not zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) != 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction bnelInstruction = new TxInstruction("bnel", "i, j, 4rs", "", "bnel $t1,$t2,label",
            "Branch if Not Equal (Likely): Branch to statement at label's address if $t1 and $t2 are not equal",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) != context.cpuState.getReg(statement.rt_ft)) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    // alternative if rt=r0
    public static final TxInstruction bnezlInstruction = new TxInstruction("bnezl", "i, 4rs", "", "bnezl $t1,label",
            "Branch if Not Equal Zero (Likely): Branch to statement at label's address if $t1 is not zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) != 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    public static final TxInstruction bgezInstruction = new TxInstruction("bgez", "i, 4rs", "", "bgez $t1,label",
            "Branch if Greater than or Equal to Zero: Branch to statement at label's address if $t1 is greater than or equal to zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) >= 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction bgezlInstruction = new TxInstruction("bgezl", "i, 4rs", "", "bgezl $t1,label",
            "Branch if Greater than or Equal to Zero (Likely): Branch to statement at label's address if $t1 is greater than or equal to zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) >= 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    public static final TxInstruction bgezalInstruction = new TxInstruction("bgezal", "i, 4rs", "", "bgezal $t1,label",
            "Branch if Greater then or Equal to Zero And Link: If $t1 is greater than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.CALL, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) >= 0) {
                        context.setDelayedPcAndRa(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14), // sign extend and x4
                                context.cpuState.getPc() + 8 // return address after the delay slot
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    // alternative if rs=0
    public static final TxInstruction balInstruction = new TxInstruction("bal", "4rs", "", "bal label",
            "Branch And Link: Set $ra to the Program Counter and branch to statement at label's address",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.CALL, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.setDelayedPcAndRa(
                            context.cpuState.pc + 4 + (statement.imm << 16 >> 14), // sign extend and x4
                            context.cpuState.getPc() + 8 // return address after the delay slot
                    );
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction bgezallInstruction = new TxInstruction("bgezall", "i, 4rs", "", "bgezall $t1,label",
            "Branch if Greater then or Equal to Zero And Link (Likely): If $t1 is greater than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.CALL, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) >= 0) {
                        context.setDelayedPcAndRa(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14), // sign extend and x4
                                context.cpuState.getPc() + 8 // return address after the delay slot
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    public static final TxInstruction bgtzInstruction = new TxInstruction("bgtz", "i, 4rs", "", "bgtz $t1,label",
            "Branch if Greater Than Zero: Branch to statement at label's address if $t1 is greater than zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) > 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction bgtzlInstruction = new TxInstruction("bgtzl", "i, 4rs", "", "bgtzl $t1,label",
            "Branch if Greater Than Zero (Likely): Branch to statement at label's address if $t1 is greater than zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) > 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    public static final TxInstruction blezInstruction = new TxInstruction("blez", "i, 4rs", "", "blez $t1,label",
            "Branch if Less than or Equal to Zero: Branch to statement at label's address if $t1 is less than or equal to zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) <= 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction blezlInstruction = new TxInstruction("blezl", "i, 4rs", "", "blezl $t1,label",
            "Branch if Less than or Equal to Zero (Likely): Branch to statement at label's address if $t1 is less than or equal to zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) <= 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    public static final TxInstruction bltzInstruction = new TxInstruction("bltz", "i, 4rs", "", "bltz $t1,label",
            "Branch if Less Than Zero: Branch to statement at label's address if $t1 is less than zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) < 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction bltzlInstruction = new TxInstruction("bltzl", "i, 4rs", "", "bltzl $t1,label",
            "Branch if Less Than Zero (Likely): Branch to statement at label's address if $t1 is less than zero",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) < 0) {
                        context.setDelayedPc(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14) // sign extend and x4
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    public static final TxInstruction bltzalInstruction = new TxInstruction("bltzal", "i, 4rs", "", "bltzal $t1,label",
            "Branch if Less Than Zero And Link: If $t1 is less than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.CALL, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) < 0) {
                        context.setDelayedPcAndRa(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14), // sign extend and x4
                                context.cpuState.getPc() + 8 // return address after the delay slot
                        );
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction bltzallInstruction = new TxInstruction("bltzall", "i, 4rs", "", "bltzall $t1,label",
            "Branch if Less Than Zero And Link (Likely): If $t1 is less than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            InstructionFormat32.I_BRANCH, null,
            Instruction.FlowType.CALL, true, Instruction.DelaySlotType.LIKELY,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) < 0) {
                        context.setDelayedPcAndRa(
                                context.cpuState.pc + 4 + (statement.imm << 16 >> 14), // sign extend and x4
                                context.cpuState.getPc() + 8 // return address after the delay slot
                        );
                        context.cpuState.pc += statement.getNumBytes(); // We take care of the incrementing the PC because it varies in 'likely' instructions
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes() << 1; // if a 'likely' test fails, the statement in the delay slot is nullified (skipped)
                        context.setStoredDelaySlotType(Instruction.DelaySlotType.NONE);  // Cancel the delay slot info used to display the next statement indented
                    }
                }
            });
    public static final TxInstruction sltInstruction = new TxInstruction("slt", "k, i, j", "kw", "slt $t1,$t2,$t3",
            "Set on Less Than: If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, (context.cpuState.getReg(statement.rs_fs) < context.cpuState.getReg(statement.rt_ft)) ? 1 : 0);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction sltuInstruction = new TxInstruction("sltu", "k, i, j", "kw", "sltu $t1,$t2,$t3",
            "Set on Less Than Unsigned: If $t2 is less than $t3 using unsigned comparision, then set $t1 to 1 else set $t1 to 0",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int first = context.cpuState.getReg(statement.rs_fs);
                    int second = context.cpuState.getReg(statement.rt_ft);
                    if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                        context.cpuState.setReg(statement.rd_fd, (first < second) ? 1 : 0);
                    } else {
                        context.cpuState.setReg(statement.rd_fd, (first >= 0) ? 1 : 0);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction sltiInstruction = new TxInstruction("slti", "j, i, s", "jw", "slti $t1,$t2,-100",
            "Set on Less Than Immediate: If $t2 is less than sign-extended 16-bit immediate, then set $t1 to 1 else set $t1 to 0",
            InstructionFormat32.I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // 16 bit immediate value in statement.imm is sign-extended
                    context.cpuState.setReg(statement.rt_ft, (context.cpuState.getReg(statement.rs_fs) < (statement.imm << 16 >> 16)) ? 1 : 0);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction sltiuInstruction = new TxInstruction("sltiu", "j, i, s", "jw", "sltiu $t1,$t2,-100",
            "Set on Less Than Immediate Unsigned: If $t2 is less than sign-extended 16-bit immediate using unsigned comparison, then set $t1 to 1 else set $t1 to 0",
            InstructionFormat32.I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int first = context.cpuState.getReg(statement.rs_fs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                        context.cpuState.setReg(statement.rt_ft, (first < second) ? 1 : 0);
                    } else {
                        context.cpuState.setReg(statement.rt_ft, (first >= 0) ? 1 : 0);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction movnInstruction = new TxInstruction("movn", "k, i, j", "kw", "movn $t1,$t2,$t3",
            "MOVe conditional on Non zero: Set $t1 to $t2 if $t3 is not zero",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rt_ft) != 0) {
                        context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs));
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction movzInstruction = new TxInstruction("movz", "k, i, j", "kw", "movz $t1,$t2,$t3",
            "MOVe conditional on Zero: Set $t1 to $t2 if $t3 is zero",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rt_ft) == 0) {
                        context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs));
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction breakInstruction = new TxInstruction("break", "u", "", "break 100",
            "Break execution with code: Terminate program execution with specified exception code",
            InstructionFormat32.BREAK, InstructionFormat16.BREAK,
            Instruction.FlowType.INT, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // so will just halt execution with a message.
                    throw new TxEmulationException(statement, "break instruction executed; code = " +
                            statement.imm + ".", Exceptions.BREAKPOINT_EXCEPTION);
                }
            });
    public static final TxInstruction jInstruction = new TxInstruction("j", "4Ru", "", "j target",
            "Jump unconditionally: Jump to statement at target address",
            InstructionFormat32.J, null,
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // The ISA mode is unchanged by the J instruction, but it only exists in 32bit, so the ISA mode LSB is always 0 anyway.
                    context.setDelayedPc(
                            (context.cpuState.pc & 0xF0000000) | (statement.imm << 2)
                    );
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction jrInstruction = new TxInstruction("jr", "i;Iu", "", "jr $t1",
            "Jump Register unconditionally: Jump to statement whose address is in $t1",
            InstructionFormat32.R, InstructionFormat16.RI,
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.setDelayedPc(
                            context.cpuState.getReg(statement.rs_fs)
                    );
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    // alternative if rs=ra
    public static final TxInstruction retInstruction = new TxInstruction("ret", "", "", "ret",
            "RETurn (formally a JR to $ra): Return to calling statement",
            InstructionFormat32.R, null,
            Instruction.FlowType.RET, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.setDelayedPc(
                            context.cpuState.getReg(TxCPUState.RA)
                    );
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction jalInstruction = new TxInstruction("jal", "4Ru", "", "jal target",
            "Jump And Link: Set $ra to Program Counter (return address) then jump to statement at target address",
            InstructionFormat32.J, InstructionFormat16.JAL_JALX,
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int pc = context.cpuState.getPc();
                    context.setDelayedPcAndRa(
                            (pc & 0xF0000001) | (statement.imm << 2), // prepare the jump. "The JAL instruction never toggles the ISA mode"
                            pc + (((TxCPUState)context.cpuState).is16bitIsaMode?4:8)  // return address after the delay slot
                    );
                    context.cpuState.pc += statement.getNumBytes(); // Execute the statement in the delay slot
                }
            });
    public static final TxInstruction jalxInstruction = new TxInstruction("jalx", "4Ru", "", "jalx target",
            "Jump And Link eXchanging isa mode: Set $ra to Program Counter (return address) then jump to statement at target address, toggling ISA mode",
            InstructionFormat32.J, InstructionFormat16.JAL_JALX,
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int pc = context.cpuState.getPc();
                    context.setDelayedPcAndRa(
                            ((pc & 0xF0000001) ^ 1) | (statement.imm << 2),  // "The JALX instruction unconditionally toggles the ISA mode"
                            pc + (((TxCPUState)context.cpuState).is16bitIsaMode?4:8)  // return address after the delay slot
                    );
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction jalrInstruction = new TxInstruction("jalr", "(k,) i;Iu", "", "jalr $t1,$t2", // TODO omit rd if rd=$ra
            "Jump And Link Register: Set $t1 to Program Counter (return address) then jump to statement whose address is in $t2",
            InstructionFormat32.R, null,
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.setDelayedPcAndRaAndTarget(
                            context.cpuState.getReg(statement.rs_fs), // Next PC
                            context.cpuState.getPc() + 8, // return address after the delay slot
                            statement.rd_fd // register to store return address into, after delay slot
                    );
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction cloInstruction = new TxInstruction("clo", "k, i", "kw", "clo $t1,$t2",
            "Count number of Leading Ones: Set $t1 to the count of leading one bits in $t2 starting at most significant bit position",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int value = context.cpuState.getReg(statement.rs_fs);
                    int leadingOnes = 0;
                    int bitPosition = 31;
                    while (Format.bitValue(value, bitPosition) == 1 && bitPosition >= 0) {
                        leadingOnes++;
                        bitPosition--;
                    }
                    context.cpuState.setReg(statement.rd_fd, leadingOnes);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction clzInstruction = new TxInstruction("clz", "k, i", "kw", "clz $t1,$t2",
            "Count number of Leading Zeroes: Set $t1 to the count of leading zero bits in $t2 starting at most significant bit positio",
            InstructionFormat32.R, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int value = context.cpuState.getReg(statement.rs_fs);
                    int leadingZeros = 0;
                    int bitPosition = 31;
                    while (Format.bitValue(value, bitPosition) == 0 && bitPosition >= 0) {
                        leadingZeros++;
                        bitPosition--;
                    }
                    context.cpuState.setReg(statement.rd_fd, leadingZeros);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction mfc0Instruction = new TxInstruction("mfc0", "j, k", "jw", "mfc0 $t1,$8",
            "Move From Coprocessor 0: Set $t1 to the value stored in Coprocessor 0 register $8",
            InstructionFormat32.CP0, InstructionFormat16.SHIFT2,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rd_fd));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction mtc0Instruction = new TxInstruction("mtc0", "j, k;Ju", "", "mtc0 $t1,$8",
            "Move To Coprocessor 0: Set Coprocessor 0 register $8 to value stored in $t1",
            InstructionFormat32.CP0, InstructionFormat16.SHIFT2,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rt_ft)) ;
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /////////////////////// CP1 and Floating Point Instructions Start Here ////////////////
    public static final TxInstruction mfc1Instruction = new TxInstruction("mfc1", "j, i", "jw", "mfc1 $t1,$8",
            "Move From Coprocessor 1: Set $t1 to the value stored in Coprocessor 1 register $8",
            InstructionFormat32.CP1_R1, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rs_fs/*fs*/));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction mtc1Instruction = new TxInstruction("mtc1", "j, i;Ju", "", "mtc1 $t1,$8",
            "Move To Coprocessor 1: Set Coprocessor 1 register $8 to value stored in $t1",
            InstructionFormat32.CP1_R1, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rs_fs/*fs*/, context.cpuState.getReg(statement.rt_ft)) ;
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction cfc1Instruction = new TxInstruction("cfc1", "j, i", "jw", "cfc1 $t1,$8",
            "move Control From Coprocessor 1: Set $t1 to the value stored in coprocessor 1 control register $8",
            InstructionFormat32.CP1_CR1, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, ((TxCPUState)context.cpuState).getCp1CrReg(statement.rs_fs/*cr#*/));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction ctc1Instruction = new TxInstruction("ctc1", "j, i;Ju", "", "ctc1 $t1,$8",
            "move Control To Coprocessor 1: Set coprocessor 1 control register $8 to value stored in $t1",
            InstructionFormat32.CP1_CR1, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            // Crash @BFC00600 : Unknown CP1 register number 71
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    ((TxCPUState)context.cpuState).setCp1CrReg(statement.rs_fs/*cr#*/, context.cpuState.getReg(statement.rt_ft)) ;
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    public static final TxInstruction lwc1Instruction = new TxInstruction("lwc1", "j, s(i)", "jw", "lwc1 $f1,-100($t2)",
            "Load Word into Coprocessor 1 (FPU): Set $f1 to 32-bit value from effective memory word address",
            InstructionFormat32.CP1_I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.load32(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction swc1Instruction = new TxInstruction("swc1", "j, s(i);Ju", "", "swc1 $f1,-100($t2)",
            "Store Word from Coprocessor 1 (FPU): Store 32 bit value in $f1 to effective memory word address",
            InstructionFormat32.CP1_I, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store32(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16), context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction addSInstruction = new TxInstruction("add.s", "k, [i, ]j", "kw", "add.s $f0,$f1,$f3",
            "floating point ADDition Single precision: Set $f0 to single-precision floating point value of $f1 plus $f3",
            InstructionFormat32.CP1_R2, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    float add1 = Float.intBitsToFloat(context.cpuState.getReg(statement.rs_fs));
                    float add2 = Float.intBitsToFloat(context.cpuState.getReg(statement.rt_ft));
                    float sum = add1 + add2;
                    // overflow detected when sum is positive or negative infinity.
                    /*
                    if (sum == Float.NEGATIVE_INFINITY || sum == Float.POSITIVE_INFINITY) {
                      throw new ProcessingException(statement,"arithmetic overflow");
                    }
                    */
                    context.cpuState.setReg(statement.rd_fd, Float.floatToIntBits(sum));
                    context.cpuState.pc += statement.getNumBytes();
                }
            }
    );
    public static final TxInstruction subSInstruction = new TxInstruction("sub.s", "k, [i, ]j", "kw", "sub.s $f0,$f1,$f3",
            "floating point SUBtraction Single precision: Set $f0 to single-precision floating point value of $f1  minus $f3",
            InstructionFormat32.CP1_R2, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    float sub1 = Float.intBitsToFloat(context.cpuState.getReg(statement.rs_fs));
                    float sub2 = Float.intBitsToFloat(context.cpuState.getReg(statement.rt_ft));
                    float diff = sub1 - sub2;
                    context.cpuState.setReg(statement.rd_fd, Float.floatToIntBits(diff));
                    context.cpuState.pc += statement.getNumBytes();
                }
            }
    );
    public static final TxInstruction mulSInstruction = new TxInstruction("mul.s", "k, [i, ]j", "kw", "mul.s $f0,$f1,$f3",
            "floating point MULtiplication Single precision: Set $f0 to single-precision floating point value of $f1 times $f3",
            InstructionFormat32.CP1_R2, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    float mul1 = Float.intBitsToFloat(context.cpuState.getReg(statement.rs_fs));
                    float mul2 = Float.intBitsToFloat(context.cpuState.getReg(statement.rt_ft));
                    float prod = mul1 * mul2;
                    context.cpuState.setReg(statement.rd_fd, Float.floatToIntBits(prod));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction divSInstruction = new TxInstruction("div.s", "k, [i, ]j", "kw", "div.s $f0,$f1,$f3",
            "floating point DIVision Single precision: Set $f0 to single-precision floating point value of $f1 divided by $f3",
            InstructionFormat32.CP1_R2, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    float div1 = Float.intBitsToFloat(context.cpuState.getReg(statement.rs_fs));
                    float div2 = Float.intBitsToFloat(context.cpuState.getReg(statement.rt_ft));
                    float quot = div1 / div2;
                    context.cpuState.setReg(statement.rd_fd, Float.floatToIntBits(quot));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction bc1fInstruction = new TxInstruction("bc1f", "[l, ]4rs", "", "bc1f 1,label",
            "Branch if specified fp condition of Coprocessor 1 flag False (BC1F, not BCLF): If Coprocessor 1 condition flag specified by immediate is false (zero) then branch to statement at label's address",
            InstructionFormat32.CP1_CC_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (((TxCPUState)context.cpuState).getConditionFlag(statement.sa_cc) == 0) {
                        context.cpuState.pc += 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes(); // normal flow
                    }
                }
            });
    public static final TxInstruction bc1tInstruction = new TxInstruction("bc1t", "[l, ]4rs", "", "bc1t 1,label",
            "Branch if specified fp condition flag of Coprocessor 1 flag True (BC1T, not BCLT): If Coprocessor 1 condition flag specified by immediate is true (one) then branch to statement at label's address",
            InstructionFormat32.CP1_CC_BRANCH, null,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (((TxCPUState)context.cpuState).getConditionFlag(statement.sa_cc) == 1) {
                        context.cpuState.pc += 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes(); // normal flow
                    }
                }
            });

    public static final TxInstruction cvtSWInstruction = new TxInstruction("cvt.s.w", "k, i", "kw", "cvt.s.w $f0,$f1",
            "ConVerT to Single precision from Word: Set $f0 to single precision equivalent of 32-bit integer value in $f2",
            InstructionFormat32.CP1_R2, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // convert integer to single (interpret $f1 value as int?)
                    context.cpuState.setReg(statement.rd_fd, Float.floatToIntBits((float) context.cpuState.getReg(statement.rs_fs)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction cvtWSInstruction = new TxInstruction("cvt.w.s", "k, i", "kw", "cvt.w.s $f0,$f1",
            "ConVerT to Word from Single precision: Set $f0 to 32-bit integer equivalent of single precision value in $f1",
            InstructionFormat32.CP1_R2, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // convert single precision in $f1 to integer stored in $f0
                    context.cpuState.setReg(statement.rd_fd, (int) Float.intBitsToFloat(context.cpuState.getReg(statement.rs_fs)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    public static final TxInstruction cEqSInstruction = new TxInstruction("c.eq.s", "[l, ]i, j", "", "c.eq.s 1,$f0,$f1",
            "Compare EQual Single precision: If $f0 is equal to $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false",
            InstructionFormat32.CP1_R_CC, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    float op1 = Float.intBitsToFloat(context.cpuState.getReg(statement.rs_fs));
                    float op2 = Float.intBitsToFloat(context.cpuState.getReg(statement.rt_ft));
                    if (op1 == op2)
                        ((TxCPUState)context.cpuState).setConditionFlag(statement.sa_cc);
                    else
                        ((TxCPUState)context.cpuState).clearConditionFlag(statement.sa_cc);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction cLeSInstruction = new TxInstruction("c.le.s", "[l, ]i, j", "", "c.le.s 1,$f0,$f1",
            "Compare Less or Equal Single precision: If $f0 is less than or equal to $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false",
            InstructionFormat32.CP1_R_CC, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    float op1 = Float.intBitsToFloat(context.cpuState.getReg(statement.rs_fs));
                    float op2 = Float.intBitsToFloat(context.cpuState.getReg(statement.rt_ft));
                    if (op1 <= op2)
                        ((TxCPUState)context.cpuState).setConditionFlag(statement.sa_cc);
                    else
                        ((TxCPUState)context.cpuState).clearConditionFlag(statement.sa_cc);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction cLtSInstruction = new TxInstruction("c.lt.s", "[l, ]i, j", "", "c.lt.s 1,$f0,$f1",
            "Compare Less Than Single precision: If $f0 is less than $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false",
            InstructionFormat32.CP1_R_CC, null,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    float op1 = Float.intBitsToFloat(context.cpuState.getReg(statement.rs_fs));
                    float op2 = Float.intBitsToFloat(context.cpuState.getReg(statement.rt_ft));
                    if (op1 < op2)
                        ((TxCPUState)context.cpuState).setConditionFlag(statement.sa_cc);
                    else
                        ((TxCPUState)context.cpuState).clearConditionFlag(statement.sa_cc);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    // TRAP instructions
    public static final TxInstruction teqInstruction = new TxInstruction("teq", "i, j, u;Iu, Ju", "", "teq $t1,$t2,$t3",
            "Trap if EQual: Trap with code $t3 if $t1 is equal to $t2",
            InstructionFormat32.TRAP, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) == context.cpuState.getReg(statement.rt_ft)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction teqiInstruction = new TxInstruction("teqi", "i, s", "", "teqi $t1,-100",
            "Trap if EQual to Immediate: Trap if $t1 is equal to sign-extended 16 bit immediate",
            InstructionFormat32.I, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) == (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tneInstruction = new TxInstruction("tne", "i, j, u", "", "tne $t1,$t2",
            "Trap if Not Equal: Trap if $t1 is not equal to $t2",
            InstructionFormat32.TRAP, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) != context.cpuState.getReg(statement.rt_ft)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tneiInstruction = new TxInstruction("tnei", "i, s", "", "tnei $t1,-100",
            "Trap if Not Equal to Immediate: Trap if $t1 is not equal to sign-extended 16 bit immediate",
            InstructionFormat32.I, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) != (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tgeInstruction = new TxInstruction("tge", "i, j, u", "", "tge $t1,$t2",
            "Trap if Greater or Equal: Trap if $t1 is greater than or equal to $t2",
            InstructionFormat32.TRAP, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) >= context.cpuState.getReg(statement.rt_ft)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tgeuInstruction = new TxInstruction("tgeu", "i, j, u", "", "tgeu $t1,$t2",
            "Trap if Greater or Equal Unsigned: Trap if $t1 is greater than or equal to $t2 using unsigned comparision",
            InstructionFormat32.TRAP, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int first = context.cpuState.getReg(statement.rs_fs);
                    int second = context.cpuState.getReg(statement.rt_ft);
                    // if signs same, do straight compare; if signs differ & first negative then first greater else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first >= second) : (first < 0)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tgeiInstruction = new TxInstruction("tgei", "i, s", "", "tgei $t1,-100",
            "Trap if Greater than or Equal to Immediate: Trap if $t1 greater than or equal to sign-extended 16 bit immediate",
            InstructionFormat32.I, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) >= (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tgeiuInstruction = new TxInstruction("tgeiu", "i, s", "", "tgeiu $t1,-100",
            "Trap if Greater or Equal to Immediate unsigned: Trap if $t1 greater than or equal to sign-extended 16 bit immediate, unsigned comparison",
            InstructionFormat32.I, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int first = context.cpuState.getReg(statement.rs_fs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    // if signs same, do straight compare; if signs differ & first negative then first greater else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first >= second) : (first < 0)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tltInstruction = new TxInstruction("tlt", "i, j, u", "", "tlt $t1,$t2",
            "Trap if Less Than: Trap if $t1 less than $t2",
            InstructionFormat32.TRAP, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) < context.cpuState.getReg(statement.rt_ft)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tltuInstruction = new TxInstruction("tltu", "i, j, u", "", "tltu $t1,$t2",
            "Trap if Less Than Unsigned: Trap if $t1 less than $t2, unsigned comparison",
            InstructionFormat32.TRAP, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int first = context.cpuState.getReg(statement.rs_fs);
                    int second = context.cpuState.getReg(statement.rt_ft);
                    // if signs same, do straight compare; if signs differ & first positive then first is less else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first < second) : (first >= 0)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tltiInstruction = new TxInstruction("tlti", "i, s", "", "tlti $t1,-100",
            "Trap if Less Than Immediate: Trap if $t1 less than sign-extended 16-bit immediate",
            InstructionFormat32.I, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) < (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction tltiuInstruction = new TxInstruction("tltiu", "i, s", "", "tltiu $t1,-100",
            "Trap if Less Than Immediate Uunsigned: Trap if $t1 less than sign-extended 16-bit immediate, unsigned comparison",
            InstructionFormat32.I, null,
            Instruction.FlowType.INT, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int first = context.cpuState.getReg(statement.rs_fs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    // if signs same, do straight compare; if signs differ & first positive then first is less else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first < second) : (first >= 0)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lbInstruction = new TxInstruction("lb", "j, s(i)", "jw", "lb $t1,-100($t2)",
            "Load Byte: Set $t1 to signed 8-bit value from effective memory byte address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // offset is sign-extended and halfword value is loaded signed
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadSigned8(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lbuInstruction = new TxInstruction("lbu", "j, s(i)", "jw", "lbu $t1,-100($t2)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned8(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lhInstruction = new TxInstruction("lh", "j, s(i)", "jw", "lh $t1,-100($t2)",
            "Load Halfword: Set $t1 to signed 16-bit value from effective memory halfword address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // offset is sign-extended and halfword value is loaded signed
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadSigned16(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction lhuInstruction = new TxInstruction("lhu", "j, s(i)", "jw", "lhu $t1,-100($t2)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // offset is sign-extended and halfword value is loaded unsigned
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned16(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction sbInstruction = new TxInstruction("sb", "j, s(i)", "jw", "sb $t1,-100($t2)",
            "Store Byte: Store the low-order 8 bits of $t1 into the effective memory byte address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store8(
                            context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16),
                            context.cpuState.getReg(statement.rt_ft) & 0x000000ff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /* This is both for the 32-bit instruction and the EXTENDed 16-bit one. Both have a fixed 16-bit immediate value */
    public static final TxInstruction shInstruction = new TxInstruction("sh", "j, s(i)", "jw", "sh $t1,-100($t2)",
            "Store Halfword: Store the low-order 16 bits of $t1 into the effective memory halfword address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store16(
                            context.cpuState.getReg(statement.rs_fs) + (statement.imm << 16 >> 16),
                            context.cpuState.getReg(statement.rt_ft) & 0x0000ffff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction syncInstruction = new TxInstruction("sync", "", "", "sync",
            "SYNC: Wait for all operations to complete",
            InstructionFormat32.I, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    /* nop. Simulator does not have any pipeline */
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction waitInstruction = new TxInstruction("wait", "", "", "wait",
            "WAIT: put the processor in stand-by",
            InstructionFormat32.I, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (Format.bitValue(context.cpuState.getReg(TxCPUState.Status), TxCPUState.Status_RP_pos) == 1) {
                        ((TxCPUState)context.cpuState).setPowerMode(TxCPUState.PowerMode.DOZE);
                    }
                    else {
                        ((TxCPUState)context.cpuState).setPowerMode(TxCPUState.PowerMode.HALT);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction eretInstruction = new TxInstruction("eret", "", "", "eret",
            "Exception RETurn: Set Program Counter to Coprocessor 0 EPC register value, clear Coprocessor Status exception level bit",
            InstructionFormat32.R, null,
            Instruction.FlowType.RET, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // See architecture spec, section 6.1.3.6
                    TxCPUState txCPUState = (TxCPUState) context.cpuState;
                    if (txCPUState.isStatusERLSet()) {
                        txCPUState.setPc(txCPUState.getReg(TxCPUState.ErrorEPC));
                        txCPUState.clearStatusERL();
                    }
                    else {
                        context.cpuState.setPc(txCPUState.getReg(TxCPUState.EPC));
                        txCPUState.clearStatusEXL();
                    }
                    txCPUState.popSscrCssIfSwitchingEnabled();
                }
            });




    /* ***************************************************************************************************
     * 16-bit specific instructions, or instructions with a different behaviour from the 32-bit equivalent
     * *************************************************************************************************** */


    public static final TxInstruction ac0iuInstruction = new TxInstruction("ac0iu", "", "", "ac0iu",
            "Add Coprocessor 0 Immediate Unsigned",
            null, InstructionFormat16.RRR3,
            Instruction.FlowType.NONE , false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rt_ft) + statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /* non-EXTENDED : zero extended and multiplied by 4 */
    public static final TxInstruction addiufp16Instruction = new TxInstruction("addiu", "F, 4u", "" /* TODO action */, "addiu $fp, -100",
            "ADD Immediate Unsigned to FP",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // If not EXTENDed, "The 8-bit immediate is shifted left by two bits and sign-extended"
                    context.cpuState.setReg(TxCPUState.FP, context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 24 >> 22));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /* EXTENDed : sign-extended and not shifted */
    public static final TxInstruction addiufpInstruction = new TxInstruction("addiu", "F, s", "" /* TODO action */, "addiu fp, -100",
            "ADD Immediate Unsigned to FP",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // "When EXTENDed, the immediate operand is not shifted at all"
                    context.cpuState.setReg(TxCPUState.FP, context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 16 >> 16));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction addiu8Instruction = new TxInstruction("addiu", "i, s", "i+", "addiu $t1,-100",
            "ADDition Immediate 'Unsigned' without overflow: add signed 16-bit immediate to $t1, no overflow",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int shift = 32 - statement.immBitWidth;
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rs_fs) + (statement.imm << shift >> shift));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /* non-EXTENDED : zero extended and multiplied by 4 */
    public static final TxInstruction addiupc16Instruction = new TxInstruction("addiu", "i, P, 4ru", "iw", "addiu r3, ABCD0123",
            "ADD Immediate Unsigned with PC",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, getMaskedBasePc(statement, context) + (statement.imm << 2));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /* EXTENDed : sign-extended and not shifted */
    public static final TxInstruction addiupcInstruction = new TxInstruction("addiu", "i, P, rs", "iw", "addiu r3, ABCD0123",
            "ADD Immediate Unsigned with PC",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, getMaskedBasePc(statement, context) + (statement.imm << 16 >> 16));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /* non-EXTENDED : zero extended and multiplied by 4 */
    public static final TxInstruction addiusp16Instruction = new TxInstruction("addiu", "i, S, 4u", "iw", "addiu r3, sp, 16",
            "ADD Immediate Unsigned with SP",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // If not EXTENDed, "The 8-bit immediate is shifted left by two bits, zero-extended and added"
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 2));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /* EXTENDed : sign-extended and not shifted */
    public static final TxInstruction addiuspInstruction = new TxInstruction("addiu", "i, S, s", "iw", "addiu r3, sp, 16",
            "ADD Immediate Unsigned with SP",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // "When EXTENDed, the immediate operand is not shifted at all"
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 16));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction adjspInstruction = new TxInstruction("addiu", "S, 4s", "" /* TODO action */, "addiu sp, 16",
            "ADD Immediate Unsigned with SP",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (statement.immBitWidth == 8) {
                        // If not EXTENDed, "The 8-bit immediate is shifted left by three bits and sign-extended"
                        context.cpuState.setReg(TxCPUState.SP, context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 24 >> 21));
                    }
                    else {
                        // "When EXTENDed, the immediate operand is not shifted at all"
                        context.cpuState.setReg(TxCPUState.SP, context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 16));
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction bInstruction = new TxInstruction("b", "2rs", "", "b 100",
            "unconditional Branch: branch to target address",
            null, InstructionFormat16.I,
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int shift = 32 - statement.immBitWidth;
                    context.cpuState.pc += statement.getNumBytes() + (statement.imm << shift >> (shift-1)); // sign extend and x2
                }
            });

    public static final TxInstruction bal16Instruction = new TxInstruction("bal", "2rs", "", "bal 100",
            "unconditional Branch And Link: branch to target address",
            null, InstructionFormat16.RI,
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(TxCPUState.RA, context.cpuState.getPc() /* incl ISA 16 LSB*/ + statement.getNumBytes());
                    int shift = 32 - statement.immBitWidth;
                    context.cpuState.pc += statement.getNumBytes() + (statement.imm << shift >> (shift-1)); // sign extend and x2
                }
            });

    public static final TxInstruction bclrInstruction = new TxInstruction("bclr", "u(i), l", "", "bclr 4(sp), 7",
            "Bit CLeaR: clear given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = 0;
                    switch (statement.rs_fs) {
                        case 0b00: address = statement.imm << 18 >> 18; break; // sign extend for R0 : shift = 32 - statement.immBitWidth = 32 - 14;
                        case 0b01: address = statement.imm + context.cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address = statement.imm + context.cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address = statement.imm + context.cpuState.getReg(TxCPUState.FP); break;
                    }
                    context.memory.store8(address, context.memory.loadUnsigned8(address) & (~(1 << statement.sa_cc)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction bclrfpInstruction = new TxInstruction("bclr", "u(F), l", "", "bclr 4(fp), 7",
            "Bit CLeaR: clear given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = statement.imm + context.cpuState.getReg(TxCPUState.FP);
                    context.memory.store8(address, context.memory.loadUnsigned8(address) & (~(1 << statement.sa_cc)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction beqz16Instruction = new TxInstruction("beqz", "i, 2rs", "", "beqz $t1,label",
            "Branch if EQual Zero: Branch to statement at label's address if $t1 is zero",
            null, InstructionFormat16.RI,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) == 0) {
                        int shift = 32 - statement.immBitWidth;
                        context.cpuState.pc += statement.getNumBytes() + (statement.imm << shift >> (shift-1)); // sign extend and x2
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes(); // normal flow
                    }
                }
            });

    public static final TxInstruction bfinsInstruction = new TxInstruction("bfins", "j, i, l, d", "", "bfins $t1, $t2, 4, 2",
            "Bit Field INSert: copy a bit field from register $t2 to register $t1",
            null, InstructionFormat16.RR_BS1F_BFINS,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int bit1 = statement.sa_cc;
                    int bit2 = statement.imm;
                    // Create a mask such as 00..001110000 for 6:4 by
                    // 1. Creating a fullly set bitmap (-1) :         11..11
                    // 2. Shifting it left by (6-4+1 = 3)   :      11..11000
                    // 3. Negating it                       :      00..00111
                    // 4. Shifting it left by 4             :  00..001110000
                    int mask = (~((-1) << (bit2-bit1+1))) << bit1;
                    context.cpuState.setReg(statement.rt_ft,
                            ( context.cpuState.getReg(statement.rt_ft)          & ~mask)
                                    | ((context.cpuState.getReg(statement.rs_fs) << bit1 )&  mask));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction bs1fInstruction = new TxInstruction("bs1f", "j, i", "", "bs1f $t1, $t2",
            "Bit Search 1 Forward: set $t2 to the position of the first 1 in register $t1",
            null, InstructionFormat16.RR_BS1F_BFINS,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int rx = context.cpuState.getReg(statement.rs_fs);
                    if (rx == 0) {
                        context.cpuState.setReg(statement.rt_ft, 0);
                    }
                    else {
                        for(int i = 0; i < 32; i++) {
                            if (((rx >> i) & 0b1) == 1) {
                                context.cpuState.setReg(statement.rt_ft, i + 1);
                                break;
                            }
                        }
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction bextInstruction = new TxInstruction("bext", "u(i), l", "", "bext 4(sp), 7",
            "Bit EXTract: extract given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = 0;
                    switch (statement.rs_fs) {
                        case 0b00: address = statement.imm << 18 >> 18; break; // sign extend for R0 : shift = 32 - statement.immBitWidth = 32 - 14;
                        case 0b01: address = statement.imm + context.cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address = statement.imm + context.cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address = statement.imm + context.cpuState.getReg(TxCPUState.FP); break;
                    }
                    context.cpuState.setReg(TxCPUState.T8, ((context.memory.loadUnsigned8(address) & (1 << statement.sa_cc)) == 0)?0:1);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction bextfpInstruction = new TxInstruction("bext", "u(F), l", "", "bext 4(sp), 7",
            "Bit EXTract: extract given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = statement.imm + context.cpuState.getReg(TxCPUState.FP);
                    context.cpuState.setReg(TxCPUState.T8, ((context.memory.loadUnsigned8(address) & (1 << statement.sa_cc)) == 0)?0:1);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction binsInstruction = new TxInstruction("bins", "u(i), l", "", "bins 4(sp), 7",
            "Bit INSert: insert given bit at memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = 0;
                    switch (statement.rs_fs) {
                        case 0b00: address = statement.imm << 18 >> 18; break; // sign extend for R0 : shift = 32 - statement.immBitWidth = 32 - 14;
                        case 0b01: address = statement.imm + context.cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address = statement.imm + context.cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address = statement.imm + context.cpuState.getReg(TxCPUState.FP); break;
                    }
                    if ((context.cpuState.getReg(TxCPUState.T8) & 1) == 0) {
                        // Clear bit
                        context.memory.store8(address, context.memory.loadUnsigned8(address) & (~(1 << statement.sa_cc)));
                    }
                    else {
                        // set bit
                        context.memory.store8(address, context.memory.loadUnsigned8(address) | (~(1 << statement.sa_cc)));
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction binsfpInstruction = new TxInstruction("bins", "u(F), l", "", "bins 4(sp), 7",
            "Bit INSert: insert given bit at memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = statement.imm + context.cpuState.getReg(TxCPUState.FP);
                    if ((context.cpuState.getReg(TxCPUState.T8) & 1) == 0) {
                        // Clear bit
                        context.memory.store8(address, context.memory.loadUnsigned8(address) & (~(1 << statement.sa_cc)));
                    }
                    else {
                        // set bit
                        context.memory.store8(address, context.memory.loadUnsigned8(address) | (~(1 << statement.sa_cc)));
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction bnez16Instruction = new TxInstruction("bnez", "i, 2rs", "", "bnez $t1,label",
            "Branch if Not Equal Zero: Branch to statement at label's address if $t1 is not zero",
            null, InstructionFormat16.RI,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rs_fs) != 0) {
                        int shift = 32 - statement.immBitWidth;
                        context.cpuState.pc += statement.getNumBytes() + (statement.imm << shift >> (shift-1)); // sign extend and x2
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes(); // normal flow
                    }
                }
            });

    public static final TxInstruction bsetInstruction = new TxInstruction("bset", "u(i), l", "", "bset 4(sp), 7",
            "Bit SET: set given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = 0;
                    switch (statement.rs_fs) {
                        case 0b00: address = statement.imm << 18 >> 18; break; // sign extend for R0 : shift = 32 - statement.immBitWidth = 32 - 14;
                        case 0b01: address = statement.imm + context.cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address = statement.imm + context.cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address = statement.imm + context.cpuState.getReg(TxCPUState.FP); break;
                    }
                    context.memory.store8(address, context.memory.loadUnsigned8(address) | (1 << statement.sa_cc));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction bsetfpInstruction = new TxInstruction("bset", "u(F), l", "", "bset 4(sp), 7",
            "Bit SET: set given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = statement.imm + context.cpuState.getReg(TxCPUState.FP);
                    context.memory.store8(address, context.memory.loadUnsigned8(address) | (1 << statement.sa_cc));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction bteqzInstruction = new TxInstruction("bteqz", "2rs", "", "bteqz label",
            "Branch if T8 EQual Zero: Branch to statement at label's address if $t1 is not zero",
            null, InstructionFormat16.RI,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(TxCPUState.T8) == 0) {
                        int shift = 32 - statement.immBitWidth;
                        context.cpuState.pc += statement.getNumBytes() + (statement.imm << shift >> (shift-1)); // sign extend and x2
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes(); // normal flow
                    }
                }
            });

    public static final TxInstruction btnezInstruction = new TxInstruction("btnez", "2rs", "", "btnez label",
            "Branch if T8 Not Equal Zero: Branch to statement at label's address if $t1 is not zero",
            null, InstructionFormat16.RI,
            Instruction.FlowType.BRA, true, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(TxCPUState.T8) != 0) {
                        int shift = 32 - statement.immBitWidth;
                        context.cpuState.pc += statement.getNumBytes() + (statement.imm << shift >> (shift-1)); // sign extend and x2
                    }
                    else {
                        context.cpuState.pc += statement.getNumBytes(); // normal flow
                    }
                }
            });

    public static final TxInstruction btstInstruction = new TxInstruction("btst", "u(i), l", "", "btst 4(sp), 7",
            "Bit TeST: extract given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = 0;
                    switch (statement.rs_fs) {
                        case 0b00: address = statement.imm << 18 >> 18; break; // sign extend for R0 : shift = 32 - statement.immBitWidth = 32 - 14;
                        case 0b01: address = statement.imm + context.cpuState.getReg(TxCPUState.GP); break;
                        case 0b10: address = statement.imm + context.cpuState.getReg(TxCPUState.SP); break;
                        case 0b11: address = statement.imm + context.cpuState.getReg(TxCPUState.FP); break;
                    }
                    context.cpuState.setReg(TxCPUState.T8, ((context.memory.loadUnsigned8(address) & (1 << statement.sa_cc)) == 0)?1:0); // !bext
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction btstfpInstruction = new TxInstruction("btst", "u(F), l", "", "btst 4(sp), 7",
            "Bit TeST: extract given bit from memory address",
            null, InstructionFormat16.SPC_BIT,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int address = statement.imm + context.cpuState.getReg(TxCPUState.FP);
                    context.cpuState.setReg(TxCPUState.T8, ((context.memory.loadUnsigned8(address) & (1 << statement.sa_cc)) == 0)?1:0); // !bext
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction cmpInstruction = new TxInstruction("cmp", "i, j", "", "cmp $t1, $t2",
            "CoMPare: set t8 to 0 if registers are equal",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(TxCPUState.T8, context.cpuState.getReg(statement.rs_fs) ^ context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction cmpiInstruction = new TxInstruction("cmpi", "i, u", "", "cmpi $t1, 15",
            "CoMPare Immediate: set t8 to 0 if register equals given value",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(TxCPUState.T8, context.cpuState.getReg(statement.rs_fs) ^ statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction diInstruction = new TxInstruction("di", "", "", "di",
            "Disable Interrupt: clears the IE bit of the status register",
            null, InstructionFormat16.I,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    ((TxCPUState)context.cpuState).clearStatusIE();
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction diveInstruction = new TxInstruction("dive", "i, j", "iw", "dive $t1,$t2",
            "DIVision with Exception: Divide $t1 by $t2 then set LO to quotient and HI to remainder",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rt_ft) == 0) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.DIVIDE_BY_ZERO_EXCEPTION);
                    }
                    context.cpuState.setReg(TxCPUState.HI, context.cpuState.getReg(statement.rs_fs) % context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.setReg(TxCPUState.LO, context.cpuState.getReg(statement.rs_fs) / context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction diveuInstruction = new TxInstruction("divu", "i, j", "iw", "divu $t1,$t2",
            "DIVision with Exception Unsigned: Divide unsigned $t1 by $t2 then set LO to quotient and HI to remainder",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    if (context.cpuState.getReg(statement.rt_ft) == 0) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.DIVIDE_BY_ZERO_EXCEPTION);
                    }
                    long oper1 = ((long) context.cpuState.getReg(statement.rs_fs)) << 32 >>> 32;
                    long oper2 = ((long) context.cpuState.getReg(statement.rt_ft)) << 32 >>> 32;
                    context.cpuState.setReg(TxCPUState.HI, (int) (((oper1 % oper2) << 32) >> 32));
                    context.cpuState.setReg(TxCPUState.LO, (int) (((oper1 / oper2) << 32) >> 32));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction eiInstruction = new TxInstruction("ei", "", "", "ei",
            "Enable Interrupt: sets the IE bit of the status register",
            null, InstructionFormat16.I,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    ((TxCPUState)context.cpuState).setStatusIE();
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction jalr16Instruction = new TxInstruction("jalr", "i;Iu", "", "jalr $t2",
            "Jump And Link Register: Set $ra to Program Counter (return address) then jump to statement whose address is in $t2",
            null, InstructionFormat16.RI,
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.setDelayedPcAndRa(
                            context.cpuState.getReg(statement.rs_fs),
                            context.cpuState.getPc() /* incl ISA 16 LSB*/ + 4 /* only exists in EXTENDed form */
                    );
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction jalrcInstruction = new TxInstruction("jalrc", "i;Iu", "", "jalrc $t2",
            "Jump And Link Register Compact: Set $ra to Program Counter (return address) then jump to statement whose address is in $t2",
            null, InstructionFormat16.RI,
            Instruction.FlowType.CALL, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(TxCPUState.RA, context.cpuState.getPc() /* incl ISA 16 LSB*/ + 2 /* no EXTENDed form exists */);
                    context.cpuState.setPc(context.cpuState.getReg(statement.rs_fs));
                }
            });

    public static final TxInstruction jrraInstruction = new TxInstruction("jr", "A", "", "jr $ra",
            "Jump Register RA unconditionally: Jump to statement whose address is in $ra",
            null, InstructionFormat16.RI,
            Instruction.FlowType.RET, false, Instruction.DelaySlotType.NORMAL,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.setDelayedPc(
                            context.cpuState.getReg(TxCPUState.RA)
                    );
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction jrcraInstruction = new TxInstruction("jrc", "A", "", "jrc $ra",
            "Jump Register RA unconditionally Compact: Jump to statement whose address is in $ra",
            null, InstructionFormat16.RI,
            Instruction.FlowType.RET, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setPc(context.cpuState.getReg(TxCPUState.RA));
                }
            });

    public static final TxInstruction jrcInstruction = new TxInstruction("jrc", "i;Iu", "", "jrc $t2",
            "Jump Register unconditionally Compact: Jump to statement whose address is in $t2",
            null, InstructionFormat16.RI,
            Instruction.FlowType.JMP, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setPc(context.cpuState.getReg(statement.rs_fs));
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of lbInstruction: does not sign-extend offset
     */
    public static final TxInstruction lb16Instruction = new TxInstruction("lb", "j, u(i)", "jw", "lb $t1,-100($t2)",
            "Load Byte: Set $t1 to signed 8-bit value from effective memory byte address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadSigned8(context.cpuState.getReg(statement.rs_fs) + statement.imm));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of lbuInstruction: does not sign-extend offset
     */
    public static final TxInstruction lbu16Instruction = new TxInstruction("lbu", "j, u(i)", "jw", "lbu $t1,-100($t2)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned8(context.cpuState.getReg(statement.rs_fs) + statement.imm));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of lbufpInstruction: sign-extends offset
     */
    public static final TxInstruction lbufpInstruction = new TxInstruction("lbu", "j, s(F)", "jw", "lbu $t1,-100($fp)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.FPB_SPB,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned8(context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lbufpInstruction: does not sign-extend offset
     */
    public static final TxInstruction lbufp16Instruction = new TxInstruction("lbu", "j, u(F)", "jw", "lbu $t1,-100($fp)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.FPB_SPB,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned8(context.cpuState.getReg(TxCPUState.FP) + statement.imm));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    /**
     * EXTENDed 16-bit ISA version of lbuspInstruction: sign-extends offset
     */
    public static final TxInstruction lbuspInstruction = new TxInstruction("lbu", "j, s(S)", "jw", "lbu $t1,-100($sp)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.FPB_SPB,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned8(context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lbuspInstruction: does not sign-extend offset
     */
    public static final TxInstruction lbusp16Instruction = new TxInstruction("lbu", "j, u(S)", "jw", "lbu $t1,-100($sp)",
            "Load Byte Unsigned: Set $t1 to unsigned 8-bit value from effective memory byte address",
            null, InstructionFormat16.FPB_SPB,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned8(context.cpuState.getReg(TxCPUState.SP) + statement.imm));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    /**
     * non-EXTENDed 16-bit ISA version of lhInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction lh16Instruction = new TxInstruction("lh", "j, 2u(i)", "jw", "lh $t1,-100($t2)",
            "Load Halfword: Set $t1 to signed 16-bit value from effective memory halfword address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadSigned16(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 1)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of lhuInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction lhu16Instruction = new TxInstruction("lhu", "j, 2u(i)", "jw", "lhu $t1,-100($t2)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned16(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 1)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of lhufpInstruction: sign-extends offset and multiplies it by 2
     */
    public static final TxInstruction lhufpInstruction = new TxInstruction("lhu", "j, 2s(F)", "jw", "lhu $t1,-100($fp)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.FPH_SPH,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned16(context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 16 >> 15)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lhufpInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction lhufp16Instruction = new TxInstruction("lhu", "j, 2u(F)", "jw", "lhu $t1,-100($fp)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.FPH_SPH,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned16(context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 1)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    /**
     * EXTENDed 16-bit ISA version of lhuspInstruction: sign-extends offset and multiplies it by 2
     */
    public static final TxInstruction lhuspInstruction = new TxInstruction("lhu", "j, 2s(S)", "jw", "lhu $t1,-100($sp)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.FPH_SPH,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned16(context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 15)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lhuspInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction lhusp16Instruction = new TxInstruction("lhu", "j, 2u(S)", "jw", "lhu $t1,-100($sp)",
            "Load Halfword Unsigned: Set $t1 to unsigned 16-bit value from effective memory halfword address",
            null, InstructionFormat16.FPH_SPH,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.loadUnsigned16(context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 1)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

/* lhu > lw */

    /**
     * non-EXTENDed 16-bit ISA version of lwInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction lw16Instruction = new TxInstruction("lw", "j, 4u(i)", "jw", "lw $t1,-100($t2)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.load32(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 2)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of lwfpInstruction: sign-extends offset
     */
    public static final TxInstruction lwfpInstruction = new TxInstruction("lw", "j, s(F)", "jw", "lw $t1,-100($fp)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.load32(context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lwfpInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction lwfp16Instruction = new TxInstruction("lw", "j, 4u(F)", "jw", "lw $t1,-100($fp)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.load32(context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 2)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    /**
     * EXTENDed 16-bit ISA version of lwpcInstruction: sign-extends offset
     */
    public static final TxInstruction lwpcInstruction = new TxInstruction("lw", "j, s(P)", "jw", "lw $t1,-100($pc)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.load32(getMaskedBasePc(statement, context) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lwpcInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction lwpc16Instruction = new TxInstruction("lw", "j, 4u(P)", "jw", "lw $t1,-100($pc)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.load32(getMaskedBasePc(statement, context) + (statement.imm << 2)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of lwspInstruction: sign-extends offset
     */
    public static final TxInstruction lwspInstruction = new TxInstruction("lw", "j, s(S)", "jw", "lw $t1,-100($sp)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.load32(context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 16)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /**
     * non-EXTENDed 16-bit ISA version of lwspInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction lwsp16Instruction = new TxInstruction("lw", "j, 4u(S)", "jw", "lw $t1,-100($sp)",
            "Load Word: Set $t1 to 32-bit value from effective memory word address",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.memory.load32(context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 2)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction liInstruction = new TxInstruction("li", "j, u", "jv", "li $t1,100",
            "Load Immediate: Set high-order 16 bits of $t1 to 0 and low-order 16 bits to 16-bit immediate",
            InstructionFormat32.I, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    // 16-bit version of maddInstruction, with no copy of LO to rd
    public static final TxInstruction madd16Instruction = new TxInstruction("madd", "i, j", "", "madd $t1,$t2",
            "Multiply ADD: Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (long) context.cpuState.getReg(statement.rs_fs) * (long) context.cpuState.getReg(statement.rt_ft);
                    long contentsHiLo = Format.twoIntsToLong(context.cpuState.getReg(TxCPUState.HI), context.cpuState.getReg(TxCPUState.LO));
                    long sum = contentsHiLo + product;
                    context.cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(sum));
                    int lo = Format.lowOrderLongToInt(sum);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    // 16-bit version of madduInstruction, with no copy of LO to rd
    public static final TxInstruction maddu16Instruction = new TxInstruction("maddu", "i, j", "", "maddu $t1,$t2",
            "Multiply ADD Unsigned: Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product, unsigned",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (((long) context.cpuState.getReg(statement.rs_fs)) << 32 >>> 32)
                            * (((long) context.cpuState.getReg(statement.rt_ft)) << 32 >>> 32);
                    long contentsHiLo = Format.twoIntsToLong(context.cpuState.getReg(TxCPUState.HI), context.cpuState.getReg(TxCPUState.LO));
                    long sum = contentsHiLo + product;
                    context.cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(sum));
                    int lo = Format.lowOrderLongToInt(sum);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction movefpInstruction = new TxInstruction("move", "F, i", "kw", "move $t1,$t2",
            "MOVE: set $t1 to FP, no overflow",
            null, InstructionFormat16.I8MOVFP,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(TxCPUState.FP, context.cpuState.getReg(statement.rs_fs));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction moveR32Instruction = new TxInstruction("move", "k, i", "kw", "move $t1,$t2",
            "MOVE: set $t1 to $t2, no overflow",
            null, InstructionFormat16.I8MOVR32,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction move32RInstruction = new TxInstruction("move", "k, i", "kw", "move $t1,$t2",
            "MOVE: set $t1 to $t2, no overflow",
            null, InstructionFormat16.I8MOV32R,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, context.cpuState.getReg(statement.rs_fs));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /** 16-bit "short" version only affects HI and LO */
    public static final TxInstruction mult16Instruction = new TxInstruction("mult", "i, j", "", "mult $t1,$t2",
            "MULTiplication: Set HI to high-order 32 bits, LO to low-order 32 bits of the product of $t1 and $t2",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (long) context.cpuState.getReg(statement.rs_fs) * (long) context.cpuState.getReg(statement.rt_ft);
                    context.cpuState.setReg(TxCPUState.HI, (int) (product >> 32));
                    int lo = (int) ((product << 32) >> 32);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /** 16-bit "short" version only affects HI and LO */
    public static final TxInstruction multu16Instruction = new TxInstruction("multu", "i, j", "", "multu $t1,$t2",
            "MULTiplication Unsigned: Set HI to high-order 32 bits, LO to low-order 32 bits of the product of unsigned $t1 and $t2",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    long product = (((long) context.cpuState.getReg(statement.rs_fs)) << 32 >>> 32)
                            * (((long) context.cpuState.getReg(statement.rt_ft)) << 32 >>> 32);
                    context.cpuState.setReg(TxCPUState.HI, (int) (product >> 32));
                    int lo = (int) ((product << 32) >> 32);
                    context.cpuState.setReg(TxCPUState.LO, lo);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction negInstruction = new TxInstruction("neg", "i, j", "iw", "neg $t1,$t2",
            "NEGate: Set $t1 to -$t2",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rs_fs, 0 - context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction notInstruction = new TxInstruction("not", "i, j", "iw", "not $t1,$t2",
            "NOT: Set $t1 to $t2 NOR 0x00000000",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rs_fs, ~context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction saddInstruction = new TxInstruction("sadd", "k, [i, ]j", "kw", "sadd $t1,$t2,$t3",
            "Saturated ADDition: set $t2 to ($t1 plus $t2), or max/min integer values if overflow occurs",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int add1 = context.cpuState.getReg(statement.rs_fs);
                    int add2 = context.cpuState.getReg(statement.rt_ft);
                    int sum = add1 + add2;
                    // overflow on A+B detected when A and B have same sign and A+B has other sign.
                    if (add1 >= 0 && add2 >= 0 && sum < 0) {
                        sum = Integer.MAX_VALUE;
                    } else if (add1 < 0 && add2 < 0 && sum >= 0) {
                        sum = Integer.MIN_VALUE;
                    }
                    context.cpuState.setReg(statement.rt_ft, sum);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    public static final TxInstruction restoreInstruction = new TxInstruction("restore", "z", "", "restore $s0, 0x8",
            "RESTORE registers and deallocate stack frame: restore given registers on the stack and adjust sp according to given value",
            null, InstructionFormat16.I8SVRS,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int aregs = (statement.getBinaryStatement() >> 16) & 0b1111; // = 0 in 16-bit form
                    int xsregs = (statement.getBinaryStatement() >> 20) & 0b111; // = 0 in 16-bit form

                    int temp;
                    if (!statement.isExtended() && statement.imm == 0) {
                        temp = context.cpuState.getReg(TxCPUState.SP) + 128;
                    }
                    else {
                        temp = context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 3);
                    }

                    int temp2 = temp;
                    if ((statement.sa_cc & 0b100) != 0) { // RA
                        temp -= 4;
                        context.cpuState.setReg(TxCPUState.RA, context.memory.load32(temp));
                    }

                    if (xsregs > 0) {
                        if (xsregs > 1) {
                            if (xsregs > 2) {
                                if (xsregs > 3) {
                                    if (xsregs > 4) {
                                        if (xsregs > 5) {
                                            if (xsregs > 6) {
                                                temp -= 4;
                                                context.cpuState.setReg(30, context.memory.load32(temp));
                                            }
                                            temp -= 4;
                                            context.cpuState.setReg(23, context.memory.load32(temp));
                                        }
                                        temp -= 4;
                                        context.cpuState.setReg(22, context.memory.load32(temp));
                                    }
                                    temp -= 4;
                                    context.cpuState.setReg(21, context.memory.load32(temp));
                                }
                                temp -= 4;
                                context.cpuState.setReg(20, context.memory.load32(temp));
                            }
                            temp -= 4;
                            context.cpuState.setReg(19, context.memory.load32(temp));
                        }
                        temp -= 4;
                        context.cpuState.setReg(18, context.memory.load32(temp));
                    }

                    if ((statement.sa_cc & 0b001) != 0) { // S1
                        temp -= 4;
                        context.cpuState.setReg(TxCPUState.S1, context.memory.load32(temp));
                    }
                    if ((statement.sa_cc & 0b010) != 0) { // S0
                        temp -= 4;
                        context.cpuState.setReg(TxCPUState.S0, context.memory.load32(temp));
                    }

                    if (aregs > 0) {
                        int astatic = 0;
                        switch (aregs) {
                            case 0b0001:
                            case 0b0101:
                            case 0b1001:
                            case 0b1101:
                                astatic = 1;
                                break;
                            case 0b0010:
                            case 0b0110:
                            case 0b1010:
                                astatic = 2;
                                break;
                            case 0b0011:
                            case 0b0111:
                                astatic = 3;
                                break;
                            case 0b1011:
                                astatic = 4;
                                break;
                        }
                        if (astatic > 0) {
                            temp -= 4;
                            context.cpuState.setReg(7, context.memory.load32(temp));
                            if (astatic > 0) {
                                temp -= 4;
                                context.cpuState.setReg(6, context.memory.load32(temp));
                                if (astatic > 0) {
                                    temp -= 4;
                                    context.cpuState.setReg(5, context.memory.load32(temp));
                                    if (astatic > 0) {
                                        temp -= 4;
                                        context.cpuState.setReg(4, context.memory.load32(temp));
                                    }
                                }
                            }
                        }
                    }

                    context.cpuState.setReg(TxCPUState.SP, temp2);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    public static final TxInstruction saveInstruction = new TxInstruction("save", "z", "", "save $s0, 0x8",
            "SAVE registers and set up stack frame: save given registers on the stack and adjust sp according to given value",
            null, InstructionFormat16.I8SVRS,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int aregs = (statement.getBinaryStatement() >> 16) & 0b1111; // = 0 in 16-bit form
                    int xsregs = (statement.getBinaryStatement() >> 20) & 0b111; // = 0 in 16-bit form

                    int temp = context.cpuState.getReg(TxCPUState.SP);

                    if (aregs > 0) {
                        int args = 0;
                        switch (aregs) {
                            case 0b0100:
                            case 0b0101:
                            case 0b0110:
                            case 0b0111:
                                args = 1;
                                break;
                            case 0b1000:
                            case 0b1001:
                            case 0b1010:
                                args = 2;
                                break;
                            case 0b1100:
                            case 0b1101:
                                args = 3;
                                break;
                            case 0b1110:
                                args = 4;
                                break;
                        }
                        if (args > 0) {
                            context.memory.store32(temp, context.cpuState.getReg(4));
                            if (args > 1) {
                                context.memory.store32(temp + 4, context.cpuState.getReg(5));
                                if (args > 2) {
                                    context.memory.store32(temp + 8, context.cpuState.getReg(6));
                                    if (args > 3) {
                                        context.memory.store32(temp + 12, context.cpuState.getReg(7));
                                    }
                                }
                            }
                        }
                    }

                    if ((statement.sa_cc & 0b100) != 0) { // RA
                        temp -= 4;
                        context.memory.store32(temp, context.cpuState.getReg(TxCPUState.RA));
                    }

                    if (xsregs > 0) {
                        if (xsregs > 1) {
                            if (xsregs > 2) {
                                if (xsregs > 3) {
                                    if (xsregs > 4) {
                                        if (xsregs > 5) {
                                            if (xsregs > 6) {
                                                temp -= 4;
                                                context.memory.store32(temp, context.cpuState.getReg(30));
                                            }
                                            temp -= 4;
                                            context.memory.store32(temp, context.cpuState.getReg(23));
                                        }
                                        temp -= 4;
                                        context.memory.store32(temp, context.cpuState.getReg(22));
                                    }
                                    temp -= 4;
                                    context.memory.store32(temp, context.cpuState.getReg(21));
                                }
                                temp -= 4;
                                context.memory.store32(temp, context.cpuState.getReg(20));
                            }
                            temp -= 4;
                            context.memory.store32(temp, context.cpuState.getReg(19));
                        }
                        temp -= 4;
                        context.memory.store32(temp, context.cpuState.getReg(18));
                    }

                    if ((statement.sa_cc & 0b001) != 0) { // S1
                        temp -= 4;
                        context.memory.store32(temp, context.cpuState.getReg(TxCPUState.S1));
                    }
                    if ((statement.sa_cc & 0b010) != 0) { // S0
                        temp -= 4;
                        context.memory.store32(temp, context.cpuState.getReg(TxCPUState.S0));
                    }

                    if (aregs > 0) {
                        int astatic = 0;
                        switch (aregs) {
                            case 0b0001:
                            case 0b0101:
                            case 0b1001:
                            case 0b1101:
                                astatic = 1;
                                break;
                            case 0b0010:
                            case 0b0110:
                            case 0b1010:
                                astatic = 2;
                                break;
                            case 0b0011:
                            case 0b0111:
                                astatic = 3;
                                break;
                            case 0b1011:
                                astatic = 4;
                                break;
                        }
                        if (astatic > 0) {
                            temp -= 4;
                            context.memory.store32(temp, context.cpuState.getReg(7));
                            if (astatic > 1) {
                                temp -= 4;
                                context.memory.store32(temp, context.cpuState.getReg(6));
                                if (astatic > 2) {
                                    temp -= 4;
                                    context.memory.store32(temp, context.cpuState.getReg(5));
                                    if (astatic > 3) {
                                        temp -= 4;
                                        context.memory.store32(temp, context.cpuState.getReg(4));
                                    }
                                }
                            }
                        }
                    }

                    if (!statement.isExtended() && statement.imm == 0) {
                        temp = context.cpuState.getReg(TxCPUState.SP) - 128;
                    }
                    else {
                        temp = context.cpuState.getReg(TxCPUState.SP) - (statement.imm << 3);
                    }
                    context.cpuState.setReg(TxCPUState.SP, temp);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });



    /**
     * non-EXTENDed 16-bit ISA version of sbInstruction: does not sign-extend offset
     */
    public static final TxInstruction sb16Instruction = new TxInstruction("sb", "j, s(i)", "", "sb $t1,-100($t2)",
            "Store Byte: Store the low-order 8 bits of $t1 into the effective memory byte address",
            InstructionFormat32.I, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store8(
                            context.cpuState.getReg(statement.rs_fs) + statement.imm,
                            context.cpuState.getReg(statement.rt_ft) & 0x000000ff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of sbfpInstruction: sign-extends offset
     */
    public static final TxInstruction sbfpInstruction = new TxInstruction("sb", "j, s(F)", "", "sb $t1,-100($fp)",
            "Store Byte: Store the low-order 8 bits of $t1 into the effective memory byte address",
            null, InstructionFormat16.FPB_SPB,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store8(
                            context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 16 >> 16),
                            context.cpuState.getReg(statement.rt_ft) & 0x000000ff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of sbfpInstruction: does not sign-extend offset
     */
    public static final TxInstruction sbfp16Instruction = new TxInstruction("sb", "j, s(F)", "", "sb $t1,-100($fp)",
            "Store Byte: Store the low-order 8 bits of $t1 into the effective memory byte address",
            null, InstructionFormat16.FPB_SPB,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store8(
                            context.cpuState.getReg(TxCPUState.FP) + statement.imm,
                            context.cpuState.getReg(statement.rt_ft) & 0x000000ff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of sbspInstruction: sign-extends offset
     */
    public static final TxInstruction sbspInstruction = new TxInstruction("sb", "j, s(S)", "", "sb $t1,-100($sp)",
            "Store Byte: Store the low-order 8 bits of $t1 into the effective memory byte address",
            null, InstructionFormat16.FPB_SPB,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store8(
                            context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 16),
                            context.cpuState.getReg(statement.rt_ft) & 0x000000ff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of sbspInstruction: does not sign-extend offset
     */
    public static final TxInstruction sbsp16Instruction = new TxInstruction("sb", "j, s(S)", "", "sb $t1,-100($sp)",
            "Store Byte: Store the low-order 8 bits of $t1 into the effective memory byte address",
            null, InstructionFormat16.FPB_SPB,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store8(
                            context.cpuState.getReg(TxCPUState.SP) + statement.imm,
                            context.cpuState.getReg(statement.rt_ft) & 0x000000ff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    /**
     * non-EXTENDed 16-bit ISA version of shInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction sh16Instruction = new TxInstruction("sh", "j, 2s(i)", "", "sh $t1,-100($t2)",
            "Store Halfword: Store the low-order 16 bits of $t1 into the effective memory halfword address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store16(
                            context.cpuState.getReg(statement.rs_fs) + (statement.imm << 1),
                            context.cpuState.getReg(statement.rt_ft) & 0x0000ffff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of shfpInstruction: sign-extends offset and multiplies it by 2
     */
    public static final TxInstruction shfpInstruction = new TxInstruction("sh", "j, 2s(F)", "", "sh $t1,-100($fp)",
            "Store Halfword: Store the low-order 16 bits of $t1 into the effective memory halfword address",
            null, InstructionFormat16.FPH_SPH,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store16(
                            context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 16 >> 15),
                            context.cpuState.getReg(statement.rt_ft) & 0x0000ffff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of shfpInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction shfp16Instruction = new TxInstruction("sh", "j, 2s(F)", "", "sh $t1,-100($fp)",
            "Store Halfword: Store the low-order 16 bits of $t1 into the effective memory halfword address",
            null, InstructionFormat16.FPH_SPH,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store16(
                            context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 1),
                            context.cpuState.getReg(statement.rt_ft) & 0x0000ffff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of shspInstruction: sign-extends offset and multiplies it by 2
     */
    public static final TxInstruction shspInstruction = new TxInstruction("sh", "j, 2s(S)", "", "sh $t1,-100($sp)",
            "Store Halfword: Store the low-order 16 bits of $t1 into the effective memory halfword address",
            null, InstructionFormat16.FPH_SPH,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store16(
                            context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 15),
                            context.cpuState.getReg(statement.rt_ft) & 0x0000ffff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of shspInstruction: does not sign-extend offset but multiplies it by 2
     */
    public static final TxInstruction shsp16Instruction = new TxInstruction("sh", "j, 2s(S)", "", "sh $t1,-100($sp)",
            "Store Halfword: Store the low-order 16 bits of $t1 into the effective memory halfword address",
            null, InstructionFormat16.FPH_SPH,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store16(
                            context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 1),
                            context.cpuState.getReg(statement.rt_ft) & 0x0000ffff);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of swInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction sw16Instruction = new TxInstruction("sw", "j, 4u(i)", "", "sw $t1,-100($t2)",
            "Store Word: Store contents of $t1 into effective memory word address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store32(context.cpuState.getReg(statement.rs_fs) + (statement.imm << 2), context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    /**
     * non-EXTENDed 16-bit ISA version of swfpInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction swfp16Instruction = new TxInstruction("sw", "j, 4u(F)", "", "sw $t1,-100($fp)",
            "Store Word: Store contents of $t1 into effective memory word address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store32(context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 2), context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of swfpInstruction: sign-extends offset
     */
    public static final TxInstruction swfpInstruction = new TxInstruction("sw", "j, s(F)", "", "sw $t1,-100($fp)",
            "Store Word: Store contents of $t1 into effective memory word address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store32(context.cpuState.getReg(TxCPUState.FP) + (statement.imm << 16 >> 16), context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of swraspInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction swrasp16Instruction = new TxInstruction("sw", "A, 4u(S)", "", "sw $ra,100($sp)",
            "Store Word: Store contents of $ra into effective memory word address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store32(context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 2), context.cpuState.getReg(TxCPUState.RA));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of swraspInstruction: sign-extends offset
     */
    public static final TxInstruction swraspInstruction = new TxInstruction("sw", "A, s(S)", "", "sw $ra,-100($sp)",
            "Store Word: Store contents of $ra into effective memory word address",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store32(context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 16), context.cpuState.getReg(TxCPUState.RA));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * non-EXTENDed 16-bit ISA version of swspInstruction: does not sign-extend offset but multiplies it by 4
     */
    public static final TxInstruction swsp16Instruction = new TxInstruction("sw", "j, 4u(S)", "", "sw $t1,-100($sp)",
            "Store Word: Store contents of $t1 into effective memory word address",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store32(context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 2), context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /**
     * EXTENDed 16-bit ISA version of swspInstruction: sign-extends offset
     */
    public static final TxInstruction swspInstruction = new TxInstruction("sw", "j, s(S)", "", "sw $t1,-100($sp)",
            "Store Word: Store contents of $t1 into effective memory word address",
            null, InstructionFormat16.RRI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.memory.store32(context.cpuState.getReg(TxCPUState.SP) + (statement.imm << 16 >> 16), context.cpuState.getReg(statement.rt_ft));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });


    public static final TxInstruction sebInstruction = new TxInstruction("seb", "i", "iw", "seb $t1",
            "Sign-Extend Byte: sign-extend the lower 8 bits of $t1",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rs_fs, context.cpuState.getReg(statement.rs_fs) << 24 >> 24);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction sehInstruction = new TxInstruction("seh", "i", "iw", "seh $t1",
            "Sign-Extend Halfword: sign-extend the lower 16 bits of $t1",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rs_fs, context.cpuState.getReg(statement.rs_fs) << 16 >> 16);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction sll5Instruction = new TxInstruction("sll", "j, d", "jw", "sll $t2, 10",
            "Shift Left Logical: Shift $t2 left by number of bits specified by immediate",
            null, InstructionFormat16.RRR2,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rt_ft) << statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction sra5Instruction = new TxInstruction("sra", "j, d", "jw", "sra $t2, 10",
            "Shift Right Arithmetic: Shift $t2 right arithmetically by number of bits specified by immediate",
            null, InstructionFormat16.RRR2,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rt_ft) >> statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction srl5Instruction = new TxInstruction("srl", "j, d", "jw", "srl $t2, 10",
            "Shift Right Logical: Shift $t2 right by number of bits specified by immediate",
            null, InstructionFormat16.RRR2,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rt_ft, context.cpuState.getReg(statement.rt_ft) >>> statement.imm);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /** 16-bit version of sltInstruction uses t8 as fixed destination */
    public static final TxInstruction slt16Instruction = new TxInstruction("slt", "i, j", "", "slt $t1,$t2",
            "Set on Less Than: If $t1 is less than $t2, then set $t8 to 1 else set $t8 to 0",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(TxCPUState.T8, (context.cpuState.getReg(statement.rs_fs) < context.cpuState.getReg(statement.rt_ft)) ? 1 : 0);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /** 16-bit non-EXTENDed version of sltiInstruction uses t8 as fixed destination and zero-extends imm */
    public static final TxInstruction slti16Instruction = new TxInstruction("slti", "i, u", "", "slti $t1,-100",
            "Set on Less Than Immediate: If $t1 is less than zero-extended immediate, then set $t8 to 1 else set $t1 to 0",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // 16 bit immediate value in statement.imm is sign-extended
                    context.cpuState.setReg(TxCPUState.T8, (context.cpuState.getReg(statement.rs_fs) < statement.imm) ? 1 : 0);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /** 16-bit EXTENDed version of sltiInstruction uses t8 as fixed destination and sign-extends imm */
    public static final TxInstruction slti16eInstruction = new TxInstruction("slti", "i, s", "", "slti $t1,-100",
            "Set on Less Than Immediate: If $t1 is less than sign-extended 16-bit immediate, then set $t8 to 1 else set $t1 to 0",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    // 16 bit immediate value in statement.imm is sign-extended
                    context.cpuState.setReg(TxCPUState.T8, (context.cpuState.getReg(statement.rs_fs) < (statement.imm << 16 >> 16)) ? 1 : 0);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /** 16-bit non-EXTENDed version of sltiuInstruction uses t8 as fixed destination and zero-extends imm */
    public static final TxInstruction sltiu16Instruction = new TxInstruction("sltiu", "i, u", "", "sltiu $t1,-100",
            "Set on Less Than Immediate Unsigned: If $t1 is less than zero-extended 8-bit immediate using unsigned comparison, then set $t8 to 1 else set $t8 to 0",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int first = context.cpuState.getReg(statement.rs_fs);
                    // 8 bit immediate value in statement.imm is zero-extended
                    int second = statement.imm ;
                    if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                        context.cpuState.setReg(TxCPUState.T8, (first < second) ? 1 : 0);
                    } else {
                        context.cpuState.setReg(TxCPUState.T8, (first >= 0) ? 1 : 0);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    /** 16-bit EXTENDed version of sltiuInstruction uses t8 as fixed destination and sign-extends imm */
    public static final TxInstruction sltiu16eInstruction = new TxInstruction("sltiu", "i, s", "", "sltiu $t1,-100",
            "Set on Less Than Immediate Unsigned: If $t1 is less than sign-extended 16-bit immediate using unsigned comparison, then set $t8 to 1 else set $t8 to 0",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int first = context.cpuState.getReg(statement.rs_fs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                        context.cpuState.setReg(TxCPUState.T8, (first < second) ? 1 : 0);
                    } else {
                        context.cpuState.setReg(TxCPUState.T8, (first >= 0) ? 1 : 0);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    /** 16-bit EXTENDed version of sltiuInstruction uses t8 as fixed destination */
    public static final TxInstruction sltu16Instruction = new TxInstruction("sltu", "i, j", "", "sltu $t1,$t2",
            "Set on Less Than Unsigned: If $t1 is less than $t2 using unsigned comparision, then set $t8 to 1 else set $t1 to 0",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int first = context.cpuState.getReg(statement.rs_fs);
                    int second = context.cpuState.getReg(statement.rt_ft);
                    if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                        context.cpuState.setReg(TxCPUState.T8, (first < second) ? 1 : 0);
                    } else {
                        context.cpuState.setReg(TxCPUState.T8, (first >= 0) ? 1 : 0);
                    }
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction ssubInstruction = new TxInstruction("ssub", "k, [i, ]j", "kw", "ssub $t1,$t2,$t3",
            "Saturated SUBtraction: set $t2 to ($t1 minus $t2), or max/min integer values if overflow occurs",
            null, InstructionFormat16.RR,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    int sub1 = context.cpuState.getReg(statement.rs_fs);
                    int sub2 = context.cpuState.getReg(statement.rt_ft);
                    int dif = sub1 + sub2;
                    // overflow on A-B detected when A and B have opposite signs and A-B has B's sign
                    if ((sub1 >= 0 && sub2 < 0 && dif < 0)) {
                        dif = Integer.MAX_VALUE;
                    } else if (sub1 < 0 && sub2 >= 0 && dif >= 0) {
                        dif = Integer.MIN_VALUE;
                    }
                    context.cpuState.setReg(statement.rt_ft, dif);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction zebInstruction = new TxInstruction("zeb", "i", "iw", "zeb $t1",
            "Zero-Extend Byte: sign-extend the lower 8 bits of $t1",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rs_fs, context.cpuState.getReg(statement.rs_fs) & 0x000000FF);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction zehInstruction = new TxInstruction("zeh", "i", "iw", "zeh $t1",
            "Zero-Extend Halfword: sign-extend the lower 16 bits of $t1",
            null, InstructionFormat16.RI,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rs_fs, context.cpuState.getReg(statement.rs_fs) & 0x0000FFFF);
                    context.cpuState.pc += statement.getNumBytes();
                }
            });

    public static final TxInstruction minInstruction = new TxInstruction("min", "k, [i, ]j", "kw", "min $t1, $t2, $t3",
            "MINimum signed: $t1 is set to the minimum of $t2 and $t3",
            null, InstructionFormat16.RR_MIN_MAX,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, Math.min(context.cpuState.getReg(statement.rs_fs), context.cpuState.getReg(statement.rt_ft)));
                    context.cpuState.pc += statement.getNumBytes();
                }
            });
    public static final TxInstruction maxInstruction = new TxInstruction("max", "k, [i, ]j", "kw", "min $t1, $t2, $t3",
            "MAXimum signed: $t1 is set to the maximum of $t2 and $t3",
            null, InstructionFormat16.RR_MIN_MAX,
            Instruction.FlowType.NONE, false, Instruction.DelaySlotType.NONE,
            new SimulationCode() {
                public void simulate(TxStatement statement, StatementContext context) throws EmulationException {
                    context.cpuState.setReg(statement.rd_fd, Math.max(context.cpuState.getReg(statement.rs_fs), context.cpuState.getReg(statement.rt_ft)));
                    context.cpuState.pc += statement.getNumBytes();
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

    private static int getMaskedBasePc(TxStatement statement, StatementContext context) throws EmulationException {
        if (statement.getDelaySlotType() != Instruction.DelaySlotType.NONE) {
            // TODO We should test (statement.delaySlotType != Instruction.DelaySlotType.NONE) and if so,
            // TODO subract 2 or 4 to the PC to point on the JR, JALR, JAL or JALX's pc.
            // TODO But we don't know anymore if it was 2 or 4 bytes :-(
            // TODO Instead of storing the delay slot type we're in, we should store the statement whose delay slot we're in...
            throw new EmulationException("Determining masked base PC in a delay slot is not implemented.");
        }
        return context.cpuState.getPc() & 0xFFFFFFFC;         // 2 LSB masked
    }


    public static TxInstruction getInstructionFor16BitStatement(int binStatement) {
        return opcode16Map[binStatement];
    }

    public static TxInstruction getExtendedInstructionFor16BitStatement(int binStatement) {
        return extendedOpcode16Map[binStatement];
    }


    public static TxInstruction getInstructionFor32BitStatement(int binStatement) throws DisassemblyException {
        return opcode32Resolver.resolve(binStatement);
    }

    public static Instruction getJalInstructionForStatement(int binaryStatement)  throws DisassemblyException {
        if ((binaryStatement & 0b00000100_00000000_00000000_00000000) == 0) {
            return jalInstruction;
        }
        else {
            return jalxInstruction;
        }
    }

    public static Instruction getMinMaxInstructionForStatement(int binaryStatement) {
        if ((binaryStatement & 0b00000100_00000000_00000000_00000000) == 0) {
            return maxInstruction;
        }
        else {
            return minInstruction;
        }
    }

    public static Instruction getBs1fBfinsInstructionForStatement(int binaryStatement) {
        if ((binaryStatement & 0b00000100_00000000_00000000_00000000) == 0) {
            return bfinsInstruction;
        }
        else {
            return bs1fInstruction;
        }
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

    static InstructionResolver balOrBgezalInstructionResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            if (((binStatement >> 21) & 0b11111) == 0) { // rs == 0
                return balInstruction;
            }
            else {
                return bgezalInstruction;
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
            throw new ReservedInstructionException("Disassembly of statement 0x" + Format.asHex(binStatement, 8) + " is reserved (star case)");
        }
    };

    static InstructionResolver betaResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            throw new ReservedInstructionException("Disassembly of statement 0x" + Format.asHex(binStatement, 8) + " is reserved (beta case)");
        }
    };

    static InstructionResolver thetaResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            // Formally, See section 3.5:
            // If the corresponding CU bit in the Status register is cleared, a Coprocessor Unusable exception is taken.
            // If the CU bit is set, a Reserved Instruction exception is taken.
            throw new ReservedInstructionException("Disassembly of statement 0x" + Format.asHex(binStatement, 8) + " is reserved (theta case)");
        }
    };

    static InstructionResolver unimplementedResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            throw new ReservedInstructionException("Disassembly of statement 0x" + Format.asHex(binStatement, 8) + " is not yet implemented");
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

    /**
     * Default instruction decoding upon class loading
     */
    static {
        init(EnumSet.noneOf(OutputOption.class));
    }

    public static void init(Set<OutputOption> outputOptions) {
        // ----------------- 16-bits -----------------

        // These are rewrites of the Toshiba architecture document, appendix F

        // WARNING : the order matters, as some instructions are special cases patching generic cases (e.g. di and ei patch sll when sa=0)

        // Fill with dummy instruction by default
        for (int i = 0; i < opcode16Map.length; i++) {
            opcode16Map[i] = unknownInstruction;
            extendedOpcode16Map[i] = unknownInstruction;
        }

        // Now patch with defined 16-bit instruction and operand combinations
        //                map                , encoding          , mask              , instruction
        expandInstruction(opcode16Map,         0b11100000_00000000, 0b11111000_10000011, ac0iuInstruction);

        expandInstruction(opcode16Map,         0b01100110_00000000, 0b11111111_00000000, addiufp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b01100110_00000000, 0b11111111_11100000, addiufpInstruction);

        expandInstruction(opcode16Map,         0b01001000_00000000, 0b11111000_00000000, addiu8Instruction);
        expandInstruction(extendedOpcode16Map, 0b01001000_00000000, 0b11111000_11100000, addiu8Instruction);

        expandInstruction(opcode16Map,         0b00001000_00000000, 0b11111000_00000000, addiupc16Instruction);
        expandInstruction(extendedOpcode16Map, 0b00001000_00000000, 0b11111000_11100000, addiupcInstruction);

        expandInstruction(opcode16Map,         0b00000000_00000000, 0b11111000_00000000, addiusp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b00000000_00000000, 0b11111000_11100000, addiuspInstruction);

        expandInstruction(opcode16Map,         0b01000000_00000000, 0b11111000_00010000, addiuInstruction);
        expandInstruction(extendedOpcode16Map, 0b01000000_00000000, 0b11111000_00010000, addiuInstruction);

        expandInstruction(opcode16Map,         0b01100011_00000000, 0b11111111_00000000, adjspInstruction);
        expandInstruction(extendedOpcode16Map, 0b01100011_00000000, 0b11111111_11100000, adjspInstruction);

/*
        expandInstruction(extendedOpcode16Map, 0b01001000_01000000, 0b11111000_11100000, addmiuInstruction); // incl with $r0
*/

        expandInstruction(opcode16Map,         0b11100000_00000001, 0b11111000_00000011, adduInstruction);

        expandInstruction(opcode16Map,         0b11101000_00001100, 0b11111000_00011111, andInstruction);

        expandInstruction(extendedOpcode16Map, 0b01001000_10000000, 0b11111000_11100000, andiInstruction);

        expandInstruction(opcode16Map,         0b00010000_00000000, 0b11111000_00000000, bInstruction);
        expandInstruction(extendedOpcode16Map, 0b00010000_00000000, 0b11111111_11100000, bInstruction);

        expandInstruction(opcode16Map,         0b11111100_00000000, 0b11111111_00000000, bal16Instruction);
        expandInstruction(extendedOpcode16Map, 0b11111100_00000000, 0b11111111_11100000, bal16Instruction);

        expandInstruction(extendedOpcode16Map, 0b11111001_00000000, 0b11111111_00000000, bclrInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b11111001_00000000, 0b11111111_00000000, bclrfpInstruction);

        expandInstruction(opcode16Map,         0b00100000_00000000, 0b11111000_00000000, beqz16Instruction);
        expandInstruction(extendedOpcode16Map, 0b00100000_00000000, 0b11111000_11100000, beqz16Instruction);

        expandInstruction(extendedOpcode16Map, 0b11111101_00000000, 0b11111111_00000000, bextInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b11111101_00000000, 0b11111111_00000000, bextfpInstruction);

        /* bs1f and bfins instructions differ in the upper 16-bits and are processed separately */

        expandInstruction(extendedOpcode16Map, 0b11111011_00000000, 0b11111111_00000000, binsInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b11111011_00000000, 0b11111111_00000000, binsfpInstruction);

        expandInstruction(opcode16Map,         0b00101000_00000000, 0b11111000_00000000, bnez16Instruction);
        expandInstruction(extendedOpcode16Map, 0b00101000_00000000, 0b11111000_11100000, bnez16Instruction);

        expandInstruction(opcode16Map,         0b11101000_00000101, 0b11111000_00011111, breakInstruction);

        expandInstruction(extendedOpcode16Map, 0b11111010_00000000, 0b11111111_00000000, bsetInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b11111010_00000000, 0b11111111_00000000, bsetfpInstruction);

        expandInstruction(opcode16Map,         0b01100000_00000000, 0b11111111_00000000, bteqzInstruction);
        expandInstruction(extendedOpcode16Map, 0b01100000_00000000, 0b11111111_11100000, bteqzInstruction);

        expandInstruction(opcode16Map,         0b01100001_00000000, 0b11111111_00000000, btnezInstruction);
        expandInstruction(extendedOpcode16Map, 0b01100001_00000000, 0b11111111_11100000, btnezInstruction);

        expandInstruction(extendedOpcode16Map, 0b11111000_00000000, 0b11111111_00000000, btstInstruction); // incl with $r0

        expandInstruction(opcode16Map,         0b11111000_00000000, 0b11111111_00000000, btstfpInstruction);

        expandInstruction(opcode16Map,         0b11101000_00001010, 0b11111000_00011111, cmpInstruction);

        expandInstruction(opcode16Map,         0b01110000_00000000, 0b11111000_00000000, cmpiInstruction);
        expandInstruction(extendedOpcode16Map, 0b01110000_00000000, 0b11111000_11100000, cmpiInstruction);

/*
        expandInstruction(extendedOpcode16Map, 0b11101000_00011111, 0b11111111_11111111, deretInstruction); // + constraints on extended part
*/
        /* di is defined later as a patch on sll5 when sa == 0 */

        expandInstruction(opcode16Map,         0b11101000_00011010, 0b11111000_00011111, divInstruction);

        expandInstruction(opcode16Map,         0b11101000_00011110, 0b11111000_00011111, diveInstruction);

        expandInstruction(opcode16Map,         0b11101000_00011111, 0b11111000_00011111, diveuInstruction);

        expandInstruction(opcode16Map,         0b11101000_00011011, 0b11111000_00011111, divuInstruction);

        /* ei is defined later as a patch on sll5 when sa == 0 */

        expandInstruction(extendedOpcode16Map, 0b11101000_00011000, 0b11111111_11111111, eretInstruction); // + constraints on extended part ?

        expandInstruction(opcode16Map,         0b11101000_01000000, 0b11111000_11111111, jalr16Instruction);

        expandInstruction(opcode16Map,         0b11101000_11000000, 0b11111000_11111111, jalrcInstruction);

        expandInstruction(opcode16Map,         0b11101000_00000000, 0b11111000_11111111, jrInstruction);

        expandInstruction(opcode16Map,         0b11101000_00100000, 0b11111111_11111111, jrraInstruction);

        expandInstruction(opcode16Map,         0b11101000_10100000, 0b11111111_11111111, jrcraInstruction);

        expandInstruction(opcode16Map,         0b11101000_10000000, 0b11111000_11111111, jrcInstruction);

        expandInstruction(opcode16Map,         0b10000000_00000000, 0b11111000_00000000, lb16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10000000_00000000, 0b11111000_00000000, lbInstruction);

        expandInstruction(opcode16Map,         0b10100000_00000000, 0b11111000_00000000, lbu16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10100000_00000000, 0b11111000_00000000, lbuInstruction);

        expandInstruction(opcode16Map,         0b00111000_00000000, 0b11111000_10000000, lbufp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b00111000_00000000, 0b11111000_11100000, lbufpInstruction);

        expandInstruction(opcode16Map,         0b01111000_00000000, 0b11111000_10000000, lbusp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b01111000_00000000, 0b11111000_11100000, lbuspInstruction);

        expandInstruction(opcode16Map,         0b10001000_00000000, 0b11111000_00000000, lh16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10001000_00000000, 0b11111000_00000000, lhInstruction);

        expandInstruction(opcode16Map,         0b10101000_00000000, 0b11111000_00000000, lhu16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10101000_00000000, 0b11111000_00000000, lhuInstruction);

        expandInstruction(opcode16Map,         0b10111000_00000001, 0b11111000_10000001, lhufp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10111000_00000001, 0b11111000_11100001, lhufpInstruction);

        expandInstruction(opcode16Map,         0b10111000_00000000, 0b11111000_10000001, lhusp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10111000_00000000, 0b11111000_11100001, lhuspInstruction);


        expandInstruction(opcode16Map,         0b01101000_00000000, 0b11111000_00000000, liInstruction);
        expandInstruction(extendedOpcode16Map, 0b01101000_00000000, 0b11111000_11100000, liInstruction);

        expandInstruction(extendedOpcode16Map, 0b01001000_11100000, 0b11111000_11100000, luiInstruction);

        expandInstruction(opcode16Map,         0b10011000_00000000, 0b11111000_00000000, lw16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10011000_00000000, 0b11111000_00000000, lwInstruction);

        expandInstruction(opcode16Map,         0b11111110_00000000, 0b11111111_00000000, lwfp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b11111110_00000000, 0b11111111_00000000, lwfpInstruction);

        expandInstruction(opcode16Map,         0b10010000_00000000, 0b11111000_00000000, lwsp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10010000_00000000, 0b11111000_11100000, lwspInstruction);

        expandInstruction(opcode16Map,         0b10110000_00000000, 0b11111000_00000000, lwpc16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10110000_00000000, 0b11111000_11100000, lwpcInstruction);

        expandInstruction(opcode16Map,         0b11101000_00010110, 0b11111000_00011111, madd16Instruction);

        expandInstruction(opcode16Map,         0b11101000_00010111, 0b11111000_00011111, maddu16Instruction);

        /* min and max instructions look like extended breaks. They differ in the upper 16-bits and are processed separately */

        expandInstruction(opcode16Map,         0b00110000_00000001, 0b11111000_00000111, mfc0Instruction);
        expandInstruction(opcode16Map,         0b00110000_00000101, 0b11111000_00000111, mtc0Instruction);

        expandInstruction(opcode16Map,         0b11101000_00010000, 0b11111000_11111111, mfhiInstruction);
        expandInstruction(opcode16Map,         0b11100000_00000010, 0b11111000_11111111, mthiInstruction);

        expandInstruction(opcode16Map,         0b11101000_00010010, 0b11111000_11111111, mfloInstruction);

        /* mtlo is defined later as a patch on srl when sa == 0 */

        expandInstruction(opcode16Map,         0b11101100_00001000, 0b11111100_00011111, movefpInstruction);

        expandInstruction(opcode16Map,         0b01100111_00000000, 0b11111111_00000000, moveR32Instruction);

        expandInstruction(opcode16Map,         0b01100101_00000000, 0b11111111_00000000, move32RInstruction);
        if (outputOptions.contains(OutputOption.DMOV)) {
            // Patch : replace "move $zero, $xx" > "nop"
            expandInstruction(opcode16Map,     0b01100101_00000000, 0b11111111_11111000, nopInstruction);
        }
        expandInstruction(opcode16Map,         0b11101000_00011100, 0b11111000_00011111, multInstruction);

        expandInstruction(opcode16Map,         0b11101000_00011000, 0b11111000_00011111, mult16Instruction);

        expandInstruction(opcode16Map,         0b11101000_00011001, 0b11111000_00011111, multu16Instruction);

        expandInstruction(opcode16Map,         0b11101000_00011101, 0b11111000_00011111, multuInstruction);

        expandInstruction(opcode16Map,         0b11101000_00001011, 0b11111000_00011111, negInstruction);

        expandInstruction(opcode16Map,         0b11101000_00001111, 0b11111000_00011111, notInstruction);

        expandInstruction(opcode16Map,         0b11101000_00001101, 0b11111000_00011111, orInstruction);

        expandInstruction(extendedOpcode16Map, 0b01001000_10100000, 0b11111000_11100000, oriInstruction);

        expandInstruction(opcode16Map,         0b11101000_00010100, 0b11111000_00011111, saddInstruction);

        expandInstruction(opcode16Map,         0b01100100_00000000, 0b11111111_10000000, restoreInstruction);

        expandInstruction(extendedOpcode16Map, 0b01100100_00000000, 0b11111111_10000000, restoreInstruction);

        expandInstruction(opcode16Map,         0b01100100_10000000, 0b11111111_10000000, saveInstruction);

        expandInstruction(extendedOpcode16Map, 0b01100100_10000000, 0b11111111_10000000, saveInstruction);

        expandInstruction(opcode16Map,         0b11000000_00000000, 0b11111000_00000000, sb16Instruction);
        expandInstruction(extendedOpcode16Map, 0b11000000_00000000, 0b11111000_00000000, sbInstruction);

        expandInstruction(opcode16Map,         0b00111000_10000000, 0b11111000_10000000, sbfp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b00111000_10000000, 0b11111000_11100000, sbfpInstruction);

        expandInstruction(opcode16Map,         0b01111000_10000000, 0b11111000_10000000, sbsp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b01111000_10000000, 0b11111000_11100000, sbspInstruction);

        expandInstruction(opcode16Map,         0b11001000_00000000, 0b11111000_00000000, sh16Instruction);
        expandInstruction(extendedOpcode16Map, 0b11001000_00000000, 0b11111000_00000000, shInstruction);

        expandInstruction(opcode16Map,         0b10111000_10000001, 0b11111000_10000001, shfp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10111000_10000001, 0b11111000_11100001, shfpInstruction);

        expandInstruction(opcode16Map,         0b10111000_10000000, 0b11111000_10000001, shsp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b10111000_10000000, 0b11111000_11100001, shspInstruction);

        expandInstruction(opcode16Map,         0b11011000_00000000, 0b11111000_00000000, sw16Instruction);
        expandInstruction(extendedOpcode16Map, 0b11011000_00000000, 0b11111000_00000000, swInstruction);

        expandInstruction(opcode16Map,         0b11111111_00000000, 0b11111111_00000000, swfp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b11111111_00000000, 0b11111111_00000000, swfpInstruction);

        expandInstruction(opcode16Map,         0b11010000_00000000, 0b11111000_00000000, swsp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b11010000_00000000, 0b11111000_11100000, swspInstruction);

        expandInstruction(opcode16Map,         0b01100010_00000000, 0b11111111_00000000, swrasp16Instruction);
        expandInstruction(extendedOpcode16Map, 0b01100010_00000000, 0b11111111_11100000, swraspInstruction);

/*
        // if EJTAG
        expandInstruction(opcode16Map,         0b11101000_00000001, 0b11111000_00011111, sdbbpInstruction);
*/

        expandInstruction(opcode16Map,         0b11101000_10010001, 0b11111000_11111111, sebInstruction);

        expandInstruction(opcode16Map,         0b11101000_10110001, 0b11111000_11111111, sehInstruction);

        expandInstruction(opcode16Map,         0b00110000_00000000, 0b11111000_00000011, sllInstruction);
        expandInstruction(extendedOpcode16Map, 0b00110000_00000000, 0b11111000_00011111, sllInstruction);

        expandInstruction(opcode16Map,         0b11100000_10000000, 0b11111000_10000011, sll5Instruction);
        // all values are valid for the sa field, except 00000. So override them with unknown:
        expandInstruction(opcode16Map,         0b11100000_10000000, 0b11111000_11111111, unknownInstruction);
        // Then patch those that have other meanings (di, ei, ...):
        expandInstruction(opcode16Map,         0b11100000_10000000, 0b11111111_11111111, diInstruction);
        expandInstruction(opcode16Map,         0b11100001_10000000, 0b11111111_11111111, eiInstruction);
        // end patches

        expandInstruction(opcode16Map,         0b11101000_00000100, 0b11111000_00011111, sllvInstruction);

        expandInstruction(opcode16Map,         0b00110000_00000011, 0b11111000_00000011, sraInstruction);
        expandInstruction(extendedOpcode16Map, 0b00110000_00000011, 0b11111000_00011111, sraInstruction);

        expandInstruction(opcode16Map,         0b11100000_00000010, 0b11111000_10000011, sra5Instruction);
        // all values are valid for the sa field, except 00000. So override them with unknown:
        expandInstruction(opcode16Map,         0b11100000_00000010, 0b11111000_11111111, unknownInstruction);

        expandInstruction(opcode16Map,         0b11101000_00000111, 0b11111000_00011111, sravInstruction);

        expandInstruction(opcode16Map,         0b00110000_00000010, 0b11111000_00000011, srlInstruction);
        expandInstruction(extendedOpcode16Map, 0b00110000_00000010, 0b11111000_00011111, srlInstruction);

        expandInstruction(opcode16Map,         0b11100000_10000010, 0b11111000_10000011, srl5Instruction);
        // all values are valid for the sa field, except 00000, which is mtlo. So patch it:
        expandInstruction(opcode16Map,         0b11100000_10000010, 0b11111000_11111111, mtloInstruction);

        expandInstruction(opcode16Map,         0b11101000_00000110, 0b11111000_00011111, srlvInstruction);

        expandInstruction(opcode16Map,         0b11101000_00000010, 0b11111000_00011111, slt16Instruction);

        expandInstruction(opcode16Map,         0b01010000_00000000, 0b11111000_00000000, slti16Instruction);
        expandInstruction(extendedOpcode16Map, 0b01010000_00000000, 0b11111000_11100000, slti16eInstruction);

        expandInstruction(opcode16Map,         0b01011000_00000000, 0b11111000_00000000, sltiu16Instruction);
        expandInstruction(extendedOpcode16Map, 0b01011000_00000000, 0b11111000_11100000, sltiu16eInstruction);

        expandInstruction(opcode16Map,         0b11101000_00000011, 0b11111000_00011111, sltu16Instruction);

        expandInstruction(opcode16Map,         0b11101000_00010101, 0b11111000_00011111, ssubInstruction);

        expandInstruction(opcode16Map,         0b11100000_00000011, 0b11111000_00000011, subuInstruction);

        expandInstruction(extendedOpcode16Map, 0b11101000_00001111, 0b11111111_11111111, syncInstruction);

/*
        expandInstruction(extendedOpcode16Map, 0b1110100000001100, 0b1111100000011111, syscallInstruction);
*/

        expandInstruction(extendedOpcode16Map, 0b11101000_00000000, 0b11111111_11111111, waitInstruction);

        expandInstruction(opcode16Map,         0b11101000_00001110, 0b11111000_00011111, xorInstruction);

        expandInstruction(extendedOpcode16Map, 0b01001000_11000000, 0b11111000_11100000, xoriInstruction);

        expandInstruction(opcode16Map,         0b11101000_00010001, 0b11111000_11111111, zebInstruction);

        expandInstruction(opcode16Map,         0b11101000_00110001, 0b11111000_11111111, zehInstruction);


        // ----------------- 32-bits -----------------

        // These are rewrites of the Toshiba architecture document, appendix E

        // Encoding of the Opcode Field
        opcodeResolvers = new InstructionResolver[64];

        opcodeResolvers[0b000000] = specialFunctionResolver;
        opcodeResolvers[0b000001] = regImmRtResolver;
        opcodeResolvers[0b000010] = new DirectInstructionResolver(jInstruction);
        opcodeResolvers[0b000011] = new DirectInstructionResolver(jalInstruction);
        opcodeResolvers[0b000100] = new DirectInstructionResolver(beqInstruction);
        if (outputOptions.contains(OutputOption.BZ)) {
            opcodeResolvers[0b000100] = beqOrBeqzResolver;
        }
        opcodeResolvers[0b000101] = new DirectInstructionResolver(bneInstruction);
        if (outputOptions.contains(OutputOption.BZ)) {
            opcodeResolvers[0b000101] = bneOrBnezResolver;
        }
        opcodeResolvers[0b000110] = new DirectInstructionResolver(blezInstruction);
        opcodeResolvers[0b000111] = new DirectInstructionResolver(bgtzInstruction);

        opcodeResolvers[0b001000] = new DirectInstructionResolver(addiInstruction);
        opcodeResolvers[0b001001] = new DirectInstructionResolver(addiuInstruction);
        if (outputOptions.contains(OutputOption.LI)) {
            opcodeResolvers[0b001001] = addiuOrLiResolver;
        }
        opcodeResolvers[0b001010] = new DirectInstructionResolver(sltiInstruction);
        opcodeResolvers[0b001011] = new DirectInstructionResolver(sltiuInstruction);
        opcodeResolvers[0b001100] = new DirectInstructionResolver(andiInstruction);
        opcodeResolvers[0b001101] = new DirectInstructionResolver(oriInstruction);
        if (outputOptions.contains(OutputOption.LI)) {
            opcodeResolvers[0b001101] = oriOrLiResolver;
        }
        opcodeResolvers[0b001110] = new DirectInstructionResolver(xoriInstruction);
        opcodeResolvers[0b001111] = new DirectInstructionResolver(luiInstruction);

        opcodeResolvers[0b010000] = cop0RsResolver;
        opcodeResolvers[0b010001] = cop1RsResolver;
        opcodeResolvers[0b010010] = /*COP2*/ thetaResolver;
        opcodeResolvers[0b010011] = /*COP3*/ thetaResolver;
        opcodeResolvers[0b010100] = new DirectInstructionResolver(beqlInstruction);
        if (outputOptions.contains(OutputOption.BZ)) {
            opcodeResolvers[0b010100] = beqlOrBeqzlResolver;
        }
        opcodeResolvers[0b010101] = new DirectInstructionResolver(bnelInstruction);
        if (outputOptions.contains(OutputOption.BZ)) {
            opcodeResolvers[0b010101] = bnelOrBnezlResolver;
        }
        opcodeResolvers[0b010110] = new DirectInstructionResolver(blezlInstruction);
        opcodeResolvers[0b010111] = new DirectInstructionResolver(bgtzlInstruction);

        opcodeResolvers[0b011000] = betaResolver;
        opcodeResolvers[0b011001] = betaResolver;
        opcodeResolvers[0b011010] = betaResolver;
        opcodeResolvers[0b011011] = betaResolver;
        opcodeResolvers[0b011100] = special2FunctionResolver;
        opcodeResolvers[0b011101] = new DirectInstructionResolver(jalxInstruction);
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
        if (outputOptions.contains(OutputOption.SHIFT)) {
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
        if (outputOptions.contains(OutputOption.RET)) {
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
        if (outputOptions.contains(OutputOption.DMOV)) {
            specialFunctionResolvers[0b100001] = adduOrMoveResolver;
        }
        specialFunctionResolvers[0b100010] = new DirectInstructionResolver(subInstruction);
        specialFunctionResolvers[0b100011] = new DirectInstructionResolver(subuInstruction);
        specialFunctionResolvers[0b100100] = new DirectInstructionResolver(andInstruction);
        specialFunctionResolvers[0b100101] = new DirectInstructionResolver(orInstruction);
        if (outputOptions.contains(OutputOption.DMOV)) {
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
        if (true) { // TODO make this an option
            regImmRtResolvers[0b10001] = balOrBgezalInstructionResolver;
        }
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