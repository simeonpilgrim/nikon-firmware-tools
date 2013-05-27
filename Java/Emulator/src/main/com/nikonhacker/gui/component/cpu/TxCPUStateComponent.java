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

    //private JComboBox powerModeList;
    private JLabel powerModeLabel;
    private JComboBox registerSetCombo;
    private JTextField pcTextField = new JTextField(8);
    private JCheckBox pcIsaMode16bCheckBox = new JCheckBox();
    private JTextField hiTextField = new JTextField(8);
    private JTextField loTextField = new JTextField(8);
    private JTextField statusTextField = new JTextField(32);
    private JTextField causeTextField = new JTextField(32);
    private JTextField epcTextField = new JTextField(8);
    private JTextField errorEpcTextField = new JTextField(8);
    private JTextField badVAddrTextField = new JTextField(8);
    private JTextField sscrTextField = new JTextField(8);

    private int displayedRegisterSet = 0;

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

        //powerModeList = new JComboBox(TxCPUState.PowerMode.values());
        powerModeLabel = new JLabel();
        registerSetCombo = new JComboBox(new Object[]{0,1,2,3,4,5,6,7});
        pcTextField.setHorizontalAlignment(JTextField.RIGHT); pcTextField.setFont(fixedFont);
        pcIsaMode16bCheckBox.setHorizontalAlignment(SwingConstants.CENTER);pcIsaMode16bCheckBox.setFont(fixedFont);
        hiTextField.setHorizontalAlignment(JTextField.RIGHT);hiTextField.setFont(fixedFont);
        loTextField.setHorizontalAlignment(JTextField.RIGHT);loTextField.setFont(fixedFont);
        statusTextField.setHorizontalAlignment(JTextField.RIGHT);statusTextField.setFont(fixedFont);
        causeTextField.setHorizontalAlignment(JTextField.RIGHT);causeTextField.setFont(fixedFont);
        epcTextField.setHorizontalAlignment(JTextField.RIGHT);epcTextField.setFont(fixedFont);
        errorEpcTextField.setHorizontalAlignment(JTextField.RIGHT);errorEpcTextField.setFont(fixedFont);
        badVAddrTextField.setHorizontalAlignment(JTextField.RIGHT);badVAddrTextField.setFont(fixedFont);
        sscrTextField.setHorizontalAlignment(JTextField.RIGHT);sscrTextField.setFont(fixedFont);

        displayedRegisterSet = cpuState.getSscrCSS();

        // General purpose registers
        for (int i = 0; i < regTextFields.length; i++) {
            regTextFields[i] = new JTextField(8);
            regTextFields[i].setHorizontalAlignment(JTextField.RIGHT);
            regTextFields[i].setFont(fixedFont);
        }

        registerSetCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayedRegisterSet = registerSetCombo.getSelectedIndex();
                boolean enabled = (displayedRegisterSet == cpuState.getSscrCSS());
                // Changing combo selection causes an update of all general purpose register fields
                // This changes just the values, not the background color, as it has no meaning
                for (int i = 1; i < regTextFields.length; i++) {
                    regTextFields[i].setEnabled(enabled);
                    regTextFields[i].setText(Format.asHex(cpuState.getShadowReg(displayedRegisterSet, i), 8));
                }
            }
        });

        JPanel registerPanel = new JPanel(new MigLayout());

        registerPanel.add(new JLabel("Power mode:"), "align left");
        //registerPanel.add(powerModeList, "wrap 15");
        registerPanel.add(powerModeLabel);
        registerPanel.add(new JLabel("Register set:"), "align left");
        registerPanel.add(registerSetCombo, "wrap 15");

        registerPanel.add(new JLabel("pc (addr)=0x"), "align right");
        registerPanel.add(pcTextField);
        registerPanel.add(pcIsaMode16bCheckBox, "align right");
        registerPanel.add(new JLabel("pc (ISA 16b)"), "wrap 15");

        for (int i = 0; i < 16; i++) {
            registerPanel.add(new JLabel(TxCPUState.registerLabels[i*2]+" = 0x"), "align right");
            registerPanel.add(regTextFields[i*2]);
            registerPanel.add(new JLabel(TxCPUState.registerLabels[i*2+1]+" = 0x"), "align right");
            registerPanel.add(regTextFields[i*2+1], "wrap" + (i==15?" 15":""));
        }

        regTextFields[0].setEnabled(false);

        registerPanel.add(new JLabel("hi / lo = 0x"), "align right");
        registerPanel.add(hiTextField);
        registerPanel.add(loTextField);
        registerPanel.add(new JLabel(), "wrap 15");

        // Status
        JLabel label1 = new JLabel("_CU_RFRMPB00N0Im___IM___KSUUREEI"); label1.setFont(fixedFont);
        JLabel label2 = new JLabel("3210PREXXV  M pl76543210XXXM0RXE"); label2.setFont(fixedFont);
        registerPanel.add(new JLabel()); registerPanel.add(label1, "span, wrap 0");
        registerPanel.add(new JLabel()); registerPanel.add(label2, "span, wrap 0");
        statusTextField.setFont(fixedFont);
        registerPanel.add(new JLabel("Status = 0b"), "align right"); registerPanel.add(statusTextField, "span, wrap");

        // Cause
        JLabel label3 = new JLabel("B0CE0000IW000000___IP___0_Exc_00"); label3.setFont(fixedFont);
        JLabel label4 = new JLabel("D       VP      76543210 Code_  "); label4.setFont(fixedFont);
        registerPanel.add(new JLabel()); registerPanel.add(label3, "span, wrap 0");
        registerPanel.add(new JLabel()); registerPanel.add(label4, "span, wrap 0");
        registerPanel.add(new JLabel("Cause = 0b"), "align right"); registerPanel.add(causeTextField, "span 3, wrap");

        registerPanel.add(new JLabel("EPC = 0x"), "align right"); registerPanel.add(epcTextField);
        registerPanel.add(new JLabel("ErrEPC=0x"), "align right"); registerPanel.add(errorEpcTextField, "wrap");
        registerPanel.add(new JLabel("BadVAddr=0x"), "align right"); registerPanel.add(badVAddrTextField);
        registerPanel.add(new JLabel("SSCR=0x"), "align right"); registerPanel.add(sscrTextField, "wrap");

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
            buttonPanel.add(new JLabel(), "wrap");

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
            powerModeLabel.setText("-");
            registerSetCombo.setEnabled(false);
            pcTextField.setText((cpuStateValidityFlags.pc == 0) ? "" : Format.asHex(cpuState.pc, 8));
            pcIsaMode16bCheckBox.setSelected(cpuStateValidityFlags.pc == 0 && ((TxCPUState) cpuState).is16bitIsaMode);
            hiTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.HI) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.HI), 8));
            loTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.LO) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.LO), 8));
            statusTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.Status) == 0)?"":maskValue(cpuState.getReg(TxCPUState.Status), cpuStateValidityFlags.getReg(TxCPUState.Status), 32));
            causeTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.Cause) == 0)?"":maskValue(cpuState.getReg(TxCPUState.Cause), cpuStateValidityFlags.getReg(TxCPUState.Cause), 32));
            epcTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.EPC) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.EPC), 8));
            errorEpcTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.ErrorEPC) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.ErrorEPC), 8));
            badVAddrTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.BadVAddr) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.BadVAddr), 8));
            sscrTextField.setText((cpuStateValidityFlags.getReg(TxCPUState.SSCR) == 0) ? "" : Format.asHex(cpuState.getReg(TxCPUState.SSCR), 8));

            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                regTextFields[i].setText((cpuStateValidityFlags.getReg(i) == 0)?"":Format.asHex(cpuState.getReg(i), 8));
            }
        }
        else {
            displayedRegisterSet = ((TxCPUState) cpuState).getSscrCSS();
            updateAndColorLabel(powerModeLabel, ((TxCPUState) cpuState).getPowerMode().name());
            updateAndColorCombo(registerSetCombo, displayedRegisterSet);
            updateAndColorTextField(pcTextField, Format.asHex(cpuState.pc, 8));
            updateAndColorCheckBox(pcIsaMode16bCheckBox, ((TxCPUState) cpuState).is16bitIsaMode);
            updateAndColorTextField(hiTextField, Format.asHex(cpuState.getReg(TxCPUState.HI), 8));
            updateAndColorTextField(loTextField, Format.asHex(cpuState.getReg(TxCPUState.LO), 8));
            updateAndColorTextField(statusTextField, Format.asBinary(cpuState.getReg(TxCPUState.Status), 32));
            updateAndColorTextField(causeTextField, Format.asBinary(cpuState.getReg(TxCPUState.Cause), 32));
            updateAndColorTextField(epcTextField, Format.asHex(cpuState.getReg(TxCPUState.EPC), 8));
            updateAndColorTextField(errorEpcTextField, Format.asHex(cpuState.getReg(TxCPUState.ErrorEPC), 8));
            updateAndColorTextField(badVAddrTextField, Format.asHex(cpuState.getReg(TxCPUState.BadVAddr), 8));
            updateAndColorTextField(sscrTextField, Format.asHex(cpuState.getReg(TxCPUState.SSCR), 8));

            // General purpose registers
            for (int i = 0; i < regTextFields.length; i++) {
                updateAndColorTextField(regTextFields[i], Format.asHex(((TxCPUState) cpuState).getShadowReg(displayedRegisterSet, i), 8));
            }
        }
    }


    private void validateAndSaveValues(TxCPUState cpuState) {
        try {
            int pc = Format.parseIntHexField(pcTextField);
            int hi = Format.parseIntHexField(hiTextField);
            int lo = Format.parseIntHexField(loTextField);
            int status = Format.parseIntBinaryField(statusTextField, false);
            int cause = Format.parseIntBinaryField(causeTextField, false);
            int epc = Format.parseIntHexField(epcTextField);
            int errorEpc = Format.parseIntHexField(errorEpcTextField);
            int badVAddr = Format.parseIntHexField(badVAddrTextField);
            int sscr = Format.parseIntHexField(sscrTextField);

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
                if (badVAddr != cpuState.getReg(TxCPUState.BadVAddr)) msg += changeString("BadVAddr", cpuState.getReg(TxCPUState.BadVAddr), errorEpc);
                if (sscr != cpuState.getReg(TxCPUState.SSCR)) msg += changeString("SSCR", cpuState.getReg(TxCPUState.SSCR), cause);
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
            cpuState.setReg(TxCPUState.BadVAddr, badVAddr);
            cpuState.setReg(TxCPUState.SSCR, sscr);

            for (int i = 0; i < regTextFields.length; i++) {
                cpuState.setReg(i, regs[i]);
            }
        }
        catch (NumberFormatException e) {
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
        }
        catch (NumberFormatException e) {
            System.err.println(e.getClass().getName() + " " + e.getMessage());
        }

    }


    public void setEditable(boolean editable) {
        pcTextField.setEditable(editable);
        pcIsaMode16bCheckBox.setEnabled(editable);
        hiTextField.setEditable(editable);
        loTextField.setEditable(editable);
        statusTextField.setEditable(editable);
        causeTextField.setEditable(editable);
        epcTextField.setEditable(editable);
        errorEpcTextField.setEditable(editable);
        for (JTextField regTextField : regTextFields) {
            regTextField.setEditable(editable);
        }
        badVAddrTextField.setEditable(editable);
        sscrTextField.setEditable(editable);

        saveButton.setEnabled(editable);
        cancelButton.setEnabled(editable);
    }
}
