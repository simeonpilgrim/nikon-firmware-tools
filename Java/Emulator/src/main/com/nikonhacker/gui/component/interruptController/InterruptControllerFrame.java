package com.nikonhacker.gui.component.interruptController;

import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;

/**
 * This component emulates an interrupt controller with manual operations
 */
public abstract class InterruptControllerFrame extends DocumentFrame {

    protected InterruptController interruptController;

    protected static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private javax.swing.Timer refreshTimer;
    private final JList interruptQueueJList;
    private final JLabel levelLabel;

    Timer interruptTimer = null;

    public InterruptControllerFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, final EmulatorUI ui, final InterruptController interruptController, final DebuggableMemory memory) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.interruptController = interruptController;

        Insets buttonInsets = new Insets(1,1,1,1);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Add custom panels, chip dependant

        addTabs(ui, interruptController, memory, buttonInsets, tabbedPane);

        // Add "Status" panel

        JPanel statusPanel = new JPanel(new BorderLayout());

        levelLabel = new JLabel("Current interrupt level...");
        statusPanel.add(levelLabel, BorderLayout.NORTH);

        interruptQueueJList = new JList();
        interruptQueueJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        interruptQueueJList.setLayoutOrientation(JList.VERTICAL);
        interruptQueueJList.setVisibleRowCount(10);

        JScrollPane listScroller = new JScrollPane(interruptQueueJList);

        statusPanel.add(listScroller, BorderLayout.CENTER);

        tabbedPane.addTab("Status", null, statusPanel);

        // Add tab panel
        getContentPane().add(tabbedPane);

        // Prepare refresh timer
        refreshTimer = new javax.swing.Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateList();
            }
        });


        // Event upon tab change

        tabbedPane.addChangeListener(new ChangeListener() {
            // This method is called whenever the selected tab changes
            public void stateChanged(ChangeEvent evt) {
                JTabbedPane pane = (JTabbedPane)evt.getSource();
                // Only refresh if corresponding tab is selected
                if (pane.getSelectedIndex() == 3) {
                    setStatusAutoRefresh(true);
                }
                else {
                    setStatusAutoRefresh(false);
                }
            }
        });

    }

    protected abstract void addTabs(EmulatorUI ui, InterruptController interruptController, DebuggableMemory memory, Insets buttonInsets, JTabbedPane tabbedPane);

    private void updateList() {
        levelLabel.setText("Current interrupt level: " + interruptController.getCurrentInterruptLevel());
        synchronized (interruptController.getInterruptRequestQueue()) {
            DefaultListModel model = new DefaultListModel();
            // Real stack
            for (InterruptRequest request : interruptController.getInterruptRequestQueue()) {
                model.addElement(request.toString());
            }
            interruptQueueJList.setModel(model);
        }
    }

    public void setStatusAutoRefresh(boolean enabled) {
        updateList();
        if (enabled) {
            if (!refreshTimer.isRunning()) {
                refreshTimer.start();
            }
        }
        else {
            if (refreshTimer.isRunning()) {
                refreshTimer.stop();
            }
        }
    }


    protected abstract void stopTimer();

    public void dispose() {
        stopTimer();
        super.dispose();
    }

}
