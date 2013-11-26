package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.jpegCodec.fr.FrJpegCodec;

// addressing JPEG encoder/decoder units
public class Expeed40X3IoListener extends IoActivityListener {

    public static final int BASE_ADDRESS = 0x4003_0000;
    public static final int ADDRESS_MASK = 0xFFEF_F000;

    public static final int NUM_JPEG_CODEC = 2;

    public Expeed40X3IoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);
    }

    @Override
    public boolean matches(int address) {
        return (address & ADDRESS_MASK) == BASE_ADDRESS;
    }

    @Override
    public Byte onLoadData8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        int unit = ((addr&0x100000)!=0 ? 1 : 0);

        FrJpegCodec jpegCodec = (FrJpegCodec)(platform.getJpegCodec()[unit]);
        switch (addr&0xFFF) {
            case 0x000: return jpegCodec.getReg000();
            case 0x007: return jpegCodec.getJPEGHeightHi();
            case 0x008: return jpegCodec.getJPEGHeightLo();
            case 0x009: return jpegCodec.getJPEGWidthHi();
            case 0x00A: return jpegCodec.getJPEGWidthLo();
            case 0x00F: return jpegCodec.getInterruptStatus();
            case 0x410: return jpegCodec.getErrorCode();
            default:
                if (logRegisterMessages) warn("JpegCodec (" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load8 is not supported yet");
        }
        return null;
    }

    @Override
    public Integer onLoadData16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        int unit = ((addr&0x100000)!=0 ? 1 : 0);

        FrJpegCodec jpegCodec = (FrJpegCodec)(platform.getJpegCodec()[unit]);
        switch (addr&0xFFF) {
            case 0x400: return jpegCodec.getReg400();
            case 0x402: return jpegCodec.getReg402();
            case 0x404: return jpegCodec.getCommand();
            case 0xF02: return jpegCodec.getYWidth();
            case 0xF04: return jpegCodec.getOutputWidth();
            case 0xF06: return jpegCodec.getOutputHeight();
            case 0xF08: return jpegCodec.getCbCrWidth();
            case 0xFF6: return jpegCodec.getTransferInterruptStatus();
            case 0x40A:
            case 0xF00: 
            case 0xF0C:
            case 0xF0E:
            case 0xF1C:
            case 0xF20: 
            case 0xFF4: 
                return jpegCodec.getRegUnimplemented();
            default:
                if (logRegisterMessages) warn("JpegCodec (" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load16 is not supported yet");
        }
        return null;
    }

    @Override
    public Integer onLoadData32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        int unit = ((addr&0x100000)!=0 ? 1 : 0);

        FrJpegCodec jpegCodec = (FrJpegCodec)(platform.getJpegCodec()[unit]);
        switch (addr&0xFFF) {
            case 0xF10: return jpegCodec.getDstAddrY();
            case 0xF14: return jpegCodec.getDstAddrCb();
            case 0xF18: return jpegCodec.getDstAddrCr();
            case 0xF24: return jpegCodec.getSizeJpeg();
            case 0xF28: return jpegCodec.getSrcAddrJpeg();
            default:
                if (logRegisterMessages) warn("JpegCodec (" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load32 is not supported yet");
        }
        return null;
    }


    @Override
    public void onStore8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        int unit = ((addr&0x100000)!=0 ? 1 : 0);

        FrJpegCodec jpegCodec = (FrJpegCodec)(platform.getJpegCodec()[unit]);
        switch (addr&0xFFF) {
            case 0x000: jpegCodec.setReg000(value); break;
            case 0x00F: jpegCodec.setInterruptStatus(value); break;
            case 0x410: jpegCodec.setErrorCode(value); break;
            case 0x007: // not writable
            case 0x008:
            case 0x009:
            case 0x00A:
                break;
            case 0x001:
            case 0x00E:
                jpegCodec.setRegUnimplemented(value); break;
            default:
                if (logRegisterMessages) warn("JpegCodec (" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store8 value 0x" + Format.asHex(value, 2) + " is not supported yet");
        }
    }

    @Override
    public void onStore16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        int unit = ((addr&0x100000)!=0 ? 1 : 0);

        FrJpegCodec jpegCodec = (FrJpegCodec)(platform.getJpegCodec()[unit]);
        switch (addr&0xFFF) {
            case 0x400: jpegCodec.setReg400(value); break;
            case 0x402: jpegCodec.setReg402(value); break;
            case 0x404: jpegCodec.setCommand(value); break;
            case 0xF02: jpegCodec.setYWidth(value); break;
            case 0xF04: jpegCodec.setOutputWidth(value); break;
            case 0xF06: jpegCodec.setOutputHeight(value); break;
            case 0xF08: jpegCodec.setCbCrWidth(value); break;
            case 0xFF6: jpegCodec.setTransferInterruptStatus(value); break;
            case 0x40A:
            case 0xF00:
            case 0xF0C:
            case 0xF0E:
            case 0xF1C:
            case 0xF20: 
            case 0xFF4: 
                jpegCodec.setRegUnimplemented(value); break;
            default:
                if (logRegisterMessages) warn("JpegCodec (" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store16 value 0x" + Format.asHex(value, 4) + " is not supported yet");
        }
    }

    @Override
    public void onStore32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        int unit = ((addr&0x100000)!=0 ? 1 : 0);

        FrJpegCodec jpegCodec = (FrJpegCodec)(platform.getJpegCodec()[unit]);
        switch (addr&0xFFF) {
            case 0xF10: jpegCodec.setDstAddrY(value); break;
            case 0xF14: jpegCodec.setDstAddrCb(value); break;
            case 0xF18: jpegCodec.setDstAddrCr(value); break;
            case 0xF24: jpegCodec.setSizeJpeg(value); break;
            case 0xF28: jpegCodec.setSrcAddrJpeg(value); break;
            default:
                if (logRegisterMessages) warn("JpegCodec (" + unit + "): Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store32 value 0x" + Format.asHex(value, 8) + " is not supported yet");
        }
    }
}
