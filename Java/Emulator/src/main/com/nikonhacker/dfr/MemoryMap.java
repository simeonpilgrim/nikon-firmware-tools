package com.nikonhacker.dfr;

import java.util.SortedSet;
import java.util.TreeSet;

public class MemoryMap {
    char opt;
    String name;

    public SortedSet<Range> ranges;

    public MemoryMap(char opt, String name) {
        this.opt = opt;
        this.name = name;
        ranges = new TreeSet<Range>();
    }

    public void add(Range r) {
        ranges.add(r);
    }

}
