package com.nikonhacker.emu;

import com.nikonhacker.BinaryArithmetics;

public class AddressRange {
    String name;
    int startAddress = 0x00000000;
    int endAddress   = 0xFFFFFFFF;

    public AddressRange(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(int startAddress) {
        this.startAddress = startAddress;
    }

    public int getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(int endAddress) {
        this.endAddress = endAddress;
    }

    public boolean includes(int address) {
        return !BinaryArithmetics.isLessThanUnsigned(address, startAddress)
                && !BinaryArithmetics.isGreaterThanUnsigned(address, endAddress);
    }
}
