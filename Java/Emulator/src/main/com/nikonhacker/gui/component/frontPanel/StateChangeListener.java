package com.nikonhacker.gui.component.frontPanel;

public interface StateChangeListener {
    public void onStateChange(int state);
    public void onStateRotateUp();
    public void onStateRotateDown();
}
