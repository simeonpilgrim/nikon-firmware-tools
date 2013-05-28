package com.nikonhacker.gui.component.timer;

import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This component shows the status of programmable timers and allows to enable/disable them globally
 */
public class ProgrammableTimersFrame extends DocumentFrame {

    private final ProgrammableTimer[] timers;

    private static final int UPDATE_INTERVAL_MS = 99; // 10fps, but not exactly so that we don't refresh exactly at the same counter value

    private javax.swing.Timer refreshTimer;
    private final JList timerList;
    private final JLabel statusLabel;
    private final JButton enableButton;
    private final JPanel topPanel;

    public ProgrammableTimersFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui, final ProgrammableTimer[] timers) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.timers = timers;

        statusLabel = new JLabel();
        enableButton = new JButton();
        enableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ui.toggleProgrammableTimers(chip);
            }
        });

        topPanel = new JPanel(new BorderLayout());
        topPanel.add(statusLabel, BorderLayout.CENTER);
        topPanel.add(enableButton, BorderLayout.EAST);

        JPanel listPanel = new JPanel(new BorderLayout());
        timerList = new JList();
        timerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timerList.setLayoutOrientation(JList.VERTICAL);
        timerList.setVisibleRowCount(10);
        JScrollPane listScroller = new JScrollPane(timerList);
        listPanel.add(listScroller, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(listPanel, BorderLayout.CENTER);

        getContentPane().add(mainPanel);

        // Prepare refresh timer
        refreshTimer = new javax.swing.Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateList();
            }
        });
        refreshTimer.start();

        updateState(timers[0].isActive());
    }

    private void toggleProgrammableTimers(int chip) {
        boolean active = !timers[0].isActive();
        for (ProgrammableTimer timer : timers) {
            timer.setActive(active);
        }
        // Start/stop button animation
        ui.setProgrammableTimerAnimationEnabled(chip, active);
        updateState(active);
    }

    public void updateState(boolean active) {
        // Change label/button
        statusLabel.setText("Timer emulation is " + (active ? "active" : "inactive"));
        enableButton.setText(active ? "Disable" : "Enable");
        topPanel.setBackground(active ? Color.GREEN : new Color(255, 128, 128));
    }

    private void updateList() {
        synchronized (timers) {
            DefaultListModel model = new DefaultListModel();
            for (ProgrammableTimer timer : timers) {
                model.addElement(timer.toString());
            }
            timerList.setModel(model);
        }
    }

    public void dispose() {
        super.dispose();
        refreshTimer.stop();
    }

}
