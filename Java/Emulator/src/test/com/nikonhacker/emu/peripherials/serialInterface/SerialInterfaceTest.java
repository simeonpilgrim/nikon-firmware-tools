package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.peripherials.interruptController.DummyInterruptController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import junit.framework.TestCase;

public class SerialInterfaceTest extends TestCase {
    public void testInterface() {

        InterruptController interruptController = new DummyInterruptController();
        SerialInterface serialInterface = new SerialInterface(5, interruptController, 0x1B);
        serialInterface.setScrIbcr(0);
        serialInterface.setScrIbcr(0xB0);
        serialInterface.setSmr(0x45);
        serialInterface.setEscrIbsr(0);
        serialInterface.setScrIbcr(0x40);
        serialInterface.setFcr0(0x0F);
        serialInterface.setScrIbcr(0x43);
        serialInterface.setScrIbcr(serialInterface.getScrIbcr() & 0xD);
        serialInterface.setFbyte2(2);
        serialInterface.setFbyte1(0);
        serialInterface.setScrIbcr(serialInterface.getScrIbcr() | 0x2);
        serialInterface.setTdr(1);
        serialInterface.setTdr(3);

    }
}