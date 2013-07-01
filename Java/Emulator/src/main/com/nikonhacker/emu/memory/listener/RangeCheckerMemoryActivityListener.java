package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.emu.memory.DebuggableMemory;

public class RangeCheckerMemoryActivityListener extends Abstract8BitMemoryActivityListener implements MemoryActivityListener{

    @Override
    public boolean matches(int address) {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Byte onLoadData8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        //TODO
        return null;
    }

    @Override
    public void onLoadInstruction8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        //TODO
    }

    @Override
    public void onStore8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        //TODO
    }
}
