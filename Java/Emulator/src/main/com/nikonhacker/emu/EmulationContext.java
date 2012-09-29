package com.nikonhacker.emu;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class EmulationContext {
    /** This is the cpuState used in this emulation context */
    public CPUState cpuState;

    /** This is the memory used in this emulation context */
    public Memory memory;

    /** This is the requested PC to jump to after having executed the statement in the delay slot */
    public Integer nextPc;

    /** This is the requested return address to set after having executed the statement in the delay slot */
    public Integer nextReturnAddress;

    /**
     * This variable is set and used by the emulator to remember if the delaySlot has been done or not
     */
    public boolean delaySlotDone;

    public void setDelayedPc(Integer nextPc) {
        this.nextPc = nextPc;
        this.delaySlotDone = false;
    }

    public void setDelayedPcAndRa(Integer nextPC, Integer nextReturnAddress) {
        this.nextPc = nextPC;
        this.nextReturnAddress = nextReturnAddress;
        this.delaySlotDone = false;
    }

}
