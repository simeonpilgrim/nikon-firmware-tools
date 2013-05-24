package com.nikonhacker.emu.peripherials.ioPort.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.IoPortsListener;
import com.nikonhacker.emu.peripherials.ioPort.function.InputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.OutputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    /** The 3 following arrays declare the functions that can be performed by each Port Pin
     * when the corresponding bit of the corresponding register is 1.
     * e.g. if fn1Handlers[2] is a TxIoPinSerialRxHandler, it means that
     * when bit 2 of functionRegister1 is set, then this port's pin 2 behaves as a Rx pin of a serial port.
     * Each array is 8 elements long and element [n] of port X corresponds to pin PXn.
     */
    private PinFunction[] functions1;
    private PinFunction[] functions2;
    private PinFunction[] functions3;
    private PinFunction[] inputFunctions  = {new InputPinFunction(), new InputPinFunction(), new InputPinFunction(), new InputPinFunction(), new InputPinFunction(), new InputPinFunction(), new InputPinFunction(), new InputPinFunction()};
    private PinFunction[] outputFunctions = {new OutputPinFunction(), new OutputPinFunction(), new OutputPinFunction(), new OutputPinFunction(), new OutputPinFunction(), new OutputPinFunction(), new OutputPinFunction(), new OutputPinFunction()};

    /** List of listeners to warn when the value of a port changes
     * TODO: distinguish internal/external listeners ?
     */
    private List<IoPortsListener> ioPortsListeners = new ArrayList<IoPortsListener>();

    private Prefs prefs;

    public TxIoPort(int portNumber, InterruptController interruptController, Prefs prefs) {
        super(portNumber, interruptController);
        this.prefs = prefs;
        externalValue = prefs.getPortValue(Constants.CHIP_TX, portNumber);
    }

    public void setFunctions1(PinFunction[] functions1) {
        if (functions1.length != 8) {
            throw new RuntimeException("Trying to assign a Function 1 Role array of length " + functions1.length + ": " + Arrays.toString(functions1));
        }
        this.functions1 = functions1;
    }

    public void setFunctions2(PinFunction[] functions2) {
        if (functions2.length != 8) {
            throw new RuntimeException("Trying to assign a Function 2 Role array of length " + functions2.length + ": " + Arrays.toString(functions2));
        }
        this.functions2 = functions2;
    }

    public void setFunctions3(PinFunction[] functions3) {
        if (functions3.length != 8) {
            throw new RuntimeException("Trying to assign a Function 3 Role array of length " + functions3.length + ": " + Arrays.toString(functions3));
        }
        this.functions3 = functions3;
    }

    public void addIoPortsListener(IoPortsListener ioPortsListener) {
        ioPortsListeners.add(ioPortsListener);
    }

    public void removeIoPortsListener(IoPortsListener ioPortsListener) {
        ioPortsListeners.remove(ioPortsListener);
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
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            int mask = 1 << bitNumber;
            if (   ((controlRegister & mask) != 0) // pin is configured as output
                && ((changedBits & mask) != 0)     // state has changed
               ) {
                // Set the value of the output port
                pins[bitNumber].setOutputValue(((value & mask) != 0)?1:0);
            }
        }
    }

    /**
     * Method called to get the external value, e.g. to be remembered to Prefs
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
        this.functionRegister1 = functionRegister1;
        updatePinHandlers();
    }

    public byte getFunctionRegister2() {
        return functionRegister2;
    }

    public void setFunctionRegister2(byte functionRegister2) {
        this.functionRegister2 = functionRegister2;
        updatePinHandlers();
    }

    public byte getFunctionRegister3() {
        return functionRegister3;
    }

    public void setFunctionRegister3(byte functionRegister3) {
        this.functionRegister3 = functionRegister3;
        updatePinHandlers();
    }

    private void updatePinHandlers() {
        for (int pinNumber = 0; pinNumber < 8; pinNumber++) {
            int pinMask = 1 << pinNumber;
            if (functions1 != null && (functionRegister1 & pinMask & 0xFF) != 0) {
                // fn1 defines the behaviour of this pin
                pins[pinNumber].setFunction(functions1[pinNumber]);
            }
            else if (functions2 != null && (functionRegister2 & pinMask & 0xFF) != 0) {
                // fn2 defines the behaviour of this pin
                pins[pinNumber].setFunction(functions2[pinNumber]);
            }
            else if (functions3 != null && (functionRegister3 & pinMask & 0xFF) != 0) {
                // fn3 defines the behaviour of this pin
                pins[pinNumber].setFunction(functions3[pinNumber]);
            }
            else if ((controlRegister & pinMask) != 0) {
                // pin is configured as plain output
                pins[pinNumber].setFunction(outputFunctions[pinNumber]);
            }
            else {
                // pin is configured as plain input
                pins[pinNumber].setFunction(inputFunctions[pinNumber]);
            }
        }
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
