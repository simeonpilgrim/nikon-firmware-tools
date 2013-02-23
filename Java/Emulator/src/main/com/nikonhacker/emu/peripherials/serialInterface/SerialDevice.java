package com.nikonhacker.emu.peripherials.serialInterface;

/**
 * Note: the names are misleading compared to Java Object model terminology, but here it is:
 * - SerialDevice is a java "Interface"
 * - SerialInterface is one java "Class" implementing the SerialDevice interface
 *
 * Reception by an implementing class is supported via the "write" method
 * Transmission by an implementing class is supported by calling the "write" method of the
 * connected serial device (which has been passed via "connectSerialDevice")
 *
 */
public interface SerialDevice {
    /**
     * Method to be used by another device to transmit to this device
     * @param value
     */
    public void write(Integer value);

    /**
     * Method used to connect another device that will receive from this device.
     * The write method of that SerialDevice will be called when there is data transmitted by this SerialDevice
     * Only one device can be connected. If you want to sniff traffic, insert a SerialWire in between
     * @param connectedDevice
     */
    public void connectSerialDevice(SerialDevice connectedDevice);

    /**
     * Method used to disconnect the another device.
     * Normally, this is implemented as a replacement of the connectedDevice by a DummySerialDevice that will
     * just read bytes sent and throw them away
     * @see DummySerialDevice
     */
    public void disconnectSerialDevice();

    /**
     * Method used to know what is the connected device that is currently called when transmitting
     * @return
     */
    public SerialDevice getConnectedSerialDevice();

    /**
     * Method to be called when reconfiguring data size (UART only)
     * @return
     */
    public void onBitNumberChange(SerialDevice serialDevice, int nbBits);
}
