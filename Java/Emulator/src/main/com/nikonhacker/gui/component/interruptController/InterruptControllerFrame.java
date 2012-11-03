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

    private javax.swing.Timer _timer;
    private final JList interruptList;

    Timer interruptTimer = null;

    public InterruptControllerFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, final EmulatorUI ui, final InterruptController interruptController, final DebuggableMemory memory) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.interruptController = interruptController;

        Insets buttonInsets = new Insets(1,1,1,1);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Add custom panels, chip dependant

        addTabs(ui, interruptController, memory, buttonInsets, tabbedPane);

        // Add "Queue" panel

        JPanel queuePanel = new JPanel(new BorderLayout());

        interruptList = new JList();
        interruptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        interruptList.setLayoutOrientation(JList.VERTICAL);
        interruptList.setVisibleRowCount(10);

        JScrollPane listScroller = new JScrollPane(interruptList);

        queuePanel.add(listScroller, BorderLayout.CENTER);

        tabbedPane.addTab("Queue", null, queuePanel);


        // Add tab panel

        getContentPane().add(tabbedPane);


        // Prepare update timer

        _timer = new javax.swing.Timer(UPDATE_INTERVAL_MS*10, new ActionListener() {
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
                    setQueueAutoRefresh(true);
                }
                else {
                    setQueueAutoRefresh(false);
                }
            }
        });

    }

    protected abstract void addTabs(EmulatorUI ui, InterruptController interruptController, DebuggableMemory memory, Insets buttonInsets, JTabbedPane tabbedPane);

    private void updateList() {
        synchronized (interruptController.getInterruptRequestQueue()) {
            DefaultListModel model = new DefaultListModel();
            // Real stack
            for (InterruptRequest request : interruptController.getInterruptRequestQueue()) {
                model.addElement(request.toString());
            }
            interruptList.setModel(model);
        }
    }

    public void setQueueAutoRefresh(boolean enabled) {
        updateList();
        if (enabled) {
            if (!_timer.isRunning()) {
                _timer.start();
            }
        }
        else {
            if (_timer.isRunning()) {
                _timer.stop();
            }
        }
    }


    protected abstract void stopTimer();

    public void dispose() {
        stopTimer();
        super.dispose();
    }

}
