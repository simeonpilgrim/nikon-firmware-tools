package com.nikonhacker.emu;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

import java.io.PrintWriter;
import java.util.*;

public abstract class Emulator {
    protected Memory memory;
    protected CPUState cpuState;
    protected long totalCycles;
    protected PrintWriter instructionPrintWriter;
    protected PrintWriter breakLogPrintWriter;
    protected Deque<CallStackItem> callStack;
    protected int sleepIntervalMs = 0;
    protected final List<BreakCondition> breakConditions = new ArrayList<BreakCondition>();
    protected Set<OutputOption> outputOptions = EnumSet.noneOf(OutputOption.class);
    protected boolean exitSleepLoop = false;

    /**
     * Provide a PrintWriter to send disassembled form of executed instructions to
     * @param instructionPrintWriter
     */
    public void setInstructionPrintWriter(PrintWriter instructionPrintWriter) {
        this.instructionPrintWriter = instructionPrintWriter;
    }

    /**
     * Provide a PrintWriter to send break triggers log to
     * @param breakLogPrintWriter
     */
    public void setBreakLogPrintWriter(PrintWriter breakLogPrintWriter) {
        this.breakLogPrintWriter = breakLogPrintWriter;
    }

    /**
     * Provide a call stack to write stack entries to it
     * @param callStack
     */
    public void setCallStack(Deque<CallStackItem> callStack) {
        this.callStack = callStack;
    }

    public long getTotalCycles() {
        return totalCycles;
    }

    /**
     * Changes the sleep interval between instructions
     * @param sleepIntervalMs
     */
    public void setSleepIntervalMs(int sleepIntervalMs) {
        this.sleepIntervalMs = sleepIntervalMs;
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

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void setCpuState(CPUState cpuState) {
        this.cpuState = cpuState;
    }

    public void exitSleepLoop() {
        this.exitSleepLoop = true;
    }

    public abstract void setOutputOptions(Set<OutputOption> outputOptions);

    public abstract BreakCondition play() throws EmulationException ;
}
