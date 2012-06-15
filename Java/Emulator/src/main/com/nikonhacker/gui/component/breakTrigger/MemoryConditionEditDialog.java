package com.nikonhacker.gui.component.breakTrigger;

import com.nikonhacker.Format;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MemoryConditionEditDialog extends JDialog {
    private final JTextField addressField;
    private JCheckBox negateCheckBox;
    private final JTextField valueField;
    private final JTextField maskField;

    public MemoryConditionEditDialog(JDialog owner, final MemoryValueBreakCondition memoryValueBreakCondition, String title) {
        super(owner, title, true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.add(new JLabel("@(0x"));
        addressField = new JTextField(10);
        addressField.setText(Format.asHex(memoryValueBreakCondition.getAddress(), 8));
        centerPanel.add(addressField);

        centerPanel.add(new JLabel(") & 0x"));
        maskField = new JTextField(10);
        maskField.setText(Format.asHex(memoryValueBreakCondition.getMask(), 8));
        centerPanel.add(maskField);

        negateCheckBox = new JCheckBox();
        negateCheckBox.setText("!");
        negateCheckBox.setSelected(memoryValueBreakCondition.isNegate());
        centerPanel.add(negateCheckBox);

        centerPanel.add(new JLabel("= 0x"));
        valueField = new JTextField(10);
        valueField.setText(Format.asHex(memoryValueBreakCondition.getValue(), 8));
        centerPanel.add(valueField);


        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                memoryValueBreakCondition.setAddress(Format.parseIntHexField(addressField));
                memoryValueBreakCondition.setNegate(negateCheckBox.isSelected());
                memoryValueBreakCondition.setValue(Format.parseIntHexField(valueField));
                memoryValueBreakCondition.setMask(Format.parseIntHexField(maskField));
                dispose();
            }
        });
        bottomPanel.add(okButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
}
