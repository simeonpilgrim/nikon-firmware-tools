package com.nikonhacker.encoding;

import java.io.IOException;

public class NopUtils extends NkldUtils {

    public static void decrypt(NopHeader nopHeader, byte[] data, int offset, int size) throws IOException {
        decryptData(nopHeader.cameraModel, nopHeader.name.getBytes(), data, offset, size);
    }

    public static final NopHeader getNopHeader (byte[] data) throws FirmwareFormatException {
        NopHeader nopHeader = new NopHeader();

        if (data.length < NopHeader.SIZE) {
            throw new FirmwareFormatException("Bad NOP size");
        }
        nopHeader.magic = getString(data, 0, 4);
        nopHeader.cameraModel = getUInt32(data, 4);
        nopHeader.id = getUInt16(data, 8);
        nopHeader.name = getString(data, 10, 20).trim();
        nopHeader.shortName = getString(data, 30, 4).trim();

        if (!nopHeader.magic.equals("NOP\0")) {
            throw new FirmwareFormatException("Bad NOP magic "+nopHeader.magic+" "+nopHeader.magic.length());
        }
        return nopHeader;
    }

}
