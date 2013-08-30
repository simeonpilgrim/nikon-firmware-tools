package com.nikonhacker.emu.peripherials.ioPort;

import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractOutputPinFunction;
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
        if (function instanceof AbstractInputPinFunction && getConnectedPin() != null) {
            ((AbstractInputPinFunction) function).setValue(getConnectedPin().getOutputValue());
        }
    }

    /**
     * To be called by CPU code
     */
    @Override
    public Integer getInputValue() {
        // Just check that this input pin is configured as input
        if (!(function instanceof AbstractInputPinFunction)) {
            if (logPinMessages) {
                System.err.println("Code is trying to read pin " + getName() + " although it is " +
                        (function==null
                                ?"not configured as input"
                                :("configured as " + function.getClass().getSimpleName())
                        )
                        + ". Forwarding request anyway...");
            }
        }

        // Normal behaviour
        return super.getInputValue();
    }

    /**
     * To be called by external component
     * This implementation delegates the behaviour to the attached function, if any
     */
    public void setInputValue(int value) {
        if (function instanceof AbstractInputPinFunction) {
            ((AbstractInputPinFunction) function).setValue(value);
        }
        else {
            if (logPinMessages) {
                System.err.println("A component connected to pin " + getName() + " tries to set its value to " + value + " although it is " +
                        (function==null
                                ?"not configured as input"
                                :("configured as " + function.getClass().getSimpleName())
                        )
                        + ". Forwarding request anyway...");
            }
        }
    }

    /**
     * To be called by external component
     * This implementation delegates the behaviour to the attached function, if any
     */
    @Override
    public Integer getOutputValue() {
        if (function instanceof AbstractOutputPinFunction) {
            return ((AbstractOutputPinFunction) function).getValue(outputValue);
        }
        else {
            if (logPinMessages) {
                System.err.println("A component connected to pin " + getName() + " tries to read its value although it is " +
                        (function==null
                                ?"not configured as output"
                                :("configured as " + function.getClass().getSimpleName())
                        )
                        + ". Returning null...");
            }
        }
        return null;
    }

    /**
     * To be called by CPU code
     */
    @Override
    public void setOutputValue(int value) {
        // Just check that this input pin is configured as output
        if (!(function instanceof AbstractOutputPinFunction)) {
            if (logPinMessages) {
                System.err.println("Code is trying to set pin " + getName() + " to " + value + " although it is " +
                        (function==null
                         ?"not configured as output"
                         :("configured as " + function.getClass().getSimpleName())
                        )
                        + ". Outputting anyway...");
            }
        }

        // Normal behaviour
        super.setOutputValue(value);
    }

    public String getFunctionFullName() {
        return function!=null?function.getFullName():getName();
    }

    public String getFunctionShortName() {
        return function!=null?function.getShortName():getName();
    }

}
