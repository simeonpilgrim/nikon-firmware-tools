package com.nikonhacker.gui.component.disassembly;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.AddressRange;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;
import com.nikonhacker.gui.swing.PrintWriterArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class DisassemblyFrame extends DocumentFrame {

    private static final int ROWS    = 50;
    private static final int COLUMNS = 100;

    private Emulator emulator;
    private List<AddressRange> addressRanges;
    private       boolean editable;
    private final JButton rangeButton;
    private final JButton startStopButton;

    private DisassemblyLogger logger = new DisassemblyLogger();
    private final JCheckBox         timestampCheckbox;
    private final JCheckBox         indentCheckbox;
    private final JCheckBox         instructionCheckbox;
    private final JCheckBox         interruptMarksCheckbox;
    private final JComboBox<Object> destinationComboBox;


    public DisassemblyFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, EmulatorUI ui, Emulator emulator, final List<AddressRange> addressRanges) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.emulator = emulator;
        this.addressRanges = addressRanges;

        JPanel selectionPanelContainer = new JPanel();

        final PrintWriterArea disassemblyLog = new PrintWriterArea(ROWS, COLUMNS);
        disassemblyLog.setAutoScroll(true);
        disassemblyLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

        selectionPanelContainer.add(new JLabel("Log..."));

        timestampCheckbox = new JCheckBox("timestamp");
        timestampCheckbox.setSelected(logger.isIncludeTimestamp());
        timestampCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.setIncludeTimestamp(timestampCheckbox.isSelected());
            }
        });
        selectionPanelContainer.add(timestampCheckbox);

        indentCheckbox = new JCheckBox("indent");
        indentCheckbox.setSelected(logger.isIncludeIndent());
        indentCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.setIncludeIndent(indentCheckbox.isSelected());
            }
        });
        selectionPanelContainer.add(indentCheckbox);

        instructionCheckbox = new JCheckBox("instruction");
        instructionCheckbox.setSelected(logger.isIncludeInstruction());
        instructionCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.setIncludeInstruction(instructionCheckbox.isSelected());
            }
        });
        selectionPanelContainer.add(instructionCheckbox);

        interruptMarksCheckbox = new JCheckBox("interrupt marks");
        interruptMarksCheckbox.setSelected(logger.isIncludeInterruptMarks());
        interruptMarksCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.setIncludeInterruptMarks(interruptMarksCheckbox.isSelected());
            }
        });
        selectionPanelContainer.add(interruptMarksCheckbox);

        destinationComboBox = new JComboBox<>();
        destinationComboBox.addItem("to here");
        destinationComboBox.addItem("to file");
        destinationComboBox.addItem("to both");
        destinationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Writer writer : logger.getWriters()) {
                    try {
                        if (writer instanceof FileWriter) {
                            String msg = "Closing file";
                            System.out.println(msg);
                            disassemblyLog.getPrintWriter().println(msg);
                        }
                        writer.close();
                    } catch (IOException e1) {
                        System.err.println("Error closing writer: " + writer);
                    }
                }
                logger.getWriters().clear();
                switch (destinationComboBox.getSelectedIndex()) {
                    case 0:
                        // To here
                        logger.getWriters().add(disassemblyLog.getPrintWriter());
                        break;
                    case 2:
                        // To here and file
                        logger.getWriters().add(disassemblyLog.getPrintWriter());
                        // go on with file...
                    case 1:
                        // To File
                        File file = new File(Constants.CHIP_LABEL[chip] + "_" + (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date()) + ".log");
                        String msg = "Opening file " + file.getAbsolutePath();
                        System.out.println(msg);
                        disassemblyLog.getPrintWriter().println(msg);
                        try {
                            logger.getWriters().add(new FileWriter(file, true));
                        } catch (IOException e1) {
                            JOptionPane.showMessageDialog(DisassemblyFrame.this, "Cannot write to " + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                }
            }
        });
        selectionPanelContainer.add(destinationComboBox);

        rangeButton = new JButton("Limit PC");
        selectionPanelContainer.add(rangeButton);
        rangeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditRangesDialog editRangesDialog = new EditRangesDialog(getEmulatorUi(), addressRanges);
                editRangesDialog.setVisible(true);
            }
        });

        startStopButton = new JButton("Start");
        selectionPanelContainer.add(startStopButton);
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleLogging();
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(selectionPanelContainer, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(disassemblyLog), BorderLayout.CENTER);

        logger.getWriters().add(disassemblyLog.getPrintWriter());
        emulator.setDisassemblyLogger(logger);

        setContentPane(contentPanel);
        pack();
    }

    public DisassemblyLogger getLogger() {
        return logger;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        updateControls();
    }

    private void updateControls() {
        timestampCheckbox.setEnabled(editable && !logger.isLogging());
        indentCheckbox.setEnabled(editable && !logger.isLogging());
        instructionCheckbox.setEnabled(editable && !logger.isLogging());
        interruptMarksCheckbox.setEnabled(editable && !logger.isLogging());
        destinationComboBox.setEnabled(editable && !logger.isLogging());
        rangeButton.setEnabled(editable && !logger.isLogging());
        startStopButton.setEnabled(editable);
    }

    private void toggleLogging() {
        if (logger.isLogging()) {
            startStopButton.setText("Start");
            logger.println("---- Realtime logging stopped.");
            logger.clearIndent();
            logger.setLogging(false);
        }
        else {
            startStopButton.setText("Stop");
            logger.setRanges(addressRanges.isEmpty()?null:addressRanges);
            logger.setLogging(true);
            logger.println("");
            logger.println("---- Starting realtime logging"  + (addressRanges.isEmpty()?"...":(", limiting PC to " + addressRanges.size() + " range(s)...")));
        }
        updateControls();
    }

    @Override
    public void dispose() {
        if (logger.isLogging()) {
            toggleLogging();
            for (Writer writer : logger.getWriters()) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error closing writer " + writer);
                }
            }
        }
        emulator.setDisassemblyLogger(null);
        super.dispose();
    }

}
