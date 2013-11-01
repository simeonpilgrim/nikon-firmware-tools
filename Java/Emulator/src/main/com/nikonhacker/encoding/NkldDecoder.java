package com.nikonhacker.encoding;

import java.io.File;
import java.util.List;

public class NkldDecoder {

    private static void usage() {
        System.out.println("Usage : " + NkldDecoder.class.getName() + " <infile> [<outfile>]");
        System.exit(-1);
    }

    public static void main(String[] args) {
        if (args.length < 1 ) {
            usage();
        }
        String outFile;
        if (args.length < 2) {
            outFile = args[0]+".bin";
        }
        else {
            outFile = args[1];
        }
        try {
            new NkldDecoder().decode(args[0], outFile, true);
        } catch (FirmwareFormatException e) {
            usage();
            e.printStackTrace();
        }
        System.out.println("Operation complete.");

    }

    public void decode(String sourceFileName, String unpackFileName, boolean ignoreCrcErrors) throws FirmwareFormatException {
        File sourceFile = new File(sourceFileName);
        if (!sourceFile.exists()) {
            throw new FirmwareFormatException("Source file does not exist");
        } else {
            try {
                byte[] source = NkldUtils.load(sourceFile);
                
                NkldHeader nkldHeader = NkldUtils.getNkldHeader(source);

                int computedCrc = NkldUtils.computeChecksum(source, 0, nkldHeader.totalLength);
                if (computedCrc != 0) {
                    String msg = "Warning : checksum not OK for " + sourceFileName + ". Computed=" + computedCrc + ", expected=0";
                    if (ignoreCrcErrors) {
                        System.err.println(msg);
                    }
                    else {
                        throw new FirmwareFormatException(msg);
                    }
                    NkldUtils.decrypt(source, nkldHeader.dataOffset, nkldHeader.dataLength);
                
                    NkldUtils.dumpFile(new File(unpackFileName), source, 0, nkldHeader.totalLength);
                }
            } catch (Exception e) {
                throw new FirmwareFormatException(e);
            }
        }
    }
}
