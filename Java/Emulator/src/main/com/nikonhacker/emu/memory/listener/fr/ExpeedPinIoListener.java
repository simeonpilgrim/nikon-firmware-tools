package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.ioPort.fr.FrIoPort;

public class ExpeedPinIoListener extends IoActivityListener {

    public static final int PORT_VALUE_ADDRESS   = 0x5000_0100;
    public static final int PORT_CONFIG_ADDRESS  = 0x5000_0200;

    public static final int PORT_BASE_ADDRESS    = 0x5000_0000;
    public static final int PORT_ADDRESS_MASK    = 0xFFFF_FC00;

    /* Range of I/O Ports. D5100 specific */
    public static final int NUM_PORT          = 14;

    public ExpeedPinIoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);
    }

    @Override
    public final boolean matches(int address) {
        return ((address & PORT_ADDRESS_MASK) == PORT_BASE_ADDRESS );
    }

    @Override
    public Byte onLoadData8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        final int portNumber = address & 0xFF;
        if (portNumber<NUM_PORT) {
            final FrIoPort port = (FrIoPort)(platform.getIoPorts()[portNumber]);
            switch (address& 0xFFFFFF00) {
                case PORT_VALUE_ADDRESS:
                    return port.getValue();
                case PORT_CONFIG_ADDRESS:
                    return port.getFunctionRegister();
            }
        }
        return null;
    }

    @Override
    public Integer onLoadData16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        switch (address& 0xFFFFFF00) {
            case PORT_VALUE_ADDRESS:
            case PORT_CONFIG_ADDRESS:
                warn("Loading 16b data from Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
        }
        return null;
    }

    @Override
    public Integer onLoadData32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        switch (address& 0xFFFFFF00) {
            case PORT_VALUE_ADDRESS:
            case PORT_CONFIG_ADDRESS:
                warn("Loading 32b data from Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
        }
        return null;
    }


    @Override
    public void onStore8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        final int portNumber = address & 0xFF;
        if (portNumber<NUM_PORT) {
            final FrIoPort port = (FrIoPort)(platform.getIoPorts()[portNumber]);
            switch (address& 0xFFFFFF00) {
                case PORT_VALUE_ADDRESS: port.setValue(value); break;
                case PORT_CONFIG_ADDRESS: port.setFunctionRegister(value); break;
            }

        }
    }

    @Override
    public void onStore16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        switch (address& 0xFFFFFF00) {
            case PORT_VALUE_ADDRESS:
            case PORT_CONFIG_ADDRESS:
                warn("Storing 16b data to Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
        }
    }

    @Override
    public void onStore32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        switch (address& 0xFFFFFF00) {
            case PORT_VALUE_ADDRESS:
            case PORT_CONFIG_ADDRESS:
                warn("Storing 32b data to Pin Port register 0x" + Format.asHex(address, 8) + " is not implemented");
        }
    }
}
