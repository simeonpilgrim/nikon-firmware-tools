package com.nikonhacker.emu.peripherials.serialInterface;

public abstract class AbstractSpiDevice implements SerialDevice {

    protected boolean selected = false;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
