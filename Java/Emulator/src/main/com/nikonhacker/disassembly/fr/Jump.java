package com.nikonhacker.disassembly.fr;


public class Jump {
    int source;
    int target;
    private FrInstruction instruction;
    boolean isDynamic;

    public Jump(int source, int target, FrInstruction instruction, boolean isDynamic) {
        this.source = source;
        this.target = target;
        this.instruction = instruction;
        this.isDynamic = isDynamic;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public FrInstruction getInstruction() {
        return instruction;
    }

    public boolean isConditional() {
        return instruction != null && instruction.isConditional;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    @Override
    public String toString() {
        return (isConditional()?"Conditional":"Inconditional") + (isDynamic()?" dynamic":"") + " jump from 0x" + Integer.toHexString(source) + " to 0x" + Integer.toHexString(target);
    }
}
