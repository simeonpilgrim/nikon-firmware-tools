package com.nikonhacker.gui.component.memoryMapped;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.MemoryActivityListener;
import com.nikonhacker.emu.memory.listener.PageAccessLoggerActivityListener;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;
import com.nikonhacker.gui.swing.PrintWriterArea;

import javax.swing.*;
import java.awt.*;
import java.util.EnumSet;


public class MemoryPageMappedComponentFrame extends DocumentFrame {
    private static final int ROWS = 60;
    private static final int COLUMNS = 80;

    private DebuggableMemory memory;
    private final MemoryActivityListener listener;

    public MemoryPageMappedComponentFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, DebuggableMemory memory, int page, CPUState cpuState) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.memory = memory;
        final PrintWriterArea textArea = new PrintWriterArea(ROWS, COLUMNS);
        textArea.setAutoScroll(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        listener = new PageAccessLoggerActivityListener(textArea.getPrintWriter(), page, cpuState, EnumSet.of(DebuggableMemory.AccessSource.CODE));
        memory.addActivityListener(listener);
        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }
    
    public void dispose() {
        memory.removeActivityListener(listener);
        super.dispose();
    }
    
}
