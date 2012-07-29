package com.nikonhacker.disassembly;


import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.Jump;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Function extends Symbol {

    public enum Type {
        /** The entry point */
        MAIN,
        /** address referenced in interrupt vector */
        INTERRUPT,
        /** code reached by at least one static CALL */
        STANDARD,
        /** code not reached by any static CALL */
        UNKNOWN
    }

    /**
     * List of Code Segments that compose the function
     */
    private List<CodeSegment> codeSegments;
    /**
     * List of jumps (JMP, Bcc) made from this function
     */
    private List<Jump> jumps;
    /**
     * List of calls to other functions made by this function
     */
    private List<Jump> calls;
    /**
     * List of calls to this function made by other functions
     * The map associates the call with the source function
     */
    private Map<Jump,Function> calledBy;

    /**
     * Function type
     * @see Type
     */
    private Type type = Type.STANDARD;


    public Function(int address, String name, String comment, Type type) {
        super(address, name, comment);
        this.type = type;
        codeSegments = new ArrayList<CodeSegment>();
        jumps = new ArrayList<Jump>();
        calls = new ArrayList<Jump>();
        calledBy = new HashMap<Jump, Function>();
    }

    public Function(Symbol symbol) {
        super(symbol.address, symbol.name, symbol.comment);
    }


    public List<CodeSegment> getCodeSegments() {
        return codeSegments;
    }

    public List<Jump> getJumps() {
        return jumps;
    }

    public List<Jump> getCalls() {
        return calls;
    }

    public Map<Jump, Function> getCalledBy() {
        return calledBy;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getFillColor() {
        switch (type) {
            case INTERRUPT:
                return "#77FF77";
            case MAIN:
                return "#FFFF77";
            case STANDARD:
                return "#77FFFF";
            default:
                return "#AAAAAA";
        }
    }

    public String getBorderColor() {
        switch (type) {
            case INTERRUPT:
                return "#007700";
            case MAIN:
                return "#777700";
            case STANDARD:
                return "#007777";
            default:
                return "#DDDDDD";
        }
    }

    @Override
    public String toString() {
        return getName() + "\n" + (getCalledBy().size()==0?"":("=> ")) + "0x" + Format.asHex(getAddress(), 8) + (getCalls().size()==0?"":(" =>"));
    }

    public String getSummary() {
        return getName() + " starting at 0x" + Integer.toHexString(getAddress()) + " [" + codeSegments.size() + " segment(s)]";
    }

    public String getTitleLine() {
        return name
                + "(" + (comment==null?"":comment) + ")"
                + (codeSegments.size()>1?(" [" + codeSegments.size() + " segment" + "s]"):"");
    }

}