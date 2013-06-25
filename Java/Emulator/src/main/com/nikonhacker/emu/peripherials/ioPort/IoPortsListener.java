package com.nikonhacker.emu.peripherials.ioPort;

/**
 * This is a listener to any change to any port (value or config)
 * @see com.nikonhacker.emu.peripherials.ioPort.function.PinFunction for individual bits
 */
public interface IoPortsListener {
    public void onConfigChange(int portNumber, byte direction, byte inputEnable);

    public void onOutputValueChange(int portNumber, byte newValue);
}
