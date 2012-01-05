package com.nikonhacker.gui;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.dfr.Format;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CPUStateEditorFrame extends DocumentFrame {

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private Timer _timer;

    private CPUState cpuState;
    private boolean editable;

    public CPUStateEditorFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, CPUState cpuState, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.cpuState = cpuState;

        getContentPane().add(new CPUStateComponent());

        loadValues();
        
        // Start update timer
        _timer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //if (!editable)
                    loadValues();
            }
        });
        if (!editable) {
            _timer.start();
        }
    }

    public void dispose() {
        _timer.stop();
        _timer = null;
        super.dispose();
    }

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
    private JButton saveButton = new JButton("Save");
    private JButton cancelButton = new JButton("Cancel");

    JLabel r15Label0 = new JLabel(" ", JLabel.LEFT);
    JLabel r15Label1 = new JLabel(" ", JLabel.LEFT);

    private class CPUStateComponent extends JComponent {
        private CPUStateComponent() {
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

            add(new JPanel());
            add(new JPanel());
            add(new JLabel(CPUState.REG_LABEL[1]+" = 0x", JLabel.RIGHT));
            add(regTextFields[1]);

            add(new JPanel());
            add(new JLabel("43210 ", JLabel.RIGHT));
            add(new JLabel(CPUState.REG_LABEL[2]+" = 0x", JLabel.RIGHT));
            add(regTextFields[2]);

            add(new JLabel("ILM = 0b", JLabel.RIGHT));
            add(ilmTextField);
            add(new JLabel(CPUState.REG_LABEL[3]+" = 0x", JLabel.RIGHT));
            add(regTextFields[3]);

            add(new JPanel());
            add(new JPanel());
            add(new JLabel(CPUState.REG_LABEL[4]+" = 0x", JLabel.RIGHT));
            add(regTextFields[4]);

            add(new JPanel());
            add(new JLabel("D01T ", JLabel.RIGHT));
            add(new JLabel(CPUState.REG_LABEL[5]+" = 0x", JLabel.RIGHT));
            add(regTextFields[5]);

            add(new JLabel("SCR = 0b", JLabel.RIGHT));
            add(scrTextField);
            add(new JLabel(CPUState.REG_LABEL[6]+" = 0x", JLabel.RIGHT));
            add(regTextFields[6]);

            add(new JPanel());
            add(new JPanel());
            add(new JLabel(CPUState.REG_LABEL[7]+" = 0x", JLabel.RIGHT));
            add(regTextFields[7]);

            add(new JPanel());
            add(new JLabel("SINZVC ", JLabel.RIGHT));
            add(new JLabel(CPUState.REG_LABEL[8]+" = 0x", JLabel.RIGHT));
            add(regTextFields[8]);

            add(new JLabel("CCR = 0b", JLabel.RIGHT));
            add(ccrTextField);
            add(new JLabel(CPUState.REG_LABEL[9]+" = 0x", JLabel.RIGHT));
            add(regTextFields[9]);

            add(new JPanel());
            add(new JPanel());
            add(new JLabel(CPUState.REG_LABEL[10]+" = 0x", JLabel.RIGHT));
            add(regTextFields[10]);

            add(new JLabel("TBR = 0x", JLabel.RIGHT));
            add(tbrTextField);
            add(new JLabel(CPUState.REG_LABEL[11]+" = 0x", JLabel.RIGHT));
            add(regTextFields[11]);

            add(new JPanel());
            add(new JPanel());
            add(new JLabel(CPUState.REG_LABEL[12]+" = 0x", JLabel.RIGHT));
            add(regTextFields[12]);

            add(new JLabel("RP = 0x", JLabel.RIGHT));
            add(rpTextField);
            add(new JLabel(CPUState.REG_LABEL[13]+" = 0x", JLabel.RIGHT));
            add(regTextFields[13]);

            add(new JPanel());
            add(new JPanel());
            add(new JLabel(CPUState.REG_LABEL[14]+" = 0x", JLabel.RIGHT));
            add(regTextFields[14]);

            add(new JLabel("SSP = 0x", JLabel.RIGHT));
            add(sspTextField);
            add(r15Label0);
            add(new JLabel()); // no idea why we get refresh problems here with a new JPanel()

            add(new JLabel("USP = 0x", JLabel.RIGHT));
            add(uspTextField);
            add(r15Label1);
            add(new JLabel()); // no idea why we get refresh problems here with a new JPanel()

            add(new JPanel());
            add(new JPanel());
            add(new JPanel());
            add(new JPanel());

            add(new JLabel("MDH/MDL = 0x", JLabel.RIGHT));
            add(mdhTextField);
            add(mdlTextField);
            add(new JPanel());

            add(new JPanel());
            Font font = saveButton.getFont();
            font = new Font(font.getName(), font.getStyle(), 10);
            saveButton.setFont(font);
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveValues();
                }
            });
            cancelButton.setFont(font);
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadValues();
                }
            });

            add(saveButton);
            add(cancelButton);
            add(new JPanel());
        }
    }

    private void saveValues() {
        try {
            int pc = Format.parseHexField(pcTextField);
            int ilm = Format.parseBinaryField(ilmTextField);
            int scr = Format.parseBinaryField(scrTextField);
            int ccr = Format.parseBinaryField(ccrTextField);
            int tbr = Format.parseHexField(tbrTextField);
            int rp = Format.parseHexField(rpTextField);
            int ssp = Format.parseHexField(sspTextField);
            int usp = Format.parseHexField(uspTextField);
            int mdh = Format.parseHexField(mdhTextField);
            int mdl = Format.parseHexField(mdlTextField);

            // General purpose registers
            int regs[] = new int[regTextFields.length];
            for (int i = 0; i < regTextFields.length; i++) {
                regs[i] = Format.parseHexField(regTextFields[i]);
            }

            // If we are here, everything has been parsed correctly. Commit to actual cpuState. 

            cpuState.pc = pc;
            cpuState.setILM(ilm);
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
    
    // This method is called whenever the contents needs to be painted
    public void loadValues() {
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
    
    public void setEditable(boolean editable) {
        this.editable = editable;
        
        if (editable) {
            if (_timer.isRunning()) {
                _timer.stop();
            }
        }
        else {
            if (!_timer.isRunning()) {
                _timer.start();
            }
        }

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
