package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.FrInterruptRequest;
import com.nikonhacker.emu.InterruptRequest;

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

    public void removeRequest(int requestNumber) {
      
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

    public void updateRequestICR(int i, byte value) {
      
    }

    public List<InterruptRequest> getInterruptRequestQueue() {
        return null;
    }
}
