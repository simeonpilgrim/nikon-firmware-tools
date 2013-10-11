package com.nikonhacker.emu.peripherials.frontPanel;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

public class ButtonOutputPin extends Pin {
    public ButtonOutputPin(String name, int initialValue) {
        super(name);
        // Note: as we're in the constructor, no pin is connected yet,
        // so instead of calling setOutputValue() which would log a warning, just assign the field directly...
        outputValue = initialValue;
    }

    @Override
    public Integer getInputValue() {
        throw new RuntimeException("ButtonOutputPin.getInputValue() should never be called");
    }

    @Override
    public boolean isInput() {
        return false;
    }
}
