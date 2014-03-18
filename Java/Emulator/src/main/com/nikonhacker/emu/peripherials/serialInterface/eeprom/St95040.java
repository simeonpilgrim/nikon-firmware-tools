package com.nikonhacker.emu.peripherials.serialInterface.eeprom;

/**
 * This is a St950x0 eeprom of 4Kbits
 */
public class St95040 extends St950x0 {

    public St95040() {
        this(null);
    }

    public St95040(String name) {
        super(name, 512);
    }
}
