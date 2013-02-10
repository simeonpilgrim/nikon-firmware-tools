package com.nikonhacker.gui.component;

import com.nikonhacker.Constants;
import com.nikonhacker.gui.EmulatorUI;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.net.URL;

public class DocumentFrame extends JInternalFrame implements InternalFrameListener {
    protected int chip;
    protected EmulatorUI ui;

    private boolean rememberLastPosition;

    public DocumentFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui) {
        super(Constants.CHIP_LABEL[chip] + " " + title, resizable, closable, maximizable, iconifiable);
        this.chip = chip;
        this.ui = ui;
        addInternalFrameListener(this);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        if (imageName != null) {
            String imgLocation = "images/" + imageName + ".png";
            URL imageURL = EmulatorUI.class.getResource(imgLocation);
            ImageIcon resizedIcon = new ImageIcon(new ImageIcon(imageURL).getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
            setFrameIcon(resizedIcon);
        }
    }

    public EmulatorUI getEmulatorUi() {
        return ui;
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        // Called only when close is initiated by clicking on the close widget
        ui.frameClosing(this);
    }

    public void internalFrameOpened(InternalFrameEvent e) {}

    public void internalFrameClosed(InternalFrameEvent e) {
        // Called no matter how the close is initiated
        if (this.rememberLastPosition) {
            String windowName = this.getClass().getSimpleName();
            ui.getPrefs().setWindowPosition(windowName, chip, getX(), getY());
            ui.getPrefs().setWindowSize(windowName, chip, getWidth(), getHeight());
        }
    }

    public void internalFrameIconified(InternalFrameEvent e) {}

    public void internalFrameDeiconified(InternalFrameEvent e) {}

    public void internalFrameActivated(InternalFrameEvent e) {}

    public void internalFrameDeactivated(InternalFrameEvent e) {}

    public void display(boolean rememberLastPosition) {
        this.rememberLastPosition = rememberLastPosition;
        pack();
        if (this.rememberLastPosition) {
            String windowName = this.getClass().getSimpleName();
            // Restore position (always)
            // TODO limit to screen visibility
            setLocation(ui.getPrefs().getWindowPositionX(windowName, chip), ui.getPrefs().getWindowPositionY(windowName, chip));
            // Restore previous size (if resizable)
            if (resizable) {
                int windowSizeX = ui.getPrefs().getWindowSizeX(windowName, chip);
                if (windowSizeX > 0) {
                    setSize(windowSizeX, ui.getPrefs().getWindowSizeY(windowName, chip));
                }
            }
        }
        setVisible(true);
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e) { /* noop */ }
    }
}
