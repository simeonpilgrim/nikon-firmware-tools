package com.nikonhacker.gui.component.breakTrigger;

import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.gui.component.cpu.CPUStateComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BreakTriggerEditDialog extends JDialog {
    private final CPUStateComponent cpuStateComponent;
    private BreakTrigger trigger;
    private final JTextField nameField;
    private JCheckBox enabledCheckBox;

    public BreakTriggerEditDialog(JDialog owner, BreakTrigger trigger, String title) {
        super(owner, title, true);
        this.trigger = trigger;

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Name : "));
        nameField = new JTextField(20);
        nameField.setText(trigger.getName());
        topPanel.add(nameField);
        enabledCheckBox = new JCheckBox();
        enabledCheckBox.setText("Enabled");
        enabledCheckBox.setSelected(trigger.isEnabled());
        topPanel.add(enabledCheckBox);
        //topPanel.add(new JLabel("Enabled"));
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
                

        JTabbedPane tabbedPane = new JTabbedPane();
        cpuStateComponent = new CPUStateComponent(trigger.getCpuStateValues(), trigger.getCpuStateFlags(), true);
        cpuStateComponent.refresh();
        tabbedPane.addTab("CPU conditions", null, cpuStateComponent);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        bottomPanel.add(okButton);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(closeButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void save() {
        trigger.setName(nameField.getText());
        trigger.setEnabled(enabledCheckBox.isSelected());
        cpuStateComponent.saveValuesAndFlags();
        dispose();
    }
}
