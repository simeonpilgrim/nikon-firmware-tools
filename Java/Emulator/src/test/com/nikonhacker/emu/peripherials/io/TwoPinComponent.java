package com.nikonhacker.emu.peripherials.io;

import com.nikonhacker.emu.peripherials.ioPort.Pin;
import com.nikonhacker.emu.peripherials.ioPort.VariableFunctionPin;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;

public class TwoPinComponent {
    private Pin    inputPin;
    private Pin    outputPin;
    private String name;

    // Registers
    private int rx;
    private int tx;

    public TwoPinComponent(String name) {
        inputPin = new VariableFunctionPin(name + ".IN");
        outputPin = new Pin(name + ".OUT");
        ((VariableFunctionPin)inputPin).setFunction(new AbstractInputPinFunction("TWO-PIN") {
            public void setValue(int value) {
                setRx(value);
            }

            @Override
            public String getShortName() {
                return getName() + ".INTST";
            }

            public String getFullName() {
                return getName() + " test input";
            }
        });

        this.name = name;
    }

    public Pin getInputPin() {
        return inputPin;
    }

    public Pin getOutputPin() {
        return outputPin;
    }

    public String getName() {
        return name;
    }

    public int getRx() {
        return rx;
    }

    public void setRx(int value) {
        this.rx = value;
    }

    public int getTx() {
        return tx;
    }

    public void setTx(int value) {
        this.tx = value;
        if (outputPin.getConnectedPin() == null) {
            System.out.println("No pin connected to " + outputPin);
        }
        else {
            outputPin.getConnectedPin().setInputValue(value);
        }
    }
}
