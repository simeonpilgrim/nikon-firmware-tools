package com.nikonhacker.disassembly;

public class Instruction {
    public boolean isConditional;
    public boolean hasDelaySlot;

    /** Type of flow control assigned to each instruction (if suitable) */
    public static enum FlowType {
        NONE,
        CALL,
        JMP,
        BRA,
        INT,
        INTE,
        RET
    }

    public FlowType flowType;

    public Instruction(FlowType flowType, boolean hasDelaySlot, boolean isConditional) {
        this.flowType = flowType;
        this.hasDelaySlot = hasDelaySlot;
        this.isConditional = isConditional;
    }

    public FlowType getFlowType() {
        return flowType;
    }

    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }

    public boolean isConditional() {
        return isConditional;
    }

    public void setConditional(boolean conditional) {
        isConditional = conditional;
    }

    public boolean hasDelaySlot() {
        return hasDelaySlot;
    }

    public void setHasDelaySlot(boolean hasDelaySlot) {
        this.hasDelaySlot = hasDelaySlot;
    }
}
