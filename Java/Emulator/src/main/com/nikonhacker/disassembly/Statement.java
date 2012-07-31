package com.nikonhacker.disassembly;

import java.util.Set;

public abstract class Statement {
    private Instruction instruction = null;
    private String comment;
    /** cached CPUState, for CALLs and INTs */
    public CPUState cpuState = null;
    /** flags (for display only) */
    public int flags;
    /** formatted operand list */
    public String operandString;

    public abstract String toString();

    public abstract String toString(Set<OutputOption> options);

    /** decoded instruction */
    public Instruction getInstruction() {
        return instruction;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    /** optional comment */
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public CPUState getCpuState() {
        return cpuState;
    }

    public void setCpuState(CPUState cpuState) {
        this.cpuState = cpuState;
    }
}
