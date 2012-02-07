package com.nikonhacker.gui.component.disassembly;

import com.nikonhacker.emu.Emulator;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.PrintStreamArea;

import javax.swing.*;
import java.awt.*;


public class DisassemblyFrame extends DocumentFrame {
    private static final int ROWS = 50;
    private static final int COLUMNS = 90;
    private Emulator emulator;
    private boolean followTail = true;
    private final JScrollPane scrollPane;

    public DisassemblyFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, Emulator emulator, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.emulator = emulator;

        final PrintStreamArea textArea = new PrintStreamArea(ROWS, COLUMNS);
        scrollPane = new JScrollPane(textArea);

        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
//        textArea.getDocument().addDocumentListener(new DocumentListener() {
//            public void insertUpdate(DocumentEvent e) {
//                if (followTail) {
//                    textArea.scrollRectToVisible(new Rectangle(0, textArea.getHeight() - 2, 1, 1));
//                }
//            }
//
//            public void removeUpdate(DocumentEvent e) {}
//            public void changedUpdate(DocumentEvent e) {
//            }
//        });
        emulator.setInstructionPrintStream(textArea.getPrintStream());

//            scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
//            public void adjustmentValueChanged(AdjustmentEvent e) {
//                if (!e.getValueIsAdjusting()) {
//                    JScrollBar scrollBar = (JScrollBar) e.getSource();
//                    if (scrollBar.getVisibleAmount() == scrollBar.getMaximum()) {
//                        // Fully visible. Following is meaningless  TODO is this useful ?
//                        followTail = false;
//                    }
//                    else {
//                        // Start following if scrollbar has been placed (dragged or due to text insertion) to (or near) the bottom
//                        followTail = (scrollBar.getValue() + scrollBar.getVisibleAmount() > scrollBar.getMaximum());
//                    }
//                }
//            }
//        });
        getContentPane().add(scrollPane);
    }
    
    public void dispose() {
        emulator.setInstructionPrintStream(null);
        super.dispose();
    }

}
