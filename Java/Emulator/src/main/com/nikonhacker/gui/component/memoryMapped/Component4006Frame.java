package com.nikonhacker.gui.component.memoryMapped;

import com.nikonhacker.disassembly.fr.CPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.gui.EmulatorUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Component4006Frame extends MemoryPageMappedComponentFrame {
    public Component4006Frame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final DebuggableMemory memory, int page, CPUState cpuState, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, memory, page, cpuState, ui);

        JButton addButton = new JButton("Store 0x1000 at 0x40060010");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                memory.store16(0x40060010, 0x1000);
            }
        });
        add(addButton, BorderLayout.SOUTH);
    }
    
    public void dispose() {
        super.dispose();
    }

}
