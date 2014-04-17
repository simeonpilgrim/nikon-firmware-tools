package com.nikonhacker.disassembly.arm;

import com.nikonhacker.disassembly.*;
import com.nikonhacker.emu.memory.Memory;

import java.io.PrintWriter;
import java.util.*;

public class ArmCodeAnalyzer extends CodeAnalyzer {

    public ArmCodeAnalyzer(CodeStructure codeStructure, SortedSet<Range> ranges, Memory memory, Map<Integer, Symbol> symbols, Map<Integer, List<Integer>> jumpHints, Set<OutputOption> outputOptions, PrintWriter debugPrintWriter) {
        super(codeStructure, ranges, memory, symbols, jumpHints, outputOptions, debugPrintWriter);
    }

    protected final int[] getJmpTableAddressSize(int address) {
        return null;
    }
}