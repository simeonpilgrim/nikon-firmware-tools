package com.nikonhacker.gui.component.analyse;

import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.fr.Dfr;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.PrintWriterArea;
import com.nikonhacker.gui.component.SearchableTextAreaPanel;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class AnalyseProgressDialog extends JDialog {
    private PrintWriterArea printWriterArea;
    JButton closeButton;
    final JDialog frame = this;
    private EmulatorUI emulatorUI;
    private DebuggableMemory memory;

    public AnalyseProgressDialog(EmulatorUI emulatorUI, DebuggableMemory memory) {
        super(emulatorUI, "Disassembly progress", true);
        this.emulatorUI = emulatorUI;
        this.memory = memory;

        JPanel panel = new JPanel(new BorderLayout());

        printWriterArea = new PrintWriterArea(25, 70);

        DefaultCaret caret = (DefaultCaret)printWriterArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        panel.add(new SearchableTextAreaPanel(printWriterArea), BorderLayout.CENTER);

        closeButton = new JButton("Close");
        closeButton.setEnabled(false);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        panel.add(closeButton, BorderLayout.SOUTH);

        setContentPane(panel);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    public void startBackgroundAnalysis(final int chip, final String optionsFilename, final String inputFilename, final String outputFilename) {

        final Dfr disassembler = new Dfr();
        Thread disassemblerThread = new Thread(new Runnable() {
            public void run() {
                boolean wasVerbose = emulatorUI.getPrefs().getOutputOptions().contains(OutputOption.VERBOSE);
                emulatorUI.getPrefs().getOutputOptions().add(OutputOption.VERBOSE);
                PrintWriter debugPrintWriter = printWriterArea.getPrintWriter();
                try {
                    debugPrintWriter.println("Initializing disassembler...");
                    disassembler.setDebugPrintWriter(debugPrintWriter);
                    disassembler.setOutputFileName(outputFilename);
                    disassembler.readOptions(optionsFilename);
                    disassembler.setOutputOptions(emulatorUI.getPrefs().getOutputOptions());
                    disassembler.setMemory(memory);
                    disassembler.initialize();
                    debugPrintWriter.println("Starting disassembly...");
                    emulatorUI.setCodeStructure(chip, disassembler.disassembleMemRanges());
                    disassembler.cleanup();
                    debugPrintWriter.println();
                    debugPrintWriter.println("Disassembly complete.");
                    if (emulatorUI.getPrefs().getOutputOptions().contains(OutputOption.STRUCTURE)) {
                        debugPrintWriter.println("You may now use the 'Code Structure' and 'Source Code' windows");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    debugPrintWriter.println("ERROR : " + e.getClass().getName() + ": " + e.getMessage());
                    debugPrintWriter.println("See console for more information");
                }
                emulatorUI.getPrefs().setOutputOption(OutputOption.VERBOSE, wasVerbose);
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                closeButton.setEnabled(true);
                emulatorUI.updateStates();
            }
        });
        disassemblerThread.start();
    }
}
