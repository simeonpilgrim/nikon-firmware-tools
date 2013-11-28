package com.nikonhacker.emu.peripherials.resolutionConverter.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.resolutionConverter.ResolutionConverter;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;

/*
    Usecase: init
    
0x0000     written to 0x40020000               (@0x0010183C)
            read from 0x40020000 : 0x0000      (@0x001DB684)
    
    Usecase: transfer YUV422 image to screen memory
    
Part 1:
		0x8000     written to 0x40020000               (@0x001DAB66)
		0x0000     written to 0x40020000               (@0x001DAB6A)
		
		0x0240     written to 0x40020018               (@0x001DAB70)
		0x0002     written to 0x4002001A               (@0x001DAB76)
		
		0x0100     written to 0x40020014               (@0x001DACEA)
		0x0100     written to 0x40020016               (@0x001DACF0)
		
		0x0000     written to 0x40020010               (@0x001DAD3C)
		0x0000     written to 0x40020012               (@0x001DAD4E)
		0x0000     written to 0x4002002C               (@0x001DAD5A)
		0x0000     written to 0x4002002E               (@0x001DAD62)
		0x0240     written to 0x40020020               (@0x001DAD6A)
		0x0280     written to 0x40020022               (@0x001DAD72)
		0x8F9AA040 written to 0x40020030               (@0x001DAD7C)
		0xCE5863A0 written to 0x40020040               (@0x001DAD86)
		0x0000     written to 0x40020004               (@0x001DAD8C)
		
		           read from 0x4002000A : 0x0000      (@0x001DADCE)
		0x8000     written to 0x4002000A               (@0x001DADCE)
		
		           read from 0x40020006 : 0x0000      (@0x001DB6B4)
		0x0000     written to 0x40020006               (@0x001DB6C2)
		           read from 0x40020006 : 0x0000      (@0x001DB6D4)
		0x0000     written to 0x40020006               (@0x001DB6E2)
		           read from 0x40020006 : 0x0000      (@0x001DB6F4)
		0x0000     written to 0x40020006               (@0x001DB702)
		0x0000     written to 0x40020008               (@0x001DB734)
		           read from 0x40020006 : 0x0000      (@0x001DB70A)
		0x0054     written to 0x40020006               (@0x001DB726)
		0x4410     written to 0x40020002               (@0x001DAEB8)			
		
		WaitEvent									
		-> interrupt -> clear 0x8000 written to 0x4002000C
		
		           read from 0x4002000A : 0x8000      (@0x001DB650)
		0x0000     written to 0x4002000A               (@0x001DB650)
		0x0000     written to 0x40020002               (@0x001DB656)
				
Part 2:
		0x8000     written to 0x40020000               (@0x001DAB66)
		0x0000     written to 0x40020000               (@0x001DAB6A)
		
		0x0120     written to 0x40020018               (@0x001DAB70)
		
		0x0002     written to 0x4002001A               (@0x001DAB76)
		
		0x0100     written to 0x40020014               (@0x001DACEA)
		0x0100     written to 0x40020016               (@0x001DACF0)
		
		0x0000     written to 0x40020010               (@0x001DAD3C)
		0x0000     written to 0x40020012               (@0x001DAD4E)
		0x0000     written to 0x4002002C               (@0x001DAD5A)
		0x0000     written to 0x4002002E               (@0x001DAD62)
		0x0120     written to 0x40020020               (@0x001DAD6A)
		0x0280     written to 0x40020022               (@0x001DAD72)
		0x8F9B1840 written to 0x40020030               (@0x001DAD7C)
		0xCE5EA380 written to 0x40020040               (@0x001DAD86)
		0x0000     written to 0x40020004               (@0x001DAD8C)
		
		           read from 0x4002000A : 0x0000      (@0x001DADCE)
		0x8000     written to 0x4002000A               (@0x001DADCE)
		
		           read from 0x40020006 : 0x0054      (@0x001DB6B4)
		0x0054     written to 0x40020006               (@0x001DB6C2)
		
		           read from 0x40020006 : 0x0054      (@0x001DB6D4)
		0x0054     written to 0x40020006               (@0x001DB6E2)
		
		           read from 0x40020006 : 0x0054      (@0x001DB6F4)
		0x0054     written to 0x40020006               (@0x001DB702)
		
		0x0000     written to 0x40020008               (@0x001DB734)
		
		           read from 0x40020006 : 0x0054      (@0x001DB70A)
		0x0054     written to 0x40020006               (@0x001DB726)
		
		0x4410     written to 0x40020002               (@0x001DAEB8)
		
		WaitEvent
		-> interrupt -> clear 0x8000 written to 0x4002000C

		           read from 0x4002000A : 0x8000      (@0x001DB650)
		0x0000     written to 0x4002000A               (@0x001DB650)
		0x0000     written to 0x40020002               (@0x001DB656)										

Part3:
		0x8000     written to 0x40020000               (@0x001DAB66)
		0x0000     written to 0x40020000               (@0x001DAB6A)
		0x0120     written to 0x40020018               (@0x001DAB70)
		0x0002     written to 0x4002001A               (@0x001DAB76)
		0x0100     written to 0x40020014               (@0x001DACEA)
		0x0100     written to 0x40020016               (@0x001DACF0)
		0x0000     written to 0x40020010               (@0x001DAD3C)
		0x0000     written to 0x40020012               (@0x001DAD4E)
		0x0000     written to 0x4002002C               (@0x001DAD5A)
		0x0000     written to 0x4002002E               (@0x001DAD62)
		0x0120     written to 0x40020020               (@0x001DAD6A)
		0x0280     written to 0x40020022               (@0x001DAD72)
		0x8F9B5440 written to 0x40020030               (@0x001DAD7C)
		0xCE64E380 written to 0x40020040               (@0x001DAD86)
		0x0000     written to 0x40020004               (@0x001DAD8C)
		           read from 0x4002000A : 0x0000      (@0x001DADCE)
		0x8000     written to 0x4002000A               (@0x001DADCE)
		           read from 0x40020006 : 0x0054      (@0x001DB6B4)
		0x0054     written to 0x40020006               (@0x001DB6C2)
		           read from 0x40020006 : 0x0054      (@0x001DB6D4)
		0x0054     written to 0x40020006               (@0x001DB6E2)
		           read from 0x40020006 : 0x0054      (@0x001DB6F4)
		0x0054     written to 0x40020006               (@0x001DB702)
		0x0000     written to 0x40020008               (@0x001DB734)
		           read from 0x40020006 : 0x0054      (@0x001DB70A)
		0x0054     written to 0x40020006               (@0x001DB726)
		0x4410     written to 0x40020002               (@0x001DAEB8)

		WaitEvent
		-> interrupt -> clear 0x8000 written to 0x4002000C

		           read from 0x4002000A : 0x8000      (@0x001DB650)
		0x0000     written to 0x4002000A               (@0x001DB650)
		0x0000     written to 0x40020002               (@0x001DB656)					
*/
public class FrResolutionConverter implements ResolutionConverter {
    private int channelNumber;
    private Platform platform;
    
    private int interruptStatus, command;
    private int scaleFactor0, scaleFactor1, scaleFactor2, scaleFactor3;
    private int destinationImageWidth, destinationImageHeight;
    
    private int sourceAddress, destinationAddress;
    private int sourceBufferWidth, destinationBufferWidth;
    
    public FrResolutionConverter(int channelNumber, Platform platform) {
        this.channelNumber = channelNumber;
        this.platform = platform;
    }
    
    public void setRegUnimplemented(int value) {
    }

    public int getRegUnimplemented() {
        return 0;
    }

    public int getInterruptStatus() {
        return interruptStatus;
    }
    
    public void setInterruptStatus (int value) {
        // now only known value is 0x08000
        interruptStatus &= (~value);
        if (interruptStatus==0)
            platform.getSharedInterruptCircuit().removeRequest(FrInterruptController.IMAGE_29_SHARED_REQUEST_NR, 29+channelNumber);
    }

    public int getScaleFactor0() {
        return scaleFactor0;
    }
    
    public void setScaleFactor0 (int value) {
        scaleFactor0 = value;
    }
    
    public int getScaleFactor1() {
        return scaleFactor1;
    }
    
    public void setScaleFactor1 (int value) {
        scaleFactor1 = value;
    }
    
    public int getScaleFactor2() {
        return scaleFactor2;
    }
    
    public void setScaleFactor2 (int value) {
        scaleFactor2 = value;
    }
    
    public int getScaleFactor3() {
        return scaleFactor3;
    }
    
    public void setScaleFactor3 (int value) {
        scaleFactor3 = value;
    }
    
    public int getDestinationImageWidth() {
        return destinationImageWidth;
    }
    
    public void setDestinationImageWidth (int value) {
        destinationImageWidth = value;
    }
    
    public int getDestinationImageHeight() {
        return destinationImageHeight;
    }
    
    public void setDestinationImageHeight (int value) {
        destinationImageHeight = value;
    }
    
    public int getCommand() {
        return command;
    }
    
    public int getDestinationBufferWidth() {
        return destinationBufferWidth;
    }
    
    public void setDestinationBufferWidth (int value) {
        destinationBufferWidth = value;
    }

    public int getSourceBufferWidth() {
        return sourceBufferWidth;
    }
    
    public void setSourceBufferWidth (int value) {
        sourceBufferWidth = value;
    }

    public int getDestinationAddressLo() {
        return (destinationAddress&0xF);
    }
    
    public int getDestinationAddressHi() {
        return (destinationAddress&(~0xF));
    }
    
    // set lower 4bits of destination address
    public void setDestinationAddressLo (int value) {
        destinationAddress = ((destinationAddress & (~0xF)) | (value&0xF));
    }
    
    // set higher 28bits of destination address
    public void setDestinationAddressHi (int value) {
        destinationAddress = ((value&(~0xF)) | (destinationAddress&0xF));
    }

    public int getSourceAddressLo() {
        return (sourceAddress&0xF);
    }
    
    public int getSourceAddressHi() {
        return (sourceAddress&(~0xF));
    }
    
    // set lower 4bits of source address
    public void setSourceAddressLo (int value) {
        sourceAddress = ((sourceAddress & (~0xF)) | (value&0xF));
    }
    
    // set higher 28bits of source address
    public void setSourceAddressHi (int value) {
        sourceAddress = ((value&(~0xF)) | (sourceAddress&0xF));
    }

    public void setCommand (int value) {
        command = value;
        if ((command&0x4000)!=0) {
            // start operation
            switch (value&0xFFF) {
                case 0x410:
                    copy();
                    interruptStatus |= 0x8000;
                    platform.getSharedInterruptCircuit().request(FrInterruptController.IMAGE_29_SHARED_REQUEST_NR, 29+channelNumber);
                    break;
                default:
                    System.out.println("ResolutionConverter(" + channelNumber + "): unsupported command 0x" + Format.asHex(value, 4));
            }
        }
    }
    
    private boolean copy () {
        DebuggableMemory memory = platform.getMemory();

        // if scaling factors are not default, can be scaling requested
        // but we still do not know what they mean
        if (scaleFactor0!=0 || scaleFactor1!=0 || scaleFactor2!=0x100 || scaleFactor3!=0x100)
            System.out.println("ResolutionConverter(" + channelNumber + "): may be scaling "+scaleFactor0+","+scaleFactor1+","+scaleFactor2+","+scaleFactor3);

        int from = sourceAddress, to=destinationAddress;
        final int addFrom = sourceBufferWidth-destinationImageWidth;
        final int addTo = destinationBufferWidth-destinationImageWidth;
        
        if (addTo<0 || addFrom<0) {
            // something is wrong
            throw new RuntimeException("ResolutionConverter(" + channelNumber +"): ImageWidth > BufferWidth");
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
        return true;
    }
}
