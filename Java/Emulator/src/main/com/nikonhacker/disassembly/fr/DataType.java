package com.nikonhacker.disassembly.fr;

import java.util.ArrayList;
import java.util.List;

/**
 * address maps
 */

public class DataType {
    public enum MemType {
        NONE,
        UNKNOWN,
        CODE,
        DATA
    }

    public enum SpecType {
        MD_WORD(0),
        MD_LONG(1),
        MD_LONGNUM(2),
        MD_VECTOR(3),
        MD_RATIONAL(4),
        UNKNONW(-1);

        private int index;

        SpecType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public List<SpecType> specTypes = new ArrayList<SpecType>();

    public MemType memType = MemType.UNKNOWN;


    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        switch (memType)
        {
            case DATA:
                sb.append(memType.name());
                sb.append(":");

                for (SpecType specType : specTypes)
                {
                    switch (specType)
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
                return memType.name();
        }
    }

    public boolean isCode() {
        return memType == MemType.CODE || memType == MemType.UNKNOWN;
    }
}
