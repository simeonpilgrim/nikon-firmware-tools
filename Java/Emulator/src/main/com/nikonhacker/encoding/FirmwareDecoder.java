package com.nikonhacker.encoding;

import java.io.File;
import java.util.List;

public class FirmwareDecoder {

    public static void usage() {
        System.out.println("Usage : " + FirmwareDecoder.class.getName() + " <infile> [<destdir>] ");
        System.exit(-1);
    }

    public static void main(String[] args) {
        if (args.length < 1 ) {
            usage();
        }
        String destDir;
        if (args.length < 2) {
            destDir = ".";
        }
        else {
            destDir = args[1];
        }
        try {
            new FirmwareDecoder().decode(args[0], destDir, true);
        } catch (FirmwareFormatException e) {
            usage();
            e.printStackTrace();
        }
        System.out.println("Operation complete.");

    }

    public void decode(String sourceFileName, String unpackDirName, boolean ignoreCrcErrors) throws FirmwareFormatException {
        File sourceFile = new File(sourceFileName);
        if (!sourceFile.exists()) {
            throw new FirmwareFormatException("Source file does not exist");
        } else {
            try {
                byte[] source = FirmwareUtils.load(sourceFile);
                byte[] decrypted = FirmwareUtils.xor(source);
                List<FirmwareFileEntry> fileEntries = FirmwareUtils.unpack(decrypted);
                File unpackDir = new File(unpackDirName);
                unpackDir.mkdirs();
                for (FirmwareFileEntry fileEntry : fileEntries) {
                    int computedCrc = FirmwareUtils.computeChecksum(fileEntry.getBuffer(), fileEntry.getOffset(), fileEntry.getLength());
                    if (computedCrc != fileEntry.getCheckSum()) {
                        String msg = "Warning : checksum not OK for " + fileEntry.getFileName() + ". Computed=" + computedCrc + ", stored=" + fileEntry.getCheckSum();
                        if (ignoreCrcErrors) {
                            System.err.println(msg);
                        }
                        else {
                            throw new FirmwareFormatException(msg);
                        }
                    }
                    FirmwareUtils.dumpFile(new File(unpackDir, fileEntry.getFileName()), fileEntry.getBuffer(), fileEntry.getOffset(), fileEntry.getLength());
                }
            } catch (Exception e) {
                throw new FirmwareFormatException(e);
            }
        }
    }
}
