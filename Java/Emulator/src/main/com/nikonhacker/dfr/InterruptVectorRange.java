package com.nikonhacker.dfr;


public class InterruptVectorRange extends Range {
    public InterruptVectorRange(int start, int end, DATA data) {
        super(start, end, data);
    }

    public InterruptVectorRange(int start, int end, int fileOffset) {
        super(start, end, fileOffset);
    }
}
