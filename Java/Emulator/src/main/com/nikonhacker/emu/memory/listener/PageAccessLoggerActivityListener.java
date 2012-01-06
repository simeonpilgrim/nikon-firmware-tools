package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.Format;

import java.io.PrintStream;

public class PageAccessLoggerActivityListener implements MemoryActivityListener {
    PrintStream printStream;
    private int page;

    public PageAccessLoggerActivityListener(PrintStream printStream, int page) {
        this.printStream = printStream;
        this.page = page;
    }

    public void onLoadData(int page, int offset, byte value) {
        if (page == this.page) {
            printStream.println("    read from 0x" + Format.asHex(page << 16 | offset & 0xFFFF, 8) + " : " + Format.asHex(value & 0xFF, 2));
        }
    }

    public void onLoadInstruction(int page, int offset, byte value) {
        if (page == this.page) {
            printStream.println("EXECUTED from 0x" + Format.asHex(page << 16 | offset & 0xFFFF, 8) + " : " + Format.asHex(value & 0xFF, 2) + " !!!");
        }
    }

    public void onStore(int page, int offset, byte value) {
        if (page == this.page) {
            printStream.println(Format.asHex(value & 0xFF, 2) + " written to 0x" + Format.asHex(page << 16 | offset & 0xFFFF, 8));
        }
    }
}
