package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.InterruptRequest;

public class DummyInterruptController implements InterruptController {
    @Override
    public boolean request(int interruptNumber) {
        return true;
    }

    @Override
    public boolean request(InterruptRequest newInterruptRequest) {
        return true;
    }

    @Override
    public void removeRequest(int requestNumber) {

    }

    @Override
    public void removeRequest(InterruptRequest interruptRequest) {

    }

    @Override
    public boolean hasPendingRequests() {
        return false;
    }

    @Override
    public InterruptRequest getNextRequest() {
        return null;
    }
}
