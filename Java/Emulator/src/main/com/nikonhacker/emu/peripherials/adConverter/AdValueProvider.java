package com.nikonhacker.emu.peripherials.adConverter;

public interface AdValueProvider {
    public int getAnalogValue(char unitName, int channel);
}
