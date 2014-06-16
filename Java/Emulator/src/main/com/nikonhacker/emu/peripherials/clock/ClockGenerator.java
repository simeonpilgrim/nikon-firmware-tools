package com.nikonhacker.emu.peripherials.clock;

import com.nikonhacker.emu.Platform;

public abstract class ClockGenerator {
    protected Platform platform;

    public ClockGenerator() {
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
}
