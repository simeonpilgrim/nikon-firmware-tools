package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.InterruptRequest;

/**
 * Dummy implementation to be used when no interrupt controller is needed
 */
public class DummyInterruptController implements InterruptController {
    public boolean request(int interruptNumber) {
        return false;
    }

    public boolean request(InterruptRequest newInterruptRequest) {
        return false;
    }

    public void removeRequest(int requestNumber) {
      
    }

    public void removeRequest(InterruptRequest interruptRequest) {
      
    }

    public boolean hasPendingRequests() {
        return false;
    }

    public InterruptRequest getNextRequest() {
        return null;
    }

    public void updateRequestICR(int i, byte value) {
      
    }
}
