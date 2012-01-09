package com.nikonhacker.gui.component;

import com.nikonhacker.gui.EmulatorUI;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.util.prefs.Preferences;

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
            Preferences prefs = Preferences.userNodeForPackage(DocumentFrame.class);
            prefs.putInt(this.getClass().getSimpleName() + EmulatorUI.PREFKEY_LAST_X, getX());
            prefs.putInt(this.getClass().getSimpleName() + EmulatorUI.PREFKEY_LAST_Y, getY());
        }
    }

    public void internalFrameIconified(InternalFrameEvent e) {}

    public void internalFrameDeiconified(InternalFrameEvent e) {}

    public void internalFrameActivated(InternalFrameEvent e) {}

    public void internalFrameDeactivated(InternalFrameEvent e) {}

    public void display(boolean rememberLastPosition) {
        this.rememberLastPosition = rememberLastPosition;
        if (this.rememberLastPosition) {
            Preferences prefs = Preferences.userNodeForPackage(DocumentFrame.class);
            setLocation(prefs.getInt(this.getClass().getSimpleName() + EmulatorUI.PREFKEY_LAST_X, 0), prefs.getInt(this.getClass().getSimpleName() + EmulatorUI.PREFKEY_LAST_Y, 0));
        }
        pack();
        setVisible(true);
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e) { /* noop */ }
    }
}
