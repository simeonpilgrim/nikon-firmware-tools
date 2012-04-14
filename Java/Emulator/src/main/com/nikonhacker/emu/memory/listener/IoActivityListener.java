package com.nikonhacker.emu.memory.listener;

public interface IoActivityListener {

    void onIoLoad8(byte[] ioPage, int offset, byte value);

    void onIoStore8(byte[] ioPage, int offset, byte value);
}
