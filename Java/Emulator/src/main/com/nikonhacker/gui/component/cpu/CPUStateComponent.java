package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.PrintWriter;

public abstract class CPUStateComponent extends JComponent {
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


    public abstract void refresh();

    public abstract void setEditable(boolean editable);

    public abstract void saveValuesAndFlags();
}
