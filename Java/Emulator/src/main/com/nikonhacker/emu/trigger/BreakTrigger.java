package com.nikonhacker.emu.trigger;
import com.nikonhacker.dfr.CPUState;

import java.util.ArrayList;
import java.util.List;

public class BreakTrigger {
    private String name;
    private boolean enabled = true;
    private CPUState cpuStateValues;
    private CPUState cpuStateFlags;

    public BreakTrigger(String name, CPUState cpuStateValues, CPUState cpuStateFlags) {
        this.cpuStateValues = cpuStateValues;
        this.cpuStateFlags = cpuStateFlags;

        this.name = name;
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

    public void setCpuStateValues(CPUState cpuStateValues) {
        this.cpuStateValues = cpuStateValues;
    }

    public CPUState getCpuStateFlags() {
        return cpuStateFlags;
    }

    public void setCpuStateFlags(CPUState cpuStateFlags) {
        this.cpuStateFlags = cpuStateFlags;
    }

    public List<BreakCondition> getBreakConditions() {
        List<BreakCondition> conditions = new ArrayList<BreakCondition>();
        if (cpuStateFlags.pc != 0) {
            conditions.add(new BreakPointCondition(cpuStateValues.pc, this));
        }
        for (int i = 0; i <= CPUState.MDL; i++) {
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
        return conditions;
    }

    @Override
    public String toString() {
        return name + (isEnabled()?"":" (disabled)");
    }


}

