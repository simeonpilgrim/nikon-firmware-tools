package com.nikonhacker.gui.component.memoryMapped;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.MemoryActivityListener;
import com.nikonhacker.emu.memory.listener.PageAccessLoggerActivityListener;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.PrintWriterArea;

import javax.swing.*;
import java.awt.*;


public class MemoryPageMappedComponentFrame extends DocumentFrame {
    private static final int ROWS = 60;
    private static final int COLUMNS = 80;

    DebuggableMemory memory;
    private final MemoryActivityListener listener;

    public MemoryPageMappedComponentFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, DebuggableMemory memory, int page, CPUState cpuState) {
        super(title, resizable, closable, maximizable, iconifiable, chip, ui);
        this.memory = memory;
        final PrintWriterArea textArea = new PrintWriterArea(ROWS, COLUMNS);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        listener = new PageAccessLoggerActivityListener(textArea.getPrintWriter(), page, cpuState);
        memory.addActivityListener(listener);
        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }
    
    public void dispose() {
        memory.removeActivityListener(listener);
        super.dispose();
    }
    
}
