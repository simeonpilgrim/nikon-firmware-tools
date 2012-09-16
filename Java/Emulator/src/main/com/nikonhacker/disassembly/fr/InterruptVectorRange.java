package com.nikonhacker.disassembly.fr;


import com.nikonhacker.disassembly.Range;
import com.nikonhacker.disassembly.RangeType;

public class InterruptVectorRange extends Range {
    public InterruptVectorRange(int start, int end, RangeType rangeType) {
        super(start, end, rangeType);
    }

    public InterruptVectorRange(int start, int end, int fileOffset) {
        super(start, end, fileOffset);
    }
}
