package com.nikonhacker.emu;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.Memory;

public class EmulationContext {
    public CPUState cpuState;
    public Memory memory;
    public Integer nextPC;
    public Integer nextReturnAddress;
}
