package com.nikonhacker.dfr;

public class Range implements Comparable<Range> {
    int start;
    int end;
    DATA data;
    int fileOffset;

    public Range(int start, int end, DATA data) {
        this.start = start;
        this.end = end;
        this.data = data;
        this.fileOffset = 0;
    }

    public Range(int start, int end, int fileOffset) {
        this.start = start;
        this.end = end;
        this.data = null;
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

    public DATA getData() {
        return data;
    }

    public void setData(DATA data) {
        this.data = data;
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
        return "Range from 0x" + Format.asHex(start, 8) + " to 0x" + Format.asHex(end, 8) + " corresponding to file at 0x" + Format.asHex(fileOffset, 8) + ", data = {" + data + "}";
    }
}
