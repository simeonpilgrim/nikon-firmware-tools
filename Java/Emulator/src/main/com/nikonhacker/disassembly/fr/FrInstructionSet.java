package com.nikonhacker.disassembly.fr;


import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.emu.EmulationException;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;

import java.util.EnumSet;
import java.util.Set;

public class FrInstructionSet {

    /**
     * These are specific timings used in instruction execution time computation.
     * For the time being, these are considered constants
     */
    public static final int CYCLES_A = 1; /* Memory access cycles, may be increased by "Ready" function. */
    public static final int CYCLES_B = 1; /* Memory access cycles, may be increased by "Ready" function. Note that if the next
                                            instruction references a register involved in a "LD" operation an interlock will be applied,
                                            increasing the number of execution cycles from 1 cycle to 2 cycles. */
    public static final int CYCLES_C = 1; /* If the instruction immediately after is a read or write operation involving register "R15", or
                                            the "SSP" or "USP" pointers, or the instruction format is TYPE-A, an interlock will be
                                            applied, increasing the number of execution cycles from 1 cycle to 2 cycles. */
    public static final int CYCLES_D = 1; /* If the instruction immediately after references the "MDH/MDL" register, interlock will be
                                            applied, increasing the number of execution cycles from 1 cycle to 2 cycles.
                                            When dedicated register such as TBR, RP, USP, SSP, MDH, and MDL is accessed with ST
                                            Rs, @-R15 command just after DIV1 command, an interlock is always brought, increasing
                                            the number of execution cycles from 1 cycle to 2 cycles. */

    public static final SimulationCode dmovbR13Dir8SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.memory.store8(statement.imm, context.cpuState.getReg(13));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode dmovhR13Dir9SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.memory.store16(statement.imm * 2, context.cpuState.getReg(13));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode dmovR13Dir10SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.memory.store32(statement.imm * 4, context.cpuState.getReg(13));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode stm1SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            int n = 0;
            if ((statement.imm & 0x1) != 0) {
                context.cpuState.setReg(15, context.cpuState.getReg(15) - 4);
                                /*special case for R15: value stored is R15 before it was decremented */
                context.memory.store32(context.cpuState.getReg(15), context.cpuState.getReg(15) + 4);
                n++;
            }
            for (int r = 1; r <= 7; r++) {
                if ((statement.imm & (1 << r)) != 0) {
                    context.cpuState.setReg(15, context.cpuState.getReg(15) - 4);
                    context.memory.store32(context.cpuState.getReg(15), context.cpuState.getReg((7 - r) + 8));
                    n++;
                }
            }
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A * n + 1;
        }
    };
    public static final SimulationCode stm0SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            int n = 0;
            for (int r = 0; r <= 7; r++) {
                if ((statement.imm & (1 << r)) != 0) {
                    context.cpuState.setReg(15, context.cpuState.getReg(15) - 4);
                    context.memory.store32(context.cpuState.getReg(15), context.cpuState.getReg(7 - r));
                    n++;
                }
            }
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A * n + 1;
        }
    };
    public static final SimulationCode dmovR15Dir10SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.memory.store32(statement.imm * 4, context.memory.load32(context.cpuState.getReg(15)));
            context.cpuState.setReg(15, context.cpuState.getReg(15) + 4);
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = 2 * FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode stPsR15SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(15, context.cpuState.getReg(15) - 4);
            context.memory.store32(context.cpuState.getReg(15), ((FrCPUState) context.cpuState).getPS());
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode stRiR15SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(15, context.cpuState.getReg(15) - 4);
            if (statement.ri_rs_fs == 15) {
                    /*special case for R15: value stored is R15 before it was decremented */
                context.memory.store32(context.cpuState.getReg(15), context.cpuState.getReg(15) + 4);
            }
            else {
                context.memory.store32(context.cpuState.getReg(15), context.cpuState.getReg(statement.ri_rs_fs));
            }
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode dmovDir10R15SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(15, context.cpuState.getReg(15) - 4);
            context.memory.store32(context.cpuState.getReg(15), context.memory.load32(statement.imm * 4));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = 2 * FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode stRsR15SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(15, context.cpuState.getReg(15) - 4);
            context.memory.store32(context.cpuState.getReg(15), context.cpuState.getReg(FrCPUState.DEDICATED_REG_OFFSET + statement.ri_rs_fs));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode dmovbDir8R13SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(13, context.memory.loadUnsigned8(statement.imm));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_B;
        }
    };
    public static final SimulationCode dmovhDir9R13SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(13, context.memory.loadUnsigned16(statement.imm * 2));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_B;
        }
    };
    public static final SimulationCode dmovDir10R13SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(13, context.memory.load32(statement.imm * 4));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_B;
        }
    };
    public static final SimulationCode ldm1SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            int n = 0;
            for (int r = 0; r <= 7; r++) {
                if ((statement.imm & (1 << r)) != 0) {
                    context.cpuState.setReg(r + 8, context.memory.load32(context.cpuState.getReg(15)));
                    context.cpuState.setReg(15, context.cpuState.getReg(15) + 4);
                    n++;
                }
            }
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = (n == 0) ? 1 : FrInstructionSet.CYCLES_A * (n - 1) + FrInstructionSet.CYCLES_B + 1;
        }
    };
    public static final SimulationCode ldm0SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            int n = 0;
            for (int r = 0; r <= 7; r++) {
                if ((statement.imm & (1 << r)) != 0) {
                    context.cpuState.setReg(r, context.memory.load32(context.cpuState.getReg(15)));
                    context.cpuState.setReg(15, context.cpuState.getReg(15) + 4);
                    n++;
                }
            }
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = (n == 0) ? 1 : (FrInstructionSet.CYCLES_A * (n - 1) + FrInstructionSet.CYCLES_B + 1);
        }
    };
    public static final SimulationCode ldR15PSSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            ((FrCPUState) context.cpuState).setPS(context.memory.load32(context.cpuState.getReg(15)), true);
            context.cpuState.setReg(15, context.cpuState.getReg(15) + 4);
                /* NZVC is part of the PS !*/
            context.cpuState.pc += 2;
            context.cycleIncrement = 1 + FrInstructionSet.CYCLES_A + FrInstructionSet.CYCLES_B;
        }
    };
    public static final SimulationCode orccrU8SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            ((FrCPUState) context.cpuState).setCCR(((FrCPUState) context.cpuState).getCCR() | statement.imm);
                /* NZVC is part of the CCR !*/
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_C;
        }
    };
    public static final SimulationCode andccrU8SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            ((FrCPUState) context.cpuState).setCCR(((FrCPUState) context.cpuState).getCCR() & statement.imm);
                /* NZVC is part of the CCR !*/
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_C;
        }
    };
    public static final SimulationCode movRiRsSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(FrCPUState.DEDICATED_REG_OFFSET + statement.rj_rt_ft, context.cpuState.getReg(statement.ri_rs_fs));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = 1;
        }
    };
    public static final SimulationCode movRsRiSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(statement.ri_rs_fs, context.cpuState.getReg(FrCPUState.DEDICATED_REG_OFFSET + statement.rj_rt_ft));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = 1;
        }
    };
    public static final SimulationCode lsr2u4RiSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            int result32 = context.cpuState.getReg(statement.ri_rs_fs) >>> (statement.imm + 16);

            ((FrCPUState) context.cpuState).N = 0;
            ((FrCPUState) context.cpuState).Z = (result32 == 0) ? 1 : 0;
            ((FrCPUState) context.cpuState).C = (context.cpuState.getReg(statement.ri_rs_fs) >> (statement.imm + 15)) & 1;
            context.cpuState.setReg(statement.ri_rs_fs, result32);
            context.cpuState.pc += 2;
            context.cycleIncrement = 1;
        }
    };
    public static final SimulationCode lsl2u4RiSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) << (statement.imm + 16);

            ((FrCPUState) context.cpuState).N = (int) ((result64 & 0x80000000L) >>> 31);
            ((FrCPUState) context.cpuState).Z = (result64 == 0) ? 1 : 0;
            ((FrCPUState) context.cpuState).C = (int) ((result64 & 0x100000000L) >>> 32);
            context.cpuState.setReg(statement.ri_rs_fs, (int) result64);
            context.cpuState.pc += 2;
            context.cycleIncrement = 1;
        }
    };
    public static final SimulationCode asr2u4RiSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            int result32 = context.cpuState.getReg(statement.ri_rs_fs) >> (statement.imm + 16);

            ((FrCPUState) context.cpuState).N = (result32 & 0x80000000) >>> 31;
            ((FrCPUState) context.cpuState).Z = (result32 == 0) ? 1 : 0;
            ((FrCPUState) context.cpuState).C = (context.cpuState.getReg(statement.ri_rs_fs) >> (statement.imm + 15)) & 1;
            context.cpuState.setReg(statement.ri_rs_fs, result32);
            context.cpuState.pc += 2;
            context.cycleIncrement = 1;
        }
    };
    public static final SimulationCode stilmU8SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            ((FrCPUState) context.cpuState).setILM(statement.imm, true);
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = 1;
        }
    };
    public static final SimulationCode addspS10SimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(15, context.cpuState.getReg(15) + (BinaryArithmetics.signExtend(8, statement.imm) * 4));
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = 1;
        }
    };
    public static final SimulationCode ldSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(statement.ri_rs_fs, context.memory.load32(context.cpuState.getReg(statement.rj_rt_ft)));
            /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_B;
        }
    };
    public static final SimulationCode lduhSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(statement.ri_rs_fs, context.memory.loadUnsigned16(context.cpuState.getReg(statement.rj_rt_ft)));
            /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_B;
        }
    };
    public static final SimulationCode ldubSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(statement.ri_rs_fs, context.memory.loadUnsigned8(context.cpuState.getReg(statement.rj_rt_ft)));
            /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_B;
        }
    };
    public static final SimulationCode stSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.memory.store32(context.cpuState.getReg(statement.rj_rt_ft), context.cpuState.getReg(statement.ri_rs_fs));
            /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode sthSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.memory.store16(context.cpuState.getReg(statement.rj_rt_ft), context.cpuState.getReg(statement.ri_rs_fs));
            /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A;
        }
    };
    public static final SimulationCode stbSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.memory.store8(context.cpuState.getReg(statement.rj_rt_ft), context.cpuState.getReg(statement.ri_rs_fs));
            /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_A;
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
    public static FrInstruction[] instructionMap = new FrInstruction[0x10000];


    /**
     * This is a "catch-all" instruction used as a safety net for unknown instructions
     */
    public static final FrInstruction defaultInstruction = new FrInstruction(InstructionFormat.W, 0, 0, "UNK", "", "", Instruction.FlowType.NONE, false, false, null);


    public static final SimulationCode ldR15RiSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(statement.ri_rs_fs, context.memory.load32(context.cpuState.getReg(15)));
            context.cpuState.setReg(15, context.cpuState.getReg(15) + 4);
                /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_B;
        }
    };

    private static SimulationCode ldR15RsSimulationCode = new SimulationCode() {
        @Override
        public void simulate(Statement statement, StatementContext context) throws EmulationException {
            context.cpuState.setReg(FrCPUState.DEDICATED_REG_OFFSET + statement.ri_rs_fs, context.memory.load32(context.cpuState.getReg(15)));
            context.cpuState.setReg(15, context.cpuState.getReg(15) + 4);
            /* No change to NZVC */
            context.cpuState.pc += 2;
            context.cycleIncrement = FrInstructionSet.CYCLES_B;
        }
    };

    /**
     * Main instruction map
     * These are the official names from Fujitsu's spec
     */
    private static void addBaseInstructions(Set<OutputOption> options) {
/*                         encode, mask,   new FrInstruction( format             ,nX,nY, name,     displayFmt,     action     , Type                     ,isCond, delay, simulationCode) */
        /* LD @(R13,Rj), Ri */
        fillInstructionMap(0x0000, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LD",     "@(A&j),i",     "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.memory.load32(context.cpuState.getReg(13) + context.cpuState.getReg(statement.rj_rt_ft)));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = CYCLES_B;
            }
        }));

        /* LDUH @(R13,Rj), Ri */
        fillInstructionMap( 0x0100, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LDUH",   "@(A&j),i",     "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.memory.loadUnsigned16(context.cpuState.getReg(13) + context.cpuState.getReg(statement.rj_rt_ft)));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_B;
            }
        }));
        /* LDUB @(R13,Rj), Ri */
        fillInstructionMap( 0x0200, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LDUB",   "@(A&j),i",     "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.memory.loadUnsigned8(context.cpuState.getReg(13) + context.cpuState.getReg(statement.rj_rt_ft)));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_B;
            }
        }));
        /* LD @(R15,udisp6), Ri */
        fillInstructionMap( 0x0300, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "LD",     "@(S&4u),i",    "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.memory.load32(context.cpuState.getReg(15) + statement.imm * 4));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_B;
            }
        }));
        /* LD @Rj, Ri */
        if (options.contains(OutputOption.MEMORY)) {
            fillInstructionMap( 0x0400, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LD",     "@j,i;G",      "G"       , Instruction.FlowType.NONE, false, false, ldSimulationCode
            ));
        } else {
            fillInstructionMap( 0x0400, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LD",     "@j,i;Ju",      "iw"       , Instruction.FlowType.NONE, false, false, ldSimulationCode
            ));
        }
        /* LDUH @Rj, Ri */
        if (options.contains(OutputOption.MEMORY)) {
            fillInstructionMap( 0x0500, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LDUH",   "@j,i;H",      "H"       , Instruction.FlowType.NONE, false, false, lduhSimulationCode
            ));
        } else {
            fillInstructionMap( 0x0500, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LDUH",   "@j,i;Ju",      "iw"       , Instruction.FlowType.NONE, false, false, lduhSimulationCode
            ));
        }
        /* LDUB @Rj, Ri */
        if (options.contains(OutputOption.MEMORY)) {
            fillInstructionMap( 0x0600, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LDUB",   "@j,i;E",      "E"       , Instruction.FlowType.NONE, false, false, ldubSimulationCode
            ));
        } else {
            fillInstructionMap( 0x0600, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LDUB",   "@j,i;Ju",      "iw"       , Instruction.FlowType.NONE, false, false, ldubSimulationCode
            ));
        }
        /* LD @R15+, Ri */
        fillInstructionMap( 0x0700, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "LD",     "@S+,i",        "iwSw"     , Instruction.FlowType.NONE, false, false, ldR15RiSimulationCode));
        /* MOV Ri, PS */
        fillInstructionMap( 0x0710, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "MOV",    "i,P",          "Pw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                ((FrCPUState)context.cpuState).setPS(context.cpuState.getReg(statement.ri_rs_fs), true);
                /* NZVC is part of the PS !*/
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_C;
            }
        }));
        /* LD @R15+, Rs */
        fillInstructionMap( 0x0780, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "LD",     "@S+,g",        "Sw"       , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0781, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "LD",     "@S+,g",        "Sw"       , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0782, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "LD",     "@S+,g",        "Sw"       , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0783, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "LD",     "@S+,g",        "Sw"       , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0784, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "LD",     "@S+,g",        "Sw"       , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0785, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "LD",     "@S+,g",        "Sw"       , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        /* LD @R15+, PS */
        fillInstructionMap( 0x0790, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "LD",     "@S+,P",        "Sw"       , Instruction.FlowType.NONE, false, false, ldR15PSSimulationCode));
        /* DMOV @dir10, R13 */
        fillInstructionMap( 0x0800, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOV",   "@4u,A",        "Aw"       , Instruction.FlowType.NONE, false, false, dmovDir10R13SimulationCode));
        /* DMOVH @dir9, R13 */
        fillInstructionMap( 0x0900, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOVH",  "@2u,A",        "Aw"       , Instruction.FlowType.NONE, false, false, dmovhDir9R13SimulationCode));
        /* DMOVB @dir8, R13 */
        fillInstructionMap( 0x0A00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOVB",  "@u,A",         "Aw"       , Instruction.FlowType.NONE, false, false, dmovbDir8R13SimulationCode));
        /* DMOV @dir10, @-R15 */
        fillInstructionMap( 0x0B00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOV",   "@4u,@-S",      "Sw"       , Instruction.FlowType.NONE, false, false, dmovDir10R15SimulationCode));
        /* DMOV @dir10, @R13+ */
        fillInstructionMap( 0x0C00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOV",   "@4u,@A+",      "Aw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store32(context.cpuState.getReg(13), context.memory.load32(statement.imm * 4));
                context.cpuState.setReg(13, context.cpuState.getReg(13) + 4);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* DMOVH @dir9, @R13+ */
        fillInstructionMap( 0x0D00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOVH",  "@2u,@A+",      "Aw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store16(context.cpuState.getReg(13), context.memory.loadUnsigned16(statement.imm * 2));
                context.cpuState.setReg(13, context.cpuState.getReg(13) + 2);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* DMOVB @dir8, @R13+ */
        fillInstructionMap( 0x0E00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOVB",  "@u,@A+",       "Aw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store8(context.cpuState.getReg(13), context.memory.loadUnsigned8(statement.imm));
                context.cpuState.setReg(13, context.cpuState.getReg(13) + 1);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* ENTER #u10 */
        fillInstructionMap( 0x0F00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "ENTER",  "#4u",          "SwFw"     , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store32(context.cpuState.getReg(15) - 4, context.cpuState.getReg(14));
                context.cpuState.setReg(14, context.cpuState.getReg(15) - 4);
                context.cpuState.setReg(15, context.cpuState.getReg(15) - statement.imm * 4);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + FrInstructionSet.CYCLES_A;
            }
        }));
        /* ST Ri, @(R13,Rj) */
        fillInstructionMap( 0x1000, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ST",     "i,@(A&j)",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store32(context.cpuState.getReg(13) + context.cpuState.getReg(statement.rj_rt_ft), context.cpuState.getReg(statement.ri_rs_fs));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_A;
            }
        }));
        /* STH Ri, @(R13,Rj) */
        fillInstructionMap( 0x1100, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "STH",    "i,@(A&j)",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store16(context.cpuState.getReg(13) + context.cpuState.getReg(statement.rj_rt_ft), context.cpuState.getReg(statement.ri_rs_fs));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_A;
            }
        }));
        /* STB Ri, @(R13,Rj) */
        fillInstructionMap( 0x1200, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "STB",    "i,@(A&j)",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store8(context.cpuState.getReg(13) + context.cpuState.getReg(statement.rj_rt_ft), context.cpuState.getReg(statement.ri_rs_fs));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_A;
            }
        }));
        /* ST Ri, @(R15,udisp6) */
        fillInstructionMap( 0x1300, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "ST",     "i,@(S&4u)",    ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store32(context.cpuState.getReg(15) + statement.imm * 4, context.cpuState.getReg(statement.ri_rs_fs));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_A;
            }
        }));
        /* ST Ri, @Rj */
        if (options.contains(OutputOption.MEMORY)) {
            fillInstructionMap( 0x1400, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ST",     "i,@j;mG",      ""         , Instruction.FlowType.NONE, false, false, stSimulationCode
            ));
        } else {
            fillInstructionMap( 0x1400, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ST",     "i,@j;Ju",      ""         , Instruction.FlowType.NONE, false, false, stSimulationCode
            ));
        }
        /* STH Ri, @Rj */
        if (options.contains(OutputOption.MEMORY)) {
            fillInstructionMap( 0x1500, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "STH",    "i,@j;mH",      ""         , Instruction.FlowType.NONE, false, false, sthSimulationCode
            ));
        } else {
            fillInstructionMap( 0x1500, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "STH",    "i,@j;Ju",      ""         , Instruction.FlowType.NONE, false, false, sthSimulationCode
            ));
        }
        /* STB Ri, @Rj */
        if (options.contains(OutputOption.MEMORY)) {
            fillInstructionMap( 0x1600, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "STB",    "i,@j;mE",      ""         , Instruction.FlowType.NONE, false, false, stbSimulationCode
            ));
        } else {
            fillInstructionMap( 0x1600, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "STB",    "i,@j;Ju",      ""         , Instruction.FlowType.NONE, false, false, stbSimulationCode
            ));
        }
        /* ST Ri, @-R15 */
        fillInstructionMap( 0x1700, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "ST",     "i,@-S",        "Sw"       , Instruction.FlowType.NONE, false, false, stRiR15SimulationCode));
        /* MOV PS, Ri */
        fillInstructionMap( 0x1710, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "MOV",    "P,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, ((FrCPUState)context.cpuState).getPS());
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ST Rs, @-R15 */
        fillInstructionMap( 0x1780, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "ST",     "g,@-S",        "Sw"       , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1781, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "ST",     "g,@-S",        "Sw"       , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1782, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "ST",     "g,@-S",        "Sw"       , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1783, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "ST",     "g,@-S",        "Sw"       , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1784, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "ST",     "g,@-S",        "Sw"       , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1785, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "ST",     "g,@-S",        "Sw"       , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        /* ST PS, @-R15 */
        fillInstructionMap( 0x1790, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "ST",     "P,@-S",        "Sw"       , Instruction.FlowType.NONE, false, false, stPsR15SimulationCode));
        /* DMOV R13, @dir10 */
        fillInstructionMap( 0x1800, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOV",   "A,@4u",        ""         , Instruction.FlowType.NONE, false, false, dmovR13Dir10SimulationCode));
        /* DMOVH R13, @dir9 */
        fillInstructionMap( 0x1900, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOVH",  "A,@2u",        ""         , Instruction.FlowType.NONE, false, false, dmovhR13Dir9SimulationCode));
        /* DMOVB R13, @dir8 */
        fillInstructionMap( 0x1A00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOVB",  "A,@u",         ""         , Instruction.FlowType.NONE, false, false, dmovbR13Dir8SimulationCode));
        /* DMOV @R15+, @dir10 */
        fillInstructionMap( 0x1B00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOV",   "@S+,@4u",      "Sw"       , Instruction.FlowType.NONE, false, false, dmovR15Dir10SimulationCode));
        /* DMOV @R13+, @dir10 */
        fillInstructionMap( 0x1C00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOV",   "@A+,@4u",      "Aw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store32(statement.imm * 4, context.memory.load32(context.cpuState.getReg(13)));
                context.cpuState.setReg(13, context.cpuState.getReg(13) + 4);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* DMOVH @R13+, @dir9 */
        fillInstructionMap( 0x1D00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOVH",  "@A+,@2u",      "Aw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store16(statement.imm * 2, context.memory.loadUnsigned16(context.cpuState.getReg(13)));
                context.cpuState.setReg(13, context.cpuState.getReg(13) + 2);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* DMOVB @R13+, @dir8 */
        fillInstructionMap( 0x1E00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "DMOVB",  "@A+,@u",       "Aw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store8(statement.imm, context.memory.loadUnsigned8(context.cpuState.getReg(13)));
                context.cpuState.setReg(13, context.cpuState.getReg(13) + 1);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* INT #u8 */
        fillInstructionMap( 0x1F00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "INT",    "#u",           ""         , Instruction.FlowType.INT, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.pushStatement(statement);
                ((FrInterruptController)context.interruptController).processInterrupt(statement.imm, context.cpuState.pc + 2, context);
                ((FrCPUState)context.cpuState).I = 0;
                /* No change to NZVC */
                context.cycleIncrement = 3 + 3 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* LD @(R14,disp10), Ri */
        fillInstructionMap( 0x2000, 0xF000, new FrInstruction(InstructionFormat.B, 0, 0, "LD",     "@(F&4s),i",    "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.memory.load32(context.cpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm) * 4));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_B;
            }
        }));
        /* ST Ri, @(R14,disp10) */
        fillInstructionMap( 0x3000, 0xF000, new FrInstruction(InstructionFormat.B, 0, 0, "ST",     "i,@(F&4s)",    ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store32(context.cpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm) * 4, context.cpuState.getReg(statement.ri_rs_fs));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_A;
            }
        }));
        /* LDUH @(R14,disp9), Ri */
        fillInstructionMap( 0x4000, 0xF000, new FrInstruction(InstructionFormat.B, 0, 0, "LDUH",   "@(F&2s),i",    "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.memory.loadUnsigned16(context.cpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm) * 2));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_B;
            }
        }));
        /* STH Ri, @(R14,disp9) */
        fillInstructionMap( 0x5000, 0xF000, new FrInstruction(InstructionFormat.B, 0, 0, "STH",    "i,@(F&2s)",    ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store16(context.cpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm) * 2, context.cpuState.getReg(statement.ri_rs_fs));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_A;
            }
        }));
        /* LDUB @(R14,disp8), Ri */
        fillInstructionMap( 0x6000, 0xF000, new FrInstruction(InstructionFormat.B, 0, 0, "LDUB",   "@(F&s),i",     "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.memory.loadUnsigned8(context.cpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm)));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_B;
            }
        }));
        /* STB Ri, @(R14,disp8) */
        fillInstructionMap( 0x7000, 0xF000, new FrInstruction(InstructionFormat.B, 0, 0, "STB",    "i,@(F&s)",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.memory.store8(context.cpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm), context.cpuState.getReg(statement.ri_rs_fs));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_A;
            }
        }));
        /* BANDL #u4, @Ri (u4: 0 to 0FH) */
        fillInstructionMap( 0x8000, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "BANDL",  "#u,@i;Iu",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Note : AND'ing only the lowest 4 bits with xxxx is like AND'ing the byte with 1111xxxx (1 is neutral for AND)
                context.memory.store8(context.cpuState.getReg(statement.ri_rs_fs), context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) & (0xF0 + statement.imm));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* BANDH #u4, @Ri (u4: 0 to 0FH) */
        fillInstructionMap( 0x8100, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "BANDH",  "#u,@i;Iu",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Note : AND'ing only the highest 4 bits with xxxx is like AND'ing the byte with xxxx1111 (1 is neutral for AND)
                context.memory.store8(context.cpuState.getReg(statement.ri_rs_fs), context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) & ((statement.imm << 4) + 0x0F));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* AND Rj, Ri */
        fillInstructionMap( 0x8200, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "AND",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.cpuState.getReg(statement.ri_rs_fs) & context.cpuState.getReg(statement.rj_rt_ft);
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ANDCCR #u8 */
        fillInstructionMap( 0x8300, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "ANDCCR", "#u",           ""         , Instruction.FlowType.NONE, false, false, andccrU8SimulationCode));
        /* AND Rj, @Ri */
        fillInstructionMap( 0x8400, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "AND",    "j,@i;Iu",      ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.memory.load32(context.cpuState.getReg(statement.ri_rs_fs)) & context.cpuState.getReg(statement.rj_rt_ft);
                context.memory.store32(context.cpuState.getReg(statement.ri_rs_fs), result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* ANDH Rj, @Ri */
        fillInstructionMap( 0x8500, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ANDH",   "j,@i;Iu",      ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.memory.loadUnsigned16(context.cpuState.getReg(statement.ri_rs_fs)) & context.cpuState.getReg(statement.rj_rt_ft);
                context.memory.store16(context.cpuState.getReg(statement.ri_rs_fs), result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x8000) >>> 15;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* ANDB Rj, @Ri */
        fillInstructionMap( 0x8600, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ANDB",   "j,@i;Iu",      ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) & context.cpuState.getReg(statement.rj_rt_ft);
                context.memory.store8(context.cpuState.getReg(statement.ri_rs_fs), result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80) >>> 7;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* STILM #u8 */
        fillInstructionMap( 0x8700, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "STILM",  "#u",           ""         , Instruction.FlowType.NONE, false, false, stilmU8SimulationCode));
        /* BTSTL #u4, @Ri (u4: 0 to 0FH) */
        fillInstructionMap( 0x8800, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "BTSTL",  "#u,@i;Iu",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Note : testing 8 bits AND 0000xxxx is like testing only the lowest 4 bits AND xxxx (0 is absorbing for AND)
                int result32 = context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) & statement.imm;
                ((FrCPUState)context.cpuState).N = 0;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 2 + FrInstructionSet.CYCLES_A;
            }
        }));
        /* BTSTH #u4, @Ri (u4: 0 to 0FH) */
        fillInstructionMap( 0x8900, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "BTSTH",  "#u,@i;Iu",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Note : testing 8 bits AND xxxx0000 is like testing only the highest 4 bits AND xxxx (0 is absorbing for AND)
                int result32 = context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) & (statement.imm << 4);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80) >>> 7;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 2 + FrInstructionSet.CYCLES_A;
            }
        }));
        /* XCHB @Rj, Ri */
        fillInstructionMap( 0x8A00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "XCHB",   "@j,i;Ju",      "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.cpuState.getReg(statement.ri_rs_fs);
                context.cpuState.setReg(statement.ri_rs_fs, context.memory.loadUnsigned8(context.cpuState.getReg(statement.rj_rt_ft)));
                context.memory.store8(context.cpuState.getReg(statement.rj_rt_ft), result32);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* MOV Rj, Ri */
        fillInstructionMap( 0x8B00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.cpuState.getReg(statement.rj_rt_ft));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* LDM0 (reglist) */
        fillInstructionMap( 0x8C00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "LDM0",   "z",            "Sw"       , Instruction.FlowType.NONE, false, false, ldm0SimulationCode));
        /* LDM1 (reglist) */
        fillInstructionMap( 0x8D00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "LDM1",   "y",            "Sw"       , Instruction.FlowType.NONE, false, false, ldm1SimulationCode));
        /* STM0 (reglist) */
        fillInstructionMap( 0x8E00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "STM0",   "xz",           "Sw"       , Instruction.FlowType.NONE, false, false, stm0SimulationCode));
        /* STM1 (reglist) */
        fillInstructionMap( 0x8F00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "STM1",   "xy",           "Sw"       , Instruction.FlowType.NONE, false, false, stm1SimulationCode));
        /* BORL #u4, @Ri (u4: 0 to 0FH) */
        fillInstructionMap( 0x9000, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "BORL",   "#u,@i;Iu",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Note : OR'ing only the lowest 4 bits with xxxx is like OR'ing the byte with 0000xxxx (0 is neutral for OR)
                context.memory.store8(context.cpuState.getReg(statement.ri_rs_fs), context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) | statement.imm);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* BORH #u4, @Ri (u4: 0 to 0FH) */
        fillInstructionMap( 0x9100, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "BORH",   "#u,@i;Iu",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Note : OR'ing only the highest 4 bits with xxxx is like OR'ing the byte with xxxx0000 (0 is neutral for OR)
                context.memory.store8(context.cpuState.getReg(statement.ri_rs_fs), context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) | (statement.imm << 4));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* OR Rj, Ri */
        fillInstructionMap( 0x9200, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "OR",     "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.cpuState.getReg(statement.ri_rs_fs) | context.cpuState.getReg(statement.rj_rt_ft);
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ORCCR #u8 */
        fillInstructionMap( 0x9300, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "ORCCR",  "#u",           ""         , Instruction.FlowType.NONE, false, false, orccrU8SimulationCode));
        /* OR Rj, @Ri */
        fillInstructionMap( 0x9400, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "OR",     "j,@i;Iu",      ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.memory.load32(context.cpuState.getReg(statement.ri_rs_fs)) | context.cpuState.getReg(statement.rj_rt_ft);
                context.memory.store32(context.cpuState.getReg(statement.ri_rs_fs), result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* ORH Rj, @Ri */
        fillInstructionMap( 0x9500, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ORH",    "j,@i;Iu",      ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.memory.loadUnsigned16(context.cpuState.getReg(statement.ri_rs_fs)) | context.cpuState.getReg(statement.rj_rt_ft);
                context.memory.store16(context.cpuState.getReg(statement.ri_rs_fs), result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x8000) >>> 15;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* ORB Rj, @Ri */
        fillInstructionMap( 0x9600, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ORB",    "j,@i;Iu",      ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) | context.cpuState.getReg(statement.rj_rt_ft);
                context.memory.store8(context.cpuState.getReg(statement.ri_rs_fs), result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80) >>> 7;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* JMP @Ri */
        fillInstructionMap( 0x9700, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "JMP",    "@i;Iu",        ""         , Instruction.FlowType.JMP, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.pc = context.cpuState.getReg(statement.ri_rs_fs);
                /* No change to NZVC */
                context.cycleIncrement = 2;
            }
        }));
        /* CALL @Ri */
        fillInstructionMap( 0x9710, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "CALL",   "@i;Iu",        ""         , Instruction.FlowType.CALL, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.pushStatement(statement);
                context.cpuState.setReg(FrCPUState.RP, context.cpuState.pc + 2);
                context.cpuState.pc = context.cpuState.getReg(statement.ri_rs_fs);
                /* No change to NZVC */
                context.cycleIncrement = 2;
            }
        }));
        /* RET */
        fillInstructionMap( 0x9720, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "RET",    "",             ""         , Instruction.FlowType.RET, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.popItem();
                context.cpuState.pc = context.cpuState.getReg(FrCPUState.RP);
                /* No change to NZVC */
                context.cycleIncrement = 2;
            }
        }));
        /* RETI */
        fillInstructionMap( 0x9730, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "RETI",   "",             ""         , Instruction.FlowType.RET, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.popItem();
                context.cpuState.pc = context.memory.load32(context.cpuState.getReg(15));
                context.cpuState.setReg(15, context.cpuState.getReg(15) + 8);
                            /* note : this is the order given in the spec but loading PS below could switch the USP<>SSP,
                            so the last SP increment would address the wrong stack
                            Doing it this way passes the test */
                ((FrCPUState)context.cpuState).setPS(context.memory.load32(context.cpuState.getReg(15) - 4), false);
                /* NZVC is part of the PS !*/
                context.cycleIncrement = 2 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* DIV0S Ri */
        fillInstructionMap( 0x9740, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "DIV0S",  "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int S1 = (context.cpuState.getReg(FrCPUState.MDL) & 0x80000000) >>> 31;
                int S2 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).D0= S1;
                ((FrCPUState)context.cpuState).D1= S1 ^ S2;
                long result64 = (long) context.cpuState.getReg(FrCPUState.MDL);
                context.cpuState.setReg(FrCPUState.MDH, (int) (result64 >>> 32));
                context.cpuState.setReg(FrCPUState.MDL, (int) (result64 & 0xFFFFFFFFL));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* DIV0U Ri */
        fillInstructionMap( 0x9750, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "DIV0U",  "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                ((FrCPUState)context.cpuState).D0=0;
                ((FrCPUState)context.cpuState).D1=0;
                context.cpuState.setReg(FrCPUState.MDH, 0);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* DIV1 Ri */
        fillInstructionMap( 0x9760, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "DIV1",   "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(FrCPUState.MDH, (context.cpuState.getReg(FrCPUState.MDH) << 1) | ((context.cpuState.getReg(FrCPUState.MDL) & 0x80000000) >>> 31));
                context.cpuState.setReg(FrCPUState.MDL, context.cpuState.getReg(FrCPUState.MDL) << 1);
                int result32;
                if (((FrCPUState)context.cpuState).D1 == 1) {
                    // Dividend and divisor have opposite signs
                    long result64 = (context.cpuState.getReg(FrCPUState.MDH) & 0xFFFFFFFFL) + (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL);
                    result32 = (int) result64;
                    ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>> 32);
                    ((FrCPUState)context.cpuState).Z = (result32 == 0)?1:0;
                }
                else {
                    // Dividend and divisor have same signs
                    long result64 = (context.cpuState.getReg(FrCPUState.MDH) & 0xFFFFFFFFL) - (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL);
                    result32 = (int) result64;
                    ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
                    ((FrCPUState)context.cpuState).Z = (result32 == 0)?1:0;
                }
                if ((((FrCPUState)context.cpuState).D0 ^ ((FrCPUState)context.cpuState).D1 ^ ((FrCPUState)context.cpuState).C) == 0) {
                    context.cpuState.setReg(FrCPUState.MDH, result32);
                    context.cpuState.setReg(FrCPUState.MDL, context.cpuState.getReg(FrCPUState.MDL) | 1);
                }
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_D;
            }
        }));
        /* DIV2 Ri */
        fillInstructionMap( 0x9770, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "DIV2",   "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).D1 == 1) {
                    long result64 = context.cpuState.getReg(FrCPUState.MDH) + context.cpuState.getReg(statement.ri_rs_fs);
                    int result32 = (int) result64;
                    ((FrCPUState)context.cpuState).C = (result32 == result64) ? 0 : 1;
                    ((FrCPUState)context.cpuState).Z = (result32 == 0)?1:0;
                }
                else {
                    long result64 = context.cpuState.getReg(FrCPUState.MDH) - context.cpuState.getReg(statement.ri_rs_fs);
                    int result32 = (int) result64;
                    ((FrCPUState)context.cpuState).C = (result32 == result64) ? 0 : 1;
                    ((FrCPUState)context.cpuState).Z = (result32 == 0)?1:0;
                }
                if (((FrCPUState)context.cpuState).Z == 1) {
                    context.cpuState.setReg(FrCPUState.MDH, 0);
                }
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* EXTSB Ri */
        fillInstructionMap( 0x9780, 0xFFF0, new ExtsbFrInstruction( 0x9780, InstructionFormat.E, 0, 0, "EXTSB",  "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, BinaryArithmetics.signExtend(8, context.cpuState.getReg(statement.ri_rs_fs)));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* EXTUB Ri */
        fillInstructionMap( 0x9790, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "EXTUB",  "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.cpuState.getReg(statement.ri_rs_fs) & 0xFF);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* EXTSH Ri */
        fillInstructionMap( 0x97A0, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "EXTSH",  "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, BinaryArithmetics.signExtend(16, context.cpuState.getReg(statement.ri_rs_fs)));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* EXTUH Ri */
        fillInstructionMap( 0x97B0, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "EXTUH",  "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFF);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* SRCH0 Ri */ // FR80/FR81 only
        fillInstructionMap( 0x97C0, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "SRCH0",  "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Search for the first 0
                context.cpuState.setReg(statement.ri_rs_fs, Format.bitSearch(context.cpuState.getReg(statement.ri_rs_fs), 0));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* SRCH1 Ri */ // FR80/FR81 only
        fillInstructionMap( 0x97D0, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "SRCH1",  "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Search for the first 1
                context.cpuState.setReg(statement.ri_rs_fs, Format.bitSearch(context.cpuState.getReg(statement.ri_rs_fs), 1));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* SRCHC Ri */ // FR80/FR81 only
        fillInstructionMap( 0x97E0, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "SRCHC",  "i",            "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Search for the first bit different from the MSB
                int result32 = context.cpuState.getReg(statement.ri_rs_fs);
                context.cpuState.setReg(statement.ri_rs_fs, Format.bitSearch(result32, (result32 & 0x80000000)==0?1:0));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* BEORL #u4, @Ri (u4: 0 to 0FH) */
        fillInstructionMap( 0x9800, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "BEORL",  "#u,@i;Iu",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Note : EOR'ing with 0000xxxx is like EOR'ing only the lowest 4 bits with xxxx (0 is neutral for EOR)
                context.memory.store8(context.cpuState.getReg(statement.ri_rs_fs), context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) ^ statement.imm);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* BEORH #u4, @Ri (u4: 0 to 0FH) */
        fillInstructionMap( 0x9900, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "BEORH",  "#u,@i;Iu",     ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                // Note : EOR'ing with xxxx0000 is like EORing only the highest 4 bits with xxxx (0 is neutral for EOR)
                context.memory.store8(context.cpuState.getReg(statement.ri_rs_fs), context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) ^ (statement.imm << 4));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* EOR Rj, Ri */
        fillInstructionMap( 0x9A00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "EOR",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.cpuState.getReg(statement.ri_rs_fs) ^ context.cpuState.getReg(statement.rj_rt_ft);
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* LDI:20 #i20, Ri */
        fillInstructionMap( 0x9B00, 0xFF00, new FrInstruction(InstructionFormat.C, 1, 0, "LDI:20", "#u,i",         "iv"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, statement.imm);
                /* No change to NZVC */
                context.cpuState.pc += 4;
                context.cycleIncrement = 2;
            }
        }));
        /* EOR Rj, @Ri */
        fillInstructionMap( 0x9C00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "EOR",    "j,@i;Iu",      ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.memory.load32(context.cpuState.getReg(statement.ri_rs_fs)) ^ context.cpuState.getReg(statement.rj_rt_ft);
                context.memory.store32(context.cpuState.getReg(statement.ri_rs_fs), result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* EORH Rj, @Ri */
        fillInstructionMap( 0x9D00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "EORH",   "j,@i;Iu",      ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.memory.loadUnsigned16(context.cpuState.getReg(statement.ri_rs_fs)) ^ context.cpuState.getReg(statement.rj_rt_ft);
                context.memory.store16(context.cpuState.getReg(statement.ri_rs_fs), result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x8000) >>> 15;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* EORB Rj, @Ri */
        fillInstructionMap( 0x9E00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "EORB",   "j,@i;Iu",      ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.memory.loadUnsigned8(context.cpuState.getReg(statement.ri_rs_fs)) ^ context.cpuState.getReg(statement.rj_rt_ft);
                context.memory.store8(context.cpuState.getReg(statement.ri_rs_fs), result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80) >>> 7;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 1 + 2 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* JMP:D @Ri */
        fillInstructionMap( 0x9F00, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "JMP:D",  "@i;Iu",        ""         , Instruction.FlowType.JMP, false, true, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.setDelayedPc(context.cpuState.getReg(statement.ri_rs_fs));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        } ));
        /* CALL:D @Ri */
        fillInstructionMap( 0x9F10, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "CALL:D", "@i;Iu",        ""         , Instruction.FlowType.CALL, false, true, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.pushStatement(statement);
                context.setDelayedPcAndRa(context.cpuState.getReg(statement.ri_rs_fs), context.cpuState.pc + 4);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        } ));
        /* RET:D */
        fillInstructionMap( 0x9F20, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "RET:D",  "",             ""         , Instruction.FlowType.RET, false, true, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.popItem();
                context.setDelayedPc(context.cpuState.getReg(FrCPUState.RP));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        } ));
        /* INTE */
        fillInstructionMap( 0x9F30, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "INTE",   "",             ""         , Instruction.FlowType.INT, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.pushStatement(statement);
                context.cpuState.setReg(FrCPUState.SSP, context.cpuState.getReg(FrCPUState.SSP) - 4);
                context.memory.store32(context.cpuState.getReg(FrCPUState.SSP), ((FrCPUState)context.cpuState).getPS());
                context.cpuState.setReg(FrCPUState.SSP, context.cpuState.getReg(FrCPUState.SSP) - 4);
                context.memory.store32(context.cpuState.getReg(FrCPUState.SSP), context.cpuState.pc + 2);
                ((FrCPUState)context.cpuState).setS(0);
                ((FrCPUState)context.cpuState).setILM(4, false);
                context.cpuState.pc = context.memory.load32(context.cpuState.getReg(FrCPUState.TBR) + 0x3D8);
                /* No change to NZVC */
                context.cycleIncrement = 3 + 3 * FrInstructionSet.CYCLES_A;
            }
        }));
        /* DIV3 */
        fillInstructionMap( 0x9F60, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "DIV3",   "",             ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).Z == 1) {
                    context.cpuState.setReg(FrCPUState.MDL, context.cpuState.getReg(FrCPUState.MDL) + 1);
                }
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* DIV4S */
        fillInstructionMap( 0x9F70, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "DIV4S",  "",             ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).D1 == 1) {
                    context.cpuState.setReg(FrCPUState.MDL, -context.cpuState.getReg(FrCPUState.MDL));
                }
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* LDI:32 #i32, Ri */
        fillInstructionMap( 0x9F80, 0xFFF0, new Ldi32FrInstruction( 0x9F80, InstructionFormat.E, 2, 0, "LDI:32", "#u,i",         "iv"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, statement.imm);
                /* No change to NZVC */
                context.cpuState.pc += 6;
                context.cycleIncrement = 3;
            }
        }));
        /* LEAVE */
        fillInstructionMap( 0x9F90, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "LEAVE",  "",             ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(15, context.cpuState.getReg(14) + 4);
                context.cpuState.setReg(14, context.memory.load32(context.cpuState.getReg(15) - 4));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = FrInstructionSet.CYCLES_B;
            }
        }));
        /* NOP */
        fillInstructionMap( 0x9FA0, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "NOP",    "",             ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                /* No change */
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ADDN #i4, Ri */
        fillInstructionMap( 0xA000, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "ADDN",   "#u,i",         "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.cpuState.getReg(statement.ri_rs_fs) + statement.imm);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ADDN2 #i4, Ri */
        fillInstructionMap( 0xA100, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "ADDN2",  "#n,i",         "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.cpuState.getReg(statement.ri_rs_fs) + BinaryArithmetics.negativeExtend(4, statement.imm));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ADDN Rj, Ri */
        fillInstructionMap( 0xA200, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ADDN",   "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.cpuState.getReg(statement.ri_rs_fs) + context.cpuState.getReg(statement.rj_rt_ft));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ADDSP #s10 */
        fillInstructionMap( 0xA300, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "ADDSP",  "#4s",          "Sw"       , Instruction.FlowType.NONE, false, false, addspS10SimulationCode));
        /* ADD #i4, Ri */
        fillInstructionMap( 0xA400, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "ADD",    "#u,i",         "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) + statement.imm;
                int result32 = (int) result64;
                int S1 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                int S2 = 0; /* unsigned extension of x means positive */
                int Sr = (int) ((result64 & 0x80000000L) >>> 31);

                ((FrCPUState)context.cpuState).N = Sr;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (~(S1 ^ S2)) & (S1 ^ Sr);
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>>32);
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ADD2 #i4, Ri */
        fillInstructionMap( 0xA500, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "ADD2",   "#n,i",         "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) + (BinaryArithmetics.negativeExtend(4, statement.imm) & 0xFFFFFFFFL);
                int result32 = (int) result64;
                int S1 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                int S2 = 1; /* negative extension of x means negative */
                int Sr = (int) ((result64 & 0x80000000L) >>> 31);

                ((FrCPUState)context.cpuState).N = Sr;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (~(S1 ^ S2)) & (S1 ^ Sr);
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>>32);
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ADD Rj, Ri */
        fillInstructionMap( 0xA600, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ADD",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) + (context.cpuState.getReg(statement.rj_rt_ft) & 0xFFFFFFFFL);
                int result32 = (int) result64;
                int S1 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                int S2 = (context.cpuState.getReg(statement.rj_rt_ft) & 0x80000000) >>> 31;
                int Sr = (int) ((result64 & 0x80000000L) >>> 31);

                ((FrCPUState)context.cpuState).N = Sr;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (~(S1 ^ S2)) & (S1 ^ Sr);
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>>32);
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ADDC Rj, Ri */
        fillInstructionMap( 0xA700, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ADDC",   "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) + (context.cpuState.getReg(statement.rj_rt_ft) & 0xFFFFFFFFL) + ((FrCPUState)context.cpuState).C;
                int result32 = (int) result64;
                int S1 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                int S2 = (context.cpuState.getReg(statement.rj_rt_ft) & 0x80000000) >>> 31; // TODO : Shouldn't it take C into account ?
                int Sr = (int) ((result64 & 0x80000000L) >>> 31);

                ((FrCPUState)context.cpuState).N = Sr;
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (~(S1 ^ S2)) & (S1 ^ Sr);
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>>32);
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* CMP #i4, Ri */
        fillInstructionMap( 0xA800, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "CMP",    "#u,i",         "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) - statement.imm;
                            /* optimize : 0 extension of x means S2 is 0, right ?  */
                int S1 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                int S2 = 0; /* unsigned extension of x means positive */
                int Sr = (int) ((result64 & 0x80000000L) >>> 31);

                ((FrCPUState)context.cpuState).N = Sr;
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (S1 ^ S2) & (S1 ^ Sr);
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* CMP2 #i4, Ri */
        fillInstructionMap( 0xA900, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "CMP2",   "#n,i",         "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) - (BinaryArithmetics.negativeExtend(4, statement.imm) & 0xFFFFFFFFL);
                int S1 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                int S2 = 1; /* negative extension of x means negative */
                int Sr = (int) ((result64 & 0x80000000L) >>> 31);

                ((FrCPUState)context.cpuState).N = Sr;
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (S1 ^ S2) & (S1 ^ Sr);
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* CMP Rj, Ri */
        fillInstructionMap( 0xAA00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "CMP",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) - (context.cpuState.getReg(statement.rj_rt_ft) & 0xFFFFFFFFL);
                int S1 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                int S2 = (context.cpuState.getReg(statement.rj_rt_ft) & 0x80000000) >>> 31;
                int Sr = (int) ((result64 & 0x80000000L) >>> 31);

                ((FrCPUState)context.cpuState).N = Sr;
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (S1 ^ S2) & (S1 ^ Sr);
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* MULU Rj,Ri */
        fillInstructionMap( 0xAB00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "MULU",   "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) * (context.cpuState.getReg(statement.rj_rt_ft) & 0xFFFFFFFFL);
                context.cpuState.setReg(FrCPUState.MDH, (int) (result64 >> 32));
                context.cpuState.setReg(FrCPUState.MDL, (int) (result64 & 0xFFFFFFFFL));

                ((FrCPUState)context.cpuState).N = (int) ((result64 & 0x80000000L) >>> 31); /*see pdf*/
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (int) ((result64 & 0x100000000L) >>> 32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 5;
            }
        }));
        /* SUB Rj, Ri */
        fillInstructionMap( 0xAC00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "SUB",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) - (context.cpuState.getReg(statement.rj_rt_ft) & 0xFFFFFFFFL);
                int S1 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                int S2 = (context.cpuState.getReg(statement.rj_rt_ft) & 0x80000000) >>> 31;
                int Sr = (int) ((result64 & 0x80000000L) >>> 31);

                ((FrCPUState)context.cpuState).N = Sr;
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (S1 ^ S2) & (S1 ^ Sr);
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
                context.cpuState.setReg(statement.ri_rs_fs, (int) result64);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* SUBC Rj, Ri */
        fillInstructionMap( 0xAD00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "SUBC",   "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) - (context.cpuState.getReg(statement.rj_rt_ft) & 0xFFFFFFFFL) - ((FrCPUState)context.cpuState).C;
                int S1 = (context.cpuState.getReg(statement.ri_rs_fs) & 0x80000000) >>> 31;
                int S2 = (context.cpuState.getReg(statement.rj_rt_ft) & 0x80000000) >>> 31; // TODO : Shouldn't it take C into account ?
                int Sr = (int) ((result64 & 0x80000000L) >>> 31);

                ((FrCPUState)context.cpuState).N = Sr;
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (S1 ^ S2) & (S1 ^ Sr);
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
                context.cpuState.setReg(statement.ri_rs_fs, (int) result64);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* SUBN Rj, Ri */
        fillInstructionMap( 0xAE00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "SUBN",   "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, context.cpuState.getReg(statement.ri_rs_fs) - context.cpuState.getReg(statement.rj_rt_ft));
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* MUL Rj,Ri */
        fillInstructionMap( 0xAF00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "MUL",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = ((long) context.cpuState.getReg(statement.rj_rt_ft)) * ((long) context.cpuState.getReg(statement.ri_rs_fs));
                context.cpuState.setReg(FrCPUState.MDH, (int) (result64 >> 32));
                context.cpuState.setReg(FrCPUState.MDL, (int) (result64 & 0xFFFFFFFFL));
                ((FrCPUState)context.cpuState).N = (int) ((result64 & 0x80000000L) >>> 31); /*see pdf*/
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).V = (int) ((result64 & 0x100000000L) >>> 32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 5;
            }
        }));
        /* LSR #u4, Ri */
        fillInstructionMap( 0xB000, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "LSR",    "#d,i",         "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.cpuState.getReg(statement.ri_rs_fs) >>> statement.imm;

                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).C = (statement.imm == 0) ? 0 : (context.cpuState.getReg(statement.ri_rs_fs) >> (statement.imm - 1)) & 1;
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* LSR2 #u4, Ri */
        fillInstructionMap( 0xB100, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "LSR2",   "#d,i",         "iw"       , Instruction.FlowType.NONE, false, false, lsr2u4RiSimulationCode));
        /* LSR Rj, Ri */
        fillInstructionMap( 0xB200, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LSR",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.cpuState.getReg(statement.ri_rs_fs) >>> (context.cpuState.getReg(statement.rj_rt_ft) & 0x1F);

                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).C = ((context.cpuState.getReg(statement.rj_rt_ft) & 0x1F) == 0) ? 0 : (context.cpuState.getReg(statement.ri_rs_fs) >> ((context.cpuState.getReg(statement.rj_rt_ft) & 0x1F) - 1)) & 1;
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* MOV Ri, Rs */
        fillInstructionMap( 0xB300, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "i,h",          ""         , Instruction.FlowType.NONE, false, false, movRiRsSimulationCode));
        fillInstructionMap( 0xB310, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "i,h",          ""         , Instruction.FlowType.NONE, false, false, movRiRsSimulationCode));
        fillInstructionMap( 0xB320, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "i,h",          ""         , Instruction.FlowType.NONE, false, false, movRiRsSimulationCode));
        fillInstructionMap( 0xB330, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "i,h",          ""         , Instruction.FlowType.NONE, false, false, movRiRsSimulationCode));
        fillInstructionMap( 0xB340, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "i,h",          ""         , Instruction.FlowType.NONE, false, false, movRiRsSimulationCode));
        fillInstructionMap( 0xB350, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "i,h",          ""         , Instruction.FlowType.NONE, false, false, movRiRsSimulationCode));

        /* LSL #u4, Ri */
        fillInstructionMap( 0xB400, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "LSL",    "#d,i",         "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) << statement.imm;

                ((FrCPUState)context.cpuState).N = (int) ((result64 & 0x80000000L) >>> 31);
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).C = (statement.imm == 0) ? 0 : (int) ((result64 & 0x100000000L) >>> 32);
                context.cpuState.setReg(statement.ri_rs_fs, (int) result64);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* LSL2 #u4, Ri */
        fillInstructionMap( 0xB500, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "LSL2",   "#d,i",         "iw"       , Instruction.FlowType.NONE, false, false, lsl2u4RiSimulationCode));
        /* LSL Rj, Ri */
        fillInstructionMap( 0xB600, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "LSL",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                long result64 = (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFFFFFFL) << (context.cpuState.getReg(statement.rj_rt_ft) & 0x1F);

                ((FrCPUState)context.cpuState).N = (int) ((result64 & 0x80000000L) >>> 31);
                ((FrCPUState)context.cpuState).Z = (result64 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).C = (int) ((result64 & 0x100000000L) >>> 32);
                context.cpuState.setReg(statement.ri_rs_fs, (int) result64);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* MOV Rs, Ri */
        fillInstructionMap( 0xB700, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "h,i",          "iw"       , Instruction.FlowType.NONE, false, false, movRsRiSimulationCode));
        fillInstructionMap( 0xB710, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "h,i",          "iw"       , Instruction.FlowType.NONE, false, false, movRsRiSimulationCode));
        fillInstructionMap( 0xB720, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "h,i",          "iw"       , Instruction.FlowType.NONE, false, false, movRsRiSimulationCode));
        fillInstructionMap( 0xB730, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "h,i",          "iw"       , Instruction.FlowType.NONE, false, false, movRsRiSimulationCode));
        fillInstructionMap( 0xB740, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "h,i",          "iw"       , Instruction.FlowType.NONE, false, false, movRsRiSimulationCode));
        fillInstructionMap( 0xB750, 0xFFF0, new FrInstruction(InstructionFormat.A, 0, 0, "MOV",    "h,i",          "iw"       , Instruction.FlowType.NONE, false, false, movRsRiSimulationCode));

        /* ASR #u4, Ri */
        fillInstructionMap( 0xB800, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "ASR",    "#d,i",         "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.cpuState.getReg(statement.ri_rs_fs) >> statement.imm;

                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).C = (statement.imm == 0) ? 0 : (context.cpuState.getReg(statement.ri_rs_fs) >> (statement.imm - 1)) & 1;
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* ASR2 #u4, Ri */
        fillInstructionMap( 0xB900, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "ASR2",   "#d,i",         "iw"       , Instruction.FlowType.NONE, false, false, asr2u4RiSimulationCode));
        /* ASR Rj, Ri */
        fillInstructionMap( 0xBA00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "ASR",    "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = context.cpuState.getReg(statement.ri_rs_fs) >> (context.cpuState.getReg(statement.rj_rt_ft) & 0x1F);

                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                ((FrCPUState)context.cpuState).C = ((context.cpuState.getReg(statement.rj_rt_ft) & 0x1F) == 0) ? 0 : (context.cpuState.getReg(statement.ri_rs_fs) >> ((context.cpuState.getReg(statement.rj_rt_ft) & 0x1F) - 1)) & 1;
                context.cpuState.setReg(statement.ri_rs_fs, result32);
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* MULUH Rj,Ri */
        fillInstructionMap( 0xBB00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "MULUH",  "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = (context.cpuState.getReg(statement.rj_rt_ft) & 0xFFFF) * (context.cpuState.getReg(statement.ri_rs_fs) & 0xFFFF);
                context.cpuState.setReg(FrCPUState.MDL, result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 3;
            }
        }));
        /* MULH Rj, Ri */
        fillInstructionMap( 0xBF00, 0xFF00, new FrInstruction(InstructionFormat.A, 0, 0, "MULH",   "j,i",          "iw"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                int result32 = ((short) context.cpuState.getReg(statement.rj_rt_ft)) * ((short) context.cpuState.getReg(statement.ri_rs_fs));
                context.cpuState.setReg(FrCPUState.MDL, result32);
                ((FrCPUState)context.cpuState).N = (result32 & 0x80000000) >>> 31;
                ((FrCPUState)context.cpuState).Z = (result32 == 0) ? 1 : 0;
                context.cpuState.pc += 2;
                context.cycleIncrement = 3;
            }
        }));
        /* LDI:8 #i8, Ri */
        fillInstructionMap( 0xC000, 0xF000, new Ldi8FrInstruction( 0xC000, InstructionFormat.B, 0, 0, "LDI:8",  "#u,i",         "iv"       , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.setReg(statement.ri_rs_fs, statement.imm);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* CALL label12 */
        fillInstructionMap( 0xD000, 0xF800, new FrInstruction(InstructionFormat.F, 0, 0, "CALL",   "2ru",          ""         , Instruction.FlowType.CALL, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.pushStatement(statement);
                context.cpuState.setReg(FrCPUState.RP, context.cpuState.pc + 2);
                context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(11, statement.imm) * 2; // TODO check *2 ?
                /* No change to NZVC */
                context.cycleIncrement = 2;
            }
        }));
        /* CALL:D label12 */
        fillInstructionMap( 0xD800, 0xF800, new FrInstruction(InstructionFormat.F, 0, 0, "CALL:D", "2ru",          ""         , Instruction.FlowType.CALL, false, true, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.pushStatement(statement);
                context.setDelayedPcAndRa(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(11, statement.imm) * 2, context.cpuState.pc + 4);  // TODO check *2
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        } ));
        /* BRA label9 */
        fillInstructionMap( 0xE000, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BRA",    "2ru",          ""         , Instruction.FlowType.JMP, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                /* No change to NZVC */
                context.cycleIncrement = 2;
            }
        }));
        /* BNO label9 */
        fillInstructionMap( 0xE100, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BNO",    "2ru",          ""         , Instruction.FlowType.NONE, false, false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                /* No branch */
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* BEQ label9 */
        fillInstructionMap( 0xE200, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BEQ",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).Z == 1) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BNE label9 */
        fillInstructionMap( 0xE300, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BNE",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).Z == 0) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BC label9 */
        fillInstructionMap( 0xE400, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BC",     "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).C == 1) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BNC label9 */
        fillInstructionMap( 0xE500, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BNC",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).C == 0) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BN label9 */
        fillInstructionMap( 0xE600, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BN",     "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).N == 1) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BP label9 */
        fillInstructionMap( 0xE700, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BP",     "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).N == 0) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BV label9 */
        fillInstructionMap( 0xE800, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BV",     "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).V == 1) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BNV label9 */
        fillInstructionMap( 0xE900, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BNV",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).V == 0) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BLT label9 */
        fillInstructionMap( 0xEA00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BLT",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if ((((FrCPUState)context.cpuState).V ^ ((FrCPUState)context.cpuState).N) == 1) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BGE label9 */
        fillInstructionMap( 0xEB00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BGE",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if ((((FrCPUState)context.cpuState).V ^ ((FrCPUState)context.cpuState).N) == 0) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BLE label9 */
        fillInstructionMap( 0xEC00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BLE",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((((FrCPUState)context.cpuState).V ^ ((FrCPUState)context.cpuState).N) | ((FrCPUState)context.cpuState).Z) == 1) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BGT label9 */
        fillInstructionMap( 0xED00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BGT",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((((FrCPUState)context.cpuState).V ^ ((FrCPUState)context.cpuState).N) | ((FrCPUState)context.cpuState).Z) == 0) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BLS label9 */
        fillInstructionMap( 0xEE00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BLS",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if ((((FrCPUState)context.cpuState).C | ((FrCPUState)context.cpuState).Z) == 1) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BHI label9 */
        fillInstructionMap( 0xEF00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BHI",    "2ru",          ""         , Instruction.FlowType.BRA, true , false, new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if ((((FrCPUState)context.cpuState).C | ((FrCPUState)context.cpuState).Z) == 0) {
                    context.cpuState.pc = context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                    context.cycleIncrement = 2;
                }
                else {
                    context.cpuState.pc += 2;
                    context.cycleIncrement = 1;
                }
                /* No change to NZVC */
            }
        }));
        /* BRA:D label9 */
        fillInstructionMap( 0xF000, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BRA:D",  "2ru",          ""         , Instruction.FlowType.JMP, false, true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;
            }
        }));
        /* BNO:D label9 */
        fillInstructionMap( 0xF100, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BNO:D",  "2ru",          ""         , Instruction.FlowType.NONE, false, true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                /* No branch */
                /* No change to NZVC */
                context.cpuState.pc += 2;
                context.cycleIncrement = 1;

            }
        }));
        /* BEQ:D label9 */
        fillInstructionMap( 0xF200, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BEQ:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).Z == 1) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                //* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BNE:D label9 */
        fillInstructionMap( 0xF300, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BNE:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).Z == 0) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BC:D label9 */
        fillInstructionMap( 0xF400, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BC:D",   "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).C == 1) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BNC:D label9 */
        fillInstructionMap( 0xF500, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BNC:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).C == 0) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BN:D label9 */
        fillInstructionMap( 0xF600, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BN:D",   "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).N == 1) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BP:D label9 */
        fillInstructionMap( 0xF700, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BP:D",   "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).N == 0) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BV:D label9 */
        fillInstructionMap( 0xF800, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BV:D",   "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).V == 1) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BNV:D label9 */
        fillInstructionMap( 0xF900, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BNV:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((FrCPUState)context.cpuState).V == 0) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BLT:D label9 */
        fillInstructionMap( 0xFA00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BLT:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if ((((FrCPUState)context.cpuState).V ^ ((FrCPUState)context.cpuState).N) == 1) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BGE:D label9 */
        fillInstructionMap( 0xFB00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BGE:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if ((((FrCPUState)context.cpuState).V ^ ((FrCPUState)context.cpuState).N) == 0) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BLE:D label9 */
        fillInstructionMap( 0xFC00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BLE:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((((FrCPUState)context.cpuState).V ^ ((FrCPUState)context.cpuState).N) | ((FrCPUState)context.cpuState).Z) == 1) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BGT:D label9 */
        fillInstructionMap( 0xFD00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BGT:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if (((((FrCPUState)context.cpuState).V ^ ((FrCPUState)context.cpuState).N) | ((FrCPUState)context.cpuState).Z) == 0) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BLS:D label9 */
        fillInstructionMap( 0xFE00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BLS:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if ((((FrCPUState)context.cpuState).C | ((FrCPUState)context.cpuState).Z) == 1) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
        /* BHI:D label9 */
        fillInstructionMap( 0xFF00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "BHI:D",  "2ru",          ""         , Instruction.FlowType.BRA, true , true , new SimulationCode() {
            @Override
            public void simulate(Statement statement, StatementContext context) throws EmulationException {
                if ((((FrCPUState)context.cpuState).C | ((FrCPUState)context.cpuState).Z) == 0) {
                    context.setDelayedPc(context.cpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2);
                }
                context.cpuState.pc += 2;
                /* No change to NZVC */
                context.cycleIncrement = 1;
            }
        }));
    }


    /**
     * These are replacement names for all stack-related operations
     */
    private static void replaceAltStackInstructions() {
        fillInstructionMap( 0x0700, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "POP",    "i",            ""         , Instruction.FlowType.NONE, false, false, ldR15RiSimulationCode));
        fillInstructionMap( 0x0780, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "POP",    "g",            ""         , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0781, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "POP",    "g",            ""         , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0782, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "POP",    "g",            ""         , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0783, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "POP",    "g",            ""         , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0784, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "POP",    "g",            ""         , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0785, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "POP",    "g",            ""         , Instruction.FlowType.NONE, false, false, ldR15RsSimulationCode));
        fillInstructionMap( 0x0790, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "POP",    "P",            ""         , Instruction.FlowType.NONE, false, false, ldR15PSSimulationCode));
        fillInstructionMap( 0x0B00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "PUSH",   "@4u",          ""         , Instruction.FlowType.NONE, false, false, dmovDir10R15SimulationCode));
        fillInstructionMap( 0x1700, 0xFFF0, new FrInstruction(InstructionFormat.E, 0, 0, "PUSH",   "i",            ""         , Instruction.FlowType.NONE, false, false, stRiR15SimulationCode));
        fillInstructionMap( 0x1780, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "PUSH",   "g",            ""         , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1781, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "PUSH",   "g",            ""         , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1782, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "PUSH",   "g",            ""         , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1783, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "PUSH",   "g",            ""         , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1784, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "PUSH",   "g",            ""         , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1785, 0xFFFF, new FrInstruction(InstructionFormat.E, 0, 0, "PUSH",   "g",            ""         , Instruction.FlowType.NONE, false, false, stRsR15SimulationCode));
        fillInstructionMap( 0x1790, 0xFFFF, new FrInstruction(InstructionFormat.Z, 0, 0, "PUSH",   "P",            ""         , Instruction.FlowType.NONE, false, false, stPsR15SimulationCode));
        fillInstructionMap( 0x1B00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "POP",    "@u",           ""         , Instruction.FlowType.NONE, false, false, dmovR15Dir10SimulationCode));
        fillInstructionMap( 0x8C00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "POP",    "z",            ""         , Instruction.FlowType.NONE, false, false, ldm0SimulationCode));
        fillInstructionMap( 0x8D00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "POP",    "y",            ""         , Instruction.FlowType.NONE, false, false, ldm1SimulationCode));
        fillInstructionMap( 0x8E00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "PUSH",   "xz",           ""         , Instruction.FlowType.NONE, false, false, stm0SimulationCode));
        fillInstructionMap( 0x8F00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "PUSH",   "xy",           ""         , Instruction.FlowType.NONE, false, false, stm1SimulationCode));
    }

    /**
     * These are replacement names for all "+16" shift opcodes (LSR2, LSL2, ASR2)
     */
    private static void replaceShiftInstructions() {
        fillInstructionMap( 0xB100, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "LSR",    "#bd,i",        "iw"       , Instruction.FlowType.NONE, false, false, lsr2u4RiSimulationCode));
        fillInstructionMap( 0xB500, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "LSL",    "#bd,i",        "iw"       , Instruction.FlowType.NONE, false, false, lsl2u4RiSimulationCode));
        fillInstructionMap( 0xB900, 0xFF00, new FrInstruction(InstructionFormat.C, 0, 0, "ASR",    "#bd,i",        "iw"       , Instruction.FlowType.NONE, false, false, asr2u4RiSimulationCode));
    }

    /**
     * These are replacement names for all some DMOV opcodes
     */
    private static void replaceAltDmovInstructions() {
        fillInstructionMap( 0x0800, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "LD",     "@4u,A",        ""         , Instruction.FlowType.NONE, false, false, dmovDir10R13SimulationCode));
        fillInstructionMap( 0x0900, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "LDUH",   "@2u,A",        ""         , Instruction.FlowType.NONE, false, false, dmovhDir9R13SimulationCode));
        fillInstructionMap( 0x0A00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "LDUB",   "@u,A",         ""         , Instruction.FlowType.NONE, false, false, dmovbDir8R13SimulationCode));
        fillInstructionMap( 0x1800, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "ST",     "A,@4u",        ""         , Instruction.FlowType.NONE, false, false, dmovR13Dir10SimulationCode));
        fillInstructionMap( 0x1900, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "STUH",   "A,@2u",        ""         , Instruction.FlowType.NONE, false, false, dmovhR13Dir9SimulationCode));
        fillInstructionMap( 0x1A00, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "STUB",   "A,@u",         ""         , Instruction.FlowType.NONE, false, false, dmovbR13Dir8SimulationCode));
    }

    /**
     * These are replacement names for dedicated opcodes
     * working on ILM, CCR and SP so that they look the same as others
     */
    private static void replaceAltSpecialInstructions() {
        fillInstructionMap( 0x8300, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "AND",    "#u,C",         "Cw"       , Instruction.FlowType.NONE, false, false, andccrU8SimulationCode));
        fillInstructionMap( 0x8700, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "MOV",    "#u,M",         ""         , Instruction.FlowType.NONE, false, false, stilmU8SimulationCode));
        fillInstructionMap( 0x9300, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "OR",     "#u,C",         "Cw"       , Instruction.FlowType.NONE, false, false, orccrU8SimulationCode));
        fillInstructionMap( 0xA300, 0xFF00, new FrInstruction(InstructionFormat.D, 0, 0, "ADD",    "#4s,S",        ""         , Instruction.FlowType.NONE, false, false, addspS10SimulationCode));
    }


    /**
     * Fake OPCodes for data reading
     * Array index is a RangeType.Width.index value
     */
    static FrInstruction[] opData = {
        new FrInstruction(InstructionFormat.W, 0, 0, "DW",     "u;a",         ""         , Instruction.FlowType.NONE, false, false, null),
        new FrInstruction(InstructionFormat.W, 1, 0, "DL",     "u;a",         ""         , Instruction.FlowType.NONE, false, false, null),
        new FrInstruction(InstructionFormat.W, 1, 0, "DL",     "u;a",         ""         , Instruction.FlowType.NONE, false, false, null),
        new FrInstruction(InstructionFormat.W, 1, 0, "DL",     "u;T #v",      ""         , Instruction.FlowType.NONE, false, false, null),
        new FrInstruction(InstructionFormat.W, 1, 0, "DR",     "q;f",         ""         , Instruction.FlowType.NONE, false, false, null),
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
        if (options.contains(OutputOption.STACK))
            replaceAltStackInstructions();
        if (options.contains(OutputOption.SHIFT))
            replaceShiftInstructions();
        if (options.contains(OutputOption.DMOV))
            replaceAltDmovInstructions();
        if (options.contains(OutputOption.SPECIALS))
            replaceAltSpecialInstructions();
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
    private static void fillInstructionMap(int encoding, int mask, FrInstruction instruction) {
        int n = (~ mask) & 0xFFFF;
        for( int i = 0 ; i <= n ; i++)
        {
            instructionMap[encoding | i] = instruction;
        }
    }

    public static class Ldi32FrInstruction extends FrInstruction {
        public Ldi32FrInstruction(int encoding, FrInstructionSet.InstructionFormat instructionFormat, int numberExtraXWords, int numberExtraYWords, String name, String displayFormat, String action, FlowType flowType, boolean isConditional, boolean hasDelaySlot, SimulationCode simulationCode) {
            super(instructionFormat, numberExtraXWords, numberExtraYWords, name, displayFormat, action, flowType, isConditional, hasDelaySlot, simulationCode);
        }
    }

    public static class Ldi8FrInstruction extends FrInstruction {
        public Ldi8FrInstruction(int encoding, FrInstructionSet.InstructionFormat instructionFormat, int numberExtraXWords, int numberExtraYWords, String name, String displayFormat, String action, FlowType flowType, boolean isConditional, boolean hasDelaySlot, SimulationCode simulationCode) {
            super(instructionFormat, numberExtraXWords, numberExtraYWords, name, displayFormat, action, flowType, isConditional, hasDelaySlot, simulationCode);
        }
    }

    public static class ExtsbFrInstruction extends FrInstruction {
        public ExtsbFrInstruction(int encoding, FrInstructionSet.InstructionFormat instructionFormat, int numberExtraXWords, int numberExtraYWords, String name, String displayFormat, String action, FlowType flowType, boolean isConditional, boolean hasDelaySlot, SimulationCode simulationCode) {
            super(instructionFormat, numberExtraXWords, numberExtraYWords, name, displayFormat, action, flowType, isConditional, hasDelaySlot, simulationCode);
        }
    }
}
