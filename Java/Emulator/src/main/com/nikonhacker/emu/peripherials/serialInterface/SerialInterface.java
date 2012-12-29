package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public abstract class SerialInterface {
    protected final int serialInterfaceNumber;
    protected final InterruptController interruptController;
    protected SerialDevice serialDevice = new NullSerialDevice();

    public SerialInterface(int serialInterfaceNumber, InterruptController interruptController) {
        this.serialInterfaceNumber = serialInterfaceNumber;
        this.interruptController = interruptController;
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

    public void connect(SerialDevice serialDevice) {
        this.serialDevice = serialDevice;
    }

    public abstract int getNbBits();

    public String getName() {
        return "Serial #" + serialInterfaceNumber;
    }
}
