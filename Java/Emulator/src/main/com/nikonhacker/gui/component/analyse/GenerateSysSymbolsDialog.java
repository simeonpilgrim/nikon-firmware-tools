package com.nikonhacker.gui.component.analyse;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.Syscall;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.PrintWriterArea;
import com.nikonhacker.gui.component.SearchableTextAreaPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.Map;

public class GenerateSysSymbolsDialog extends JDialog {
    private static final int INTERRUPT_VECTOR_BASE_ADDRESS = 0xDFC00;

    private PrintWriterArea printWriterArea;
    JButton closeButton;
    final JDialog frame = this;
    private EmulatorUI emulatorUI;
    private DebuggableMemory memory;

    public GenerateSysSymbolsDialog(EmulatorUI emulatorUI, DebuggableMemory memory) {
        super(emulatorUI, "System call Symbols generation", true);
        this.emulatorUI = emulatorUI;
        this.memory = memory;

        JPanel panel = new JPanel(new BorderLayout());

        printWriterArea = new PrintWriterArea(25, 70);
        printWriterArea.setAutoScroll(true);

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

    public void startGeneration() {
        Thread disassemblerThread = new Thread(new Runnable() {
            public void run() {
                PrintWriter debugPrintWriter = printWriterArea.getPrintWriter();
                try {
                    Map<Integer,Syscall> syscallMap = Syscall.getMap(memory);
                    debugPrintWriter.println("The following lines can be pasted to a dfr.txt file :");
                    debugPrintWriter.println();
                    for (Syscall syscall : syscallMap.values()) {
                        debugPrintWriter.println("-s 0x" + Format.asHex(syscall.getAddress(), 8) + "=" + syscall.getRawText());
                    }
                    debugPrintWriter.println();
                    debugPrintWriter.println("The lines above can be pasted to a dfr.txt file");
                } catch (Exception e) {
                    e.printStackTrace();
                    debugPrintWriter.println("ERROR : " + e.getClass().getName() + ": " + e.getMessage());
                    debugPrintWriter.println("See console for more information");
                }
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                closeButton.setEnabled(true);
                emulatorUI.updateStates();
            }
        });
        disassemblerThread.start();
    }

}
