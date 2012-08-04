package com.nikonhacker.disassembly;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.DataType;

public class Range implements Comparable<Range> {
    private int start;
    private int end;
    private DataType dataType;
    private int fileOffset;

    public Range(int start, int end, DataType dataType) {
        this.start = start;
        this.end = end;
        this.dataType = dataType;
        this.fileOffset = 0;
    }

    public Range(int start, int end, int fileOffset) {
        this.start = start;
        this.end = end;
        this.dataType = null;
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

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
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
        return "Range from 0x" + Format.asHex(start, 8) + " to 0x" + Format.asHex(end, 8) + " corresponding to file at 0x" + Format.asHex(fileOffset, 8) + ", dataType = {" + dataType + "}";
    }
}
