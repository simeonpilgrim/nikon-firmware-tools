package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.MasterClock;
import com.nikonhacker.emu.memory.DebuggableMemory;

import java.io.PrintWriter;
import java.util.Set;

public abstract class AbstractAccessLoggerActivityListener implements MemoryActivityListener {
    private PrintWriter                        printWriter;
    private CPUState                           cpuState;
    private Set<DebuggableMemory.AccessSource> selectedAccessSources;
    private MasterClock                        masterClock;

    public AbstractAccessLoggerActivityListener(PrintWriter printWriter, CPUState cpuState, Set<DebuggableMemory.AccessSource> selectedAccessSources, MasterClock masterClock) {
        this.printWriter = printWriter;
        this.cpuState = cpuState;
        this.selectedAccessSources = selectedAccessSources;
        this.masterClock = masterClock;
    }


    public Byte onLoadData8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        if (selectedAccessSources.contains(accessSource)) {
            String msg = "            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFF, 2) + "        ";
            switch (accessSource) {
                case CODE:
                    msg += "(@0x" + Format.asHex(cpuState.pc, 8) + ")";
                    break;
                case DMA:
                    msg += "(DMA ctrlr)";
                    break;
                case IMGA:
                    msg += "(IMGA ctrlr)";
                    break;
            }
            if (masterClock != null) {
                msg = masterClock.getFormatedTotalElapsedTimeMs() + " " + msg;
            }
            printWriter.println(msg);
        }
        return null;
    }

    public Integer onLoadData16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        if (selectedAccessSources.contains(accessSource)) {
            String msg = "            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFFFF, 4) + "      ";
            switch (accessSource) {
                case CODE:
                    msg += "(@0x" + Format.asHex(cpuState.pc, 8) + ")";
                    break;
                case DMA:
                    msg += "(DMA ctrlr)";
                    break;
                case IMGA:
                    msg += "(IMGA ctrlr)";
                    break;
            }
            if (masterClock != null) {
                msg = masterClock.getFormatedTotalElapsedTimeMs() + " " + msg;
            }
            printWriter.println(msg);
        }
        return null;
    }

    public Integer onLoadData32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        if (selectedAccessSources.contains(accessSource)) {
            String msg = "            read from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value, 8) + "  ";
            switch (accessSource) {
                case CODE:
                    msg += "(@0x" + Format.asHex(cpuState.pc, 8) + ")";
                    break;
                case DMA:
                    msg += "(DMA ctrlr)";
                    break;
                case IMGA:
                    msg += "(IMGA ctrlr)";
                    break;
            }
            if (masterClock != null) {
                msg = masterClock.getFormatedTotalElapsedTimeMs() + " " + msg;
            }
            printWriter.println(msg);
        }
        return null;
    }


    public void onLoadInstruction8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        if (selectedAccessSources.contains(accessSource)) {
            String msg = "   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFF, 2) + "        ";
            switch (accessSource) {
                case CODE:
                    msg += "(@0x" + Format.asHex(cpuState.pc, 8) + ")";
                    break;
                case DMA:
                    msg += "(DMA ctrlr)";
                    break;
                case IMGA:
                    msg += "(IMGA ctrlr)";
                    break;
            }
            if (masterClock != null) {
                msg = masterClock.getFormatedTotalElapsedTimeMs() + " " + msg;
            }
            printWriter.println(msg);
        }
    }

    public void onLoadInstruction16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        if (selectedAccessSources.contains(accessSource)) {
            String msg = "   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value & 0xFFFF, 4) + "      ";
            switch (accessSource) {
                case CODE:
                    msg += "(@0x" + Format.asHex(cpuState.pc, 8) + ")";
                    break;
                case DMA:
                    msg += "(DMA ctrlr)";
                    break;
                case IMGA:
                    msg += "(IMGA ctrlr)";
                    break;
            }
            if (masterClock != null) {
                msg = masterClock.getFormatedTotalElapsedTimeMs() + " " + msg;
            }
            printWriter.println(msg);
        }
    }

    public void onLoadInstruction32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        if (selectedAccessSources.contains(accessSource)) {
            String msg = "   CODE EXECUTED from 0x" + Format.asHex(address, 8) + " : 0x" + Format.asHex(value, 8) + "  ";
            switch (accessSource) {
                case CODE:
                    msg += "(@0x" + Format.asHex(cpuState.pc, 8) + ")";
                    break;
                case DMA:
                    msg += "(DMA ctrlr)";
                    break;
                case IMGA:
                    msg += "(IMGA ctrlr)";
                    break;
            }
            if (masterClock != null) {
                msg = masterClock.getFormatedTotalElapsedTimeMs() + " " + msg;
            }
            printWriter.println(msg);
        }
    }


    public void onStore8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        if (selectedAccessSources.contains(accessSource)) {
            String msg = "0x" + Format.asHex(value & 0xFF, 2) + "       written to 0x" + Format.asHex(address, 8) + "               ";
            switch (accessSource) {
                case CODE:
                    msg += "(@0x" + Format.asHex(cpuState.pc, 8) + ")";
                    break;
                case DMA:
                    msg += "(DMA ctrlr)";
                    break;
                case IMGA:
                    msg += "(IMGA ctrlr)";
                    break;
            }
            if (masterClock != null) {
                msg = masterClock.getFormatedTotalElapsedTimeMs() + " " + msg;
            }
            printWriter.println(msg);
        }
    }

    public void onStore16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        if (selectedAccessSources.contains(accessSource)) {
            String msg = "0x" + Format.asHex(value & 0xFFFF, 4) + "     written to 0x" + Format.asHex(address, 8) + "               ";
            switch (accessSource) {
                case CODE:
                    msg += "(@0x" + Format.asHex(cpuState.pc, 8) + ")";
                    break;
                case DMA:
                    msg += "(DMA ctrlr)";
                    break;
                case IMGA:
                    msg += "(IMGA ctrlr)";
                    break;
            }
            if (masterClock != null) {
                msg = masterClock.getFormatedTotalElapsedTimeMs() + " " + msg;
            }
            printWriter.println(msg);
        }
    }

    public void onStore32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        if (selectedAccessSources.contains(accessSource)) {
            String msg = "0x" + Format.asHex(value, 8) + " written to 0x" + Format.asHex(address, 8) + "               ";
            switch (accessSource) {
                case CODE:
                    msg += "(@0x" + Format.asHex(cpuState.pc, 8) + ")";
                    break;
                case DMA:
                    msg += "(DMA ctrlr)";
                    break;
                case IMGA:
                    msg += "(IMGA ctrlr)";
                    break;
            }
            if (masterClock != null) {
                msg = masterClock.getFormatedTotalElapsedTimeMs() + " " + msg;
            }
            printWriter.println(msg);
        }
    }
}
