package com.nikonhacker.emu.memory.listener;

public abstract class Abstract8BitMemoryActivityListener implements MemoryActivityListener {

    public abstract Byte onLoadData8(byte[] pageData, int address, byte value);

    /**
     * Default implementation calls onLoadData8
     * @param pageData
     * @param address
     * @param value
     */
    public Integer onLoadData16(byte[] pageData, int address, int value) {
        onLoadData8(pageData, address, (byte) (value >>> 8));
        onLoadData8(pageData, address + 1, (byte) (value & 0xFF));
        return null;
    }

    /**
     * Default implementation calls onLoadData8
     * @param pageData
     * @param address
     * @param value
     */
    public Integer onLoadData32(byte[] pageData, int address, int value) {
        onLoadData8(pageData, address, (byte) (value >>> 24));
        onLoadData8(pageData, address + 1, (byte) ((value >>> 16) & 0xFF));
        onLoadData8(pageData, address + 2, (byte) ((value >>> 8) & 0xFF));
        onLoadData8(pageData, address + 3, (byte) (value & 0xFF));
        return null;
    }


    public abstract void onLoadInstruction8(byte[] pageData, int address, byte value);

    /**
     * Default implementation calls onLoadInstruction8
     * @param pageData
     * @param address
     * @param value
     */
    public void onLoadInstruction16(byte[] pageData, int address, int value) {
        onLoadInstruction8(pageData, address, (byte) (value >>> 8));
        onLoadInstruction8(pageData, address + 1, (byte) (value & 0xFF));
    }

    /**
     * Default implementation calls onLoadInstruction8
     * @param pageData
     * @param address
     * @param value
     */
    public void onLoadInstruction32(byte[] pageData, int address, int value) {
        onLoadInstruction8(pageData, address, (byte) (value >>> 24));
        onLoadInstruction8(pageData, address + 1, (byte) ((value >>> 16) & 0xFF));
        onLoadInstruction8(pageData, address + 2, (byte) ((value >>> 8) & 0xFF));
        onLoadInstruction8(pageData, address + 3, (byte) (value & 0xFF));
    }


    public abstract void onStore8(byte[] pageData, int address, byte value);

    /**
     * Default implementation calls onStore8
     * @param pageData
     * @param address
     * @param value
     */
    public void onStore16(byte[] pageData, int address, int value) {
        onStore8(pageData, address, (byte) (value >>> 8));
        onStore8(pageData, address + 1, (byte) (value & 0xFF));
    }

    /**
     * Default implementation calls onStore8
     * @param pageData
     * @param address
     * @param value
     */
    public void onStore32(byte[] pageData, int address, int value) {
        onStore8(pageData, address, (byte) (value >>> 24));
        onStore8(pageData, address + 1, (byte) ((value >>> 16) & 0xFF));
        onStore8(pageData, address + 2, (byte) ((value >>> 8) & 0xFF));
        onStore8(pageData, address + 3, (byte) (value & 0xFF));
    }

}
