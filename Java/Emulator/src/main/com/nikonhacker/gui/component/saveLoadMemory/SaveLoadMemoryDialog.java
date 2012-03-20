package com.nikonhacker.gui.component.saveLoadMemory;


import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.gui.component.FileSelectionPanel;
import net.miginfocom.swing.MigLayout;

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
        final JRadioButton saveButton = new JRadioButton("Save to disk");
        final JRadioButton loadButton = new JRadioButton("Load from disk");
        final JRadioButton withEndButton = new JRadioButton("End address");
        final JLabel endAddressLabel = new JLabel("0x");
        final JTextField endAddressField = new JTextField(10);
        final JRadioButton withLengthButton = new JRadioButton("Length");
        final JLabel lengthLabel = new JLabel("0x");
        final JTextField lengthField = new JTextField(10);
        final FileSelectionPanel fileSelectionPanel = new FileSelectionPanel(null, filenameField, false);

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
                fileSelectionPanel.setDialogTitle(saveButton.isSelected()?"Select file to dump memory to":"Select file to load memory from");
            }
        };
        withEndButton.addActionListener(buttonListener);
        withLengthButton.addActionListener(buttonListener);
        saveButton.addActionListener(buttonListener);
        loadButton.addActionListener(buttonListener);

        saveButton.setSelected(saveMode);
        loadButton.setSelected(!saveMode);
        withEndButton.setSelected(true);
        lengthLabel.setEnabled(false);
        lengthField.setEnabled(false);

        // OK / Cancel  buttons
        JButton okButton = new JButton("OK");
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

        // Layout

        JPanel optionsPanel = new JPanel(new MigLayout("", "[25%!][grow,left][grow,right][grow,left][25%!]"));
        addSeparator(optionsPanel, "Direction");
        optionsPanel.add(new Label(""));
        optionsPanel.add(saveButton, "span 2");
        optionsPanel.add(loadButton, "wrap");

        addSeparator(optionsPanel, "Range");
        optionsPanel.add(new JLabel(""), "span 1 3");
        optionsPanel.add(new JLabel("Start address"), "alignx right");
        optionsPanel.add(new JLabel("0x"));
        optionsPanel.add(startAddressField);
        optionsPanel.add(new JLabel(""), "span 1 3, wrap");

        optionsPanel.add(withEndButton, "skip, alignx left");
        optionsPanel.add(endAddressLabel);
        optionsPanel.add(endAddressField, "wrap");

        optionsPanel.add(withLengthButton, "skip, alignx left");
        optionsPanel.add(lengthLabel);
        optionsPanel.add(lengthField, "wrap");

        addSeparator(optionsPanel, "File");

        optionsPanel.add(fileSelectionPanel, "span 5, wrap");

//        JPanel bottomPanel = new JPanel(new FlowLayout());
//        bottomPanel.add(okButton);
//        bottomPanel.add(cancelButton);

        JPanel bottomPanel = new JPanel(new MigLayout("nogrid, fillx, aligny 100%, gapy unrel"));
        bottomPanel.add(okButton, "sgx 1,tag OK");
        bottomPanel.add(cancelButton, "sgx 1,tag Cancel");

        optionsPanel.add(bottomPanel, "alignx center, span 5");


        // Content panel

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(optionsPanel, BorderLayout.CENTER);
//        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Set default values if a startAddress was given
        
        if (startAddress != null) {
            String startAddressString = Format.asHex(startAddress, 8);
            startAddressField.setText(startAddressString);
            if (endAddress != null) {
                String endAddressString = Format.asHex(endAddress, 8);
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

    private void addSeparator(JPanel panel, String text)
    {
        panel.add(new JLabel(text, SwingConstants.LEADING), "gapbottom 1, span, split 2, aligny center");
        panel.add(new JSeparator(), "gapleft rel, growx");
    }

    private void saveOrLoadMemoryArea(JRadioButton saveButton, JRadioButton withLengthButton, JTextField lengthField, JTextField endAddressField, JTextField startAddressField, Memory memory, JTextField filenameField) {
        if (saveButton.isSelected()) {
            // Save
            try {
                if ((withLengthButton.isSelected() && lengthField.getText().length() == 0) || (!withLengthButton.isSelected() && endAddressField.getText().length() == 0)) {
                    JOptionPane.showMessageDialog(this, "A length or end address must be given for saving memory to file", "Error parsing parameters", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    File destinationFile = new File(filenameField.getText());
                    if (!destinationFile.exists() || JOptionPane.showConfirmDialog(this, "Are you sure you want to overwrite " + destinationFile.getName(), "Confirm overwrite", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        int startAddress = Format.parseIntHexField(startAddressField);
                        int length;
                        if (withLengthButton.isSelected()) {
                            length = Format.parseIntHexField(lengthField);
                        }
                        else {
                            length = Format.parseIntHexField(endAddressField) - startAddress + 1;
                        }
                        memory.saveToFile(destinationFile, startAddress, length);
                        JOptionPane.showMessageDialog(this, "Dump complete", "Done", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    }
                }
            } catch (NumberFormatException e) {
                // do nothing, field is now red
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
                    int startAddress = Format.parseIntHexField(startAddressField);
                    memory.loadFile(sourceFile, startAddress);
                    JOptionPane.showMessageDialog(this, "Load complete", "Done", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } catch (NumberFormatException e) {
                // do nothing, field is now red
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error loading memory from file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
