package com.nikonhacker.emu.trigger;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;

import java.util.ArrayList;
import java.util.List;

public class AndCondition implements BreakCondition {
    List<BreakCondition> conditions = new ArrayList<BreakCondition>();
    public AndCondition() {
    }

    public AndCondition(List<BreakCondition> conditions) {
        this.conditions = conditions;
    }

    public List<BreakCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<BreakCondition> conditions) {
        this.conditions = conditions;
    }

    public void addBreakCondition(BreakCondition condition) {
        conditions.add(condition);
    }

    public boolean matches(CPUState cpuState, Memory memory) {
        for (BreakCondition condition : conditions) {
            if (!condition.matches(cpuState, memory)) {
                return false;
            }
        }
        return true;
    }
}