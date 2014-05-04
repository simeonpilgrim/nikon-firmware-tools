package com.nikonhacker.encoding;

public class NkldHeader {

    /* offset of data block */
    public int dataOffset;
    public int totalLength;
    public int majorVersion;
    public int minorVersion;
    public int entryCount;
    public int magic;
    public int dataLength;
    public int unknown;
}
