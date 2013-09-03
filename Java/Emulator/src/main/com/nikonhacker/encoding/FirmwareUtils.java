package com.nikonhacker.encoding;

import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FirmwareUtils {
    private static int[] Xor_Ord1 = {
            0xE8, 0xFE, 0x46, 0xE3, 0x7D, 0xAB, 0x5C, 0xB9, 0xA5, 0x53, 0xC4, 0xC7, 0x32, 0xCD, 0x9C, 0xED,
            0x94, 0x67, 0x4E, 0x5B, 0x48, 0x3A, 0x52, 0xB6, 0x34, 0xF7, 0x8E, 0x12, 0x90, 0x98, 0x82, 0x3C,
            0x09, 0x2B, 0x68, 0xA8, 0x24, 0xD5, 0x10, 0x9D, 0x9A, 0xD9, 0xA9, 0x7F, 0x50, 0x4B, 0xDD, 0x9E,
            0x62, 0x1C, 0x6A, 0x64, 0x02, 0x11, 0xDA, 0x4A, 0x6E, 0x35, 0xBD, 0xA0, 0xB1, 0xD7, 0xDE, 0x83,
            0x5D, 0xE5, 0x5E, 0xCC, 0xDB, 0xAC, 0x18, 0xE2, 0x25, 0x69, 0x07, 0xBC, 0x39, 0x97, 0x14, 0xEB,
            0xB2, 0x73, 0x36, 0x8A, 0x99, 0xB8, 0x1E, 0x2E, 0xEF, 0x93, 0xBA, 0xEE, 0xF5, 0xC5, 0x7B, 0x74,
            0x8D, 0xE1, 0xC3, 0x4F, 0x41, 0x42, 0x6C, 0xE6, 0xA2, 0x06, 0x6F, 0x85, 0x2A, 0x2F, 0x1B, 0x38,
            0x08, 0xAF, 0x44, 0x00, 0x33, 0x63, 0x91, 0x22, 0x87, 0x70, 0xB0, 0x43, 0xB5, 0x66, 0xE0, 0xFF,
            0x30, 0xAD, 0x8F, 0xC1, 0xF4, 0xEA, 0xF9, 0xF6, 0x51, 0xD6, 0xD4, 0x3E, 0x04, 0x72, 0x3D, 0x54,
            0x78, 0xC0, 0x7E, 0x26, 0xFA, 0x56, 0x58, 0xC9, 0x55, 0xC8, 0xA6, 0x16, 0x23, 0x84, 0xB7, 0xCB,
            0x45, 0x7C, 0xD8, 0x7A, 0x27, 0x2D, 0xCA, 0x03, 0x3F, 0x17, 0x0B, 0x57, 0xBB, 0x3B, 0xF0, 0x49,
            0x1F, 0xD1, 0x86, 0x80, 0x95, 0x20, 0x6B, 0xC6, 0xBF, 0xAA, 0x79, 0xD2, 0x75, 0xCF, 0xAE, 0xD0,
            0x5F, 0xF1, 0x61, 0xF3, 0xFB, 0xCE, 0x29, 0x65, 0x0F, 0x31, 0x2C, 0xFD, 0x76, 0x0C, 0x4C, 0x4D,
            0x60, 0xF8, 0x88, 0x6D, 0xA4, 0xEC, 0x9B, 0x92, 0x47, 0xA1, 0xE4, 0x21, 0xFC, 0x81, 0xA7, 0xC2,
            0x0E, 0xA3, 0x40, 0x0D, 0x8B, 0x59, 0x96, 0x37, 0xE7, 0xF2, 0x77, 0x1D, 0x28, 0x0A, 0x8C, 0x5A,
            0x15, 0x9F, 0x01, 0xB3, 0xD3, 0xBE, 0xB4, 0x1A, 0x89, 0xE9, 0x13, 0x05, 0xDF, 0xDC, 0x71, 0x19,
    };

    private static int[] Xor_Ord2 = {
            0x76, 0x0F, 0x43, 0xD9, 0xDB, 0xDC, 0x9B, 0x49, 0x4E, 0x42, 0xB7, 0x9F, 0xEC, 0x55, 0x19, 0x11,
            0x58, 0x23, 0x69, 0xA2, 0xB8, 0x68, 0xE8, 0x2B, 0x91, 0xF3, 0x1A, 0x34, 0xED, 0x0A, 0x06, 0x89,
            0xB2, 0x79, 0x2A, 0xC8, 0xEE, 0xA3, 0xB5, 0xD0, 0xFD, 0x17, 0xF9, 0xCE, 0x74, 0x39, 0x47, 0xC5,
            0xC1, 0x5D, 0x86, 0x7F, 0x6A, 0xAB, 0xE5, 0xF5, 0xC9, 0x96, 0x71, 0x1C, 0x09, 0x25, 0xD3, 0x8C,
            0x0C, 0x02, 0xB1, 0x48, 0x7C, 0x46, 0x3E, 0x08, 0x7B, 0x01, 0x54, 0x6B, 0xB9, 0x4F, 0xCD, 0xF1,
            0x51, 0x50, 0x59, 0xA4, 0xA7, 0x6C, 0x3F, 0xB6, 0x9D, 0xBC, 0x4C, 0x9E, 0x16, 0x37, 0xA1, 0xC0,
            0x6E, 0xE2, 0xDA, 0x3D, 0x22, 0xCB, 0xE7, 0x5B, 0x98, 0x53, 0x92, 0x36, 0x90, 0xAC, 0x31, 0x24,
            0x21, 0xC6, 0x63, 0x35, 0xB4, 0x5C, 0x1F, 0x77, 0x4A, 0xE4, 0x0D, 0x13, 0x8D, 0xC7, 0x99, 0x7E,
            0x81, 0x3C, 0x60, 0x28, 0xF0, 0xBF, 0x82, 0x2C, 0x78, 0x7A, 0x5F, 0x93, 0x84, 0x70, 0xEA, 0x9A,
            0x8E, 0xD2, 0x27, 0xE0, 0xCF, 0x6D, 0x10, 0x9C, 0x56, 0x07, 0x12, 0xFA, 0x26, 0x97, 0x80, 0xE3,
            0xE1, 0x61, 0x8A, 0x75, 0xA9, 0x5A, 0xDE, 0x1E, 0x5E, 0x4D, 0x66, 0x0E, 0xBA, 0x4B, 0x20, 0x40,
            0xA8, 0x8F, 0x52, 0x7D, 0xDF, 0xE6, 0xAF, 0x6F, 0xBE, 0xFC, 0x94, 0xA0, 0x3A, 0x33, 0x45, 0x14,
            0x62, 0x00, 0x87, 0xAE, 0xB3, 0x8B, 0xD4, 0xCA, 0xFF, 0xE9, 0x04, 0x88, 0xCC, 0x41, 0xD7, 0xD6,
            0xBB, 0x95, 0x32, 0x18, 0xF8, 0x72, 0x65, 0x3B, 0x29, 0xAD, 0x44, 0x1B, 0xC2, 0xD8, 0x1D, 0xA5,
            0xDD, 0x67, 0xD5, 0x30, 0xA6, 0xEF, 0x2F, 0xF2, 0x83, 0x2D, 0x03, 0xBD, 0x15, 0x2E, 0xD1, 0x73,
            0x57, 0xAA, 0xFB, 0x85, 0xF4, 0x64, 0x0B, 0xC4, 0xF7, 0xEB, 0x38, 0xC3, 0xF6, 0x05, 0xFE, 0xB0,
    };

    private static int[] Xor_Ord3_Eye = {
            /*0*/ /*1*/ /*2*/ /*3*/ /*4*/ /*5*/ /*6*/ /*7*/ /*8*/ /*9*/ /*a*/ /*b*/ /*c*/ /*d*/ /*e*/ /*f*/
            /*00*/  0x00, 0x25, 0x06, 0xbb, 0xe3, 0x82, 0x70, 0xce, 0x86, 0xfb, 0x100, 0x3a, 0xf8, 0xbf, 0x76, 0xf5, /*00*/ // 0x0a is 0x00, therefore put 0x100.
            /*10*/  0xc3, 0xf6, 0x9d, 0x9e, 0x10, 0x00, 0xfc, 0x9c, 0xdd, 0x12, 0x46, 0x93, 0x4f, 0xa6, 0xad, 0x68, /*10*/
            /*20*/  0x94, 0xb8, 0x22, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xa9, /*20*/
            /*30*/  0x3e, 0x00, 0x00, 0x00, 0x00, 0xaa, 0xba, 0xb2, 0xd0, 0x79, 0xa5, 0x00, 0x00, 0x6a, 0x00, 0xd4, /*30*/
            /*40*/  0x83, 0x55, 0x87, 0x9b, 0x5c, 0x27, 0xab, 0x77, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*40*/
            /*50*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*50*/
            /*60*/  0x00, 0x00, 0xcb, 0x71, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*60*/
            /*70*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xbe, 0x37, 0x90, 0xe8, 0x3b, 0x92, /*70*/
            /*80*/  0x35, 0x4b, 0xd2, 0xd7, 0xd8, 0x0e, 0x51, 0x8b, 0xb0, 0xec, 0x4c, 0xb9, 0x59, 0x2e, 0x66, 0x54, /*80*/
            /*90*/  0xb5, 0xd1, 0x00, 0x1e, 0x0f, 0x5a, 0xc4, 0xcd, 0xeb, 0x4e, 0x7c, 0x74, 0x8e, 0xfa, 0x81, 0xe2, /*90*/
            /*A0*/  0x7f, 0x9a, 0xa8, 0x5f, 0x32, 0x89, 0x98, 0x07, 0x39, 0xc0, 0x38, 0x1f, 0x01, 0xe0, 0x8d, 0x1d, /*A0*/
            /*B0*/  0xf3, 0x96, 0x57, 0xa0, 0x0b, 0xed, 0x09, 0x18, 0x8c, 0xf4, 0x0d, 0x21, 0xd9, 0x23, 0x78, 0xa1, /*B0*/
            /*C0*/  0xc1, 0xfe, 0x3c, 0x30, 0x73, 0x5d, 0x40, 0x99, 0xb6, 0x6c, 0x7b, 0x14, 0xca, 0xc2, 0xe6, 0x8a, /*C0*/
            /*D0*/  0xe9, 0xae, 0x7a, 0xd6, 0xf1, 0x3d, 0xa4, 0x34, 0x2b, 0xda, 0x63, 0x84, 0xb3, 0xaf, 0x88, 0xb7, /*D0*/
            /*E0*/  0x45, 0x97, 0xdc, 0x20, 0x17, 0x3f, 0x4a, 0x52, 0x03, 0x33, 0xdf, 0xd3, 0x04, 0x69, 0x91, 0xf0, /*E0*/
            /*F0*/  0xf9, 0xcc, 0x50, 0x44, 0xff, 0x80, 0x58, 0xf7, 0xc9, 0x60, 0x6b, 0x53, 0xa7, 0x85, 0xee, 0x1c, /*F0*/
    };

    private static int[] fixKnownTRUE = {
            /*00*/  0x2f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*00*/
            /*10*/  0x00, 0x00, 0x00, 0x00, 0x00, 0xea, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*10*/
            /*20*/  0x00, 0x00, 0x00, 0xe1, 0x62, 0x5b, 0xcf, 0x67, 0x00, 0xe7, 0x48, 0x0c, 0x7e, 0x7d, 0xb1, 0x00, /*20*/
            /*30*/  0x00, 0x1b, 0xc5, 0x0a, 0x11, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xdb, 0xc6, 0x00, 0xa3, 0x00, /*30*/
            /*40*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x36, 0x42, 0x61, 0x2d, 0xb4, 0x4d, 0x1a, 0x9f, /*40*/
            /*50*/  0x64, 0x8f, 0xd5, 0x43, 0x05, 0x24, 0x72, 0x02, 0x41, 0x15, 0x6d, 0x6e, 0x47, 0x26, 0x65, 0x13, /*50*/
            /*60*/  0x49, 0x95, 0x00, 0x00, 0xef, 0x6f, 0x2c, 0xbd, 0xfd, 0xac, 0x31, 0xde, 0x2a, 0x5e, 0x29, 0xa2, /*60*/
            /*70*/  0xbc, 0x28, 0xc7, 0x56, 0xc8, 0x16, 0x75, 0xe4, 0x19, 0xf2, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*70*/
            /*80*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*80*/
            /*90*/  0x00, 0x00, 0xe5, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*90*/
            /*A0*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*A0*/
            /*B0*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*B0*/
            /*C0*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*C0*/
            /*D0*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*D0*/
            /*E0*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*E0*/
            /*F0*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /*F0*/
    };

    private static final int[] HEADER = {
        0x91, 0x87, 0x3F, 0x9A, 0x04, 0xD2, 0x25, 0xC0, 0xDC, 0x2A, 0xBD, 0xBE, 0x4B, 0xB4, 0xE5, 0x94,
        0xED, 0x1E, 0x37, 0x22, 0x31, 0x43, 0x2B, 0xCF, 0x4D, 0x8E, 0xF7, 0x6B, 0xE9, 0xE1, 0xFB, 0x45
    };

    private static final int CRC_MASK = 0x1021;
    private static final int CRC_INIT_FILE = 0x0000;
    private static final int CRC_INIT_PACK = 0xcd18;

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

    public static byte[] xor(byte[] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            int ord1_idx = i & 0xFF;
            int ord2_idx = (i >> 8) & 0xFF;
            int ord3_idx = (i >> 16) & 0xFF;

            int b = data[i] ^ Xor_Ord1[ord1_idx] ^ Xor_Ord2[ord2_idx];
            int fix = Xor_Ord3_Eye[ord3_idx] ^ fixKnownTRUE[ord3_idx];

            data[i] = (byte) (b ^ fix);
        }

        return data;
    }

    public static void main(String[] args) throws IOException {
        byte[] hello = "Hello World !".getBytes();
        byte[] enc = xor(hello);
        dumpBytesAsString(enc);
        byte[] dec = xor(enc);
        dumpBytesAsString(dec);
        System.out.println("done");
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

    /**
     * Reference implementation from Wikipedia and Simeon's rewrite
     * Not optimized
     */
    public static int referenceComputeChecksum(byte[] buffer, int offset, int length, int initValue, int mask) throws NoSuchAlgorithmException {

        int rem = initValue;

        for (int i = 0; i < length; i++) {
            rem = rem ^ (buffer[offset + i] << 8);
            for (int j = 0; j < 8; j++) {
                if ((rem & 0x8000) != 0) {
                    rem = (rem << 1) ^ mask;
                } else {
                    rem = rem << 1;

                }
                rem = rem & 0xFFFF;
            }
        }
        return rem;
    }

    /**
     * Optimized implementation using Jacksum.
     * Just faster.
     *
     * @throws NoSuchAlgorithmException
     */
    private static int fastComputeChecksum(byte[] buffer, int offset, int length, int initValue, int mask) throws NoSuchAlgorithmException {
        AbstractChecksum checksum;
        checksum = JacksumAPI.getChecksumInstance("crc:16," + Integer.toHexString(mask) + "," + Integer.toHexString(initValue) + ",false,false,0");

        checksum.reset();

        checksum.update(buffer, offset, length);

        return (int) checksum.getValue();
    }

    public static int computeChecksum(byte[] buffer, int offset, int length) throws NoSuchAlgorithmException {
        return fastComputeChecksum(buffer, offset, length, CRC_INIT_FILE, CRC_MASK);
    }

    public static List<FirmwareFileEntry> unpack(byte[] buffer) throws IOException, NoSuchAlgorithmException {
        assertEquals(buffer, 0, 0x20, HEADER);
        int offset = 0x20;

        long count = getUInt32(buffer, offset); offset +=4;
        long headerLen = getUInt32(buffer, offset); offset +=4;
        if (headerLen != 0x30 + count * 0x20) {
            System.err.println("Warning : header length (0x" + Integer.toHexString((int) headerLen) + ") is not 0x30 + " + count + " * 0x20 !");
        }
        assertZero32(buffer, offset); offset +=4;
        assertZero32(buffer, offset); offset +=4;

        ArrayList<FirmwareFileEntry> fileEntries = new ArrayList<FirmwareFileEntry>();

        for (int c = 0; c < count; c++)
        {
            // Read Header
            String firmwareName = getString(buffer, offset, 16); offset += 16;
            firmwareName = firmwareName.trim();
            long fileOffset = getUInt32(buffer, offset); offset +=4;
            long fileLength = getUInt32(buffer, offset) - 2 /*CRC*/; offset +=4;
            assertZero32(buffer, offset); offset +=4;
            assertZero32(buffer, offset); offset +=4;

            // Read CRC at the end of the block
            int packedFileChecksum = getUInt16(buffer, (int) (fileOffset + fileLength));

            // Create data structure for file description
            fileEntries.add(new FirmwareFileEntry(firmwareName, buffer, (int) fileOffset, (int) fileLength, packedFileChecksum));
        }

        // Package checksum seems to be unused. Checking anyway...

        offset = fileEntries.get((int) (count - 1)).getOffset()  // start of last contained file
                + fileEntries.get((int) (count - 1)).getLength() // length of last contained file
                + 2;                                             // length of CRC of last contained file
        int computedChecksum = FirmwareUtils.fastComputeChecksum(buffer, 0, offset, CRC_INIT_PACK, CRC_MASK);

        int start = 0;
        int end = 0x70;
        int csa = FirmwareUtils.fastComputeChecksum(buffer, start, end - start, 0, CRC_MASK);
        end += fileEntries.get(0).getLength() + 2;
        int csb = FirmwareUtils.fastComputeChecksum(buffer, start, end - start, 0, CRC_MASK);
        end += fileEntries.get(1).getLength() + 2;
        int csc = FirmwareUtils.fastComputeChecksum(buffer, start, end - start, 0, CRC_MASK);
        start = 0x70;
        int csd = FirmwareUtils.fastComputeChecksum(buffer, start, end - start, 0, CRC_MASK);
        start += fileEntries.get(0).getLength() + 2;
        int cse = FirmwareUtils.fastComputeChecksum(buffer, start, end - start, 0, CRC_MASK);

//        for (int i= 0; i < offset; i++) {
//            int partialChecksum = NikonUtils.fastComputeChecksum(buffer, 0, i, 0, CRC_MASK);
//            if (partialChecksum == computedChecksum) {
//                System.out.println("Checksum from " + 0 + " to 0x" + Integer.toHexString(i) + " matches the global checksum (0x" + Integer.toHexString(computedChecksum) + ").");
//            }
//        }
        int packageChecksum = getUInt16(buffer, offset); offset +=2;

        if (packageChecksum != computedChecksum) {
            System.err.println("Warning : computed payload checksum (" + computedChecksum + ") does not match the one stored at 0x" + Integer.toHexString(offset - 2) + " (" + packageChecksum + ")");
        }

        assertZero16(buffer, offset); offset +=2;
        assertZero32(buffer, offset); offset +=4;
        assertZero32(buffer, offset); offset +=4;
        assertZero32(buffer, offset); offset +=4;

        return fileEntries;
    }
    
    private static void bruteFindOffset(byte[] buffer, int totalLength, int compareChecksum, int init, int mask) throws NoSuchAlgorithmException, IOException {
        String fileName = "d:\\work\\NikonForce.txt";
        FileWriter f = new FileWriter(fileName, true);
        f.write(new Date().toString());
        for (int start = 0; start < 0x100; start++) {
            System.out.print(".");
            f.write(".");
            int computedChecksum = FirmwareUtils.fastComputeChecksum(buffer, start, totalLength - start, init, mask);

            if (compareChecksum == computedChecksum) {
                printlnToWriterAndStdout(f, "");
                printlnToWriterAndStdout(f, "============================================");
                printlnToWriterAndStdout(f, "Found : start = 0x" + Integer.toHexString(start));
                printlnToWriterAndStdout(f, "============================================");
            }
        }
        f.close();
    }
    

    private static void bruteFindInitAndMask(byte[] buffer, int offset, int length, int compareChecksum, int startInit, int endInit, int startMask, int endMask) throws NoSuchAlgorithmException, IOException {
        String fileName = "d:\\work\\NikonForce.txt";
        FileWriter f = new FileWriter(fileName, true);
        f.write(new Date().toString());
        for (int mask = startMask; mask < endMask; mask++) {
            printToWriterAndStdout(f, "-------------------------------------- MASK 0x" + Integer.toHexString(mask) + " --------------------------------------");
            for (int init = startInit; init < endInit; init++) {
                if (init % 0x100 == 0) {
                    f.close();
                    f = new FileWriter(fileName, true);
                    printlnToWriterAndStdout(f, "");
                    printToWriterAndStdout(f, Integer.toHexString(init));
                }
                System.out.print(".");
                f.write(".");
                int computedChecksum = FirmwareUtils.fastComputeChecksum(buffer, offset, length, init, mask);

                if (compareChecksum == computedChecksum) {
                    printlnToWriterAndStdout(f, "");
                    printlnToWriterAndStdout(f, "============================================");
                    printlnToWriterAndStdout(f, "Found : init = 0x" + Integer.toHexString(init) + " - mask = 0x" + Integer.toHexString(mask) + " on full file");
                    printlnToWriterAndStdout(f, "============================================");
                }
            }
        }
        f.close();
    }

    private static void printToWriterAndStdout(FileWriter f, String msg) throws IOException {
        f.write(msg);
        System.out.print(msg);
    }

    private static void printlnToWriterAndStdout(FileWriter f, String msg) throws IOException {
        f.write(msg + System.lineSeparator());
        System.out.println(msg);
    }

    private static void assertEquals(byte[] buffer, int start, int length, int[] ref) {
        for (int i = 0; i < length; i++) {
            if ((buffer[start + i] & 0xFF) != HEADER[i]) {
                System.err.println("Warning : byte 0x" + Integer.toHexString(start + i) +  " does not match expected reference. " +
                        "This is probably not a file that can be handled by this decoder.");
                break;
            }
        }
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

    public static byte[] pack(List<FirmwareFileEntry> fileEntries) throws IOException, NoSuchAlgorithmException {
        int headerSize = 0x30 + fileEntries.size() * 0x20; // header
        int totalLength = headerSize;
        for (FirmwareFileEntry fileEntry : fileEntries) {
            totalLength += fileEntry.getLength() + 2;
        }
        totalLength += 0x10; // footer

        byte[] buffer = new byte[totalLength];

        for (int i = 0; i < 0x20; i++) {
            buffer[i] = (byte) HEADER[i];
        }

        setUInt32(buffer, 0x20, fileEntries.size());
        setUInt32(buffer, 0x24, headerSize);
        int offset = headerSize;
        for (int i = 0; i < fileEntries.size(); i++) {
            // Fill header info
            FirmwareFileEntry fileEntry = fileEntries.get(i);
            setString(buffer, 0x30 + 0x20 * i, fileEntry.getFileName(), 0x10);
            setUInt32(buffer, 0x40 + 0x20 * i, offset);
            setUInt32(buffer, 0x44 + 0x20 * i, fileEntry.getLength() + 2);

            // Fill contents
            for (int j = 0; j < fileEntry.getLength(); j++) {
                buffer[offset + j] = fileEntry.getBuffer()[fileEntry.getOffset() + j];
            }
            offset += fileEntry.getLength();

            // Add CRC
            setUInt16(buffer, offset, fileEntry.getCheckSum());
            offset += 2;
        }
        int computedChecksum = FirmwareUtils.fastComputeChecksum(buffer, 0, offset, CRC_INIT_PACK, CRC_MASK);
        setUInt16(buffer, offset, computedChecksum);

        return buffer;
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

    private static int getUInt16(byte[] buffer, int offset) {
        final int v0 = buffer[offset] & 0xFF;
        final int v1 = buffer[offset + 1] & 0xFF;
        return ((v0 << 8) | v1);
    }

    private static void setUInt16(byte[] buffer, int offset, int value) {
        buffer[offset + 1] = (byte) (value & 0xff);
        value = value >> 8;
        buffer[offset] = (byte) (value & 0xff);
    }

    private static long getUInt32(byte[] buffer, int offset) {
        final long v0 = buffer[offset] & 0xFF;
        final long v1 = buffer[offset + 1] & 0xFF;
        final long v2 = buffer[offset + 2] & 0xFF;
        final long v3 = buffer[offset + 3] & 0xFF;
        return ((v0 << 24) | (v1 << 16) | (v2 << 8) | v3);
    }

    private static void setUInt32(byte[] buffer, int offset, int value) {
        for (int i = 0; i < 4; i++) {
            buffer[offset + (3 - i)] = (byte) (value & 0xff);
            value = value >> 8;
        }
    }

    private static String getString(byte[] buffer, int offset, int length) {
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
