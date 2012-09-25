package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public abstract class CPUStateComponent extends JComponent {
    protected CPUState cpuState;
    protected CPUState cpuStateValidityFlags;
    protected JButton saveButton;
    protected JButton cancelButton = new JButton("Cancel");
    protected JTextField[] regTextFields;

    protected void dumpFieldToRegister(JTextField textField, int registerNumber) {
        if (StringUtils.isBlank(textField.getText())) {
            cpuStateValidityFlags.setReg(registerNumber, 0);
        }
        else {
            cpuState.setReg(registerNumber, Format.parseIntHexField(textField));
            cpuStateValidityFlags.setReg(registerNumber, 1);
        }
    }

    public abstract void refresh();

    protected void setAllCpuStateFlags(CPUState cpuStateFlags, boolean validity) {
        int value = validity?1:0;

        for (int i = 0; i < regTextFields.length; i++) {
            cpuStateFlags.setReg(i, value);
        }
    }

    public abstract void setEditable(boolean editable);

    public abstract void saveValuesAndFlags();
}
