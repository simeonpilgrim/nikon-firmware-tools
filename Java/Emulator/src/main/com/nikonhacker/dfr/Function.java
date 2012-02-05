package com.nikonhacker.dfr;


import com.nikonhacker.Format;

import java.util.ArrayList;
import java.util.List;

public class Function extends Symbol {
    private int endAddress;
    private List<CodeSegment> codeSegments;
    private List<Jump> jumps;
    private List<Jump> calls;
    private Type type = Type.STANDARD;

    public enum Type {MAIN, INTERRUPT, STANDARD, UNKNOWN}


    public Function(int address, String name, String comment) {
        super(address, name, comment);
        codeSegments = new ArrayList<CodeSegment>();
        jumps = new ArrayList<Jump>();
        calls = new ArrayList<Jump>();
    }

    public Function(int address, String name, String comment, Type type) {
        super(address, name, comment);
        this.type = type;
        codeSegments = new ArrayList<CodeSegment>();
        jumps = new ArrayList<Jump>();
        calls = new ArrayList<Jump>();
    }

    public Function(Symbol symbol) {
        super(symbol.address, symbol.name, symbol.comment);
    }


    public int getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(int endAddress) {
        this.endAddress = endAddress;
    }

    public List<CodeSegment> getCodeSegments() {
        return codeSegments;
    }

    public void setCodeSegments(List<CodeSegment> codeSegments) {
        this.codeSegments = codeSegments;
    }

    public List<Jump> getJumps() {
        return jumps;
    }

    public void setJumps(List<Jump> jumps) {
        this.jumps = jumps;
    }

    public List<Jump> getCalls() {
        return calls;
    }

    public void setCalls(List<Jump> calls) {
        this.calls = calls;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getColor() {
        switch (type) {
            case INTERRUPT:
                return "#FFFF77";
            case MAIN:
                return "#77FF77";
            case STANDARD:
                return "#77FFFF";
            default:
                return "#777777";
        }
    }

    @Override
    public String toString() {
        return getName() + "\n0x" + Format.asHex(getAddress(), 8);
    }

    public String getSummary() {
        return getName() + " starting at 0x" + Integer.toHexString(getAddress()) + " [" + codeSegments.size() + " segment(s)]";
    }

    public String getTitleLine() {
        return getName()
                + "(" + getComment() + ")"
                + (codeSegments.size()>1?(" [" + codeSegments.size() + " segment" + "s]"):"");
    }

}