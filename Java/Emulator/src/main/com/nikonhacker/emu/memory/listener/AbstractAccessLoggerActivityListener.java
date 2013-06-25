package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;

import java.io.PrintWriter;

public abstract class AbstractAccessLoggerActivityListener implements MemoryActivityListener {
    private PrintWriter printWriter;
    private CPUState cpuState;

    public AbstractAccessLoggerActivityListener(PrintWriter printWriter, CPUState cpuState) {
        this.printWriter = printWriter;
        this.cpuState = cpuState;
    }


    public Byte onLoadData8(byte[] pageData, int address, byte value) {
        printWriter.println("            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFF, 2) + "        (@0x" + Format.asHex(cpuState.pc, 8) + ")");
        return null;
    }

    public Integer onLoadData16(byte[] pageData, int address, int value) {
        printWriter.println("            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFFFF, 4) + "      (@0x" + Format.asHex(cpuState.pc, 8) + ")");
        return null;
    }

    public Integer onLoadData32(byte[] pageData, int address, int value) {
        printWriter.println("            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value, 8) + "  (@0x" + Format.asHex(cpuState.pc, 8) + ")");
        return null;
    }


    public void onLoadInstruction8(byte[] pageData, int address, byte value) {
        printWriter.println("   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFF, 2) + "        (@0x" + Format.asHex(cpuState.pc, 8) + ")");
    }

    public void onLoadInstruction16(byte[] pageData, int address, int value) {
        printWriter.println("   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFFFF, 4) + "      (@0x" + Format.asHex(cpuState.pc, 8) + ")");
    }

    public void onLoadInstruction32(byte[] pageData, int address, int value) {
        printWriter.println("   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value, 8) + "  (@0x" + Format.asHex(cpuState.pc, 8) + ")");
    }


    public void onStore8(byte[] pageData, int address, byte value) {
        printWriter.println("0x" + Format.asHex(value & 0xFF, 2) + "       written to 0x" + Format.asHex(address, 8) + "               (@0x" + Format.asHex(cpuState.pc, 8) + ")");
    }

    public void onStore16(byte[] pageData, int address, int value) {
        printWriter.println("0x" + Format.asHex(value & 0xFFFF, 4) + "     written to 0x" + Format.asHex(address, 8) + "               (@0x" + Format.asHex(cpuState.pc, 8) + ")");
    }

    public void onStore32(byte[] pageData, int address, int value) {
        printWriter.println("0x" + Format.asHex(value, 8) + " written to 0x" + Format.asHex(address, 8) + "               (@0x" + Format.asHex(cpuState.pc, 8) + ")");
    }
}
