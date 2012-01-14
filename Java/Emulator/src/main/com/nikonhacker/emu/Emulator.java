package com.nikonhacker.emu;

import com.nikonhacker.Format;
import com.nikonhacker.dfr.*;
import com.nikonhacker.emu.memory.AutoAllocatingMemory;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakCondition;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Emulator {

    private long totalCycles;
    private int interruptPeriod;
    private boolean exitRequired = false;
    private Memory memory;
    private CPUState cpuState;

    private Integer nextPC = null;
    private Integer nextRP = null;
    private boolean delaySlotDone = false;
    private PrintStream instructionPrintStream;
    private int sleepIntervalMs = 0;
    private boolean sleepIntervalChanged = false;
    private List<BreakCondition> breakConditions = new ArrayList<BreakCondition>();
    
    private Set<OutputOption> outputOptions = EnumSet.noneOf(OutputOption.class);

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

        CPUState cpuState = new CPUState(initialPc);
        Emulator emulator = new Emulator(1000);
        emulator.setMemory(memory);
        emulator.setCpuState(cpuState);
        emulator.setInstructionPrintStream(System.out);

        emulator.play();
    }


    public Emulator(int interruptPeriod) {
        this.interruptPeriod = interruptPeriod;
    }


    public long getTotalCycles() {
        return totalCycles;
    }

    public void setTotalCycles(long totalCycles) {
        this.totalCycles = totalCycles;
    }

    public int getInterruptPeriod() {
        return interruptPeriod;
    }

    /**
     * Sets how often external interrupts are checked
     * @param interruptPeriod the period, in CPU cycles
     */
    public void setInterruptPeriod(int interruptPeriod) {
        this.interruptPeriod = interruptPeriod;
    }

    /**
     * Call this to request a stop/pause of the emulator
     * @param exitRequired
     */
    public void setExitRequired(boolean exitRequired) {
        this.exitRequired = exitRequired;
    }


    /**
     * Provide a printstream to send disassembled form of executed instructions to
     * @param instructionPrintStream
     */
    public void setInstructionPrintStream(PrintStream instructionPrintStream) {
        this.instructionPrintStream = instructionPrintStream;
    }

    /**
     * Changes the sleep interval between instructions
     * @param sleepIntervalMs
     */
    public void setSleepIntervalMs(int sleepIntervalMs) {
        this.sleepIntervalMs = sleepIntervalMs;
    }

    public void setSleepIntervalChanged() {
        this.sleepIntervalChanged = true;
    }

    public void setOutputOptions(Set<OutputOption> outputOptions) {
        this.outputOptions = outputOptions;
    }

    public Memory getMemory() {
        return memory;
    }


    public void setMemory(Memory memory) {
        this.memory = memory;
    }


    public CPUState getCpuState() {
        return cpuState;
    }

    public void setCpuState(CPUState cpuState) {
        this.cpuState = cpuState;
    }


    public void play() throws EmulationException {
        /* temporary variables */
        int cycleRemaining = interruptPeriod;
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

        DisassemblyState disassemblyState = new DisassemblyState();

        cpuState.setAllRegistersValid();

        try {
            for (;;) {
                
                disassemblyState.reset();

                disassemblyState.getNextInstruction(memory, cpuState.pc);
    
                disassemblyState.opcode = OpCode.opCodeMap[disassemblyState.data[0]];
    
                disassemblyState.decodeInstructionOperands(cpuState, memory);

                if (instructionPrintStream != null) {
                    PrintStream ips = instructionPrintStream;
                    // copying to make sure we keep a reference even if instructionPrintStream gets set to null in between but still avoid costly synchronization
                    if (ips != null) {
                        // OK. copy is still not null
                        disassemblyState.formatOperandsAndComment(cpuState, false, outputOptions);
                        ips.print("0x" + Format.asHex(cpuState.pc, 8) + " " + disassemblyState);
                    }
                }
                
                switch (disassemblyState.opcode.encoding) {
                    case 0xA600: /* ADD Rj, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) + (cpuState.getReg(disassemblyState.j) & 0xFFFFFFFFL);
                        result32 = (int) result64;
                        S1 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassemblyState.j) & 0x80000000) >>> 31;
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA400: /* ADD #i4, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) + disassemblyState.x;
                        result32 = (int) result64;
                        S1 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        S2 = 0; /* unsigned extension of x means positive */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA500: /* ADD2 #i4, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) + (Dfr.extn(4, disassemblyState.x) & 0xFFFFFFFFL);
                        result32 = (int) result64;
                        S1 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        S2 = 1; /* negative extension of x means negative */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA700: /* ADDC Rj, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) + (cpuState.getReg(disassemblyState.j) & 0xFFFFFFFFL) + cpuState.C;
                        result32 = (int) result64;
                        S1 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassemblyState.j) & 0x80000000) >>> 31; // TODO : Shouldn't it take C into account ?
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA200: /* ADDN Rj, Ri */
                        cpuState.setReg(disassemblyState.i, cpuState.getReg(disassemblyState.i) + cpuState.getReg(disassemblyState.j));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA000: /* ADDN #i4, Ri */
                        cpuState.setReg(disassemblyState.i, cpuState.getReg(disassemblyState.i) + disassemblyState.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA100: /* ADDN2 #i4, Ri */
                        cpuState.setReg(disassemblyState.i, cpuState.getReg(disassemblyState.i) + Dfr.extn(4, disassemblyState.x));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAC00: /* SUB Rj, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) - (cpuState.getReg(disassemblyState.j) & 0xFFFFFFFFL);
                        S1 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassemblyState.j) & 0x80000000) >>> 31;
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
    
                        cpuState.setReg(disassemblyState.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAD00: /* SUBC Rj, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) - (cpuState.getReg(disassemblyState.j) & 0xFFFFFFFFL) - cpuState.C;
                        S1 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassemblyState.j) & 0x80000000) >>> 31; // TODO : Shouldn't it take C into account ?
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
    
                        cpuState.setReg(disassemblyState.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAE00: /* SUBN Rj, Ri */
                        cpuState.setReg(disassemblyState.i, cpuState.getReg(disassemblyState.i) - cpuState.getReg(disassemblyState.j));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAA00: /* CMP Rj, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) - (cpuState.getReg(disassemblyState.j) & 0xFFFFFFFFL);
                        S1 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassemblyState.j) & 0x80000000) >>> 31;
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA800: /* CMP #i4, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) - disassemblyState.x;
                        /* optimize : 0 extension of x means S2 is 0, right ?  */
                        S1 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        S2 = 0; /* unsigned extension of x means positive */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA900: /* CMP2 #i4, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) - (Dfr.extn(4, disassemblyState.x) & 0xFFFFFFFFL);
                        S1 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        S2 = 1; /* negative extension of x means negative */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x8200: /* AND Rj, Ri */
                        result32 = cpuState.getReg(disassemblyState.i) & cpuState.getReg(disassemblyState.j);
                        cpuState.setReg(disassemblyState.i, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x8400: /* AND Rj, @Ri */
                        result32 = memory.load32(cpuState.getReg(disassemblyState.i)) & cpuState.getReg(disassemblyState.j);
                        memory.store32(cpuState.getReg(disassemblyState.i), result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8500: /* ANDH Rj, @Ri */
                        result32 = memory.loadUnsigned16(cpuState.getReg(disassemblyState.i)) & cpuState.getReg(disassemblyState.j);
                        memory.store16(cpuState.getReg(disassemblyState.i), result32);
    
                        cpuState.N = (result32 & 0x8000) >>> 15;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8600: /* ANDB Rj, @Ri */
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) & cpuState.getReg(disassemblyState.j);
                        memory.store8(cpuState.getReg(disassemblyState.i), result32);
    
                        cpuState.N = (result32 & 0x80) >>> 7;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9200: /* OR Rj, Ri */
                        result32 = cpuState.getReg(disassemblyState.i) | cpuState.getReg(disassemblyState.j);
                        cpuState.setReg(disassemblyState.i, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9400: /* OR Rj, @Ri */
                        result32 = memory.load32(cpuState.getReg(disassemblyState.i)) | cpuState.getReg(disassemblyState.j);
                        memory.store32(cpuState.getReg(disassemblyState.i), result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9500: /* ORH Rj, @Ri */
                        result32 = memory.loadUnsigned16(cpuState.getReg(disassemblyState.i)) | cpuState.getReg(disassemblyState.j);
                        memory.store16(cpuState.getReg(disassemblyState.i), result32);
    
                        cpuState.N = (result32 & 0x8000) >>> 15;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9600: /* ORB Rj, @Ri */
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) | cpuState.getReg(disassemblyState.j);
                        memory.store8(cpuState.getReg(disassemblyState.i), result32);
    
                        cpuState.N = (result32 & 0x80) >>> 7;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9A00: /* EOR Rj, Ri */
                        result32 = cpuState.getReg(disassemblyState.i) ^ cpuState.getReg(disassemblyState.j);
                        cpuState.setReg(disassemblyState.i, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9C00: /* EOR Rj, @Ri */
                        result32 = memory.load32(cpuState.getReg(disassemblyState.i)) ^ cpuState.getReg(disassemblyState.j);
                        memory.store32(cpuState.getReg(disassemblyState.i), result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9D00: /* EORH Rj, @Ri */
                        result32 = memory.loadUnsigned16(cpuState.getReg(disassemblyState.i)) ^ cpuState.getReg(disassemblyState.j);
                        memory.store16(cpuState.getReg(disassemblyState.i), result32);
    
                        cpuState.N = (result32 & 0x8000) >>> 15;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9E00: /* EORB Rj, @Ri */
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) ^ cpuState.getReg(disassemblyState.j);
                        memory.store8(cpuState.getReg(disassemblyState.i), result32);
    
                        cpuState.N = (result32 & 0x80) >>> 7;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8000: /* BANDL #u4, @Ri (u4: 0 to 0FH) */
                        memory.store8(cpuState.getReg(disassemblyState.i), memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) & (0xF0 + disassemblyState.x));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8100: /* BANDH #u4, @Ri (u4: 0 to 0FH) */
                        memory.store8(cpuState.getReg(disassemblyState.i), memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) & ((disassemblyState.x << 4) + 0x0F));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9000: /* BORL #u4, @Ri (u4: 0 to 0FH) */
                        memory.store8(cpuState.getReg(disassemblyState.i), memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) | disassemblyState.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9100: /* BORH #u4, @Ri (u4: 0 to 0FH) */
                        memory.store8(cpuState.getReg(disassemblyState.i), memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) | (disassemblyState.x << 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9800: /* BEORL #u4, @Ri (u4: 0 to 0FH) */
                        memory.store8(cpuState.getReg(disassemblyState.i), memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) ^ disassemblyState.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9900: /* BEORH #u4, @Ri (u4: 0 to 0FH) */
                        memory.store8(cpuState.getReg(disassemblyState.i), memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) ^ (disassemblyState.x << 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8800: /* BTSTL #u4, @Ri (u4: 0 to 0FH) */
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) & disassemblyState.x;
    
                        cpuState.N = 0;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 2 + a;
                        break;
    
                    case 0x8900: /* BTSTH #u4, @Ri (u4: 0 to 0FH) */
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassemblyState.i)) & (disassemblyState.x << 4);
    
                        cpuState.N = (result32 & 0x80) >>> 7;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 2 + a;
                        break;
    
                    case 0xAF00: /* MUL Rj,Ri */
                        result64 = ((long) cpuState.getReg(disassemblyState.j)) * ((long) cpuState.getReg(disassemblyState.i));
                        cpuState.setReg(CPUState.MDH, (int) (result64 >> 32));
                        cpuState.setReg(CPUState.MDL, (int) (result64 & 0xFFFFFFFFL));
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31); /*see pdf*/
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (int) ((result64 & 0x100000000L) >>> 32);

                        cpuState.pc += 2;

                        cycles = 5;
                        break;
    
                    case 0xAB00: /* MULU Rj,Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) * (cpuState.getReg(disassemblyState.j) & 0xFFFFFFFFL);
                        cpuState.setReg(CPUState.MDH, (int) (result64 >> 32));
                        cpuState.setReg(CPUState.MDL, (int) (result64 & 0xFFFFFFFFL));
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31); /*see pdf*/
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (int) ((result64 & 0x100000000L) >>> 32);

                        cpuState.pc += 2;

                        cycles = 5;
                        break;
    
                    case 0xBF00: /* MULH Rj,Ri */
                        result32 = ((short) cpuState.getReg(disassemblyState.j)) * ((short) cpuState.getReg(disassemblyState.i));
                        cpuState.setReg(CPUState.MDL, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 3;
                        break;
    
                    case 0xBB00: /* MULUH Rj,Ri */
                        result32 = (cpuState.getReg(disassemblyState.j) & 0xFFFF) * (cpuState.getReg(disassemblyState.i) & 0xFFFF);
                        cpuState.setReg(CPUState.MDL, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 3;
                        break;
    
                    case 0x9740: /* DIV0S Ri */
                        S1 = (cpuState.getReg(CPUState.MDL) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassemblyState.i) & 0x80000000) >>> 31;
                        cpuState.D0= S1;
                        cpuState.D1= S1 ^ S2;
                        result64 = (long) cpuState.getReg(CPUState.MDL);
                        cpuState.setReg(CPUState.MDH, (int) (result64 >>> 32));
                        cpuState.setReg(CPUState.MDL, (int) (result64 & 0xFFFFFFFFL));

                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9750: /* DIV0U Ri */
                        cpuState.D0=0;
                        cpuState.D1=0;
                        cpuState.setReg(CPUState.MDH, 0);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9760: /* DIV1 Ri */
                        cpuState.setReg(CPUState.MDH, (cpuState.getReg(CPUState.MDH) << 1) | ((cpuState.getReg(CPUState.MDL) & 0x80000000) >>> 31));
                        cpuState.setReg(CPUState.MDL, cpuState.getReg(CPUState.MDL) << 1);
                        if (cpuState.D1 == 1) {
                            // Dividend and divisor have opposite signs
                            result64 = (cpuState.getReg(CPUState.MDH) & 0xFFFFFFFFL) + (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL);
                            result32 = (int) result64;
                            cpuState.C = (int) ((result64 & 0x100000000L) >>> 32);
                            cpuState.Z = (result32 == 0)?1:0;
                        }
                        else {
                            // Dividend and divisor have same signs
                            result64 = (cpuState.getReg(CPUState.MDH) & 0xFFFFFFFFL) - (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL);
                            result32 = (int) result64;
                            cpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
                            cpuState.Z = (result32 == 0)?1:0;
                        }
                        if ((cpuState.D0 ^ cpuState.D1 ^ cpuState.C) == 0) {
                            cpuState.setReg(CPUState.MDH, result32);
                            cpuState.setReg(CPUState.MDL, cpuState.getReg(CPUState.MDL) | 1);
                        }

                        cpuState.pc += 2;

                        cycles = d;
                        break;
    
                    case 0x9770: /* DIV2 Ri */
                        if (cpuState.D1 == 1) {
                            result64 = cpuState.getReg(CPUState.MDH) + cpuState.getReg(disassemblyState.i);
                            result32 = (int) result64;
                            cpuState.C = (result32 == result64) ? 0 : 1;
                            cpuState.Z = (result32 == 0)?1:0;
                        }
                        else {
                            result64 = cpuState.getReg(CPUState.MDH) - cpuState.getReg(disassemblyState.i);
                            result32 = (int) result64;
                            cpuState.C = (result32 == result64) ? 0 : 1;
                            cpuState.Z = (result32 == 0)?1:0;
                        }
                        if (cpuState.Z == 1) {
                            cpuState.setReg(CPUState.MDH, 0);
                        }

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F60: /* DIV3 */
                        if (cpuState.Z == 1) {
                            cpuState.setReg(CPUState.MDL, cpuState.getReg(CPUState.MDL) + 1);
                        }
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F70: /* DIV4S */
                        if (cpuState.D1 == 1) {
                            cpuState.setReg(CPUState.MDL, -cpuState.getReg(CPUState.MDL));
                        }
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB600: /* LSL Rj, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) << (cpuState.getReg(disassemblyState.j) & 0x1F);
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31);
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32);
    
                        cpuState.setReg(disassemblyState.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB400: /* LSL #u4, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) << disassemblyState.x;
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31);
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.C = (disassemblyState.x == 0) ? 0 : (int) ((result64 & 0x100000000L) >>> 32);
    
                        cpuState.setReg(disassemblyState.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB500: /* LSL2 #u4, Ri */
                        result64 = (cpuState.getReg(disassemblyState.i) & 0xFFFFFFFFL) << (disassemblyState.x + 16);
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31);
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32);
    
                        cpuState.setReg(disassemblyState.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB200: /* LSR Rj, Ri */
                        result32 = cpuState.getReg(disassemblyState.i) >>> (cpuState.getReg(disassemblyState.j) & 0x1F);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = ((cpuState.getReg(disassemblyState.j) & 0x1F) == 0) ? 0 : (cpuState.getReg(disassemblyState.i) >> ((cpuState.getReg(disassemblyState.j) & 0x1F) - 1)) & 1;

                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB000: /* LSR #u4, Ri */
                        result32 = cpuState.getReg(disassemblyState.i) >>> disassemblyState.x;
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = (disassemblyState.x == 0) ? 0 : (cpuState.getReg(disassemblyState.i) >> (disassemblyState.x - 1)) & 1;
    
                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB100: /* LSR2 #u4, Ri */
                        result32 = cpuState.getReg(disassemblyState.i) >>> (disassemblyState.x + 16);
    
                        cpuState.N = 0;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = (cpuState.getReg(disassemblyState.i) >> (disassemblyState.x + 15)) & 1;
    
                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xBA00: /* ASR Rj, Ri */
                        result32 = cpuState.getReg(disassemblyState.i) >> (cpuState.getReg(disassemblyState.j) & 0x1F);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = ((cpuState.getReg(disassemblyState.j) & 0x1F) == 0) ? 0 : (cpuState.getReg(disassemblyState.i) >> ((cpuState.getReg(disassemblyState.j) & 0x1F) - 1)) & 1;
    
                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB800: /* ASR #u4, Ri */
                        result32 = cpuState.getReg(disassemblyState.i) >> disassemblyState.x;
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = (disassemblyState.x == 0) ? 0 : (cpuState.getReg(disassemblyState.i) >> (disassemblyState.x - 1)) & 1;
    
                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB900: /* ASR2 #u4, Ri */
                        result32 = cpuState.getReg(disassemblyState.i) >> (disassemblyState.x + 16);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = (cpuState.getReg(disassemblyState.i) >> (disassemblyState.x + 15)) & 1;
    
                        cpuState.setReg(disassemblyState.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F80: /* LDI:32 #i32, Ri */
                        cpuState.setReg(disassemblyState.i, disassemblyState.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 6;

                        cycles = 3;
                        break;
    
                    case 0x9B00: /* LDI:20 #i20, Ri */
                        cpuState.setReg(disassemblyState.i, disassemblyState.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 4;

                        cycles = 2;
                        break;
    
                    case 0xC000: /* LDI:8 #i8, Ri */
                        cpuState.setReg(disassemblyState.i, disassemblyState.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x0400: /* LD @Rj, Ri */
                        cpuState.setReg(disassemblyState.i, memory.load32(cpuState.getReg(disassemblyState.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0000: /* LD @(R13,Rj), Ri */
                        cpuState.setReg(disassemblyState.i, memory.load32(cpuState.getReg(13) + cpuState.getReg(disassemblyState.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x2000: /* LD @(R14,disp10), Ri */
                        cpuState.setReg(disassemblyState.i, memory.load32(cpuState.getReg(14) + Dfr.signExtend(8, disassemblyState.x) * 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0300: /* LD @(R15,udisp6), Ri */
                        cpuState.setReg(disassemblyState.i, memory.load32(cpuState.getReg(15) + disassemblyState.x * 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0700: /* LD @R15+, Ri */
                        cpuState.setReg(disassemblyState.i, memory.load32(cpuState.getReg(15)));
                        cpuState.setReg(15, cpuState.getReg(15) + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0780: /* LD @R15+, Rs */
                    case 0x0781:
                    case 0x0782:
                    case 0x0783:
                    case 0x0784:
                    case 0x0785:
                        cpuState.setReg(CPUState.DEDICATED_REG_OFFSET + disassemblyState.i, memory.load32(cpuState.getReg(15)));
                        cpuState.setReg(15, cpuState.getReg(15) + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0790: /* LD @R15+, PS */
                        cpuState.setPS(memory.load32(cpuState.getReg(15)));
                        cpuState.setReg(15, cpuState.getReg(15) + 4);
    
                        /* NZVC is part of the PS !*/

                        cpuState.pc += 2;

                        cycles = 1 + a + b;
                        break;
    
                    case 0x0500: /* LDUH @Rj, Ri */
                        cpuState.setReg(disassemblyState.i, memory.loadUnsigned16(cpuState.getReg(disassemblyState.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0100: /* LDUH @(R13,Rj), Ri */
                        cpuState.setReg(disassemblyState.i, memory.loadUnsigned16(cpuState.getReg(13) + cpuState.getReg(disassemblyState.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x4000: /* LDUH @(R14,disp9), Ri */
                        cpuState.setReg(disassemblyState.i, memory.loadUnsigned16(cpuState.getReg(14) + Dfr.signExtend(8, disassemblyState.x) * 2));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0600: /* LDUB @Rj, Ri */
                        cpuState.setReg(disassemblyState.i, memory.loadUnsigned8(cpuState.getReg(disassemblyState.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0200: /* LDUB @(R13,Rj), Ri */
                        cpuState.setReg(disassemblyState.i, memory.loadUnsigned8(cpuState.getReg(13) + cpuState.getReg(disassemblyState.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x6000: /* LDUB @(R14,disp8), Ri */
                        cpuState.setReg(disassemblyState.i, memory.loadUnsigned8(cpuState.getReg(14) + disassemblyState.x));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1400: /* ST Ri, @Rj */
                        memory.store32(cpuState.getReg(disassemblyState.j), cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1000: /* ST Ri, @(R13,Rj) */
                        memory.store32(cpuState.getReg(13) + cpuState.getReg(disassemblyState.j), cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x3000: /* ST Ri, @(R14,disp10) */
                        memory.store32(cpuState.getReg(14) + Dfr.signExtend(8, disassemblyState.x) * 4, cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1300: /* ST Ri, @(R15,udisp6) */
                        memory.store32(cpuState.getReg(15) + disassemblyState.x * 4, cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1700: /* ST Ri, @-R15 */
                        cpuState.setReg(15, cpuState.getReg(15) - 4);
                        if (disassemblyState.i == 15) {
                            /*special case for R15: value stored is R15 before it was decremented */
                            memory.store32(cpuState.getReg(15), cpuState.getReg(15) + 4);
                        }
                        else {
                            memory.store32(cpuState.getReg(15), cpuState.getReg(disassemblyState.i));
                        }
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1780: /* ST Rs, @-R15 */
                    case 0x1781:
                    case 0x1782:
                    case 0x1783:
                    case 0x1784:
                    case 0x1785:
                        cpuState.setReg(15, cpuState.getReg(15) - 4);
                        memory.store32(cpuState.getReg(15), cpuState.getReg(CPUState.DEDICATED_REG_OFFSET + disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1790: /* ST PS, @-R15 */
                        cpuState.setReg(15, cpuState.getReg(15) - 4);
                        memory.store32(cpuState.getReg(15), cpuState.getPS());
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1500: /* STH Ri, @Rj */
                        memory.store16(cpuState.getReg(disassemblyState.j), cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1100: /* STH Ri, @(R13,Rj) */
                        memory.store16(cpuState.getReg(13) + cpuState.getReg(disassemblyState.j), cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x5000: /* STH Ri, @(R14,disp9) */
                        memory.store16(cpuState.getReg(14) + Dfr.signExtend(8, disassemblyState.x) * 2, cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1600: /* STB Ri, @Rj */
                        memory.store8(cpuState.getReg(disassemblyState.j), cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1200: /* STB Ri, @(R13,Rj) */
                        memory.store8(cpuState.getReg(13) + cpuState.getReg(disassemblyState.j), cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x7000: /* STB Ri, @(R14,disp8) */
                        memory.store8(cpuState.getReg(14) + Dfr.signExtend(8, disassemblyState.x), cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x8B00: /* MOV Rj, Ri */
                        cpuState.setReg(disassemblyState.i, cpuState.getReg(disassemblyState.j));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB700: /* MOV Rs, Ri */
                    case 0xB710:
                    case 0xB720:
                    case 0xB730:
                    case 0xB740:
                    case 0xB750:
                        cpuState.setReg(disassemblyState.i, cpuState.getReg(CPUState.DEDICATED_REG_OFFSET + disassemblyState.j));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB300: /* MOV Ri, Rs */
                    case 0xB310:
                    case 0xB320:
                    case 0xB330:
                    case 0xB340:
                    case 0xB350:
                        cpuState.setReg(CPUState.DEDICATED_REG_OFFSET + disassemblyState.j, cpuState.getReg(disassemblyState.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x1710: /* MOV PS, Ri */
                        cpuState.setReg(disassemblyState.i, cpuState.getPS());
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x0710: /* MOV Ri, PS */
                        cpuState.setPS(cpuState.getReg(disassemblyState.i));
    
                        /* NZVC is part of the PS !*/

                        cpuState.pc += 2;

                        cycles = c;
                        break;
    
                    case 0x9700: /* JMP @Ri */
                        cpuState.pc = cpuState.getReg(disassemblyState.i);
    
                        /* No change to NZVC */

                        cycles = 2;
                        break;
    
                    case 0xD000: /* CALL label12 */
                        cpuState.setReg(CPUState.RP, cpuState.pc + 2);
                        cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(11, disassemblyState.x) * 2; // TODO check *2 ?


                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0x9710: /* CALL @Ri */
                        cpuState.setReg(CPUState.RP, cpuState.pc + 2);
                        cpuState.pc = cpuState.getReg(disassemblyState.i);
    
                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0x9720: /* RET */
                        cpuState.pc = cpuState.getReg(CPUState.RP);
    
                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0x1F00: /* INT #u8 */
                        cpuState.setReg(CPUState.SSP, cpuState.getReg(CPUState.SSP) - 4);
                        memory.store32(cpuState.getReg(CPUState.SSP), cpuState.getPS());
                        cpuState.setReg(CPUState.SSP, cpuState.getReg(CPUState.SSP) - 4);
                        memory.store32(cpuState.getReg(CPUState.SSP), cpuState.pc + 2);
                        cpuState.I = 0;
                        cpuState.setS(0);
                        cpuState.pc = memory.load32(cpuState.getReg(CPUState.TBR) + 0x3FC - disassemblyState.x * 4);
    
                        /* No change to NZVC */
    
                        cycles = 3 + 3 * a;
                        break;
    
                    case 0x9F30: /* INTE */
                        cpuState.setReg(CPUState.SSP, cpuState.getReg(CPUState.SSP) - 4);
                        memory.store32(cpuState.getReg(CPUState.SSP), cpuState.getPS());
                        cpuState.setReg(CPUState.SSP, cpuState.getReg(CPUState.SSP) - 4);
                        memory.store32(cpuState.getReg(CPUState.SSP), cpuState.pc + 2);
                        cpuState.setS(0);
                        cpuState.setILM(4);
                        cpuState.pc = memory.load32(cpuState.getReg(CPUState.TBR) + 0x3D8);
    
                        /* No change to NZVC */
    
                        cycles = 3 + 3 * a;
                        break;
    
                    case 0x9730: /* RETI */
                        cpuState.pc = memory.load32(cpuState.getReg(15));
                        cpuState.setReg(15, cpuState.getReg(15) + 8);
                        /* note : this is the order given in the spec but loading PS below could switch the USP<>SSP,
                        so the last SP increment would address the wrong stack
                        Doing it this way passes the test */
                        cpuState.setPS(memory.load32(cpuState.getReg(15) - 4));
    
                        /* NZVC is part of the PS !*/
    
                        cycles = 2 + 2 * a;
                        break;
    
                    case 0xE100: /* BNO label9 */
                        /* No branch */
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xE000: /* BRA label9 */
                        cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
    
                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0xE200: /* BEQ label9 */
                        if (cpuState.Z == 1) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }

                        /* No change to NZVC */
    
                        break;
    
                    case 0xE300: /* BNE label9 */
                        if (cpuState.Z == 0) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE400: /* BC label9 */
                        if (cpuState.C == 1) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE500: /* BNC label9 */
                        if (cpuState.C == 0) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE600: /* BN label9 */
                        if (cpuState.N == 1) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE700: /* BP label9 */
                        if (cpuState.N == 0) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE800: /* BV label9 */
                        if (cpuState.V == 1) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xE900: /* BNV label9 */
                        if (cpuState.V == 0) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEA00: /* BLT label9 */
                        if ((cpuState.V ^ cpuState.N) == 1) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEB00: /* BGE label9 */
                        if ((cpuState.V ^ cpuState.N) == 0) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEC00: /* BLE label9 */
                        if (((cpuState.V ^ cpuState.N) | cpuState.Z) == 1) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xED00: /* BGT label9 */
                        if (((cpuState.V ^ cpuState.N) | cpuState.Z) == 0) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEE00: /* BLS label9 */
                        if ((cpuState.C | cpuState.Z) == 1) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0xEF00: /* BHI label9 */
                        if ((cpuState.C | cpuState.Z) == 0) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0x9F00: /* JMP:D @Ri */
                        setDelayedChanges(cpuState.getReg(disassemblyState.i), null);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xD800: /* CALL:D label12 */
                        setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(11, disassemblyState.x) * 2, cpuState.pc + 4);  // TODO check *2
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F10: /* CALL:D @Ri */
                        setDelayedChanges(cpuState.getReg(disassemblyState.i), cpuState.pc + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F20: /* RET:D */
                        setDelayedChanges(cpuState.getReg(CPUState.RP), null);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xF100: /* BNO:D label9 */
                        /* No branch */
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xF000: /* BRA:D label9 */
                        setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xF200: /* BEQ:D label9 */
                        if (cpuState.Z == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;

                        //* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF300: /* BNE:D label9 */
                        if (cpuState.Z == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF400: /* BC:D label9 */
                        if (cpuState.C == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF500: /* BNC:D label9 */
                        if (cpuState.C == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF600: /* BN:D label9 */
                        if (cpuState.N == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF700: /* BP:D label9 */
                        if (cpuState.N == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF800: /* BV:D label9 */
                        if (cpuState.V == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF900: /* BNV:D label9 */
                        if (cpuState.V == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFA00: /* BLT:D label9 */
                        if ((cpuState.V ^ cpuState.N) == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFB00: /* BGE:D label9 */
                        if ((cpuState.V ^ cpuState.N) == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFC00: /* BLE:D label9 */
                        if (((cpuState.V ^ cpuState.N) | cpuState.Z) == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFD00: /* BGT:D label9 */
                        if (((cpuState.V ^ cpuState.N) | cpuState.Z) == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFE00: /* BLS:D label9 */
                        if ((cpuState.C | cpuState.Z) == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;

                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFF00: /* BHI:D label9 */
                        if ((cpuState.C | cpuState.Z) == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassemblyState.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0x0800: /* DMOV @dir10, R13 */
                        cpuState.setReg(13, memory.load32(disassemblyState.x * 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1800: /* DMOV R13, @dir10 */
                        memory.store32(disassemblyState.x * 4, cpuState.getReg(13));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x0C00: /* DMOV @dir10, @R13+ */
                        memory.store32(cpuState.getReg(13), memory.load32(disassemblyState.x * 4));
                        cpuState.setReg(13, cpuState.getReg(13) + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1C00: /* DMOV @R13+, @dir10 */
                        memory.store32(disassemblyState.x * 4, memory.load32(cpuState.getReg(13)));
                        cpuState.setReg(13, cpuState.getReg(13) + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x0B00: /* DMOV @dir10, @–R15 */
                        cpuState.setReg(15, cpuState.getReg(15) - 4);
                        memory.store32(cpuState.getReg(15), memory.load32(disassemblyState.x * 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1B00: /* DMOV @R15+, @dir10 */
                        memory.store32(disassemblyState.x * 4, memory.load32(cpuState.getReg(15)));
                        cpuState.setReg(15, cpuState.getReg(15) + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x0900: /* DMOVH @dir9, R13 */
                        cpuState.setReg(13, memory.loadUnsigned16(disassemblyState.x * 2));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1900: /* DMOVH R13, @dir9 */
                        memory.store16(disassemblyState.x * 2, cpuState.getReg(13));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x0D00: /* DMOVH @dir9, @R13+ */
                        memory.store16(cpuState.getReg(13), memory.loadUnsigned16(disassemblyState.x * 2));
                        cpuState.setReg(13, cpuState.getReg(13) + 2);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1D00: /* DMOVH @R13+, @dir9 */
                        memory.store16(disassemblyState.x * 2, memory.loadUnsigned16(cpuState.getReg(13)));
                        cpuState.setReg(13, cpuState.getReg(13) + 2);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x0A00: /* DMOVB @dir8, R13 */
                        cpuState.setReg(13, memory.loadUnsigned8(disassemblyState.x));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1A00: /* DMOVB R13, @dir8 */
                        memory.store8(disassemblyState.x, cpuState.getReg(13));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x0E00: /* DMOVB @dir8, @R13+ */
                        memory.store8(cpuState.getReg(13), memory.loadUnsigned8(disassemblyState.x));
                        cpuState.setReg(13, cpuState.getReg(13) + 1);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1E00: /* DMOVB @R13+, @dir8 */
                        memory.store8(disassemblyState.x, memory.loadUnsigned8(cpuState.getReg(13)));
                        cpuState.setReg(13, cpuState.getReg(13) + 1);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0xBC00: /* LDRES @Ri+, #u4 */
                        /* TODO FUTURE */
                        System.err.println(disassemblyState.opcode.toString() + " is not implemented (resource) at PC=0x" + Format.asHex(cpuState.pc-2,8));
                        /*sentToResource(x, memory.load32(cpuState.getReg(i)));
                        cpuState.getReg(i) + = 4;*/
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0xBD00: /* STRES #u4, @Ri+ */
                        /* TODO FUTURE */
                        System.err.println(disassemblyState.opcode.toString() + " is not implemented (resource) at PC=0x" + Format.asHex(cpuState.pc-2,8));
                        /* memory.store32(cpuState.getReg(i), getFromResource(x); cpuState.getReg(i) + = 4;*/
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x9FC0: /* COPOP #u4, #CC, CRj, CRi */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(disassemblyState.opcode.toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(cpuState.pc-2,8));
    
                        /* No change to NZVC */

                        cpuState.pc += 4;

                        cycles = 2 + a;
                        break;
    
                    case 0x9FD0: /* COPLD #u4, #CC, Rj, CRi */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(disassemblyState.opcode.toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(cpuState.pc-2,8));
                        /* cpuState.getReg(CPUState.COPROCESSOR_REG_OFFSET + i) = cpuState.getReg(j); */
    
                        /* No change to NZVC */

                        cpuState.pc += 4;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9FE0: /* COPST #u4, #CC, CRj, Ri */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(disassemblyState.opcode.toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(cpuState.pc-2,8));
                        /* cpuState.getReg(i) = cpuState.getReg(CPUState.COPROCESSOR_REG_OFFSET + j); */
    
                        /* No change to NZVC */

                        cpuState.pc += 4;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9FF0: /* COPSV #u4, #CC, CRj, Ri */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(disassemblyState.opcode.toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(cpuState.pc-2,8));
                        /* cpuState.getReg(i) = cpuState.getReg(CPUState.COPROCESSOR_REG_OFFSET + j);*/
    
                        /* No change to NZVC */

                        cpuState.pc += 4;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9FA0: /* NOP */
                        /* No change */
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x8300: /* ANDCCR #u8 */
                        cpuState.setCCR(cpuState.getCCR() & disassemblyState.x);
    
                        /* NZVC is part of the CCR !*/

                        cpuState.pc += 2;

                        cycles = c;
                        break;
                    
                    case 0x9300: /* ORCCR #u8 */
                        cpuState.setCCR(cpuState.getCCR() | disassemblyState.x);
    
                        /* NZVC is part of the CCR !*/

                        cpuState.pc += 2;

                        cycles = c;
                        break;
                    
                    case 0x8700: /* STILM #u8 */
                        cpuState.setILM(disassemblyState.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0xA300: /* ADDSP #s10 */
                        cpuState.setReg(15, cpuState.getReg(15) + (Dfr.signExtend(8, disassemblyState.x) * 4));
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x9780: /* EXTSB Ri */
                        cpuState.setReg(disassemblyState.i, Dfr.signExtend(8, cpuState.getReg(disassemblyState.i)));
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x9790: /* EXTUB Ri */
                        cpuState.setReg(disassemblyState.i, cpuState.getReg(disassemblyState.i) & 0xFF);
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x97A0: /* EXTSH Ri */
                        cpuState.setReg(disassemblyState.i, Dfr.signExtend(16, cpuState.getReg(disassemblyState.i)));
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x97B0: /* EXTUH Ri */
                        cpuState.setReg(disassemblyState.i, cpuState.getReg(disassemblyState.i) & 0xFFFF);
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x8C00: /* LDM0 (reglist) */
                        n = 0;
                        for (int r = 0; r <= 7; r++) {
                            if ((disassemblyState.x & (1 << r)) != 0) {
                                cpuState.setReg(r, memory.load32(cpuState.getReg(15)));
                                cpuState.setReg(15, cpuState.getReg(15) + 4);
                                n++;
                            }
                        }
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = (n == 0) ? 1 : (a * (n - 1) + b + 1);
                        break;
                    
                    case 0x8D00: /* LDM1 (reglist) */
                        n = 0;
                        for (int r = 0; r <= 7; r++) {
                            if ((disassemblyState.x & (1 << r)) != 0) {
                                cpuState.setReg(r + 8, memory.load32(cpuState.getReg(15)));
                                cpuState.setReg(15, cpuState.getReg(15) + 4);
                                n++;
                            }
                        }
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = (n == 0) ? 1 : a * (n - 1) + b + 1;
                        break;
                    
                    case 0x8E00: /* STM0 (reglist) */
                        n = 0;
                        for (int r = 0; r <= 7; r++) {
                            if ((disassemblyState.x & (1 << r)) != 0) {
                                cpuState.setReg(15, cpuState.getReg(15) - 4);
                                memory.store32(cpuState.getReg(15), cpuState.getReg(7-r));
                                n++;
                            }
                        }

                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a * n + 1;
                        break;
                    
                    case 0x8F00: /* STM1 (reglist) */
                        n = 0;
                        if ((disassemblyState.x & 0x1) != 0) {
                            cpuState.setReg(15, cpuState.getReg(15) - 4);
                            /*special case for R15: value stored is R15 before it was decremented */
                            memory.store32(cpuState.getReg(15), cpuState.getReg(15) + 4);
                            n++;
                        }
                        for (int r = 1; r <= 7; r++) {
                            if ((disassemblyState.x & (1 << r)) != 0) {
                                cpuState.setReg(15, cpuState.getReg(15) - 4);
                                memory.store32(cpuState.getReg(15), cpuState.getReg((7-r) + 8));
                                n++;
                            }
                        }
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a * n + 1;
                        break;
                    
                    case 0x0F00: /* ENTER #u10 */
                        memory.store32(cpuState.getReg(15) - 4, cpuState.getReg(14));
                        cpuState.setReg(14, cpuState.getReg(15) - 4);
                        cpuState.setReg(15, cpuState.getReg(15) - disassemblyState.x * 4);
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + a;
                        break;
                    
                    case 0x9F90: /* LEAVE */
                        cpuState.setReg(15, cpuState.getReg(14) + 4);
                        cpuState.setReg(14, memory.load32(cpuState.getReg(15) - 4));
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
                    
                    case 0x8A00: /* XCHB @Rj, Ri */
                        result32 = cpuState.getReg(disassemblyState.i);
                        cpuState.setReg(disassemblyState.i, memory.loadUnsigned8(cpuState.getReg(disassemblyState.j)));
                        memory.store8(cpuState.getReg(disassemblyState.j), result32);
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
                    
                    default:
                        throw new EmulationException("Unknown opcode encoding : 0x" + Integer.toHexString(disassemblyState.opcode.encoding));
                }

                /* Delay slot processing */
                if (nextPC != null) {
                    if (delaySlotDone) {
                        cpuState.pc = nextPC;
                        nextPC = null;
                        if (nextRP != null) {
                            cpuState.setReg(CPUState.RP, nextRP);
                            nextRP = null;
                        }
                    }
                    else {
                        delaySlotDone = true;
                    }
                }
    
                /* Pause if requested */
                if (sleepIntervalMs != 0) {
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
                                if (sleepIntervalChanged) {
                                    break;
                                }
                            } catch (InterruptedException e) {
                                // noop
                            }
                        }
                    }
                    sleepIntervalChanged = false;
                }

                cycleRemaining -= cycles;

                if (cycleRemaining <= 0) {
                    // TODO : Check for interrupts

                    /* Break if requested */
                    for (BreakCondition breakCondition : breakConditions) {
                        if(breakCondition.matches(cpuState, memory)) {
                            exitRequired = true;
                            break;
                        }
                    }


                    totalCycles += interruptPeriod;
                    cycleRemaining += interruptPeriod;
                    if (exitRequired) break;
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.err.println(cpuState);
            disassemblyState.formatOperandsAndComment(cpuState, false, outputOptions);
            System.err.println("Offending instruction : " + disassemblyState);
            System.err.println("(just before PC=0x" + Format.asHex(cpuState.pc, 8) + ")");
        }
    }

    private void setDelayedChanges(Integer nextPC, Integer nextRP) {
        this.nextPC = nextPC;
        this.nextRP = nextRP;
        this.delaySlotDone = false;
    }

    public void setBreakConditions(List<BreakCondition> breakConditions) {
        this.breakConditions = breakConditions;
    }
}
