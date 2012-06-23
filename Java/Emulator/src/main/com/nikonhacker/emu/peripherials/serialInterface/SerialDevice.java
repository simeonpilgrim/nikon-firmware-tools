package com.nikonhacker.emu.peripherials.serialInterface;

/**
 * External emulated devices that wish to be connected to the FR's Serial Interface must :
 * - implement this interface to be informed when data transmitted by the FR MCU is ready and when config changes
 * - call SerialInterface.read() to obtain these values
 * - call SerialInterface.write() to transmit data back to the MCU
 */

public interface SerialDevice {
    public void onValueReady(SerialInterface serialInterface);
    public void onBitNumberChange(SerialInterface serialInterface, int nbBits);
}
