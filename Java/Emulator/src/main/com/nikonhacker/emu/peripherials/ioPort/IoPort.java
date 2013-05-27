package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

import java.util.ArrayList;
import java.util.List;

public abstract class IoPort {

    public static final int PORT_0 = 0;
    public static final int PORT_1 = 1;
    public static final int PORT_2 = 2;
    public static final int PORT_3 = 3;
    public static final int PORT_4 = 4;
    public static final int PORT_5 = 5;
    public static final int PORT_6 = 6;
    public static final int PORT_7 = 7;
    public static final int PORT_8 = 8;
    public static final int PORT_9 = 9;
    public static final int PORT_A = 10;
    public static final int PORT_B = 11;
    public static final int PORT_C = 12;
    public static final int PORT_D = 13;
    public static final int PORT_E = 14;
    public static final int PORT_F = 15;
    public static final int PORT_G = 16;
    public static final int PORT_H = 17;
    public static final int PORT_I = 18;
    public static final int PORT_J = 19;


    /** This is the number of this port */
    protected int portNumber;

    /** InterruptController is passed to constructor to be able to actually trigger requests */
    protected InterruptController interruptController;

    protected VariableFunctionPin[] pins;

    /** Port register value as set by external devices. This value will be remembered in Prefs */
    protected byte externalValue;
    /** Port register value as set by CPU */
    protected byte internalValue;

    /** Port direction mask - 0=Input 1=Output */
    protected byte direction;

    /** List of listeners to warn when the value of a full port changes
     * TODO: does this have sense ? Shouldn't we only work at the pin level ?
     * TODO: distinguish internal/external listeners ?
     */
    protected List<IoPortsListener> ioPortsListeners = new ArrayList<IoPortsListener>();

    public IoPort(int portNumber, InterruptController interruptController) {
        this.portNumber = portNumber;
        this.interruptController = interruptController;
        pins = new VariableFunctionPin[8];
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            pins[bitNumber] = new VariableFunctionPin(getShortName() + bitNumber);
        }
    }

    public InterruptController getInterruptController() {
        return interruptController;
    }

    public byte getInternalValue() {
        return internalValue;
    }

    /**
     * Method called by CPU to set the value
     */
    public void setInternalValue(byte newValue) {
        for (IoPortsListener ioPortsListener : ioPortsListeners) {
            ioPortsListener.onOutputValueChange(portNumber, newValue);
        }

        // Warn connected device, if any
        int changedBits = internalValue ^ newValue; // todo : if changebits != 0
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            int mask = 1 << bitNumber;
            if (   ((direction & mask) != 0) // pin is configured as output
                    && ((changedBits & mask) != 0)     // state has changed
                    ) {
                // Set the value of the output port
                pins[bitNumber].setOutputValue(((newValue & mask) != 0)?1:0);
            }
        }
        this.internalValue = newValue;
    }

    /**
     * Method called to get the external value, e.g. to be remembered to Prefs
     */
    public byte getExternalValue() {
        return externalValue;
    }

    /**
     * Method called by external devices to set the value
     */
    public void setExternalValue(byte externalValue) {
        this.externalValue = externalValue;
    }

    public byte getDirection() {
        return direction;
    }

    public void setDirection(byte direction) {
        this.direction = direction;
    }

    private String getPortCharacter() {
        return ((portNumber < 10)?String.valueOf(portNumber):String.valueOf((char)('A' - 10 + portNumber)));  // 0-9 then A-Z
    }

    protected String getShortName() {
        return "P" + getPortCharacter();
    }

    protected String getFullName() {
        return "IoPort " + getPortCharacter();
    }

    public VariableFunctionPin getPin(int pinNumber) {
        return pins[pinNumber];
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public void addIoPortsListener(IoPortsListener ioPortsListener) {
        ioPortsListeners.add(ioPortsListener);
    }

    public void removeIoPortsListener(IoPortsListener ioPortsListener) {
        ioPortsListeners.remove(ioPortsListener);
    }
}
