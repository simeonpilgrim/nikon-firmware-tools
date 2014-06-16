package com.nikonhacker.itron;

import com.nikonhacker.disassembly.Function;

public class ReturnStackEntry {
    public final int      returnAddress;
    public final int      stackAddress;
    public final Function function;

    public ReturnStackEntry(final int pc, final int sp, final Function func) {
        returnAddress = pc;
        stackAddress = sp;
        function = func;
    }
}
