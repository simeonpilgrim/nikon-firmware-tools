package com.nikonhacker.disassembly.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.DisassemblyException;
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
 * The list of TxInstruction objects, each of which represents a MIPS instruction.
 * The instruction may either be basic (translates into binary machine code) or
 * extended (translates into sequence of one or more basic instructions).
 *
 *
 * @author Pete Sanderson and Ken Vollmar
 * @version August 2003-5
 */

public class TxInstructionSet
{
    public static final TxInstruction addInstruction = new TxInstruction("add", "k, i, j", "add $t1,$t2,$t3",
            "Addition with overflow : set $t1 to ($t2 plus $t3)",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 100000",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int add1 = cpuState.getReg(statement.rs);
                    int add2 = cpuState.getReg(statement.rt);
                    int sum = add1 + add2;
                    // overflow on A+B detected when A and B have same sign and A+B has other sign.
                    if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    cpuState.setReg(statement.rd, sum);
                }
            });
    public static final TxInstruction subInstruction = new TxInstruction("sub", "k, i, j", "sub $t1,$t2,$t3",
            "Subtraction with overflow : set $t1 to ($t2 minus $t3)",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 100010",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int sub1 = cpuState.getReg(statement.rs);
                    int sub2 = cpuState.getReg(statement.rt);
                    int dif = sub1 - sub2;
                    // overflow on A-BREAK detected when A and BREAK have opposite signs and A-BREAK has BREAK's sign
                    if ((sub1 >= 0 && sub2 < 0 && dif < 0) || (sub1 < 0 && sub2 >= 0 && dif >= 0)) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    cpuState.setReg(statement.rd, dif);
                }
            });
    public static final TxInstruction addiInstruction = new TxInstruction("addi", "j, i, s", "addi $t1,$t2,-100",
            "Addition immediate with overflow : set $t1 to ($t2 plus signed 16-bit immediate)",
            TxInstruction.Format.I,
            "001000 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int add1 = cpuState.getReg(statement.rs);
                    int add2 = statement.imm << 16 >> 16;
                    int sum = add1 + add2;
                    // overflow on A+BREAK detected when A and BREAK have same sign and A+BREAK has other sign.
                    if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                        throw new TxEmulationException(statement, "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                    cpuState.setReg(statement.rt, sum);
                }
            });
    public static final TxInstruction adduInstruction = new TxInstruction("addu", "k, i, j", "addu $t1,$t2,$t3",
            "Addition unsigned without overflow : set $t1 to ($t2 plus $t3), no overflow",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 100001",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rs) + cpuState.getReg(statement.rt));
                }
            });
    public static final TxInstruction subuInstruction = new TxInstruction("subu", "k, i, j", "subu $t1,$t2,$t3",
            "Subtraction unsigned without overflow : set $t1 to ($t2 minus $t3), no overflow",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 100011",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rs) - cpuState.getReg(statement.rt));
                }
            });
    public static final TxInstruction addiuInstruction = new TxInstruction("addiu", "j, i, u", "addiu $t1,$t2,-100", // TODO why -100 for unsigned ?
            "Addition immediate unsigned without overflow : set $t1 to ($t2 plus signed 16-bit immediate), no overflow",
            TxInstruction.Format.I,
            "001001 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt, cpuState.getReg(statement.rs) + (statement.imm << 16 >> 16));
                }
            });
    public static final TxInstruction multInstruction = new TxInstruction("mult", "(k,) i, j", "mult $t1,$t2",
            "Multiplication : Set hi to high-order 32 bits, lo to low-order 32 bits of the product of $t1 and $t2 (use mfhi to access hi, mflo to access lo)",
            TxInstruction.Format.R,
            "000000 fffff sssss 00000 00000 011000",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (long) cpuState.getReg(statement.rs) * (long) cpuState.getReg(statement.rt);
                    cpuState.setReg(TxCPUState.HI, (int) (product >> 32));
                    int lo = (int) ((product << 32) >> 32);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd, lo);
                }
            });
    public static final TxInstruction multuInstruction = new TxInstruction("multu", "(k,) i, j", "multu $t1,$t2",
            "Multiplication unsigned : Set HI to high-order 32 bits, LO to low-order 32 bits of the product of unsigned $t1 and $t2 (use mfhi to access HI, mflo to access LO)",
            TxInstruction.Format.R,
            "000000 fffff sssss 00000 00000 011001",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (((long) cpuState.getReg(statement.rs)) << 32 >>> 32)
                            * (((long) cpuState.getReg(statement.rt)) << 32 >>> 32);
                    cpuState.setReg(TxCPUState.HI, (int) (product >> 32));
                    int lo = (int) ((product << 32) >> 32);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd, lo);
                }
            });
    public static final TxInstruction mulInstruction = new TxInstruction("mul", "k, i, j", "mul $t1,$t2,$t3",
            "Multiplication without overflow  : Set $t1 to low-order 32 bits of the product of $t2 and $t3",
            TxInstruction.Format.R,
            "011100 sssss ttttt fffff 00000 000010",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (long) cpuState.getReg(statement.rs) * (long) cpuState.getReg(statement.rt);
                    cpuState.setReg(statement.rd, (int) ((product << 32) >> 32));
                }
            });
    public static final TxInstruction maddInstruction = new TxInstruction("madd", "(k,) i, j", "madd $t1,$t2",
            "Multiply add : Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product (use mfhi to access HI, mflo to access LO)",
            TxInstruction.Format.R,
            "011100 fffff sssss 00000 00000 000000",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (long) cpuState.getReg(statement.rs) * (long) cpuState.getReg(statement.rt);
                    long contentsHiLo = Format.twoIntsToLong(cpuState.getReg(TxCPUState.HI), cpuState.getReg(TxCPUState.LO));
                    long sum = contentsHiLo + product;
                    cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(sum));
                    int lo = Format.lowOrderLongToInt(sum);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd, lo);
                }
            });
    public static final TxInstruction madduInstruction = new TxInstruction("maddu", "(k,) i, j", "maddu $t1,$t2",
            "Multiply add unsigned : Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product, unsigned (use mfhi to access HI, mflo to access LO)",
            TxInstruction.Format.R,
            "011100 fffff sssss 00000 00000 000001",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (((long) cpuState.getReg(statement.rs)) << 32 >>> 32)
                            * (((long) cpuState.getReg(statement.rt)) << 32 >>> 32);
                    long contentsHiLo = Format.twoIntsToLong(cpuState.getReg(TxCPUState.HI), cpuState.getReg(TxCPUState.LO));
                    long sum = contentsHiLo + product;
                    cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(sum));
                    int lo = Format.lowOrderLongToInt(sum);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd, lo);
                }
            });
    public static final TxInstruction msubInstruction = new TxInstruction("msub", "(k,) i, j", "msub $t1,$t2",
            "Multiply subtract : Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decrement LO by low-order 32 bits of product (use mfhi to access HI, mflo to access LO)",
            TxInstruction.Format.R,
            "011100 fffff sssss 00000 00000 000100",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (long) cpuState.getReg(statement.rs) * (long) cpuState.getReg(statement.rt);
                    long contentsHiLo = Format.twoIntsToLong(cpuState.getReg(TxCPUState.HI), cpuState.getReg(TxCPUState.LO));
                    long diff = contentsHiLo - product;
                    cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(diff));
                    int lo = Format.lowOrderLongToInt(diff);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd, lo);
                }
            });
    public static final TxInstruction msubuInstruction = new TxInstruction("msubu", "(k,) i, j", "msubu $t1,$t2",
            "Multiply subtract unsigned : Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decement LO by low-order 32 bits of product, unsigned (use mfhi to access HI, mflo to access LO)",
            TxInstruction.Format.R,
            "011100 fffff sssss 00000 00000 000101",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    long product = (((long) cpuState.getReg(statement.rs)) << 32 >>> 32)
                            * (((long) cpuState.getReg(statement.rt)) << 32 >>> 32);
                    long contentsHiLo = Format.twoIntsToLong(cpuState.getReg(TxCPUState.HI), cpuState.getReg(TxCPUState.LO));
                    long diff = contentsHiLo - product;
                    cpuState.setReg(TxCPUState.HI, Format.highOrderLongToInt(diff));
                    int lo = Format.lowOrderLongToInt(diff);
                    cpuState.setReg(TxCPUState.LO, lo);
                    cpuState.setReg(statement.rd, lo);
                }
            });
    public static final TxInstruction divInstruction = new TxInstruction("div", "i, j", "div $t1,$t2",
            "Division with overflow : Divide $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)",
            TxInstruction.Format.R,
            "000000 fffff sssss 00000 00000 011010",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt) == 0) {
                        // Note: no exceptions, and undefined results for zero divide
                        return;
                    }

                    cpuState.setReg(TxCPUState.HI, cpuState.getReg(statement.rs) % cpuState.getReg(statement.rt));
                    cpuState.setReg(TxCPUState.LO, cpuState.getReg(statement.rs) / cpuState.getReg(statement.rt));
                }
            });
    public static final TxInstruction divuInstruction = new TxInstruction("divu", "i, j", "divu $t1,$t2",
            "Division unsigned without overflow : Divide unsigned $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)",
            TxInstruction.Format.R,
            "000000 fffff sssss 00000 00000 011011",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt) == 0) {
                        // Note: no exceptions, and undefined results for zero divide
                        return;
                    }
                    long oper1 = ((long) cpuState.getReg(statement.rs)) << 32 >>> 32;
                    long oper2 = ((long) cpuState.getReg(statement.rt)) << 32 >>> 32;
                    cpuState.setReg(TxCPUState.HI, (int) (((oper1 % oper2) << 32) >> 32));
                    cpuState.setReg(TxCPUState.LO, (int) (((oper1 / oper2) << 32) >> 32));
                }
            });
    public static final TxInstruction mfhiInstruction = new TxInstruction("mfhi", "k", "mfhi $t1",
            "Move from HI register : Set $t1 to contents of HI (see multiply and divide operations)",
            TxInstruction.Format.R,
            "000000 00000 00000 fffff 00000 010000",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.getReg(TxCPUState.HI));
                }
            });
    public static final TxInstruction mfloInstruction = new TxInstruction("mflo", "k", "mflo $t1",
            "Move from LO register : Set $t1 to contents of LO (see multiply and divide operations)",
            TxInstruction.Format.R,
            "000000 00000 00000 fffff 00000 010010",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.getReg(TxCPUState.LO));
                }
            });
    public static final TxInstruction mthiInstruction = new TxInstruction("mthi", "i", "mthi $t1",
            "Move to HI registerr : Set HI to contents of $t1 (see multiply and divide operations)",
            TxInstruction.Format.R,
            "000000 fffff 00000 00000 00000 010001",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.HI, cpuState.getReg(statement.rs));
                }
            });
    public static final TxInstruction mtloInstruction = new TxInstruction("mtlo", "i", "mtlo $t1",
            "Move to LO register : Set LO to contents of $t1 (see multiply and divide operations)",
            TxInstruction.Format.R,
            "000000 fffff 00000 00000 00000 010011",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.LO, cpuState.getReg(statement.rs));
                }
            });
    public static final TxInstruction andInstruction = new TxInstruction("and", "k, i, j", "and $t1,$t2,$t3",
            "Bitwise AND : Set $t1 to bitwise AND of $t2 and $t3",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 100100",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rs) & cpuState.getReg(statement.rt));
                }
            });
    public static final TxInstruction orInstruction = new TxInstruction("or", "k, i, j", "or $t1,$t2,$t3",
            "Bitwise OR : Set $t1 to bitwise OR of $t2 and $t3",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 100101",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rs) | cpuState.getReg(statement.rt));
                }
            });
    public static final TxInstruction andiInstruction = new TxInstruction("andi", "j, i, s", "andi $t1,$t2,100",
            "Bitwise AND immediate : Set $t1 to bitwise AND of $t2 and zero-extended 16-bit immediate",
            TxInstruction.Format.I,
            "001100 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                    cpuState.setReg(statement.rt, cpuState.getReg(statement.rs) & (statement.imm & 0x0000FFFF));
                }
            });
    public static final TxInstruction oriInstruction = new TxInstruction("or", "j, i, s", "ori $t1,$t2,100",
            "Bitwise OR immediate : Set $t1 to bitwise OR of $t2 and zero-extended 16-bit immediate",
            TxInstruction.Format.I,
            "001101 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                    cpuState.setReg(statement.rt, cpuState.getReg(statement.rs) | (statement.imm & 0x0000FFFF));
                }
            });
    public static final TxInstruction norInstruction = new TxInstruction("nor", "k, i, j", "nor $t1,$t2,$t3",
            "Bitwise NOR : Set $t1 to bitwise NOR of $t2 and $t3",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 100111",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, ~(cpuState.getReg(statement.rs) | cpuState.getReg(statement.rt)));
                }
            });
    public static final TxInstruction xorInstruction = new TxInstruction("xor", "k, i, j", "xor $t1,$t2,$t3",
            "Bitwise XOR (exclusive OR) : Set $t1 to bitwise XOR of $t2 and $t3",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 100110",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rs) ^ cpuState.getReg(statement.rt));
                }
            });
    public static final TxInstruction xoriInstruction = new TxInstruction("xori", "j, i, s", "xori $t1,$t2,100",
            "Bitwise XOR immediate : Set $t1 to bitwise XOR of $t2 and zero-extended 16-bit immediate",
            TxInstruction.Format.I,
            "001110 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                    cpuState.setReg(statement.rt, cpuState.getReg(statement.rs) ^ (statement.imm & 0x0000FFFF));
                }
            });
    public static final TxInstruction nopInstruction = new TxInstruction("nop", "", "nop",
            "Nop (formally a useless shift left logical) : Do nothing",
            TxInstruction.Format.R,
            "000000 00000 00000 00000 00000 000000",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // nop
                }
            });
    public static final TxInstruction sllInstruction = new TxInstruction("sll", "k, j, l", "sll $t1,$t2,10", // todo l should be presented as immediate, not a register number. Maybe L is not a good idea
            "Shift left logical : Set $t1 to result of shifting $t2 left by number of bits specified by immediate",
            TxInstruction.Format.R,
            "000000 00000 sssss fffff ttttt 000000",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rt) << statement.sa);
                }
            });
    public static final TxInstruction sllvInstruction = new TxInstruction("sllv", "k, j, i", "sllv $t1,$t2,$t3",
            "Shift left logical variable : Set $t1 to result of shifting $t2 left by number of bits specified by value in low-order 5 bits of $t3",
            TxInstruction.Format.R,
            "000000 ttttt sssss fffff 00000 000100",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // Mask all but low 5 bits of register containing shift amount.
                    cpuState.setReg(statement.rd,
                            cpuState.getReg(statement.rt) << (cpuState.getReg(statement.rs) & 0b11111));
                }
            });
    public static final TxInstruction srlInstruction = new TxInstruction("srl", "k, j, l", "srl $t1,$t2,10",
            "Shift right logical : Set $t1 to result of shifting $t2 right by number of bits specified by immediate",
            TxInstruction.Format.R,
            "000000 00000 sssss fffff ttttt 000010",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // must zero-fill, so use ">>>" instead of ">>".
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rt) >>> statement.sa);
                }
            });
    public static final TxInstruction sraInstruction = new TxInstruction("sra", "k, j, l", "sra $t1,$t2,10",
            "Shift right arithmetic : Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by immediate",
            TxInstruction.Format.R,
            "000000 00000 sssss fffff ttttt 000011",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // must sign-fill, so use ">>".
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rt) >> statement.sa);
                }
            });
    public static final TxInstruction sravInstruction = new TxInstruction("srav", "k, j, i", "srav $t1,$t2,$t3",
            "Shift right arithmetic variable : Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3",
            TxInstruction.Format.R,
            "000000 ttttt sssss fffff 00000 000111",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // Mask all but low 5 bits of register containing shift amount. Use ">>" to sign-fill.
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rt) >> (cpuState.getReg(statement.rs) & 0b11111));
                }
            });
    public static final TxInstruction srlvInstruction = new TxInstruction("srlv", "k, j, i", "srlv $t1,$t2,$t3",
            "Shift right logical variable : Set $t1 to result of shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3",
            TxInstruction.Format.R,
            "000000 ttttt sssss fffff 00000 000110",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // Mask all but low 5 bits of register containing shift amount. Use ">>>" to zero-fill.
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rt) >>> (cpuState.getReg(statement.rs) & 0b11111));
                }
            });
    public static final TxInstruction lwInstruction = new TxInstruction("lw", "j, s(i)", "lw $t1,-100($t2)",
            "Load word : Set $t1 to contents of effective memory word address",
            TxInstruction.Format.I,
            "100011 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    cpuState.setReg(statement.rt, memory.load32(cpuState.getReg(statement.rs) + statement.imm));
                }
            });
    public static final TxInstruction lwlInstruction = new TxInstruction("lwl", "j, s(i)", "lwl $t1,-100($t2)",
            "Load word left : Load from 1 to 4 bytes left-justified into $t1, starting with effective memory byte address and continuing through the low-order byte of its word",
            TxInstruction.Format.I,
            "100010 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    int address = cpuState.getReg(statement.rs) + statement.imm;
                    int result = cpuState.getReg(statement.rt);
                    for (int i = 0; i <= address % 4; i++) {
                        result = Format.setByte(result, 3 - i, memory.loadUnsigned8(address - i));
                    }
                    cpuState.setReg(statement.rt, result);
                }
            });
    public static final TxInstruction lwrInstruction = new TxInstruction("lwr", "j, s(i)", "lwr $t1,-100($t2)",
            "Load word right : Load from 1 to 4 bytes right-justified into $t1, starting with effective memory byte address and continuing through the high-order byte of its word",
            TxInstruction.Format.I,
            "100110 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    int address = cpuState.getReg(statement.rs) + statement.imm;
                    int result = cpuState.getReg(statement.rt);
                    for (int i = 0; i <= 3 - (address % 4); i++) {
                        result = Format.setByte(result, i, memory.loadUnsigned8(address + i));
                    }
                    cpuState.setReg(statement.rt, result);
                }
            });
    public static final TxInstruction swInstruction = new TxInstruction("sw", "j, s(i)", "sw $t1,-100($t2)",
            "Store word : Store contents of $t1 into effective memory word address",
            TxInstruction.Format.I,
            "101011 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    memory.store32(cpuState.getReg(statement.rs) + statement.imm, cpuState.getReg(statement.rt));
                }
            });
    public static final TxInstruction swlInstruction = new TxInstruction("swl", "j, s(i)", "swl $t1,-100($t2)",
            "Store word left : Store high-order 1 to 4 bytes of $t1 into memory, starting with effective byte address and continuing through the low-order byte of its word",
            TxInstruction.Format.I,
            "101010 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    int address = cpuState.getReg(statement.rs) + statement.imm;
                    int source = cpuState.getReg(statement.rt);
                    for (int i = 0; i <= address % 4; i++) {
                        memory.store8(address - i, Format.getByte(source, 3 - i));
                    }
                }
            });
    public static final TxInstruction swrInstruction = new TxInstruction("swr", "j, s(i)", "swr $t1,-100($t2)",
            "Store word right : Store low-order 1 to 4 bytes of $t1 into memory, starting with high-order byte of word containing effective byte address and continuing through that byte address",
            TxInstruction.Format.I,
            "101110 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // todo check sign extension of offset
                    int address = cpuState.getReg(statement.rs) + statement.imm;
                    int source = cpuState.getReg(statement.rt);
                    for (int i = 0; i <= 3 - (address % 4); i++) {
                        memory.store8(address + i, Format.getByte(source, i));
                    }
                }
            });
    public static final TxInstruction luiInstruction = new TxInstruction("lui", "j, u", "lui $t1,100", // todo disassemble with << 16
            "Load upper immediate : Set high-order 16 bits of $t1 to 16-bit immediate and low-order 16 bits to 0",
            TxInstruction.Format.I,
            "001111 00000 fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt, statement.imm << 16);
                }
            });
    public static final TxInstruction beqInstruction = new TxInstruction("beq", "i, j, 4ru", "beq $t1,$t2,label",
            "Branch if equal : Branch to statement at label's address if $t1 and $t2 are equal",
            TxInstruction.Format.I_BRANCH,
            "000100 fffff sssss tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) == cpuState.getReg(statement.rt)) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    public static final TxInstruction bneInstruction = new TxInstruction("bne", "i, j, 4ru", "bne $t1,$t2,label",
            "Branch if not equal : Branch to statement at label's address if $t1 and $t2 are not equal",
            TxInstruction.Format.I_BRANCH,
            "000101 fffff sssss tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) != cpuState.getReg(statement.rt)) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    /* alternate mnemonic when Rt is 0 */
    public static final TxInstruction bnezInstruction = new TxInstruction("bnez", "i, 4ru", "bnez $t1,label",
            "Branch if Not Equal Zero : Branch to statement at label's address if $t1 is not zero",
            TxInstruction.Format.I_BRANCH,
            "000101 fffff 00000 tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) != 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    public static final TxInstruction bgezInstruction = new TxInstruction("bgez", "i, 4rs", "bgez $t1,label",
            "Branch if greater than or equal to zero : Branch to statement at label's address if $t1 is greater than or equal to zero",
            TxInstruction.Format.I_BRANCH,
            "000001 fffff 00001 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) >= 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });

    public static final TxInstruction bgezalInstruction = new TxInstruction("bgezal", "i, 4rs", "bgezal $t1,label",
            "Branch if greater then or equal to zero and link : If $t1 is greater than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            TxInstruction.Format.I_BRANCH,
            "000001 fffff 10001 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) >= 0) {
                        cpuState.setReg(TxCPUState.RA, cpuState.pc + 8);
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    public static final TxInstruction bgtzInstruction = new TxInstruction("bgtz", "i, 4rs", "bgtz $t1,label",
            "Branch if greater than zero : Branch to statement at label's address if $t1 is greater than zero",
            TxInstruction.Format.I_BRANCH,
            "000111 fffff 00000 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) > 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    public static final TxInstruction blezInstruction = new TxInstruction("blez", "i, 4rs", "blez $t1,label",
            "Branch if less than or equal to zero : Branch to statement at label's address if $t1 is less than or equal to zero",
            TxInstruction.Format.I_BRANCH,
            "000110 fffff 00000 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) <= 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    public static final TxInstruction bltzInstruction = new TxInstruction("bltz", "i, 4rs", "bltz $t1,label",
            "Branch if less than zero : Branch to statement at label's address if $t1 is less than zero",
            TxInstruction.Format.I_BRANCH,
            "000001 fffff 00000 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) < 0) {
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    public static final TxInstruction bltzalInstruction = new TxInstruction("bltzal", "i, 4rs", "bltzal $t1,label",
            "Branch if less than zero and link : If $t1 is less than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
            TxInstruction.Format.I_BRANCH,
            "000001 fffff 10000 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) < 0) {
                        cpuState.setReg(TxCPUState.RA, cpuState.pc + 8); // the "and link" part
                        cpuState.pc = cpuState.pc + 4 + (statement.imm << 16 >> 14); // sign extend and x4
                    }
                }
            });
    public static final TxInstruction sltInstruction = new TxInstruction("slt", "k, i, j", "slt $t1,$t2,$t3",
            "Set on Less Than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 101010",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, (cpuState.getReg(statement.rs) < cpuState.getReg(statement.rt)) ? 1 : 0);
                }
            });
    public static final TxInstruction sltuInstruction = new TxInstruction("sltu", "k, i, j", "sltu $t1,$t2,$t3",
            "Set on Less Than Unsigned : If $t2 is less than $t3 using unsigned comparision, then set $t1 to 1 else set $t1 to 0",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 101011",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs);
                    int second = cpuState.getReg(statement.rt);
                    if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                        cpuState.setReg(statement.rd, (first < second) ? 1 : 0);
                    } else {
                        cpuState.setReg(statement.rd, (first >= 0) ? 1 : 0);
                    }
                }
            });
    public static final TxInstruction sltiInstruction = new TxInstruction("slti", "j, i, s", "slti $t1,$t2,-100",
            "Set less than immediate : If $t2 is less than sign-extended 16-bit immediate, then set $t1 to 1 else set $t1 to 0",
            TxInstruction.Format.I,
            "001010 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // 16 bit immediate value in statement.imm is sign-extended
                    cpuState.setReg(statement.rt, (cpuState.getReg(statement.rs) < (statement.imm << 16 >> 16)) ? 1 : 0);
                }
            });
    public static final TxInstruction sltiuInstruction = new TxInstruction("sltiu", "j, i, imm", "sltiu $t1,$t2,-100",
            "Set less than immediate unsigned : If $t2 is less than  sign-extended 16-bit immediate using unsigned comparison, then set $t1 to 1 else set $t1 to 0",
            TxInstruction.Format.I,
            "001011 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                        cpuState.setReg(statement.rt, (first < second) ? 1 : 0);
                    } else {
                        cpuState.setReg(statement.rt, (first >= 0) ? 1 : 0);
                    }
                }
            });
    public static final TxInstruction movnInstruction = new TxInstruction("movn", "k, i, j", "movn $t1,$t2,$t3",
            "MOVe conditional on Non zero : Set $t1 to $t2 if $t3 is not zero",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 001011",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt) != 0) {
                        cpuState.setReg(statement.rd, cpuState.getReg(statement.rs));
                    }
                }
            });
    public static final TxInstruction movzInstruction = new TxInstruction("movz", "k, i, j", "movz $t1,$t2,$t3",
            "MOVe conditional on Zero : Set $t1 to $t2 if $t3 is zero",
            TxInstruction.Format.R,
            "000000 sssss ttttt fffff 00000 001010",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rt) == 0) {
                        cpuState.setReg(statement.rd, cpuState.getReg(statement.rs));
                    }
                }
            });
    public static final TxInstruction breakInstruction = new TxInstruction("break", "u", "break 100",
            "Break execution with code : Terminate program execution with specified exception code",
            TxInstruction.Format.BREAK,
            "000000 ffffffffffffffffffff 001101",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // so will just halt execution with a message.
                    throw new TxEmulationException(statement, "break instruction executed; code = " +
                            statement.imm + ".", Exceptions.BREAKPOINT_EXCEPTION);
                }
            });
    public static final TxInstruction jInstruction = new TxInstruction("j", "4ru", "j target",
            "Jump unconditionally : Jump to statement at target address",
            TxInstruction.Format.J,
            "000010 ffffffffffffffffffffffffff",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.pc = (cpuState.pc & 0xF0000000) | (statement.imm << 2);
                }
            });
    public static final TxInstruction jrInstruction = new TxInstruction("jr", "i", "jr $t1",
            "Jump register unconditionally : Jump to statement whose address is in $t1",
            TxInstruction.Format.R,
            "000000 fffff 00000 00000 00000 001000",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.pc = cpuState.getReg(statement.rs);
                }
            });
    public static final TxInstruction jalInstruction = new TxInstruction("jal", "4ru", "jal target",
            "Jump and link : Set $ra to Program Counter (return address) then jump to statement at target address",
            TxInstruction.Format.J,
            "000011 ffffffffffffffffffffffffff",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(TxCPUState.RA, cpuState.pc + 8);
                    cpuState.pc = (cpuState.pc & 0xF0000000) | (statement.imm << 2);
                }
            });
    public static final TxInstruction jalrInstruction = new TxInstruction("jalr", "(k,) i", "jalr $t1,$t2", // special case : print short version if $t2 is RA
            "Jump And Link Register : Set $t1 to Program Counter (return address) then jump to statement whose address is in $t2",
            TxInstruction.Format.R,
            "000000 sssss 00000 fffff 00000 001001",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.pc + 8);
                    cpuState.pc = cpuState.getReg(statement.rs);
                }
            });
    public static final TxInstruction cloInstruction = new TxInstruction("clo", "k, i", "clo $t1,$t2",
            "Count number of Leading Ones : Set $t1 to the count of leading one bits in $t2 starting at most significant bit position",
            TxInstruction.Format.R,
            // MIPS32 requires rd (first) operand to appear twice in machine code.
            // It has to be same as rt (third) operand in machine code, but the
            // source statement does not have or permit third operand.
            // In the machine code, rd and rt are adjacent, but my mask
            // substitution cannot handle adjacent placement of the same source
            // operand (e.g. "... sssss fffff fffff ...") because it would interpret
            // the mask to be the total length of both (10 bits).  I could code it
            // to have 3 marsOperands then define a pseudo-instruction of two marsOperands
            // to translate into this, but then both would show up in instruction set
            // list and I don't want that.  So I will use the convention of Computer
            // Organization and Design 3rd Edition, Appendix A, and code the rt bits
            // as 0's.  The generated code does not match SPIM and would not run
            // on a real MIPS machine but since I am providing no means of storing
            // the binary code that is not really an issue.
            "011100 sssss 00000 fffff 00000 100001",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int value = cpuState.getReg(statement.rs);
                    int leadingOnes = 0;
                    int bitPosition = 31;
                    while (Format.bitValue(value, bitPosition) == 1 && bitPosition >= 0) {
                        leadingOnes++;
                        bitPosition--;
                    }
                    cpuState.setReg(statement.rd, leadingOnes);
                }
            });
    public static final TxInstruction clzInstruction = new TxInstruction("clz", "k, i", "clz $t1,$t2",
            "Count number of leading zeroes : Set $t1 to the count of leading zero bits in $t2 starting at most significant bit positio",
            TxInstruction.Format.R,
            // See comments for "clo" instruction above.  They apply here too.
            "011100 sssss 00000 fffff 00000 100000",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int value = cpuState.getReg(statement.rs);
                    int leadingZeros = 0;
                    int bitPosition = 31;
                    while (Format.bitValue(value, bitPosition) == 0 && bitPosition >= 0) {
                        leadingZeros++;
                        bitPosition--;
                    }
                    cpuState.setReg(statement.rd, leadingZeros);
                }
            });

    public static final TxInstruction mfc0Instruction = new TxInstruction("mfc0", "j, k", "mfc0 $t1,$8",
            "Move from Coprocessor 0 : Set $t1 to the value stored in Coprocessor 0 register $8",
            TxInstruction.Format.CP,
            "010000 00000 fffff sssss 00000000 eee",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt, cpuState.getReg(statement.rd));
                }
            });

    public static final TxInstruction mtc0Instruction = new TxInstruction("mtc0", "j, k", "mtc0 $t1,$8",
            "Move to Coprocessor 0 : Set Coprocessor 0 register $8 to value stored in $t1",
            TxInstruction.Format.CP,
            "010000 00100 fffff sssss 00000000 eee",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rd, cpuState.getReg(statement.rt)) ;
                }
            });

    public static final TxInstruction teqInstruction = new TxInstruction("teq", "i, j, u", "teq $t1,$t2,$t3",
            "Trap if EQual : Trap with code $t3 if $t1 is equal to $t2",
            TxInstruction.Format.TRAP,
            "000000 fffff sssss 00000 00000 110100",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) == cpuState.getReg(statement.rt)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction teqiInstruction = new TxInstruction("teqi", "i, s", "teqi $t1,-100",
            "Trap if EQual to Immediate : Trap if $t1 is equal to sign-extended 16 bit immediate",
            TxInstruction.Format.I,
            "000001 fffff 01100 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) == (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tneInstruction = new TxInstruction("tne", "i, j, u", "tne $t1,$t2",
            "Trap if not equal : Trap if $t1 is not equal to $t2",
            TxInstruction.Format.TRAP,
            "000000 fffff sssss 00000 00000 110110",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) != cpuState.getReg(statement.rt)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tneiInstruction = new TxInstruction("tnei", "i, s", "tnei $t1,-100",
            "Trap if not equal to immediate : Trap if $t1 is not equal to sign-extended 16 bit immediate",
            TxInstruction.Format.I,
            "000001 fffff 01110 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) != (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tgeInstruction = new TxInstruction("tge", "i, j, u", "tge $t1,$t2",
            "Trap if greater or equal : Trap if $t1 is greater than or equal to $t2",
            TxInstruction.Format.TRAP,
            "000000 fffff sssss 00000 00000 110000",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) >= cpuState.getReg(statement.rt)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tgeuInstruction = new TxInstruction("tgeu", "i, j, u", "tgeu $t1,$t2",
            "Trap if greater or equal unsigned : Trap if $t1 is greater than or equal to $t2 using unsigned comparision",
            TxInstruction.Format.TRAP,
            "000000 fffff sssss 00000 00000 110001",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs);
                    int second = cpuState.getReg(statement.rt);
                    // if signs same, do straight compare; if signs differ & first negative then first greater else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first >= second) : (first < 0)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tgeiInstruction = new TxInstruction("tgei", "i, s", "tgei $t1,-100",
            "Trap if greater than or equal to immediate : Trap if $t1 greater than or equal to sign-extended 16 bit immediate",
            TxInstruction.Format.I,
            "000001 fffff 01000 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) >= (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tgeiuInstruction = new TxInstruction("tgeiu", "i, s", "tgeiu $t1,-100",
            "Trap if greater or equal to immediate unsigned : Trap if $t1 greater than or equal to sign-extended 16 bit immediate, unsigned comparison",
            TxInstruction.Format.I,
            "000001 fffff 01001 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    // if signs same, do straight compare; if signs differ & first negative then first greater else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first >= second) : (first < 0)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tltInstruction = new TxInstruction("tlt", "i, j, u", "tlt $t1,$t2",
            "Trap if less than: Trap if $t1 less than $t2",
            TxInstruction.Format.TRAP,
            "000000 fffff sssss 00000 00000 110010",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) < cpuState.getReg(statement.rt)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tltuInstruction = new TxInstruction("tltu", "i, j, u", "tltu $t1,$t2",
            "Trap if less than unsigned : Trap if $t1 less than $t2, unsigned comparison",
            TxInstruction.Format.TRAP,
            "000000 fffff sssss 00000 00000 110011",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs);
                    int second = cpuState.getReg(statement.rt);
                    // if signs same, do straight compare; if signs differ & first positive then first is less else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first < second) : (first >= 0)) {
                        throw new TxEmulationException(statement, "trap with code " + statement.imm, Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tltiInstruction = new TxInstruction("tlti", "i, s", "tlti $t1,-100",
            "Trap if less than immediate : Trap if $t1 less than sign-extended 16-bit immediate",
            TxInstruction.Format.I,
            "000001 fffff 01010 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    if (cpuState.getReg(statement.rs) < (statement.imm << 16 >> 16)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction tltiuInstruction = new TxInstruction("tltiu", "i, s", "tltiu $t1,-100",
            "Trap if less than immediate unsigned : Trap if $t1 less than sign-extended 16-bit immediate, unsigned comparison",
            TxInstruction.Format.I,
            "000001 fffff 01011 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    int first = cpuState.getReg(statement.rs);
                    // 16 bit immediate value in statement.imm is sign-extended
                    int second = statement.imm << 16 >> 16;
                    // if signs same, do straight compare; if signs differ & first positive then first is less else second
                    if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first < second) : (first >= 0)) {
                        throw new TxEmulationException(statement, "trap", Exceptions.TRAP_EXCEPTION);
                    }
                }
            });
    public static final TxInstruction lbInstruction = new TxInstruction("lb", "j, s(i)", "lb $t1,-100($t2)",
        "Load byte : Set $t1 to sign-extended 8-bit value from effective memory byte address",
        TxInstruction.Format.I,
        "100000 ttttt fffff ssssssssssssssss",
        new SimulationCode() {
            public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                cpuState.setReg(statement.rt,
                        // TODO check precedence
                        memory.loadUnsigned8(
                                cpuState.getReg(statement.rs) + (statement.imm << 16 >> 16))
                                << 24
                                >> 24);
            }
        });
    public static final TxInstruction lhInstruction = new TxInstruction("lh", "j, s(i)", "lh $t1,-100($t2)",
            "Load halfword : Set $t1 to sign-extended 16-bit value from effective memory halfword address",
            TxInstruction.Format.I,
            "100001 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt,
                            // TODO check precedence
                            // TODO check load/un/signed ?
                            memory.loadUnsigned16(
                                    cpuState.getReg(statement.rs) + (statement.imm << 16 >> 16))
                                    << 16
                                    >> 16);
                }
            });
    public static final TxInstruction lhuInstruction = new TxInstruction("lhu", "j, s(i)", "lhu $t1,-100($t2)",
            "Load halfword unsigned : Set $t1 to zero-extended 16-bit value from effective memory halfword address",
            TxInstruction.Format.I,
            "100101 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    // offset is sign-extended and loaded halfword value is zero-extended
                    cpuState.setReg(statement.rt,
                            // TODO check load/un/signed ?
                            memory.loadUnsigned16(
                                    cpuState.getReg(statement.rs) + (statement.imm << 16 >> 16))
                                    & 0x0000ffff);
                }
            });
    public static final TxInstruction lbuInstruction = new TxInstruction("lbu", "j, s(i)", "lbu $t1,-100($t2)",
            "Load byte unsigned : Set $t1 to zero-extended 8-bit value from effective memory byte address",
            TxInstruction.Format.I,
            "100100 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    cpuState.setReg(statement.rt,
                            // TODO check load/un/signed ?
                            memory.loadUnsigned8(
                                    cpuState.getReg(statement.rs) + (statement.imm << 16 >> 16))
                                    & 0x000000ff);
                }
            });
    public static final TxInstruction sbInstruction = new TxInstruction("sb", "j, s(i)", "sb $t1,-100($t2)",
            "Store byte : Store the low-order 8 bits of $t1 into the effective memory byte address",
            TxInstruction.Format.I,
            "101000 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    memory.store8(
                            cpuState.getReg(statement.rs) + (statement.imm << 16 >> 16),
                            cpuState.getReg(statement.rt) & 0x000000ff);
                }
            });
    public static final TxInstruction shInstruction = new TxInstruction("sh", "j, s(i)", "sh $t1,-100($t2)",
            "Store halfword : Store the low-order 16 bits of $t1 into the effective memory halfword address",
            TxInstruction.Format.I,
            "101001 ttttt fffff ssssssssssssssss",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    memory.store16(
                            cpuState.getReg(statement.rs) + (statement.imm << 16 >> 16),
                            cpuState.getReg(statement.rt) & 0x0000ffff);
                }
            });
    public static final TxInstruction syncInstruction = new TxInstruction("sync", "", "sync",
            "Sync : Wait for all operations to complete",
            TxInstruction.Format.I,
            "000000 00000000000000000000 001111",
            new SimulationCode() {
                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
                    /* nop. Simulator does not have any pipeline */
                }
            });
    public static final TxInstruction waitInstruction = new TxInstruction("wait", "", "wait",
            "Wait : put the processor in stand-by",
            TxInstruction.Format.I,
            "010000 1 0000000000000000000 100000",
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

//    public static final TxInstruction eretInstruction = new TxInstruction("eret",
//            "Exception return : Set Program Counter to Coprocessor 0 EPC register value, set Coprocessor Status register bit 1 (exception level) to zero",
//            TxInstruction.Format.R,
//            "010000 1 0000000000000000000 011000",
//            new SimulationCode() {
//                public void simulate(TxStatement statement, TxCPUState cpuState, Memory memory) throws EmulationException {
//                    // set EXL bit (bit 1) in Status register to 0 and set PC to EPC
//                    Coprocessor0.updateRegister(Coprocessor0.STATUS,
//                            Format.clearBit(Coprocessor0.getValue(Coprocessor0.STATUS), Coprocessor0.EXCEPTION_LEVEL));
//                    RegisterFile.setProgramCounter(Coprocessor0.getValue(Coprocessor0.EPC));
//                }
//            });

//    private ArrayList<TxInstruction> instructionList;
//    private SyscallLoader syscallLoader;
//
//    /**
//     * Creates a new InstructionSet object.
//     */
//    public TxInstructionSet()
//    {
//        instructionList = new ArrayList<TxInstruction>();
//
//    }
//    /**
//     * Retrieve the current instruction set.
//     */
//    public ArrayList getInstructionList()
//    {
//        return instructionList;
//
//    }
//    void populate() {
//        instructionList.add(
//                new TxInstruction("syscall",
//                        "Issue a system call : Execute the system call specified by value in $v0",
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
//
//    /*  METHOD TO ADD PSEUDO-INSTRUCTIONS


//        }
//            in = new BufferedReader(new InputStreamReader(is));
//            is = this.getClass().getResourceAsStream("/PseudoOps.txt");
//            // leading "/" prevents package name being prepended to filepath.
//        {
//        try
//        BufferedReader in = null;
//        InputStream is = null;
//    {
//    private void addPseudoInstructions()
//
//    */

//    /**
//     *  Given an operator mnemonic, will return the corresponding TxInstruction object(s)
//     *  from the instruction set.  Uses straight linear search technique.
//     *  @param name operator mnemonic (e.g. addi, sw,...)
//     *  @return list of corresponding TxInstruction object(s), or null if not found.
//     */
//    public ArrayList matchOperator(String name)
//    {
//        ArrayList matchingInstructions = null;
//        // Linear search for now....
//        for (int i = 0; i < instructionList.size(); i++)
//        {
//            if (((TxInstruction) instructionList.get(i)).getName().equalsIgnoreCase(name))
//            {
//                if (matchingInstructions == null)
//                    matchingInstructions = new ArrayList();
//                matchingInstructions.add(instructionList.get(i));
//            }
//        }
//        return matchingInstructions;
//    }
//
//
//    /**
//     *  Given a string, will return the TxInstruction object(s) from the instruction
//     *  set whose operator mnemonic prefix matches it.  Case-insensitive.  For example
//     *  "s" will match "sw", "sh", "sb", etc.  Uses straight linear search technique.
//     *  @param name a string
//     *  @return list of matching TxInstruction object(s), or null if none match.
//     */
//    public ArrayList prefixMatchOperator(String name)
//    {
//        ArrayList matchingInstructions = null;
//        // Linear search for now....
//        if (name != null) {
//            for (int i = 0; i < instructionList.size(); i++)
//            {
//                if (((TxInstruction) instructionList.get(i)).getName().toLowerCase().startsWith(name.toLowerCase()))
//                {
//                    if (matchingInstructions == null)
//                        matchingInstructions = new ArrayList();
//                    matchingInstructions.add(instructionList.get(i));
//                }
//            }
//        }
//        return matchingInstructions;
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

    public static TxInstruction getInstructionForStatement(int binStatement) throws DisassemblyException {
        return opcodeResolver.resolve(binStatement);
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

    private static InstructionResolver opcodeResolver = new InstructionResolver() {
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

    private static InstructionResolver cop0CoFunctionResolver = new InstructionResolver() {
        @Override
        public TxInstruction resolve(int binStatement) throws ReservedInstructionException {
            return cop0CoFunctionResolvers[binStatement & 0b111111].resolve(binStatement);
        }
    };


    static {
        // These are rewrites of the Toshiba archtecture document, appendix E

        // Encoding of the Opcode Field
        opcodeResolvers = new InstructionResolver[64];

        opcodeResolvers[0b000000] = specialFunctionResolver;
        opcodeResolvers[0b000001] = regImmRtResolver;
        opcodeResolvers[0b000010] = new DirectInstructionResolver(jInstruction);
        opcodeResolvers[0b000011] = new DirectInstructionResolver(jalInstruction);
        opcodeResolvers[0b000100] = new DirectInstructionResolver(beqInstruction);
        opcodeResolvers[0b000101] = new DirectInstructionResolver(bneInstruction);
        if (true/*OptionAltInstructions*/) {
            opcodeResolvers[0b000101] = bneOrBnezResolver;
        }
        opcodeResolvers[0b000110] = new DirectInstructionResolver(blezInstruction);
        opcodeResolvers[0b000111] = new DirectInstructionResolver(bgtzInstruction);

        opcodeResolvers[0b001000] = new DirectInstructionResolver(addiInstruction);
        opcodeResolvers[0b001001] = new DirectInstructionResolver(addiuInstruction);
        opcodeResolvers[0b001010] = new DirectInstructionResolver(sltiInstruction);
        opcodeResolvers[0b001011] = new DirectInstructionResolver(sltiuInstruction);
        opcodeResolvers[0b001100] = new DirectInstructionResolver(andiInstruction);
        opcodeResolvers[0b001101] = new DirectInstructionResolver(oriInstruction);
        opcodeResolvers[0b001110] = new DirectInstructionResolver(xoriInstruction);
        opcodeResolvers[0b001111] = new DirectInstructionResolver(luiInstruction);

        opcodeResolvers[0b010000] = cop0RsResolver;
        opcodeResolvers[0b010001] = /*COP1*/ thetaResolver;
        opcodeResolvers[0b010010] = /*COP2*/ thetaResolver;
        opcodeResolvers[0b010011] = /*COP3*/ thetaResolver;
        opcodeResolvers[0b010100] = unimplementedResolver; // new DirectInstructionResolver(beqlInstruction);
        opcodeResolvers[0b010101] = unimplementedResolver; // new DirectInstructionResolver(bnelInstruction);
        opcodeResolvers[0b010110] = new DirectInstructionResolver(blezInstruction);
        opcodeResolvers[0b010111] = new DirectInstructionResolver(bgtzInstruction);

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
        opcodeResolvers[0b110001] = /*LWC1*/ betaResolver;
        opcodeResolvers[0b110010] = /*LWC2*/ betaResolver;
        opcodeResolvers[0b110011] = /*PREF*/ betaResolver;
        opcodeResolvers[0b110100] = betaResolver;
        opcodeResolvers[0b110101] = /*LDC1*/ betaResolver;
        opcodeResolvers[0b110110] = /*LDC2*/ betaResolver;
        opcodeResolvers[0b110111] = betaResolver;

        opcodeResolvers[0b111000] = /*SC*/ betaResolver;
        opcodeResolvers[0b111001] = /*SWC1*/ betaResolver;
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
        specialFunctionResolvers[0b100010] = new DirectInstructionResolver(subInstruction);
        specialFunctionResolvers[0b100011] = new DirectInstructionResolver(subuInstruction);
        specialFunctionResolvers[0b100100] = new DirectInstructionResolver(andInstruction);
        specialFunctionResolvers[0b100101] = new DirectInstructionResolver(orInstruction);
        if (true/*OptionAltInstructions*/) {
            // TODO liOrOrResolver "or $k1, 0, 0x00D2" > "li $k1, 0x00D2"
            specialFunctionResolvers[0b100101] = new DirectInstructionResolver(orInstruction);
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
        regImmRtResolvers[0b00010] = unimplementedResolver; // new DirectInstructionResolver(bltzlInstruction);
        regImmRtResolvers[0b00011] = unimplementedResolver; // new DirectInstructionResolver(bgezlInstruction);
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
        regImmRtResolvers[0b10010] = unimplementedResolver; // new DirectInstructionResolver(bltzallInstruction);
        regImmRtResolvers[0b10011] = unimplementedResolver; // new DirectInstructionResolver(bgezallInstruction);
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

        cop0CoFunctionResolvers[0b011000] = unimplementedResolver; // new DirectInstructionResolver(eretInstruction);
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

    }

}