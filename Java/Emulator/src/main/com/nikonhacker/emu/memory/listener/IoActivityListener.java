package com.nikonhacker.emu.memory.listener;

public interface IoActivityListener {

    Byte onIoLoad8(byte[] ioPage, int addr, byte value);

    Integer onIoLoad16(byte[] ioPage, int addr, int value);

    Integer onIoLoad32(byte[] ioPage, int addr, int value);

    void onIoStore8(byte[] ioPage, int addr, byte value);

    void onIoStore16(byte[] ioPage, int addr, int value);

    void onIoStore32(byte[] ioPage, int addr, int value);
}
