package com.nikonhacker.emu;

import com.nikonhacker.IndentPrinter;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.StatementContext;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

import java.io.PrintWriter;
import java.util.*;

public abstract class Emulator implements Clockable {
    protected long totalCycles;
    protected IndentPrinter printer;
    protected PrintWriter breakLogPrintWriter;
    protected int sleepIntervalMs = 0;
    protected final List<BreakCondition> breakConditions = new ArrayList<BreakCondition>();
    protected Set<OutputOption> outputOptions = EnumSet.noneOf(OutputOption.class);
    protected boolean exitSleepLoop = false;

    StatementContext context = new StatementContext();

    protected Platform platform;

    protected final List<CycleCounterListener> cycleCounterListeners = new ArrayList<CycleCounterListener>();

    /**
     * An Emulator must receive a platform.
     * @param platform
     */
    public Emulator(Platform platform) {
        this.platform = platform;
    }

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

    // TODO shouldn't the context be filled in the constructor ? Are memory, etc. changed after Emulator construction ?
    public void setContext(DebuggableMemory memory, CPUState cpuState, InterruptController interruptController) {
        context.memory = memory;
        context.cpuState = cpuState;
        context.interruptController = interruptController;
    }
    public void setContextFromPlatform(Platform platform) {
        setContext(platform.getMemory(), platform.getCpuState(), platform.getInterruptController());
    }

    public void setOutputOptions(Set<OutputOption> outputOptions) {
        this.outputOptions = outputOptions;
        context.outputOptions = outputOptions;
    }

    /**
     * Start emulating in sync mode
     * @return the condition that made emulation stop
     * @throws EmulationException
     */
    public BreakCondition play() throws EmulationException {
        Object o = null;
        while (o == null) {
            o = onClockTick();
        }
        return (BreakCondition)o;
    }

    /**
     * Perform one emulation step
     * @return the condition that made emulation stop, or null if it should continue
     * @throws EmulationException
     */
    public abstract BreakCondition onClockTick() throws EmulationException ;

    public void addCycleCounterListener(CycleCounterListener cycleCounterListener) {
        synchronized (cycleCounterListeners) {
            if (!cycleCounterListeners.contains(cycleCounterListener)) {
                cycleCounterListeners.add(cycleCounterListener);
            }
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
