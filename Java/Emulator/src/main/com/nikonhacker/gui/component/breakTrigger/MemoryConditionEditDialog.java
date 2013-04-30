package com.nikonhacker.gui.component.breakTrigger;

import com.nikonhacker.Format;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MemoryConditionEditDialog extends JDialog {

    public MemoryConditionEditDialog(JDialog owner, final MemoryValueBreakCondition memoryValueBreakCondition, String title) {
        super(owner, title, true);

        JPanel mainPanel = new JPanel(new BorderLayout());

        final JTextField addressField = new JTextField(10);
        addressField.setText(Format.asHex(memoryValueBreakCondition.getAddress(), 8));

        ButtonGroup group = new ButtonGroup();
        final JRadioButton onChangeRadioButton = new JRadioButton();
        onChangeRadioButton.setSelected(memoryValueBreakCondition.isChangeDetection());
        group.add(onChangeRadioButton);
        final JRadioButton onGivenValueRadioButton = new JRadioButton();
        onGivenValueRadioButton.setSelected(!memoryValueBreakCondition.isChangeDetection());
        group.add(onGivenValueRadioButton);

        final JTextField maskField = new JTextField(10);
        maskField.setText(Format.asHex(memoryValueBreakCondition.getMask(), 8));

        final JCheckBox negateCheckBox = new JCheckBox();
        negateCheckBox.setText("!");
        negateCheckBox.setSelected(memoryValueBreakCondition.isNegate());

        final JTextField valueField = new JTextField(10);
        valueField.setText(Format.asHex(memoryValueBreakCondition.getValue(), 8));


        JPanel centerPanel = new JPanel(new MigLayout());

        // Span on 2 lines:

        centerPanel.add(new JLabel("@(0x"), "span 1 2");
        centerPanel.add(addressField, "span 1 2");

        centerPanel.add(new JLabel(") & 0x"), "span 1 2");
        centerPanel.add(maskField, "span 1 2");

        // End of first line:

        centerPanel.add(onChangeRadioButton);

        centerPanel.add(new JLabel(" changes"), "span 3, wrap");

        // End of second line:

        centerPanel.add(onGivenValueRadioButton);

        centerPanel.add(negateCheckBox);

        centerPanel.add(new JLabel("= 0x"));

        centerPanel.add(valueField);


        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                memoryValueBreakCondition.setAddress(Format.parseIntHexField(addressField));
                memoryValueBreakCondition.setMask(Format.parseIntHexField(maskField));
                memoryValueBreakCondition.setChangeDetection(onChangeRadioButton.isSelected());
                memoryValueBreakCondition.setNegate(negateCheckBox.isSelected());
                memoryValueBreakCondition.setValue(Format.parseIntHexField(valueField));
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
