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
     * This method indicates that the target of this device is targetDevice, and that the source of targetDevice is this
     * @param targetDevice
     */
    public void connectTargetDevice(SerialDevice targetDevice);

    /**
     * Method used to know what target device is currently called when transmitting
     * @return
     */
    public SerialDevice getTargetDevice();

    /**
     * Method used to set the target device that will be called when transmitting.
     * Normally called by connectSerialDevice()
     */
    public void setTargetDevice(SerialDevice targetDevice);


    /**
     * Method used to know what source device this device receives data from.
     * This field is mainly a way for intermediate loggers to know what was the original device connected to them
     * @return
     */
    public SerialDevice getSourceDevice();


    /**
     * Method used to set the source device that this device will receive data from.
     * This field is mainly a way for intermediate loggers to set the original device connected to them
     * @return
     */
    public void setSourceDevice(SerialDevice sourceDevice);

    /**
     * Method to be called when reconfiguring data size (UART only)
     * @return
     */
    public void onBitNumberChange(SerialDevice serialDevice, int numBits);
}
