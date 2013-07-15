package com.nikonhacker.gui.component.memoryMapped;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.MemoryActivityListener;
import com.nikonhacker.emu.memory.listener.RangeAccessLoggerActivityListener;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;
import com.nikonhacker.gui.swing.PrintWriterArea;
import com.nikonhacker.gui.swing.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;


public class CustomMemoryRangeLoggerFrame extends DocumentFrame {
    private static final int ROWS    = 60;
    private static final int COLUMNS = 80;

    private DebuggableMemory memory;
    private CPUState         cpuState;
    private java.util.Map<JTextField, MemoryActivityListener> listeners = new HashMap<>();
    private final PrintWriterArea textArea;

    // By default, only log code access
    private final Set<DebuggableMemory.AccessSource> selectedAccessSource = EnumSet.of(DebuggableMemory.AccessSource.CODE);

    public CustomMemoryRangeLoggerFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, EmulatorUI ui, final DebuggableMemory memory, final CPUState cpuState) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.memory = memory;
        this.cpuState = cpuState;

        textArea = new PrintWriterArea(ROWS, COLUMNS);
        textArea.setAutoScroll(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

        JPanel selectionPanelContainer = new JPanel(new VerticalLayout());

        addRange(selectionPanelContainer);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(selectionPanelContainer, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton clearButton = new JButton("Clear log");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    textArea.clear();
                } catch (IOException e1) {
                    // ignore
                }
            }
        });
        contentPanel.add(clearButton, BorderLayout.SOUTH);

        getContentPane().add(contentPanel);
    }

    private void addRange(final JPanel selectionPanelContainer) {
        final JTextField minAddressField = new JTextField(8);
        final JTextField maxAddressField = new JTextField(8);
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int minAddress = Format.parseIntHexField(minAddressField);
                int maxAddress = Format.parseIntHexField(maxAddressField);
                MemoryActivityListener listener = listeners.get(minAddressField);
                if (listener != null) {
                    memory.removeActivityListener(listener);
                    textArea.getPrintWriter().println("Stopping previous listener");
                }
                listener = new RangeAccessLoggerActivityListener(textArea.getPrintWriter(), minAddress, maxAddress, cpuState, selectedAccessSource);
                memory.addActivityListener(listener);
                listeners.put(minAddressField, listener);
                textArea.getPrintWriter().println("Starting listener for " + Constants.CHIP_LABEL[chip] + " range 0x" + Format.asHex(minAddress, 8) + " - 0x" + Format.asHex(maxAddress, 8));
            }
        };

        final JPanel selectionPanel = new JPanel();
        selectionPanel.add(new JLabel("0x"));
        selectionPanel.add(minAddressField);
        minAddressField.addActionListener(actionListener);

        selectionPanel.add(new JLabel("- 0x"));
        selectionPanel.add(maxAddressField);
        maxAddressField.addActionListener(actionListener);

        for (final DebuggableMemory.AccessSource accessSource : DebuggableMemory.AccessSource.selectableAccessSource) {
            final JCheckBox checkBox = new JCheckBox(accessSource.name());
            checkBox.setSelected(selectedAccessSource.contains(accessSource));
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (checkBox.isSelected()) {
                        selectedAccessSource.add(accessSource);
                    }
                    else {
                        selectedAccessSource.remove(accessSource);
                    }
                }
            });
            selectionPanel.add(checkBox);
        }

        final JButton goButton = new JButton("Go");
        goButton.addActionListener(actionListener);
        selectionPanel.add(goButton);

        final JButton plusButton = new JButton("+");
        plusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addRange(selectionPanelContainer);
            }
        });
        selectionPanel.add(plusButton);
        final CustomMemoryRangeLoggerFrame frame = this;
        final JButton minusButton = new JButton("-");
        minusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listeners.size() > 1) {
                    MemoryActivityListener listener = listeners.get(minAddressField);
                    if (listener != null) {
                        memory.removeActivityListener(listener);
                        textArea.getPrintWriter().println("Stopping previous listener");
                    }
                    listeners.remove(minAddressField);
                    selectionPanelContainer.remove(selectionPanel);
                    frame.pack();
                }
            }
        });
        selectionPanel.add(minusButton);

        selectionPanelContainer.add(selectionPanel);
        this.pack();
    }

    public void dispose() {
        for (MemoryActivityListener listener : listeners.values()) {
            memory.removeActivityListener(listener);
        }
        super.dispose();
    }
}
