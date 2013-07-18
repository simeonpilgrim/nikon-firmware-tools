package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.MasterClock;
import com.nikonhacker.emu.memory.DebuggableMemory;

import java.io.PrintWriter;
import java.util.EnumSet;

public class PageAccessLoggerActivityListener extends AbstractAccessLoggerActivityListener implements MemoryActivityListener {
    private int targetPage;

    public PageAccessLoggerActivityListener(PrintWriter printWriter, int targetPage, CPUState cpuState, EnumSet<DebuggableMemory.AccessSource> selectedAccessSources, MasterClock masterClock) {
        super(printWriter, cpuState, selectedAccessSources, masterClock);
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
