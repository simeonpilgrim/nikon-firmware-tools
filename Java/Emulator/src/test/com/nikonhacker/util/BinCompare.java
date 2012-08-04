package com.nikonhacker.util;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.ParsingException;

import java.io.FileInputStream;
import java.io.IOException;

public class BinCompare {
    public static void main(String[] args) throws IOException, ParsingException {
        if (args.length < 6) {
            System.err.println("Usage BinCompare <file1> <file2> <start1> <start2> <skip1> <skip2>");
            System.exit(-1);
        }
        FileInputStream fis1 = new FileInputStream(args[0]);
        FileInputStream fis2 = new FileInputStream(args[1]);
        int start1 = Format.parseUnsigned(args[2]);
        int start2 = Format.parseUnsigned(args[3]);
        int skip1 = Format.parseUnsigned(args[4]);
        int skip2 = Format.parseUnsigned(args[5]);
        fis1.skip(skip1);
        fis2.skip(skip2);
        int i = 0;
        while(true) {
            int b1 = fis1.read();
            int b2 = fis2.read();
            if (b1 == -1) {
                if (b2 == -1) {
                    System.out.println("Comparison complete");
                    break;
                }
                else {
                    System.out.println("End of file1 reached");
                    break;
                }
            }
            if (b2 == -1) {
                System.out.println("End of file2 reached");
                break;
            }
            if (b1 != b2) {
                System.out.println("Difference : at 0x" + Format.asHex(start1 + skip1 + i, 8) + " file1 : 0x" + Format.asHex(b1, 2) + " - file2 - 0x" + Format.asHex(b2, 2));
            }            
            i++;
        }
        fis1.close();
        fis2.close();
    }
}
