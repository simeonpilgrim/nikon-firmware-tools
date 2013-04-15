package com.nikonhacker.gui.component.serialInterface.eeprom;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St950x0;
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
public class EepromSerialPanel extends SerialDevicePanel implements HexEditorListener {

    private final RxTxSerialPanel rxTxSerialPanel;
    private final Prefs prefs;
    private St950x0 eeprom;
    private final HexEditor eepromHexEditor;

    public EepromSerialPanel(St950x0 eeprom, EmulatorUI ui) {
        super();
        this.prefs = ui.getPrefs();
        this.eeprom = eeprom;

        JTabbedPane tabbedPane = new JTabbedPane();
        rxTxSerialPanel = new RxTxSerialPanel(eeprom);
        tabbedPane.add("Rx/Tx interface", rxTxSerialPanel);

        JPanel editorPanel = new JPanel(new BorderLayout());
        JPanel selectionPanel = new JPanel();

        JButton loadButton = new JButton("Load...");
        loadButton.setToolTipText("Load memory from file");
        selectionPanel.add(loadButton);
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                load();
            }
        });

        JButton saveButton = new JButton("Save...");
        saveButton.setToolTipText("Save memory to file");
        selectionPanel.add(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });

        editorPanel.add(selectionPanel, BorderLayout.NORTH);

        eepromHexEditor = new HexEditor();
        eepromHexEditor.setRowHeaderOffset(0);
        eepromHexEditor.setRowHeaderMinDigits(4);
        eepromHexEditor.setCellEditable(true);
        eepromHexEditor.setAlternateRowBG(true);
        refreshContents();

        eepromHexEditor.addHexEditorListener(this);

        editorPanel.add(eepromHexEditor, BorderLayout.CENTER);

        tabbedPane.addTab("Contents", editorPanel);

        add(tabbedPane);
    }

    private void load() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Binary files (*.bin)", "bin"));
        fc.setCurrentDirectory(new File("."));

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fc.getSelectedFile();
                prefs.setLastEepromFileName(selectedFile.getAbsolutePath());
                eeprom.loadBinary(selectedFile);
                refreshContents();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading eeprom contents from file: " + e.getMessage(), "Load error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void save() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Binary files (*.bin)", "bin"));
        fc.setCurrentDirectory(new File("."));

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fc.getSelectedFile();
                if (!file.exists() || JOptionPane.showConfirmDialog(this, "Do you want to overwrite\n" + file.getAbsolutePath() + " ?", "File exists", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    eeprom.saveBinary(file);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving eeprom contents from file: " + e.getMessage(), "Load error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void refreshContents() {
        try {
            eepromHexEditor.open(new ByteArrayInputStream(eeprom.getMemory()));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading eeprom contents in Hex editor. See console for more information.");
        }
    }

    public void dispose() {
        rxTxSerialPanel.dispose();
    }

    @Override
    public void hexBytesChanged(HexEditorEvent e) {
        // todo
    }
}
