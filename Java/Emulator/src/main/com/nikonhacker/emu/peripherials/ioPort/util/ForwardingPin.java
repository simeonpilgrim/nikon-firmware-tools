package com.nikonhacker.emu.peripherials.ioPort.util;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

public class ForwardingPin extends Pin {
    protected Pin targetPin;

    public ForwardingPin(String name) {
        super(name);
    }

    @Override
    public void setInputValue(int value) {
        if (targetPin.getConnectedPin() != null) {
            targetPin.getConnectedPin().setInputValue(value);
        }
    }

    @Override
    public Integer getOutputValue() {
        if (targetPin.getConnectedPin() != null) {
            return targetPin.getConnectedPin().getOutputValue();
        }
        else {
            return null;
        }
    }

    public void setTargetPin(Pin targetPin) {
        this.targetPin = targetPin;
    }

    public Pin getTargetPin() {
        return targetPin;
    }
}
