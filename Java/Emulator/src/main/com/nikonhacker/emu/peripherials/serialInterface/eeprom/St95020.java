package com.nikonhacker.emu.peripherials.serialInterface.eeprom;

/**
 * This is a St950x0 eeprom of 2Kbits
 */
public class St95020 extends St950x0 {

    public St95020() {
        this(null);
    }

    public St95020(String name) {
        super(name, 256);
    }
}
