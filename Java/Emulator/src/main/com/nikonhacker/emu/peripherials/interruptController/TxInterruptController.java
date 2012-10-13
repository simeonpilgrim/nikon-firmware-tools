package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.interrupt.InterruptRequest;

public class TxInterruptController extends AbstractInterruptController implements InterruptController {

    public static final int ADDRESS_INTERRUPT_BEV0_IV0 = 0x80000180;
    public static final int ADDRESS_INTERRUPT_BEV0_IV1 = 0x80000200;
    public static final int ADDRESS_INTERRUPT_BEV1_IV0 = 0xBFC00380;
    public static final int ADDRESS_INTERRUPT_BEV1_IV1 = 0xBFC00400;

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
