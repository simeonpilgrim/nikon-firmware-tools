package com.nikonhacker.dfr;


public class Jump {
    int source;
    int target;
    private OpCode opcode;
    boolean isDynamic;

    public Jump(int source, int target, OpCode opcode, boolean isDynamic) {
        this.source = source;
        this.target = target;
        this.opcode = opcode;
        this.isDynamic = isDynamic;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public OpCode getOpcode() {
        return opcode;
    }

    public boolean isConditional() {
        return opcode != null && opcode.isConditional;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    @Override
    public String toString() {
        return (isConditional()?"Conditional":"Inconditional") + (isDynamic()?" dynamic":"") + " jump from 0x" + Integer.toHexString(source) + " to 0x" + Integer.toHexString(target);
    }
}
