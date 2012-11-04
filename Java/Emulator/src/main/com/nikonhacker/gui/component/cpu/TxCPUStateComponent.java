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
    private JCheckBox pcIsaMode16bCheckBox = new JCheckBox();
    private JTextField hiTextField = new JTextField();
    private JTextField loTextField = new JTextField();
    private JTextField statusTextField = new JTextField();
    private JTextField causeTextField = new JTextField();
    private JTextField epcTextField = new JTextField();
    private JTextField errorEpcTextField = new JTextField();

    public TxCPUStateComponent(final TxCPUState cpuState, boolean filterMode) {
        this.regTextFields = new JTextField[32];

        TxCPUState allFlagsSet = new TxCPUState();
        setAllCpuStateFlags(allFlagsSet, true);

        init(cpuState, allFlagsSet, filterMode);
    }

    public TxCPUStateComponent(final TxCPUState cpuState, TxCPUState cpuStateValidityFlags, boolean filterMode) {
        this.regTextFields = new JTextField[32];

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
        pcIsaMode16bCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
        hiTextField.setHorizontalAlignment(JTextField.RIGHT);
        loTextField.setHorizontalAlignment(JTextField.RIGHT);
        statusTextField.setHorizontalAlignment(JTextField.RIGHT);
        causeTextField.setHorizontalAlignment(JTextField.RIGHT);
        epcTextField.setHorizontalAlignment(JTextField.RIGHT);
        errorEpcTextField.setHorizontalAlignment(JTextField.RIGHT);

        //setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setLayout(new GridLayout(0, 4));

        add(new JLabel("pc (addr)=0x", JLabel.RIGHT));
        add(pcTextField);
        add(new JLabel("pc (ISA 16b):", JLabel.RIGHT));
        add(pcIsaMode16bCheckBox);

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

        add(new JLabel());
        add(new JLabel());
        add(new JLabel());
        add(new JLabel());

        add(new JLabel("Status = 0x", JLabel.RIGHT));
        add(statusTextField);
        add(new JLabel("Cause = 0x", JLabel.RIGHT));
        add(causeTextField);
        add(new JLabel("EPC = 0x", JLabel.RIGHT));
        add(epcTextField);
        add(new JLabel("ErrEPC=0x", JLabel.RIGHT));
        add(errorEpcTextField);

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
            pcIsaMode16bCheckBox.setSelected(cpuStateValidityFlags.pc == 0 && ((TxCPUState)cpuState).is16bitIsaMode);
            hiTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.HI) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.HI), 8));
            loTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.LO) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.LO), 8));
            statusTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.Status) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.Status), 8));
            causeTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.Cause) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.Cause), 8));
            epcTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.EPC) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.EPC), 8));
            errorEpcTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.ErrorEPC) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.ErrorEPC), 8));

            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                regTextFields[i].setText((cpuStateValidityFlags.getReg(i) == 0)?"":Format.asHex(cpuState.getReg(i), 8));
            }
        }
        else {
            pcTextField.setText(Format.asHex(cpuState.pc, 8));
            pcIsaMode16bCheckBox.setSelected(((TxCPUState)cpuState).is16bitIsaMode);
            hiTextField.setText(Format.asHex(cpuState.getReg(TxCPUState.HI), 8));
            loTextField.setText(Format.asHex(cpuState.getReg(TxCPUState.LO), 8));
            statusTextField.setText(Format.asHex(cpuState.getReg(TxCPUState.Status), 8));
            causeTextField.setText(Format.asHex(cpuState.getReg(TxCPUState.Cause), 8));
            epcTextField.setText(Format.asHex(cpuState.getReg(TxCPUState.EPC), 8));
            errorEpcTextField.setText(Format.asHex(cpuState.getReg(TxCPUState.ErrorEPC), 8));

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
//        statusTextField.setText("");
//        causeTextField.setText("");
//        epcTextField.setText("");
//        errorEpcTextField.setText("");
//
//        // General purpose registers
//        for (JTextField regTextField : regTextFields) {
//            regTextField.setText("");
//        }
//    }


    private void validateAndSaveValues(TxCPUState cpuState) {
        try {
            int pc = Format.parseIntHexField(pcTextField);
            int hi = Format.parseIntHexField(hiTextField);
            int lo = Format.parseIntHexField(loTextField);
            int status = Format.parseIntHexField(statusTextField);
            int cause = Format.parseIntHexField(causeTextField);
            int epc = Format.parseIntHexField(epcTextField);
            int errorPc = Format.parseIntHexField(errorEpcTextField);

            // General purpose registers
            int regs[] = new int[regTextFields.length];
            for (int i = 0; i < regTextFields.length; i++) {
                regs[i] = Format.parseIntHexField(regTextFields[i]);
            }

            // If we are here, everything has been parsed correctly. Commit to actual cpuState.

            cpuState.pc = pc;
            cpuState.is16bitIsaMode = pcIsaMode16bCheckBox.isSelected();
            cpuState.setReg(TxCPUState.HI, hi);
            cpuState.setReg(TxCPUState.LO, lo);
            cpuState.setReg(TxCPUState.Status, status);
            cpuState.setReg(TxCPUState.Cause, cause);
            cpuState.setReg(TxCPUState.EPC, epc);
            cpuState.setReg(TxCPUState.ErrorEPC, errorPc);

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
            dumpFieldToRegister(statusTextField, TxCPUState.Status);
            dumpFieldToRegister(causeTextField, TxCPUState.Cause);
            dumpFieldToRegister(epcTextField, TxCPUState.EPC);
            dumpFieldToRegister(errorEpcTextField, TxCPUState.ErrorEPC);

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

        pcIsaMode16bCheckBox.setEnabled(editable);

        hiTextField.setEditable(editable);
        hiTextField.setBackground(Color.WHITE);

        loTextField.setEditable(editable);
        loTextField.setBackground(Color.WHITE);

        statusTextField.setEditable(editable);
        statusTextField.setBackground(Color.WHITE);

        causeTextField.setEditable(editable);
        causeTextField.setBackground(Color.WHITE);

        epcTextField.setEditable(editable);
        epcTextField.setBackground(Color.WHITE);

        errorEpcTextField.setEditable(editable);
        errorEpcTextField.setBackground(Color.WHITE);

        for (JTextField regTextField : regTextFields) {
            regTextField.setEditable(editable);
            regTextField.setBackground(Color.WHITE);
        }

        saveButton.setEnabled(editable);
        cancelButton.setEnabled(editable);
    }

}
