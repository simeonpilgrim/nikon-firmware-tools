package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.fr.FrCPUState;
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
public class FrCPUStateComponent extends CPUStateComponent {
    private boolean filterMode;

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

    public FrCPUStateComponent(final FrCPUState cpuState, boolean filterMode) {
        FrCPUState allFlagsSet = new FrCPUState();

        regTextFields = new JTextField[32];

        setAllCpuStateFlags(allFlagsSet, true);

        init(cpuState, allFlagsSet, filterMode);
    }

    public FrCPUStateComponent(final FrCPUState cpuState, FrCPUState cpuStateValidityFlags, boolean filterMode) {
        init(cpuState, cpuStateValidityFlags, filterMode);
    }

    @Override
    protected void setAllCpuStateFlags(CPUState cpuStateFlags, boolean validity) {
        int value = validity?1:0;

        cpuStateFlags.pc = value;
        ((FrCPUState)cpuStateFlags).setPS(validity ? 0xFFFFFFFF : 0, false);
        cpuStateFlags.setReg(FrCPUState.TBR, value);
        cpuStateFlags.setReg(FrCPUState.RP, value);
        cpuStateFlags.setReg(FrCPUState.SSP, value);
        cpuStateFlags.setReg(FrCPUState.USP, value);
        cpuStateFlags.setReg(FrCPUState.MDH, value);
        cpuStateFlags.setReg(FrCPUState.MDL, value);
        super.setAllCpuStateFlags(cpuStateFlags, validity);
    }

    public void init(final FrCPUState cpuState, FrCPUState cpuStateValidityFlags, boolean filterMode) {
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
        add(new JLabel(FrCPUState.registerLabels[0]+" = 0x", JLabel.RIGHT));
        add(regTextFields[0]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(FrCPUState.registerLabels[1]+" = 0x", JLabel.RIGHT));
        add(regTextFields[1]);

        add(new JLabel());
        add(new JLabel("43210 ", JLabel.RIGHT));
        add(new JLabel(FrCPUState.registerLabels[2]+" = 0x", JLabel.RIGHT));
        add(regTextFields[2]);

        add(new JLabel("ILM = 0b", JLabel.RIGHT));
        add(ilmTextField);
        add(new JLabel(FrCPUState.registerLabels[3]+" = 0x", JLabel.RIGHT));
        add(regTextFields[3]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(FrCPUState.registerLabels[4]+" = 0x", JLabel.RIGHT));
        add(regTextFields[4]);

        add(new JLabel());
        add(new JLabel("D01T ", JLabel.RIGHT));
        add(new JLabel(FrCPUState.registerLabels[5]+" = 0x", JLabel.RIGHT));
        add(regTextFields[5]);

        add(new JLabel("SCR = 0b", JLabel.RIGHT));
        add(scrTextField);
        add(new JLabel(FrCPUState.registerLabels[6]+" = 0x", JLabel.RIGHT));
        add(regTextFields[6]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(FrCPUState.registerLabels[7]+" = 0x", JLabel.RIGHT));
        add(regTextFields[7]);

        add(new JLabel());
        add(new JLabel("SINZVC ", JLabel.RIGHT));
        add(new JLabel(FrCPUState.registerLabels[8]+" = 0x", JLabel.RIGHT));
        add(regTextFields[8]);

        add(new JLabel("CCR = 0b", JLabel.RIGHT));
        add(ccrTextField);
        add(new JLabel(FrCPUState.registerLabels[9]+" = 0x", JLabel.RIGHT));
        add(regTextFields[9]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(FrCPUState.registerLabels[10]+" = 0x", JLabel.RIGHT));
        add(regTextFields[10]);

        add(new JLabel("TBR = 0x", JLabel.RIGHT));
        add(tbrTextField);
        add(new JLabel(FrCPUState.registerLabels[11]+" = 0x", JLabel.RIGHT));
        add(regTextFields[11]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(FrCPUState.registerLabels[12]+" = 0x", JLabel.RIGHT));
        add(regTextFields[12]);

        add(new JLabel("RP = 0x", JLabel.RIGHT));
        add(rpTextField);
        add(new JLabel(FrCPUState.registerLabels[13]+" = 0x", JLabel.RIGHT));
        add(regTextFields[13]);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel(FrCPUState.registerLabels[14]+" = 0x", JLabel.RIGHT));
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
    @Override
    public void refresh() {
        if (filterMode) {
            pcTextField.setText((cpuStateValidityFlags.pc == 0)?"":Format.asHex(cpuState.pc, 8));
            ilmTextField.setText((((FrCPUState)cpuStateValidityFlags).getILM() == 0)?"":maskValue(((FrCPUState)cpuState).getILM(), ((FrCPUState)cpuStateValidityFlags).getILM(), 5));
            scrTextField.setText((((FrCPUState)cpuStateValidityFlags).getSCR() == 0)?"":maskValue(((FrCPUState)cpuState).getSCR(), ((FrCPUState)cpuStateValidityFlags).getSCR(), 3));
            ccrTextField.setText((((FrCPUState)cpuStateValidityFlags).getCCR() == 0)?"":maskValue(((FrCPUState)cpuState).getCCR(), ((FrCPUState)cpuStateValidityFlags).getCCR(), 6));
            tbrTextField.setText((cpuStateValidityFlags.getReg(FrCPUState.TBR) == 0)?"":Format.asHex(cpuState.getReg(FrCPUState.TBR), 8));
            rpTextField.setText((cpuStateValidityFlags.getReg(FrCPUState.RP) == 0)?"":Format.asHex(cpuState.getReg(FrCPUState.RP), 8));
            sspTextField.setText((cpuStateValidityFlags.getReg(FrCPUState.SSP) == 0)?"":Format.asHex(cpuState.getReg(FrCPUState.SSP), 8));
            uspTextField.setText((cpuStateValidityFlags.getReg(FrCPUState.USP) == 0)?"":Format.asHex(cpuState.getReg(FrCPUState.USP), 8));
            mdhTextField.setText((cpuStateValidityFlags.getReg(FrCPUState.MDH) == 0)?"":Format.asHex(cpuState.getReg(FrCPUState.MDH), 8));
            mdlTextField.setText((cpuStateValidityFlags.getReg(FrCPUState.MDL) == 0)?"":Format.asHex(cpuState.getReg(FrCPUState.MDL), 8));


            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                regTextFields[i].setText((cpuStateValidityFlags.getReg(i) == 0)?"":Format.asHex(cpuState.getReg(i), 8));
            }

            r15Label0.setText("");
            r15Label1.setText("");
        }
        else {
            pcTextField.setText(Format.asHex(cpuState.pc, 8));
            ilmTextField.setText(Format.asBinary((((FrCPUState)cpuState).getILM()), 5));
            scrTextField.setText(Format.asBinary((((FrCPUState)cpuState).getSCR()), 3));
            ccrTextField.setText(Format.asBinary((((FrCPUState)cpuState).getCCR()), 6));
            tbrTextField.setText(Format.asHex(cpuState.getReg(FrCPUState.TBR), 8));
            rpTextField.setText(Format.asHex(cpuState.getReg(FrCPUState.RP), 8));
            sspTextField.setText(Format.asHex(cpuState.getReg(FrCPUState.SSP), 8));
            uspTextField.setText(Format.asHex(cpuState.getReg(FrCPUState.USP), 8));
            mdhTextField.setText(Format.asHex(cpuState.getReg(FrCPUState.MDH), 8));
            mdlTextField.setText(Format.asHex(cpuState.getReg(FrCPUState.MDL), 8));
    
            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                regTextFields[i].setText(Format.asHex(cpuState.getReg(i), 8));
            }
            r15Label0.setText((((FrCPUState)cpuState).getS() == 0)?"=R15":"");
            r15Label1.setText((((FrCPUState)cpuState).getS() == 1)?"=R15":"");
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


//    /**
//     * Clears all fields of the component
//     */
//    public void clear() {
//        setAllCpuStateFlags(cpuStateValidityFlags, false);
//        refresh();
////        pcTextField.setText("");
////        ilmTextField.setText("");
////        scrTextField.setText("");
////        ccrTextField.setText("");
////        tbrTextField.setText("");
////        rpTextField.setText("");
////        sspTextField.setText("");
////        uspTextField.setText("");
////        mdhTextField.setText("");
////        mdlTextField.setText("");
////
////        // General purpose registers
////        for (JTextField regTextField : regTextFields) {
////            regTextField.setText("");
////        }
////
////        r15Label0.setText("");
////        r15Label1.setText("");
//    }


    private void validateAndSaveValues(FrCPUState cpuState) {
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
            cpuState.setReg(FrCPUState.TBR, tbr);
            cpuState.setReg(FrCPUState.RP, rp);
            cpuState.setReg(FrCPUState.SSP, ssp);
            cpuState.setReg(FrCPUState.USP, usp);
            cpuState.setReg(FrCPUState.MDH, mdh);
            cpuState.setReg(FrCPUState.MDL, mdl);

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
    @Override
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
                ((FrCPUState)cpuStateValidityFlags).setILM(0, false);
            }
            else {
                ((FrCPUState)cpuState).setILM(Format.parseIntBinaryField(ilmTextField, true), false);
                ((FrCPUState)cpuStateValidityFlags).setILM(Integer.parseInt(ilmTextField.getText().replace('0', '1').replace('?', '0'), 2), false);
            }

            if (StringUtils.isBlank(scrTextField.getText())) {
                ((FrCPUState)cpuStateValidityFlags).setSCR(0);
            }
            else {
                ((FrCPUState)cpuState).setSCR(Format.parseIntBinaryField(scrTextField, true));
                ((FrCPUState)cpuStateValidityFlags).setSCR(Integer.parseInt(scrTextField.getText().replace('0', '1').replace('?', '0'), 2));
            }

            if (StringUtils.isBlank(ccrTextField.getText())) {
                ((FrCPUState)cpuStateValidityFlags).setCCR(0);
            }
            else {
                ((FrCPUState)cpuState).setCCR(Format.parseIntBinaryField(ccrTextField, true));
                ((FrCPUState)cpuStateValidityFlags).setCCR(Integer.parseInt(ccrTextField.getText().replace('0', '1').replace('?', '0'), 2));
            }


            dumpFieldToRegister(tbrTextField, FrCPUState.TBR);
            dumpFieldToRegister(rpTextField, FrCPUState.RP);
            dumpFieldToRegister(sspTextField, FrCPUState.SSP);
            dumpFieldToRegister(uspTextField, FrCPUState.USP);
            dumpFieldToRegister(mdhTextField, FrCPUState.MDH);
            dumpFieldToRegister(mdlTextField, FrCPUState.MDL);

            for (int i = 0; i < regTextFields.length; i++) {
                dumpFieldToRegister(regTextFields[i], i);
            }
        } catch (NumberFormatException e) {
            System.err.println(e.getClass().getName() + " " + e.getMessage());
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
