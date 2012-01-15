package com.nikonhacker.dfr;


import java.util.ArrayList;
import java.util.List;

public class Function extends Symbol {
    private int endPoint;
    private List<CodeRange> codeRanges;
    private List<Jump> jumps;

    
    public Function(int address, String name, String comment) {
        super(address, name, comment);
        codeRanges = new ArrayList<CodeRange>();
        jumps = new ArrayList<Jump>();
    }

    public Function(Symbol symbol) {
        super(symbol.address, symbol.name, symbol.comment);
    }


    public int getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(int endPoint) {
        this.endPoint = endPoint;
    }

    public List<CodeRange> getCodeRanges() {
        return codeRanges;
    }

    public void setCodeRanges(List<CodeRange> codeRanges) {
        this.codeRanges = codeRanges;
    }

    public List<Jump> getJumps() {
        return jumps;
    }

    public void setJumps(List<Jump> jumps) {
        this.jumps = jumps;
    }

    public class Jump{
        int from;
        int to;
        boolean isConditional;

        public Jump(int from, int to, boolean conditional) {
            this.from = from;
            this.to = to;
            isConditional = conditional;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

        public boolean isConditional() {
            return isConditional;
        }
    }
    
    public class CodeRange{
        int start;
        int address;

        public CodeRange(int start, int address) {
            this.start = start;
            this.address = address;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getAddress() {
            return address;
        }

        public void setAddress(int address) {
            this.address = address;
        }
    }
}
