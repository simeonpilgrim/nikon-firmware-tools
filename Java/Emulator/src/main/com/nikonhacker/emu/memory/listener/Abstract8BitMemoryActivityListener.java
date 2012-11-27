package com.nikonhacker.emu.memory.listener;

public abstract class Abstract8BitMemoryActivityListener implements MemoryActivityListener {
    public abstract void onLoadData8(int address, byte value);

    /**
     * Default implementation calls onLoadData8
     * @param address
     * @param value
     */
    public void onLoadData16(int address, int value) {
        onLoadData8(address, (byte) (value >>> 8));
        onLoadData8(address + 1, (byte) (value & 0xFF));
    }

    /**
     * Default implementation calls onLoadData8
     * @param address
     * @param value
     */
    public void onLoadData32(int address, int value) {
        onLoadData8(address, (byte) (value >>> 24));
        onLoadData8(address + 1, (byte) ((value >>> 16) & 0xFF));
        onLoadData8(address + 2, (byte) ((value >>> 8) & 0xFF));
        onLoadData8(address + 3, (byte) (value & 0xFF));        
    }

    public abstract void onLoadInstruction8(int address, byte value);

    /**
     * Default implementation calls onLoadInstruction8
     * @param address
     * @param value
     */
    public void onLoadInstruction16(int address, int value) {
        onLoadInstruction8(address, (byte) (value >>> 8));
        onLoadInstruction8(address + 1, (byte) (value & 0xFF));
    }

    /**
     * Default implementation calls onLoadInstruction8
     * @param address
     * @param value
     */
    public void onLoadInstruction32(int address, int value) {
        onLoadInstruction8(address, (byte) (value >>> 24));
        onLoadInstruction8(address + 1, (byte) ((value >>> 16) & 0xFF));
        onLoadInstruction8(address + 2, (byte) ((value >>> 8) & 0xFF));
        onLoadInstruction8(address + 3, (byte) (value & 0xFF));
    }

    public abstract void onStore8(int address, byte value);

    /**
     * Default implementation calls onStore8
     * @param address
     * @param value
     */
    public void onStore16(int address, int value) {
        onStore8(address, (byte) (value >>> 8));
        onStore8(address + 1, (byte) (value & 0xFF));
    }

    /**
     * Default implementation calls onStore8
     * @param address
     * @param value
     */
    public void onStore32(int address, int value) {
        onStore8(address, (byte) (value >>> 24));
        onStore8(address + 1, (byte) ((value >>> 16) & 0xFF));
        onStore8(address + 2, (byte) ((value >>> 8) & 0xFF));
        onStore8(address + 3, (byte) (value & 0xFF));
    }

}
