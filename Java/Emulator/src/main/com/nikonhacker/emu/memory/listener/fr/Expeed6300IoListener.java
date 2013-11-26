package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.sdController.fr.FrSdController;

// addressing resolution converter units
public class Expeed6300IoListener extends IoActivityListener {

    public static final int BASE_ADDRESS1 = 0x6300_0000;
    public static final int ADDRESS_MASK1 = 0xFFFF_F000;

    public static final int BASE_ADDRESS2 = 0x6400_0000;
    public static final int ADDRESS_MASK2 = 0xFFFF_F000;

    public static final int NUM_SD_CONTROLLER = 2;
    
    public Expeed6300IoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);
    }

    @Override
    public boolean matches(int address) {
        return (((address & ADDRESS_MASK1) == BASE_ADDRESS1) || ((address & ADDRESS_MASK2) == BASE_ADDRESS2));
    }

    @Override
    public Byte onLoadData8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load8 is not supported yet");
        return null;
    }

    @Override
    public Integer onLoadData16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&4000000)!=0 ? 1 : 0 );

        FrSdController sdController = (FrSdController)(platform.getSdController()[unit]);
        switch (addr&0xFFF) {
            default:
                if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load16 is not supported yet");
        }
        return null;
    }

    @Override
    public Integer onLoadData32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&4000000)!=0 ? 1 : 0 );

        FrSdController sdController = (FrSdController)(platform.getSdController()[unit]);
        switch (addr&0xFFF) {
            default:
                if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load32 is not supported yet");
        }
        return null;
    }


    @Override
    public void onStore8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store8 value 0x" + Format.asHex(value, 2) + " is not supported yet");
    }

    @Override
    public void onStore16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&4000000)!=0 ? 1 : 0 );

        FrSdController sdController = (FrSdController)(platform.getSdController()[unit]);
        switch (addr&0xFFF) {
            default:
                if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store16 value 0x" + Format.asHex(value, 4) + " is not supported yet");
        }
    }

    @Override
    public void onStore32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&4000000)!=0 ? 1 : 0 );

        FrSdController sdController = (FrSdController)(platform.getSdController()[unit]);
        switch (addr&0xFFF) {
            default:
                if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store32 value 0x" + Format.asHex(value, 8) + " is not supported yet");
        }
    }
}
