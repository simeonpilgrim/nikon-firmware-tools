package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.emu.memory.DebuggableMemory;

public abstract class Abstract8BitMemoryActivityListener implements MemoryActivityListener {

    public abstract Byte onLoadData8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource);

    /**
     * Default implementation calls onLoadData8
     * @param pageData
     * @param address
     * @param value
     * @param accessSource
     */
    public Integer onLoadData16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        onLoadData8(pageData, address, (byte) (value >>> 8), accessSource);
        onLoadData8(pageData, address + 1, (byte) (value & 0xFF), accessSource);
        return null;
    }

    /**
     * Default implementation calls onLoadData8
     * @param pageData
     * @param address
     * @param value
     * @param accessSource
     */
    public Integer onLoadData32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        onLoadData8(pageData, address, (byte) (value >>> 24), accessSource);
        onLoadData8(pageData, address + 1, (byte) ((value >>> 16) & 0xFF), accessSource);
        onLoadData8(pageData, address + 2, (byte) ((value >>> 8) & 0xFF), accessSource);
        onLoadData8(pageData, address + 3, (byte) (value & 0xFF), accessSource);
        return null;
    }


    public abstract void onLoadInstruction8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource);

    /**
     * Default implementation calls onLoadInstruction8
     * @param pageData
     * @param address
     * @param value
     * @param accessSource
     */
    public void onLoadInstruction16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        onLoadInstruction8(pageData, address, (byte) (value >>> 8), accessSource);
        onLoadInstruction8(pageData, address + 1, (byte) (value & 0xFF), accessSource);
    }

    /**
     * Default implementation calls onLoadInstruction8
     * @param pageData
     * @param address
     * @param value
     * @param accessSource
     */
    public void onLoadInstruction32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        onLoadInstruction8(pageData, address, (byte) (value >>> 24), accessSource);
        onLoadInstruction8(pageData, address + 1, (byte) ((value >>> 16) & 0xFF), accessSource);
        onLoadInstruction8(pageData, address + 2, (byte) ((value >>> 8) & 0xFF), accessSource);
        onLoadInstruction8(pageData, address + 3, (byte) (value & 0xFF), accessSource);
    }


    public abstract void onStore8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource);

    /**
     * Default implementation calls onStore8
     * @param pageData
     * @param address
     * @param value
     * @param accessSource
     */
    public void onStore16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        onStore8(pageData, address, (byte) (value >>> 8), accessSource);
        onStore8(pageData, address + 1, (byte) (value & 0xFF), accessSource);
    }

    /**
     * Default implementation calls onStore8
     * @param pageData
     * @param address
     * @param value
     * @param accessSource
     */
    public void onStore32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        onStore8(pageData, address, (byte) (value >>> 24), accessSource);
        onStore8(pageData, address + 1, (byte) ((value >>> 16) & 0xFF), accessSource);
        onStore8(pageData, address + 2, (byte) ((value >>> 8) & 0xFF), accessSource);
        onStore8(pageData, address + 3, (byte) (value & 0xFF), accessSource);
    }

}
