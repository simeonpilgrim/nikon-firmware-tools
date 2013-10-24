package com.nikonhacker.emu.peripherials.interruptController;

public interface SharedInterruptCircuit {
    public boolean request(int interruptNumber, int sourceNumber);

    public void removeRequest(int interruptNumber, int sourceNumber);
}
