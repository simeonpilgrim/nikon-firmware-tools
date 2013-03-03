package com.nikonhacker.emu;

public interface CycleCounterListener {
    public void onCycleCountChange(long oldCount, int increment);
}
