package com.nikonhacker.gui.component.analyse;

import com.nikonhacker.Format;
import com.nikonhacker.dfr.Dfr;
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
import java.net.URL;
import java.util.Properties;

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

    public void startGeneration() {
        Thread disassemblerThread = new Thread(new Runnable() {
            public void run() {
                PrintWriter debugPrintWriter = printWriterArea.getPrintWriter();
                try {
                    debugPrintWriter.println("Assuming interrupt vector at 0x" + Format.asHex(INTERRUPT_VECTOR_BASE_ADDRESS, 8) + "...");
                    int int40address = memory.load32(INTERRUPT_VECTOR_BASE_ADDRESS + 0x3FC - 0x40 * 4);
                    debugPrintWriter.println("INT 0x40 is at 0x" + Format.asHex(int40address, 8) + "...");
                    debugPrintWriter.println("Assuming the layout of D5100 is standard, the base address for system calls computation is stored at 0x" + Format.asHex(int40address + 64, 8) + "...");
                    int baseAddress = memory.loadInstruction32(int40address + 64);
                    debugPrintWriter.println("Base address is thus 0x" + Format.asHex(baseAddress, 8) + "...");

                    Properties properties = new Properties() ;
                    URL url = GenerateSysSymbolsDialog.class.getResource("realos-systemcalls.properties");
                    //properties.load(new FileInputStream(new File(url.getFile())));
                    properties.load(url.openStream());

                    debugPrintWriter.println("The following lines can be pasted to a dfr.txt file :");
                    debugPrintWriter.println();

                    for (Object o : properties.keySet()) {
                        int offset = Format.parseUnsigned((String) o);
                        String value = (String) properties.get(o);
                        int sysFunctionAddress = baseAddress + Dfr.signExtend(16, memory.loadInstruction16(baseAddress + offset * 2));
                        debugPrintWriter.println("-s 0x" + Format.asHex(sysFunctionAddress, 8) + "=" + value);
                    }
                    debugPrintWriter.println();
                    debugPrintWriter.println("The lines above can be pasted to a dfr.txt file :");
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
