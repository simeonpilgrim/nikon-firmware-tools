package com.nikonhacker.gui.component.interruptController;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.InterruptRequest;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
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
    protected static final int INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET = 0x10;
    private static final int ICR0_BASE_ADDRESS = 0x400;

    private Emulator emulator;

    Timer timer = null;

    public InterruptControllerFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final Emulator emulator, final DebuggableMemory memory, final EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.emulator = emulator;

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
                    int irNumber = interruptNumber - INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET;
                    interruptName = "IR" + (irNumber < 10 ? "0" : "") + irNumber;
                    int icrAddress = irNumber + ICR0_BASE_ADDRESS;
                    icr = memory.loadUnsigned8(icrAddress) & 0x1F;
                    isNMI = false;
                }
                InterruptRequest interruptRequest = new InterruptRequest(interruptNumber, isNMI, icr);
                if (emulator.addInterruptRequest(interruptRequest)) {
                    ui.setStatusText(interruptName + " (" +  interruptRequest + ") was requested.");
                }
                else {
                    ui.setStatusText(interruptName + " (" +  interruptRequest + ") was rejected (already requested).");
                }
            }
        };

        JPanel standardButtonGrid = new JPanel(new GridLayout(0,4));

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 16; j++) {
                final int value = i * 16 + j;
                JInterruptButton button = new JInterruptButton("IR" + (value<10?"0":"") + value, value + INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET);
                button.setMargin(buttonInsets);
                button.addActionListener(standardInterruptButtonListener);
                standardButtonGrid.add(button);
            }
        }
        
        standardInterruptControllerPanel.add(standardButtonGrid, BorderLayout.CENTER);
        
        JInterruptButton nmiButton = new JInterruptButton("INT 0x0F = NMI", 0x0F);
        nmiButton.setMargin(buttonInsets);
        nmiButton.addActionListener(standardInterruptButtonListener);

        standardInterruptControllerPanel.add(nmiButton, BorderLayout.SOUTH);


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
                if (emulator.addInterruptRequest(new InterruptRequest(interruptNumber, nmiCheckBox.isSelected(), icrComboBox.getSelectedIndex()))) {
                    ui.setStatusText("Interrupt 0x" + Format.asHex(interruptNumber, 2) + " was requested.");
                }
                else {
                    ui.setStatusText("Interrupt 0x" + Format.asHex(interruptNumber, 2) + " was rejected (already requested).");
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
                if (timer == null) {
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

        getContentPane().add(tabbedPane);
        pack();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        ui.setStatusText("Stopped interrupt timer");
    }

    private void startTimer(final int interruptNumber, final boolean isNmi, final int icr, int interval) {
        timer = new Timer(false);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                emulator.addInterruptRequest(new InterruptRequest(interruptNumber, isNmi, icr));
            }
        }, 0, interval);
        ui.setStatusText("Interrupt 0x" + Format.asHex(interruptNumber, 2) + " will be requested every " + interval + "ms");
    }

    public void dispose() {
        stopTimer();
        super.dispose();
    }

}
