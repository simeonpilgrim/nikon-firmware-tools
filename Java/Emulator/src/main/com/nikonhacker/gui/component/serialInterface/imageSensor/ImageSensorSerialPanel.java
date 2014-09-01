package com.nikonhacker.gui.component.serialInterface.imageSensor;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.serialInterface.imageSensor.Imx071;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.serialInterface.RxTxSerialPanel;
import com.nikonhacker.gui.component.serialInterface.SerialDevicePanel;
import org.fife.ui.hex.event.HexEditorEvent;
import org.fife.ui.hex.event.HexEditorListener;
import org.fife.ui.hex.swing.HexEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * This file is part of NikonEmulator, a NikonHacker.com project.
 */
public class ImageSensorSerialPanel extends SerialDevicePanel implements HexEditorListener {

    private final Prefs prefs;
    private final Imx071 imageSensor;
    private final HexEditor eepromHexEditor;

    private Timer refreshTimer;

    public ImageSensorSerialPanel(final Imx071 imageSensor, EmulatorUI ui) {
        super();
        this.prefs = ui.getPrefs();
        this.imageSensor = imageSensor;

        // Hex editor
        JPanel panel = new JPanel();

        eepromHexEditor = new HexEditor();
        eepromHexEditor.setRowHeaderOffset(0);
        eepromHexEditor.setRowHeaderMinDigits(2);
        eepromHexEditor.setCellEditable(false);
        eepromHexEditor.setAlternateRowBG(true);
        eepromHexEditor.addHexEditorListener(this);
        panel.add(eepromHexEditor, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        refreshData();

        // Start update timer
        refreshTimer = new Timer(ui.getPrefs().getRefreshIntervalMs(), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (eepromHexEditor.isShowing()) {
                    refreshData();
                }
            }
        });
        refreshTimer.start();
    }

    private final void refreshData() {
        try {
            eepromHexEditor.open(new ByteArrayInputStream(imageSensor.getMemory()));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading eeprom contents in Hex editor. See console for more information.");
        }
    }

    public void dispose() {
        refreshTimer.stop();
        refreshTimer = null;
    }

    @Override
    public void hexBytesChanged(HexEditorEvent event) {
        if (event.isModification()) {
            try {
                imageSensor.getMemory()[event.getOffset()] = eepromHexEditor.getByte(event.getOffset());
            }
            catch (ArrayIndexOutOfBoundsException exception) {
                JOptionPane.showMessageDialog(this, "Error writing to memory: " + exception.getMessage(), "Write error", JOptionPane.ERROR_MESSAGE);
                // Reload to show unedited values
                refreshData();
            }
        }
    }
}
