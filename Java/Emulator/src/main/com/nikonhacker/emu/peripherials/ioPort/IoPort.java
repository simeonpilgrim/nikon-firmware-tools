package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

import java.util.ArrayList;
import java.util.List;

public abstract class IoPort {

    public static final boolean DEBUG  = false;

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


    /**
     * List of listeners to warn when the configuration of a port changes
     */
    protected List<IoPortConfigListener> ioPortConfigListeners = new ArrayList<IoPortConfigListener>();

    public IoPort(int portNumber, InterruptController interruptController) {
        this.portNumber = portNumber;
        this.interruptController = interruptController;
        pins = new VariableFunctionPin[8];
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            pins[bitNumber] = new VariableFunctionPin(getShortName() + bitNumber);
        }
    }

    /**
     * Method called by CPU to get the value
     */
    public byte getValue() {
        byte value = 0;
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            int mask = 1 << bitNumber;
            if (pins[bitNumber].isInput()) {
                // IN
                Integer inputValue = pins[bitNumber].getInputValue();
                // dangling inputs are considered low level
                if (inputValue != null && inputValue != 0) {
                    value |= mask;
                }
            }
            else {
                // OUT
                Integer outputValue = pins[bitNumber].getOutputValue();
                if (outputValue != null) {
                    if (outputValue != 0) {
                        value |= mask;
                    }
                }
                else {
                    if (DEBUG) System.err.println("OutputValue is null for pin " + pins[bitNumber].getName());
                }
            }
        }
        return value;
    }

    /**
     * Method called by CPU to set the value
     */
    public void setValue(byte newValue) {
        // Forward to individual output pins
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            int mask = 1 << bitNumber;
            if (!pins[bitNumber].isInput()) {
                // pin is configured as output. Set the value of the output port
                pins[bitNumber].setOutputValue(((newValue & mask) != 0)?1:0);
            }
        }
    }

    /**
     * Port pin direction mask - 0=Input 1=Output
     */
    public byte getDirection() {
        byte direction = 0;
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            if (!getPin(bitNumber).isInput()) {
                direction |= (1 << bitNumber);
            }
        }
        return direction;
    }

    /**
     * Port pin direction mask - 0=Input 1=Output
     */
    public void setDirection(byte direction) {
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            getPin(bitNumber).setIsInput((direction & (1 << bitNumber)) == 0);
        }
        for (IoPortConfigListener ioPortConfigListener : ioPortConfigListeners) {
            ioPortConfigListener.onConfigChange(portNumber);
        }
    }

    /**
     * Port pin input enabled mask - 0=Disabled 1=Enabled
     */
    public byte getInputEnabled() {
        byte inputEnabled = 0;
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            if (!getPin(bitNumber).isInputEnabled()) {
                inputEnabled |= (1 << bitNumber);
            }
        }
        return inputEnabled;
    }

    /**
     * Port pin input enabled mask - 0=Disabled 1=Enabled
     */
    public void setInputEnabled(byte inputEnabled) {
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            getPin(bitNumber).setIsInputEnabled((inputEnabled & (1 << bitNumber)) == 0);
        }
        for (IoPortConfigListener ioPortConfigListener : ioPortConfigListeners) {
            ioPortConfigListener.onConfigChange(portNumber);
        }
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

    public void addIoPortsListener(IoPortConfigListener ioPortConfigListener) {
        ioPortConfigListeners.add(ioPortConfigListener);
    }

    public void removeIoPortsListener(IoPortConfigListener ioPortConfigListener) {
        ioPortConfigListeners.remove(ioPortConfigListener);
    }
}
