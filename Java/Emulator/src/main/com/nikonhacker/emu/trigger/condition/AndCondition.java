package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

import java.util.ArrayList;
import java.util.List;

public class AndCondition extends AbstractLoggingBreakCondition implements BreakCondition {
    List<BreakCondition> conditions = new ArrayList<BreakCondition>();

    public AndCondition(List<BreakCondition> conditions, BreakTrigger breakTrigger) {
        super(breakTrigger);
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

    public boolean matches(FrCPUState cpuState, Memory memory) {
        for (BreakCondition condition : conditions) {
            if (!condition.matches(cpuState, memory)) {
                return false;
            }
        }
        return true;
    }
}
