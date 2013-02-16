package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public abstract class CPUStateComponent extends JComponent {
    public static final Color COLOR_UNCHANGED = Color.WHITE;
    public static final Color COLOR_CHANGED = Color.CYAN;

    protected CPUState cpuState;
    protected CPUState cpuStateValidityFlags;
    protected JButton saveButton;
    protected JButton cancelButton = new JButton("Cancel");
    protected JTextField[] regTextFields;

    protected PrintWriter instructionPrintWriter;

    protected void dumpFieldToRegister(JTextField textField, int registerNumber) {
        if (StringUtils.isBlank(textField.getText())) {
            cpuStateValidityFlags.setReg(registerNumber, 0);
        }
        else {
            cpuState.setReg(registerNumber, Format.parseIntHexField(textField));
            cpuStateValidityFlags.setReg(registerNumber, 1);
        }
    }

    protected void dumpBinFieldToRegister(JTextField textField, int registerNumber) {
        if (StringUtils.isBlank(textField.getText())) {
            cpuStateValidityFlags.setReg(registerNumber, 0);
        }
        else {
            cpuState.setReg(registerNumber, Format.parseIntBinaryField(textField, true));
            cpuStateValidityFlags.setReg(registerNumber, Integer.parseInt(textField.getText().replace('0', '1').replace('?', '0'), 2));
        }
    }

    protected void setAllCpuStateFlags(CPUState cpuStateFlags, boolean validity) {
        int value = validity?1:0;

        for (int i = 0; i < regTextFields.length; i++) {
            cpuStateFlags.setReg(i, value);
        }
    }

    protected static String changeString(String label, int oldValue, int newValue) {
        return changeString(label, "0x" + Format.asHex(oldValue, 8) + " -> 0x" + Format.asHex(newValue, 8));
    }

    protected static String changeString(String label, String text) {
        return "        " + label + ": " + text + "\n";
    }

    public void setInstructionPrintWriter(PrintWriter instructionPrintWriter) {
        this.instructionPrintWriter = instructionPrintWriter;
    }


    protected String maskValue(int value, int mask, int numChars) {
        String formattedValue = Format.asBinary(value, numChars);
        String formattedMask = Format.asBinary(mask, numChars);
        String out = "";
        for (int i = 0; i < numChars; i++) {
            out += (formattedMask.charAt(i) == '1')?formattedValue.charAt(i):"?";
        }
        return out;
    }

    public abstract void refresh();

    public abstract void setEditable(boolean editable);

    public abstract void saveValuesAndFlags();

    protected void updateAndColorCheckBox(JCheckBox checkBox, boolean newValue) {
        checkBox.setBackground(checkBox.isSelected() == newValue? COLOR_UNCHANGED : COLOR_CHANGED);
        checkBox.setSelected(newValue);
    }

    /**
     * Returns true if combo value has changed
     * @param comboBox
     * @param newSelectedIndex
     * @return
     */
    protected boolean updateAndColorCombo(JComboBox comboBox, int newSelectedIndex) {
        boolean changed = comboBox.getSelectedIndex() != newSelectedIndex;
        comboBox.setBackground(changed ? COLOR_CHANGED : COLOR_UNCHANGED);
        // Only set combo index if it changed, because it forces an update of all 32 general purpose registers
        // without any background change, and comparison afterwards would always be false as value has changed
        if (changed) {
            comboBox.setSelectedIndex(newSelectedIndex);
        }
        return !changed;
    }

    protected void updateAndColorTextField(JTextField textField, String newValue) {
        textField.setBackground(textField.getText().equals(newValue)? COLOR_UNCHANGED : COLOR_CHANGED);
        textField.setText(newValue);
    }

    protected void updateAndColorLabel(JLabel label, String newValue) {
        label.setBackground(label.getText().equals(newValue)? COLOR_UNCHANGED : COLOR_CHANGED);
        label.setText(newValue);
    }
}
