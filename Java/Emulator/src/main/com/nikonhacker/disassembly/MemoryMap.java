package com.nikonhacker.disassembly;

import com.nikonhacker.disassembly.Range;

import java.util.SortedSet;
import java.util.TreeSet;

public class MemoryMap {
    char opt;
    String name;

    public SortedSet<Range> ranges;

    public MemoryMap(char option, String label) {
        this.opt = option;
        this.name = label;
        ranges = new TreeSet<Range>();
    }

    public void add(Range r) {
        ranges.add(r);
    }

}
