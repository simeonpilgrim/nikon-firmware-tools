package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.resolutionConverter.fr.FrResolutionConverter;

// addressing resolution converter units
public class Expeed4002IoListener extends IoActivityListener {

    public static final int BASE_ADDRESS1 = 0x4002_0000;
    public static final int ADDRESS_MASK1 = 0xFFFF_F000;

    public static final int BASE_ADDRESS2 = 0x400F_0000;
    public static final int ADDRESS_MASK2 = 0xFFEF_F000;

    public static final int NUM_IMAGE_TRANSFER_CIRCUIT = 3;
    
    public Expeed4002IoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);
    }

    @Override
    public boolean matches(int address) {
        return (((address & ADDRESS_MASK1) == BASE_ADDRESS1) || ((address & ADDRESS_MASK2) == BASE_ADDRESS2));
    }

    @Override
    public Byte onLoadData8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&0x100000)!=0 ? 2 : ((addr>>16)&1) );

        FrResolutionConverter resolutionConverter = (FrResolutionConverter)(platform.getResolutionConverter()[unit]);
        switch (addr&0xFFF) {
            default:
                if (logRegisterMessages) warn("ResolutionConverter(" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load8 is not supported yet");
        }
        return null;
    }

    @Override
    public Integer onLoadData16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&0x100000)!=0 ? 2 : ((addr>>16)&1) );

        FrResolutionConverter resolutionConverter = (FrResolutionConverter)(platform.getResolutionConverter()[unit]);
        switch (addr&0xFFF) {
            case 0x002: return resolutionConverter.getCommand();
            case 0x00C: return resolutionConverter.getInterruptStatus();
            case 0x010: return resolutionConverter.getScaleFactor0();
            case 0x012: return resolutionConverter.getScaleFactor1();
            case 0x014: return resolutionConverter.getScaleFactor2();
            case 0x016: return resolutionConverter.getScaleFactor3();
            case 0x018: return resolutionConverter.getDestinationImageWidth();
            case 0x01A: return resolutionConverter.getDestinationImageHeight();
            case 0x020: return resolutionConverter.getSourceBufferWidth();
            case 0x022: return resolutionConverter.getDestinationBufferWidth();
            case 0x02C: return resolutionConverter.getSourceAddressLo();
            case 0x02E: return resolutionConverter.getDestinationAddressLo();
            case 0x000:
            case 0x004:
            case 0x006:
            case 0x008:
            case 0x00A:
                return resolutionConverter.getRegUnimplemented();
            default:
                if (logRegisterMessages) warn("ResolutionConverter(" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load16 is not supported yet");
        }
        return null;
    }

    @Override
    public Integer onLoadData32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&0x100000)!=0 ? 2 : ((addr>>16)&1) );

        FrResolutionConverter resolutionConverter = (FrResolutionConverter)(platform.getResolutionConverter()[unit]);
        switch (addr&0xFFF) {
            case 0x030: return resolutionConverter.getSourceAddressHi();
            case 0x040: return resolutionConverter.getDestinationAddressHi();
            default:
                if (logRegisterMessages) warn("ResolutionConverter(" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load32 is not supported yet");
        }
        return null;
    }


    @Override
    public void onStore8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&0x100000)!=0 ? 2 : ((addr>>16)&1) );

        FrResolutionConverter resolutionConverter = (FrResolutionConverter)(platform.getResolutionConverter()[unit]);
        switch (addr&0xFFF) {
            default:
                if (logRegisterMessages) warn("ResolutionConverter(" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store8 value 0x" + Format.asHex(value, 2) + " is not supported yet");
        }
    }

    @Override
    public void onStore16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&0x100000)!=0 ? 2 : ((addr>>16)&1) );

        FrResolutionConverter resolutionConverter = (FrResolutionConverter)(platform.getResolutionConverter()[unit]);
        switch (addr&0xFFF) {
            case 0x002: resolutionConverter.setCommand(value); break;
            case 0x00C: resolutionConverter.setInterruptStatus(value); break;
            case 0x010: resolutionConverter.setScaleFactor0(value); break;
            case 0x012: resolutionConverter.setScaleFactor1(value); break;
            case 0x014: resolutionConverter.setScaleFactor2(value); break;
            case 0x016: resolutionConverter.setScaleFactor3(value); break;
            case 0x018: resolutionConverter.setDestinationImageWidth(value); break;
            case 0x01A: resolutionConverter.setDestinationImageHeight(value); break;
            case 0x020: resolutionConverter.setSourceBufferWidth(value); break;
            case 0x022: resolutionConverter.setDestinationBufferWidth(value); break;
            case 0x02C: resolutionConverter.setSourceAddressLo(value); break;
            case 0x02E: resolutionConverter.setDestinationAddressLo(value); break;
            case 0x000:
            case 0x004:
            case 0x006:
            case 0x008:
            case 0x00A:
                resolutionConverter.setRegUnimplemented(value); break;
            default:
                if (logRegisterMessages) warn("ResolutionConverter(" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store16 value 0x" + Format.asHex(value, 4) + " is not supported yet");
        }
    }

    @Override
    public void onStore32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        final int unit = ( (addr&0x100000)!=0 ? 2 : ((addr>>16)&1) );

        FrResolutionConverter resolutionConverter = (FrResolutionConverter)(platform.getResolutionConverter()[unit]);
        switch (addr&0xFFF) {
            case 0x030: resolutionConverter.setSourceAddressHi(value); break;
            case 0x040: resolutionConverter.setDestinationAddressHi(value); break;
            default:
                if (logRegisterMessages) warn("ResolutionConverter(" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store32 value 0x" + Format.asHex(value, 8) + " is not supported yet");
        }
    }
}
