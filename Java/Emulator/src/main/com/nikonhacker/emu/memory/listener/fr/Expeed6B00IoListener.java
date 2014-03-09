package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrSharedInterruptCircuit;

public class Expeed6B00IoListener extends IoActivityListener {

    public static final int BASE_ADDRESS = 0x6B00_0000;
    public static final int ADDRESS_MASK = 0xFFFF_FF00;

    // shared interrupt circuit
    private static final int REGISTER_SHARED_INT_CONFIG_BEGIN = 0x6B000000;
    private static final int REGISTER_SHARED_INT_CONFIG_END   = 0x6B00003B;

    private static final int REGISTER_SHARED_INT_STATUS_BEGIN = 0x6B000080;
    private static final int REGISTER_SHARED_INT_STATUS_END   = 0x6B0000BB;

    public Expeed6B00IoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);
    }

    @Override
    public boolean matches(int address) {
        return (address & ADDRESS_MASK) == BASE_ADDRESS;
    }

    @Override
    public Byte onLoadData8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_SHARED_INT_CONFIG_BEGIN && addr <= REGISTER_SHARED_INT_CONFIG_END) {
            // shared interrupt circuit
            stop("Shared interrupt config registers cannot be accessed by 8-bit for now");
        } else if (addr >= REGISTER_SHARED_INT_STATUS_BEGIN && addr <= REGISTER_SHARED_INT_STATUS_END) {
            // shared interrupt circuit
            stop("Shared interrupt circuit registers cannot be accessed by 8-bit for now");
        }
        return null;
    }

    @Override
    public Integer onLoadData16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_SHARED_INT_CONFIG_BEGIN && addr <= REGISTER_SHARED_INT_CONFIG_END) {
            // shared interrupt circuit
            stop("Shared interrupt config registers cannot be accessed by 16-bit for now");
        } else if (addr >= REGISTER_SHARED_INT_STATUS_BEGIN && addr <= REGISTER_SHARED_INT_STATUS_END) {
            // shared interrupt circuit
            stop("Shared interrupt circuit registers cannot be accessed by 16-bit for now");
        }
        return null;
    }

    @Override
    public Integer onLoadData32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_SHARED_INT_CONFIG_BEGIN && addr <= REGISTER_SHARED_INT_CONFIG_END) {
            // shared interrupt circuit
            FrSharedInterruptCircuit sharedInterruptCircuit = (FrSharedInterruptCircuit)platform.getSharedInterruptCircuit();

            return sharedInterruptCircuit.getConfigReg((addr-REGISTER_SHARED_INT_CONFIG_BEGIN)>>2);
        } else if (addr >= REGISTER_SHARED_INT_STATUS_BEGIN && addr <= REGISTER_SHARED_INT_STATUS_END) {
            // shared interrupt circuit
            FrSharedInterruptCircuit sharedInterruptCircuit = (FrSharedInterruptCircuit)platform.getSharedInterruptCircuit();

            return sharedInterruptCircuit.getStatusReg((addr-REGISTER_SHARED_INT_STATUS_BEGIN)>>2);
        }
        return null;
    }


    @Override
    public void onStore8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
       if (addr >= REGISTER_SHARED_INT_CONFIG_BEGIN && addr <= REGISTER_SHARED_INT_CONFIG_END) {
           // shared interrupt circuit
            stop("Shared interrupt config registers cannot be written by 8-bit for now");
       } else if (addr >= REGISTER_SHARED_INT_STATUS_BEGIN && addr <= REGISTER_SHARED_INT_STATUS_END) {
           // shared interrupt circuit
            
           // ignore write
       }
    }

    @Override
    public void onStore16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
       if (addr >= REGISTER_SHARED_INT_CONFIG_BEGIN && addr <= REGISTER_SHARED_INT_CONFIG_END) {
           // shared interrupt circuit
            stop("Shared interrupt config registers cannot be written by 16-bit for now");
       } else if (addr >= REGISTER_SHARED_INT_STATUS_BEGIN && addr <= REGISTER_SHARED_INT_STATUS_END) {
           // shared interrupt circuit
            
           // ignore write
       }
    }

    @Override
    public void onStore32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
       if (addr >= REGISTER_SHARED_INT_CONFIG_BEGIN && addr <= REGISTER_SHARED_INT_CONFIG_END) {
           // shared interrupt circuit
            FrSharedInterruptCircuit sharedInterruptCircuit = (FrSharedInterruptCircuit)platform.getSharedInterruptCircuit();

            sharedInterruptCircuit.setConfigReg((addr-REGISTER_SHARED_INT_CONFIG_BEGIN)>>2,value);
       } else if (addr >= REGISTER_SHARED_INT_STATUS_BEGIN && addr <= REGISTER_SHARED_INT_STATUS_END) {
           // shared interrupt circuit
            
           // ignore write
       }
    }
}
