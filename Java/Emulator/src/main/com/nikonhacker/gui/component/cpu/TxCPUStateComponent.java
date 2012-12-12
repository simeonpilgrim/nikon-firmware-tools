package com.nikonhacker.gui.component.cpu;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
import net.miginfocom.swing.MigLayout;
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

    private JTextField pcTextField = new JTextField(8);
    private JCheckBox pcIsaMode16bCheckBox = new JCheckBox();
    private JTextField hiTextField = new JTextField(8);
    private JTextField loTextField = new JTextField(8);
    private JTextField statusTextField = new JTextField(32);
    private JTextField causeTextField = new JTextField(32);
    private JTextField epcTextField = new JTextField(8);
    private JTextField errorEpcTextField = new JTextField(8);

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

        Font fixedFont = new Font(Font.MONOSPACED, Font.PLAIN, 11);

        for (int i = 0; i < regTextFields.length; i++) {
            regTextFields[i] = new JTextField(8);
            regTextFields[i].setHorizontalAlignment(JTextField.RIGHT);
            regTextFields[i].setFont(fixedFont);
        }
        pcTextField.setHorizontalAlignment(JTextField.RIGHT); pcTextField.setFont(fixedFont);
        pcIsaMode16bCheckBox.setHorizontalAlignment(SwingConstants.CENTER);pcIsaMode16bCheckBox.setFont(fixedFont);
        hiTextField.setHorizontalAlignment(JTextField.RIGHT);hiTextField.setFont(fixedFont);
        loTextField.setHorizontalAlignment(JTextField.RIGHT);loTextField.setFont(fixedFont);
        statusTextField.setHorizontalAlignment(JTextField.RIGHT);statusTextField.setFont(fixedFont);
        causeTextField.setHorizontalAlignment(JTextField.RIGHT);causeTextField.setFont(fixedFont);
        epcTextField.setHorizontalAlignment(JTextField.RIGHT);epcTextField.setFont(fixedFont);
        errorEpcTextField.setHorizontalAlignment(JTextField.RIGHT);errorEpcTextField.setFont(fixedFont);

        //setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setLayout(new MigLayout());

        add(new JLabel("pc (addr)=0x"), "align right");
        add(pcTextField);
        add(pcIsaMode16bCheckBox, "align right");
        add(new JLabel("pc (ISA 16b)"), "wrap 15");

        for (int i = 0; i < 16; i++) {
            add(new JLabel(TxCPUState.registerLabels[i*2]+" = 0x"), "align right");
            add(regTextFields[i*2]);
            add(new JLabel(TxCPUState.registerLabels[i*2+1]+" = 0x"), "align right");
            add(regTextFields[i*2+1], "wrap" + (i==15?" 15":""));
        }

        regTextFields[0].setEnabled(false);

        add(new JLabel("hi / lo = 0x"), "align right");
        add(hiTextField);
        add(loTextField);
        add(new JLabel(), "wrap 15");

        // Status
        JLabel label1 = new JLabel("_CU_RFRMPB00N0Im___IM___KSUUREEI"); label1.setFont(fixedFont);
        JLabel label2 = new JLabel("3210PREXXV  M pl76543210XXXM0RXE"); label2.setFont(fixedFont);
        add(new JLabel()); add(label1, "span, wrap 0");
        add(new JLabel()); add(label2, "span, wrap 0");
        statusTextField.setFont(fixedFont);
        add(new JLabel("Status = 0b"), "align right"); add(statusTextField, "span, wrap");

        // Cause
        JLabel label3 = new JLabel("B0CE0000IW000000___IP___0_Exc_00"); label3.setFont(fixedFont);
        JLabel label4 = new JLabel("D       VP      76543210 Code_  "); label4.setFont(fixedFont);
        add(new JLabel()); add(label3, "span, wrap 0");
        add(new JLabel()); add(label4, "span, wrap 0");
        add(new JLabel("Cause = 0b"), "align right"); add(causeTextField, "span 3, wrap");

        add(new JLabel("EPC = 0x"), "align right"); add(epcTextField);
        add(new JLabel("ErrEPC=0x"), "align right"); add(errorEpcTextField, "wrap");

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
            add(new JLabel(), "wrap");
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
            statusTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.Status) == 0) ? "" : Format.asBinary(cpuState.getReg(TxCPUState.Status), 32));
            causeTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.Cause) == 0) ? "" : Format.asBinary(cpuState.getReg(TxCPUState.Cause), 32));
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
            statusTextField.setText(Format.asBinary(cpuState.getReg(TxCPUState.Status), 32));
            causeTextField.setText(Format.asBinary(cpuState.getReg(TxCPUState.Cause), 32));
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
            int status = Format.parseIntBinaryField(statusTextField, false);
            int cause = Format.parseIntBinaryField(causeTextField, false);
            int epc = Format.parseIntHexField(epcTextField);
            int errorEpc = Format.parseIntHexField(errorEpcTextField);

            // General purpose registers
            int regs[] = new int[regTextFields.length];
            for (int i = 0; i < regTextFields.length; i++) {
                regs[i] = Format.parseIntHexField(regTextFields[i]);
            }

            // If we are here, everything has been parsed correctly. Commit to actual cpuState.

            // Log changes to real-time disassembly window
            if (instructionPrintWriter != null) {
                String msg = "";
                if (pc != cpuState.pc) msg += changeString("PC", cpuState.pc, pc);
                if (pcIsaMode16bCheckBox.isSelected() != cpuState.is16bitIsaMode) msg += changeString("PC ISA 16-bit", String.valueOf(cpuState.is16bitIsaMode) + " -> " + String.valueOf(pcIsaMode16bCheckBox.isSelected()));
                if (hi != cpuState.getReg(TxCPUState.HI)) msg += changeString("HI", cpuState.getReg(TxCPUState.HI), hi);
                if (lo != cpuState.getReg(TxCPUState.LO)) msg += changeString("LO", cpuState.getReg(TxCPUState.LO), lo);
                if (status != cpuState.getReg(TxCPUState.Status)) msg += changeString("Status", cpuState.getReg(TxCPUState.Status), status);
                if (cause != cpuState.getReg(TxCPUState.Cause)) msg += changeString("Cause", cpuState.getReg(TxCPUState.Cause), cause);
                if (epc != cpuState.getReg(TxCPUState.EPC)) msg += changeString("EPC", cpuState.getReg(TxCPUState.EPC), epc);
                if (errorEpc != cpuState.getReg(TxCPUState.ErrorEPC)) msg += changeString("ErrorEPC", cpuState.getReg(TxCPUState.ErrorEPC), errorEpc);
                for (int i = 0; i < regs.length; i++) {
                    if (regs[i] != cpuState.getReg(i)) msg += changeString(TxCPUState.registerLabels[i], cpuState.getReg(i), regs[i]);
                }
                if (msg.length() > 0) {
                    instructionPrintWriter.print("=====> Manual CPU state change:\n" + msg);
                }
            }

            cpuState.pc = pc;
            cpuState.is16bitIsaMode = pcIsaMode16bCheckBox.isSelected();
            cpuState.setReg(TxCPUState.HI, hi);
            cpuState.setReg(TxCPUState.LO, lo);
            cpuState.setReg(TxCPUState.Status, status);
            cpuState.setReg(TxCPUState.Cause, cause);
            cpuState.setReg(TxCPUState.EPC, epc);
            cpuState.setReg(TxCPUState.ErrorEPC, errorEpc);

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
            dumpBinFieldToRegister(statusTextField, TxCPUState.Status);
            dumpBinFieldToRegister(causeTextField, TxCPUState.Cause);
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
