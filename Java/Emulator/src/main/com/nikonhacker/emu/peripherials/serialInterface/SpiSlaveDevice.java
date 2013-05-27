package com.nikonhacker.emu.peripherials.serialInterface;

public abstract class SpiSlaveDevice implements SerialDevice {

    protected boolean selected = false;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
