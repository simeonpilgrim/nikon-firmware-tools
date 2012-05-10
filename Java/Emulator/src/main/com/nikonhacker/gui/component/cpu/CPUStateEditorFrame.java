package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.dfr.CPUState;
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

    public CPUStateEditorFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final CPUState cpuState, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);

        cpuPanel = new CPUStateComponent(cpuState, false);

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
