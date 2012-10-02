package com.nikonhacker.gui.component.disassembly;

import com.nikonhacker.emu.Emulator;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.PrintWriterArea;
import com.nikonhacker.gui.component.SearchableTextAreaPanel;

import java.awt.*;


public class DisassemblyFrame extends DocumentFrame {
    private static final int ROWS = 50;
    private static final int COLUMNS = 100;
    private Emulator emulator;

    public DisassemblyFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, Emulator emulator) {
        super(title, resizable, closable, maximizable, iconifiable, chip, ui);
        this.emulator = emulator;

        PrintWriterArea textArea = new PrintWriterArea(ROWS, COLUMNS);

        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

        emulator.setInstructionPrintWriter(textArea.getPrintWriter());

        getContentPane().add(new SearchableTextAreaPanel(textArea));
    }
    
    public void dispose() {
        emulator.setInstructionPrintWriter(null);
        super.dispose();
    }

}
