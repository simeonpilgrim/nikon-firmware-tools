package com.nikonhacker.emu.peripherials.serialInterface.fr;

import com.nikonhacker.emu.MasterClock;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.interruptController.DummyInterruptController;
import junit.framework.TestCase;

public class FrSerialInterfaceTest extends TestCase {
    public void testInterface() {

        MasterClock masterClock = new MasterClock();
        Platform platform = new Platform(masterClock);
        platform.setInterruptController(new DummyInterruptController());
        FrSerialInterface serialInterface = new FrSerialInterface(5, platform, true);
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
