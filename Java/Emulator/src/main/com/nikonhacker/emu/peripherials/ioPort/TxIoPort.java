package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.emu.peripherials.interruptController.TxInterruptController;

public class TxIoPort extends IoPort {


    /** Port register (value) */
    private byte value;
    /** Port control register */
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

    public TxIoPort(int portNumber, TxInterruptController interruptController) {
        super(portNumber, interruptController);
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    public byte getControlRegister() {
        return controlRegister;
    }

    public void setControlRegister(byte controlRegister) {
        this.controlRegister = controlRegister;
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
