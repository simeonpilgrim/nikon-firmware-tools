package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.imageTransferCircuit.fr.FrImageTransferCircuit;
import com.nikonhacker.emu.peripherials.imageTransferCircuit.fr.FrImageTransferChannel;

// addressing ImageTransferCircuit
public class Expeed4018IoListener extends IoActivityListener {

    public static final int BASE_ADDRESS = 0x4018_0000;
    public static final int ADDRESS_MASK = 0xFFFF_F000;

    public static final int NUM_IMAGE_TRANSFER_CONTROLLER = 2;

    public Expeed4018IoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);
    }

    @Override
    public boolean matches(int address) {
        return (address & ADDRESS_MASK) == BASE_ADDRESS;
    }

    @Override
    public Byte onLoadData8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        if (logRegisterMessages) warn("FrImageTransferCircuit: registers can't be addressed as byte");
        return null;
    }

    @Override
    public Integer onLoadData16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        FrImageTransferCircuit imageTransferCircuit = (FrImageTransferCircuit)(platform.getImageTransferCircuit());
        
        switch (addr&0xFFF) {
            case 0x00: return imageTransferCircuit.getEnabled();
            case 0x10: return imageTransferCircuit.getInterruptMask();
            case 0x12: return imageTransferCircuit.getInterruptStatus();
            default:
                int unit = ((addr&0x40)!=0 ? 1 : 0);
                switch (addr&0xFBF) {
                    case 0x100: imageTransferCircuit.channels[unit].setCommand(value); break;
                    case 0x10C: imageTransferCircuit.channels[unit].setSourceBufferWidth(value); break;
                    case 0x10E: imageTransferCircuit.channels[unit].setDestinationBufferWidth(value); break;
                    case 0x110: imageTransferCircuit.channels[unit].setDestinationImageWidth(value); break;
                    case 0x112: imageTransferCircuit.channels[unit].setDestinationImageHeight(value); break;
                    case 0x102:
                    case 0x11C: return imageTransferCircuit.channels[unit].getRegUnimplemented();
                    default:
                        if (logRegisterMessages) warn("FrImageTransferCircuit: Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load16 is not supported yet");
                }
        }
        return null;
    }

    @Override
    public Integer onLoadData32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        int unit = ((addr&0x40)!=0 ? 1 : 0);

        FrImageTransferCircuit imageTransferCircuit = (FrImageTransferCircuit)(platform.getImageTransferCircuit());
        switch (addr&0xFBF) {
            case 0x114: return imageTransferCircuit.channels[unit].getSourceAddress();
            case 0x118: return imageTransferCircuit.channels[unit].getDestinationAddress();
            case 0x104:
            case 0x108: return imageTransferCircuit.channels[unit].getRegUnimplemented();
            default:
                if (logRegisterMessages) warn("FrImageTransferCircuit: Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Load32 is not supported yet");
        }
        return null;
    }


    @Override
    public void onStore8(byte[] pageData, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        if (logRegisterMessages) warn("FrImageTransferCircuit: registers can't be addressed as byte");
    }

    @Override
    public void onStore16(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        FrImageTransferCircuit imageTransferCircuit = (FrImageTransferCircuit)(platform.getImageTransferCircuit());

        switch (addr&0xFFF) {
            case 0x00: imageTransferCircuit.setEnabled(value);
            case 0x10: imageTransferCircuit.setInterruptMask(value); break;
            case 0x12: imageTransferCircuit.setInterruptStatus(value); break;
            default:
                int unit = ((addr&0x40)!=0 ? 1 : 0);
                switch (addr&0xFBF) {
                    case 0x100: imageTransferCircuit.channels[unit].setCommand(value); break;
                    case 0x10C: imageTransferCircuit.channels[unit].setSourceBufferWidth(value); break;
                    case 0x10E: imageTransferCircuit.channels[unit].setDestinationBufferWidth(value); break;
                    case 0x110: imageTransferCircuit.channels[unit].setDestinationImageWidth(value); break;
                    case 0x112: imageTransferCircuit.channels[unit].setDestinationImageHeight(value); break;
                    case 0x102:
                    case 0x11C: imageTransferCircuit.channels[unit].setRegUnimplemented(value); break;
                    default:
                        if (logRegisterMessages) warn("FrImageTransferCircuit: Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store16 value 0x" + Format.asHex(value, 4) + " is not supported yet");
                }
        }
    }

    @Override
    public void onStore32(byte[] pageData, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        int unit = ((addr&0x40)!=0 ? 1 : 0);

        FrImageTransferCircuit imageTransferCircuit = (FrImageTransferCircuit)(platform.getImageTransferCircuit());
        switch (addr&0xFBF) {
            case 0x114: imageTransferCircuit.channels[unit].setSourceAddress(value); break;
            case 0x118: imageTransferCircuit.channels[unit].setDestinationAddress(value); break;
            case 0x104:
            case 0x108: imageTransferCircuit.channels[unit].setRegUnimplemented(value); break;
            default:
                if (logRegisterMessages) warn("FrImageTransferCircuit: Register 0x" + Format.asHex(addr&0xFFF, 3) + ": Store32 value 0x" + Format.asHex(value, 8) + " is not supported yet");
        }
    }
}
