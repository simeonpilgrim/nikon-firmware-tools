package com.nikonhacker.emu.peripherials.frontPanel;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.ioPort.Pin;

public class ButtonOutputPin extends Pin {
    private String key;
    private Prefs prefs;

    public ButtonOutputPin(String key, Prefs prefs, boolean isReversed) {
        super(key + " button");
        this.key = key;
        this.prefs = prefs;

        // Initialize with last stored value
        Integer buttonValue = prefs.getButtonValue(key);
        // Note: as no pin is connected yet, avoid calling setOutputValue() which would log a warning...
        if (buttonValue==null) {
            outputValue = isReversed?1:0;
        }
        else {
            outputValue = buttonValue;
        }
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
