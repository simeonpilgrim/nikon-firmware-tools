package com.nikonhacker.gui.component.memoryHexEditor;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.saveLoadMemory.SaveLoadMemoryDialog;
import org.fife.ui.hex.event.HexEditorEvent;
import org.fife.ui.hex.event.HexEditorListener;
import org.fife.ui.hex.swing.HexEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class MemoryHexEditorFrame extends DocumentFrame implements ActionListener, HexEditorListener {
    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private DebuggableMemory memory;
    private CPUState cpuState;
    private String baseTitle;

    private Timer refreshTimer;
    private JTextField addressField;
    private HexEditor hexEditor;
    private JButton leftButton;
    private JButton rightButton;
    private JButton fpButton;
    private JButton spButton;
    private byte[] currentPage;
    private int baseAddress;
    private JComboBox registerCombo;
    private JButton saveLoadButton;

    public MemoryHexEditorFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, DebuggableMemory memory, CPUState cpuState, int baseAddress, boolean editable) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.baseTitle = title;
        this.memory = memory;
        this.cpuState = cpuState;

        getContentPane().add(createEditor(baseAddress, editable));

        // Start update timer
        refreshTimer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        refreshTimer.start();
    }

    private void refreshData() {
        if (ui.isEmulatorPlaying(chip)) {
            if (currentPage != null) {
                try {
                    hexEditor.open(new ByteArrayInputStream(currentPage));
                    hexEditor.setColorMap(createColorMap());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private JPanel createEditor(int baseAddress, boolean editable) {
        JPanel editorPanel = new JPanel(new BorderLayout());

        JPanel selectionPanel = new JPanel();

        fpButton = new JButton("Go to FP");
        selectionPanel.add(fpButton);
        fpButton.addActionListener(this);

//        selectionPanel.add(Box.createHorizontalGlue());

        spButton = new JButton("Go to SP");
        selectionPanel.add(spButton);
        spButton.addActionListener(this);

//        selectionPanel.add(Box.createHorizontalGlue());

        selectionPanel.add(new JLabel("Go to reg"));
        Vector<String> labels = new Vector<String>();
        labels.add("--");
        if (chip == Constants.CHIP_FR) {
            labels.addAll(Arrays.asList(FrCPUState.registerLabels));
        }
        else {
            labels.addAll(Arrays.asList(TxCPUState.registerLabels));
        }
        registerCombo = new JComboBox(labels);
        registerCombo.setMaximumRowCount(17);
        registerCombo.addActionListener(this);
        selectionPanel.add(registerCombo);

        
        Box.Filler largeFiller = new Box.Filler(new Dimension(0, 0), new Dimension(60, 0), new Dimension(60, 0));
        selectionPanel.add(largeFiller);

        
        leftButton = new JButton("<<");
        leftButton.setToolTipText("Previous page (-0x10000)");
        selectionPanel.add(leftButton);
        leftButton.addActionListener(this);
        
        selectionPanel.add(new JLabel("Go to  0x"));
        addressField = new JTextField(Format.asHex(baseAddress, 8), 8);
        selectionPanel.add(addressField);
        addressField.addActionListener(this);
        
        JButton goButton = new JButton("Go");
        selectionPanel.add(goButton);
        goButton.addActionListener(this);

        rightButton = new JButton(">>");
        rightButton.setToolTipText("Next page (+0x10000)");
        selectionPanel.add(rightButton);
        rightButton.addActionListener(this);

        
        selectionPanel.add(largeFiller);

        
        saveLoadButton = new JButton("Save/Load");
        saveLoadButton.setToolTipText("Save/Load selected area to/from file");
        selectionPanel.add(saveLoadButton);
        saveLoadButton.addActionListener(this);


        editorPanel.add(selectionPanel, BorderLayout.NORTH);

        hexEditor = new HexEditor();
        hexEditor.setRowHeaderOffset(baseAddress);
        hexEditor.setRowHeaderMinDigits(8);
        hexEditor.setCellEditable(editable);
        hexEditor.setAlternateRowBG(true);

        hexEditor.addHexEditorListener(this);

        loadPage(baseAddress);
        
        editorPanel.add(hexEditor, BorderLayout.CENTER);
        
        return editorPanel;
    }

    private void loadPage(int baseAddress) {
        this.baseAddress = baseAddress;
        addressField.setBackground(Color.WHITE);

        setTitle(baseTitle + " from 0x" + Format.asHex(baseAddress, 8) + " to 0x" + Format.asHex(baseAddress + memory.getPageSize() - 1, 8));

        try {
            currentPage = memory.getPageForAddress(baseAddress);
            if (currentPage == null) {
                memory.map(baseAddress, memory.getPageSize(), true, true, true);
                currentPage = memory.getPageForAddress(baseAddress);
            }
            hexEditor.open(new ByteArrayInputStream(currentPage));
            hexEditor.setColorMap(createColorMap());
            hexEditor.setRowHeaderOffset(baseAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Color[] createColorMap() {
        Color[] colorMap = null; // default if memory is not tracked
        TrackingMemoryActivityListener activityListener = ui.getTrackingMemoryActivityListener(chip);
        if (activityListener != null) {
            int[] cellActivityMap = activityListener.getCellActivityMap(baseAddress >>> 16);
            colorMap = new Color[0x10000];
            if (cellActivityMap == null) {
                // Memory is tracked, but this is page has never been accessed
                Arrays.fill(colorMap, Color.LIGHT_GRAY);
            }
            else {
                // Memory is tracked, set cell color according to access
                for (int i = 0; i < cellActivityMap.length; i++) {
                    int activity = cellActivityMap[i];
                    if (activity == 0 ) {
                        colorMap[i] = Color.LIGHT_GRAY;
                    }
                    else {
                        colorMap[i] = new Color((activity & 0xFF0000) == 0?0:0xFF, (activity & 0xFF00) == 0?0:0x7F, (activity & 0xFF) == 0?0:0xFF);
                    }
                }
            }
        }
        return colorMap;
    }

    public void dispose() {
        refreshTimer.stop();
        refreshTimer = null;
        super.dispose();
    }

    public void setEditable(boolean editable) {
        hexEditor.setCellEditable(editable);
    }

    public void actionPerformed(ActionEvent e) {
        int address;
        int selectionLength = 1;
        // Handle "FP" button
        if (fpButton.equals(e.getSource())) {
            if (chip == Constants.CHIP_FR) {
                addressField.setText(Format.asHex(cpuState.getReg(FrCPUState.FP), 8));
            }
            else {
                addressField.setText(Format.asHex(cpuState.getReg(TxCPUState.FP), 8));
            }
            registerCombo.setSelectedIndex(0);
            selectionLength = 4;
        }
        // Handle "SP" button
        else if (spButton.equals(e.getSource())) {
            if (chip == Constants.CHIP_FR) {
                addressField.setText(Format.asHex(cpuState.getReg(FrCPUState.SP), 8));
            }
            else {
                addressField.setText(Format.asHex(cpuState.getReg(TxCPUState.SP), 8));
            }
            registerCombo.setSelectedIndex(0);
            selectionLength = 4;
        }
        // Handle register combo
        else if (registerCombo.equals(e.getSource())) {
            int selectedIndex = registerCombo.getSelectedIndex();
            if (selectedIndex == 0) {
                return;
            }
            addressField.setText(Format.asHex(cpuState.getReg(selectedIndex - 1), 8));
            selectionLength = 4;
        }
        // Handle "previous" button
        else if (leftButton.equals(e.getSource())) {
            long longAddress = Format.parseIntHexField(addressField) & 0xFFFFFFFFL;
            longAddress -= memory.getPageSize();
            if (longAddress >= 0) {
                addressField.setText(Format.asHex((int) longAddress, 8));
            }
            registerCombo.setSelectedIndex(0);
        }
        // Handle "next" button
        else if (rightButton.equals(e.getSource())) {
            address = Format.parseIntHexField(addressField);
            address += memory.getPageSize();
            if (address < 0x100000000L) {
                addressField.setText(Format.asHex(address, 8));
            }
            registerCombo.setSelectedIndex(0);
        }
        // Handle "save/load" button
        else if (saveLoadButton.equals(e.getSource())) {
            int start = hexEditor.getSmallestSelectionIndex();
            int end =  hexEditor.getLargestSelectionIndex();
            new SaveLoadMemoryDialog(getEmulatorUi(), memory, baseAddress + start, (start==end)?null:(baseAddress + end)).setVisible(true);
            return;
        }
        
        // other cases mean it was just the "GO" button or Return key in the text field
        jumpToAddressField(selectionLength);
    }

    private void jumpToAddressField(int selectionLength) {
        // read address in field and load corresponding page
        int address = Format.parseIntHexField(addressField);
        jumpToAddress(address, selectionLength);
    }

    public void jumpToAddress(int address, int selectionLength) {
        int baseAddress = address & 0xFFFF0000;
        loadPage(baseAddress);
        int offset = address & 0x0000FFFF;
        hexEditor.setSelectedRange(offset, offset + selectionLength - 1);
    }

    public void hexBytesChanged(HexEditorEvent e) {
        if (e.isModification()) {
            memory.store8((int) ((baseAddress & 0xFFFFFFFFL) + (long)e.getOffset()), hexEditor.getByte(e.getOffset()));
        }
    }
}
