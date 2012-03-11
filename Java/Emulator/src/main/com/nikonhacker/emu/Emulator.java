package com.nikonhacker.emu;

import com.nikonhacker.Format;
import com.nikonhacker.dfr.*;
import com.nikonhacker.emu.memory.AutoAllocatingMemory;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Emulator {

    private long totalCycles;
    private int interruptPeriod = 1; // By default, check interrupts at each instruction
    private Memory memory;
    private CPUState cpuState;

    private Integer nextPC = null;
    private Integer nextRP = null;
    private boolean delaySlotDone = false;
    private PrintWriter instructionPrintWriter;
    private Deque<CallStackItem> callStack;
    private int sleepIntervalMs = 0;
    private boolean exitSleepLoop = false;
    private final List<BreakCondition> breakConditions = new ArrayList<BreakCondition>();
    private final List<InterruptRequest> interruptRequests = new ArrayList<InterruptRequest>();

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
        emulator.setInstructionPrintWriter(new PrintWriter(System.out));

        emulator.play();
    }

    public Emulator() {
    }

    public Emulator(int interruptPeriod) {
        this.interruptPeriod = interruptPeriod;
    }


    public long getTotalCycles() {
        return totalCycles;
    }

    /**
     * Sets how often external interrupts are checked
     * @param interruptPeriod the period, in CPU cycles
     */
    public void setInterruptPeriod(int interruptPeriod) {
        this.interruptPeriod = interruptPeriod;
    }

    /**
     * Provide a PrintWriter to send disassembled form of executed instructions to
     * @param instructionPrintWriter
     */
    public void setInstructionPrintWriter(PrintWriter instructionPrintWriter) {
        this.instructionPrintWriter = instructionPrintWriter;
    }


    /**
     * Provide a call stack to write stack entries to it
     * @param callStack
     */
    public void setCallStack(Deque<CallStackItem> callStack) {
        this.callStack = callStack;
    }

    /**
     * Changes the sleep interval between instructions
     * @param sleepIntervalMs
     */
    public void setSleepIntervalMs(int sleepIntervalMs) {
        this.sleepIntervalMs = sleepIntervalMs;
    }

    public void exitSleepLoop() {
        this.exitSleepLoop = true;
    }

    public void setOutputOptions(Set<OutputOption> outputOptions) {
        OpCode.initOpcodeMap(outputOptions);
        DisassembledInstruction.initFormatChars(outputOptions);
        CPUState.initRegisterLabels(outputOptions);
        this.outputOptions = outputOptions;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }


    public void setCpuState(CPUState cpuState) {
        this.cpuState = cpuState;
    }

    /**
     * Starts emulating
     * @return the BreakCondition that made the emulator stop
     * @throws EmulationException
     */
    public BreakCondition play() throws EmulationException {
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

        DisassembledInstruction disassembledInstruction = new DisassembledInstruction();

        cpuState.setAllRegistersValid();

        try {
            for (;;) {
                
                disassembledInstruction.reset();

                disassembledInstruction.getNextInstruction(memory, cpuState.pc);
    
                disassembledInstruction.opcode = OpCode.opCodeMap[disassembledInstruction.data[0]];
    
                disassembledInstruction.decodeInstructionOperands(cpuState.pc, memory);

                if (instructionPrintWriter != null) {
                    // copying to make sure we keep a reference even if instructionPrintWriter gets set to null in between but still avoid costly synchronization
                    PrintWriter printWriter = instructionPrintWriter;
                    if (printWriter != null) {
                        // OK. copy is still not null
                        disassembledInstruction.formatOperandsAndComment(cpuState, false, outputOptions);
                        printWriter.print("0x" + Format.asHex(cpuState.pc, 8) + " " + disassembledInstruction);
                    }
                }
                
                switch (disassembledInstruction.opcode.encoding) {
                    case 0xA600: /* ADD Rj, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) + (cpuState.getReg(disassembledInstruction.j) & 0xFFFFFFFFL);
                        result32 = (int) result64;
                        S1 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassembledInstruction.j) & 0x80000000) >>> 31;
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA400: /* ADD #i4, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) + disassembledInstruction.x;
                        result32 = (int) result64;
                        S1 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
                        S2 = 0; /* unsigned extension of x means positive */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA500: /* ADD2 #i4, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) + (Dfr.extn(4, disassembledInstruction.x) & 0xFFFFFFFFL);
                        result32 = (int) result64;
                        S1 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
                        S2 = 1; /* negative extension of x means negative */
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA700: /* ADDC Rj, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) + (cpuState.getReg(disassembledInstruction.j) & 0xFFFFFFFFL) + cpuState.C;
                        result32 = (int) result64;
                        S1 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassembledInstruction.j) & 0x80000000) >>> 31; // TODO : Shouldn't it take C into account ?
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (~(S1 ^ S2)) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>>32);
    
                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA200: /* ADDN Rj, Ri */
                        cpuState.setReg(disassembledInstruction.i, cpuState.getReg(disassembledInstruction.i) + cpuState.getReg(disassembledInstruction.j));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA000: /* ADDN #i4, Ri */
                        cpuState.setReg(disassembledInstruction.i, cpuState.getReg(disassembledInstruction.i) + disassembledInstruction.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA100: /* ADDN2 #i4, Ri */
                        cpuState.setReg(disassembledInstruction.i, cpuState.getReg(disassembledInstruction.i) + Dfr.extn(4, disassembledInstruction.x));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAC00: /* SUB Rj, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) - (cpuState.getReg(disassembledInstruction.j) & 0xFFFFFFFFL);
                        S1 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassembledInstruction.j) & 0x80000000) >>> 31;
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
    
                        cpuState.setReg(disassembledInstruction.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAD00: /* SUBC Rj, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) - (cpuState.getReg(disassembledInstruction.j) & 0xFFFFFFFFL) - cpuState.C;
                        S1 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassembledInstruction.j) & 0x80000000) >>> 31; // TODO : Shouldn't it take C into account ?
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */
    
                        cpuState.setReg(disassembledInstruction.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAE00: /* SUBN Rj, Ri */
                        cpuState.setReg(disassembledInstruction.i, cpuState.getReg(disassembledInstruction.i) - cpuState.getReg(disassembledInstruction.j));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xAA00: /* CMP Rj, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) - (cpuState.getReg(disassembledInstruction.j) & 0xFFFFFFFFL);
                        S1 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassembledInstruction.j) & 0x80000000) >>> 31;
                        Sr = (int) ((result64 & 0x80000000L) >>> 31);
    
                        cpuState.N = Sr;
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (S1 ^ S2) & (S1 ^ Sr);
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32); /* TODO is this really the definition of borrow ? */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xA800: /* CMP #i4, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) - disassembledInstruction.x;
                        /* optimize : 0 extension of x means S2 is 0, right ?  */
                        S1 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
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
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) - (Dfr.extn(4, disassembledInstruction.x) & 0xFFFFFFFFL);
                        S1 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
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
                        result32 = cpuState.getReg(disassembledInstruction.i) & cpuState.getReg(disassembledInstruction.j);
                        cpuState.setReg(disassembledInstruction.i, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x8400: /* AND Rj, @Ri */
                        result32 = memory.load32(cpuState.getReg(disassembledInstruction.i)) & cpuState.getReg(disassembledInstruction.j);
                        memory.store32(cpuState.getReg(disassembledInstruction.i), result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8500: /* ANDH Rj, @Ri */
                        result32 = memory.loadUnsigned16(cpuState.getReg(disassembledInstruction.i)) & cpuState.getReg(disassembledInstruction.j);
                        memory.store16(cpuState.getReg(disassembledInstruction.i), result32);
    
                        cpuState.N = (result32 & 0x8000) >>> 15;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8600: /* ANDB Rj, @Ri */
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) & cpuState.getReg(disassembledInstruction.j);
                        memory.store8(cpuState.getReg(disassembledInstruction.i), result32);
    
                        cpuState.N = (result32 & 0x80) >>> 7;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9200: /* OR Rj, Ri */
                        result32 = cpuState.getReg(disassembledInstruction.i) | cpuState.getReg(disassembledInstruction.j);
                        cpuState.setReg(disassembledInstruction.i, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9400: /* OR Rj, @Ri */
                        result32 = memory.load32(cpuState.getReg(disassembledInstruction.i)) | cpuState.getReg(disassembledInstruction.j);
                        memory.store32(cpuState.getReg(disassembledInstruction.i), result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9500: /* ORH Rj, @Ri */
                        result32 = memory.loadUnsigned16(cpuState.getReg(disassembledInstruction.i)) | cpuState.getReg(disassembledInstruction.j);
                        memory.store16(cpuState.getReg(disassembledInstruction.i), result32);
    
                        cpuState.N = (result32 & 0x8000) >>> 15;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9600: /* ORB Rj, @Ri */
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) | cpuState.getReg(disassembledInstruction.j);
                        memory.store8(cpuState.getReg(disassembledInstruction.i), result32);
    
                        cpuState.N = (result32 & 0x80) >>> 7;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9A00: /* EOR Rj, Ri */
                        result32 = cpuState.getReg(disassembledInstruction.i) ^ cpuState.getReg(disassembledInstruction.j);
                        cpuState.setReg(disassembledInstruction.i, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9C00: /* EOR Rj, @Ri */
                        result32 = memory.load32(cpuState.getReg(disassembledInstruction.i)) ^ cpuState.getReg(disassembledInstruction.j);
                        memory.store32(cpuState.getReg(disassembledInstruction.i), result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9D00: /* EORH Rj, @Ri */
                        result32 = memory.loadUnsigned16(cpuState.getReg(disassembledInstruction.i)) ^ cpuState.getReg(disassembledInstruction.j);
                        memory.store16(cpuState.getReg(disassembledInstruction.i), result32);
    
                        cpuState.N = (result32 & 0x8000) >>> 15;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9E00: /* EORB Rj, @Ri */
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) ^ cpuState.getReg(disassembledInstruction.j);
                        memory.store8(cpuState.getReg(disassembledInstruction.i), result32);
    
                        cpuState.N = (result32 & 0x80) >>> 7;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8000: /* BANDL #u4, @Ri (u4: 0 to 0FH) */
                        // Note : AND'ing with FFFFxxxx is like AND'ing only the lowest 4 bits with xxxx (1 is neutral for AND)
                        memory.store8(cpuState.getReg(disassembledInstruction.i), memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) & (0xF0 + disassembledInstruction.x));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8100: /* BANDH #u4, @Ri (u4: 0 to 0FH) */
                        // Note : AND'ing with xxxxFFFF is like AND'ing only the highest 4 bits with xxxx (1 is neutral for AND)
                        memory.store8(cpuState.getReg(disassembledInstruction.i), memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) & ((disassembledInstruction.x << 4) + 0x0F));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9000: /* BORL #u4, @Ri (u4: 0 to 0FH) */
                        // Note : OR'ing with 0000xxxx is like OR'ing only the lowest 4 bits with xxxx (0 is neutral for OR)
                        memory.store8(cpuState.getReg(disassembledInstruction.i), memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) | disassembledInstruction.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9100: /* BORH #u4, @Ri (u4: 0 to 0FH) */
                        // Note : OR'ing with xxxx0000 is like OR'ing only the highest 4 bits with xxxx (0 is neutral for OR)
                        memory.store8(cpuState.getReg(disassembledInstruction.i), memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) | (disassembledInstruction.x << 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9800: /* BEORL #u4, @Ri (u4: 0 to 0FH) */
                        // Note : EOR'ing with 0000xxxx is like EOR'ing only the lowest 4 bits with xxxx (0 is neutral for EOR)
                        memory.store8(cpuState.getReg(disassembledInstruction.i), memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) ^ disassembledInstruction.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9900: /* BEORH #u4, @Ri (u4: 0 to 0FH) */
                        // Note : EOR'ing with xxxx0000 is like EORing only the highest 4 bits with xxxx (0 is neutral for EOR)
                        memory.store8(cpuState.getReg(disassembledInstruction.i), memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) ^ (disassembledInstruction.x << 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x8800: /* BTSTL #u4, @Ri (u4: 0 to 0FH) */
                        // Note : testing 8 bits AND 0000xxxx is like testing only the lowest 4 bits AND xxxx (0 is absorbing for AND)
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) & disassembledInstruction.x;
    
                        cpuState.N = 0;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 2 + a;
                        break;
    
                    case 0x8900: /* BTSTH #u4, @Ri (u4: 0 to 0FH) */
                        // Note : testing 8 bits AND xxxx0000 is like testing only the highest 4 bits AND xxxx (0 is absorbing for AND)
                        result32 = memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.i)) & (disassembledInstruction.x << 4);
    
                        cpuState.N = (result32 & 0x80) >>> 7;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 2 + a;
                        break;
    
                    case 0xAF00: /* MUL Rj,Ri */
                        result64 = ((long) cpuState.getReg(disassembledInstruction.j)) * ((long) cpuState.getReg(disassembledInstruction.i));
                        cpuState.setReg(CPUState.MDH, (int) (result64 >> 32));
                        cpuState.setReg(CPUState.MDL, (int) (result64 & 0xFFFFFFFFL));
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31); /*see pdf*/
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (int) ((result64 & 0x100000000L) >>> 32);

                        cpuState.pc += 2;

                        cycles = 5;
                        break;
    
                    case 0xAB00: /* MULU Rj,Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) * (cpuState.getReg(disassembledInstruction.j) & 0xFFFFFFFFL);
                        cpuState.setReg(CPUState.MDH, (int) (result64 >> 32));
                        cpuState.setReg(CPUState.MDL, (int) (result64 & 0xFFFFFFFFL));
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31); /*see pdf*/
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.V = (int) ((result64 & 0x100000000L) >>> 32);

                        cpuState.pc += 2;

                        cycles = 5;
                        break;
    
                    case 0xBF00: /* MULH Rj,Ri */
                        result32 = ((short) cpuState.getReg(disassembledInstruction.j)) * ((short) cpuState.getReg(disassembledInstruction.i));
                        cpuState.setReg(CPUState.MDL, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 3;
                        break;
    
                    case 0xBB00: /* MULUH Rj,Ri */
                        result32 = (cpuState.getReg(disassembledInstruction.j) & 0xFFFF) * (cpuState.getReg(disassembledInstruction.i) & 0xFFFF);
                        cpuState.setReg(CPUState.MDL, result32);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;

                        cpuState.pc += 2;

                        cycles = 3;
                        break;
    
                    case 0x9740: /* DIV0S Ri */
                        S1 = (cpuState.getReg(CPUState.MDL) & 0x80000000) >>> 31;
                        S2 = (cpuState.getReg(disassembledInstruction.i) & 0x80000000) >>> 31;
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
                            result64 = (cpuState.getReg(CPUState.MDH) & 0xFFFFFFFFL) + (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL);
                            result32 = (int) result64;
                            cpuState.C = (int) ((result64 & 0x100000000L) >>> 32);
                            cpuState.Z = (result32 == 0)?1:0;
                        }
                        else {
                            // Dividend and divisor have same signs
                            result64 = (cpuState.getReg(CPUState.MDH) & 0xFFFFFFFFL) - (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL);
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
                            result64 = cpuState.getReg(CPUState.MDH) + cpuState.getReg(disassembledInstruction.i);
                            result32 = (int) result64;
                            cpuState.C = (result32 == result64) ? 0 : 1;
                            cpuState.Z = (result32 == 0)?1:0;
                        }
                        else {
                            result64 = cpuState.getReg(CPUState.MDH) - cpuState.getReg(disassembledInstruction.i);
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
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) << (cpuState.getReg(disassembledInstruction.j) & 0x1F);
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31);
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32);
    
                        cpuState.setReg(disassembledInstruction.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB400: /* LSL #u4, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) << disassembledInstruction.x;
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31);
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.C = (disassembledInstruction.x == 0) ? 0 : (int) ((result64 & 0x100000000L) >>> 32);
    
                        cpuState.setReg(disassembledInstruction.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB500: /* LSL2 #u4, Ri */
                        result64 = (cpuState.getReg(disassembledInstruction.i) & 0xFFFFFFFFL) << (disassembledInstruction.x + 16);
    
                        cpuState.N = (int) ((result64 & 0x80000000L) >>> 31);
                        cpuState.Z = (result64 == 0) ? 1 : 0;
                        cpuState.C = (int) ((result64 & 0x100000000L) >>> 32);
    
                        cpuState.setReg(disassembledInstruction.i, (int) result64);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB200: /* LSR Rj, Ri */
                        result32 = cpuState.getReg(disassembledInstruction.i) >>> (cpuState.getReg(disassembledInstruction.j) & 0x1F);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = ((cpuState.getReg(disassembledInstruction.j) & 0x1F) == 0) ? 0 : (cpuState.getReg(disassembledInstruction.i) >> ((cpuState.getReg(disassembledInstruction.j) & 0x1F) - 1)) & 1;

                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB000: /* LSR #u4, Ri */
                        result32 = cpuState.getReg(disassembledInstruction.i) >>> disassembledInstruction.x;
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = (disassembledInstruction.x == 0) ? 0 : (cpuState.getReg(disassembledInstruction.i) >> (disassembledInstruction.x - 1)) & 1;
    
                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB100: /* LSR2 #u4, Ri */
                        result32 = cpuState.getReg(disassembledInstruction.i) >>> (disassembledInstruction.x + 16);
    
                        cpuState.N = 0;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = (cpuState.getReg(disassembledInstruction.i) >> (disassembledInstruction.x + 15)) & 1;
    
                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xBA00: /* ASR Rj, Ri */
                        result32 = cpuState.getReg(disassembledInstruction.i) >> (cpuState.getReg(disassembledInstruction.j) & 0x1F);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = ((cpuState.getReg(disassembledInstruction.j) & 0x1F) == 0) ? 0 : (cpuState.getReg(disassembledInstruction.i) >> ((cpuState.getReg(disassembledInstruction.j) & 0x1F) - 1)) & 1;
    
                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB800: /* ASR #u4, Ri */
                        result32 = cpuState.getReg(disassembledInstruction.i) >> disassembledInstruction.x;
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = (disassembledInstruction.x == 0) ? 0 : (cpuState.getReg(disassembledInstruction.i) >> (disassembledInstruction.x - 1)) & 1;
    
                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xB900: /* ASR2 #u4, Ri */
                        result32 = cpuState.getReg(disassembledInstruction.i) >> (disassembledInstruction.x + 16);
    
                        cpuState.N = (result32 & 0x80000000) >>> 31;
                        cpuState.Z = (result32 == 0) ? 1 : 0;
                        cpuState.C = (cpuState.getReg(disassembledInstruction.i) >> (disassembledInstruction.x + 15)) & 1;
    
                        cpuState.setReg(disassembledInstruction.i, result32);

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F80: /* LDI:32 #i32, Ri */
                        cpuState.setReg(disassembledInstruction.i, disassembledInstruction.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 6;

                        cycles = 3;
                        break;
    
                    case 0x9B00: /* LDI:20 #i20, Ri */
                        cpuState.setReg(disassembledInstruction.i, disassembledInstruction.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 4;

                        cycles = 2;
                        break;
    
                    case 0xC000: /* LDI:8 #i8, Ri */
                        cpuState.setReg(disassembledInstruction.i, disassembledInstruction.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x0400: /* LD @Rj, Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.load32(cpuState.getReg(disassembledInstruction.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0000: /* LD @(R13,Rj), Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.load32(cpuState.getReg(13) + cpuState.getReg(disassembledInstruction.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x2000: /* LD @(R14,disp10), Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.load32(cpuState.getReg(14) + Dfr.signExtend(8, disassembledInstruction.x) * 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0300: /* LD @(R15,udisp6), Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.load32(cpuState.getReg(15) + disassembledInstruction.x * 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0700: /* LD @R15+, Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.load32(cpuState.getReg(15)));
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
                        cpuState.setReg(CPUState.DEDICATED_REG_OFFSET + disassembledInstruction.i, memory.load32(cpuState.getReg(15)));
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
                        cpuState.setReg(disassembledInstruction.i, memory.loadUnsigned16(cpuState.getReg(disassembledInstruction.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0100: /* LDUH @(R13,Rj), Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.loadUnsigned16(cpuState.getReg(13) + cpuState.getReg(disassembledInstruction.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x4000: /* LDUH @(R14,disp9), Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.loadUnsigned16(cpuState.getReg(14) + Dfr.signExtend(8, disassembledInstruction.x) * 2));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0600: /* LDUB @Rj, Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x0200: /* LDUB @(R13,Rj), Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.loadUnsigned8(cpuState.getReg(13) + cpuState.getReg(disassembledInstruction.j)));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x6000: /* LDUB @(R14,disp8), Ri */
                        cpuState.setReg(disassembledInstruction.i, memory.loadUnsigned8(cpuState.getReg(14) + disassembledInstruction.x));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1400: /* ST Ri, @Rj */
                        memory.store32(cpuState.getReg(disassembledInstruction.j), cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1000: /* ST Ri, @(R13,Rj) */
                        memory.store32(cpuState.getReg(13) + cpuState.getReg(disassembledInstruction.j), cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x3000: /* ST Ri, @(R14,disp10) */
                        memory.store32(cpuState.getReg(14) + Dfr.signExtend(8, disassembledInstruction.x) * 4, cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1300: /* ST Ri, @(R15,udisp6) */
                        memory.store32(cpuState.getReg(15) + disassembledInstruction.x * 4, cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1700: /* ST Ri, @-R15 */
                        cpuState.setReg(15, cpuState.getReg(15) - 4);
                        if (disassembledInstruction.i == 15) {
                            /*special case for R15: value stored is R15 before it was decremented */
                            memory.store32(cpuState.getReg(15), cpuState.getReg(15) + 4);
                        }
                        else {
                            memory.store32(cpuState.getReg(15), cpuState.getReg(disassembledInstruction.i));
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
                        memory.store32(cpuState.getReg(15), cpuState.getReg(CPUState.DEDICATED_REG_OFFSET + disassembledInstruction.i));
    
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
                        memory.store16(cpuState.getReg(disassembledInstruction.j), cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1100: /* STH Ri, @(R13,Rj) */
                        memory.store16(cpuState.getReg(13) + cpuState.getReg(disassembledInstruction.j), cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x5000: /* STH Ri, @(R14,disp9) */
                        memory.store16(cpuState.getReg(14) + Dfr.signExtend(8, disassembledInstruction.x) * 2, cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1600: /* STB Ri, @Rj */
                        memory.store8(cpuState.getReg(disassembledInstruction.j), cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x1200: /* STB Ri, @(R13,Rj) */
                        memory.store8(cpuState.getReg(13) + cpuState.getReg(disassembledInstruction.j), cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x7000: /* STB Ri, @(R14,disp8) */
                        memory.store8(cpuState.getReg(14) + Dfr.signExtend(8, disassembledInstruction.x), cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x8B00: /* MOV Rj, Ri */
                        cpuState.setReg(disassembledInstruction.i, cpuState.getReg(disassembledInstruction.j));
    
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
                        cpuState.setReg(disassembledInstruction.i, cpuState.getReg(CPUState.DEDICATED_REG_OFFSET + disassembledInstruction.j));
    
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
                        cpuState.setReg(CPUState.DEDICATED_REG_OFFSET + disassembledInstruction.j, cpuState.getReg(disassembledInstruction.i));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x1710: /* MOV PS, Ri */
                        cpuState.setReg(disassembledInstruction.i, cpuState.getPS());
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x0710: /* MOV Ri, PS */
                        cpuState.setPS(cpuState.getReg(disassembledInstruction.i));
    
                        /* NZVC is part of the PS !*/

                        cpuState.pc += 2;

                        cycles = c;
                        break;
    
                    case 0x9700: /* JMP @Ri */
                        cpuState.pc = cpuState.getReg(disassembledInstruction.i);
    
                        /* No change to NZVC */

                        cycles = 2;
                        break;
    
                    case 0xD000: /* CALL label12 */
                        if (callStack != null) {
                            synchronized (callStack) {
                                // test again. This avoids the cost of synchronisation when running with call stack window closed,
                                // but still guarantees synchronized access when it's open
                                if (callStack != null) {
                                    callStack.push(new CallStackItem(cpuState.pc, cpuState.getReg(CPUState.SP)));
                                }
                            }
                        }
                        cpuState.setReg(CPUState.RP, cpuState.pc + 2);
                        cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(11, disassembledInstruction.x) * 2; // TODO check *2 ?

                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0x9710: /* CALL @Ri */
                        if (callStack != null) {
                            synchronized (callStack) {
                                // test again. This avoids the cost of synchronisation when running with call stack window closed,
                                // but still guarantees synchronized access when it's open
                                if (callStack != null) {
                                    callStack.push(new CallStackItem(cpuState.pc, cpuState.getReg(CPUState.SP)));
                                }
                            }
                        }
                        cpuState.setReg(CPUState.RP, cpuState.pc + 2);
                        cpuState.pc = cpuState.getReg(disassembledInstruction.i);
    
                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0x9720: /* RET */
                        if (callStack != null) {
                            synchronized (callStack) {
                                // test again. This avoids the cost of synchronisation when running with call stack window closed,
                                // but still guarantees synchronized access when it's open
                                if (callStack != null && !callStack.isEmpty()) {
                                    callStack.pop();
                                }
                            }
                        }
                        cpuState.pc = cpuState.getReg(CPUState.RP);
    
                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0x1F00: /* INT #u8 */
                        if (callStack != null) {
                            synchronized (callStack) {
                                // test again. This avoids the cost of synchronisation when running with call stack window closed,
                                // but still guarantees synchronized access when it's open
                                if (callStack != null) {
                                    callStack.push(new CallStackItem(cpuState.pc, cpuState.getReg(CPUState.SP) /* +8 ? */));
                                }
                            }
                        }
                        processInterrupt(disassembledInstruction.x, cpuState.pc + 2);
                        cpuState.I = 0;

                        /* No change to NZVC */
    
                        cycles = 3 + 3 * a;
                        break;
    
                    case 0x9F30: /* INTE */
                        if (callStack != null) {
                            synchronized (callStack) {
                                // test again. This avoids the cost of synchronisation when running with call stack window closed,
                                // but still guarantees synchronized access when it's open
                                if (callStack != null) {
                                    callStack.push(new CallStackItem(cpuState.pc, cpuState.getReg(CPUState.SP) /* +8 ? */));
                                }
                            }
                        }
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
                        if (callStack != null) {
                            synchronized (callStack) {
                                // test again. This avoids the cost of synchronisation when running with call stack window closed,
                                // but still guarantees synchronized access when it's open
                                if (callStack != null && !callStack.isEmpty()) {
                                    callStack.pop();
                                }                                    
                            }
                        }
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
                        cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
    
                        /* No change to NZVC */
    
                        cycles = 2;
                        break;
    
                    case 0xE200: /* BEQ label9 */
                        if (cpuState.Z == 1) {
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
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
                            cpuState.pc = cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2;
                            cycles = 2;
                        }
                        else {
                            cpuState.pc += 2;
                            cycles = 1;
                        }
    
                        /* No change to NZVC */
    
                        break;
    
                    case 0x9F00: /* JMP:D @Ri */
                        setDelayedChanges(cpuState.getReg(disassembledInstruction.i), null);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xD800: /* CALL:D label12 */
                        if (callStack != null) {
                            synchronized (callStack) {
                                // test again. This avoids the cost of synchronisation when running with call stack window closed,
                                // but still guarantees synchronized access when it's open
                                if (callStack != null) {
                                    callStack.push(new CallStackItem(cpuState.pc, cpuState.getReg(CPUState.SP)));
                                }
                            }
                        }
                        setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(11, disassembledInstruction.x) * 2, cpuState.pc + 4);  // TODO check *2
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F10: /* CALL:D @Ri */
                        if (callStack != null) {
                            synchronized (callStack) {
                                // test again. This avoids the cost of synchronisation when running with call stack window closed,
                                // but still guarantees synchronized access when it's open
                                if (callStack != null) {
                                    callStack.push(new CallStackItem(cpuState.pc, cpuState.getReg(CPUState.SP)));
                                }
                            }
                        }
                        setDelayedChanges(cpuState.getReg(disassembledInstruction.i), cpuState.pc + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0x9F20: /* RET:D */
                        if (callStack != null) {
                            synchronized (callStack) {
                                // test again. This avoids the cost of synchronisation when running with call stack window closed,
                                // but still guarantees synchronized access when it's open
                                if (callStack != null && !callStack.isEmpty()) {
                                    callStack.pop();
                                }
                            }
                        }
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
                        setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
    
                    case 0xF200: /* BEQ:D label9 */
                        if (cpuState.Z == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;

                        //* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF300: /* BNE:D label9 */
                        if (cpuState.Z == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF400: /* BC:D label9 */
                        if (cpuState.C == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF500: /* BNC:D label9 */
                        if (cpuState.C == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF600: /* BN:D label9 */
                        if (cpuState.N == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF700: /* BP:D label9 */
                        if (cpuState.N == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF800: /* BV:D label9 */
                        if (cpuState.V == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xF900: /* BNV:D label9 */
                        if (cpuState.V == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFA00: /* BLT:D label9 */
                        if ((cpuState.V ^ cpuState.N) == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFB00: /* BGE:D label9 */
                        if ((cpuState.V ^ cpuState.N) == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFC00: /* BLE:D label9 */
                        if (((cpuState.V ^ cpuState.N) | cpuState.Z) == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFD00: /* BGT:D label9 */
                        if (((cpuState.V ^ cpuState.N) | cpuState.Z) == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFE00: /* BLS:D label9 */
                        if ((cpuState.C | cpuState.Z) == 1) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;

                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0xFF00: /* BHI:D label9 */
                        if ((cpuState.C | cpuState.Z) == 0) {
                            setDelayedChanges(cpuState.pc + 2 + Dfr.signExtend(8, disassembledInstruction.x) * 2, null);
                        }

                        cpuState.pc += 2;
    
                        /* No change to NZVC */
    
                        cycles = 1;
                        break;
    
                    case 0x0800: /* DMOV @dir10, R13 */
                        cpuState.setReg(13, memory.load32(disassembledInstruction.x * 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1800: /* DMOV R13, @dir10 */
                        memory.store32(disassembledInstruction.x * 4, cpuState.getReg(13));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x0C00: /* DMOV @dir10, @R13+ */
                        memory.store32(cpuState.getReg(13), memory.load32(disassembledInstruction.x * 4));
                        cpuState.setReg(13, cpuState.getReg(13) + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1C00: /* DMOV @R13+, @dir10 */
                        memory.store32(disassembledInstruction.x * 4, memory.load32(cpuState.getReg(13)));
                        cpuState.setReg(13, cpuState.getReg(13) + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x0B00: /* DMOV @dir10, @–R15 */
                        cpuState.setReg(15, cpuState.getReg(15) - 4);
                        memory.store32(cpuState.getReg(15), memory.load32(disassembledInstruction.x * 4));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1B00: /* DMOV @R15+, @dir10 */
                        memory.store32(disassembledInstruction.x * 4, memory.load32(cpuState.getReg(15)));
                        cpuState.setReg(15, cpuState.getReg(15) + 4);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x0900: /* DMOVH @dir9, R13 */
                        cpuState.setReg(13, memory.loadUnsigned16(disassembledInstruction.x * 2));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1900: /* DMOVH R13, @dir9 */
                        memory.store16(disassembledInstruction.x * 2, cpuState.getReg(13));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x0D00: /* DMOVH @dir9, @R13+ */
                        memory.store16(cpuState.getReg(13), memory.loadUnsigned16(disassembledInstruction.x * 2));
                        cpuState.setReg(13, cpuState.getReg(13) + 2);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1D00: /* DMOVH @R13+, @dir9 */
                        memory.store16(disassembledInstruction.x * 2, memory.loadUnsigned16(cpuState.getReg(13)));
                        cpuState.setReg(13, cpuState.getReg(13) + 2);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x0A00: /* DMOVB @dir8, R13 */
                        cpuState.setReg(13, memory.loadUnsigned8(disassembledInstruction.x));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = b;
                        break;
    
                    case 0x1A00: /* DMOVB R13, @dir8 */
                        memory.store8(disassembledInstruction.x, cpuState.getReg(13));
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x0E00: /* DMOVB @dir8, @R13+ */
                        memory.store8(cpuState.getReg(13), memory.loadUnsigned8(disassembledInstruction.x));
                        cpuState.setReg(13, cpuState.getReg(13) + 1);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0x1E00: /* DMOVB @R13+, @dir8 */
                        memory.store8(disassembledInstruction.x, memory.loadUnsigned8(cpuState.getReg(13)));
                        cpuState.setReg(13, cpuState.getReg(13) + 1);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
    
                    case 0xBC00: /* LDRES @Ri+, #u4 */
                        /* TODO FUTURE */
                        System.err.println(disassembledInstruction.opcode.toString() + " is not implemented (resource) at PC=0x" + Format.asHex(cpuState.pc-2,8));
                        /*sentToResource(x, memory.load32(cpuState.getReg(i)));
                        cpuState.getReg(i) + = 4;*/
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0xBD00: /* STRES #u4, @Ri+ */
                        /* TODO FUTURE */
                        System.err.println(disassembledInstruction.opcode.toString() + " is not implemented (resource) at PC=0x" + Format.asHex(cpuState.pc-2,8));
                        /* memory.store32(cpuState.getReg(i), getFromResource(x); cpuState.getReg(i) + = 4;*/
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = a;
                        break;
    
                    case 0x9FC0: /* COPOP #u4, #CC, CRj, CRi */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(disassembledInstruction.opcode.toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(cpuState.pc-2,8));
    
                        /* No change to NZVC */

                        cpuState.pc += 4;

                        cycles = 2 + a;
                        break;
    
                    case 0x9FD0: /* COPLD #u4, #CC, Rj, CRi */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(disassembledInstruction.opcode.toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(cpuState.pc-2,8));
                        /* cpuState.getReg(CPUState.COPROCESSOR_REG_OFFSET + i) = cpuState.getReg(j); */
    
                        /* No change to NZVC */

                        cpuState.pc += 4;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9FE0: /* COPST #u4, #CC, CRj, Ri */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(disassembledInstruction.opcode.toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(cpuState.pc-2,8));
                        /* cpuState.getReg(i) = cpuState.getReg(CPUState.COPROCESSOR_REG_OFFSET + j); */
    
                        /* No change to NZVC */

                        cpuState.pc += 4;

                        cycles = 1 + 2 * a;
                        break;
    
                    case 0x9FF0: /* COPSV #u4, #CC, CRj, Ri */
                        /* TODO FUTURE coprocessor operation */
                        System.err.println(disassembledInstruction.opcode.toString() + " is not implemented (coprocessor) at PC=0x" + Format.asHex(cpuState.pc-2,8));
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
                        cpuState.setCCR(cpuState.getCCR() & disassembledInstruction.x);
    
                        /* NZVC is part of the CCR !*/

                        cpuState.pc += 2;

                        cycles = c;
                        break;
                    
                    case 0x9300: /* ORCCR #u8 */
                        cpuState.setCCR(cpuState.getCCR() | disassembledInstruction.x);
    
                        /* NZVC is part of the CCR !*/

                        cpuState.pc += 2;

                        cycles = c;
                        break;
                    
                    case 0x8700: /* STILM #u8 */
                        cpuState.setILM(disassembledInstruction.x);
    
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0xA300: /* ADDSP #s10 */
                        cpuState.setReg(15, cpuState.getReg(15) + (Dfr.signExtend(8, disassembledInstruction.x) * 4));
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x9780: /* EXTSB Ri */
                        cpuState.setReg(disassembledInstruction.i, Dfr.signExtend(8, cpuState.getReg(disassembledInstruction.i)));
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x9790: /* EXTUB Ri */
                        cpuState.setReg(disassembledInstruction.i, cpuState.getReg(disassembledInstruction.i) & 0xFF);
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x97A0: /* EXTSH Ri */
                        cpuState.setReg(disassembledInstruction.i, Dfr.signExtend(16, cpuState.getReg(disassembledInstruction.i)));
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x97B0: /* EXTUH Ri */
                        cpuState.setReg(disassembledInstruction.i, cpuState.getReg(disassembledInstruction.i) & 0xFFFF);
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 1;
                        break;
                    
                    case 0x8C00: /* LDM0 (reglist) */
                        n = 0;
                        for (int r = 0; r <= 7; r++) {
                            if ((disassembledInstruction.x & (1 << r)) != 0) {
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
                            if ((disassembledInstruction.x & (1 << r)) != 0) {
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
                            if ((disassembledInstruction.x & (1 << r)) != 0) {
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
                        if ((disassembledInstruction.x & 0x1) != 0) {
                            cpuState.setReg(15, cpuState.getReg(15) - 4);
                            /*special case for R15: value stored is R15 before it was decremented */
                            memory.store32(cpuState.getReg(15), cpuState.getReg(15) + 4);
                            n++;
                        }
                        for (int r = 1; r <= 7; r++) {
                            if ((disassembledInstruction.x & (1 << r)) != 0) {
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
                        cpuState.setReg(15, cpuState.getReg(15) - disassembledInstruction.x * 4);
                        
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
                        result32 = cpuState.getReg(disassembledInstruction.i);
                        cpuState.setReg(disassembledInstruction.i, memory.loadUnsigned8(cpuState.getReg(disassembledInstruction.j)));
                        memory.store8(cpuState.getReg(disassembledInstruction.j), result32);
                        
                        /* No change to NZVC */

                        cpuState.pc += 2;

                        cycles = 2 * a;
                        break;
                    
                    default:
                        throw new EmulationException("Unknown opcode encoding : 0x" + Integer.toHexString(disassembledInstruction.opcode.encoding));
                }

                cycleRemaining -= cycles;
                totalCycles += cycles;

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
                else {
                    // If not in a delay slot, see if it's time to check interrupts
                    if (cycleRemaining <= 0) {
                        /* Time to process waiting interrupts, if any */
                        synchronized (interruptRequests) {
                            if(!interruptRequests.isEmpty()) {
                                InterruptRequest interruptRequest = interruptRequests.get(0);
                                if (cpuState.accepts(interruptRequest)){
                                    if (instructionPrintWriter != null) {
                                        instructionPrintWriter.println("------------------------- Accepting " + interruptRequest);
                                    }
                                    interruptRequests.remove(0);
                                    processInterrupt(interruptRequest.getInterruptNumber(), cpuState.pc);
                                    cpuState.setILM(interruptRequest.getICR());
                                }
                            }
                        }

                        cycleRemaining += interruptPeriod;
                    }
                }

                /* Break if requested */
                if (!breakConditions.isEmpty()) {
                    synchronized (breakConditions) {
                        for (BreakCondition breakCondition : breakConditions) {
                            if(breakCondition.matches(cpuState, memory)) {
                                return breakCondition;
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
            System.err.println(cpuState);
            try {
                disassembledInstruction.formatOperandsAndComment(cpuState, false, outputOptions);
                System.err.println("Offending instruction : " + disassembledInstruction);
            }
            catch(Exception e1) {
                System.err.println("Cannot disassemble offending instruction :" + disassembledInstruction.formatDataAsHex());
            }
            System.err.println("(on or before PC=0x" + Format.asHex(cpuState.pc, 8) + ")");
            throw new EmulationException(e);
        }
    }

    private void processInterrupt(int interruptNumber, int pcToStore) {
        cpuState.setReg(CPUState.SSP, cpuState.getReg(CPUState.SSP) - 4);
        memory.store32(cpuState.getReg(CPUState.SSP), cpuState.getPS());
        cpuState.setReg(CPUState.SSP, cpuState.getReg(CPUState.SSP) - 4);
        memory.store32(cpuState.getReg(CPUState.SSP), pcToStore);
        cpuState.setS(0);
        cpuState.pc = memory.load32(cpuState.getReg(CPUState.TBR) + 0x3FC - interruptNumber * 4);
    }

    private void setDelayedChanges(Integer nextPC, Integer nextRP) {
        this.nextPC = nextPC;
        this.nextRP = nextRP;
        this.delaySlotDone = false;
    }

    public void clearBreakConditions() {
        synchronized (breakConditions) {
            breakConditions.clear();
        }
    }

    public void addBreakCondition(BreakCondition breakCondition) {
        synchronized (breakConditions) {
            breakConditions.add(breakCondition);
        }
    }

    public boolean addInterruptRequest(InterruptRequest newInterruptRequest) {
        synchronized (interruptRequests) {
            for (InterruptRequest currentInterruptRequest : interruptRequests) {
                if (currentInterruptRequest.getInterruptNumber() == newInterruptRequest.getInterruptNumber()) {
                    // Same number. Keep highest priority one
                    if ((newInterruptRequest.isNMI() && !currentInterruptRequest.isNMI())
                            || (newInterruptRequest.getICR() < currentInterruptRequest.getICR())) {
                        // New is better. Remove old one then go on adding
                        interruptRequests.remove(currentInterruptRequest);
                        break;
                    }
                    else {
                        // Old is better. No change to the list. Exit
                        return false;
                    }
                }
            }
            interruptRequests.add(newInterruptRequest);
            Collections.sort(interruptRequests, new Comparator<InterruptRequest>() {
                public int compare(InterruptRequest o1, InterruptRequest o2) {
                    // returns a negative number if o1 has higher priority (NMI or lower ICR) and should appear first
                    if (o2.isNMI() != o1.isNMI()) {
                        // If only one is NMI, it has to come on top
                        return (o1.isNMI()?-1:1);
                    }
                    else {
                        // Lower ICR gets a higher priority
                        return o1.getICR() - o2.getICR();
                    }
                }
            });
            return true;
        }
    }

}
