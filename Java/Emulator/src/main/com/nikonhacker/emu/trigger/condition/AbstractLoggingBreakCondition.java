package com.nikonhacker.emu.trigger.condition;

import com.nikonhacker.Format;
import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.CallStackItem;
import com.nikonhacker.emu.trigger.BreakTrigger;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.util.Deque;

public abstract class AbstractLoggingBreakCondition implements BreakCondition {
    private BreakTrigger breakTrigger;

    public AbstractLoggingBreakCondition(BreakTrigger breakTrigger) {
        this.breakTrigger = breakTrigger;
    }

    public void log(PrintWriter printWriter, CPUState cpuState, Deque<CallStackItem> callStack) {
        String msg = getBreakTrigger().getName() + " triggered at 0x" + Format.asHex(cpuState.pc, 8);
        if (callStack != null && callStack.size() > 1) {
            for (CallStackItem callStackItem : callStack) {
                msg += " << " + StringUtils.strip(callStackItem.toString()).replaceAll("\\s+", " ");
            }
        }
        printWriter.print(msg + "\n");
    }

    public BreakTrigger getBreakTrigger() {
        return breakTrigger;
    }
}
