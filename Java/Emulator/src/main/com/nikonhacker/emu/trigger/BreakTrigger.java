package com.nikonhacker.emu.trigger;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.disassembly.Function;
import com.nikonhacker.disassembly.Symbol;
import com.nikonhacker.disassembly.fr.*;
import com.nikonhacker.emu.CallStackItem;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.condition.*;
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
    private FrCPUState cpuStateValues;
    private FrCPUState cpuStateFlags;
    private List<MemoryValueBreakCondition> memoryValueBreakConditions;
    private boolean mustBeLogged = false;
    private boolean mustBreak = true;
    private Integer interruptToRequest = null;
    private Integer pcToSet = null;
    private Function function;

    public BreakTrigger(String name, FrCPUState cpuStateValues, FrCPUState cpuStateFlags, List<MemoryValueBreakCondition> memoryValueBreakConditions) {
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

    public FrCPUState getCpuStateValues() {
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

    public Integer getPcToSet() {
        return pcToSet;
    }

    public void setPcToSet(Integer pcToSet) {
        this.pcToSet = pcToSet;
    }

    public void setCpuStateValues(FrCPUState cpuStateValues) {
        this.cpuStateValues = cpuStateValues;
    }

    public FrCPUState getCpuStateFlags() {
        return cpuStateFlags;
    }

    public void setCpuStateFlags(FrCPUState cpuStateFlags) {
        this.cpuStateFlags = cpuStateFlags;
    }

    public List<MemoryValueBreakCondition> getMemoryValueBreakConditions() {
        if (memoryValueBreakConditions == null) {
            memoryValueBreakConditions = new ArrayList<MemoryValueBreakCondition>();
        }
        return memoryValueBreakConditions;
    }

    public List<BreakCondition> getBreakConditions(CodeStructure codeStructure, DebuggableMemory memory) {
        List<BreakCondition> conditions = new ArrayList<BreakCondition>();
        if (cpuStateFlags.pc != 0) {
            if (codeStructure != null && codeStructure.getFunctions().containsKey(cpuStateValues.pc)) {
                // this is a break on a function. Store it for later
                function = codeStructure.getFunctions().get(cpuStateValues.pc);

                // In case this is a syscall, we can replace it by the actual syscall name and params
                if (cpuStateFlags.getReg(12) != 0) {
                    try {
                        Map<Integer,Syscall> syscallMap = Syscall.getMap(memory);
                        int int40address = Syscall.getInt40address();
                        if (int40address == cpuStateValues.pc) {
                            // We're on a syscall. Use the given syscall
                            Syscall syscall = syscallMap.get(cpuStateValues.getReg(12));
                            if (syscall != null) {
                                function = codeStructure.getFunctions().get(syscall.getAddress());
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
        for (int i = 0; i <= FrCPUState.MDL; i++) {
            if (cpuStateFlags.getReg(i) != 0) {
                conditions.add(new RegisterEqualityBreakCondition(i, cpuStateValues.getReg(i), this));
            }
        }
        if (cpuStateFlags.getCCR() != 0) {
            conditions.add(new CCRBreakCondition(cpuStateValues.getCCR(), cpuStateFlags.getCCR(), this));
        }
        if (cpuStateFlags.getSCR() != 0) {
            conditions.add(new SCRBreakCondition(cpuStateValues.getSCR(), cpuStateFlags.getSCR(), this));
        }
        if (cpuStateFlags.getILM() != 0) {
            conditions.add(new ILMBreakCondition(cpuStateValues.getILM(), cpuStateFlags.getILM(), this));
        }

        conditions.addAll(memoryValueBreakConditions);

        return conditions;
    }

    @Override
    public String toString() {
        return name + (getMustBreak()?"break":"") + (getMustBeLogged()?" log":"");
    }

    /**
     * This is the default logging behaviour
     * @param printWriter printWriter to which the log must be output
     * @param cpuState optional cpu state at the time the condition matches
     * @param callStack optional call stack at the time the condition matches
     * @param memory
     */
    public void log(PrintWriter printWriter, FrCPUState cpuState, Deque<CallStackItem> callStack, Memory memory) {
        String msg;
        if (function != null) {
            // This is a function call. Parse its arguments and log them
            msg = function.getName() + "(";
            if (function.getParameterList() != null) {
                for (Symbol.Parameter parameter : function.getParameterList()) {
                    if (parameter.getInVariable() != null) {
                        String paramString = parameter.getInVariable() + "=";
                        int value = cpuState.getReg(parameter.getRegister());
                        if (parameter.getInVariable().startsWith("sz")) {
                            paramString+="\"";
                            // Dump as String
                            int character = memory.loadUnsigned8(value++);
                            while (character > 0) {
                                paramString += (char)character;
                                character = memory.loadUnsigned8(value++);
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
            msg = name + " triggered at 0x" + Format.asHex(cpuState.pc, 8);
        }

        if (callStack != null) {
            for (CallStackItem callStackItem : callStack) {
                msg += " << " + StringUtils.strip(callStackItem.toString()).replaceAll("\\s+", " ");
            }
        }
        printWriter.print(msg + "\n");
    }


}

