package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.listener.IoActivityListener;

public class ExpeedPinIoListener extends IoActivityListener {

    // Interrupt Controller
    public static final int PORT_BASE_ADDRESS = 0x5000_0100;

    private byte portValues[] = new byte[16];

    private Platform platform;


    public ExpeedPinIoListener(Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean matches(int address) {
        return address >>> 4 == 0x5000_010;
    }

    @Override
    public Byte onLoadData8(byte[] pageData, int address, byte value) {
        int portNumber = address - PORT_BASE_ADDRESS;
        //TODO: just return platform.getIoPorts()[portNumber].getInternalValue();
        return portValues[portNumber];
    }

    @Override
    public Integer onLoadData16(byte[] pageData, int address, int value) {
        return null;
    }

    @Override
    public Integer onLoadData32(byte[] pageData, int address, int value) {
        return null;
    }


    @Override
    public void onStore8(byte[] pageData, int address, byte value) {
        int portNumber = address - PORT_BASE_ADDRESS;
        //TODO: don't process here, just call platform.getIoPorts()[portNumber].setInternalValue(value);
        //TODO: the rest of the code should be moved there
        int compare = value ^ portValues[portNumber];
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            if (Format.isBitSet(compare, bitNumber)) {
                // State of this bit changed
                if (Format.isBitSet(value, bitNumber)) {
                    // State is now 1: rising edge
                    // TODO: warn listeners that this pin is rising
                }
                else {
                    // State is now 0: falling edge
                    // TODO: warn listeners that this pin is falling
                }
            }
        }
        portValues[portNumber] = value;
    }

    @Override
    public void onStore16(byte[] pageData, int address, int value) {
        System.err.println("Storing 16b data to Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
    }

    @Override
    public void onStore32(byte[] pageData, int address, int value) {
        System.err.println("Storing 32b data to Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
    }
}
