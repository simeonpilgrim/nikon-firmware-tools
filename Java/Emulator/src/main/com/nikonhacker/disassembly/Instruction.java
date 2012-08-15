package com.nikonhacker.disassembly;

public class Instruction {
    public boolean isConditional;
    public DelaySlotType delaySlotType;
    private String name;
    private String displayFormat;

    /** Type of flow control assigned to each instruction (if suitable) */
    public static enum FlowType {
        /** This instruction does not change the program flow */
        NONE,

        /** This instruction changes the program flow to a subroutine (with return foreseen) */
        CALL,

        /** This instruction changes the program flow unconditionally (with no return foreseen) */
        JMP,

        /** This instruction changes the program flow conditionally (with no return foreseen) */
        BRA,

        /** This instruction changes the program flow by calling an interrupt */
        INT,

        /** This instruction changes the program flow by returning from a subroutine or interrupt */
        RET
    }

    /** Type of delay slot */
    public static enum DelaySlotType {
        /** This instruction is not followed by a delay slot */
        NONE,

        /** This instruction is followed by a normal delay slot, executed unconditionally */
        NORMAL,

        /** This instruction is followed by a delay slot, executed only if the branch is followed */
        LIKELY
    }

    public FlowType flowType;

    public Instruction(String name, String displayFormat, FlowType flowType, boolean isConditional, DelaySlotType delaySlotType) {
        this.name = name;
        this.displayFormat = displayFormat;
        this.flowType = flowType;
        this.isConditional = isConditional;
        this.delaySlotType = delaySlotType;
    }

    public String getName() {
        return name;
    }

    public String getDisplayFormat() {
        return displayFormat;
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
        return delaySlotType != DelaySlotType.NONE;
    }

    public DelaySlotType getDelaySlotType() {
        return delaySlotType;
    }

    public void setDelaySlotType(DelaySlotType delaySlotType) {
        this.delaySlotType = delaySlotType;
    }
}
