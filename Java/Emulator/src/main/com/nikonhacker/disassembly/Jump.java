package com.nikonhacker.disassembly;


import com.nikonhacker.disassembly.Instruction;

public class Jump {
    private int source;
    private int target;
    private Instruction instruction;
    private boolean isDynamic;

    public Jump(int source, int target, Instruction instruction, boolean isDynamic) {
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

    public Instruction getInstruction() {
        return instruction;
    }

    public boolean isConditional() {
        return instruction != null && instruction.isConditional();
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    @Override
    public String toString() {
        return (isConditional()?"Conditional":"Inconditional") + (isDynamic()?" dynamic":"") + " jump from 0x" + Integer.toHexString(source) + " to 0x" + Integer.toHexString(target);
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }
}
