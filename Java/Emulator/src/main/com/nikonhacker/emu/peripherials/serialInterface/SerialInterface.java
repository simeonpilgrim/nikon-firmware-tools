package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the base class for emulation of a Serial Interface.
 *
 * Serial interfaces are emulated at the value level (byte or group of 5-9 bits), not at the electric (bit) level.
 * Consequently, all information regarding clocks and edges can be ignored.
 *
 * The logic is as follows:
 * To transmit data, a microcontroller writes to its serial interface's registers, and as soon as one value is "ready
 * for reading" (equivalent of the physical transmission of the last bit), the sending corresponding SerialInterface
 * calls the valueReady() method implemented here, which informs all registered listeners that a value can be read.
 * The configuration MUST be such that exactly one of those listeners then calls the read() method of that serial
 * interface, getting the actual value but also informing the sender that the next value can be sent.
 * This listener can be a dummy value reader, a logger, or a connection to another Serial Interface's RX pin
 *
 * In the other direction, a "device" (either a listener to another serial interface as described above, or a GUI input)
 * calls the write() method of a SerialInterface to transmit a value to it. The implementation of write() must set
 * registers of the receiving serial interface to the corresponding value, and can inform the
 * microcontroller (via interrupt) that new data is available. That microcontroller will then read the
 * serial interface registers and act accordingly.
 *
 * TODO ? Note: onValueReady() could include the actual value as a param to help debugging by adding several listeners (loggers for example)
 * TODO ? But maybe having several listeners is a bad idea in the end, and logging should be performed by chaining listeners and piping values
 */
public abstract class SerialInterface {
    protected final int serialInterfaceNumber;
    protected final InterruptController interruptController;
    protected List<SerialInterfaceListener> listeners = new ArrayList<SerialInterfaceListener>();

    public SerialInterface(int serialInterfaceNumber, InterruptController interruptController) {
        this.serialInterfaceNumber = serialInterfaceNumber;
        this.interruptController = interruptController;
        // By default, a Serial Interface uses a dummy
        addSerialValueReadyListener(new ByteEaterSerialInterfaceListener());
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
     * @param value 5 to 9 bits integer corresponding to a single value written by a device to this serial port
     */
    public abstract void write(int value);

    public abstract int getNumBits();


    public String getName() {
        return "Serial #" + serialInterfaceNumber;
    }

    public void addSerialValueReadyListener(SerialInterfaceListener listener) {
        listeners.add(listener);
    }

    public void removeSerialValueReadyListener(SerialInterfaceListener listener) {
        listeners.remove(listener);
    }

    public void clearSerialValueReadyListeners() {
        listeners.clear();
    }

    public void bitNumberChanged(int nbBits) {
        for (SerialInterfaceListener listener : listeners) {
            listener.onBitNumberChange(this, nbBits);
        }
    }

    public void valueReady() {
        for (SerialInterfaceListener listener : listeners) {
            listener.onValueReady(this);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
