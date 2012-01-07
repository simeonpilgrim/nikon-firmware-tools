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

    public void onLoadData8(int address, byte value) {
        if (address >>> 16 == page) {
            printStream.println("            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFF, 2));
        }
    }

    public void onLoadData16(int address, int value) {
        if (address >>> 16 == page) {
            printStream.println("            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFFFF, 4));
        }
    }

    public void onLoadData32(int address, int value) {
        if (address >>> 16 == page) {
            printStream.println("            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value, 8));
        }
    }

    public void onLoadInstruction8(int address, byte value) {
        if (address >>> 16 == page) {
            printStream.println("   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFF, 2));
        }
    }

    public void onLoadInstruction16(int address, int value) {
        if (address >>> 16 == page) {
            printStream.println("   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFFFF, 4));
        }
    }

    public void onLoadInstruction32(int address, int value) {
        if (address >>> 16 == page) {
            printStream.println("   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value, 8));
        }
    }

    public void onStore8(int address, byte value) {
        if (address >>> 16 == page) {
            printStream.println("0x" + Format.asHex(value & 0xFF, 2) + "       written to 0x" + Format.asHex(address, 8));
        }
    }

    public void onStore16(int address, int value) {
        if (address >>> 16 == page) {
            printStream.println("0x" + Format.asHex(value & 0xFFFF, 4) + "     written to 0x" + Format.asHex(address, 8));
        }
    }

    public void onStore32(int address, int value) {
        if (address >>> 16 == page) {
            printStream.println("0x" + Format.asHex(value, 8) + " written to 0x" + Format.asHex(address, 8));
        }
    }
}
