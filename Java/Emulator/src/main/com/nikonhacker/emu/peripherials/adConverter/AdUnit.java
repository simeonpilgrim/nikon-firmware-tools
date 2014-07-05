package com.nikonhacker.emu.peripherials.adConverter;

public interface AdUnit {
    public char getUnitName();
    public int getNumChannels();
    public AdValueProvider getProvider(int channel);
    public void setProvider(int channel, AdValueProvider provider);
}
