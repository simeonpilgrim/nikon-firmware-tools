package com.nikonhacker.gui.component.saveLoadMemory;


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

public class SaveLoadMemoryDialog extends JDialog {

    public SaveLoadMemoryDialog(Frame owner, final Memory memory) {
        this(owner,  memory, null, null);
    }

    public SaveLoadMemoryDialog(Frame owner, final Memory memory, Integer startAddress) {
        this(owner,  memory, startAddress, null);
    }

    public SaveLoadMemoryDialog(Frame owner, final Memory memory, Integer startAddress, Integer endAddress) {
        this(owner,  memory, startAddress, endAddress, true);
    }

    public SaveLoadMemoryDialog(Frame owner, final Memory memory, Integer startAddress, Integer endAddress, boolean saveMode) {
        super(owner, "Save/Load memory area to/from file", true);

        // Start / End / Length range bloc definition
        
        final JTextField filenameField = new JTextField();
        final JTextField startAddressField = new JTextField(10);
        final JRadioButton saveButton = new JRadioButton();
        final JRadioButton loadButton = new JRadioButton();
        final JRadioButton withEndButton = new JRadioButton();
        final JLabel endAddressLabel = new JLabel("End address");
        final JTextField endAddressField = new JTextField(10);
        final JRadioButton withLengthButton = new JRadioButton();
        final JLabel lengthLabel = new JLabel("Length");
        final JTextField lengthField = new JTextField(10);

        ButtonGroup endOrLengthGroup = new ButtonGroup();
        endOrLengthGroup.add(withEndButton);
        endOrLengthGroup.add(withLengthButton);

        ButtonGroup saveOrLoadGroup = new ButtonGroup();
        saveOrLoadGroup.add(saveButton);
        saveOrLoadGroup.add(loadButton);

        ActionListener buttonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                withEndButton.setEnabled(saveButton.isSelected());
                withLengthButton.setEnabled(saveButton.isSelected());
                endAddressLabel.setEnabled(withEndButton.isSelected() && saveButton.isSelected());
                endAddressField.setEnabled(withEndButton.isSelected() && saveButton.isSelected());
                lengthLabel.setEnabled(withLengthButton.isSelected() && saveButton.isSelected());
                lengthField.setEnabled(withLengthButton.isSelected() && saveButton.isSelected());
            }
        };
        withEndButton.addActionListener(buttonListener);
        withLengthButton.addActionListener(buttonListener);
        saveButton.addActionListener(buttonListener);
        loadButton.addActionListener(buttonListener);
        withEndButton.setHorizontalAlignment(SwingConstants.RIGHT);
        withLengthButton.setHorizontalAlignment(SwingConstants.RIGHT);
        saveButton.setHorizontalAlignment(SwingConstants.RIGHT);
        loadButton.setHorizontalAlignment(SwingConstants.RIGHT);

        saveButton.setSelected(saveMode);
        loadButton.setSelected(!saveMode);
        withEndButton.setSelected(true);
        lengthLabel.setEnabled(false);
        lengthField.setEnabled(false);

        JPanel rangePanel = new JPanel(new GridLayout(5,3));
        JLabel directionLabel = new JLabel("Direction");
        directionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rangePanel.add(directionLabel);
        rangePanel.add(saveButton);
        rangePanel.add(new JLabel("Save"));

        rangePanel.add(new JLabel());
        rangePanel.add(loadButton);
        rangePanel.add(new JLabel("Load"));

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
        mainPanel.add(new FileSelectionPanel("File", filenameField, false));


        // OK / Cancel  bottom bloc definition
        
        JPanel bottomPanel = new JPanel(new FlowLayout());

        JButton okButton = new JButton("OK");
        bottomPanel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveOrLoadMemoryArea(saveButton, withLengthButton, lengthField, endAddressField, startAddressField, memory, filenameField);
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
        
        // Set default values if a startAddress was given
        
        if (startAddress != null) {
            String startAddressString = "0x" + Format.asHex(startAddress, 8);
            startAddressField.setText(startAddressString);
            if (endAddress != null) {
                String endAddressString = "0x" + Format.asHex(endAddress, 8);
                endAddressField.setText(endAddressString);
                withEndButton.setSelected(true);
                if (saveMode) {
                    filenameField.setText(new File("Dump_" + startAddressString + "-" + endAddressString + ".bin").getAbsolutePath());
                }
            }
            else {
                if (saveMode) {
                    filenameField.setText(new File("Dump_" + startAddressString + ".bin").getAbsolutePath());
                }
            }
        }

        setContentPane(contentPanel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void saveOrLoadMemoryArea(JRadioButton saveButton, JRadioButton withLengthButton, JTextField lengthField, JTextField endAddressField, JTextField startAddressField, Memory memory, JTextField filenameField) {
        if (saveButton.isSelected()) {
            // Save
            try {
                File destinationFile = new File(filenameField.getText());
                if (!destinationFile.exists() || JOptionPane.showConfirmDialog(this, "Are you sure you want to overwrite " + destinationFile.getName(), "Confirm overwrite", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    int startAddress = Format.parseUnsignedField(startAddressField);
                    int length;
                    if (withLengthButton.isSelected()) {
                        length = Format.parseUnsignedField(lengthField);
                    }
                    else {
                        length = Format.parseUnsignedField(endAddressField) - startAddress + 1;
                    }    
                    memory.saveToFile(destinationFile, startAddress, length);
                    JOptionPane.showMessageDialog(this, "Dump complete", "Done", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (ParsingException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error parsing parameters", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error saving memory to file", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            // Load
            try {
                File sourceFile = new File(filenameField.getText());
                if (!sourceFile.exists()) {
                    JOptionPane.showMessageDialog(this, "File does not exist :\n" + sourceFile.getAbsolutePath(), "Load error", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    int startAddress = Format.parseUnsignedField(startAddressField);
                    memory.loadFile(sourceFile, startAddress);
                    JOptionPane.showMessageDialog(this, "Load complete", "Done", JOptionPane.INFORMATION_MESSAGE);
                }
                dispose();
            } catch (ParsingException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error parsing parameters", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error loading memory from file", JOptionPane.ERROR_MESSAGE);
            }
        }
        dispose();
    }

}
