package com.nikonhacker.emu.memory.listener;

public interface MemoryActivityListener {

    void onLoadData8(int address, byte value);

    void onLoadData16(int address, int value);

    void onLoadData32(int address, int value);


    void onLoadInstruction8(int address, byte value);

    void onLoadInstruction16(int address, int value);

    void onLoadInstruction32(int address, int value);


    void onStore8(int address, byte value);

    void onStore16(int address, int value);

    void onStore32(int address, int value);

}
