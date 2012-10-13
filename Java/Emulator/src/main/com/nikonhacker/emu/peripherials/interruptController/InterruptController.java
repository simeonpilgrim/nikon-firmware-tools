package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.interrupt.InterruptRequest;

import java.util.List;

public interface InterruptController {
    public boolean request(int interruptNumber);

    public boolean request(InterruptRequest newInterruptRequest);

    public void removeRequest(int requestNumber);

    public void removeRequest(InterruptRequest interruptRequest);

    public boolean hasPendingRequests();

    public InterruptRequest getNextRequest();

    List<InterruptRequest> getInterruptRequestQueue();
}
