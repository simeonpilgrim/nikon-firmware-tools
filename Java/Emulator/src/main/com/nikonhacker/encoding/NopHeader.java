package com.nikonhacker.encoding;

public class NopHeader {

    public static final int SIZE = 0x35F;

    public String magic;
    public int    cameraModel;
    public int    id;
    public String name;
    public String shortName;
}
