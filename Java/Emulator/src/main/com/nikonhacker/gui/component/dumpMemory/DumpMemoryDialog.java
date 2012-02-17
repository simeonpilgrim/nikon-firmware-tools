package com.nikonhacker.gui.component.dumpMemory;


import com.nikonhacker.Format;
import com.nikonhacker.dfr.ParsingException;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.gui.component.FileSelectionPanel;
import com.nikonhacker.gui.component.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class DumpMemoryDialog extends JDialog {

    public DumpMemoryDialog(Frame owner, final Memory memory) {
        super(owner, "Dump memory area to file", true);

        // Start / End / Length range bloc definition
        
        final JTextField destinationField = new JTextField();
        final JTextField startAddressField = new JTextField(10);
        final JRadioButton withEndButton = new JRadioButton();
        final JLabel endAddressLabel = new JLabel("End address");
        final JTextField endAddressField = new JTextField(10);
        final JRadioButton withLengthButton = new JRadioButton();
        final JLabel lengthLabel = new JLabel("Length");
        final JTextField lengthField = new JTextField(10);

        ButtonGroup group = new ButtonGroup();
        group.add(withEndButton);
        group.add(withLengthButton);

        ActionListener buttonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                endAddressLabel.setEnabled(withEndButton.isSelected());
                endAddressField.setEnabled(withEndButton.isSelected());
                lengthLabel.setEnabled(withLengthButton.isSelected());
                lengthField.setEnabled(withLengthButton.isSelected());
            }
        };
        withEndButton.addActionListener(buttonListener);
        withLengthButton.addActionListener(buttonListener);
        withEndButton.setHorizontalAlignment(SwingConstants.RIGHT);
        withLengthButton.setHorizontalAlignment(SwingConstants.RIGHT);

        withEndButton.setSelected(true);
        lengthLabel.setEnabled(false);
        lengthField.setEnabled(false);

        JPanel rangePanel = new JPanel(new GridLayout(3,3));
        rangePanel.add(new JLabel());
        rangePanel.add(new JLabel("Start address"));
        rangePanel.add(startAddressField);
        rangePanel.add(withEndButton);
        rangePanel.add(endAddressLabel);
        rangePanel.add(endAddressField);
        rangePanel.add(withLengthButton);
        rangePanel.add(lengthLabel);
        rangePanel.add(lengthField);


        // Main panel

        JPanel mainPanel = new JPanel(new VerticalLayout());
        mainPanel.add(rangePanel);
        mainPanel.add(new FileSelectionPanel("Destination file", destinationField, false));


        // OK / Cancel  bottom bloc definition
        
        JPanel bottomPanel = new JPanel(new FlowLayout());

        JButton okButton = new JButton("OK");
        bottomPanel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveMemoryArea(withLengthButton, lengthField, endAddressField, startAddressField, memory, destinationField);
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(cancelButton);


        // Content panel

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void saveMemoryArea(JRadioButton withLengthButton, JTextField lengthField, JTextField endAddressField, JTextField startAddressField, Memory memory, JTextField destinationField) {
        try {
            int startAddress = Format.parseUnsignedField(startAddressField);
            int length;
            if (withLengthButton.isSelected()) {
                length = Format.parseUnsignedField(lengthField);
            }
            else {
                length = Format.parseUnsignedField(endAddressField) - startAddress + 1;
            }

            memory.saveToFile(new File(destinationField.getText()), startAddress, length);
            JOptionPane.showMessageDialog(this, "Dump complete", "Done", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ParsingException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error parsing parameters", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error dumping memory to file", JOptionPane.ERROR_MESSAGE);
        }
    }

}
