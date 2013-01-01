package com.nikonhacker.disassembly;

import com.nikonhacker.emu.CallStackItem;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.lang3.StringUtils;

import java.util.Deque;
import java.util.Set;

/**
 * Represents the (execution or disassembly) context in which this statement is to be interpreted
 */
public class StatementContext {
    /** This is the cpuState used in this context */
    public CPUState cpuState;

    /** This is the memory used in this context */
    public Memory memory;

    /** This is the requested PC to jump to after having executed the statement in the delay slot */
    public Integer nextPc;

    /** This is the requested return address to set after having executed the statement in the delay slot */
    public Integer nextReturnAddress;

    /** A custom register to be used as target for the return address, after having executed the statement in the delay slot */
    public Integer nextReturnAddressTargetRegister;

    /** The function call stack */
    public Deque<CallStackItem> callStack;

    public Set<OutputOption> outputOptions;

    /**
     * This variable is set and used by the emulator to remember if the delaySlot has been done or not
     */
    public boolean delaySlotDone;

    /** Temp storage */
    private Instruction.DelaySlotType storedDelaySlotType = Instruction.DelaySlotType.NONE;
    /** Temp storage */
    private boolean isLineBreakRequested;


    public void setDelayedPc(Integer nextPc) {
        this.nextPc = nextPc;
        this.delaySlotDone = false;
    }

    public void setDelayedPcAndRa(Integer nextPC, Integer nextReturnAddress) {
        this.nextPc = nextPC;
        this.nextReturnAddress = nextReturnAddress;
        this.delaySlotDone = false;
    }

    public void setDelayedPcAndRaAndTarget(Integer nextPC, Integer nextReturnAddress, Integer nextReturnAddressTargetRegister) {
        this.nextPc = nextPC;
        this.nextReturnAddress = nextReturnAddress;
        this.nextReturnAddressTargetRegister = nextReturnAddressTargetRegister;
        this.delaySlotDone = false;
    }

    public void setStoredDelaySlotType(Instruction.DelaySlotType storedDelaySlotType) {
        this.storedDelaySlotType = storedDelaySlotType;
    }

    public Instruction.DelaySlotType getStoredDelaySlotType() {
        return storedDelaySlotType;
    }

    public void setLineBreakRequest(boolean request) {
        isLineBreakRequested = request;
    }

    public boolean isLineBreakRequested() {
        return isLineBreakRequested;
    }

    public void pushStatement(Statement statement) {
        if (callStack != null) {
            //Double test to avoid useless synchronization if not tracking, at the cost of a double test when tracking (debug)
            synchronized (callStack) {
                if (callStack != null) {
                    try {
                        statement.formatOperandsAndComment(this, false, outputOptions);
                    } catch (DisassemblyException e) {
                        e.printStackTrace();
                    }
                    String target = statement.getCommentString();
                    if (StringUtils.isBlank(target)) target = statement.getOperandString();
                    callStack.push(new CallStackItem(cpuState.pc, cpuState.getSp(), statement.getInstruction(), statement.toString(outputOptions), target));
                }
            }
        }
    }

    public CallStackItem popStatement() {
        if (callStack != null && !callStack.isEmpty()) {
            return callStack.pop();
        }
        return null;
    }
}