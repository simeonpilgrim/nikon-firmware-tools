package com.nikonhacker.emu.peripherials.ioPort;

/**
 * This is a listener to any change to any port (value or config)
 * @see IoPortPinListener for individual bits
 */
public interface IoPortsListener {
    public void onConfigChange(int portNumber, byte config, byte inputEnable);

    public void onOutputValueChange(int portNumber, byte newValue);
}
