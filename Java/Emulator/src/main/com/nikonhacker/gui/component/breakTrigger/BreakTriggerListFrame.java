package com.nikonhacker.gui.component.breakTrigger;


import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.PrintWriterArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BreakTriggerListFrame extends DocumentFrame {

    private static final int WINDOW_WIDTH = 250;
    private static final int WINDOW_HEIGHT = 300;

    int chip;
    private List<BreakTrigger> breakTriggers;
    private DebuggableMemory memory;
    private final EventList<BreakTrigger> triggerList;
    private final JTable triggerTable;
    private final Emulator emulator;

    public BreakTriggerListFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, Emulator emulator, List<BreakTrigger> breakTriggers, DebuggableMemory memory, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.chip = chip;
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
        rightPanel.setLayout(new GridLayout(4, 1));

        JButton addButton = new JButton("Add");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addTrigger();
            }
        });
        rightPanel.add(addButton);

        JButton addSyscallButton = new JButton("Add syscall");
        addSyscallButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addSyscallButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSyscallTrigger();
            }
        });
        rightPanel.add(addSyscallButton);

        JButton editButton = new JButton("Edit");
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editTrigger(triggerTable.getSelectedRow());
            }
        });
        rightPanel.add(editButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteTrigger(triggerTable.getSelectedRow());
            }
        });
        rightPanel.add(deleteButton);

        editPanel.add(rightPanel, BorderLayout.EAST);

        tabbedPane.addTab("Trigger list", null, editPanel);


        PrintWriterArea triggerLog = new PrintWriterArea(30, 40);

        tabbedPane.addTab("Trigger log", null, new JScrollPane(triggerLog));

        emulator.setBreakLogPrintWriter(triggerLog.getPrintWriter());


        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
        pack();
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

    private void deleteTrigger(int index) {
        if (index != -1) {
            breakTriggers.remove(index);
            ui.onBreaktriggersChange(chip);
            if (!breakTriggers.isEmpty()) {
                int newIndex = Math.min(index, breakTriggers.size() - 1);
                triggerTable.getSelectionModel().clearSelection();
                triggerTable.getSelectionModel().addSelectionInterval(newIndex, newIndex);
            }
        }
    }

    private void editTrigger(int index) {
        if (index != -1) {
            editTrigger(breakTriggers.get(index));

            triggerTable.getSelectionModel().clearSelection();
            triggerTable.getSelectionModel().addSelectionInterval(index, index);
        }
    }

    private void addTrigger() {
        FrCPUState cpuStateFlags = new FrCPUState();
        cpuStateFlags.clear();
        BreakTrigger trigger = new BreakTrigger(findNewName(), new FrCPUState(), cpuStateFlags, new ArrayList<MemoryValueBreakCondition>());
        breakTriggers.add(trigger);
        ui.onBreaktriggersChange(chip);

        editTrigger(trigger);

        int newIndex = breakTriggers.size() - 1;
        triggerTable.getSelectionModel().clearSelection();
        triggerTable.getSelectionModel().addSelectionInterval(newIndex, newIndex);
    }

    private void addSyscallTrigger() {
        FrCPUState cpuStateFlags = new FrCPUState();
        cpuStateFlags.clear();
        BreakTrigger trigger = new BreakTrigger(findNewName(), new FrCPUState(), cpuStateFlags, new ArrayList<MemoryValueBreakCondition>());
        breakTriggers.add(trigger);
        ui.onBreaktriggersChange(chip);

        new SyscallBreakTriggerCreateDialog(null, trigger, "Add syscall trigger", memory).setVisible(true);
        ui.onBreaktriggersChange(chip);

        int newIndex = breakTriggers.size() - 1;
        triggerTable.getSelectionModel().clearSelection();
        triggerTable.getSelectionModel().addSelectionInterval(newIndex, newIndex);
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
            return true;
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
