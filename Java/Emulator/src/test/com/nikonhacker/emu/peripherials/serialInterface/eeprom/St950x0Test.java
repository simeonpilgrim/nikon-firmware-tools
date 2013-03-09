package com.nikonhacker.emu.peripherials.serialInterface.eeprom;

import com.nikonhacker.emu.peripherials.serialInterface.BidirectionalConsoleLoggerSerialCable;

public class St950x0Test {

    public static void main(String[] args) {
        System.err.println("Starting...");

        St950x0TesterDevice tester = new St950x0TesterDevice("Tester");
        St950x0 eeprom = new St95040("Eeprom");

        BidirectionalConsoleLoggerSerialCable cable = new BidirectionalConsoleLoggerSerialCable(tester, eeprom);
        cable.connect();

        System.err.println("RDSR");
        eeprom.setSelected(true);
        tester.doSendRdsr();
        eeprom.setSelected(false);

        System.err.println("WRITE without a WREN: should fail");
        eeprom.setSelected(true);
        tester.doSendWrite();
        eeprom.setSelected(false);

        System.err.println("READ: check that write failed");
        eeprom.setSelected(true);
        tester.doSendRead();
        eeprom.setSelected(false);

        System.err.println("WREN");
        eeprom.setSelected(true);
        tester.doSendWren();
        eeprom.setSelected(false);

        System.err.println("WRITE with a WREN: should succeed");
        eeprom.setSelected(true);
        tester.doSendWrite();
        eeprom.setSelected(false);

        System.err.println("READ: check that write succeeded");
        eeprom.setSelected(true);
        tester.doSendRead();
        eeprom.setSelected(false);

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            // noop
        }
    }
}
