package com.nikonhacker.dfr;


public class Jump {
    int source;
    int target;
    private OpCode opcode;

    public Jump(int source, int target, OpCode opcode) {
        this.source = source;
        this.target = target;
        this.opcode = opcode;
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

    @Override
    public String toString() {
        return (isConditional()?"Conditional":"Inconditional") + " jump from 0x" + Integer.toHexString(source) + " to 0x" + Integer.toHexString(target);
    }
}
