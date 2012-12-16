package com.nikonhacker.emu.peripherials.ioPort;

public interface IoPortListener {
    public void onConfigChange(int portNumber, byte config);

    public void onOutputValueChange(int portNumber, byte newValue);
}
