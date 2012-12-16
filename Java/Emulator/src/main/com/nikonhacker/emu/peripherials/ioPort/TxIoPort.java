package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.Constants;
import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.TxInterruptController;

import java.util.ArrayList;
import java.util.List;

public class TxIoPort extends IoPort {

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
    private byte inputEnableControlRegister;

    private List<IoPortListener> ioPortListeners = new ArrayList<IoPortListener>();

    private Prefs prefs;

    public TxIoPort(int portNumber, InterruptController interruptController, Prefs prefs) {
        super(portNumber, interruptController);
        this.prefs = prefs;
        externalValue = prefs.getPortValue(Constants.CHIP_TX, portNumber);
   }

    public void addIoPortListener(IoPortListener ioPortListener) {
        ioPortListeners.add(ioPortListener);
    }

    public void removeIoPortListener(IoPortListener ioPortListener) {
        ioPortListeners.remove(ioPortListener);
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
        this.internalValue = value;
        for (IoPortListener ioPortListener : ioPortListeners) {
            ioPortListener.onOutputValueChange(portNumber, value);
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
        for (IoPortListener ioPortListener : ioPortListeners) {
            ioPortListener.onConfigChange(portNumber, controlRegister);
        }
    }

    public byte getFunctionRegister1() {
        return functionRegister1;
    }

    public void setFunctionRegister1(byte functionRegister1) {
        this.functionRegister1 = functionRegister1;
    }

    public byte getFunctionRegister2() {
        return functionRegister2;
    }

    public void setFunctionRegister2(byte functionRegister2) {
        this.functionRegister2 = functionRegister2;
    }

    public byte getFunctionRegister3() {
        return functionRegister3;
    }

    public void setFunctionRegister3(byte functionRegister3) {
        this.functionRegister3 = functionRegister3;
    }

    public byte getOpenDrainControlRegister() {
        return openDrainControlRegister;
    }

    public void setOpenDrainControlRegister(byte openDrainControlRegister) {
        this.openDrainControlRegister = openDrainControlRegister;
    }

    public byte getPullUpControlRegister() {
        return pullUpControlRegister;
    }

    public void setPullUpControlRegister(byte pullUpControlRegister) {
        this.pullUpControlRegister = pullUpControlRegister;
    }

    public byte getInputEnableControlRegister() {
        return inputEnableControlRegister;
    }

    public void setInputEnableControlRegister(byte inputEnableControlRegister) {
        this.inputEnableControlRegister = inputEnableControlRegister;
    }
}
