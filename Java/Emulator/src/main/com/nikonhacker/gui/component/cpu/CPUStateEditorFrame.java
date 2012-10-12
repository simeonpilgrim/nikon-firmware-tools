package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CPUStateEditorFrame extends DocumentFrame {

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private Timer _timer;

    final CPUStateComponent cpuPanel;

    private boolean editable;

    public CPUStateEditorFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, final CPUState cpuState) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        if (cpuState instanceof FrCPUState) {
            cpuPanel = new FrCPUStateComponent((FrCPUState) cpuState, false);
        }
        else {
            cpuPanel = new TxCPUStateComponent((TxCPUState) cpuState, false);
        }

        getContentPane().add(cpuPanel);

        cpuPanel.refresh();

        // Prepare update timer
        _timer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cpuPanel.refresh();
            }
        });
        if (!editable) {
            _timer.start();
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        cpuPanel.refresh();
        cpuPanel.setEditable(editable);
        if (editable) {
            if (_timer.isRunning()) {
                _timer.stop();
            }
        }
        else {
            if (!_timer.isRunning()) {
                _timer.start();
            }
        }
    }

    public void dispose() {
        _timer.stop();
        _timer = null;
        super.dispose();
    }
}
