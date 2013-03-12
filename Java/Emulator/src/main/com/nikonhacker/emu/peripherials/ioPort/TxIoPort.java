package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.Constants;
import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxIoPort extends IoPort {
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

    /** Port register value as set by external devices. This value will be remembered in Prefs */
    private byte externalValue;
    /** Port register value as set by CPU */
    private byte internalValue;

    /** Port control register - 0=Input 1=Output */
    private byte controlRegister;

    /** Port function register 1 */
    private byte functionRegister1;
    /** Port function register 2 */
    private byte functionRegister2;
    /** Port function register 3 */
    private byte functionRegister3;
    /** Port open-drain control register */
    private byte openDrainControlRegister;
    /** Port pull-up control register */
    private byte pullUpControlRegister;
    /** Port input enable control register */
    private byte inputEnableControlRegister = (byte) 0xFF;

    private List<IoPortsListener> ioPortsListeners = new ArrayList<IoPortsListener>();

    private Map<Integer, IoPortPinListener> ioOutputPortPinListeners = new HashMap<Integer, IoPortPinListener>();

    private Prefs prefs;

    public TxIoPort(int portNumber, InterruptController interruptController, Prefs prefs) {
        super(portNumber, interruptController);
        this.prefs = prefs;
        externalValue = prefs.getPortValue(Constants.CHIP_TX, portNumber);
   }


    public void addIoPortsListener(IoPortsListener ioPortsListener) {
        ioPortsListeners.add(ioPortsListener);
    }

    public void removeIoPortsListener(IoPortsListener ioPortsListener) {
        ioPortsListeners.remove(ioPortsListener);
    }


    /**
     * IoOutputPortPinListener is to notify external device connected to this pin that its state
     * has been changed by the code
     */
    public void addIoOutputPortPinListener(int pin, IoPortPinListener ioOutputPortPinListener) {
        ioOutputPortPinListeners.put(pin, ioOutputPortPinListener);
    }

    public void removeIoOutputPortPinListener(int pin) {
        ioOutputPortPinListeners.remove(pin);
    }

    public void clearIoOutputPortPinListener() {
        ioOutputPortPinListeners.clear();
    }


    public TxIoPort(int portNumber, TxInterruptController interruptController) {
        super(portNumber, interruptController);
    }

    /**
     * Method to get the register value
     * @return internal values for outputs combined with the external values for inputs
     */
    public byte getValue() {
        return (byte) ((internalValue & controlRegister) | (externalValue & ~controlRegister));
    }

    /**
     * Method called by CPU to set the value
     * @return
     */
    public void setInternalValue(byte value) {
        int oldValue = this.internalValue;
        this.internalValue = value;
        for (IoPortsListener ioPortsListener : ioPortsListeners) {
            ioPortsListener.onOutputValueChange(portNumber, value);
        }

        // Warn connected device, if any
        int changedBits = oldValue ^ value;
        for (Integer bitNr : ioOutputPortPinListeners.keySet()) {
            // See if bit to which this listener is attached has changed
            int mask = 1 << bitNr;
            if (   ((controlRegister & mask) != 0) // pin is configured as output
                && ((changedBits & mask) != 0)     // state has changed
               ) {
                // Call the listener
                ioOutputPortPinListeners.get(bitNr).onPinValueChange((value & mask) != 0);
            }
        }
    }

    /**
     * Method called to get the external value, e.g.  to be remembered to Prefs
     */
    public byte getExternalValue() {
        return externalValue;
    }

    /**
     * Method called by external devices to set the value
     * @return
     */
    public void setExternalValue(byte value) {
        this.externalValue = value;
        // TODO implement interrupts (if any) ?
        prefs.setPortValue(Constants.CHIP_TX, portNumber, value);
    }

    /**
     * Get configuration register mask - called by external devices
     * @return mask: 0=Input 1=Output
     */
    public byte getControlRegister() {
        return controlRegister;
    }

    /**
     * Set configuration register mask - called by CPU
     * @param controlRegister mask: 0=Input 1=Output
     */
    public void setControlRegister(byte controlRegister) {
        this.controlRegister = controlRegister;
        for (IoPortsListener ioPortsListener : ioPortsListeners) {
            ioPortsListener.onConfigChange(portNumber, controlRegister, inputEnableControlRegister);
        }
    }

    public byte getFunctionRegister1() {
        return functionRegister1;
    }

    public void setFunctionRegister1(byte functionRegister1) {
//        System.out.println("Port #" + portNumber + " : setFunctionRegister1(0x" + Format.asHex( functionRegister1 & 0xFF, 2) + ")");
//        if (functionRegister1 != 0) {
//            throw new RuntimeException("Port #" + portNumber + " : setFunctionRegister1(0x" + Format.asHex( functionRegister1 & 0xFF, 2) + ") is not supported for the moment");
//        }
        this.functionRegister1 = functionRegister1;
    }

    public byte getFunctionRegister2() {
        return functionRegister2;
    }

    public void setFunctionRegister2(byte functionRegister2) {
//        System.out.println("Port #" + portNumber + " : setFunctionRegister2(0x" + Format.asHex( functionRegister2 & 0xFF, 2) + ")");
//        if (functionRegister1 != 0) {
//            throw new RuntimeException("Port #" + portNumber + " : setFunctionRegister2(0x" + Format.asHex( functionRegister2 & 0xFF, 2) + ") is not supported for the moment");
//        }
        this.functionRegister2 = functionRegister2;
    }

    public byte getFunctionRegister3() {
        return functionRegister3;
    }

    public void setFunctionRegister3(byte functionRegister3) {
//        System.out.println("Port #" + portNumber + " : setFunctionRegister3(0x" + Format.asHex( functionRegister3 & 0xFF, 2) + ")");
//        if (functionRegister1 != 0) {
//            throw new RuntimeException("Port #" + portNumber + " : setFunctionRegister3(0x" + Format.asHex( functionRegister3 & 0xFF, 2) + ") is not supported for the moment");
//        }
        this.functionRegister3 = functionRegister3;
    }

    public byte getOpenDrainControlRegister() {
        return openDrainControlRegister;
    }

    public void setOpenDrainControlRegister(byte openDrainControlRegister) {
//        System.out.println("Port #" + portNumber + " : setOpenDrainControlRegister(0x" + Format.asHex( openDrainControlRegister& 0xFF, 2) + ")");
        this.openDrainControlRegister = openDrainControlRegister;
    }

    public byte getPullUpControlRegister() {
        return pullUpControlRegister;
    }

    public void setPullUpControlRegister(byte pullUpControlRegister) {
//        System.out.println("Port #" + portNumber + " : setPullUpControlRegister(0x" + Format.asHex(pullUpControlRegister& 0xFF, 2) + ")");
        this.pullUpControlRegister = pullUpControlRegister;
    }

    public byte getInputEnableControlRegister() {
        return inputEnableControlRegister;
    }

    public void setInputEnableControlRegister(byte inputEnableControlRegister) {
        this.inputEnableControlRegister = inputEnableControlRegister;
        for (IoPortsListener ioPortsListener : ioPortsListeners) {
            ioPortsListener.onConfigChange(portNumber, controlRegister, inputEnableControlRegister);
        }
    }
}
