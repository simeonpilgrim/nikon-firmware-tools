package com.nikonhacker.emu;

import com.nikonhacker.IndentPrinter;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.StatementContext;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.peripherials.interruptController.DummyInterruptController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

import java.io.PrintWriter;
import java.util.*;

public abstract class Emulator {
    protected long totalCycles;
    protected IndentPrinter printer;
    protected PrintWriter breakLogPrintWriter;
    protected int sleepIntervalMs = 0;
    protected final List<BreakCondition> breakConditions = new ArrayList<BreakCondition>();
    protected Set<OutputOption> outputOptions = EnumSet.noneOf(OutputOption.class);
    protected boolean exitSleepLoop = false;

    StatementContext context = new StatementContext();

    // TODO : Shouldn't these 2 only be in the StatementContext object ?
    // TODO : or better yet in a Platform object
    protected Memory memory;
    protected CPUState cpuState;

    protected InterruptController interruptController = new DummyInterruptController();
    protected final Set<CycleCounterListener> cycleCounterListeners = new HashSet<CycleCounterListener>();

    /**
     * Provide an output to send disassembled form of executed instructions to
     * @param printer
     */
    public void setPrinter(PrintWriter printer) {
        if (printer == null)
            this.printer = null;
        else
            this.printer = new IndentPrinter(printer);
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
        context.callStack = callStack;
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

    public void exitSleepLoop() {
        this.exitSleepLoop = true;
    }


    public void setCpuState(CPUState cpuState) {
        this.cpuState = cpuState;
        context.cpuState = cpuState;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
        context.memory = memory;
    }


    public void setInterruptController(InterruptController interruptController) {
        this.interruptController = interruptController;
    }

    public void setOutputOptions(Set<OutputOption> outputOptions) {
        this.outputOptions = outputOptions;
        context.outputOptions = outputOptions;
    }

    /**
     * Starts emulating
     * @return the BreakCondition that caused the emulator to stop
     * @throws EmulationException
     */
    public abstract BreakCondition play() throws EmulationException ;

    public void addCycleCounterListener(CycleCounterListener cycleCounterListener) {
        synchronized (cycleCounterListeners) {
            cycleCounterListeners.add(cycleCounterListener);
        }
    }

    public void removeCycleCounterListener(CycleCounterListener cycleCounterListener) {
        synchronized (cycleCounterListeners) {
            cycleCounterListeners.remove(cycleCounterListener);
        }
    }

    public void clearCycleCounterListeners() {
        synchronized (cycleCounterListeners) {
            cycleCounterListeners.clear();
        }
    }
}
