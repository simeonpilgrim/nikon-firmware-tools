package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.disassembly.CPUState;

import java.io.PrintWriter;

public class RangeAccessLoggerActivityListener extends AbstractAccessLoggerActivityListener implements MemoryActivityListener {

    private final int minAddress;
    private final int maxAddress;

    public RangeAccessLoggerActivityListener(PrintWriter printWriter, int minAddress, int maxAddress, CPUState cpuState) {
        super(printWriter, cpuState);
        this.minAddress = minAddress;
        this.maxAddress = maxAddress;
    }

    @Override
    public boolean matches(int address) {
        return !BinaryArithmetics.isLessThanUnsigned(address, minAddress)
            && !BinaryArithmetics.isGreaterThanUnsigned(address, maxAddress);
    }

    @Override
    public boolean isLoggerOnly() {
        return true;
    }
}
