package com.nikonhacker.gui.component.breakTrigger;


import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BreakTriggerListFrame extends DocumentFrame {

    private List<BreakTrigger> breakTriggers;
    private final JList triggerList;

    public BreakTriggerListFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, List<BreakTrigger> breakTriggers, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.breakTriggers = breakTriggers;

        JPanel mainPanel = new JPanel(new BorderLayout());

        DefaultListModel listModel = createListModel(breakTriggers);
        
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
                addTrigger();
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
        JButton okButton = new JButton("Close");
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
        //setLocationRelativeTo(null);
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
            ui.onBreaktriggersChange();
            if (!breakTriggers.isEmpty()) {
                triggerList.setSelectedIndex(Math.min(index, breakTriggers.size() - 1));
            }
        }
    }

    private void editTrigger(int index) {
        if (index != -1) {
            editTrigger(breakTriggers.get(index));
            triggerList.setSelectedIndex(index);
        }
    }

    private void addTrigger() {
        CPUState cpuStateFlags = new CPUState();
        cpuStateFlags.clear();
        BreakTrigger trigger = new BreakTrigger(findNewName(), new CPUState(), cpuStateFlags, new ArrayList<MemoryValueBreakCondition>());
        breakTriggers.add(trigger);
        ui.onBreaktriggersChange();
        triggerList.setSelectedIndex(breakTriggers.size() - 1);
        editTrigger(trigger);
    }

    private void toggleTrigger(int index) {
        if (index != -1) {
            BreakTrigger trigger = breakTriggers.get(index);
            trigger.setEnabled(!trigger.isEnabled());
            ui.onBreaktriggersChange();
            triggerList.setSelectedIndex(index);
        }
    }

    private void editTrigger(BreakTrigger trigger) {
        new BreakTriggerEditDialog(null, trigger, "Edit trigger conditions").setVisible(true);
        ui.onBreaktriggersChange();
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
        super.dispose();
    }

    public void updateBreaktriggers() {
        triggerList.setModel(createListModel(breakTriggers));
    }
}
