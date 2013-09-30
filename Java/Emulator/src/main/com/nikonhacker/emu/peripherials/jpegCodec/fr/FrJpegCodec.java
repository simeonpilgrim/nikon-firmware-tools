package com.nikonhacker.emu.peripherials.jpegCodec.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.jpegCodec.JpegCodec;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/*
    Usecase: decode JPEG to YCbCr422

General init:    
0x0000     written to 0x4003040A               (@0x0010182C)
0x0000     written to 0x4013040A               (@0x00101834)
0x0000     written to 0x40030F00               (@0x001019F6)
0x0000     written to 0x40030F20               (@0x001019FC)
0x0000     written to 0x40130F00               (@0x00101A02)
0x0000     written to 0x40130F20               (@0x00101A08)

Start usecase:
            read from 0x40030400 : 0x0000      (@0x001E35F0)
            read from 0x4003040A : 0x0000      (@0x001E3648)
0x0000     written to 0x4003040A               (@0x001E3652)
            read from 0x40030400 : 0x0000      (@0x001E366A)
0x1000     written to 0x40030400               (@0x001E3672)
            read from 0x40030400 : 0x1000      (@0x001E3678)
0x5000     written to 0x40030400               (@0x001E368A)
            read from 0x40030400 : 0x5000      (@0x001E368C)
{
            read from 0x40030400 : 0x5000      (@0x001E369C)
} loop while ([0x40030400]&0xC000)!=0xC000

            read from 0x40030404 : 0x0000      (@0x001E36AC)
0x0800     written to 0x40030404               (@0x001E36B4)

0x88       written to 0x40030000               (@0x001E392C)
0x04       written to 0x40030001               (@0x001E3932)
0x8100     written to 0x40030402               (@0x001E393C)
0xE8       written to 0x4003000E               (@0x001E3942)
0x0041     written to 0x40030F00               (@0x001E394A)
0x0001     written to 0x40030F20               (@0x001E3952)
0x8F9B9040 written to 0x40030F28               (@0x001E395C)
0x00000001 written to 0x40030F24               (@0x001E396E)
0x0000     written to 0x40030F1C               (@0x001E3976)
0x0000     written to 0x40030F0C               (@0x001E397C)
0x0000     written to 0x40030F0E               (@0x001E3982)
0x8F9AA040 written to 0x40030F10               (@0x001E3990)
0x8F9B1840 written to 0x40030F14               (@0x001E399E)
0x8F9B5440 written to 0x40030F18               (@0x001E39AC)
0x0280     written to 0x40030F02               (@0x001E39BA)
0x0140     written to 0x40030F08               (@0x001E39C8)
0x8000     written to 0x40030FF4               (@0x001E39D2)

ClearEventFlag

0x4001     written to 0x40030F20               (@0x001E3A04)
0x8000     written to 0x40030404               (@0x001E3A0E)

WaitForEventFlag

-> interrupt -> if ok set event flag pattern=2

            read from 0x40030009 : 0x00        (@0x001E3A6E)
            read from 0x4003000A : 0x00        (@0x001E3A72)
            read from 0x40030007 : 0x00        (@0x001E3A7E)
            read from 0x40030008 : 0x00        (@0x001E3A82)
            read from 0x40030000 : 0x88        (@0x001E3AB2)
0x0280     written to 0x40030F04               (@0x001E3AD2)
0x0008     written to 0x40030F06               (@0x001E3AE4)
0x60       written to 0x4003000E               (@0x001E3AEA)

ClearEventFlag

0x4041     written to 0x40030F00               (@0x001E3B00)
0x2000     written to 0x40030404               (@0x001E3B0A)

WaitForEventFlag

-> interrupt -> if ok set event flag pattern=5

            read from 0x40030F00 : 0x4041      (@0x001E36D8)
0x0041     written to 0x40030F00               (@0x001E3702)
            read from 0x40030F20 : 0x4001      (@0x001E3708)
0x0001     written to 0x40030F20               (@0x001E3730)
            read from 0x40030404 : 0x2000      (@0x001E3736)
0x2800     written to 0x40030404               (@0x001E373E)
0x0000     written to 0x40030400               (@0x001E3746)
            read from 0x4003040A : 0x0000      (@0x001E374C)
0x8000     written to 0x4003040A               (@0x001E3754)

*/

// helper
/*
 coderat: attempt to extend class ByteArrayInputStream failed, because
          it hardly rely on "byte[] buf" that we do not have here. So
          add a new input stream class. Another possibility would be to
          implement directly subclass of ImageInputStreamImpl.
*/
class DebuggableMemoryInputStream extends java.io.InputStream {
    private DebuggableMemory memory;
    private int size,addr;
    
    protected int pos = 0;
    protected int mark = 0;
    
    DebuggableMemoryInputStream(int addr, int size,DebuggableMemory memory) {
        this.addr = addr;
        this.size = size;
        this.memory = memory;
    }

    public synchronized int available()  {
        return size - pos;
    }
 
    public synchronized int read() {
        if (pos>=size)
            return -1;
        return memory.loadUnsigned8(addr + (pos++), DebuggableMemory.AccessSource.IMGA);
    }

    public synchronized int read(byte[] b) {
        // from oracle doc
        if (b.length==0)
            return 0;
        return this.read(b,0,b.length);
    }
    
    public synchronized int read(byte[] b, int off, int len) {
        // from oracle doc
        if (b == null) {
            throw new NullPointerException();
        }
        // from oracle doc
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (pos >= size) {
            return -1;
        }
        if (len > size-pos) {
            len = size - pos;
        }
        if (len>0) {
            // TODO optimize to use .loadBlock()
            for (int i=0;i<len;i++) {
                b[off++] = (byte)memory.loadUnsigned8(addr + (pos++), DebuggableMemory.AccessSource.IMGA);
            }
        }
        return len;
    }

    public synchronized void reset() {
        pos = mark;
    }
    
    public synchronized long skip(long n) {
        if (n<=0)
            return 0;
        if (n > size-pos) {
            n = size-pos;
        }
        pos += n;
        return n;
    }
    
    public synchronized void mark(int readlimit) {
        mark = pos;
    }
    
    public boolean markSupported() {
        return true;
    }
 
    public void close() {
    }
    
    public void rewind() {
        pos = mark = 0;
    }
}

public class FrJpegCodec implements JpegCodec {
    private int codecNumber;
    private Platform platform;
    
    private int addrJpeg, addrY, addrCb, addrCr;

    // number of 512B pages
    private int sizeJpeg;
    
    private int widthY, widthCbCr;
    private int reg400,reg402;
    
    private int command;
    
    private DebuggableMemoryInputStream stream = null;
    private BufferedImage image = null;
    
    private int outputWidth, outputHeight;
    private byte interruptStatus, errorCode, reg000;
    private int transferInterruptStatus;
    
    public FrJpegCodec(int codecNumber, Platform platform) {
        this.codecNumber = codecNumber;
        this.platform = platform;
    }
    
    public void setSrcAddrJpeg(int value) {
        if ((value & 0xF)!=0)
            throw new RuntimeException("JpegCodec (" + codecNumber +"): JPEG buffer address is not aligned to 16");
        addrJpeg = value;
    }
    
    public int getSrcAddrJpeg() {
        return addrJpeg;
    }

    public void setDstAddrY(int value) {
        if ((value & 0xF)!=0)
            throw new RuntimeException("JpegCodec (" + codecNumber +"): Y buffer address is not aligned to 16");
        addrY = value;
    }
    
    public int getDstAddrY() {
        return addrY;
    }

    public void setDstAddrCb(int value) {
        if ((value & 0xF)!=0)
            throw new RuntimeException("JpegCodec (" + codecNumber +"): U buffer address is not aligned to 16");
        addrCb = value;
    }
    
    public int getDstAddrCb() {
        return addrCb;
    }

    public void setDstAddrCr(int value) {
        if ((value & 0xF)!=0)
            throw new RuntimeException("JpegCodec (" + codecNumber +"): V buffer address is not aligned to 16");
        addrCr = value;
    }

    public int getDstAddrCr() {
        return addrCr;
    }
    
    public void setReg400(int value) {
        reg400 = value;
        // coderat: no idea why, but it resolves loop waiting for this
        if ((value&0x4000)!=0) {
            reg400 |= 0x8000;
        }
    }

    public int getReg400() {
        return reg400;
    }
    
    public void setReg402(int value) {
        // coderat: no idea why, but it resolves loop in interrupt
        if ((value&0x80)!=0 && command==0x2000) {
            transferInterruptStatus = 0x8000;
            platform.getSharedInterruptCircuit().request(FrInterruptController.IMAGE_28_SHARED_REQUEST_NR, 29-codecNumber);
        }
        reg402 = value & (~0x80);
    }

    public int getReg402() {
        return reg402;
    }

    public void setReg000(byte value) {
        reg000 = value;
    }

    public byte getReg000() {
        return reg000;
    }

    public void setRegUnimplemented(int value) {
    }

    public int getRegUnimplemented() {
        return 0;
    }

    public void setSizeJpeg(int value) {
        sizeJpeg = value;
    }

    public int getSizeJpeg() {
        return sizeJpeg;
    }

    public void setYWidth(int value) {
        if ((value & 0x1F)!=0)
            throw new RuntimeException("JpegCodec (" + codecNumber +"): Y row width not aligned to 32");
        widthY = value;
    }

    public int getYWidth() {
        return widthY;
    }

    public void setCbCrWidth(int value) {
        if ((value & 0x1F)!=0)
            throw new RuntimeException("JpegCodec (" + codecNumber +"): U(V) row width not aligned to 32");
        widthCbCr = value;
    }

    public int getCbCrWidth() {
        return widthCbCr;
    }

    public void setCommand(int value) {
        command = value;
        if ((value&0x800)!=0) {
            // TODO cancel operation
        } else {
            switch (value) {
                case 0x8000:    // decode JPEG header, report errors or width and height
                    if (loadImage()) {
                        interruptStatus = 8;
                        reg000 |= 1;
                    } else {
                        System.out.println("JpegCodec (" + codecNumber + "): decoding header failed");
                        interruptStatus = 0x20;
                        errorCode = 1;
                    }
                    reg402 |= 0x80;
                    platform.getSharedInterruptCircuit().request(FrInterruptController.IMAGE_28_SHARED_REQUEST_NR, 29-codecNumber);
                    break;
                case 0x2000:    // transfer decoded JPEG data
                    if (image==null)
                        throw new RuntimeException("JpegCodec (" + codecNumber +"): not possible to transfer data");
                    if (convertRgbToYCbCr422(image)) {
                        // first must come interrupt for setting event pattern=1
                        interruptStatus = (byte)0xC0;

                    // afterwards must come interrupt to set event pattern=4
                    } else {
                        System.out.println("JpegCodec (" + codecNumber + "): transfer pixels failed");
                        interruptStatus = 0x20;
                        errorCode = 1;
                    }
                    reg402 |= 0x80;
                    platform.getSharedInterruptCircuit().request(FrInterruptController.IMAGE_28_SHARED_REQUEST_NR, 29-codecNumber);
                    // important: dispose and remove image
                    image.flush();
                    image=null;
                    break;
                default:
                    System.out.println("JpegCodec (" + codecNumber + "): unsupported command 0x" + Format.asHex(value, 4));
            }
        }
    }

    public int getCommand() {
        return command;
    }
    
    public byte getJPEGWidthHi() {
        if (image==null)
            return 0;
        return (byte)((image.getWidth()>>8)&0xFF);
    }
    
    public byte getJPEGWidthLo() {
        if (image==null)
            return 0;
        return (byte)(image.getWidth()&0xFF);
    }
    
    public byte getJPEGHeightHi() {
        if (image==null)
            return 0;
        return (byte)((image.getHeight()>>8)&0xFF);
    }
    
    public byte getJPEGHeightLo() {
        if (image==null)
            return 0;
        return (byte)(image.getHeight()&0xFF);
    }

    public void setOutputWidth(int value) {
        if ((value & 0xF)!=0)
            throw new RuntimeException("JpegCodec (" + codecNumber +"): output width not aligned to 16");
        outputWidth = value;
    }

    public int getOutputWidth() {
        return outputWidth;
    }

    public void setOutputHeight(int value) {
        if ((value & 7)!=0)
            throw new RuntimeException("JpegCodec (" + codecNumber +"): output height not aligned to 8");
        outputHeight = value;
    }

    public int getOutputHeight() {
        return outputHeight;
    }
    
    public void setInterruptStatus (byte value) {
        interruptStatus &= value;
        if (interruptStatus==0 && transferInterruptStatus==0)
            platform.getSharedInterruptCircuit().removeRequest(FrInterruptController.IMAGE_28_SHARED_REQUEST_NR, 29-codecNumber);
    }

    public byte getInterruptStatus() {
        return interruptStatus;
    }
    
    public void setTransferInterruptStatus (int value) {
        transferInterruptStatus &= (~value);
        if (interruptStatus==0 && transferInterruptStatus==0)
            platform.getSharedInterruptCircuit().removeRequest(FrInterruptController.IMAGE_28_SHARED_REQUEST_NR, 29-codecNumber);
    }

    public int getTransferInterruptStatus() {
        return transferInterruptStatus;
    }

    public void setErrorCode (byte value) {
        errorCode = value;
    }

    public byte getErrorCode() {
        return errorCode;
    }

    public boolean loadImage() {
        /* Method 3 */
        stream = new DebuggableMemoryInputStream(addrJpeg,sizeJpeg*512,platform.getMemory());

        // get only JPEG readers: we do not want to read other image types!
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpg");
        
        if (readers.hasNext()) {
            try {
                ImageInputStream iis = ImageIO.createImageInputStream(stream);
    
                // try each reader and find one
                while (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    ImageReadParam param = reader.getDefaultReadParam();
                    try {
                        reader.setInput(iis, true, true);
                        image = reader.read(0, param);
                        // if found
                        if (image!=null)
                            break;
                    } catch (java.io.IOException e) {
                        // ignore and try next reader
                    } finally {
                        // absolutely necessary
                        reader.dispose();
                    }
                    stream.rewind();
                }
                iis.close();
            } catch (java.io.IOException e) {
                // ImageInputStream create/close can also throw, so catch
            }
        }
		if (image==null) {
		    // this is not exception case
		    return false;
		}
        if (image.getAlphaRaster() != null) {
            throw new RuntimeException("Alpha channel is not allowed in JPEG");
        }

        final int width = image.getWidth();
        final int height = image.getHeight();

		if (width<=0 || width>0xFFFF || (width&1)!=0 || height<=0 || height>0xFFFF){
            throw new RuntimeException("invalid image properties");
        }
        return true;
    }
    /*
     calculate Y4 for YCbCr444 and YCbCr422
     float used for better performance
     */
    private static final int getY(int r, int g, int b) {
        /* NOTE: using original long coefficients from http://en.wikipedia.org/wiki/YCbCr#JPEG_conversion 
           seems to bring no difference.

           use canonical calculation formula here, because result is very close to original */
        return Math.round(0.299f * r + 0.587f * g + 0.114f * b);
    }

    /*
     calculate Cb2 for YCbCr422
     float used for better performance
     */
    private static final int getCb2(int r1, int g1, int b1,int r2, int g2, int b2) {
        /* NOTE: using original long coefficients from http://en.wikipedia.org/wiki/YCbCr#JPEG_conversion 
           seems to bring no difference.

           use canonical calculation formula here, because result is very close to original */
        return Math.round((-0.169f * (r1+r2) - 0.331f * (g1+g2) + 0.5f * (b1 + b2))/2.f) + 128;
    }

    /*
     calculate Cr2 for YCbCr422
     float used for better performance
     */
    private static final int getCr2(int r1, int g1, int b1,int r2, int g2, int b2) {
        /* NOTE: using original long coefficients from http://en.wikipedia.org/wiki/YCbCr#JPEG_conversion 
           seems to bring no difference.

           use canonical calculation formula here, because result is very close to original */
        return Math.round((0.5f * (r1+r2) - 0.419f * (g1+g2) - 0.081f * (b1 + b2))/2.f) + 128;
    }

    private boolean convertRgbToYCbCr422(BufferedImage image) {
        DebuggableMemory memory = platform.getMemory();
        
        if (image==null)
            return false;

        final int width = image.getWidth();
        final int height = image.getHeight();

        // set/check output width/height
        if (((width+0xF)& (~0xF))!= outputWidth || ((height+7)&(~7))!= outputHeight) {
            // TODO
            throw new RuntimeException("JpegCodec (" + codecNumber +"): output width/height not supported");
        }

        // use byte array for performance reason
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        final int addY = widthY - width;
        final int addCbCr = widthCbCr - (width>>1);
        int offsetY = addrY;
        int offsetCb = addrCb;
        int offsetCr = addrCr;
        
        // create output data and obey alignment
        for (int pixel = 0; pixel < pixels.length; offsetY += addY, offsetCb += addCbCr, offsetCr += addCbCr) {
            for (int i=0; i< width; i+=2, pixel += 6, offsetY+=2) {
                /* coderat: this implementation was selected after performance tests
                   I use canonical calculation formula, because it is closest to original
                   But still 1-2 values out of 256 differs from original result (are rounded higher)
                   TODO: investigate why still some bytes different
                 */

                // coderat: do not use any standard Java methods, because they are too slowly
                final int b1 = ((int)pixels[pixel] & 0xFF);
                final int g1 = ((int)pixels[pixel+1] & 0xFF);
                final int r1 = ((int)pixels[pixel+2] & 0xFF);
                final int b2 = ((int)pixels[pixel+3] & 0xFF);
                final int g2 = ((int)pixels[pixel+4] & 0xFF);
                final int r2 = ((int)pixels[pixel+5] & 0xFF);
    
                // TODO optimize to use .storeBlock(), because can be up to 16Mpix
                // coderat: no clamp is need here, because conversion formules RGB->YCbCr are already biased
                memory.store16(offsetY, (getY(r1, g1, b1)<<8)|getY(r2, g2, b2), DebuggableMemory.AccessSource.IMGA);
                memory.store8(offsetCb++, getCb2(r1, g1, b1, r2, g2, b2), DebuggableMemory.AccessSource.IMGA);
                memory.store8(offsetCr++, getCr2(r1, g1, b1, r2, g2, b2), DebuggableMemory.AccessSource.IMGA);
            }
        }
        return true;
    }
}
