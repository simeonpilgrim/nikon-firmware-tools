package com.nikonhacker.disassembly;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Jump jump = (Jump) o;

        if (source != jump.source) return false;
        if (target != jump.target) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = source;
        result = 31 * result + target;
        return result;
    }
}
