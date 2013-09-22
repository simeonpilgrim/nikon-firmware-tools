package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

public abstract class SpiSlaveDevice extends SerialDevice {

    protected boolean selected = false;
    private Pin selectPin;

    protected SpiSlaveDevice() {
        selectPin = new SelectPin(this.getClass().getSimpleName() + " ~SELECT pin");
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Pin getSelectPin() {
        return selectPin;
    }

    private class SelectPin extends Pin {
        public SelectPin(String name) {
            super(name);
        }

        @Override
        public void setInputValue(int value) {
            setSelected(value == 0);
        }
    }
}
