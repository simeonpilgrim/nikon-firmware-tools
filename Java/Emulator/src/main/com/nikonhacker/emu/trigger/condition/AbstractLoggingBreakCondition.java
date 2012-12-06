package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.emu.trigger.BreakTrigger;

public abstract class AbstractLoggingBreakCondition implements BreakCondition {
    private BreakTrigger breakTrigger;

    /**
     * Construct a new BreakCondition
     * @param breakTrigger the breaktrigger to which this condition belongs
     */
    public AbstractLoggingBreakCondition(BreakTrigger breakTrigger) {
        this.breakTrigger = breakTrigger;
    }

    /**
     * @return the breaktrigger to which this condition belongs
     */
    public BreakTrigger getBreakTrigger() {
        return breakTrigger;
    }

}
