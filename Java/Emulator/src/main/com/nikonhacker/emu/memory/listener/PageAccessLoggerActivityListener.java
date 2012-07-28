package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.disassembly.fr.CPUState;

import java.io.PrintWriter;

public class PageAccessLoggerActivityListener extends AbstractAccessLoggerActivityListener implements MemoryActivityListener {
    private int targetPage;

    public PageAccessLoggerActivityListener(PrintWriter printWriter, int targetPage, CPUState cpuState) {
        super(printWriter, cpuState);
        this.targetPage = targetPage;
    }

    @Override
    protected boolean matches(int address) {
        return address >>> 16 == targetPage;
    }
}
