package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.InterruptRequest;

public interface InterruptController {
    public boolean request(int interruptNumber);

    public boolean request(InterruptRequest newInterruptRequest);

    public void removeRequest(int requestNumber);

    public void removeRequest(InterruptRequest interruptRequest);

    public boolean hasPendingRequests();

    public InterruptRequest getNextRequest();

    public void updateRequestICR(int i, byte value);
}
