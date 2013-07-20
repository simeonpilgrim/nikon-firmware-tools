package com.nikonhacker.disassembly;

import com.nikonhacker.Format;

/**
 * Note: this class has a natural ordering that is inconsistent with equals
 */
public class Range implements Comparable<Range> {
    private int start;
    private int end;
    private RangeType rangeType;
    private int fileOffset;

    public Range(int start, int end, RangeType rangeType) {
        this.start = start;
        this.end = end;
        this.rangeType = rangeType;
        this.fileOffset = 0;
    }

    public Range(int start, int end, int fileOffset) {
        this.start = start;
        this.end = end;
        this.rangeType = null;
        this.fileOffset = fileOffset;
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

    public RangeType getRangeType() {
        return rangeType;
    }

    public void setRangeType(RangeType rangeType) {
        this.rangeType = rangeType;
    }

    public int getFileOffset() {
        return fileOffset;
    }

    public void setFileOffset(int fileOffset) {
        this.fileOffset = fileOffset;
    }

    public int compareTo(Range o) {
        return start - o.start;
    }

    @Override
    public String toString() {
        return "Range from 0x" + Format.asHex(start, 8) + " to 0x" + Format.asHex(end, 8) + " corresponding to file at 0x" + Format.asHex(fileOffset, 8) + ", rangeType = {" + rangeType + "}";
    }
}
