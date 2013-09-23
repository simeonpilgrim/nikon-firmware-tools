package com.nikonhacker.emu.peripherials.frontPanel;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.ioPort.Pin;

public class ButtonOutputPin extends Pin {
    private String key;
    private Prefs prefs;

    public ButtonOutputPin(String key, Prefs prefs) {
        super(key + " button");
        this.key = key;
        this.prefs = prefs;
        Integer buttonValue = prefs.getButtonValue(key);
        // Initialize with last stored value
        // Note: as no pin is connected yet, avoid calling setOutputValue() which would log a warning...
        outputValue = (buttonValue==null ? 0 : buttonValue);
    }

    @Override
    public Integer getInputValue() {
        throw new RuntimeException("ButtonOutputPin.getInputValue() should never be called");
    }

    @Override
    public void setOutputValue(int value) {
        prefs.setButtonValue(key, value);
        super.setOutputValue(value);
    }

    @Override
    public boolean isInput() {
        return false;
    }
}
