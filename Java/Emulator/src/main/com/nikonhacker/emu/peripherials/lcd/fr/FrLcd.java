package com.nikonhacker.emu.peripherials.lcd.fr;

import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.ioPort.Pin;
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

    private PowerPin powerPin;
    private boolean isPoweredOn = false;

    public FrLcd(Platform platform) {
        this.platform = platform;
        powerPin = new PowerPin("Main lcd power");
    }

    public final static BufferedImage getImage(int width, int height) {
        // alignment
        if ((width&1)!=0)
            return null;
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
         @param yAddr start of Y buffer
         @param cbAddr start of Cb buffer
         @param crAddr start of Cr buffer
     */
    public final void updateImage(BufferedImage img, int yAddr, int cbAddr, int crAddr, int align) {
        final int imageWidth = img.getWidth();
        final int imageHeight = img.getHeight();

        if ((imageWidth&0x1)!=0 || (align&1)!=0) {
            throw new RuntimeException("Lcd: image width must be aligned to 32!");
        }

        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        DebuggableMemory memory = platform.getMemory();

        // calculate addition factor for alignment
        final int addY = ((imageWidth % align) != 0 ? (align-(imageWidth % align)) : 0);
        final int addCbCr = (((imageWidth>>1) % align) != 0 ? (align-((imageWidth>>1) % align)) : 0);

        // optimisation for buffered image TYPE_INT_RGB
        // coderat: this optimized code is 2x faster as before
        for (int yPos = 0, pixelPos=0; yPos < imageHeight; yPos++) {
            for (int xPos = 0; xPos < imageWidth; xPos+=2, yAddr+=2, pixelPos+=2) {
                final int y = memory.loadUnsigned16(yAddr, null);
                setPixelsFromYCbCr422(pixels, pixelPos,
                                      y>>8,
                                      y&0xFF,
                                      memory.loadUnsigned8(cbAddr++, null),
                                      memory.loadUnsigned8(crAddr++, null));
            }
            yAddr += addY;
            cbAddr += addCbCr;
            crAddr += addCbCr;
        }
    }

    public Pin getPowerPin() {
        return powerPin;
    }

    private class PowerPin extends Pin {
        public PowerPin(String name) {
            super(name);
        }

        @Override
        public void setInputValue(int value) {
            isPoweredOn = (value == 0);
        }
    }

    public boolean isPoweredOn() {
        return isPoweredOn;
    }
}
