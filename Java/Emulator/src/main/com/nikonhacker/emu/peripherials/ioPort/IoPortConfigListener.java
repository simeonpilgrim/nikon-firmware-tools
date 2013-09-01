package com.nikonhacker.emu.peripherials.ioPort;

/**
 * This is a listener to any change of configuration to any port
 * @see com.nikonhacker.emu.peripherials.ioPort.function.PinFunction for individual bits
 */
public interface IoPortConfigListener {
    public void onConfigChange(int portNumber);
}
