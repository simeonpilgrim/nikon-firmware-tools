package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.fr.FrInterruptRequest;

import java.util.List;

/**
 * Dummy implementation to be used when no interrupt controller is needed
 */
public class DummyInterruptController implements InterruptController {
    public boolean request(int interruptNumber) {
        return false;
    }

    @Override
    public boolean request(InterruptRequest newInterruptRequest) {
        return false;
    }

    @Override
    public void removeRequest(int interruptNumber) {
    }

    @Override
    public void removeRequest(InterruptRequest interruptRequest) {

    }

    public boolean hasPendingRequests() {
        return false;
    }

    public FrInterruptRequest getNextRequest() {
        return null;
    }

    public List<InterruptRequest> getInterruptRequestQueue() {
        return null;
    }

    @Override
    public String getStatus() {
        return "-";
    }
}
