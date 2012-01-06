package com.nikonhacker.gui;

import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.PageAccessLoggerActivityListener;

import javax.swing.*;
import java.awt.*;


public class MemoryMappedComponentFrame extends DocumentFrame {
    private static final int ROWS = 60;
    private static final int COLUMNS = 40;

    DebuggableMemory memory;
    private final PageAccessLoggerActivityListener listener;

    public MemoryMappedComponentFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, DebuggableMemory memory, int page, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.memory = memory;
        final PrintWriterArea textArea = new PrintWriterArea(ROWS, COLUMNS);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        listener = new PageAccessLoggerActivityListener(textArea.getPrintStream(), page);
        memory.addActivityListener(listener);
        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.PAGE_START);
    }
    
    public void dispose() {
        memory.removeActivityListener(listener);
        super.dispose();
    }
    
}
