package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

/**
 * This is an active object which basically says if a condition (or a set thereof) matches
 * BreakTriggers are converted to a set of BreakConditions upon Emulator start
 * @see BreakTrigger
 */
public interface BreakCondition {
    /**
     * @return the breaktrigger to which this condition belongs
     */
    BreakTrigger getBreakTrigger();

    boolean matches(FrCPUState cpuState, Memory memory);
}
