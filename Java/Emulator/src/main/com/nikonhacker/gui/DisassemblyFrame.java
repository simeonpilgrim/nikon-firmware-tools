package com.nikonhacker.gui;

import com.nikonhacker.emu.Emulator;

import javax.swing.*;
import java.awt.*;


public class DisassemblyFrame extends DocumentFrame {
    private static final int ROWS = 50;
    private static final int COLUMNS = 90;
    private Emulator emulator;

    public DisassemblyFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, Emulator emulator, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.emulator = emulator;

        PrintWriterArea textArea = new PrintWriterArea(ROWS, COLUMNS);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        emulator.setInstructionPrintStream(textArea.getPrintStream());

        getContentPane().add(new JScrollPane(textArea));
    }
    
    public void dispose() {
        emulator.setInstructionPrintStream(null);
        super.dispose();
    }

}
