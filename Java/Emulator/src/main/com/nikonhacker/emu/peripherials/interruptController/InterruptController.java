package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.InterruptRequest;

public interface InterruptController {
    boolean request(int interruptNumber);

    boolean request(InterruptRequest newInterruptRequest);

    void removeRequest(int requestNumber);

    void removeRequest(InterruptRequest interruptRequest);

    boolean hasPendingRequests();

    InterruptRequest getNextRequest();
}
