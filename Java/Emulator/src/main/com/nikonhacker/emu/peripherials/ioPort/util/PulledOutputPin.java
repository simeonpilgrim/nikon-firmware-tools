package com.nikonhacker.emu.peripherials.ioPort.util;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

// This pin gets specific value from begining
public class PulledOutputPin extends Pin {
    public PulledOutputPin(String name, int initialValue) {
        super(name);
        // VICNE WORKAROUND:
        // Note: as we're in the constructor, no pin is connected yet,
        // so instead of calling setOutputValue() which would log a warning, just assign the field directly...
        outputValue = initialValue;
    }

    @Override
    public Integer getInputValue() {
        throw new RuntimeException("getInputValue() should never be called");
    }

    @Override
    public boolean isInput() {
        return false;
    }
}
