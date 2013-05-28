package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.emu.memory.listener.IoActivityListener;

public class Expeed4006IoListener extends IoActivityListener {

    public static final int BASE_ADDRESS = 0x4006_0000;
    public static final int ADDRESS_MASK = 0xFFFF_F000;

    public Expeed4006IoListener() {
    }

    @Override
    public boolean matches(int address) {
        return (address & ADDRESS_MASK) == BASE_ADDRESS;
    }


    @Override
    public Byte onLoadData8(byte[] pageData, int address, byte value) {
        return null;
    }

    @Override
    public Integer onLoadData16(byte[] pageData, int address, int value) {
        // return fake acknowledge at register 0x40060010
        if (address == 0x40060010) return 0x1000;
        // otherwise, ignore
        return null;
    }

    @Override
    public Integer onLoadData32(byte[] pageData, int address, int value) {
        return null;
    }


    @Override
    public void onStore8(byte[] pageData, int address, byte value) {
    }

    @Override
    public void onStore16(byte[] pageData, int address, int value) {
        //System.err.println("Storing 0x" + Format.asHex(value, 4) + " to unknown component at 0x" + Format.asHex(address, 8));
    }

    @Override
    public void onStore32(byte[] pageData, int address, int value) {
    }
}
