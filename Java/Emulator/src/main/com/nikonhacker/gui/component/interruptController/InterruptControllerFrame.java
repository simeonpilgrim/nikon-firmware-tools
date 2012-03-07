package com.nikonhacker.gui.component.interruptController;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.InterruptRequest;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


public class InterruptControllerFrame extends DocumentFrame {
    private Emulator emulator;

    public InterruptControllerFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final Emulator emulator, final EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.emulator = emulator;

        JPanel interruptControllerPanel = new JPanel(new BorderLayout());
        
        JPanel topToolbar = new JPanel(new FlowLayout());
        topToolbar.add(new JLabel("ICR : "));
        Vector<String> labels = new Vector<String>();
        for (int i = 0; i < 32; i++) {
            labels.add(Format.asBinary(i, 5));
        }
        final JComboBox icrComboBox = new JComboBox(labels);
        topToolbar.add(icrComboBox);

        final JCheckBox nmiCheckBox = new JCheckBox("NMI");
        topToolbar.add(nmiCheckBox);

                
        interruptControllerPanel.add(topToolbar, BorderLayout.NORTH);
        
        Insets insets = new Insets(1,1,1,1);

        ActionListener interruptButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JInterruptButton button = (JInterruptButton) e.getSource();
                int interruptNumber = button.getInterruptNumber();
                if (emulator.addInterruptRequest(new InterruptRequest(interruptNumber, nmiCheckBox.isSelected(), icrComboBox.getSelectedIndex()))) {
                    ui.setStatusText("Interrupt 0x" + interruptNumber + " was requested.");
                }
                else {
                    ui.setStatusText("Interrupt 0x" + interruptNumber + " was rejected (already requested).");
                }
            }
        };

        JPanel buttonGrid = new JPanel(new GridLayout(16, 16));
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                final int value = i * 16 + j;
                JInterruptButton button = new JInterruptButton(Format.asHex(value, 2), value);
                button.setMargin(insets);
                button.addActionListener(interruptButtonListener);
                buttonGrid.add(button);
            }
        }

        interruptControllerPanel.add(buttonGrid, BorderLayout.CENTER);

        getContentPane().add(interruptControllerPanel);
        pack();
    }

    public void dispose() {
        emulator.setInstructionPrintWriter(null);
        super.dispose();
    }

}
