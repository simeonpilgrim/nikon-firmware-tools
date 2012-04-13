package com.nikonhacker.dfr;


public class CodeSegment {
    int start;
    int end;

    public CodeSegment() {
    }

    public CodeSegment(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "Code Segment from 0x" + Integer.toHexString(start) + " to 0x" + Integer.toHexString(end);
    }
}
