package com.nikonhacker.emu.trigger;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.fr.Syscall;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.CallStackItem;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.condition.BreakCondition;
import com.nikonhacker.emu.trigger.condition.BreakPointCondition;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import com.nikonhacker.emu.trigger.condition.RegisterEqualityBreakCondition;
import com.nikonhacker.emu.trigger.condition.fr.CCRBreakCondition;
import com.nikonhacker.emu.trigger.condition.fr.ILMBreakCondition;
import com.nikonhacker.emu.trigger.condition.fr.SCRBreakCondition;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * A trigger represents a CPU state accompanied by memory conditions which,
 * when matched together, will trigger a break or a log
 */
public class BreakTrigger {
    private String name;
    private Boolean enabled = true;

    private CPUState                        cpuStateValues;
    private CPUState                        cpuStateFlags;
    private List<MemoryValueBreakCondition> memoryValueBreakConditions;

    private boolean mustBeLogged        = false;
    private boolean mustBreak           = true;
    private Integer interruptToRequest  = null;
    private Integer interruptToWithdraw = null;
    /**
     * @deprecated should be removed once prefs have all migrated to newCpuStateFlags
     */
    private Integer pcToSet             = null;
    private boolean mustStartLogging    = false;
    private boolean mustStopLogging     = false;
    private CPUState newCpuStateValues;
    private CPUState newCpuStateFlags;

    private Function function;

    public BreakTrigger(String name, CPUState cpuStateValues, CPUState cpuStateFlags, List<MemoryValueBreakCondition> memoryValueBreakConditions) {
        this.name = name;
        this.cpuStateValues = cpuStateValues;
        this.cpuStateFlags = cpuStateFlags;
        this.memoryValueBreakConditions = memoryValueBreakConditions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CPUState getCpuStateValues() {
        return cpuStateValues;
    }

    public boolean mustBeLogged() {
        return mustBeLogged;
    }

    public boolean getMustBeLogged() {
        return mustBeLogged;
    }

    public void setMustBeLogged(boolean mustBeLogged) {
        this.mustBeLogged = mustBeLogged;
    }

    public boolean mustBreak() {
        return mustBreak;
    }

    public boolean getMustBreak() {
        return mustBreak;
    }

    public void setMustBreak(boolean mustBreak) {
        this.mustBreak = mustBreak;
    }

    public Integer getInterruptToRequest() {
        return interruptToRequest;
    }

    public void setInterruptToRequest(Integer interruptToRequest) {
        this.interruptToRequest = interruptToRequest;
    }

    public Integer getInterruptToWithdraw() {
        return interruptToWithdraw;
    }

    public void setInterruptToWithdraw(Integer interruptToWithdraw) {
        this.interruptToWithdraw = interruptToWithdraw;
    }

    public Integer getPcToSet() {
        if (getNewCpuStateFlags().pc == 0)
            return null;
        else {
            return getNewCpuStateValues().getPc();
        }
    }

    public void setPcToSet(Integer pcToSet) {
        if (pcToSet == null) {
            getNewCpuStateFlags().pc = 0;
        }
        else {
            getNewCpuStateFlags().pc = 1;
            getNewCpuStateFlags().setPc(pcToSet);
        }
    }

    public boolean getMustStartLogging() {
        return mustStartLogging;
    }

    public void setMustStartLogging(boolean mustStartLogging) {
        this.mustStartLogging = mustStartLogging;
    }

    public boolean getMustStopLogging() {
        return mustStopLogging;
    }

    public void setMustStopLogging(boolean mustStopLogging) {
        this.mustStopLogging = mustStopLogging;
    }

    public void setCpuStateValues(CPUState cpuStateValues) {
        this.cpuStateValues = cpuStateValues;
    }

    public CPUState getCpuStateFlags() {
        return cpuStateFlags;
    }

    public void setCpuStateFlags(CPUState cpuStateFlags) {
        this.cpuStateFlags = cpuStateFlags;
    }

    public CPUState getNewCpuStateValues() {
        if (newCpuStateValues == null) {
            newCpuStateValues = (cpuStateValues instanceof FrCPUState)?(new FrCPUState()) : (new TxCPUState());
        }
        return newCpuStateValues;
    }

    public CPUState getNewCpuStateFlags() {
        if (newCpuStateFlags == null) {
            newCpuStateFlags = (cpuStateFlags instanceof FrCPUState)?(new FrCPUState()) : (new TxCPUState());
            newCpuStateFlags.clear();
            newCpuStateFlags.pc = 0;
        }
        return newCpuStateFlags;
    }

    public List<MemoryValueBreakCondition> getMemoryValueBreakConditions() {
        if (memoryValueBreakConditions == null) {
            memoryValueBreakConditions = new ArrayList<MemoryValueBreakCondition>();
        }
        return memoryValueBreakConditions;
    }

    public List<BreakCondition> getBreakConditions(CodeStructure codeStructure, Memory memory) {
        List<BreakCondition> conditions = new ArrayList<BreakCondition>();
        if (cpuStateFlags.pc != 0) {
            if (codeStructure != null && codeStructure.isFunction(cpuStateValues.pc)) {
                // this is a break on a function. Store it for later
                function = codeStructure.getFunction(cpuStateValues.pc);

                // In case this is a syscall, we can replace it by the actual syscall name and params
                if (cpuStateFlags.getReg(12) != 0) {
                    try {
                        Map<Integer,Syscall> syscallMap = Syscall.getMap(memory);
                        int int40address = Syscall.getInt40address();
                        if (int40address == cpuStateValues.pc) {
                            // We're on a syscall. Use the given syscall
                            Syscall syscall = syscallMap.get(cpuStateValues.getReg(12));
                            if (syscall != null) {
                                function = codeStructure.getFunction(syscall.getAddress());
                            }
                        }
                    }
                    catch (ParsingException e) {
                        System.err.println("Could not determine syscall list.");
                        e.printStackTrace();
                    }
                }
            }
            conditions.add(new BreakPointCondition(cpuStateValues.pc, this));
        }
        if (cpuStateFlags instanceof FrCPUState) {
            for (int i = 0; i <= FrCPUState.MDL; i++) {
                if (cpuStateFlags.getReg(i) != 0) {
                    conditions.add(new RegisterEqualityBreakCondition(i, cpuStateValues.getReg(i), this));
                }
            }
            if (((FrCPUState)cpuStateFlags).getCCR() != 0) {
                conditions.add(new CCRBreakCondition(((FrCPUState)cpuStateValues).getCCR(), ((FrCPUState)cpuStateFlags).getCCR(), this));
            }
            if (((FrCPUState)cpuStateFlags).getSCR() != 0) {
                conditions.add(new SCRBreakCondition(((FrCPUState)cpuStateValues).getSCR(), ((FrCPUState)cpuStateFlags).getSCR(), this));
            }
            if (((FrCPUState)cpuStateFlags).getILM() != 0) {
                conditions.add(new ILMBreakCondition(((FrCPUState)cpuStateValues).getILM(), ((FrCPUState)cpuStateFlags).getILM(), this));
            }
        }
        else {
            for (int i = 0; i < TxCPUState.Status; i++) {
                if (cpuStateFlags.getReg(i) != 0) {
                    conditions.add(new RegisterEqualityBreakCondition(i, cpuStateValues.getReg(i), this));
                }
            }
        }

        conditions.addAll(memoryValueBreakConditions);

        return conditions;
    }

    @Override
    public String toString() {
        return (enabled?"ON:  ":"OFF: ") + name + "[" + ((getMustBreak()?"break ":"") + (getMustBeLogged()?"log ":"") + (interruptToRequest!=null?"interrupt ":"") + (interruptToWithdraw!=null?"nointerrupt ":"") + (getPcToSet()!=null?"jump ":"") + (getMustBeLogged()?"startlog ":"") + (getMustBeLogged()?"stoplog ":"")).trim() + "]";
    }

    /**
     * This is the default logging behaviour
     * @param printWriter printWriter to which the log must be output
     * @param platform the platform we're running
     * @param callStack optional call stack at the time the condition matches
     */
    public void log(PrintWriter printWriter, Platform platform, Deque<CallStackItem> callStack) {
        String msg = platform.getMasterClock().getFormatedTotalElapsedTimeMs() + " ";
        if (function != null) {
            // This is a function call. Parse its arguments and log them
            msg += function.getName() + "(";
            if (function.getParameterList() != null) {
                for (Symbol.Parameter parameter : function.getParameterList()) {
                    if (parameter.getInVariableName() != null) {
                        String paramString = parameter.getInVariableName() + "=";
                        int value = platform.getCpuState().getReg(parameter.getRegister());
                        if (parameter.getInVariableName().startsWith("sz")) {
                            paramString+="\"";
                            // Dump as String
                            int character = platform.getMemory().loadUnsigned8(value++, com.nikonhacker.emu.memory.DebuggableMemory.AccessSource.CODE);
                            while (character > 0) {
                                paramString += (char)character;
                                character = platform.getMemory().loadUnsigned8(value++, com.nikonhacker.emu.memory.DebuggableMemory.AccessSource.CODE);
                            }
                            paramString+="\"";
                        }
                        else {
                            // Dump as Int
                            paramString += "0x" + Format.asHex(value,8);
                        }
                        if (!msg.endsWith("(")) {
                            msg+=", ";
                        }
                        msg += paramString;
                    }
                }
            }
            msg +=") ";
        }
        else {
            msg += name;
        }

        String addr = Format.asHex(platform.getCpuState().pc, 8);
        if (!msg.toUpperCase().contains(addr)) {
            // Address is not part of the name of the trigger. Add it.
            msg +=  " triggered at 0x" + addr;
        }

        if (callStack != null) {
            for (CallStackItem callStackItem : callStack) {
                msg += " << " + StringUtils.strip(callStackItem.toString()).replaceAll("\\s+", " ");
            }
        }
        printWriter.print(msg + "\n");
    }

        public boolean isActive() {
        return  enabled &&
                (mustBeLogged
                || mustBreak
                || (interruptToRequest != null)
                || (interruptToWithdraw != null)
                || !newCpuStateFlags.hasAllRegistersZero()
                || mustStartLogging
                || mustStopLogging
                );
    }

    /**
     * This method makes sure loading prefs from an old file (without enabled field)
     * initializes that field to true
     */
    private Object readResolve() {
        if (enabled == null) enabled = true;
        if (pcToSet != null && newCpuStateFlags == null) {
            getNewCpuStateFlags().pc=1;
            getNewCpuStateValues().setPc(pcToSet);
        }
        return this;
    }
}

