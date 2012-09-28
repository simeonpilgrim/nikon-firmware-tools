package com.nikonhacker.emu;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.fr.FrInstruction;
import com.nikonhacker.disassembly.fr.FrInstructionSet;
import com.nikonhacker.disassembly.fr.FrStatement;
import com.nikonhacker.emu.memory.AutoAllocatingMemory;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.peripherials.interruptController.FrInterruptController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * This Emulator is based on :
 * - FR Family instruction manual for the basics - http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-00101-5E.pdf
 * - FR80 Family programming manual for specifics - http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-00104-3E.pdf
 * All implemented operations can be tested with the EmulatorTest class
 */
public class FrEmulator extends Emulator {

    private InterruptController interruptController;

    protected Integer nextPC = null;
    protected Integer nextReturnAddress = null;
    protected boolean delaySlotDone = false;

    public static void main(String[] args) throws IOException, EmulationException, ParsingException {
        if (args.length < 2) {
            System.err.println("Usage Emulator <file> <initialPc>");
            System.err.println(" e.g. Emulator fw.bin 0x40000");
            System.exit(-1);
        }
        int initialPc = Format.parseUnsigned(args[1]);
        AutoAllocatingMemory memory = new AutoAllocatingMemory();
        memory.loadFile(new File(args[0]), initialPc); // TODO use ranges
        EmulatorOptions.debugMemory = false;

        FrEmulator emulator = new FrEmulator();
        emulator.setMemory(memory);
        emulator.setCpuState(new FrCPUState(initialPc));
        emulator.setInterruptController(new FrInterruptController(memory));
        emulator.setInstructionPrintWriter(new PrintWriter(System.out));

        emulator.play();
    }

    public FrEmulator() {
    }


    public void setInterruptController(InterruptController interruptController) {
        this.interruptController = interruptController;
    }

    @Override
    public void setOutputOptions(Set<OutputOption> outputOptions) {
        FrInstructionSet.init(outputOptions);
        FrStatement.initFormatChars(outputOptions);
        FrCPUState.initRegisterLabels(outputOptions);
        this.outputOptions = outputOptions;
    }

    /**
     * Starts emulating
     * @return the BreakCondition that caused the emulator to stop
     * @throws EmulationException
     */
    @Override
    public BreakCondition play() throws EmulationException {
        /* temporary variables */
        int result32, S1, S2, Sr, n;
        long result64;

        int cycles;

        int a = 1; /* Memory access cycles, may be increased by "Ready" function. */
        int b = 1; /* Memory access cycles, may be increased by "Ready" function. Note that if the next
                    instruction references a register involved in a "LD" operation an interlock will be applied,
                    increasing the number of execution cycles from 1 cycle to 2 cycles. */
        int c = 1; /* If the instruction immediately after is a read or write operation involving register "R15", or
                    the "SSP" or "USP" pointers, or the instruction format is TYPE-A, an interlock will be
                    applied, increasing the number of execution cycles from 1 cycle to 2 cycles. */
        int d = 1; /* If the instruction immediately after references the "MDH/MDL" register, interlock will be
                    applied, increasing the number of execution cycles from 1 cycle to 2 cycles.
                    When dedicated register such as TBR, RP, USP, SSP, MDH, and MDL is accessed with ST
                    Rs, @-R15 command just after DIV1 command, an interlock is always brought, increasing
                    the number of execution cycles from 1 cycle to 2 cycles. */

        FrStatement statement = new FrStatement();

        FrCPUState frCpuState = (FrCPUState)cpuState;

        frCpuState.setAllRegistersDefined();

        try {
            for (;;) {
                
                statement.reset();

                statement.getNextStatement(memory, frCpuState.pc);
    
                statement.setInstruction(FrInstructionSet.instructionMap[statement.data[0]]);
    
                statement.decodeOperands(frCpuState.pc, memory);

                if (instructionPrintWriter != null) {
                    // copying to make sure we keep a reference even if instructionPrintWriter gets set to null in between but still avoid costly synchronization
                    PrintWriter printWriter = instructionPrintWriter;
                    if (printWriter != null) {
                        // OK. copy is still not null
                        statement.formatOperandsAndComment(frCpuState, false, outputOptions);
                        printWriter.print("0x" + Format.asHex(frCpuState.pc, 8) + " " + statement);
                    }
                }
                
                switch (((FrInstruction)(statement.getInstruction())).encoding) {
                    case 0xA600: /* ADD Rj, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) + (frCpuState.getReg(statement.j) & 0xFFFFFFFFL);
                        result32 = (int) result64;
                        S1 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        S2 = (frCpuState.getReg(statement.j) & 0x80000000) >>> 31;
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        frCpuState.N = Sr;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;
                        frCpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA400: /* ADD #i4, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) + statement.imm;
                        result32 = (int) result64;
                        S1 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        S2 = 0; /* unsigned extension of x means positive */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        frCpuState.N = Sr;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;
                        frCpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA500: /* ADD2 #i4, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) + (BinaryArithmetics.extn(4, statement.imm) & 0xFFFFFFFFL);
                        result32 = (int) result64;
                        S1 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        S2 = 1; /* negative extension of x means negative */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        frCpuState.N = Sr;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;
                        frCpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA700: /* ADDC Rj, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) + (frCpuState.getReg(statement.j) & 0xFFFFFFFFL) + frCpuState.C;
                        result32 = (int) result64;
                        S1 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        S2 = (frCpuState.getReg(statement.j) & 0x80000000) >>> 31; // TODO : Shouldn't it take C into account ?
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        frCpuState.N = Sr;
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA200: /* ADDN Rj, Ri */
                        frCpuState.setReg(statement.i, frCpuState.getReg(statement.i) + frCpuState.getReg(statement.j));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA000: /* ADDN #i4, Ri */
                        frCpuState.setReg(statement.i, frCpuState.getReg(statement.i) + statement.imm);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA100: /* ADDN2 #i4, Ri */
                        frCpuState.setReg(statement.i, frCpuState.getReg(statement.i) + BinaryArithmetics.extn(4, statement.imm));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAC00: /* SUB Rj, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) - (frCpuState.getReg(statement.j) & 0xFFFFFFFFL);
                        S1 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        S2 = (frCpuState.getReg(statement.j) & 0x80000000) >>> 31;
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        frCpuState.N = Sr;
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
    
                        frCpuState.setReg(statement.i, (int) result64);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAD00: /* SUBC Rj, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) - (frCpuState.getReg(statement.j) & 0xFFFFFFFFL) - frCpuState.C;
                        S1 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        S2 = (frCpuState.getReg(statement.j) & 0x80000000) >>> 31; // TODO : Shouldn't it take C into account ?
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        frCpuState.N = Sr;
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
    
                        frCpuState.setReg(statement.i, (int) result64);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAE00: /* SUBN Rj, Ri */
                        frCpuState.setReg(statement.i, frCpuState.getReg(statement.i) - frCpuState.getReg(statement.j));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAA00: /* CMP Rj, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) - (frCpuState.getReg(statement.j) & 0xFFFFFFFFL);
                        S1 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        S2 = (frCpuState.getReg(statement.j) & 0x80000000) >>> 31;
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        frCpuState.N = Sr;
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA800: /* CMP #i4, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) - statement.imm;
                        /* optimize : 0 extension of x means S2 is 0, right ?  */
                        S1 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        S2 = 0; /* unsigned extension of x means positive */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        frCpuState.N = Sr;
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA900: /* CMP2 #i4, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) - (BinaryArithmetics.extn(4, statement.imm) & 0xFFFFFFFFL);
                        S1 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        S2 = 1; /* negative extension of x means negative */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        frCpuState.N = Sr;
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x8200: /* AND Rj, Ri */
                        result32 = frCpuState.getReg(statement.i) & frCpuState.getReg(statement.j);
                        frCpuState.setReg(statement.i, result32);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x8400: /* AND Rj, @Ri */
                        result32 = memory.load32(frCpuState.getReg(statement.i)) & frCpuState.getReg(statement.j);
                        memory.store32(frCpuState.getReg(statement.i), result32);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8500: /* ANDH Rj, @Ri */
                        result32 = memory.loadUnsigned16(frCpuState.getReg(statement.i)) & frCpuState.getReg(statement.j);
                        memory.store16(frCpuState.getReg(statement.i), result32);
    
                        frCpuState.N = (result32 & 0x8000) >>> 15;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8600: /* ANDB Rj, @Ri */
                        result32 = memory.loadUnsigned8(frCpuState.getReg(statement.i)) & frCpuState.getReg(statement.j);
                        memory.store8(frCpuState.getReg(statement.i), result32);
    
                        frCpuState.N = (result32 & 0x80) >>> 7;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9200: /* OR Rj, Ri */
                        result32 = frCpuState.getReg(statement.i) | frCpuState.getReg(statement.j);
                        frCpuState.setReg(statement.i, result32);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9400: /* OR Rj, @Ri */
                        result32 = memory.load32(frCpuState.getReg(statement.i)) | frCpuState.getReg(statement.j);
                        memory.store32(frCpuState.getReg(statement.i), result32);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9500: /* ORH Rj, @Ri */
                        result32 = memory.loadUnsigned16(frCpuState.getReg(statement.i)) | frCpuState.getReg(statement.j);
                        memory.store16(frCpuState.getReg(statement.i), result32);
    
                        frCpuState.N = (result32 & 0x8000) >>> 15;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9600: /* ORB Rj, @Ri */
                        result32 = memory.loadUnsigned8(frCpuState.getReg(statement.i)) | frCpuState.getReg(statement.j);
                        memory.store8(frCpuState.getReg(statement.i), result32);
    
                        frCpuState.N = (result32 & 0x80) >>> 7;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9A00: /* EOR Rj, Ri */
                        result32 = frCpuState.getReg(statement.i) ^ frCpuState.getReg(statement.j);
                        frCpuState.setReg(statement.i, result32);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9C00: /* EOR Rj, @Ri */
                        result32 = memory.load32(frCpuState.getReg(statement.i)) ^ frCpuState.getReg(statement.j);
                        memory.store32(frCpuState.getReg(statement.i), result32);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9D00: /* EORH Rj, @Ri */
                        result32 = memory.loadUnsigned16(frCpuState.getReg(statement.i)) ^ frCpuState.getReg(statement.j);
                        memory.store16(frCpuState.getReg(statement.i), result32);
    
                        frCpuState.N = (result32 & 0x8000) >>> 15;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9E00: /* EORB Rj, @Ri */
                        result32 = memory.loadUnsigned8(frCpuState.getReg(statement.i)) ^ frCpuState.getReg(statement.j);
                        memory.store8(frCpuState.getReg(statement.i), result32);
    
                        frCpuState.N = (result32 & 0x80) >>> 7;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8000: /* BANDL #u4, @Ri (u4: 0 to 0FH) */
                        // Note : AND'ing only the lowest 4 bits with xxxx is like AND'ing the byte with 1111xxxx (1 is neutral for AND)
                        memory.store8(frCpuState.getReg(statement.i), memory.loadUnsigned8(frCpuState.getReg(statement.i)) & (0xF0 + statement.imm));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8100: /* BANDH #u4, @Ri (u4: 0 to 0FH) */
                        // Note : AND'ing only the highest 4 bits with xxxx is like AND'ing the byte with xxxx1111 (1 is neutral for AND)
                        memory.store8(frCpuState.getReg(statement.i), memory.loadUnsigned8(frCpuState.getReg(statement.i)) & ((statement.imm << 4) + 0x0F));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9000: /* BORL #u4, @Ri (u4: 0 to 0FH) */
                        // Note : OR'ing only the lowest 4 bits with xxxx is like OR'ing the byte with 0000xxxx (0 is neutral for OR)
                        memory.store8(frCpuState.getReg(statement.i), memory.loadUnsigned8(frCpuState.getReg(statement.i)) | statement.imm);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9100: /* BORH #u4, @Ri (u4: 0 to 0FH) */
                        // Note : OR'ing only the highest 4 bits with xxxx is like OR'ing the byte with xxxx0000 (0 is neutral for OR)
                        memory.store8(frCpuState.getReg(statement.i), memory.loadUnsigned8(frCpuState.getReg(statement.i)) | (statement.imm << 4));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9800: /* BEORL #u4, @Ri (u4: 0 to 0FH) */
                        // Note : EOR'ing with 0000xxxx is like EOR'ing only the lowest 4 bits with xxxx (0 is neutral for EOR)
                        memory.store8(frCpuState.getReg(statement.i), memory.loadUnsigned8(frCpuState.getReg(statement.i)) ^ statement.imm);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9900: /* BEORH #u4, @Ri (u4: 0 to 0FH) */
                        // Note : EOR'ing with xxxx0000 is like EORing only the highest 4 bits with xxxx (0 is neutral for EOR)
                        memory.store8(frCpuState.getReg(statement.i), memory.loadUnsigned8(frCpuState.getReg(statement.i)) ^ (statement.imm << 4));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8800: /* BTSTL #u4, @Ri (u4: 0 to 0FH) */
                        // Note : testing 8 bits AND 0000xxxx is like testing only the lowest 4 bits AND xxxx (0 is absorbing for AND)
                        result32 = memory.loadUnsigned8(frCpuState.getReg(statement.i)) & statement.imm;
    
                        frCpuState.N = 0;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 2 + a;
                        break;
    
                    case 0x8900: /* BTSTH #u4, @Ri (u4: 0 to 0FH) */
                        // Note : testing 8 bits AND xxxx0000 is like testing only the highest 4 bits AND xxxx (0 is absorbing for AND)
                        result32 = memory.loadUnsigned8(frCpuState.getReg(statement.i)) & (statement.imm << 4);
    
                        frCpuState.N = (result32 & 0x80) >>> 7;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 2 + a;
                        break;
    
                    case 0xAF00: /* MUL Rj,Ri */
                        result64 = ((long) frCpuState.getReg(statement.j)) * ((long) frCpuState.getReg(statement.i));
                        frCpuState.setReg(FrCPUState.MDH, (int) (result64 >> 32));
                        frCpuState.setReg(FrCPUState.MDL, (int) (result64 & 0xFFFFFFFFL));
    
                        frCpuState.N = (int) ((result64 & 0x80000000L) >>> 31); /*see pdf*/
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.V = (int) ((result64 & 0x100000000L) >>> 32);

                        frCpuState.pc += 2;

                        cycles = 5;
                        break;
    
                    case 0xAB00: /* MULU Rj,Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) * (frCpuState.getReg(statement.j) & 0xFFFFFFFFL);
                        frCpuState.setReg(FrCPUState.MDH, (int) (result64 >> 32));
                        frCpuState.setReg(FrCPUState.MDL, (int) (result64 & 0xFFFFFFFFL));
    
                        frCpuState.N = (int) ((result64 & 0x80000000L) >>> 31); /*see pdf*/
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.V = (int) ((result64 & 0x100000000L) >>> 32);

                        frCpuState.pc += 2;

                        cycles = 5;
                        break;
    
                    case 0xBF00: /* MULH Rj,Ri */
                        result32 = ((short) frCpuState.getReg(statement.j)) * ((short) frCpuState.getReg(statement.i));
                        frCpuState.setReg(FrCPUState.MDL, result32);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 3;
                        break;
    
                    case 0xBB00: /* MULUH Rj,Ri */
                        result32 = (frCpuState.getReg(statement.j) & 0xFFFF) * (frCpuState.getReg(statement.i) & 0xFFFF);
                        frCpuState.setReg(FrCPUState.MDL, result32);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;

                        frCpuState.pc += 2;

                        cycles = 3;
                        break;
    
                    case 0x9740: /* DIV0S Ri */
                        S1 = (frCpuState.getReg(FrCPUState.MDL) & 0x80000000) >>> 31;
                        S2 = (frCpuState.getReg(statement.i) & 0x80000000) >>> 31;
                        frCpuState.D0= S1;
                        frCpuState.D1= S1 ^ S2;
                        result64 = (long) frCpuState.getReg(FrCPUState.MDL);
                        frCpuState.setReg(FrCPUState.MDH, (int) (result64 >>> 32));
                        frCpuState.setReg(FrCPUState.MDL, (int) (result64 & 0xFFFFFFFFL));

                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9750: /* DIV0U Ri */
                        frCpuState.D0=0;
                        frCpuState.D1=0;
                        frCpuState.setReg(FrCPUState.MDH, 0);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9760: /* DIV1 Ri */
                        frCpuState.setReg(FrCPUState.MDH, (frCpuState.getReg(FrCPUState.MDH) << 1) | ((frCpuState.getReg(FrCPUState.MDL) & 0x80000000) >>> 31));
                        frCpuState.setReg(FrCPUState.MDL, frCpuState.getReg(FrCPUState.MDL) << 1);
                        if (frCpuState.D1 == 1) {
                            // Dividend and divisor have opposite signs
                            result64 = (frCpuState.getReg(FrCPUState.MDH) & 0xFFFFFFFFL) + (frCpuState.getReg(statement.i) & 0xFFFFFFFFL);
                            result32 = (int) result64;
                            frCpuState.C = (int) ((result64 & 0x100000000L) >>> 32);
                            frCpuState.Z = (result32 == 0)?1:0;
                        }
                        else {
                            // Dividend and divisor have same signs
                            result64 = (frCpuState.getReg(FrCPUState.MDH) & 0xFFFFFFFFL) - (frCpuState.getReg(statement.i) & 0xFFFFFFFFL);
                            result32 = (int) result64;
                            frCpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
                            frCpuState.Z = (result32 == 0)?1:0;
                        }
                        if ((frCpuState.D0 ^ frCpuState.D1 ^ frCpuState.C) == 0) {
                            frCpuState.setReg(FrCPUState.MDH, result32);
                            frCpuState.setReg(FrCPUState.MDL, frCpuState.getReg(FrCPUState.MDL) | 1);
                        }

                        frCpuState.pc += 2;

                        cycles = d;
                        break;
    
                    case 0x9770: /* DIV2 Ri */
                        if (frCpuState.D1 == 1) {
                            result64 = frCpuState.getReg(FrCPUState.MDH) + frCpuState.getReg(statement.i);
                            result32 = (int) result64;
                            frCpuState.C = (result32 == result64) ? 0 : 1;
                            frCpuState.Z = (result32 == 0)?1:0;
                        }
                        else {
                            result64 = frCpuState.getReg(FrCPUState.MDH) - frCpuState.getReg(statement.i);
                            result32 = (int) result64;
                            frCpuState.C = (result32 == result64) ? 0 : 1;
                            frCpuState.Z = (result32 == 0)?1:0;
                        }
                        if (frCpuState.Z == 1) {
                            frCpuState.setReg(FrCPUState.MDH, 0);
                        }

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F60: /* DIV3 */
                        if (frCpuState.Z == 1) {
                            frCpuState.setReg(FrCPUState.MDL, frCpuState.getReg(FrCPUState.MDL) + 1);
                        }
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F70: /* DIV4S */
                        if (frCpuState.D1 == 1) {
                            frCpuState.setReg(FrCPUState.MDL, -frCpuState.getReg(FrCPUState.MDL));
                        }
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB600: /* LSL Rj, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) << (frCpuState.getReg(statement.j) & 0x1F);
    
                        frCpuState.N = (int) ((result64 & 0x80000000L) >>> 31);
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>> 32);
    
                        frCpuState.setReg(statement.i, (int) result64);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB400: /* LSL #u4, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) << statement.imm;
    
                        frCpuState.N = (int) ((result64 & 0x80000000L) >>> 31);
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.C = (statement.imm == 0) ? 0 : (int) ((result64 & 0x100000000L) >>> 32);
    
                        frCpuState.setReg(statement.i, (int) result64);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB500: /* LSL2 #u4, Ri */
                        result64 = (frCpuState.getReg(statement.i) & 0xFFFFFFFFL) << (statement.imm + 16);
    
                        frCpuState.N = (int) ((result64 & 0x80000000L) >>> 31);
                        frCpuState.Z = (result64 == 0) ? 1 : 0;
                        frCpuState.C = (int) ((result64 & 0x100000000L) >>> 32);
    
                        frCpuState.setReg(statement.i, (int) result64);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB200: /* LSR Rj, Ri */
                        result32 = frCpuState.getReg(statement.i) >>> (frCpuState.getReg(statement.j) & 0x1F);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;
                        frCpuState.C = ((frCpuState.getReg(statement.j) & 0x1F) == 0) ? 0 : (frCpuState.getReg(statement.i) >> ((frCpuState.getReg(statement.j) & 0x1F) - 1)) & 1;

                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB000: /* LSR #u4, Ri */
                        result32 = frCpuState.getReg(statement.i) >>> statement.imm;
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;
                        frCpuState.C = (statement.imm == 0) ? 0 : (frCpuState.getReg(statement.i) >> (statement.imm - 1)) & 1;
    
                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB100: /* LSR2 #u4, Ri */
                        result32 = frCpuState.getReg(statement.i) >>> (statement.imm + 16);
    
                        frCpuState.N = 0;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;
                        frCpuState.C = (frCpuState.getReg(statement.i) >> (statement.imm + 15)) & 1;
    
                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xBA00: /* ASR Rj, Ri */
                        result32 = frCpuState.getReg(statement.i) >> (frCpuState.getReg(statement.j) & 0x1F);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;
                        frCpuState.C = ((frCpuState.getReg(statement.j) & 0x1F) == 0) ? 0 : (frCpuState.getReg(statement.i) >> ((frCpuState.getReg(statement.j) & 0x1F) - 1)) & 1;
    
                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB800: /* ASR #u4, Ri */
                        result32 = frCpuState.getReg(statement.i) >> statement.imm;
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;
                        frCpuState.C = (statement.imm == 0) ? 0 : (frCpuState.getReg(statement.i) >> (statement.imm - 1)) & 1;
    
                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB900: /* ASR2 #u4, Ri */
                        result32 = frCpuState.getReg(statement.i) >> (statement.imm + 16);
    
                        frCpuState.N = (result32 & 0x80000000) >>> 31;
                        frCpuState.Z = (result32 == 0) ? 1 : 0;
                        frCpuState.C = (frCpuState.getReg(statement.i) >> (statement.imm + 15)) & 1;
    
                        frCpuState.setReg(statement.i, result32);

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F80: /* LDI:32 #i32, Ri */
                        frCpuState.setReg(statement.i, statement.imm);
    
                        /* No change to NZVC */

                        frCpuState.pc += 6;

                        cycles = 3;
                        break;
    
                    case 0x9B00: /* LDI:20 #i20, Ri */
                        frCpuState.setReg(statement.i, statement.imm);
    
                        /* No change to NZVC */

                        frCpuState.pc += 4;

                        cycles = 2;
                        break;
    
                    case 0xC000: /* LDI:8 #i8, Ri */
                        frCpuState.setReg(statement.i, statement.imm);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x0400: /* LD @Rj, Ri */
                        frCpuState.setReg(statement.i, memory.load32(frCpuState.getReg(statement.j)));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0000: /* LD @(R13,Rj), Ri */
                        frCpuState.setReg(statement.i, memory.load32(frCpuState.getReg(13) + frCpuState.getReg(statement.j)));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x2000: /* LD @(R14,disp10), Ri */
                        frCpuState.setReg(statement.i, memory.load32(frCpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm) * 4));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0300: /* LD @(R15,udisp6), Ri */
                        frCpuState.setReg(statement.i, memory.load32(frCpuState.getReg(15) + statement.imm * 4));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0700: /* LD @R15+, Ri */
                        frCpuState.setReg(statement.i, memory.load32(frCpuState.getReg(15)));
                        frCpuState.setReg(15, frCpuState.getReg(15) + 4);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0780: /* LD @R15+, Rs */
                    case 0x0781:
                    case 0x0782:
                    case 0x0783:
                    case 0x0784:
                    case 0x0785:
                        frCpuState.setReg(FrCPUState.DEDICATED_REG_OFFSET + statement.i, memory.load32(frCpuState.getReg(15)));
                        frCpuState.setReg(15, frCpuState.getReg(15) + 4);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0790: /* LD @R15+, PS */
                        frCpuState.setPS(memory.load32(frCpuState.getReg(15)), true);
                        frCpuState.setReg(15, frCpuState.getReg(15) + 4);
    
                        /* NZVC is part of the PS !*/

                        frCpuState.pc += 2;

                        cycles = 1 + a + b;
                        break;
    
                    case 0x0500: /* LDUH @Rj, Ri */
                        frCpuState.setReg(statement.i, memory.loadUnsigned16(frCpuState.getReg(statement.j)));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0100: /* LDUH @(R13,Rj), Ri */
                        frCpuState.setReg(statement.i, memory.loadUnsigned16(frCpuState.getReg(13) + frCpuState.getReg(statement.j)));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x4000: /* LDUH @(R14,disp9), Ri */
                        frCpuState.setReg(statement.i, memory.loadUnsigned16(frCpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm) * 2));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0600: /* LDUB @Rj, Ri */
                        frCpuState.setReg(statement.i, memory.loadUnsigned8(frCpuState.getReg(statement.j)));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0200: /* LDUB @(R13,Rj), Ri */
                        frCpuState.setReg(statement.i, memory.loadUnsigned8(frCpuState.getReg(13) + frCpuState.getReg(statement.j)));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x6000: /* LDUB @(R14,disp8), Ri */
                        frCpuState.setReg(statement.i, memory.loadUnsigned8(frCpuState.getReg(14) + statement.imm));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1400: /* ST Ri, @Rj */
                        memory.store32(frCpuState.getReg(statement.j), frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1000: /* ST Ri, @(R13,Rj) */
                        memory.store32(frCpuState.getReg(13) + frCpuState.getReg(statement.j), frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x3000: /* ST Ri, @(R14,disp10) */
                        memory.store32(frCpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm) * 4, frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1300: /* ST Ri, @(R15,udisp6) */
                        memory.store32(frCpuState.getReg(15) + statement.imm * 4, frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1700: /* ST Ri, @-R15 */
                        frCpuState.setReg(15, frCpuState.getReg(15) - 4);
                        if (statement.i == 15) {
                            /*special case for R15: value stored is R15 before it was decremented */
                            memory.store32(frCpuState.getReg(15), frCpuState.getReg(15) + 4);
                        }
                        else {
                            memory.store32(frCpuState.getReg(15), frCpuState.getReg(statement.i));
                        }
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1780: /* ST Rs, @-R15 */
                    case 0x1781:
                    case 0x1782:
                    case 0x1783:
                    case 0x1784:
                    case 0x1785:
                        frCpuState.setReg(15, frCpuState.getReg(15) - 4);
                        memory.store32(frCpuState.getReg(15), frCpuState.getReg(FrCPUState.DEDICATED_REG_OFFSET + statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1790: /* ST PS, @-R15 */
                        frCpuState.setReg(15, frCpuState.getReg(15) - 4);
                        memory.store32(frCpuState.getReg(15), frCpuState.getPS());
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1500: /* STH Ri, @Rj */
                        memory.store16(frCpuState.getReg(statement.j), frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1100: /* STH Ri, @(R13,Rj) */
                        memory.store16(frCpuState.getReg(13) + frCpuState.getReg(statement.j), frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x5000: /* STH Ri, @(R14,disp9) */
                        memory.store16(frCpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm) * 2, frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1600: /* STB Ri, @Rj */
                        memory.store8(frCpuState.getReg(statement.j), frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1200: /* STB Ri, @(R13,Rj) */
                        memory.store8(frCpuState.getReg(13) + frCpuState.getReg(statement.j), frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x7000: /* STB Ri, @(R14,disp8) */
                        memory.store8(frCpuState.getReg(14) + BinaryArithmetics.signExtend(8, statement.imm), frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x8B00: /* MOV Rj, Ri */
                        frCpuState.setReg(statement.i, frCpuState.getReg(statement.j));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB700: /* MOV Rs, Ri */
                    case 0xB710:
                    case 0xB720:
                    case 0xB730:
                    case 0xB740:
                    case 0xB750:
                        frCpuState.setReg(statement.i, frCpuState.getReg(FrCPUState.DEDICATED_REG_OFFSET + statement.j));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB300: /* MOV Ri, Rs */
                    case 0xB310:
                    case 0xB320:
                    case 0xB330:
                    case 0xB340:
                    case 0xB350:
                        frCpuState.setReg(FrCPUState.DEDICATED_REG_OFFSET + statement.j, frCpuState.getReg(statement.i));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x1710: /* MOV PS, Ri */
                        frCpuState.setReg(statement.i, frCpuState.getPS());
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x0710: /* MOV Ri, PS */
                        frCpuState.setPS(frCpuState.getReg(statement.i), true);
    
                        /* NZVC is part of the PS !*/

                        frCpuState.pc += 2;

                        cycles = c;
                        break;
    
                    case 0x9700: /* JMP @Ri */
                        frCpuState.pc = frCpuState.getReg(statement.i);
    
                        /* No change to NZVC */

                        cycles = 2;
                        break;
    
                    case 0xD000: /* CALL label12 */
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null) {
                                    pushInstruction(statement);
                                }
                            }
                        }
                        frCpuState.setReg(FrCPUState.RP, frCpuState.pc + 2);
                        frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(11, statement.imm) * 2; // TODO check *2 ?

                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0x9710: /* CALL @Ri */
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null) {
                                    pushInstruction(statement);
                                }
                            }
                        }
                        frCpuState.setReg(FrCPUState.RP, frCpuState.pc + 2);
                        frCpuState.pc = frCpuState.getReg(statement.i);
    
                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0x9720: /* RET */
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null && !callStack.isEmpty()) {
                                    callStack.pop();
                                }
                            }
                        }
                        frCpuState.pc = frCpuState.getReg(FrCPUState.RP);
    
                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0x1F00: /* INT #u8 */
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null) {
                                    pushInstruction(statement);
                                }
                            }
                        }
                        processInterrupt(statement.imm, frCpuState.pc + 2);
                        frCpuState.I = 0;

                        /* No change to NZVC */
    
                        cycles = 3 + 3 * a;
                        break;
    
                    case 0x9F30: /* INTE */
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null) {
                                    pushInstruction(statement);
                                }
                            }
                        }
                        frCpuState.setReg(FrCPUState.SSP, frCpuState.getReg(FrCPUState.SSP) - 4);
                        memory.store32(frCpuState.getReg(FrCPUState.SSP), frCpuState.getPS());
                        frCpuState.setReg(FrCPUState.SSP, frCpuState.getReg(FrCPUState.SSP) - 4);
                        memory.store32(frCpuState.getReg(FrCPUState.SSP), frCpuState.pc + 2);
                        frCpuState.setS(0);
                        frCpuState.setILM(4, false);
                        frCpuState.pc = memory.load32(frCpuState.getReg(FrCPUState.TBR) + 0x3D8);
    
                        /* No change to NZVC */
    
                        cycles = 3 + 3 * a;
                        break;
    
                    case 0x9730: /* RETI */
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null && !callStack.isEmpty()) {
                                    callStack.pop();
                                }                                    
                            }
                        }
                        frCpuState.pc = memory.load32(frCpuState.getReg(15));
                        frCpuState.setReg(15, frCpuState.getReg(15) + 8);
                        /* note : this is the order given in the spec but loading PS below could switch the USP<>SSP,
                        so the last SP increment would address the wrong stack
                        Doing it this way passes the test */
                        frCpuState.setPS(memory.load32(frCpuState.getReg(15) - 4), false);
    
                        /* NZVC is part of the PS !*/
    
                        cycles = 2 + 2 * a;
                        break;
    
                    case 0xE100: /* BNO label9 */
                        /* No branch */
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xE000: /* BRA label9 */
                        frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
    
                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0xE200: /* BEQ label9 */
                        if (frCpuState.Z == 1) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }

                        /* No change to NZVC */
    
                        break;
    
                    case 0xE300: /* BNE label9 */
                        if (frCpuState.Z == 0) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE400: /* BC label9 */
                        if (frCpuState.C == 1) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE500: /* BNC label9 */
                        if (frCpuState.C == 0) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE600: /* BN label9 */
                        if (frCpuState.N == 1) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE700: /* BP label9 */
                        if (frCpuState.N == 0) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE800: /* BV label9 */
                        if (frCpuState.V == 1) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE900: /* BNV label9 */
                        if (frCpuState.V == 0) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEA00: /* BLT label9 */
                        if ((frCpuState.V ^ frCpuState.N) == 1) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEB00: /* BGE label9 */
                        if ((frCpuState.V ^ frCpuState.N) == 0) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEC00: /* BLE label9 */
                        if (((frCpuState.V ^ frCpuState.N) | frCpuState.Z) == 1) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xED00: /* BGT label9 */
                        if (((frCpuState.V ^ frCpuState.N) | frCpuState.Z) == 0) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEE00: /* BLS label9 */
                        if ((frCpuState.C | frCpuState.Z) == 1) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEF00: /* BHI label9 */
                        if ((frCpuState.C | frCpuState.Z) == 0) {
                            frCpuState.pc = frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2;
                            cycles = 2;
                        }
                        else {
                            frCpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0x9F00: /* JMP:D @Ri */
                        setDelayedChanges(frCpuState.getReg(statement.i), null);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xD800: /* CALL:D label12 */
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null) {
                                    pushInstruction(statement);
                                }
                            }
                        }
                        setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(11, statement.imm) * 2, frCpuState.pc + 4);  // TODO check *2
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F10: /* CALL:D @Ri */
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null) {
                                    pushInstruction(statement);
                                }
                            }
                        }
                        setDelayedChanges(frCpuState.getReg(statement.i), frCpuState.pc + 4);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F20: /* RET:D */
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null && !callStack.isEmpty()) {
                                    callStack.pop();
                                }
                            }
                        }
                        setDelayedChanges(frCpuState.getReg(FrCPUState.RP), null);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xF100: /* BNO:D label9 */
                        /* No branch */
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xF000: /* BRA:D label9 */
                        setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xF200: /* BEQ:D label9 */
                        if (frCpuState.Z == 1) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;

                        //* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF300: /* BNE:D label9 */
                        if (frCpuState.Z == 0) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF400: /* BC:D label9 */
                        if (frCpuState.C == 1) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF500: /* BNC:D label9 */
                        if (frCpuState.C == 0) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF600: /* BN:D label9 */
                        if (frCpuState.N == 1) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF700: /* BP:D label9 */
                        if (frCpuState.N == 0) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF800: /* BV:D label9 */
                        if (frCpuState.V == 1) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF900: /* BNV:D label9 */
                        if (frCpuState.V == 0) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFA00: /* BLT:D label9 */
                        if ((frCpuState.V ^ frCpuState.N) == 1) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFB00: /* BGE:D label9 */
                        if ((frCpuState.V ^ frCpuState.N) == 0) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFC00: /* BLE:D label9 */
                        if (((frCpuState.V ^ frCpuState.N) | frCpuState.Z) == 1) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFD00: /* BGT:D label9 */
                        if (((frCpuState.V ^ frCpuState.N) | frCpuState.Z) == 0) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFE00: /* BLS:D label9 */
                        if ((frCpuState.C | frCpuState.Z) == 1) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;

                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFF00: /* BHI:D label9 */
                        if ((frCpuState.C | frCpuState.Z) == 0) {
                            setDelayedChanges(frCpuState.pc + 2 + BinaryArithmetics.signExtend(8, statement.imm) * 2, null);
                        }

                        frCpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0x0800: /* DMOV @dir10, R13 */
                        frCpuState.setReg(13, memory.load32(statement.imm * 4));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1800: /* DMOV R13, @dir10 */
                        memory.store32(statement.imm * 4, frCpuState.getReg(13));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x0C00: /* DMOV @dir10, @R13+ */
                        memory.store32(frCpuState.getReg(13), memory.load32(statement.imm * 4));
                        frCpuState.setReg(13, frCpuState.getReg(13) + 4);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1C00: /* DMOV @R13+, @dir10 */
                        memory.store32(statement.imm * 4, memory.load32(frCpuState.getReg(13)));
                        frCpuState.setReg(13, frCpuState.getReg(13) + 4);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x0B00: /* DMOV @dir10, @-R15 */
                        frCpuState.setReg(15, frCpuState.getReg(15) - 4);
                        memory.store32(frCpuState.getReg(15), memory.load32(statement.imm * 4));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1B00: /* DMOV @R15+, @dir10 */
                        memory.store32(statement.imm * 4, memory.load32(frCpuState.getReg(15)));
                        frCpuState.setReg(15, frCpuState.getReg(15) + 4);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x0900: /* DMOVH @dir9, R13 */
                        frCpuState.setReg(13, memory.loadUnsigned16(statement.imm * 2));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1900: /* DMOVH R13, @dir9 */
                        memory.store16(statement.imm * 2, frCpuState.getReg(13));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x0D00: /* DMOVH @dir9, @R13+ */
                        memory.store16(frCpuState.getReg(13), memory.loadUnsigned16(statement.imm * 2));
                        frCpuState.setReg(13, frCpuState.getReg(13) + 2);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1D00: /* DMOVH @R13+, @dir9 */
                        memory.store16(statement.imm * 2, memory.loadUnsigned16(frCpuState.getReg(13)));
                        frCpuState.setReg(13, frCpuState.getReg(13) + 2);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x0A00: /* DMOVB @dir8, R13 */
                        frCpuState.setReg(13, memory.loadUnsigned8(statement.imm));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1A00: /* DMOVB R13, @dir8 */
                        memory.store8(statement.imm, frCpuState.getReg(13));
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x0E00: /* DMOVB @dir8, @R13+ */
                        memory.store8(frCpuState.getReg(13), memory.loadUnsigned8(statement.imm));
                        frCpuState.setReg(13, frCpuState.getReg(13) + 1);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1E00: /* DMOVB @R13+, @dir8 */
                        memory.store8(statement.imm, memory.loadUnsigned8(frCpuState.getReg(13)));
                        frCpuState.setReg(13, frCpuState.getReg(13) + 1);
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0xBC00: /* LDRES @Ri+, #u4 */
                        /* TODO FUTURE */
                        System.err.println(statement.getInstruction().toString() + " is not implemented (resource) at PC=0x" + Format.asHex(frCpuState.pc-2,8));
                        /*sentToResource(x, memory.load32(frCpuState.getReg(i)));
                        frCpuState.getReg(i) + = 4;*/
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0xBD00: /* STRES #u4, @Ri+ */
                        /* TODO FUTURE */
                        System.err.println(statement.getInstruction().toString() + " is not implemented (resource) at PC=0x" + Format.asHex(frCpuState.pc-2,8));
                        /* memory.store32(frCpuState.getReg(i), getFromResource(x); frCpuState.getReg(i) + = 4;*/
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x9FC0: /* COPOP #u4, #CC, CRj, CRi */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(statement.getInstruction().toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(frCpuState.pc-2,8));
    
                        /* No change to NZVC */

                        frCpuState.pc += 4;

                        cycles = 2 + a;
                        break;
    
                    case 0x9FD0: /* COPLD #u4, #CC, Rj, CRi */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(statement.getInstruction().toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(frCpuState.pc-2,8));
                        /* frCpuState.getReg(CPUState.COPROCESSOR_REG_OFFSET + i) = frCpuState.getReg(j); */
    
                        /* No change to NZVC */

                        frCpuState.pc += 4;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9FE0: /* COPST #u4, #CC, CRj, Ri */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(statement.getInstruction().toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(frCpuState.pc-2,8));
                        /* frCpuState.getReg(i) = frCpuState.getReg(CPUState.COPROCESSOR_REG_OFFSET + j); */
    
                        /* No change to NZVC */

                        frCpuState.pc += 4;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9FF0: /* COPSV #u4, #CC, CRj, Ri */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(statement.getInstruction().toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(frCpuState.pc-2,8));
                        /* frCpuState.getReg(i) = frCpuState.getReg(CPUState.COPROCESSOR_REG_OFFSET + j);*/
    
                        /* No change to NZVC */

                        frCpuState.pc += 4;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9FA0: /* NOP */
                        /* No change */
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x8300: /* ANDCCR #u8 */
                        frCpuState.setCCR(frCpuState.getCCR() & statement.imm);
    
                        /* NZVC is part of the CCR !*/

                        frCpuState.pc += 2;

                        cycles = c;
                        break;
                    
                    case 0x9300: /* ORCCR #u8 */
                        frCpuState.setCCR(frCpuState.getCCR() | statement.imm);
    
                        /* NZVC is part of the CCR !*/

                        frCpuState.pc += 2;

                        cycles = c;
                        break;
                    
                    case 0x8700: /* STILM #u8 */
                        frCpuState.setILM(statement.imm, true);

                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0xA300: /* ADDSP #s10 */
                        frCpuState.setReg(15, frCpuState.getReg(15) + (BinaryArithmetics.signExtend(8, statement.imm) * 4));
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x9780: /* EXTSB Ri */
                        frCpuState.setReg(statement.i, BinaryArithmetics.signExtend(8, frCpuState.getReg(statement.i)));
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x9790: /* EXTUB Ri */
                        frCpuState.setReg(statement.i, frCpuState.getReg(statement.i) & 0xFF);
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x97A0: /* EXTSH Ri */
                        frCpuState.setReg(statement.i, BinaryArithmetics.signExtend(16, frCpuState.getReg(statement.i)));
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x97B0: /* EXTUH Ri */
                        frCpuState.setReg(statement.i, frCpuState.getReg(statement.i) & 0xFFFF);
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;

                    case 0x97C0: /* SRCH0 Ri */
                        // Search for the first 0
                        frCpuState.setReg(statement.i, bitSearch(frCpuState.getReg(statement.i), 0));

                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;

                    case 0x97D0: /* SRCH1 Ri */
                        // Search for the first 1
                        frCpuState.setReg(statement.i, bitSearch(frCpuState.getReg(statement.i), 1));

                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;

                    case 0x97E0: /* SRCHC Ri */
                        // Search for the first bit different from the MSB
                        result32 = frCpuState.getReg(statement.i);
                        frCpuState.setReg(statement.i, bitSearch(result32, (result32 & 0x80000000)==0?1:0));

                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1;
                        break;

                    case 0x8C00: /* LDM0 (reglist) */
                        n = 0;
                        for (int r = 0; r <= 7; r++) {
                            if ((statement.imm & (1 << r)) != 0) {
                                frCpuState.setReg(r, memory.load32(frCpuState.getReg(15)));
                                frCpuState.setReg(15, frCpuState.getReg(15) + 4);
                                n++;
                            }
                        }
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = (n == 0) ? 1 : (a * (n - 1) + b + 1);
                        break;
                    
                    case 0x8D00: /* LDM1 (reglist) */
                        n = 0;
                        for (int r = 0; r <= 7; r++) {
                            if ((statement.imm & (1 << r)) != 0) {
                                frCpuState.setReg(r + 8, memory.load32(frCpuState.getReg(15)));
                                frCpuState.setReg(15, frCpuState.getReg(15) + 4);
                                n++;
                            }
                        }
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = (n == 0) ? 1 : a * (n - 1) + b + 1;
                        break;
                    
                    case 0x8E00: /* STM0 (reglist) */
                        n = 0;
                        for (int r = 0; r <= 7; r++) {
                            if ((statement.imm & (1 << r)) != 0) {
                                frCpuState.setReg(15, frCpuState.getReg(15) - 4);
                                memory.store32(frCpuState.getReg(15), frCpuState.getReg(7-r));
                                n++;
                            }
                        }

                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a * n + 1;
                        break;
                    
                    case 0x8F00: /* STM1 (reglist) */
                        n = 0;
                        if ((statement.imm & 0x1) != 0) {
                            frCpuState.setReg(15, frCpuState.getReg(15) - 4);
                            /*special case for R15: value stored is R15 before it was decremented */
                            memory.store32(frCpuState.getReg(15), frCpuState.getReg(15) + 4);
                            n++;
                        }
                        for (int r = 1; r <= 7; r++) {
                            if ((statement.imm & (1 << r)) != 0) {
                                frCpuState.setReg(15, frCpuState.getReg(15) - 4);
                                memory.store32(frCpuState.getReg(15), frCpuState.getReg((7-r) + 8));
                                n++;
                            }
                        }
    
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = a * n + 1;
                        break;
                    
                    case 0x0F00: /* ENTER #u10 */
                        memory.store32(frCpuState.getReg(15) - 4, frCpuState.getReg(14));
                        frCpuState.setReg(14, frCpuState.getReg(15) - 4);
                        frCpuState.setReg(15, frCpuState.getReg(15) - statement.imm * 4);
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 1 + a;
                        break;
                    
                    case 0x9F90: /* LEAVE */
                        frCpuState.setReg(15, frCpuState.getReg(14) + 4);
                        frCpuState.setReg(14, memory.load32(frCpuState.getReg(15) - 4));
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = b;
                        break;
                    
                    case 0x8A00: /* XCHB @Rj, Ri */
                        result32 = frCpuState.getReg(statement.i);
                        frCpuState.setReg(statement.i, memory.loadUnsigned8(frCpuState.getReg(statement.j)));
                        memory.store8(frCpuState.getReg(statement.j), result32);
                        
                        /* No change to NZVC */

                        frCpuState.pc += 2;

                        cycles = 2 * a;
                        break;
                    
                    default:
                        String msg = "; Unknown instruction : " + Format.asHex(frCpuState.pc, 8) + " " + Format.asHex(statement.imm,4) + ". Triggering unknown instruction exception...";
                        System.out.println(msg);
                        if (instructionPrintWriter != null) {
                            instructionPrintWriter.println(msg);
                        }
                        if (callStack != null) {
                            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
                            synchronized (callStack) {
                                if (callStack != null) {
                                    pushInstruction(statement);
                                }
                            }
                        }

                        processInterrupt(0x0E, frCpuState.pc);

                        /* No change to NZVC */

                        cycles = 7;
                        break;

                }

                totalCycles += cycles;

                /* Delay slot processing */
                if (nextPC != null) {
                    if (delaySlotDone) {
                        frCpuState.pc = nextPC;
                        nextPC = null;
                        if (nextReturnAddress != null) {
                            frCpuState.setReg(FrCPUState.RP, nextReturnAddress);
                            nextReturnAddress = null;
                        }
                    }
                    else {
                        delaySlotDone = true;
                    }
                }
                else {
                    // If not in a delay slot, check interrupts
                    if(interruptController.hasPendingRequests()) { // This call is not synchronized, so it skips fast
                        InterruptRequest interruptRequest = interruptController.getNextRequest();
                        //Double test because lack of synchronization means the status could have changed in between
                        if (interruptRequest != null) {
                            if (frCpuState.accepts(interruptRequest)){
                                if (instructionPrintWriter != null) {
                                    PrintWriter printWriter = instructionPrintWriter;
                                    if (printWriter != null) {
                                        printWriter.println("------------------------- Accepting " + interruptRequest);
                                    }
                                }
                                interruptController.removeRequest(interruptRequest);
                                processInterrupt(interruptRequest.getInterruptNumber(), frCpuState.pc);
                                frCpuState.setILM(interruptRequest.getICR(), false);
                            }
                        }
                    }
                }

                /* Break if requested */
                if (!breakConditions.isEmpty()) {
                    //Double test to avoid useless synchronization if empty, at the cost of a double test when not empty (debug)
                    synchronized (breakConditions) {
                        for (BreakCondition breakCondition : breakConditions) {
                            if(breakCondition.matches(frCpuState, memory)) {
                                BreakTrigger trigger = breakCondition.getBreakTrigger();
                                if (trigger != null) {
                                    if (trigger.mustBeLogged() && breakLogPrintWriter != null) {
                                        trigger.log(breakLogPrintWriter, frCpuState, callStack, memory);
                                    }
                                    if (trigger.getInterruptToRequest() != null) {
                                        interruptController.request(trigger.getInterruptToRequest());
                                    }
                                    if (trigger.getPcToSet() != null) {
                                        frCpuState.pc = trigger.getPcToSet();
                                    }
                                }
                                if (trigger == null || trigger.mustBreak()) {
                                    return breakCondition;
                                }
                            }
                        }
                    }
                }

                /* Pause if requested */
                if (sleepIntervalMs != 0) {
                    exitSleepLoop = false;
                    if (sleepIntervalMs < 100) {
                        try {
                            Thread.sleep(sleepIntervalMs);
                        } catch (InterruptedException e) {
                            // noop
                        }
                    }
                    else {
                        for (int i = 0; i < sleepIntervalMs /100; i++) {
                            try {
                                Thread.sleep(100);
                                if (exitSleepLoop) {
                                    break;
                                }
                            } catch (InterruptedException e) {
                                // noop
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.err.println(frCpuState);
            try {
                statement.formatOperandsAndComment(frCpuState, false, outputOptions);
                System.err.println("Offending instruction : " + statement);
            }
            catch(Exception e1) {
                System.err.println("Cannot disassemble offending instruction :" + statement.formatAsHex());
            }
            System.err.println("(on or before PC=0x" + Format.asHex(frCpuState.pc, 8) + ")");
            throw new EmulationException(e);
        }
    }

    private void pushInstruction(FrStatement statement) {
        statement.formatOperandsAndComment(cpuState, false, outputOptions);
        callStack.push(new CallStackItem(cpuState.pc, cpuState.getReg(FrCPUState.SP), statement.toString()));
    }

    private int bitSearch(int value, int testBit) {
        if (testBit == 0) value = ~value;
        int mask = 0x80000000;
        for (int i = 0; i < 31; i++) {
            if ((value & mask) != 0) return i;
            mask >>= 1;
        }
        return 32;
    }

    private void processInterrupt(int interruptNumber, int pcToStore) {
        FrCPUState frCpuState = (FrCPUState) cpuState;
        frCpuState.setReg(FrCPUState.SSP, frCpuState.getReg(FrCPUState.SSP) - 4);
        memory.store32(frCpuState.getReg(FrCPUState.SSP), frCpuState.getPS());
        frCpuState.setReg(FrCPUState.SSP, frCpuState.getReg(FrCPUState.SSP) - 4);
        memory.store32(frCpuState.getReg(FrCPUState.SSP), pcToStore);
        frCpuState.setS(0);
        frCpuState.pc = memory.load32(frCpuState.getReg(FrCPUState.TBR) + 0x3FC - interruptNumber * 4);
    }

    private void setDelayedChanges(Integer nextPC, Integer nextRP) {
        this.nextPC = nextPC;
        this.nextReturnAddress = nextRP;
        this.delaySlotDone = false;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void setCpuState(CPUState cpuState) {
        this.cpuState = cpuState;
    }
}
