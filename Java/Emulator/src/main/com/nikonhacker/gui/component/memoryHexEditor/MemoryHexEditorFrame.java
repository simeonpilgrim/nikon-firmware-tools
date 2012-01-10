package com.nikonhacker.gui.component.memoryHexEditor;

import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
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

public class MemoryHexEditorFrame extends DocumentFrame implements ActionListener, HexEditorListener {
    DebuggableMemory memory;
    String baseTitle;
    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private Timer _timer;
    private JTextField addressField;
    private HexEditor hexEditor;
    private JButton leftButton;
    private JButton rightButton;
    private byte[] currentPage;
    private int baseAddress;

    public MemoryHexEditorFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, DebuggableMemory memory, int baseAddress, boolean editable, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.baseTitle = title;
        this.memory = memory;


        getContentPane().add(createEditor(baseAddress, editable));

        // Start update timer
        _timer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        _timer.start();
    }

    private void refreshData() {
        if (currentPage != null) {
            try {
                hexEditor.open(new ByteArrayInputStream(currentPage));
                hexEditor.setColorMap(createColorMap());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private JPanel createEditor(int baseAddress, boolean editable) {
        JPanel editorPanel = new JPanel(new BorderLayout());
        
        JPanel selectionPanel = new JPanel();

        leftButton = new JButton("<<");
        selectionPanel.add(leftButton);
        leftButton.addActionListener(this);
        
        selectionPanel.add(Box.createHorizontalGlue());

        selectionPanel.add(new JLabel("Go to  0x"));

        addressField = new JTextField(Format.asHex(baseAddress, 8), 8);
        selectionPanel.add(addressField);
        addressField.addActionListener(this);
        
        JButton goButton = new JButton("Go");
        selectionPanel.add(goButton);
        goButton.addActionListener(this);

        selectionPanel.add(Box.createHorizontalGlue());

        rightButton = new JButton(">>");
        selectionPanel.add(rightButton);
        rightButton.addActionListener(this);


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
        TrackingMemoryActivityListener activityListener = ui.getTrackingMemoryActivityListener();
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
        _timer.stop();
        _timer = null;
        super.dispose();
    }

    public void setEditable(boolean editable) {
        hexEditor.setCellEditable(editable);
    }

    public void actionPerformed(ActionEvent e) {
        int address;
        // Handle left button
        if (leftButton.equals(e.getSource())) {
            long longAddress = Format.parseHexField(addressField) & 0xFFFFFFFFL;
            longAddress -= memory.getPageSize();
            if (longAddress >= 0) {
                addressField.setText(Format.asHex((int) longAddress, 8));
            }
        }
        // Handle right button
        else if (rightButton.equals(e.getSource())) {
            address = Format.parseHexField(addressField);
            address += memory.getPageSize();
            if (address < 0xFFFFFFFFL) {
                addressField.setText(Format.asHex(address, 8));
            }
        }

        // In any case, read address and load corresponding page
        address = Format.parseHexField(addressField);
        int baseAddress = address & 0xFFFF0000;
        loadPage(baseAddress);
        int offset = address & 0x0000FFFF;
        hexEditor.setSelectedRange(offset, offset);
    }

    public void hexBytesChanged(HexEditorEvent e) {
        if (e.isModification()) {
            memory.store8((int) ((baseAddress & 0xFFFFFFFFL) + (long)e.getOffset()), hexEditor.getByte(e.getOffset()));
        }
    }
}
