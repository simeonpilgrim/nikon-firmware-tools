package com.nikonhacker.emu.memory.listener;

/**
 * An IO Activity Listener is hooked to a page of memory addresses meant to contain IO registers
 */
public abstract class IoActivityListener implements MemoryActivityListener {
    @Override
    public boolean isLoggerOnly() {
        return false;
    }

    @Override
    public void onLoadInstruction8(byte[] pageData, int address, byte value) {
    }

    @Override
    public void onLoadInstruction16(byte[] pageData, int address, int value) {
    }

    @Override
    public void onLoadInstruction32(byte[] pageData, int address, int value) {
    }
}
