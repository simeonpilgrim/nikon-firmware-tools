package com.nikonhacker.emu.peripherials.serialInterface.eeprom;

/**
 * This is a St950x0 eeprom of 1Kbits
 */
public class St95010 extends St950x0 {

    public St95010() {
        this(null);
    }

    public St95010(String name) {
        super(name, 128);
    }
}
