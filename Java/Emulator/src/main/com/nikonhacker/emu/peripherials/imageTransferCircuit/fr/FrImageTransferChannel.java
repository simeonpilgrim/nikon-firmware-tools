package com.nikonhacker.emu.peripherials.imageTransferCircuit.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.imageTransferCircuit.ImageTransferChannel;

/*
Usecase 1: fill block with byte

            read from 0x40180000 : 0x0000      (@0x001DA7CC)
0x0000     written to 0x4018010C               (@0x001DA55C)
0x0280     written to 0x4018010E               (@0x001DA566)
0x0280     written to 0x40180110               (@0x001DA570)
0x01E0     written to 0x40180112               (@0x001DA57A)
0x00000000 written to 0x40180114               (@0x001DA584)
0xCE57DC60 written to 0x40180118               (@0x001DA58E)
0x0404     written to 0x40180102               (@0x001DA5AA)
0xF0000000 written to 0x40180104               (@0x001DA5B4)
0xF0000000 written to 0x40180108               (@0x001DA5BE)
0x8000     written to 0x4018011C               (@0x001DA5D2)
            read from 0x40180010 : 0x0000      (@0x001DA802)
0x0010     written to 0x40180010               (@0x001DA812)
0x4700     written to 0x40180100               (@0x001DA60C)

0x0000     written to 0x40180100               (@0x001DA6EA)

            read from 0x40180000 : 0x0000      (@0x001DA7CC)
0x0000     written to 0x4018010C               (@0x001DA55C)
0x0280     written to 0x4018010E               (@0x001DA566)
0x0140     written to 0x40180110               (@0x001DA570)
0x01E0     written to 0x40180112               (@0x001DA57A)
0x00000000 written to 0x40180114               (@0x001DA584)
0xCE5E1C60 written to 0x40180118               (@0x001DA58E)
0x0404     written to 0x40180102               (@0x001DA5AA)
0xF0000000 written to 0x40180104               (@0x001DA5B4)
0xF0000000 written to 0x40180108               (@0x001DA5BE)
0x8000     written to 0x4018011C               (@0x001DA5D2)
            read from 0x40180010 : 0x0010      (@0x001DA802)
0x0010     written to 0x40180010               (@0x001DA812)
0x4780     written to 0x40180100               (@0x001DA60C)

0x0000     written to 0x40180100               (@0x001DA6EA)

Usecase 2: copy

0x0280     written to 0x4018010C               (@0x001DA55C)
0x0280     written to 0x4018010E               (@0x001DA566)
0x0280     written to 0x40180110               (@0x001DA570)
0x0780     written to 0x40180112               (@0x001DA57A)
0xCE57DC60 written to 0x40180114               (@0x001DA584)
0xCE451C60 written to 0x40180118               (@0x001DA58E)
0x0404     written to 0x40180102               (@0x001DA5AA)
0xF0000000 written to 0x40180104               (@0x001DA5B4)
0xF0000000 written to 0x40180108               (@0x001DA5BE)
0x8000     written to 0x4018011C               (@0x001DA5D2)
            read from 0x40180010 : 0x0000      (@0x001DA802)
0x0010     written to 0x40180010               (@0x001DA812)
0x4600     written to 0x40180100               (@0x001DA60C)

*/

public class FrImageTransferChannel implements ImageTransferChannel {
    private int channelNumber;
    
    private int command;
    private Platform platform;
    private FrImageTransferCircuit imageTransferCircuit;
    
    private int sourceAddress, destinationAddress;
    private int destinationBufferWidth, destinationImageWidth, destinationImageHeight,sourceBufferWidth;
    
    public FrImageTransferChannel(int number, FrImageTransferCircuit imageTransferCircuit, Platform platform) {
        this.channelNumber = number;
        this.platform = platform;
        this.imageTransferCircuit = imageTransferCircuit;
    }

    public void setRegUnimplemented(int value) {
    }

    public int getRegUnimplemented() {
        return 0;
    }

    public void setDestinationImageWidth(int value) {
        destinationImageWidth = value;
    }

    public int getDestinationImageWidth() {
        return destinationImageWidth;
    }

    public void setDestinationBufferWidth(int value) {
        destinationBufferWidth = value;
    }

    public int getDestinationBufferWidth() {
        return destinationBufferWidth;
    }

    public void setSourceBufferWidth(int value) {
        sourceBufferWidth = value;
    }

    public int getSourceBufferWidth() {
        return sourceBufferWidth;
    }

    public void setDestinationImageHeight(int value) {
        destinationImageHeight = value;
    }

    public int getDestinationImageHeight() {
        return destinationImageHeight;
    }

    public void setDestinationAddress(int value) {
        destinationAddress = value;
    }

    public int getDestinationAddress() {
        return destinationAddress;
    }

    public void setSourceAddress(int value) {
        sourceAddress = value;
    }

    public int getSourceAddress() {
        return sourceAddress;
    }

    public int getCommand () {
        return command;
    }
    
    public void setCommand (int value) {
        command = value;
        if ((value&0x4000)!=0) {
            // start
            switch (value&0xF00) {
                case 0x700: //fill destination image
                    fill(value&0xFF);
                    imageTransferCircuit.requestInterrupt(channelNumber);
                    break;
                case 0x600: //copy image
                    imageTransferCircuit.requestInterrupt(channelNumber);
                    break;
                default:
                    System.out.println("ImageTransferChannel(" + channelNumber + "): unsupported command 0x" + Format.asHex(value, 4));
            }
        }
    }
    
    private void copy () {
        int from = sourceAddress, to=destinationAddress;
        final int addFrom = sourceBufferWidth-destinationImageWidth;
        final int addTo = destinationBufferWidth-destinationImageWidth;
        DebuggableMemory memory = platform.getMemory();
        
        if (addTo<0 || addFrom<0) {
            // something is wrong
            throw new RuntimeException("ImageTransferChannel(" + channelNumber +"): ImageWidth > BufferWidth");
        }
        
        for (int y=0; y<destinationImageHeight; y++) {
            for (int x=0; x<destinationImageWidth; x++,to++,from++) {
                memory.store8(to, 
                              memory.loadUnsigned8(from, DebuggableMemory.AccessSource.IMGA), 
                              DebuggableMemory.AccessSource.IMGA);
            }
            from += addFrom;
            to += addTo;
        }
    }

    private void fill(int value){
        int to=destinationAddress;
        final int addTo = destinationBufferWidth-destinationImageWidth;
        DebuggableMemory memory = platform.getMemory();
        
        if (addTo<0) {
            // something is wrong
            throw new RuntimeException("ImageTransferChannel(" + channelNumber +"): ImageWidth > BufferWidth");
        }
        
        // fill image
        for (int y=0; y<destinationImageHeight; y++, to += addTo) {
            for (int x=0; x<destinationImageWidth; x++, to++) {
                memory.store8(to, value, DebuggableMemory.AccessSource.IMGA);
            }
        }
    }
}
