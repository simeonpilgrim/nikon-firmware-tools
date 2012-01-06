package com.nikonhacker.emu.memory.listener;

public interface MemoryActivityListener {

    void onLoadData(int page, int offset, byte value);

    void onLoadInstruction(int page, int offset, byte value);

    void onStore(int page, int offset, byte value);

}
