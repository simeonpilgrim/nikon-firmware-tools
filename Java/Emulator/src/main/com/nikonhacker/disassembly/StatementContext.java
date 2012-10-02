package com.nikonhacker.disassembly;

import com.nikonhacker.emu.memory.Memory;

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

}
