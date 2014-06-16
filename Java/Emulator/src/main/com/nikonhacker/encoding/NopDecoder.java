package com.nikonhacker.encoding;

import java.io.File;
import java.util.List;

public class NopDecoder {

    private static void usage() {
        System.out.println("Usage : " + NopDecoder.class.getName() + " <infile> [<outfile>]");
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
            new NopDecoder().decode(args[0], outFile, true);
        } catch (FirmwareFormatException e) {
            e.printStackTrace();
            usage();
        }
        System.out.println("Operation complete.");

    }

    public void decode(String sourceFileName, String unpackFileName, boolean ignoreCrcErrors) throws FirmwareFormatException {
        File sourceFile = new File(sourceFileName);
        if (!sourceFile.exists()) {
            throw new FirmwareFormatException("Source file does not exist");
        } else {
            try {
                byte[] source = NopUtils.load(sourceFile);

                NopHeader nopHeader = NopUtils.getNopHeader(source);

                // TODO this is only simple length check
                if (source.length <= NopHeader.SIZE + 3) {
                    throw new FirmwareFormatException("Bad NOP file length");
                }

                int computedCrc = NopUtils.computeChecksum(source, 0, source.length-2);
                if (computedCrc != NopUtils.getUInt16(source, source.length-2)) {
                    String msg = "Warning : checksum not OK for " + sourceFileName + ". Computed=" + computedCrc + ", expected=0";
                    if (ignoreCrcErrors) {
                        System.err.println(msg);
                    }
                    else {
                        throw new FirmwareFormatException(msg);
                    }
                }

                NopUtils.decrypt(nopHeader, source, NopHeader.SIZE, source.length - 3 - NopHeader.SIZE);
                NopUtils.dumpFile(new File(unpackFileName), source, 0, source.length);
            } catch (Exception e) {
                throw new FirmwareFormatException(e);
            }
        }
    }
}
