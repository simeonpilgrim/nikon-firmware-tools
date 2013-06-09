package com.nikonhacker.gui.component.disassembly;

import com.nikonhacker.emu.Emulator;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.PrintWriterArea;
import com.nikonhacker.gui.component.SearchableTextAreaPanel;
import com.nikonhacker.gui.swing.DocumentFrame;

import java.awt.*;
import java.io.PrintWriter;


public class DisassemblyFrame extends DocumentFrame {
    private static final int ROWS = 50;
    private static final int COLUMNS = 100;
    private Emulator emulator;
    private final PrintWriter instructionPrintWriter;

    public DisassemblyFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, Emulator emulator) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.emulator = emulator;

        PrintWriterArea textArea = new PrintWriterArea(ROWS, COLUMNS);
        textArea.setAutoScroll(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

        instructionPrintWriter = textArea.getPrintWriter();

        emulator.setPrinter(instructionPrintWriter);

        getContentPane().add(new SearchableTextAreaPanel(textArea));
    }

    public PrintWriter getInstructionPrintWriter() {
        return instructionPrintWriter;
    }

    public void dispose() {
        emulator.setPrinter(null);
        super.dispose();
    }

}
