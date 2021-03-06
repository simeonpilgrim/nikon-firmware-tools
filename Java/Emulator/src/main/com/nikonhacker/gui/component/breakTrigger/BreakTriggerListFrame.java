package com.nikonhacker.gui.component.breakTrigger;


import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.Prefs;
import com.nikonhacker.XStreamUtils;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;
import com.nikonhacker.gui.swing.PrintWriterArea;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BreakTriggerListFrame extends DocumentFrame {

    private static final int WINDOW_WIDTH  = 250;
    private static final int WINDOW_HEIGHT = 300;
    private static final String TRIGGERS_EXTENSION = ".xtriggers";

    private       List<BreakTrigger>      breakTriggers;
    private       Memory                  memory;
    private final EventList<BreakTrigger> triggerList;
    private final JTable                  triggerTable;
    private final Emulator                emulator;
    private       boolean                 editable;
    private final JButton                 addButton;
    private final JButton                 editButton;
    private final JButton                 deleteButton;
    private final JButton                 addSyscallButton;

    public BreakTriggerListFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui, Emulator emulator, final List<BreakTrigger> breakTriggers, Memory memory) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.emulator = emulator;
        this.breakTriggers = breakTriggers;
        this.memory = memory;

        JPanel mainPanel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel editPanel = new JPanel(new BorderLayout());

        triggerList = GlazedLists.threadSafeList(new BasicEventList<BreakTrigger>());
        updateBreaktriggers();

        triggerTable = new JTable(new EventTableModel<BreakTrigger>(triggerList, new BreakTriggerTableFormat())){
            // add coloring based on trigger enabled or not
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                boolean triggerEnabled = breakTriggers.get(row).isEnabled();
                c.setForeground(triggerEnabled?Color.BLACK:Color.GRAY);
                if (!isRowSelected(row)) {
                    c.setBackground(triggerEnabled?Color.WHITE:Color.LIGHT_GRAY);
                }
                return c;
            }
        };
        triggerTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        triggerTable.getColumnModel().getColumn(1).setPreferredWidth(500);
        triggerTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        triggerTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        triggerTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        triggerTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        triggerTable.getColumnModel().getColumn(6).setPreferredWidth(200);
        triggerTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        triggerTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        triggerTable.setToolTipText("Right mouse button jumps to source");
        triggerTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // right mouse button
                if (e.getButton() == MouseEvent.BUTTON3) {
                    final int row = triggerTable.rowAtPoint(e.getPoint());
                    if (row!=-1) {
                        final BreakTrigger trigger = breakTriggers.get(row);
                        if (trigger.getCpuStateFlags().pc==1)
                            ui.jumpToSource(chip, trigger.getCpuStateValues().pc);
                    }
                }
            }
        });

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

        JButton moveUpButton = new JButton("Move Up");
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

        JButton moveDownButton = new JButton("Move Down");
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

        JButton exportButton = new JButton("Export selected");
        exportButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] selections = triggerTable.getSelectedRows();
                if (selections.length == 0) {
                    JOptionPane.showMessageDialog(BreakTriggerListFrame.this, "Please make a selection first (SHIFT and CTRL-click are supported)", "Nothing to export", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    final JFileChooser fc = new JFileChooser();

                    fc.setDialogTitle("Select destination file");
                    fc.setCurrentDirectory(new java.io.File("."));

                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    /* TODO add chip name to triggers extension */
                    fc.setFileFilter(Format.createFilter(TRIGGERS_EXTENSION, "Emulator break triggers (*" + TRIGGERS_EXTENSION + ")"));

                    if (fc.showOpenDialog(BreakTriggerListFrame.this) == JFileChooser.APPROVE_OPTION) {
                        File destinationFile = fc.getSelectedFile();
                        if (!(destinationFile.getAbsolutePath().toLowerCase().endsWith(TRIGGERS_EXTENSION))) {
                            destinationFile = new File(destinationFile.getAbsolutePath() + TRIGGERS_EXTENSION);
                        }
                        if (!destinationFile.exists() || JOptionPane.showConfirmDialog(BreakTriggerListFrame.this, "Are you sure you want to overwrite " + destinationFile.getName(), "Confirm overwrite", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            List<BreakTrigger> selectedTriggers = new ArrayList<BreakTrigger>();
                            for (int selection : selections) {
                                selectedTriggers.add(triggerList.get(selection));
                            }
                            try {
                                XStreamUtils.save(selectedTriggers, new FileOutputStream(destinationFile), Prefs.getPrefsXStream());
                                JOptionPane.showMessageDialog(BreakTriggerListFrame.this, "Export complete", "Done", JOptionPane.INFORMATION_MESSAGE);
                                triggerTable.clearSelection();
                            } catch (FileNotFoundException e1) {
                                JOptionPane.showMessageDialog(BreakTriggerListFrame.this, "Could not export to file '" + destinationFile.getAbsolutePath() + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });
        rightPanel.add(exportButton);

        JButton importButton = new JButton("Add from file");
        importButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();

                fc.setDialogTitle("Select destination file");
                fc.setCurrentDirectory(new java.io.File("."));

                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(Format.createFilter(TRIGGERS_EXTENSION, "Emulator break triggers (*" + TRIGGERS_EXTENSION + ")"));

                if (fc.showOpenDialog(BreakTriggerListFrame.this) == JFileChooser.APPROVE_OPTION) {
                    File destinationFile = fc.getSelectedFile();
                    if (!destinationFile.exists()) {
                        JOptionPane.showMessageDialog(BreakTriggerListFrame.this, "Could not find file '" + destinationFile.getAbsolutePath() + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        try {
                            List<BreakTrigger> importedTrigger = (List<BreakTrigger>) XStreamUtils.load(new FileInputStream(destinationFile), Prefs.getPrefsXStream());
                            breakTriggers.addAll(importedTrigger);
                            updateBreaktriggers();
                            triggerTable.setRowSelectionInterval(breakTriggers.size() - importedTrigger.size(), breakTriggers.size() - 1);
                            JOptionPane.showMessageDialog(BreakTriggerListFrame.this, "Import complete", "Done", JOptionPane.INFORMATION_MESSAGE);
                        } catch (FileNotFoundException e1) {
                            JOptionPane.showMessageDialog(BreakTriggerListFrame.this, "Could not import to file '" + destinationFile.getAbsolutePath() + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        rightPanel.add(importButton);

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
        // Create new
        BreakTrigger trigger = new BreakTrigger(chip, findNewName());
        breakTriggers.add(trigger);
        ui.onBreaktriggersChange(chip);

        // Edit it
        editTrigger(trigger);

        // Select it
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
        // Create blank
        BreakTrigger trigger = new BreakTrigger(chip, findNewName());
        breakTriggers.add(trigger);
        ui.onBreaktriggersChange(chip);

        // Open edit dialog
        new SyscallBreakTriggerCreateDialog(null, trigger, "Add syscall trigger", memory).setVisible(true);
        ui.onBreaktriggersChange(chip);

        // Select
        setSelectedIndex(breakTriggers.size() - 1);
    }

    private void editTrigger(BreakTrigger trigger) {
        new BreakTriggerEditDialog(null, chip, trigger, "Edit trigger").setVisible(true);
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
            if (name.equals(breakTrigger.getName())) {
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
            return 9;
        }

        public BreakTrigger setColumnValue(BreakTrigger baseObject, Object editedValue, int column) {
            switch (column) {
                case 0:
                    baseObject.setEnabled((Boolean) editedValue);
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
                case 1:
                    baseObject.setName((String) editedValue);
                    return baseObject;
                case 2:
                    baseObject.setMustBreak((Boolean) editedValue);
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
                case 3:
                    baseObject.setMustBeLogged((Boolean) editedValue);
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
                case 4:
                    if ("".equals(editedValue)) {
                        baseObject.setInterruptToRequest(null);
                    }
                    else {
                        try {
                            int interrupt = Format.parseUnsigned("0x" + editedValue);
                            baseObject.setInterruptToRequest(interrupt);
                        } catch (ParsingException e) {
                            // ignore the change
                        }
                    }
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
                case 5:
                    if ("".equals(editedValue)) {
                        baseObject.setInterruptToWithdraw(null);
                    }
                    else {
                        try {
                            int interrupt = Format.parseUnsigned("0x" + editedValue);
                            baseObject.setInterruptToWithdraw(interrupt);
                        } catch (ParsingException e) {
                            // ignore the change
                        }
                    }
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
                case 6:
                    if ("".equals(editedValue)) {
                        baseObject.setPcToSet(null);
                    }
                    else {
                        try {
                            int pcToSet = Format.parseUnsigned("0x" + editedValue);
                            baseObject.setPcToSet(pcToSet);
                        } catch (ParsingException e) {
                            // ignore the change
                        }
                    }
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
                case 7:
                    baseObject.setMustStartLogging((Boolean) editedValue);
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
                case 8:
                    baseObject.setMustStopLogging((Boolean) editedValue);
                    ui.onBreaktriggersChange(chip);
                    return baseObject;
            }
            return baseObject;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Enabled";
                case 1:
                    return "Name";
                case 2:
                    return "Break";
                case 3:
                    return "Log";
                case 4:
                    return "Interrupt On";
                case 5:
                    return "Interrupt Off";
                case 6:
                    return "JMP to";
                case 7:
                    return "Start Log";
                case 8:
                    return "Stop Log";
            }
            return null;
        }

        public Object getColumnValue(BreakTrigger baseObject, int column) {
            switch (column) {
                case 0:
                    return baseObject.isEnabled();
                case 1:
                    return baseObject.getName();
                case 2:
                    return baseObject.mustBreak();
                case 3:
                    return baseObject.mustBeLogged();
                case 4:
                    return baseObject.getInterruptToRequest()==null?"":Format.asHex(baseObject.getInterruptToRequest(), 2);
                case 5:
                    return baseObject.getInterruptToWithdraw()==null?"":Format.asHex(baseObject.getInterruptToWithdraw(), 2);
                case 6:
                    return baseObject.getPcToSet()==null?"":Format.asHex(baseObject.getPcToSet(), 8);
                case 7:
                    return baseObject.getMustStartLogging();
                case 8:
                    return baseObject.getMustStopLogging();
            }
            return null;
        }

        public Class getColumnClass(int column) {
            switch (column) {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
                case 2:
                    return Boolean.class;
                case 3:
                    return Boolean.class;
                case 4:
                    return String.class;
                case 5:
                    return String.class;
                case 6:
                    return String.class;
                case 7:
                    return Boolean.class;
                case 8:
                    return Boolean.class;
            }
            return null;
        }

        public Comparator getColumnComparator(int column) {
            return null;
        }
    }
}
