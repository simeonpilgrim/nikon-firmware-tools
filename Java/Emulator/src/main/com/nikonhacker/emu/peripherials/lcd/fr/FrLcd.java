package com.nikonhacker.emu.peripherials.lcd.fr;

import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.lcd.Lcd;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class FrLcd implements Lcd {
    public static final int CAMERA_SCREEN_MEMORY_Y = 0xCE57DC60;
    public static final int CAMERA_SCREEN_MEMORY_U = CAMERA_SCREEN_MEMORY_Y + 0x64000;
    public static final int CAMERA_SCREEN_MEMORY_V = CAMERA_SCREEN_MEMORY_Y + 2 * 0x64000;
    public static final int CAMERA_SCREEN_WIDTH    = 640;
    public static final int CAMERA_SCREEN_HEIGHT   = 480;

    private Platform platform;
    
    public FrLcd(Platform platform) {
        this.platform = platform;
    }

    /**    create default screen image object of defualt size
           @return Initialised image object with deault LCD screen "width" and "height"
     */
    public final static BufferedImage getScreenImage() {
        return new BufferedImage(CAMERA_SCREEN_WIDTH, CAMERA_SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
    }
    
    private static final int clamp(int x) {
        return (x<=255 ? (x>=0 ? x : 0 ) : 255);
    }

    private static final void setPixelsFromYCbCr422(int[] pixels,int pos, int y, int y1, int u, int v) {
        // full range YCbCr to RGB conversion
        final int factorR = Math.round(1.4f * (v-128) );
        final int factorG = Math.round(-0.343f * (u-128) - 0.711f * (v-128));
        final int factorB = Math.round(1.765f * (u-128) );
        
        // coderat: conversion YCbCr->RGB clamp is needed, because RGB do not include complete YCbCr space
        pixels[pos]   = (clamp(y+factorR) << 16) | (clamp(y+factorG) << 8) | clamp(y+factorB);
        pixels[pos+1] = (clamp(y1+factorR) << 16) | (clamp(y1+factorG) << 8) | clamp(y1+factorB);
    }

    /**    this method can be used to show any screen or picture buffer in YCbCr 4:2:2 format
    
         @param img Initialised image object with "width" and "height" of image to be displayed
         @param yStart start of Y buffer
         @param uStart start of Cb buffer
         @param vStart start of Cr buffer
     */
    public final void updateImage(BufferedImage img, int yStart, int uStart, int vStart) {
        final int screenWidth = img.getWidth();
        final int screenHeight = img.getHeight();
        int yOffset=0, uAddr=uStart, vAddr=vStart;
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        final int halfWidth = screenWidth>>1;
        
        DebuggableMemory memory = platform.getMemory();

        // optimisation for buffered image TYPE_INT_RGB
        // coderat: this optimized code is 2x faster as before
        for (int yPos = 0; yPos < screenHeight; yPos++) {
            for (int xPos = 0; xPos < halfWidth; xPos++,yOffset+=2) {
                final int y = memory.loadUnsigned16(yStart+yOffset, null);
                setPixelsFromYCbCr422(pixels, yOffset, 
                                      y>>8,
                                      y&0xFF, 
                                      memory.loadUnsigned8(uAddr++, null), 
                                      memory.loadUnsigned8(vAddr++, null));
            }
            uAddr += halfWidth;
            vAddr += halfWidth;
        }
    }
}
