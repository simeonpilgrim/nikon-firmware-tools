package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.disassembly.DisassemblyLogger;
import com.nikonhacker.gui.swing.DocumentFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CPUStateEditorFrame extends DocumentFrame {

    private final CPUStateComponent cpuPanel;

    private boolean editable = false;

    public CPUStateEditorFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, final CPUState cpuState, int refreshInterval) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        if (cpuState instanceof FrCPUState) {
            cpuPanel = new FrCPUStateComponent((FrCPUState) cpuState, false);
        }
        else {
            cpuPanel = new TxCPUStateComponent((TxCPUState) cpuState, false);
        }

        getContentPane().add(cpuPanel);

    }

    public void setLogger(DisassemblyLogger logger) {
        if (cpuPanel != null) {
            cpuPanel.setLogger(logger);
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        cpuPanel.setEditable(editable);
        if (editable) {
            cpuPanel.refresh();
        }
    }

    public void dispose() {
        super.dispose();
    }
}
