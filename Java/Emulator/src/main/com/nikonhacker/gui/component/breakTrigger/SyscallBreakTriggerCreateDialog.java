package com.nikonhacker.gui.component.breakTrigger;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.CPUState;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.disassembly.fr.Symbol;
import com.nikonhacker.disassembly.fr.Syscall;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.trigger.BreakTrigger;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class SyscallBreakTriggerCreateDialog extends JDialog implements ActionListener {
    private final BreakTrigger trigger;
    private final List<JLabel> labels = new ArrayList<JLabel>();
    private final List<JTextField> values = new ArrayList<JTextField>();
    private final JComboBox syscallCombo = new JComboBox();
    private List<Syscall> syscallList = null;


    public SyscallBreakTriggerCreateDialog(JDialog owner, BreakTrigger trigger, String title, DebuggableMemory memory) {
        super(owner, title, true);
        this.trigger = trigger;

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel editPanel = new JPanel(new GridLayout(17, 2));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(editPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        try {
            syscallList = new ArrayList<Syscall>();
            syscallList.addAll(Syscall.getMap(memory).values());
            Collections.sort(syscallList, new Comparator<Syscall>() {
                public int compare(Syscall o1, Syscall o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            topPanel.add(new JLabel("System call:"));

            Vector<String> syscallNames = new Vector<String>();
            for (Syscall syscall : syscallList) {
                syscallNames.add(syscall.getName());
            }

            syscallCombo.setModel(new DefaultComboBoxModel(syscallNames.toArray()));
            syscallCombo.setMaximumRowCount(15);
            syscallCombo.addActionListener(this);
            topPanel.add(syscallCombo);

            for (int i = 0; i < 17; i++) {
                JLabel label = new JLabel();
                labels.add(label);
                editPanel.add(label);
                JTextField field = new JTextField();
                values.add(field);
                editPanel.add(field);
            }

            refreshParams();

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    save();
                }
            });
            bottomPanel.add(okButton);

        } catch (ParsingException e) {
            topPanel.add(new JLabel("Error parsing syscall definition file"));
        }


        JButton okButton = new JButton("Cancel");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(okButton);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }


    public void actionPerformed(ActionEvent e) {
        refreshParams();
    }


    public void refreshParams() {
        int selectedIndex = syscallCombo.getSelectedIndex();
        Syscall syscall = syscallList.get(selectedIndex);
        List<Symbol.Parameter> parameterList = syscall.getParameterList();
        Map<Integer,String> inParameterMap = new HashMap<Integer, String>();
        if (parameterList != null) {
            for (Symbol.Parameter parameter : parameterList) {
                if (StringUtils.isNotBlank(parameter.getInVariable())) {
                    inParameterMap.put(parameter.getRegister(), parameter.getInVariable());
                }
            }
        }
        for (int i = 0; i < 16; i++) {
            if (i == 12) {
                labels.get(i).setText("R12 - Function Code");
                labels.get(i).setEnabled(false);
                values.get(i).setText(Format.asHex(syscall.getFunctionCode(), 8));
                values.get(i).setEnabled(false);
            }
            else {
                if (inParameterMap.containsKey(i)) {
                    labels.get(i).setText("R" + i + " - " + inParameterMap.get(i));
                    labels.get(i).setVisible(true);
                    values.get(i).setVisible(true);
                }
                else {
                    labels.get(i).setVisible(false);
                    values.get(i).setVisible(false);
                    values.get(i).setText("");
                }
            }
        }
        labels.get(16).setText("PC");
        labels.get(16).setEnabled(false);
        values.get(16).setText(Format.asHex(Syscall.getInt40address(), 8));
        values.get(16).setEnabled(false);
    }


    private void save() {
        CPUState cpuStateFlags = new CPUState();
        CPUState cpuStateValues = new CPUState();
        cpuStateFlags.clear();

        int selectedIndex = syscallCombo.getSelectedIndex();
        Syscall syscall = syscallList.get(selectedIndex);
        String triggerName = syscall.getName() +"(";

        List<Symbol.Parameter> parameterList = syscall.getParameterList();
        Map<Integer,String> inParameterMap = new HashMap<Integer, String>();
        if (parameterList != null) {
            for (Symbol.Parameter parameter : parameterList) {
                if (StringUtils.isNotBlank(parameter.getInVariable())) {
                    inParameterMap.put(parameter.getRegister(), parameter.getInVariable());
                }
            }
        }
        for (int i = 0; i < 16; i++) {
            if (i == 12) {
                cpuStateFlags.setReg(12, 0xFFFFFFFF);
                cpuStateValues.setReg(12, syscall.getFunctionCode());
            }
            else {
                if (inParameterMap.containsKey(i) && StringUtils.isNotBlank(values.get(i).getText())) {
                    cpuStateFlags.setReg(i, 0xFFFFFFFF);
                    int value = Format.parseIntHexField(values.get(i));
                    cpuStateValues.setReg(i, value);
                    if (!triggerName.endsWith("(")) {
                        triggerName += ", ";
                    }
                    triggerName += inParameterMap.get(i) + "=0x" + Format.asHex(value, 8);
                }
            }
        }
        cpuStateFlags.pc = 0xFFFFFFFF;
        cpuStateValues.pc = Syscall.getInt40address();

        trigger.setCpuStateFlags(cpuStateFlags);
        trigger.setCpuStateValues(cpuStateValues);
        trigger.setName(triggerName + ")");

        dispose();
    }


}
