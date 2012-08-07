package com.nikonhacker.disassembly.fr;

import java.util.ArrayList;
import java.util.List;

/**
 * address maps
 */

public class RangeType {
    public enum MemoryType {
        NONE,
        UNKNOWN,
        CODE,
        DATA
    }

    public enum Width {
        MD_WORD(0),
        MD_LONG(1),
        MD_LONGNUM(2),
        MD_VECTOR(3),
        MD_RATIONAL(4),
        UNKNONW(-1);

        private int index;

        Width(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public List<Width> widths = new ArrayList<Width>();

    public MemoryType memoryType = MemoryType.UNKNOWN;


    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        switch (memoryType)
        {
            case DATA:
                sb.append(memoryType.name());
                sb.append(":");

                for (Width width : widths)
                {
                    switch (width)
                    {
                        case MD_LONG: sb.append("L"); break;
                        case MD_LONGNUM: sb.append("N"); break;
                        case MD_RATIONAL: sb.append("R"); break;
                        case MD_VECTOR: sb.append("V"); break;
                        case MD_WORD: sb.append("W"); break;
                        default: sb.append("?"); break;
                    }
                }
                return sb.toString();
            default:
                return memoryType.name();
        }
    }

    public boolean isCode() {
        return memoryType == MemoryType.CODE || memoryType == MemoryType.UNKNOWN;
    }
}
