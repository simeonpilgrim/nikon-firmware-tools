package com.nikonhacker.emu;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.AndCondition;
import com.nikonhacker.emu.trigger.condition.BreakCondition;
import com.nikonhacker.emu.trigger.condition.BreakPointCondition;
import com.nikonhacker.gui.component.disassembly.DisassemblyLogger;

import java.io.PrintWriter;
import java.util.*;

public abstract class Emulator implements Clockable {
    protected long                       totalCycles;
    protected DisassemblyLogger          logger;
    protected PrintWriter                breakLogPrintWriter;
    protected       int                  sleepIntervalMs = 0;
    protected final List<BreakCondition> breakConditions = new ArrayList<BreakCondition>();
    protected final HashMap<Integer, BreakCondition> pcBreakConditions = new HashMap<Integer, BreakCondition>();
    protected       boolean              breakConditionsPresent;
    protected       Set<OutputOption>    outputOptions   = EnumSet.noneOf(OutputOption.class);
    protected       boolean              exitSleepLoop   = false;

    StatementContext context = new StatementContext();

    protected Platform platform;

    protected final List<CycleCounterListener> cycleCounterListeners = new ArrayList<CycleCounterListener>();

    protected Statement statement;

    /**
     * An Emulator must receive a platform.
     * @param platform
     */
    public Emulator(Platform platform) {
        this.platform = platform;
    }

    /**
     * Provide an output to send disassembled form of executed instructions to
     * @param logger
     */
    public void setDisassemblyLogger(DisassemblyLogger logger) {
        this.logger = logger;
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

    public final void clearBreakConditions() {
        synchronized (breakConditions) {
            breakConditions.clear();
            pcBreakConditions.clear();
            breakConditionsPresent = false;
        }
    }

    public final void addBreakCondition(BreakCondition breakCondition) {
        boolean isPCCondition = false;
        int pc = 0;

        if (breakCondition instanceof BreakPointCondition) {
            isPCCondition = true;
            pc = ((BreakPointCondition)breakCondition).getPc();
        } else if (breakCondition instanceof AndCondition) {
            List<BreakCondition> conditions = ((AndCondition)breakCondition).getConditions();
            if (conditions.size() == 1) {
                BreakCondition brk = conditions.get(0);
                if (brk instanceof BreakPointCondition) {
                    isPCCondition = true;
                    pc = ((BreakPointCondition)brk).getPc();
                }
            }
        }
        synchronized (breakConditions) {
            if (isPCCondition) {
                pcBreakConditions.put(pc,breakCondition);
            } else {
                breakConditions.add(breakCondition);
            }
            breakConditionsPresent = true;
        }
    }

    public void exitSleepLoop() {
        exitSleepLoop = true;
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

    @Override
    public String toString() {
        return Constants.CHIP_LABEL[getChip()] + " Emulator";
    }

    protected void logIfRequested(DisassemblyLogger logger) throws DisassemblyException {
        if (logger != null && logger.mustLog(platform.cpuState.pc)) {
            StringBuilder msg = new StringBuilder();
            if (logger.isIncludeTimestamp()) {
                msg.append(platform.getMasterClock().getFormatedTotalElapsedTimeMs()).append(" ");
            }
            msg.append("0x").append(Format.asHex(platform.cpuState.pc, 8));

            if (logger.isIncludeIndent()) {
                msg.append(logger.getIndent());
                switch(statement.getInstruction().getFlowType()) {
                    case CALL:
                    case INT:
                        logger.indent();
                        break;
                    case RET:
                        logger.outdent();
                        break;
                }
            }

            if (logger.isIncludeInstruction()) {
                statement.formatOperandsAndComment(context, false, outputOptions);
                msg.append(" ").append(statement.toString(outputOptions));
            }
            logger.println(msg.toString());
        }
    }

    protected void sleep() {
        exitSleepLoop = false;
        if (sleepIntervalMs < 100) {
            try {
                Thread.sleep(sleepIntervalMs);
            } catch (InterruptedException e) {
                // noop
            }
        }
        else {
            for (int i = 0; i < sleepIntervalMs / 100; i++) {
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

    private final boolean executeBreakCondition(final BreakCondition breakCondition) {
        BreakTrigger trigger = breakCondition.getBreakTrigger();
        if (trigger != null) {
            if (trigger.mustBeLogged() && breakLogPrintWriter != null) {
                trigger.log(breakLogPrintWriter, platform, context.callStack);
            }
            if (trigger.getInterruptToRequest() != null) {
                platform.interruptController.request(trigger.getInterruptToRequest());
            }
            if (trigger.getInterruptToWithdraw() != null) {
                platform.interruptController.removeRequest(trigger.getInterruptToWithdraw());
            }
            platform.cpuState.applyRegisterChanges(trigger.getNewCpuStateValues(), trigger.getNewCpuStateFlags());
            if (trigger.getMustStartLogging() && logger != null) {
                logger.setLogging(true);
            }
            if (trigger.getMustStopLogging() && logger != null) {
                logger.setLogging(false);
            }
        }
        if (trigger == null || trigger.mustBreak()) {
            return true;
        }
        return false;
    }
    
    protected final BreakCondition processConditions() {
        synchronized(breakConditions) {
            // check fast pc-based conditions first
            final BreakCondition pcBreakCondition = pcBreakConditions.get(platform.cpuState.getPc()&(~1));
            if (pcBreakCondition!=null) {
                if (executeBreakCondition(pcBreakCondition))
                    return pcBreakCondition;
            }
            // check all other conditions if any
            if (!breakConditions.isEmpty())
                for (BreakCondition breakCondition : breakConditions) {
                    if (breakCondition.matches(platform.cpuState, platform.memory)) {
                        if (executeBreakCondition(breakCondition))
                            return breakCondition;
                    }
                }
        }
        return null;
    }
}
