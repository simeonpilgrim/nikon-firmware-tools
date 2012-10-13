package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.interrupt.InterruptRequest;

public class TxInterruptController extends AbstractInterruptController implements InterruptController {
    @Override
    public boolean request(int interruptNumber) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeRequest(int requestNumber) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean request(InterruptRequest interruptRequest) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
