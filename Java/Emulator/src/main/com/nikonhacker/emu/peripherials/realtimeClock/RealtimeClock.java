package com.nikonhacker.emu.peripherials.realtimeClock;

import com.nikonhacker.emu.Platform;

public abstract class RealtimeClock {
    protected Platform platform;

    public RealtimeClock(Platform platform) {
        this.platform = platform;
    }

    public Platform getPlatform() {
        return platform;
    }
}
