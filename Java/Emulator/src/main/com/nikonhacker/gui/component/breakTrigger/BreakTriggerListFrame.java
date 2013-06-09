package com.nikonhacker.gui.component.breakTrigger;


import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.PrintWriterArea;
import com.nikonhacker.gui.swing.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BreakTriggerListFrame extends DocumentFrame {

    private static final int WINDOW_WIDTH = 250;
    private static final int WINDOW_HEIGHT = 300;

    private List<BreakTrigger> breakTriggers;
    private Memory memory;
    private final EventList<BreakTrigger> triggerList;
    private final JTable triggerTable;
    private final Emulator emulator;
    private boolean editable;
    private final JButton addButton;
    private final JButton editButton;
    private final JButton deleteButton;
    private final JButton moveUpButton;
    private final JButton moveDownButton;
    private final JButton addSyscallButton;

    public BreakTriggerListFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, Emulator emulator, List<BreakTrigger> breakTriggers, Memory memory) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.emulator = emulator;
        this.breakTriggers = breakTriggers;
        this.memory = memory;

        JPanel mainPanel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel editPanel = new JPanel(new BorderLayout());

        triggerList = GlazedLists.threadSafeList(new BasicEventList<BreakTrigger>());
        updateBreaktriggers();

        EventTableModel<BreakTrigger> etm = new EventTableModel<BreakTrigger>(triggerList, new BreakTriggerTableFormat());
        triggerTable = new JTable(etm);
        triggerTable.getColumnModel().getColumn(0).setPreferredWidth(1000);
        triggerTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        triggerTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        triggerTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        triggerTable.getColumnModel().getColumn(4).setPreferredWidth(200);

        JScrollPane listScroller = new JScrollPane(triggerTable);
        listScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        editPanel.add(listScroller, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(0, 1));

        addButton = new JButton("Add");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addTrigger();
            }
        });
        rightPanel.add(addButton);

        if (chip == Constants.CHIP_FR) {
            addSyscallButton = new JButton("Add syscall");
            addSyscallButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            addSyscallButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addSyscallTrigger();
                }
            });
            rightPanel.add(addSyscallButton);
        }
        else {
            addSyscallButton = null;
        }

        editButton = new JButton("Edit");
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editTrigger(triggerTable.getSelectedRow());
            }
        });
        rightPanel.add(editButton);

        deleteButton = new JButton("Delete");
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteTriggers(triggerTable.getSelectedRows());
            }
        });
        rightPanel.add(deleteButton);

        moveUpButton = new JButton("Move Up");
        moveUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        moveUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] selections = triggerTable.getSelectedRows();
                if (moveTrigger(selections, -1)) {
                    triggerTable.clearSelection();
                    triggerTable.addRowSelectionInterval(selections[0] - 1, selections[selections.length - 1] - 1);
                }
            }
        });
        rightPanel.add(moveUpButton);

        moveDownButton = new JButton("Move Down");
        moveDownButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        moveDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] selections = triggerTable.getSelectedRows();
                if (moveTrigger(selections, 1)) {
                    triggerTable.clearSelection();
                    triggerTable.addRowSelectionInterval(selections[0] + 1, selections[selections.length - 1] + 1);
                }
            }
        });
        rightPanel.add(moveDownButton);

        editPanel.add(rightPanel, BorderLayout.EAST);

        tabbedPane.addTab("Trigger list", null, editPanel);


        PrintWriterArea triggerLog = new PrintWriterArea(30, 40);
        triggerLog.setAutoScroll(true);

        tabbedPane.addTab("Trigger log", null, new JScrollPane(triggerLog));

        emulator.setBreakLogPrintWriter(triggerLog.getPrintWriter());


        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
        pack();
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        addButton.setEnabled(editable);
        editButton.setEnabled(editable);
        deleteButton.setEnabled(editable);
        if (addSyscallButton != null) addSyscallButton.setEnabled(editable);
    }

    @Override
    public void dispose() {
        emulator.setBreakLogPrintWriter(null);
        super.dispose();
    }

    public void updateBreaktriggers() {
        triggerList.clear();
        for (BreakTrigger breakTrigger : breakTriggers) {
            triggerList.add(breakTrigger);
        }
    }

    private void deleteTriggers(int[] indices) {
        if (indices.length > 0) {
            String message;
            if (indices.length ==1) {
                message = "Are you sure you want to delete the trigger '" + triggerList.get(indices[0]).getName() + "' ?";
            }
            else {
                message = "Are you sure you want to delete " + indices.length + " triggers ?";
            }
            if (JOptionPane.showConfirmDialog(this, message, "Delete ?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                List<Integer> indexList = new ArrayList<>();
                for (int index : indices) {
                    indexList.add(index);
                }
                // Make sure indices are sorted (the getSelectedRows() does not guarantee that)
                Collections.sort(indexList);
                // Reverse order because items at the end of the list will be shifted when we remove items
                Collections.reverse(indexList);
                // Remove, starting at the maximum index
                for (Integer index : indexList) {
                    breakTriggers.remove(index.intValue());
                }
                ui.onBreaktriggersChange(chip);
                if (!breakTriggers.isEmpty()) {
                    setSelectedIndex(Math.min(indices[0], breakTriggers.size() - 1));
                }
            }
        }
    }

    private void editTrigger(int index) {
        if (index != -1) {
            editTrigger(breakTriggers.get(index));
            setSelectedIndex(index);
        }
    }

    private void addTrigger() {
        CPUState cpuStateFlags;
        CPUState cpuStateValues;
        if (chip == Constants.CHIP_FR) {
            cpuStateFlags = new FrCPUState();
            cpuStateValues = new FrCPUState();
        }
        else {
            cpuStateFlags = new TxCPUState();
            cpuStateValues = new TxCPUState();
        }
        cpuStateFlags.clear();
        cpuStateFlags.pc = 0;
        BreakTrigger trigger = new BreakTrigger(findNewName(), cpuStateValues, cpuStateFlags, new ArrayList<MemoryValueBreakCondition>());
        breakTriggers.add(trigger);
        ui.onBreaktriggersChange(chip);

        editTrigger(trigger);

        setSelectedIndex(breakTriggers.size() - 1);
    }

    private boolean moveTrigger(int[] selectedRows, int direction) {
        boolean hasMoved = false;
        if (selectedRows.length > 0) {
            // if there are holes in the selection, then only select the first one
            if (selectedRows[selectedRows.length - 1] - selectedRows[0] != selectedRows.length - 1) {
                selectedRows = new int[]{selectedRows[0]};
            }
            if (direction < 0 && selectedRows[0] > 0) {
                // Move up
                Collections.rotate(breakTriggers.subList(selectedRows[0] - 1, selectedRows[0] + selectedRows.length), - 1);
                hasMoved = true;
            }
            if (direction > 0 && selectedRows[selectedRows.length - 1] < breakTriggers.size() - 1) {
                // Move down
                Collections.rotate(breakTriggers.subList(selectedRows[0], selectedRows[0] + selectedRows.length + 1), 1);
                hasMoved = true;
            }
            updateBreaktriggers();
        }
        return hasMoved;
    }

    private void setSelectedIndex(int index) {
        triggerTable.getSelectionModel().clearSelection();
        triggerTable.getSelectionModel().addSelectionInterval(index, index);
    }

    private void addSyscallTrigger() {
        FrCPUState cpuStateFlags = new FrCPUState();
        cpuStateFlags.clear();
        BreakTrigger trigger = new BreakTrigger(findNewName(), new FrCPUState(), cpuStateFlags, new ArrayList<MemoryValueBreakCondition>());
        breakTriggers.add(trigger);
        ui.onBreaktriggersChange(chip);

        new SyscallBreakTriggerCreateDialog(null, trigger, "Add syscall trigger", memory).setVisible(true);
        ui.onBreaktriggersChange(chip);

        setSelectedIndex(breakTriggers.size() - 1);
    }

    private void editTrigger(BreakTrigger trigger) {
        new BreakTriggerEditDialog(null, chip, trigger, "Edit trigger conditions").setVisible(true);
        ui.onBreaktriggersChange(chip);
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

    private class BreakTriggerTableFormat implements AdvancedTableFormat<BreakTrigger>, WritableTableFormat<BreakTrigger> {

        public boolean isEditable(BreakTrigger baseObject, int column) {
            return editable;
        }

        public int getColumnCount() {
            return 5;
        }


        public BreakTrigger setColumnValue(BreakTrigger baseObject, Object editedValue, int column) {
            switch (column) {
                case 0:
                    baseObject.setName((String) editedValue);
                    return baseObject;
                case 1:
                    baseObject.setMustBreak((Boolean) editedValue);
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
                case 2:
                    baseObject.setMustBeLogged((Boolean) editedValue);
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
                case 3:
                    if ("".equals(editedValue)) {
                        baseObject.setInterruptToRequest(null);
                    }
                    else {
                        try {
                            int interrupt = Format.parseUnsigned("0x" + (String)editedValue);
                            baseObject.setInterruptToRequest(interrupt);
                        } catch (ParsingException e) {
                            // ignore the change
                        }
                    }
                    return baseObject;
                case 4:
                    if ("".equals(editedValue)) {
                        baseObject.setPcToSet(null);
                    }
                    else {
                        try {
                            int pcToSet = Format.parseUnsigned("0x" + (String)editedValue);
                            baseObject.setPcToSet(pcToSet);
                        } catch (ParsingException e) {
                            // ignore the change
                        }
                    }
                    return baseObject;
            }
            return baseObject;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Break";
                case 2:
                    return "Log";
                case 3:
                    return "Interrupt";
                case 4:
                    return "JMP to";
            }
            return null;
        }

        public Object getColumnValue(BreakTrigger baseObject, int column) {
            switch (column) {
                case 0:
                    return baseObject.getName();
                case 1:
                    return baseObject.mustBreak();
                case 2:
                    return baseObject.mustBeLogged();
                case 3:
                    return baseObject.getInterruptToRequest()==null?"":Format.asHex(baseObject.getInterruptToRequest(), 2);
                case 4:
                    return baseObject.getPcToSet()==null?"":Format.asHex(baseObject.getPcToSet(), 8);
            }
            return null;
        }

        public Class getColumnClass(int column) {
            switch (column) {
                case 0:
                    return String.class;
                case 1:
                    return Boolean.class;
                case 2:
                    return Boolean.class;
                case 3:
                    return String.class;
                case 4:
                    return String.class;
            }
            return null;
        }

        public Comparator getColumnComparator(int column) {
            return null;
        }
    }
}
