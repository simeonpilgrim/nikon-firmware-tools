package com.nikonhacker.emu.memory.listener;

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
    public Byte onLoadData8(byte[] pageData, int address, byte value) {
        //TODO
        return null;
    }

    @Override
    public void onLoadInstruction8(byte[] pageData, int address, byte value) {
        //TODO
    }

    @Override
    public void onStore8(byte[] pageData, int address, byte value) {
        //TODO
    }
}
