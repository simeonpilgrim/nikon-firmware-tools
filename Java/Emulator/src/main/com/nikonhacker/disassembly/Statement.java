package com.nikonhacker.disassembly;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;

public abstract class Statement {
    protected Instruction instruction = null;
    /** cached CPUState, for CALLs and INTs */
    public CPUState cpuState = null;

    /** "flags" (for display only) */
    private Instruction.DelaySlotType delaySlotType;
    private boolean mustInsertLineBreak;

    private String operandString;
    private String commentString;

    /** immediate operand */
    public int imm; // as-is from binary code
    public int decodedImm; // interpreted

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

    /** formatted operand list */
    public String getOperandString() {
        return operandString;
    }

    public void setOperandString(String operandString) {
        this.operandString = operandString;
    }

    /** flags (for display only) */
    public Instruction.DelaySlotType getDelaySlotType() {
        return delaySlotType;
    }

    public void setDelaySlotType(Instruction.DelaySlotType delaySlotType) {
        this.delaySlotType = delaySlotType;
    }

    public boolean mustInsertLineBreak() {
        return mustInsertLineBreak;
    }

    public void setMustInsertLineBreak(boolean mustInsertLineBreak) {
        this.mustInsertLineBreak = mustInsertLineBreak;
    }

    /**
     * Simple and fast version used by realtime disassembly trace
     */
    @Override
    public String toString() {
        String out = formatAsHex();

        switch (delaySlotType) {
            case NONE:
                out += "              " + StringUtils.rightPad(instruction.getName(), 7) + " " + getOperandString();
                break;
            case NORMAL:
                out += "               " + StringUtils.rightPad(instruction.getName(), 6) + " " + getOperandString();
                break;
            case LIKELY:
                out += "               ?" + StringUtils.rightPad(instruction.getName(), 5) + " " + getOperandString();
                break;
            default:
                throw new RuntimeException("Unknown delay slot type : " + delaySlotType);
        }

        if (StringUtils.isNotBlank(getCommentString())) {
            out += StringUtils.leftPad("; " + getCommentString(), 22);
        }
        out += "\n";
        if (mustInsertLineBreak) {
            out += "\n";
        }
        return out;
    }


    /**
     * Full fledged version used for offline disassembly
     * @param options
     * @return
     */
    public String toString(Set<OutputOption> options) {
        String out = "";

        if (options.contains(OutputOption.HEXCODE)) {
            out += formatAsHex();
        }

        if (options.contains(OutputOption.BLANKS)) {
            out += "              ";
        }

        if (instruction != null) {
            if (delaySlotType != Instruction.DelaySlotType.NONE) {
                out += "  " + StringUtils.rightPad(instruction.getName(), 6) + " " + operandString;
            }
            else {
                out += " " + StringUtils.rightPad(instruction.getName(), 7) + " " + operandString;
            }
        }
        else {
            out += " (no instruction)" + operandString;
        }

        if (StringUtils.isNotBlank(commentString)) {
            out += StringUtils.leftPad("; " + commentString, 22);
        }
        out += "\n";

        if (mustInsertLineBreak) {
            out += "\n";
        }
        return out;
    }

    protected abstract String formatAsHex();

    public abstract int getNumBytes();
}
