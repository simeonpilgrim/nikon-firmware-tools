package com.nikonhacker.disassembly;

import java.util.Set;

public abstract class Statement {
    protected Instruction instruction = null;
    /** cached CPUState, for CALLs and INTs */
    public CPUState cpuState = null;
    /** flags (for display only) */
    public int flags;

    protected String operandString;
    protected String commentString;

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
    public String getCommentString() {
        return commentString;
    }

    public void setCommentString(String commentString) {
        this.commentString = commentString;
    }

    public CPUState getCpuState() {
        return cpuState;
    }

    public void setCpuState(CPUState cpuState) {
        this.cpuState = cpuState;
    }

    /** formatted operand list */
    public String getOperandString() {
        return operandString;
    }

    public void setOperandString(String operandString) {
        this.operandString = operandString;
    }
}
