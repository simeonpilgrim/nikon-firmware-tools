package com.nikonhacker.disassembly.fr;


import com.nikonhacker.disassembly.Range;

public class InterruptVectorRange extends Range {
    public InterruptVectorRange(int start, int end, DataType dataType) {
        super(start, end, dataType);
    }

    public InterruptVectorRange(int start, int end, int fileOffset) {
        super(start, end, fileOffset);
    }
}
