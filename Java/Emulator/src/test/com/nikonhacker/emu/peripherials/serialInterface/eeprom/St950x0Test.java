package com.nikonhacker.emu.peripherials.serialInterface.eeprom;

import com.nikonhacker.emu.peripherials.serialInterface.BidirectionalConsoleLoggerSerialCable;
import com.nikonhacker.emu.peripherials.serialInterface.TestDevice;

public class St950x0Test {

    public static void main(String[] args) {
        System.err.println("Starting...");

        TestDevice d1 = new TestDevice("Tester");
        St950x0 d2 = new St950x0("Eeprom") ;

        BidirectionalConsoleLoggerSerialCable cable = new BidirectionalConsoleLoggerSerialCable(d1, d2);
        cable.connect();

        d1.sendBytes();

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            // noop
        }
    }
}
