package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.Format;
import com.nikonhacker.dfr.CPUState;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This component displays a CPU State.
 * It has two modes :
 * - normal : CPUState is passed and values are reloaded each time "refresh" is called
 * - filter : in this case, both a CPUState and validityFlags are passed, and allow to setup a breakpoint by specifying only some fields
 */
public class CPUStateComponent extends JComponent {
    private CPUState cpuState;
    private CPUState cpuStateValidityFlags;
    private boolean filterMode;

    private JTextField[] regTextFields = new JTextField[15];

    private JTextField pcTextField = new JTextField();
    private JTextField ilmTextField = new JTextField();
    private JTextField scrTextField = new JTextField();
    private JTextField ccrTextField = new JTextField();
    private JTextField tbrTextField = new JTextField();
    private JTextField rpTextField = new JTextField();
    private JTextField sspTextField = new JTextField();
    private JTextField uspTextField = new JTextField();
    private JTextField mdhTextField = new JTextField();
    private JTextField mdlTextField = new JTextField();

    JLabel r15Label0 = new JLabel(" ", JLabel.LEFT);
    JLabel r15Label1 = new JLabel(" ", JLabel.LEFT);

    private JButton saveButton;
    private JButton cancelButton = new JButton("Cancel");

    public CPUStateComponent(final CPUState cpuState, boolean filterMode) {
        CPUState allFlagsSet = new CPUState();
        setAllCpuStateFlags(allFlagsSet, true);

        init(cpuState, allFlagsSet, filterMode);
    }

    public CPUStateComponent(final CPUState cpuState, CPUState cpuStateValidityFlags, boolean filterMode) {
        init(cpuState, cpuStateValidityFlags, filterMode);
    }

    private void setAllCpuStateFlags(CPUState cpuStateFlags, boolean validity) {

        int value = validity?1:0;

        cpuStateFlags.pc = value;
        cpuStateFlags.setPS(validity ? 0xFFFFFFFF : 0, false);
        cpuStateFlags.setReg(CPUState.TBR, value);
        cpuStateFlags.setReg(CPUState.RP, value);
        cpuStateFlags.setReg(CPUState.SSP, value);
        cpuStateFlags.setReg(CPUState.USP, value);
        cpuStateFlags.setReg(CPUState.MDH, value);
        cpuStateFlags.setReg(CPUState.MDL, value);
        for (int i = 0; i < regTextFields.length; i++) {
            cpuStateFlags.setReg(i, value);
        }
    }

    public void init(final CPUState cpuState, CPUState cpuStateValidityFlags, boolean filterMode) {
        this.cpuState = cpuState;
        this.cpuStateValidityFlags = cpuStateValidityFlags;
        this.filterMode = filterMode;
        
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        for (int i = 0; i < regTextFields.length; i++) {
            regTextFields[i] = new JTextField();
            regTextFields[i].setHorizontalAlignment(JTextField.RIGHT);
        }
        pcTextField.setHorizontalAlignment(JTextField.RIGHT);
        ilmTextField.setHorizontalAlignment(JTextField.RIGHT);
        scrTextField.setHorizontalAlignment(JTextField.RIGHT);
        ccrTextField.setHorizontalAlignment(JTextField.RIGHT);
        tbrTextField.setHorizontalAlignment(JTextField.RIGHT);
        rpTextField.setHorizontalAlignment(JTextField.RIGHT);
        sspTextField.setHorizontalAlignment(JTextField.RIGHT);
        uspTextField.setHorizontalAlignment(JTextField.RIGHT);
        mdhTextField.setHorizontalAlignment(JTextField.RIGHT);
        mdlTextField.setHorizontalAlignment(JTextField.RIGHT);

        //setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setLayout(new GridLayout(0, 4));

        add(new JLabel("PC = 0x", JLabel.RIGHT));
        add(pcTextField);
        add(new JLabel(CPUState.REG_LABEL[0]+" = 0x", JLabel.RIGHT));
        add(regTextFields[0]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(CPUState.REG_LABEL[1]+" = 0x", JLabel.RIGHT));
        add(regTextFields[1]);

        add(new JLabel());
        add(new JLabel("43210 ", JLabel.RIGHT));
        add(new JLabel(CPUState.REG_LABEL[2]+" = 0x", JLabel.RIGHT));
        add(regTextFields[2]);

        add(new JLabel("ILM = 0b", JLabel.RIGHT));
        add(ilmTextField);
        add(new JLabel(CPUState.REG_LABEL[3]+" = 0x", JLabel.RIGHT));
        add(regTextFields[3]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(CPUState.REG_LABEL[4]+" = 0x", JLabel.RIGHT));
        add(regTextFields[4]);

        add(new JLabel());
        add(new JLabel("D01T ", JLabel.RIGHT));
        add(new JLabel(CPUState.REG_LABEL[5]+" = 0x", JLabel.RIGHT));
        add(regTextFields[5]);

        add(new JLabel("SCR = 0b", JLabel.RIGHT));
        add(scrTextField);
        add(new JLabel(CPUState.REG_LABEL[6]+" = 0x", JLabel.RIGHT));
        add(regTextFields[6]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(CPUState.REG_LABEL[7]+" = 0x", JLabel.RIGHT));
        add(regTextFields[7]);

        add(new JLabel());
        add(new JLabel("SINZVC ", JLabel.RIGHT));
        add(new JLabel(CPUState.REG_LABEL[8]+" = 0x", JLabel.RIGHT));
        add(regTextFields[8]);

        add(new JLabel("CCR = 0b", JLabel.RIGHT));
        add(ccrTextField);
        add(new JLabel(CPUState.REG_LABEL[9]+" = 0x", JLabel.RIGHT));
        add(regTextFields[9]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(CPUState.REG_LABEL[10]+" = 0x", JLabel.RIGHT));
        add(regTextFields[10]);

        add(new JLabel("TBR = 0x", JLabel.RIGHT));
        add(tbrTextField);
        add(new JLabel(CPUState.REG_LABEL[11]+" = 0x", JLabel.RIGHT));
        add(regTextFields[11]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(CPUState.REG_LABEL[12]+" = 0x", JLabel.RIGHT));
        add(regTextFields[12]);

        add(new JLabel("RP = 0x", JLabel.RIGHT));
        add(rpTextField);
        add(new JLabel(CPUState.REG_LABEL[13]+" = 0x", JLabel.RIGHT));
        add(regTextFields[13]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(CPUState.REG_LABEL[14]+" = 0x", JLabel.RIGHT));
        add(regTextFields[14]);

        add(new JLabel("SSP = 0x", JLabel.RIGHT));
        add(sspTextField);
        add(r15Label0);
        add(new JLabel());

        add(new JLabel("USP = 0x", JLabel.RIGHT));
        add(uspTextField);
        add(r15Label1);
        add(new JLabel());

        add(new JLabel());
        add(new JLabel());
        add(new JLabel());
        add(new JLabel());

        add(new JLabel("MDH/MDL = 0x", JLabel.RIGHT));
        add(mdhTextField);
        add(mdlTextField);
        add(new JLabel());

        if (!filterMode) {
            add(new JLabel());
            saveButton = new JButton("Save");
            Font font = saveButton.getFont();
            font = new Font(font.getName(), font.getStyle(), 10);
            saveButton.setFont(font);
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    validateAndSaveValues(cpuState);
                }
            });
            cancelButton.setFont(font);
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    refresh();
                }
            });
            add(saveButton);
            add(cancelButton);
            add(new JLabel());
        }

    }

    /**
     * This method is called whenever the contents needs to be painted based on the current cpuState
     */
    public void refresh() {
        if (filterMode) {
            pcTextField.setText((cpuStateValidityFlags.pc == 0)?"":Format.asHex(cpuState.pc, 8));
            ilmTextField.setText((cpuStateValidityFlags.getILM() == 0)?"":maskValue(cpuState.getILM(), cpuStateValidityFlags.getILM(), 5));
            scrTextField.setText((cpuStateValidityFlags.getSCR() == 0)?"":maskValue(cpuState.getSCR(), cpuStateValidityFlags.getSCR(), 3));
            ccrTextField.setText((cpuStateValidityFlags.getCCR() == 0)?"":maskValue(cpuState.getCCR(), cpuStateValidityFlags.getCCR(), 6));
            tbrTextField.setText((cpuStateValidityFlags.getReg(CPUState.TBR) == 0)?"":Format.asHex(cpuState.getReg(CPUState.TBR), 8));
            rpTextField.setText((cpuStateValidityFlags.getReg(CPUState.RP) == 0)?"":Format.asHex(cpuState.getReg(CPUState.RP), 8));
            sspTextField.setText((cpuStateValidityFlags.getReg(CPUState.SSP) == 0)?"":Format.asHex(cpuState.getReg(CPUState.SSP), 8));
            uspTextField.setText((cpuStateValidityFlags.getReg(CPUState.USP) == 0)?"":Format.asHex(cpuState.getReg(CPUState.USP), 8));
            mdhTextField.setText((cpuStateValidityFlags.getReg(CPUState.MDH) == 0)?"":Format.asHex(cpuState.getReg(CPUState.MDH), 8));
            mdlTextField.setText((cpuStateValidityFlags.getReg(CPUState.MDL) == 0)?"":Format.asHex(cpuState.getReg(CPUState.MDL), 8));


            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                regTextFields[i].setText((cpuStateValidityFlags.getReg(i) == 0)?"":Format.asHex(cpuState.getReg(i), 8));
            }

            r15Label0.setText("");
            r15Label1.setText("");
        }
        else {
            pcTextField.setText(Format.asHex(cpuState.pc, 8));
            ilmTextField.setText(Format.asBinary(cpuState.getILM(), 5));
            scrTextField.setText(Format.asBinary(cpuState.getSCR(), 3));
            ccrTextField.setText(Format.asBinary(cpuState.getCCR(), 6));
            tbrTextField.setText(Format.asHex(cpuState.getReg(CPUState.TBR), 8));
            rpTextField.setText(Format.asHex(cpuState.getReg(CPUState.RP), 8));
            sspTextField.setText(Format.asHex(cpuState.getReg(CPUState.SSP), 8));
            uspTextField.setText(Format.asHex(cpuState.getReg(CPUState.USP), 8));
            mdhTextField.setText(Format.asHex(cpuState.getReg(CPUState.MDH), 8));
            mdlTextField.setText(Format.asHex(cpuState.getReg(CPUState.MDL), 8));
    
            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                regTextFields[i].setText(Format.asHex(cpuState.getReg(i), 8));
            }
            r15Label0.setText((cpuState.getS() == 0)?"=R15":"");
            r15Label1.setText((cpuState.getS() == 1)?"=R15":"");
        }
    }

    private String maskValue(int value, int mask, int numChars) {
        String formattedValue = Format.asBinary(value, numChars);
        String formattedMask = Format.asBinary(mask, numChars);
        String out = "";
        for (int i = 0; i < numChars; i++) {
            out += (formattedMask.charAt(i) == '1')?formattedValue.charAt(i):"?";
        }
        return out;
    }


    /**
     * Clears all fields of the component
     */
    public void clear() {
        setAllCpuStateFlags(cpuStateValidityFlags, false);
        refresh();
//        pcTextField.setText("");
//        ilmTextField.setText("");
//        scrTextField.setText("");
//        ccrTextField.setText("");
//        tbrTextField.setText("");
//        rpTextField.setText("");
//        sspTextField.setText("");
//        uspTextField.setText("");
//        mdhTextField.setText("");
//        mdlTextField.setText("");
//
//        // General purpose registers
//        for (JTextField regTextField : regTextFields) {
//            regTextField.setText("");
//        }
//
//        r15Label0.setText("");
//        r15Label1.setText("");
    }


    private void validateAndSaveValues(CPUState cpuState) {
        try {
            int pc = Format.parseIntHexField(pcTextField);
            int ilm = Format.parseIntBinaryField(ilmTextField, false);
            int scr = Format.parseIntBinaryField(scrTextField, false);
            int ccr = Format.parseIntBinaryField(ccrTextField, false);
            int tbr = Format.parseIntHexField(tbrTextField);
            int rp = Format.parseIntHexField(rpTextField);
            int ssp = Format.parseIntHexField(sspTextField);
            int usp = Format.parseIntHexField(uspTextField);
            int mdh = Format.parseIntHexField(mdhTextField);
            int mdl = Format.parseIntHexField(mdlTextField);

            // General purpose registers
            int regs[] = new int[regTextFields.length];
            for (int i = 0; i < regTextFields.length; i++) {
                regs[i] = Format.parseIntHexField(regTextFields[i]);
            }

            // If we are here, everything has been parsed correctly. Commit to actual cpuState.

            cpuState.pc = pc;
            cpuState.setILM(ilm, false);
            cpuState.setSCR(scr);
            cpuState.setCCR(ccr);
            cpuState.setReg(CPUState.TBR, tbr);
            cpuState.setReg(CPUState.RP, rp);
            cpuState.setReg(CPUState.SSP, ssp);
            cpuState.setReg(CPUState.USP, usp);
            cpuState.setReg(CPUState.MDH, mdh);
            cpuState.setReg(CPUState.MDL, mdl);

            for (int i = 0; i < regTextFields.length; i++) {
                cpuState.setReg(i, regs[i]);
            }
        } catch (NumberFormatException e) {
            // noop
        }
    }

    /**
     * This method saves values in "filter" mode
     */
    public void saveValuesAndFlags() {

        try {
            if (StringUtils.isBlank(pcTextField.getText())) {
                cpuStateValidityFlags.pc = 0;
            }
            else {
                cpuState.pc = Format.parseIntHexField(pcTextField);
                cpuStateValidityFlags.pc = 1;
            }

            if (StringUtils.isBlank(ilmTextField.getText())) {
                cpuStateValidityFlags.setILM(0, false);
            }
            else {
                cpuState.setILM(Format.parseIntBinaryField(ilmTextField, true), false);
                cpuStateValidityFlags.setILM(Integer.parseInt(ilmTextField.getText().replace('0', '1').replace('?', '0'), 2), false);
            }

            if (StringUtils.isBlank(scrTextField.getText())) {
                cpuStateValidityFlags.setSCR(0);
            }
            else {
                cpuState.setSCR(Format.parseIntBinaryField(scrTextField, true));
                cpuStateValidityFlags.setSCR(Integer.parseInt(scrTextField.getText().replace('0', '1').replace('?', '0'), 2));
            }

            if (StringUtils.isBlank(ccrTextField.getText())) {
                cpuStateValidityFlags.setCCR(0);
            }
            else {
                cpuState.setCCR(Format.parseIntBinaryField(ccrTextField, true));
                cpuStateValidityFlags.setCCR(Integer.parseInt(ccrTextField.getText().replace('0', '1').replace('?', '0'), 2));
            }


            dumpFieldToRegister(tbrTextField, CPUState.TBR);
            dumpFieldToRegister(rpTextField, CPUState.RP);
            dumpFieldToRegister(sspTextField, CPUState.SSP);
            dumpFieldToRegister(uspTextField, CPUState.USP);
            dumpFieldToRegister(mdhTextField, CPUState.MDH);
            dumpFieldToRegister(mdlTextField, CPUState.MDL);

            for (int i = 0; i < regTextFields.length; i++) {
                dumpFieldToRegister(regTextFields[i], i);
            }
        } catch (NumberFormatException e) {
            System.err.println(e.getClass().getName() + " " + e.getMessage());
        }

    }

    private void dumpFieldToRegister(JTextField textField, int registerNumber) {
        if (StringUtils.isBlank(textField.getText())) {
            cpuStateValidityFlags.setReg(registerNumber, 0);
        }
        else {
            cpuState.setReg(registerNumber, Format.parseIntHexField(textField));
            cpuStateValidityFlags.setReg(registerNumber, 1);
        }
    }


    public void setEditable(boolean editable) {

        pcTextField.setEditable(editable);
        pcTextField.setBackground(Color.WHITE);

        ilmTextField.setEditable(editable);
        ilmTextField.setBackground(Color.WHITE);

        scrTextField.setEditable(editable);
        scrTextField.setBackground(Color.WHITE);

        ccrTextField.setEditable(editable);
        ccrTextField.setBackground(Color.WHITE);

        tbrTextField.setEditable(editable);
        tbrTextField.setBackground(Color.WHITE);

        rpTextField.setEditable(editable);
        rpTextField.setBackground(Color.WHITE);

        sspTextField.setEditable(editable);
        sspTextField.setBackground(Color.WHITE);

        uspTextField.setEditable(editable);
        uspTextField.setBackground(Color.WHITE);

        mdhTextField.setEditable(editable);
        mdhTextField.setBackground(Color.WHITE);

        mdlTextField.setEditable(editable);
        mdlTextField.setBackground(Color.WHITE);

        for (JTextField regTextField : regTextFields) {
            regTextField.setEditable(editable);
            regTextField.setBackground(Color.WHITE);
        }

        saveButton.setEnabled(editable);
        cancelButton.setEnabled(editable);
    }

}
