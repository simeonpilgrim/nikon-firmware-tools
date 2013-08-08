package com.nikonhacker.emu.peripherials.clock.fr;

import com.nikonhacker.emu.peripherials.clock.ClockGenerator;

public class FrClockGenerator extends ClockGenerator {
    /** FREQUENCY @132MHz seems to be a frequently documented value */
    public static final int FREQUENCY      = 132_000_000; // 132MHz
    /** PCLK @50MHz was determined based on the system clock ticking every ms */
    public final static int PCLK_FREQUENCY = 50_000_000;

    public FrClockGenerator() {
        super();
    }
}
