package com.nikonhacker.gui.component.interruptController;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.InterruptRequest;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.ExpeedIoListener;
import com.nikonhacker.emu.peripherials.interruptController.FrInterruptController;
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
import java.util.TimerTask;
import java.util.Vector;

/**
 * This component emulates an interrupt controller with manual operations
 * 
 * See http://mcu.emea.fujitsu.com/document/products_mcu/mb91350/documentation/ds91350a-ds07-16503-5e.pdf, p55-62
 */
public class InterruptControllerFrame extends DocumentFrame {

    private InterruptController interruptController;

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private javax.swing.Timer _timer;
    private final JList interruptList;

    Timer interruptTimer = null;

    public InterruptControllerFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, final EmulatorUI ui, final InterruptController interruptController, final DebuggableMemory memory) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.interruptController = interruptController;

        Insets buttonInsets = new Insets(1,1,1,1);

        JTabbedPane tabbedPane = new JTabbedPane();
        
        
        // Standard button interrupt panel (only the ones defined as External + NMI)
        
        JPanel standardInterruptControllerPanel = new JPanel(new BorderLayout());

        ActionListener standardInterruptButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JInterruptButton button = (JInterruptButton) e.getSource();
                int interruptNumber = button.getInterruptNumber();
                String interruptName;
                int icr;
                boolean isNMI;
                if (interruptNumber == 0xF) {
                    interruptName = "NMI";
                    icr = 0;
                    isNMI = true;
                }
                else {
                    int irNumber = interruptNumber - FrInterruptController.INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET;
                    interruptName = "IR" + (irNumber < 10 ? "0" : "") + irNumber;
                    int icrAddress = irNumber + ExpeedIoListener.REGISTER_ICR00;
                    icr = memory.loadUnsigned8(icrAddress) & 0x1F;
                    isNMI = false;
                }
                InterruptRequest interruptRequest = new InterruptRequest(interruptNumber, isNMI, icr);
                if (interruptController.request(interruptRequest)) {
                    ui.setStatusText(Constants.CHIP_FR, interruptName + " (" +  interruptRequest + ") was requested.");
                }
                else {
                    ui.setStatusText(Constants.CHIP_FR, interruptName + " (" +  interruptRequest + ") was rejected (already requested).");
                }
            }
        };

        JPanel standardButtonGrid = new JPanel(new GridLayout(0,4));

        JInterruptButton nmiButton = new JInterruptButton("INT 0x0F = NMI", 0x0F);
        nmiButton.setForeground(Color.RED);
        nmiButton.setMargin(buttonInsets);
        nmiButton.addActionListener(standardInterruptButtonListener);
        standardButtonGrid.add(nmiButton);

        for (int value = 0; value < 47; value++) {
            JInterruptButton button = new JInterruptButton("INT 0x" + Format.asHex(value + FrInterruptController.INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET, 2)
                    + " = IR" + (value<10?"0":"") + value, value + FrInterruptController.INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET);
            button.setMargin(buttonInsets);
            button.addActionListener(standardInterruptButtonListener);
            standardButtonGrid.add(button);
        }

        standardInterruptControllerPanel.add(standardButtonGrid, BorderLayout.CENTER);

        tabbedPane.addTab("Standard", null, standardInterruptControllerPanel);

        // Custom 256 button panel

        JPanel customInterruptControllerPanel = new JPanel(new BorderLayout());
        
        JPanel topToolbar = new JPanel(new FlowLayout());
        topToolbar.add(new JLabel("ICR : "));
        Vector<String> labels = new Vector<String>();
        for (int i = 0; i < 32; i++) {
            labels.add(Format.asBinary(i, 5));
        }
        final JComboBox icrComboBox = new JComboBox(labels);
        topToolbar.add(icrComboBox);

        final JCheckBox nmiCheckBox = new JCheckBox("NMI");
        topToolbar.add(nmiCheckBox);

        customInterruptControllerPanel.add(topToolbar, BorderLayout.NORTH);
        
        ActionListener customInterruptButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JInterruptButton button = (JInterruptButton) e.getSource();
                int interruptNumber = button.getInterruptNumber();
                if (interruptController.request(new InterruptRequest(interruptNumber, nmiCheckBox.isSelected(), icrComboBox.getSelectedIndex()))) {
                    ui.setStatusText(Constants.CHIP_FR, "Interrupt 0x" + Format.asHex(interruptNumber, 2) + " was requested.");
                }
                else {
                    ui.setStatusText(Constants.CHIP_FR, "Interrupt 0x" + Format.asHex(interruptNumber, 2) + " was rejected (already requested).");
                }
            }
        };

        JPanel customButtonGrid = new JPanel(new GridLayout(16, 16));
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                final int value = i * 16 + j;
                JInterruptButton button = new JInterruptButton(Format.asHex(value, 2), value);
                button.setMargin(buttonInsets);
                button.addActionListener(customInterruptButtonListener);
                customButtonGrid.add(button);
            }
        }

        customInterruptControllerPanel.add(customButtonGrid, BorderLayout.CENTER);

        tabbedPane.addTab("Custom", null, customInterruptControllerPanel);


        // Timer panel
        
        JPanel timerPanel = new JPanel(new BorderLayout());
        
        JPanel timerIntermediaryPanel = new JPanel(new FlowLayout());

        JPanel timerParamPanel = new JPanel(new GridLayout(0, 3));

        timerParamPanel.add(new JLabel("Request INT"));
        Vector<String> timerInterruptNumber = new Vector<String>();
        for (int i = 0; i < 255; i++) {
            timerInterruptNumber.add("0x" + Format.asHex(i, 2));
        }
        final JComboBox timerInterruptComboBox = new JComboBox(timerInterruptNumber);
        timerParamPanel.add(timerInterruptComboBox);
        timerParamPanel.add(new JLabel(""));

        timerParamPanel.add(new JLabel("with ICR : "));
        Vector<String> timerIcrLabels = new Vector<String>();
        for (int i = 0; i < 32; i++) {
            timerIcrLabels.add(Format.asBinary(i, 5));
        }
        final JComboBox timerIcrComboBox = new JComboBox(timerIcrLabels);
        timerParamPanel.add(timerIcrComboBox);        
        timerParamPanel.add(new JLabel(""));

        timerParamPanel.add(new JLabel("NMI"));
        final JCheckBox timerNmiCheckBox = new JCheckBox();
        timerParamPanel.add(timerNmiCheckBox);
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
                    startTimer(timerInterruptComboBox.getSelectedIndex(), timerNmiCheckBox.isSelected(), timerIcrComboBox.getSelectedIndex(), Integer.parseInt((String) intervalsComboBox.getSelectedItem()));
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
        
//        timerPanel.add(new JLabel("WARNING: Use 'debug' and not 'play' otherwise interrupts are only checked every 1000 cycles"), BorderLayout.SOUTH);

        tabbedPane.addTab("Timer", null, timerPanel);


        // Queue panel

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


    private void stopTimer() {
        if (interruptTimer != null) {
            interruptTimer.cancel();
            interruptTimer = null;
        }
        ui.setStatusText(Constants.CHIP_FR, "Stopped interrupt timer");
    }

    private void startTimer(final int interruptNumber, final boolean isNmi, final int icr, int interval) {
        interruptTimer = new Timer(false);
        interruptTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                interruptController.request(new InterruptRequest(interruptNumber, isNmi, icr));
            }
        }, 0, interval);
        ui.setStatusText(Constants.CHIP_FR, "Interrupt 0x" + Format.asHex(interruptNumber, 2) + " will be requested every " + interval + "ms");
    }

    public void dispose() {
        stopTimer();
        super.dispose();
    }

}
