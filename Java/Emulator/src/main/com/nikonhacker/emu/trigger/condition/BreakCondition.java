package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.CallStackItem;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;

import java.io.PrintWriter;
import java.util.Deque;

public interface BreakCondition {
    BreakTrigger getBreakTrigger();

    boolean matches(CPUState cpuState, Memory memory);

    void log(PrintWriter printWriter, CPUState cpuState, Deque<CallStackItem> callStack);
}
