package com.nikonhacker.gui.component.memoryMapped;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.MemoryActivityListener;
import com.nikonhacker.emu.memory.listener.RangeAccessLoggerActivityListener;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.PrintWriterArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class CustomMemoryRangeLoggerFrame extends DocumentFrame implements ActionListener {
    private static final int ROWS = 60;
    private static final int COLUMNS = 80;

    DebuggableMemory memory;
    private CPUState cpuState;
    private MemoryActivityListener listener;
    private final JTextField minAddressField, maxAddressField;
    private final PrintWriterArea textArea;

    public CustomMemoryRangeLoggerFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, DebuggableMemory memory, CPUState cpuState) {
        super(title, resizable, closable, maximizable, iconifiable, chip, ui);
        this.memory = memory;
        this.cpuState = cpuState;

        textArea = new PrintWriterArea(ROWS, COLUMNS);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

        JPanel selectionPanel = new JPanel();
        selectionPanel.add(new JLabel("Min address: 0x"));
        minAddressField = new JTextField(8);
        selectionPanel.add(minAddressField);
        minAddressField.addActionListener(this);

        selectionPanel.add(new JLabel("Max address: 0x"));
        maxAddressField = new JTextField(8);
        selectionPanel.add(maxAddressField);
        maxAddressField.addActionListener(this);

        final JButton goButton = new JButton("Go");
        goButton.addActionListener(this);
        selectionPanel.add(goButton);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(selectionPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        getContentPane().add(contentPanel);
    }

    public void actionPerformed(ActionEvent e) {
        int minAddress = Format.parseIntHexField(minAddressField);
        int maxAddress = Format.parseIntHexField(maxAddressField);
        if (listener != null) {
            memory.removeActivityListener(listener);
            textArea.getPrintWriter().println("Stopping previous listener");
        }
        listener = new RangeAccessLoggerActivityListener(textArea.getPrintWriter(), minAddress, maxAddress, cpuState);
        memory.addActivityListener(listener);
        textArea.getPrintWriter().println("Starting listener for range 0x" + Format.asHex(minAddress, 8) + " - 0x" + Format.asHex(maxAddress, 8));
    }

    public void dispose() {
        if (listener != null) {
            memory.removeActivityListener(listener);
        }
        super.dispose();
    }
}
