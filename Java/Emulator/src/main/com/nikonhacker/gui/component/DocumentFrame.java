package com.nikonhacker.gui.component;

import com.nikonhacker.gui.EmulatorUI;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

public class DocumentFrame extends JInternalFrame implements InternalFrameListener {
    protected EmulatorUI ui;
    private boolean rememberLastPosition;

    public DocumentFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable);
        this.ui = ui;
        addInternalFrameListener(this);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        // Called only when close is initiated by clicking on the close widget
        ui.frameClosing(this);
    }

    public void internalFrameOpened(InternalFrameEvent e) {}

    public void internalFrameClosed(InternalFrameEvent e) {
        // Called no matter how the close is initiated
        if (this.rememberLastPosition) {
            ui.getPrefs().setWindowPosition(this.getClass().getSimpleName(), getX(), getY());
        }
    }

    public void internalFrameIconified(InternalFrameEvent e) {}

    public void internalFrameDeiconified(InternalFrameEvent e) {}

    public void internalFrameActivated(InternalFrameEvent e) {}

    public void internalFrameDeactivated(InternalFrameEvent e) {}

    public void display(boolean rememberLastPosition) {
        this.rememberLastPosition = rememberLastPosition;
        if (this.rememberLastPosition) {
            String windowName = this.getClass().getSimpleName();
            setLocation(ui.getPrefs().getWindowPositionX(windowName), ui.getPrefs().getWindowPositionY(windowName));
        }
        pack();
        setVisible(true);
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e) { /* noop */ }
    }
}
