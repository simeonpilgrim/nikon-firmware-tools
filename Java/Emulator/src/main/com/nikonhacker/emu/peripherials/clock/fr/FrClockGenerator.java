package com.nikonhacker.emu.peripherials.clock.fr;

import com.nikonhacker.emu.peripherials.clock.ClockGenerator;

public class FrClockGenerator implements ClockGenerator {
    /** PCLK @50MHz was determined based on the system clock ticking every ms */
    public final static int PCLK_FREQUENCY = 50000000; //Hz, or 50MHz
}
