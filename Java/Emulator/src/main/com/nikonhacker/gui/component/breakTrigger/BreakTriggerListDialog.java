package com.nikonhacker.gui.component.breakTrigger;


import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.trigger.BreakTrigger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class BreakTriggerListDialog extends JDialog {

    private List<BreakTrigger> breakTriggers;
    private final JList triggerList;
    private final DefaultListModel listModel;

    public BreakTriggerListDialog(Frame owner, final List<BreakTrigger> breakTriggers) {
        super(owner, "Setup breakpoints and triggers", true);
        this.breakTriggers = breakTriggers;

        JPanel mainPanel = new JPanel(new BorderLayout());

        listModel = createListModel(breakTriggers);
        
        triggerList = new JList(listModel);
        triggerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        triggerList.setLayoutOrientation(JList.VERTICAL);
        triggerList.setVisibleRowCount(10);
        JScrollPane listScroller = new JScrollPane(triggerList);
        listScroller.setPreferredSize(new Dimension(250, 300));
        mainPanel.add(listScroller, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(4, 1));

        JButton addButton = new JButton("Add");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addTrigger(triggerList.getSelectedIndex());
            }
        });
        rightPanel.add(addButton);

        JButton editButton = new JButton("Edit");
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editTrigger(triggerList.getSelectedIndex());
            }
        });
        rightPanel.add(editButton);

        JButton toggleButton = new JButton("Toggle");
        toggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        toggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toggleTrigger(triggerList.getSelectedIndex());
            }
        });
        rightPanel.add(toggleButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteTrigger(triggerList.getSelectedIndex());
            }
        });
        rightPanel.add(deleteButton);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveTriggers();
            }
        });
        bottomPanel.add(okButton);
//        JButton closeButton = new JButton("Close");
//        closeButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                dispose();
//            }
//        });
//        bottomPanel.add(closeButton);
//

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void toggleTrigger(int index) {
        BreakTrigger trigger = breakTriggers.get(index);
        trigger.setEnabled(!trigger.isEnabled());
        triggerList.setModel(createListModel(breakTriggers));
        triggerList.setSelectedIndex(index);
    }

    private DefaultListModel createListModel(List<BreakTrigger> breakTriggers) {
        DefaultListModel model = new DefaultListModel();
        for (BreakTrigger breakTrigger : breakTriggers) {
            model.addElement(breakTrigger);
        }
        return model;
    }


    private void deleteTrigger(int index) {
        if (index != -1) {
            breakTriggers.remove(index);
        }
        triggerList.setModel(createListModel(breakTriggers));
    }

    private void editTrigger(int index) {
        if (index != -1) {
            BreakTrigger trigger = breakTriggers.get(index);
            editTrigger(trigger);
            triggerList.setSelectedIndex(index);
        }
    }

    private void editTrigger(BreakTrigger trigger) {
        new BreakTriggerEditDialog(this, trigger, "Edit trigger conditions").setVisible(true);
        triggerList.setModel(createListModel(breakTriggers));
    }

    private void addTrigger(int index) {
        CPUState cpuStateFlags = new CPUState();
        cpuStateFlags.clear();
        BreakTrigger trigger = new BreakTrigger(findNewName(), new CPUState(), cpuStateFlags);
        breakTriggers.add(trigger);
        triggerList.setModel(createListModel(breakTriggers));
        triggerList.setSelectedIndex(index+1);
        editTrigger(trigger);
    }

    private String findNewName() {
        int i = 1;
        String name;
        do {
            name = "Trigger_" + i;
            i++;
        }
        while (isNameInUse(name));        
        return name;
    }

    private boolean isNameInUse(String name) {
        for (BreakTrigger breakTrigger : breakTriggers) {
            if (breakTrigger.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void saveTriggers() {
        dispose();
    }

}
