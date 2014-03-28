package com.nikonhacker.encoding;

import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NkldUtils {

    private static final int[] tab0 = {
            0xc1,0xbf,0x6d,0x0d,0x59,0xc5,0x13,0x9d,0x83,0x61,0x6b,0x4f,0xc7,0x7f,0x3d,0x3d,
			0x53,0x59,0xe3,0xc7,0xe9,0x2f,0x95,0xa7,0x95,0x1f,0xdf,0x7f,0x2b,0x29,0xc7,0x0d,
			0xdf,0x07,0xef,0x71,0x89,0x3d,0x13,0x3d,0x3b,0x13,0xfb,0x0d,0x89,0xc1,0x65,0x1f,
			0xb3,0x0d,0x6b,0x29,0xe3,0xfb,0xef,0xa3,0x6b,0x47,0x7f,0x95,0x35,0xa7,0x47,0x4f,
			0xc7,0xf1,0x59,0x95,0x35,0x11,0x29,0x61,0xf1,0x3d,0xb3,0x2b,0x0d,0x43,0x89,0xc1,
			0x9d,0x9d,0x89,0x65,0xf1,0xe9,0xdf,0xbf,0x3d,0x7f,0x53,0x97,0xe5,0xe9,0x95,0x17,
			0x1d,0x3d,0x8b,0xfb,0xc7,0xe3,0x67,0xa7,0x07,0xf1,0x71,0xa7,0x53,0xb5,0x29,0x89,
			0xe5,0x2b,0xa7,0x17,0x29,0xe9,0x4f,0xc5,0x65,0x6d,0x6b,0xef,0x0d,0x89,0x49,0x2f,
			0xb3,0x43,0x53,0x65,0x1d,0x49,0xa3,0x13,0x89,0x59,0xef,0x6b,0xef,0x65,0x1d,0x0b,
			0x59,0x13,0xe3,0x4f,0x9d,0xb3,0x29,0x43,0x2b,0x07,0x1d,0x95,0x59,0x59,0x47,0xfb,
			0xe5,0xe9,0x61,0x47,0x2f,0x35,0x7f,0x17,0x7f,0xef,0x7f,0x95,0x95,0x71,0xd3,0xa3,
			0x0b,0x71,0xa3,0xad,0x0b,0x3b,0xb5,0xfb,0xa3,0xbf,0x4f,0x83,0x1d,0xad,0xe9,0x2f,
			0x71,0x65,0xa3,0xe5,0x07,0x35,0x3d,0x0d,0xb5,0xe9,0xe5,0x47,0x3b,0x9d,0xef,0x35,
			0xa3,0xbf,0xb3,0xdf,0x53,0xd3,0x97,0x53,0x49,0x71,0x07,0x35,0x61,0x71,0x2f,0x43,
			0x2f,0x11,0xdf,0x17,0x97,0xfb,0x95,0x3b,0x7f,0x6b,0xd3,0x25,0xbf,0xad,0xc7,0xc5,
			0xc5,0xb5,0x8b,0xef,0x2f,0xd3,0x07,0x6b,0x25,0x49,0x95,0x25,0x49,0x6d,0x71,0xc7 };

    private static final int[] tab1 = {
            0xa7,0xbc,0xc9,0xad,0x91,0xdf,0x85,0xe5,0xd4,0x78,0xd5,0x17,0x46,0x7c,0x29,0x4c,
			0x4d,0x03,0xe9,0x25,0x68,0x11,0x86,0xb3,0xbd,0xf7,0x6f,0x61,0x22,0xa2,0x26,0x34,
			0x2a,0xbe,0x1e,0x46,0x14,0x68,0x9d,0x44,0x18,0xc2,0x40,0xf4,0x7e,0x5f,0x1b,0xad,
			0x0b,0x94,0xb6,0x67,0xb4,0x0b,0xe1,0xea,0x95,0x9c,0x66,0xdc,0xe7,0x5d,0x6c,0x05,
			0xda,0xd5,0xdf,0x7a,0xef,0xf6,0xdb,0x1f,0x82,0x4c,0xc0,0x68,0x47,0xa1,0xbd,0xee,
			0x39,0x50,0x56,0x4a,0xdd,0xdf,0xa5,0xf8,0xc6,0xda,0xca,0x90,0xca,0x01,0x42,0x9d,
			0x8b,0x0c,0x73,0x43,0x75,0x05,0x94,0xde,0x24,0xb3,0x80,0x34,0xe5,0x2c,0xdc,0x9b,
			0x3f,0xca,0x33,0x45,0xd0,0xdb,0x5f,0xf5,0x52,0xc3,0x21,0xda,0xe2,0x22,0x72,0x6b,
			0x3e,0xd0,0x5b,0xa8,0x87,0x8c,0x06,0x5d,0x0f,0xdd,0x09,0x19,0x93,0xd0,0xb9,0xfc,
			0x8b,0x0f,0x84,0x60,0x33,0x1c,0x9b,0x45,0xf1,0xf0,0xa3,0x94,0x3a,0x12,0x77,0x33,
			0x4d,0x44,0x78,0x28,0x3c,0x9e,0xfd,0x65,0x57,0x16,0x94,0x6b,0xfb,0x59,0xd0,0xc8,
			0x22,0x36,0xdb,0xd2,0x63,0x98,0x43,0xa1,0x04,0x87,0x86,0xf7,0xa6,0x26,0xbb,0xd6,
			0x59,0x4d,0xbf,0x6a,0x2e,0xaa,0x2b,0xef,0xe6,0x78,0xb6,0x4e,0xe0,0x2f,0xdc,0x7c,
			0xbe,0x57,0x19,0x32,0x7e,0x2a,0xd0,0xb8,0xba,0x29,0x00,0x3c,0x52,0x7d,0xa8,0x49,
			0x3b,0x2d,0xeb,0x25,0x49,0xfa,0xa3,0xaa,0x39,0xa7,0xc5,0xa7,0x50,0x11,0x36,0xfb,
			0xc6,0x67,0x4a,0xf5,0xa5,0x12,0x65,0x7e,0xb0,0xdf,0xaf,0x4e,0xb3,0x61,0x7f,0x2f };


    private static final int[] tab2 = {
            0x00,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x0C,0x0C,0x0C,0x0C,0x0C,0x08,
            0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,
            0x08,0x05,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,
            0x02,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x02,0x02,0x02,0x02,0x02,
            0x02,0x02,0x90,0x90,0x90,0x90,0x90,0x90,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,
            0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x02,0x02,0x02,0x02,
            0x02,0x02,0x50,0x50,0x50,0x50,0x50,0x50,0x40,0x40,0x40,0x40,0x40,0x40,0x40,0x40,
            0x40,0x40,0x40,0x40,0x40,0x40,0x40,0x40,0x40,0x40,0x40,0x40,0x02,0x02,0x02,0x02,
            0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00 };

    private static final int CRC_MASK = 0x1021;
    private static final int CRC_INIT_FILE = 0x0000;

    public static byte[] load(File f) throws IOException {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(f);
            int count = (int) f.length();

            byte[] data = new byte[count];
            fis.read(data);

            return data;
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void dumpBytesAsString(byte[] dec) {
        String out = "";
        for (byte b : dec) {
            out += (char)b;
        }
        System.out.println(out);
    }


    public static void compareFiles(File aDst, File refFolder) throws IOException {
        if (FileUtils.contentEquals(aDst, new File(refFolder, aDst.getName()))) {
            System.out.println(aDst.getAbsolutePath() + " is OK !!!!!!!!!");
        }
        else {
            System.out.println(aDst.getAbsolutePath() + " is not OK :-(");
        }
    }

    private static final int init_cj(int v)
    {
        final int b0 = v & 0xFF;
        final int b1 = (v>>8) & 0xFF;
        final int b2 = (v>>16) & 0xFF;
        final int b3 = (v>>24) & 0xFF;

        return tab1[b0 ^ b1 ^ b2 ^ b3];
    }

    private static final int init_ci(byte[] s)
    {
        int n = 0;
        for (int ch : s) {
            int b = ch;

            if ((tab2[b+1] & 0x20) != 0) {
                b = b - 0x30;
            } else {
                b = b % 10;
            }
            n = (n * 10) + b;
        }

        return tab0[n & 0xff];
    }

    protected static final void decryptData(int iv, byte[] key, byte[] data, int offset, int size) throws IOException {
        int r9 = init_cj(iv);
        final int r5 = init_ci(key);

        int r4 = r5 * 0x260;

        for (int i = 0; i < size; i++,offset++)
        {
            r9 += r4;
            r4 += r5;
            data[offset] ^= (byte)r9;
        }
    }

    public static void decrypt(byte[] data, int offset, int size) throws IOException {
        final int v = 0xB401C81B;
        final byte[] s= { 'N','C','D','S','L','R' };
        decryptData(v, s, data, offset, size);
    }

    public static final NkldHeader getNkldHeader (byte[] data) throws FirmwareFormatException {
        NkldHeader nkldHeader = new NkldHeader();

        nkldHeader.dataOffset = getUInt16(data,0);
        nkldHeader.totalLength = getUInt16(data,2);
        nkldHeader.majorVersion = data[4];
        nkldHeader.minorVersion = data[5];
        nkldHeader.entryCount = getUInt16(data,6);
        nkldHeader.magic = getUInt32(data,8);
        nkldHeader.dataLength = getUInt16(data,12);
        nkldHeader.crcPadding = getUInt16(data,14);

        if (nkldHeader.magic != 0x87C7CAAC || nkldHeader.majorVersion != 1)
            System.err.println("Warning : unsupported magic and version");

        if ( (nkldHeader.dataOffset + nkldHeader.dataLength) > nkldHeader.totalLength ||
            nkldHeader.totalLength>data.length) {
            throw new FirmwareFormatException("Bad NKLD header totalLength/dataOffset/dataLength");
        }
        return nkldHeader;
    }
    /**
     * Optimized implementation using Jacksum.
     * Just faster.
     *
     * @throws NoSuchAlgorithmException
     */
    public static int computeChecksum(byte[] buffer, int offset, int length) throws NoSuchAlgorithmException {
        AbstractChecksum checksum;
        checksum = JacksumAPI.getChecksumInstance("crc:16," + Integer.toHexString(CRC_MASK) + "," + Integer.toHexString(CRC_INIT_FILE) + ",false,false,0");

        checksum.reset();

        checksum.update(buffer, offset, length);

        return (int) checksum.getValue();
    }

    private static void assertZero32(byte[] buffer, int offset) {
        long zero = getUInt32(buffer, offset);
        if (zero != 0) {
            System.err.println("Warning : 0x00000000 was expected at 0x" + Integer.toHexString(offset));
        }
    }

    private static void assertZero16(byte[] buffer, int offset) {
        int zero = getUInt16(buffer, offset);
        if (zero != 0) {
            System.err.println("Warning : 0x0000 was expected at 0x" + Integer.toHexString(offset));
        }
    }

    public static void dumpFile(File file, byte[] buffer, int offset, int length) throws IOException {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            fos.write(buffer, offset, length);
        }
        finally
        {
            if (fos != null) fos.close();
        }
    }

    protected static int getUInt16(byte[] buffer, int offset) {
        final int v0 = buffer[offset] & 0xFF;
        final int v1 = buffer[offset + 1] & 0xFF;
        return ((v0 << 8) | v1);
    }

    private static void setUInt16(byte[] buffer, int offset, int value) {
        buffer[offset + 1] = (byte) (value & 0xff);
        value = value >> 8;
        buffer[offset] = (byte) (value & 0xff);
    }

    protected static int getUInt32(byte[] buffer, int offset) {
        final int v0 = buffer[offset] & 0xFF;
        final int v1 = buffer[offset + 1] & 0xFF;
        final int v2 = buffer[offset + 2] & 0xFF;
        final int v3 = buffer[offset + 3] & 0xFF;
        return ((v0 << 24) | (v1 << 16) | (v2 << 8) | v3);
    }

    private static void setUInt32(byte[] buffer, int offset, int value) {
        for (int i = 0; i < 4; i++) {
            buffer[offset + (3 - i)] = (byte) (value & 0xff);
            value = value >> 8;
        }
    }

    protected static String getString(byte[] buffer, int offset, int length) {
        StringBuilder b = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            b.append((char) buffer[offset + i]);
        }
        return b.toString();
    }

    private static void setString(byte[] out, int offset, String s, int fieldLength) throws IOException {
        if (s.length() > fieldLength) {
            throw new IOException("Cannot store file '" + s + "' : name is longer than 16 chars");
        }
        for (int i = 0; i < fieldLength; i++) {
            if (i < s.length()) {
                out[offset + i] = (byte) s.charAt(i);
            }
            else {
                out[offset + i] = 0;
            }
        }
    }
}
