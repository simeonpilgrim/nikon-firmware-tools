package com.nikonhacker.gui.component.interruptController;

import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;

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

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private       javax.swing.Timer refreshTimer;
    private final JList             interruptQueueJList;
    private final JLabel            statusText;

    Timer interruptTimer = null;

    private boolean editable = false;
    private final JTabbedPane tabbedPane;
    private final JButton removeButton;

    public InterruptControllerFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui, final InterruptController interruptController, final Memory memory) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.interruptController = interruptController;

        Insets buttonInsets = new Insets(1, 1, 1, 1);

        tabbedPane = new JTabbedPane();

        // Add custom panels, chip dependant

        addTabs(ui, interruptController, memory, buttonInsets, tabbedPane);

        // Add "Status" panel

        JPanel statusPanel = new JPanel(new BorderLayout());

        statusText = new JLabel("Current interrupt level...");
        statusPanel.add(statusText, BorderLayout.NORTH);

        interruptQueueJList = new JList();
        interruptQueueJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        interruptQueueJList.setLayoutOrientation(JList.VERTICAL);
        interruptQueueJList.setVisibleRowCount(10);

        JScrollPane listScroller = new JScrollPane(interruptQueueJList);

        statusPanel.add(listScroller, BorderLayout.CENTER);

        removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = interruptQueueJList.getSelectedIndex();
                if (selectedIndex != -1) {
                    interruptController.getInterruptRequestQueue().remove(selectedIndex);
                    updateList();
                }
            }
        });
        statusPanel.add(removeButton, BorderLayout.SOUTH);

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
                updateInterruptListRefreshTimer();
            }
        });

    }

    protected abstract void addTabs(EmulatorUI ui, InterruptController interruptController, Memory memory, Insets buttonInsets, JTabbedPane tabbedPane);

    protected abstract int getStatusTabIndex();

    private void updateList() {
        statusText.setText(interruptController.getStatus());
        synchronized (interruptController.getInterruptRequestQueue()) {
            DefaultListModel model = new DefaultListModel();
            // Real stack
            for (InterruptRequest request : interruptController.getInterruptRequestQueue()) {
                model.addElement(request.toString());
            }
            interruptQueueJList.setModel(model);
        }
    }

    protected void updateInterruptListRefreshTimer() {
        // Only refresh if corresponding tab is selected && editable is false
        if (tabbedPane.getSelectedIndex() == getStatusTabIndex() && !editable) {
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


    public void setEditable(boolean editable) {
        updateList();
        this.editable = editable;
        removeButton.setEnabled(editable);
        updateInterruptListRefreshTimer();
    }

    protected abstract void stopTimer();

    public void dispose() {
        stopTimer();
        super.dispose();
    }

}
