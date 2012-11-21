package com.nikonhacker.emu;

import com.nikonhacker.disassembly.tx.TxCPUState;

public interface CpuPowerModeChangeListener {
    void onCpuPowerModeChange(TxCPUState.PowerMode run);
}
