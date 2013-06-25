package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;

/**
 * This particular pin can be dynamically assigned a PinFunction to define or change its behaviour
 */
public class VariableFunctionPin extends Pin {
    private PinFunction function;

    public VariableFunctionPin(String name) {
        super(name);
    }

    public void setFunction(PinFunction function) {
        this.function = function;
    }

    public PinFunction getFunction() {
        return function;
    }

    /** To be called by external component */
    public void setInputValue(int value) {
        super.setInputValue(value);
        if (function instanceof AbstractInputPinFunction) {
            ((AbstractInputPinFunction) function).setValue(value);
        }
    }

    /** To be called by CPU code */
    public void setOutputValue(int value) {
        if (function instanceof AbstractInputPinFunction) {
            System.err.println("Code is trying to set pin " + getName() + " to " + value + " although it is " +
                    (function==null
                     ?"not configured as input"
                     :("configured as " + function.getClass().getSimpleName())
                    )
                    + ". Outputting anyway...");
        }
        super.setOutputValue(value);
    }

    public String getFunctionFullName() {
        return function!=null?function.getFullName():getName();
    }

    public String getFunctionShortName() {
        return function!=null?function.getShortName():getName();
    }

}
