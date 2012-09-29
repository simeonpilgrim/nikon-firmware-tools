package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
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
public class TxCPUStateComponent extends CPUStateComponent {
    private boolean filterMode;

    private JTextField pcTextField = new JTextField();
    private JTextField hiTextField = new JTextField();
    private JTextField loTextField = new JTextField();

    public TxCPUStateComponent(final TxCPUState cpuState, boolean filterMode) {
        TxCPUState allFlagsSet = new TxCPUState();

        regTextFields = new JTextField[32];

        setAllCpuStateFlags(allFlagsSet, true);

        init(cpuState, allFlagsSet, filterMode);
    }

    public TxCPUStateComponent(final TxCPUState cpuState, TxCPUState cpuStateValidityFlags, boolean filterMode) {
        init(cpuState, cpuStateValidityFlags, filterMode);
    }

    @Override
    protected void setAllCpuStateFlags(CPUState cpuStateFlags, boolean validity) {
        int value = validity?1:0;

        cpuStateFlags.pc = value;
        cpuStateFlags.setReg(TxCPUState.HI, value);
        cpuStateFlags.setReg(TxCPUState.LO, value);
        super.setAllCpuStateFlags(cpuStateFlags, validity);
    }

    public void init(final TxCPUState cpuState, TxCPUState cpuStateValidityFlags, boolean filterMode) {
        this.cpuState = cpuState;
        this.cpuStateValidityFlags = cpuStateValidityFlags;
        this.filterMode = filterMode;

        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        for (int i = 0; i < regTextFields.length; i++) {
            regTextFields[i] = new JTextField();
            regTextFields[i].setHorizontalAlignment(JTextField.RIGHT);
        }
        pcTextField.setHorizontalAlignment(JTextField.RIGHT);
        hiTextField.setHorizontalAlignment(JTextField.RIGHT);
        loTextField.setHorizontalAlignment(JTextField.RIGHT);

        //setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setLayout(new GridLayout(0, 4));

        add(new JLabel("pc = 0x", JLabel.RIGHT));
        add(pcTextField);
        add(new JLabel());
        add(new JLabel());

        add(new JLabel());
        add(new JLabel());
        add(new JLabel());
        add(new JLabel());

        for (int i = 0; i < 16; i++) {
            add(new JLabel(TxCPUState.registerLabels[i*2]+" = 0x", JLabel.RIGHT));
            add(regTextFields[i*2]);
            add(new JLabel(TxCPUState.registerLabels[i*2+1]+" = 0x", JLabel.RIGHT));
            add(regTextFields[i*2+1]);
        }

        regTextFields[0].setEnabled(false);

        add(new JLabel());
        add(new JLabel());
        add(new JLabel());
        add(new JLabel());

        add(new JLabel("hi / lo = 0x", JLabel.RIGHT));
        add(hiTextField);
        add(loTextField);
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
            hiTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.HI) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.HI), 8));
            loTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.LO) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.LO), 8));

            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                regTextFields[i].setText((cpuStateValidityFlags.getReg(i) == 0)?"":Format.asHex(cpuState.getReg(i), 8));
            }
        }
        else {
            pcTextField.setText(Format.asHex(cpuState.pc, 8));
            hiTextField.setText(Format.asHex(cpuState.getReg(TxCPUState.HI), 8));
            loTextField.setText(Format.asHex(cpuState.getReg(TxCPUState.LO), 8));

            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                regTextFields[i].setText(Format.asHex(cpuState.getReg(i), 8));
            }
        }
    }

//    /**
//     * Clears all fields of the component
//     */
//    public void clear() {
//        setAllCpuStateFlags(cpuStateValidityFlags, false);
//        refresh();
//        pcTextField.setText("");
//        hiTextField.setText("");
//        loTextField.setText("");
//
//        // General purpose registers
//        for (JTextField regTextField : regTextFields) {
//            regTextField.setText("");
//        }
//    }


    private void validateAndSaveValues(TxCPUState cpuState) {
        try {
            int pc = Format.parseIntHexField(pcTextField);
            int mdh = Format.parseIntHexField(hiTextField);
            int mdl = Format.parseIntHexField(loTextField);

            // General purpose registers
            int regs[] = new int[regTextFields.length];
            for (int i = 0; i < regTextFields.length; i++) {
                regs[i] = Format.parseIntHexField(regTextFields[i]);
            }

            // If we are here, everything has been parsed correctly. Commit to actual cpuState.

            cpuState.pc = pc;
            cpuState.setReg(TxCPUState.HI, mdh);
            cpuState.setReg(TxCPUState.LO, mdl);

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


            dumpFieldToRegister(hiTextField, TxCPUState.HI);
            dumpFieldToRegister(loTextField, TxCPUState.LO);

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

        hiTextField.setEditable(editable);
        hiTextField.setBackground(Color.WHITE);

        loTextField.setEditable(editable);
        loTextField.setBackground(Color.WHITE);

        for (JTextField regTextField : regTextFields) {
            regTextField.setEditable(editable);
            regTextField.setBackground(Color.WHITE);
        }

        saveButton.setEnabled(editable);
        cancelButton.setEnabled(editable);
    }

}
