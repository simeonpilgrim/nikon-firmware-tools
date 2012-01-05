package com.nikonhacker.emu.memory.listener;

public interface MemoryActivityListener {

    void onLoadData(int page, int offset);

    void onLoadInstruction(int page, int offset);

    void onStore(int page, int offset);

}
