package com.nikonhacker.gui.component.interruptController;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.TxInterruptController;
import com.nikonhacker.gui.EmulatorUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;
import java.util.Vector;

/**
 * This component emulates a TX interrupt controller with manual operations
 */
public class TxInterruptControllerFrame extends InterruptControllerFrame {

    public TxInterruptControllerFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, final EmulatorUI ui, final InterruptController interruptController, final DebuggableMemory memory) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui, interruptController, memory);
    }

    /**
     * Create and add the Interrupt Controller Frame tabs pertaining to the FR CPU
     * @param ui
     * @param interruptController
     * @param memory
     * @param buttonInsets
     * @param tabbedPane
     */
    protected void addTabs(final EmulatorUI ui, final InterruptController interruptController, final DebuggableMemory memory, Insets buttonInsets, JTabbedPane tabbedPane) {

        // Standard button interrupt panel ("hardware interrupts")

        JPanel standardInterruptControllerPanel = new JPanel(new BorderLayout());

        ActionListener standardInterruptButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JInterruptButton button = (JInterruptButton) e.getSource();
                int interruptNumber = button.getInterruptNumber();

                String interruptName = "Interrupt " + button.getText() + " (" + interruptNumber + ")";
                if (interruptController.request(interruptNumber)) {
                    ui.setStatusText(Constants.CHIP_TX, interruptName + " was requested.");
                }
                else {
                    ui.setStatusText(Constants.CHIP_TX, interruptName + " was rejected !");
                }
            }
        };

        JPanel standardButtonGrid = new JPanel(new GridLayout(0,8));

        for (int value = 0; value < 128; value++) {
            JInterruptButton button = createInterruptButton(value);
            button.setMargin(buttonInsets);
            button.addActionListener(standardInterruptButtonListener);
            standardButtonGrid.add(button);
        }

        standardInterruptControllerPanel.add(standardButtonGrid, BorderLayout.CENTER);

        tabbedPane.addTab("Standard", null, standardInterruptControllerPanel);

        // Timer panel

        JPanel timerPanel = new JPanel(new BorderLayout());

        JPanel timerIntermediaryPanel = new JPanel(new FlowLayout());

        JPanel timerParamPanel = new JPanel(new GridLayout(0, 3));

        timerParamPanel.add(new JLabel("Request INT"));
        Vector<ListEntry> timerInterruptNumber = new Vector<ListEntry>();
        for (int i = 0; i < 128; i++) {
            String description = TxInterruptController.hardwareInterruptDescription[i].description;
            if (description != null) {
                timerInterruptNumber.add(new ListEntry(i, "0x" + Format.asHex(i, 2) + " " + description));
            }
        }
        final JComboBox timerInterruptComboBox = new JComboBox(timerInterruptNumber);
        timerParamPanel.add(timerInterruptComboBox);
        timerParamPanel.add(new JLabel(""));

        timerParamPanel.add(new JLabel("every "));
        Vector<String> intervals = new Vector<String>();
        intervals.add("1000");
        intervals.add("100");
        intervals.add("10");
        intervals.add("1");
        final JComboBox intervalsComboBox = new JComboBox(intervals);
        timerParamPanel.add(intervalsComboBox);
        timerParamPanel.add(new JLabel("ms"));

        timerParamPanel.add(new JLabel(""));

        JButton startTimerButton = new JButton("Start");
        startTimerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (interruptTimer == null) {
                    startTimer(((ListEntry)(timerInterruptComboBox.getSelectedItem())).value, Integer.parseInt((String) intervalsComboBox.getSelectedItem()));
                    ((JButton) e.getSource()).setText("Stop");
                }
                else {
                    stopTimer();
                    ((JButton) e.getSource()).setText("Start");
                }
            }
        });
        timerParamPanel.add(startTimerButton);

        timerIntermediaryPanel.add(timerParamPanel);

        timerPanel.add(timerIntermediaryPanel,BorderLayout.CENTER);

        tabbedPane.addTab("Timer", null, timerPanel);
    }

    private JInterruptButton createInterruptButton(int value) {
        TxInterruptController.InterruptDescription interruptDescription = TxInterruptController.hardwareInterruptDescription[value];
        String symbolicName = interruptDescription.symbolicName;
        JInterruptButton button = new JInterruptButton(symbolicName, value);
        if (symbolicName == null) {
            button.setText("-");
            button.setForeground(Color.ORANGE.darker());
        }

        button.setToolTipText(Format.asHex(value, 2) + " " + interruptDescription.description);

        return button;
    }

    protected void startTimer(final int interruptNumber, int interval) {
        interruptTimer = new java.util.Timer(false);
        interruptTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                interruptController.request(interruptNumber);
            }
        }, 0, interval);
        ui.setStatusText(Constants.CHIP_FR, "Interrupt 0x" + Format.asHex(interruptNumber, 2) + " will be requested every " + interval + "ms");
    }

    protected void stopTimer() {
        if (interruptTimer != null) {
            interruptTimer.cancel();
            interruptTimer = null;
        }
        ui.setStatusText(Constants.CHIP_TX, "Stopped interrupt timer");
    }

    private class ListEntry {
        int value;
        String text;

        public ListEntry(int value, String text) {
            this.value = value;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
