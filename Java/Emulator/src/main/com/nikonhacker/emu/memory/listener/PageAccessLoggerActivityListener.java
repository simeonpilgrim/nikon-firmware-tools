package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.disassembly.CPUState;

import java.io.PrintWriter;

public class PageAccessLoggerActivityListener extends AbstractAccessLoggerActivityListener implements MemoryActivityListener {
    private int targetPage;

    public PageAccessLoggerActivityListener(PrintWriter printWriter, int targetPage, CPUState cpuState) {
        super(printWriter, cpuState);
        this.targetPage = targetPage;
    }

    @Override
    public boolean matches(int address) {
        return address >>> 16 == targetPage;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
