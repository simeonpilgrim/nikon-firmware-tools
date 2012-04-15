package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.Format;

import java.io.PrintWriter;

public class PageAccessLoggerActivityListener implements MemoryActivityListener {
    PrintWriter printWriter;
    private int targetPage;

    public PageAccessLoggerActivityListener(PrintWriter printWriter, int targetPage) {
        this.printWriter = printWriter;
        this.targetPage = targetPage;
    }

    public void onLoadData8(int address, byte value) {
        if (address >>> 16 == targetPage) {
            printWriter.println("            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFF, 2));
        }
    }

    public void onLoadData16(int address, int value) {
        if (address >>> 16 == targetPage) {
            printWriter.println("            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFFFF, 4));
        }
    }

    public void onLoadData32(int address, int value) {
        if (address >>> 16 == targetPage) {
            printWriter.println("            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value, 8));
        }
    }

    public void onLoadInstruction8(int address, byte value) {
        if (address >>> 16 == targetPage) {
            printWriter.println("   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFF, 2));
        }
    }

    public void onLoadInstruction16(int address, int value) {
        if (address >>> 16 == targetPage) {
            printWriter.println("   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFFFF, 4));
        }
    }

    public void onLoadInstruction32(int address, int value) {
        if (address >>> 16 == targetPage) {
            printWriter.println("   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value, 8));
        }
    }

    public void onStore8(int address, byte value) {
        if (address >>> 16 == targetPage) {
            printWriter.println("0x" + Format.asHex(value & 0xFF, 2) + "       written to 0x" + Format.asHex(address, 8));
        }
    }

    public void onStore16(int address, int value) {
        if (address >>> 16 == targetPage) {
            printWriter.println("0x" + Format.asHex(value & 0xFFFF, 4) + "     written to 0x" + Format.asHex(address, 8));
        }
    }

    public void onStore32(int address, int value) {
        if (address >>> 16 == targetPage) {
            printWriter.println("0x" + Format.asHex(value, 8) + " written to 0x" + Format.asHex(address, 8));
        }
    }
}
