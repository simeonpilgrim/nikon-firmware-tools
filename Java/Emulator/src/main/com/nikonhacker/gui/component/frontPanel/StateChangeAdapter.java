package com.nikonhacker.gui.component.frontPanel;

/**
 * This is a base class with all methods implemented as no-op
 */
public abstract class StateChangeAdapter implements StateChangeListener {
    @Override
    public void onStateChange(int state) {}

    @Override
    public void onStateRotateUp() {}

    @Override
    public void onStateRotateDown() {}
}
