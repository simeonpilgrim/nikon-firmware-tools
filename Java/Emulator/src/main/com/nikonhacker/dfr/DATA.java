package com.nikonhacker.dfr;

import java.util.ArrayList;
import java.util.List;

/**
 * address maps
 */

public class DATA {
    public final static int SpecType_MD_WORD = 0;
    public final static int SpecType_MD_LONG = 1;
    public final static int SpecType_MD_LONGNUM = 2;
    public final static int SpecType_MD_VECTOR=3;
    public final static int SpecType_MD_RATIONAL=4;
    public final static int SpecType_UNKNOWN=-1;

    public final static int MEMTYPE_NONE = 0;
    public final static int MEMTYPE_UNKNOWN=1;
    public final static int MEMTYPE_CODE=2;
    public final static int MEMTYPE_DATA=3;

    public List<Integer> spec = new ArrayList<Integer>();
    public int memType = MEMTYPE_UNKNOWN;


    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        switch (memType)
        {
            case MEMTYPE_NONE:
                return "NONE";
            case MEMTYPE_UNKNOWN:
                return "UNKNOWN";
            case MEMTYPE_CODE:
                return "CODE";
            case MEMTYPE_DATA:
                sb.append("DATA:");

                for (int s : spec)
                {
                    switch (s)
                    {
                        case SpecType_MD_LONG: sb.append("L"); break;
                        case SpecType_MD_LONGNUM: sb.append("N"); break;
                        case SpecType_MD_RATIONAL: sb.append("R"); break;
                        case SpecType_MD_VECTOR: sb.append("V"); break;
                        case SpecType_MD_WORD: sb.append("W"); break;
                        default: sb.append("?"); break;
                    }
                }
                return sb.toString();
            default:
                return "Unknown memtype : " + memType;
        }
    }

    public boolean isCode() {
        return memType == DATA.MEMTYPE_CODE || memType == MEMTYPE_UNKNOWN;
    }
}
