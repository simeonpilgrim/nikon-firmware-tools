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

    private JLabel r15Label0 = new JLabel(" ", JLabel.LEFT);
    private JLabel r15Label1 = new JLabel(" ", JLabel.LEFT);

    public FrCPUStateComponent(final FrCPUState cpuState, boolean filterMode) {
        this.regTextFields = new JTextField[FrCPUState.NUM_STD_REGISTERS];

        FrCPUState allFlagsSet = new FrCPUState();
        setAllCpuStateFlags(allFlagsSet, true);

        init(cpuState, allFlagsSet, filterMode);
    }

    public FrCPUStateComponent(final FrCPUState cpuState, FrCPUState cpuStateValidityFlags, boolean filterMode) {
        this.regTextFields = new JTextField[32];

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

        Font fixedFont = new Font(Font.MONOSPACED, Font.PLAIN, 11);

        for (int i = 0; i < regTextFields.length; i++) {
            regTextFields[i] = new JTextField();
            regTextFields[i].setHorizontalAlignment(JTextField.RIGHT);
            regTextFields[i].setFont(fixedFont);
        }
        pcTextField.setHorizontalAlignment(JTextField.RIGHT); pcTextField.setFont(fixedFont);
        ilmTextField.setHorizontalAlignment(JTextField.RIGHT); ilmTextField.setFont(fixedFont);
        scrTextField.setHorizontalAlignment(JTextField.RIGHT); scrTextField.setFont(fixedFont);
        ccrTextField.setHorizontalAlignment(JTextField.RIGHT); ccrTextField.setFont(fixedFont);

        JPanel registerPanel = new JPanel(new GridLayout(0, 4));

        registerPanel.add(new JLabel("PC = 0x", JLabel.RIGHT));
        registerPanel.add(pcTextField);
        registerPanel.add(new JLabel(FrCPUState.registerLabels[0] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[0]);

        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel(FrCPUState.registerLabels[1] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[1]);

        registerPanel.add(new JLabel());
        JLabel label1 = new JLabel("43210", JLabel.RIGHT); label1.setFont(fixedFont);
        registerPanel.add(label1);
        registerPanel.add(new JLabel(FrCPUState.registerLabels[2] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[2]);

        registerPanel.add(new JLabel("PS.ILM = 0b", JLabel.RIGHT));
        registerPanel.add(ilmTextField);
        registerPanel.add(new JLabel(FrCPUState.registerLabels[3] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[3]);

        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel(FrCPUState.registerLabels[4] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[4]);

        registerPanel.add(new JLabel());
        JLabel label2 = new JLabel("D01T", JLabel.RIGHT); label2.setFont(fixedFont);
        registerPanel.add(label2);
        registerPanel.add(new JLabel(FrCPUState.registerLabels[5] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[5]);

        registerPanel.add(new JLabel("PS.SCR = 0b", JLabel.RIGHT));
        registerPanel.add(scrTextField);
        registerPanel.add(new JLabel(FrCPUState.registerLabels[6] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[6]);

        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel(FrCPUState.registerLabels[7] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[7]);

        registerPanel.add(new JLabel());
        JLabel label3 = new JLabel("SINZVC", JLabel.RIGHT); label3.setFont(fixedFont);
        registerPanel.add(label3);
        registerPanel.add(new JLabel(FrCPUState.registerLabels[8] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[8]);

        registerPanel.add(new JLabel("PS.CCR = 0b", JLabel.RIGHT));
        registerPanel.add(ccrTextField);
        registerPanel.add(new JLabel(FrCPUState.registerLabels[9] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[9]);

        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel(FrCPUState.registerLabels[10] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[10]);

        registerPanel.add(new JLabel("TBR = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[FrCPUState.TBR]);
        registerPanel.add(new JLabel(FrCPUState.registerLabels[11] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[11]);

        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel(FrCPUState.registerLabels[12] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[12]);

        registerPanel.add(new JLabel("RP = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[FrCPUState.RP]);
        registerPanel.add(new JLabel(FrCPUState.registerLabels[13] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[13]);

        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel(FrCPUState.registerLabels[14] + " = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[14]);

        registerPanel.add(new JLabel("SSP = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[FrCPUState.SSP]);
        registerPanel.add(r15Label0);
        registerPanel.add(new JLabel());

        registerPanel.add(new JLabel("USP = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[FrCPUState.USP]);
        registerPanel.add(r15Label1);
        registerPanel.add(new JLabel());

        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel());
        registerPanel.add(new JLabel());

        registerPanel.add(new JLabel("MDH/MDL = 0x", JLabel.RIGHT));
        registerPanel.add(regTextFields[FrCPUState.MDH]);
        registerPanel.add(regTextFields[FrCPUState.MDL]);
        registerPanel.add(new JLabel());

        setLayout(new BorderLayout());
        add(new JScrollPane(registerPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        if (!filterMode) {
            JPanel buttonPanel = new JPanel();

            buttonPanel.add(new JLabel());
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
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            buttonPanel.add(new JLabel());

            add(buttonPanel, BorderLayout.SOUTH);
        }

        // Force a refresh, so that the one that will be triggered by the timer will leave a white background
        refresh();
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


            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                regTextFields[i].setText((cpuStateValidityFlags.getReg(i) == 0)?"":Format.asHex(cpuState.getReg(i), 8));
            }

            r15Label0.setText("");
            r15Label1.setText("");
        }
        else {
            updateAndColorTextField(pcTextField, Format.asHex(cpuState.pc, 8));
            updateAndColorTextField(ilmTextField, Format.asBinary((((FrCPUState)cpuState).getILM()), 5));
            updateAndColorTextField(scrTextField, Format.asBinary((((FrCPUState)cpuState).getSCR()), 3));
            updateAndColorTextField(ccrTextField, Format.asBinary((((FrCPUState)cpuState).getCCR()), 6));

            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                updateAndColorTextField(regTextFields[i], Format.asHex(cpuState.getReg(i), 8));
            }
            updateAndColorLabel(r15Label0, (((FrCPUState)cpuState).getS() == 0)?"=R15":"");
            updateAndColorLabel(r15Label1, (((FrCPUState)cpuState).getS() == 1)?"=R15":"");
        }
    }


    private void validateAndSaveValues(FrCPUState cpuState) {
        try {
            int pc = Format.parseIntHexField(pcTextField);
            int ilm = Format.parseIntBinaryField(ilmTextField, false);
            int scr = Format.parseIntBinaryField(scrTextField, false);
            int ccr = Format.parseIntBinaryField(ccrTextField, false);

            // General purpose registers
            int regs[] = new int[regTextFields.length];
            for (int i = 0; i < regTextFields.length; i++) {
                regs[i] = Format.parseIntHexField(regTextFields[i]);
            }

            // If we are here, everything has been parsed correctly. Commit to actual cpuState.

            // Log changes to real-time disassembly window
            if (logger != null) {
                String msg = "";
                if (pc != cpuState.getPc()) msg += changeString("PC", cpuState.getPc(), pc);
                if (ilm != cpuState.getILM()) msg += changeString("ILM", cpuState.getILM(), ilm);
                if (scr != cpuState.getSCR()) msg += changeString("SCR", cpuState.getSCR(), scr);
                if (ccr != cpuState.getCCR()) msg += changeString("CCR", cpuState.getCCR(), ccr);
                for (int i = 0; i < regs.length; i++) {
                    if (regs[i] != cpuState.getReg(i)) msg += changeString(FrCPUState.registerLabels[i], cpuState.getReg(i), regs[i]);
                }
                if (msg.length() > 0) {
                    logger.println("=====> Manual CPU state change:\n" + msg);
                }
            }

            cpuState.pc = pc;
            cpuState.setILM(ilm, false);
            cpuState.setSCR(scr);
            cpuState.setCCR(ccr);

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

            for (int i = 0; i < regTextFields.length; i++) {
                dumpFieldToRegister(regTextFields[i], i);
            }
        } catch (NumberFormatException e) {
            System.err.println(e.getClass().getName() + " " + e.getMessage());
        }

    }


    public void setEditable(boolean editable) {
        pcTextField.setEditable(editable);
        ilmTextField.setEditable(editable);
        scrTextField.setEditable(editable);
        ccrTextField.setEditable(editable);
        for (JTextField regTextField : regTextFields) {
            regTextField.setEditable(editable);
        }

        saveButton.setEnabled(editable);
        cancelButton.setEnabled(editable);
    }

}
