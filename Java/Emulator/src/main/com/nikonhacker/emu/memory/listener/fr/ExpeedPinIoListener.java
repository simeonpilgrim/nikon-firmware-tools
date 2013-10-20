package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;

public class ExpeedPinIoListener extends IoActivityListener {

    public static final int PORT_BASE_ADDRESS = 0x5000_0100;

    /* Range of I/O Ports. Let's assume 16 */
    public static final int NUM_PORT          = 16;
    public static final int PORT_ADDRESS_MASK = ~(NUM_PORT - 1);

    public ExpeedPinIoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);
    }

    @Override
    public boolean matches(int address) {
        return (address & PORT_ADDRESS_MASK) == PORT_BASE_ADDRESS;
    }


    @Override
    public Byte onLoadData8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        int portNumber = address - PORT_BASE_ADDRESS;
        return platform.getIoPorts()[portNumber].getValue();
    }

    @Override
    public Integer onLoadData16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        warn("Loading 16b data from Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
        return null;
    }

    @Override
    public Integer onLoadData32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        warn("Loading 32b data from Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
        return null;
    }


    @Override
    public void onStore8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        int portNumber = address - PORT_BASE_ADDRESS;
        platform.getIoPorts()[portNumber].setValue(value);
    }

    @Override
    public void onStore16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        warn("Storing 16b data to Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
    }

    @Override
    public void onStore32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        warn("Storing 32b data to Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
    }
}
