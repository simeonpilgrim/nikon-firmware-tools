package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

import java.util.ArrayList;
import java.util.List;

public class AndCondition implements BreakCondition {
    List<BreakCondition> conditions = new ArrayList<BreakCondition>();
    private BreakTrigger breakTrigger;

    public AndCondition(List<BreakCondition> conditions, BreakTrigger breakTrigger) {
        this.conditions = conditions;
        this.breakTrigger = breakTrigger;
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

    public BreakTrigger getBreakTrigger() {
        return breakTrigger;
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
