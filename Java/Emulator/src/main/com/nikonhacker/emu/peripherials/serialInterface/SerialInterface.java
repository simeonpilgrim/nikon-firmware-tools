package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

/**
 * This class is the base class for emulation of the serial interface of a microcontroller.
 *
 * Note: the names are misleading compared to Java Object model terminology, but here it is:
 * - SerialDevice is a java "Interface"
 * - SerialInterface is one java "Class" implementing the SerialDevice interface
 *
 * Serial interfaces are emulated at the value level (byte or group of 5-9 bits), not at the electric (bit) level.
 * Consequently, all information regarding clocks and edges is ignored.
 *
 * The logic is as follows:
 * To transmit data, a microcontroller writes to its serial interface's registers, and as soon as one value is "ready
 * for reading" (equivalent of the physical transmission of the last bit), the SerialInterface calls the valueReady()
 * method implemented here.
 *
 * In the other direction, a "device" (either an actual serial interface, a GUI input or any source)
 * calls the write() method of a SerialInterface to transmit a value to it. The implementation of write() must set
 * registers of the receiving serial interface to the corresponding value, and can inform the
 * microcontroller (via interrupt) that new data is available. That microcontroller will then read the
 * serial interface registers and act accordingly.
 */
public abstract class SerialInterface extends AbstractSerialDevice {
    protected final int                 serialInterfaceNumber;
    protected final InterruptController interruptController;
    protected final Emulator            emulator;

    public SerialInterface(int serialInterfaceNumber, InterruptController interruptController, Emulator emulator) {
        this.serialInterfaceNumber = serialInterfaceNumber;
        this.interruptController = interruptController;
        this.emulator = emulator;
        // By default, a Serial Interface is connected to a dummy device
        DummySerialDevice dummySerialDevice = new DummySerialDevice();
        connectTargetDevice(dummySerialDevice);
        dummySerialDevice.connectTargetDevice(this);
    }

    public int getSerialInterfaceNumber() {
        return serialInterfaceNumber;
    }

    /**
     * Gets the data transmitted via Serial port
     * This can only be called by external software to simulate data reading by another device
     * @return 5 to 9 bits integer corresponding to a single value read by a device from this serial port,
     * or null in case no value is ready
     */
    public abstract Integer read();

    // RECEPTION LOGIC

    /**
     * Sets the data received via Serial port
     * This can only be called by external software to simulate data writing by another device
     * @param value integer (5 to 9 bits) corresponding to a single value written by an external device to this serial port
     */
    public abstract void write(Integer value);

    public abstract int getNumBits();

    public abstract String getName();

    public void bitNumberChanged(int nbBits) {
        targetDevice.onBitNumberChange(this, nbBits);
    }

    public void valueReady(Integer value) {
        targetDevice.write(value);
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " #" + serialInterfaceNumber;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        if (getNumBits() != numBits) {
            System.err.println(toString() + ": Serial device (" + serialDevice + ") tries to switch to " + numBits + " while this device is in " + getNumBits() + " bits...");
        }
    }

}
