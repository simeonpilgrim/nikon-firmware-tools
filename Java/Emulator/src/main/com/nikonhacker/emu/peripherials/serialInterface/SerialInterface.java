package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

import java.util.ArrayList;
import java.util.List;

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
     * @return 5 to 9 bits integer corresponding to a single value read by a device from this serial port
     */
    public abstract Integer read();

    // RECEPTION LOGIC

    /**
     * Sets the data received via Serial port
     * This can only be called by external software to simulate data writing by another device
     * @param value 5 to 9 bits integer corresponding to a single value written by a device to this serial port
     */
    public abstract void write(int value);

    public abstract int getNbBits();

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
}
