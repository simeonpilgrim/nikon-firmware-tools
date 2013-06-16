package com.nikonhacker.gui.component.memoryHexEditor;

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
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.saveLoadMemory.SaveLoadMemoryDialog;
import com.nikonhacker.gui.swing.DocumentFrame;
import org.fife.ui.hex.event.HexEditorEvent;
import org.fife.ui.hex.event.HexEditorListener;
import org.fife.ui.hex.swing.HexEditor;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MemoryHexEditorFrame extends DocumentFrame implements ActionListener, HexEditorListener {
    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private DebuggableMemory memory;
    private CPUState cpuState;

    private Timer refreshTimer;
    private JTextField addressField;
    private HexEditor hexEditor;
    private JButton leftButton;
    private JButton rightButton;
    private JButton fpButton;
    private JButton spButton;
    private byte[] currentPage;
    private int baseAddress;
    private JComboBox registerCombo;
    private JButton saveLoadButton;

    private final JTable watchTable;
    private final EventList<MemoryWatch> watchList;
    private final JTabbedPane tabbedPane;
    private boolean editable;

    public MemoryHexEditorFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, DebuggableMemory memory, CPUState cpuState, int baseAddress, boolean editable) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.memory = memory;
        this.cpuState = cpuState;


        JPanel mainPanel = new JPanel(new BorderLayout());

        tabbedPane = new JTabbedPane();

        tabbedPane.add("General", createGeneralEditorPanel(baseAddress, editable));
        loadPage(baseAddress);

        JPanel editPanel = new JPanel(new BorderLayout());

        watchList = GlazedLists.threadSafeList(new BasicEventList<MemoryWatch>());
        refreshMemoryWatches();

        EventTableModel<MemoryWatch> etm = new EventTableModel<MemoryWatch>(watchList, new MemoryWatchTableFormat());
        watchTable = new JTable(etm);
        watchTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        watchTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        watchTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        JScrollPane listScroller = new JScrollPane(watchTable);
        //listScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        editPanel.add(listScroller, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(0, 1));

        JButton addButton = new JButton("Add");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addWatch();
            }
        });
        rightPanel.add(addButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteWatch(watchTable.getSelectedRow());
            }
        });
        rightPanel.add(deleteButton);

        JButton createTriggerButton = new JButton("Convert to trigger");
        createTriggerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createTriggerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                convertWatchToTrigger(watchTable.getSelectedRow());
            }
        });
        rightPanel.add(createTriggerButton);

        editPanel.add(rightPanel, BorderLayout.EAST);

        tabbedPane.addTab("Watches", editPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Start update timer
        refreshTimer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        refreshTimer.start();
    }

    private void refreshMemoryWatcheValues() {
        for (int i = 0; i < watchList.size(); i++) {
            watchList.set(i, watchList.get(i));
        }
    }

    private void refreshMemoryWatches() {
        watchList.clear();
        for (MemoryWatch memoryWatch : ui.getPrefs().getWatches(chip)) {
            watchList.add(memoryWatch);
        }
    }

    private void addWatch() {
        MemoryWatch memoryWatch = new MemoryWatch(findNewName(), 0);
        List<MemoryWatch> watches = ui.getPrefs().getWatches(chip);
        watches.add(memoryWatch);
        refreshMemoryWatches();
        setSelectedWatchIndex(watches.size() - 1);
    }

    private void deleteWatch(int index) {
        if (index != -1) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the watch '" + watchList.get(index).getName() + "' ?", "Delete ?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                List<MemoryWatch> watches = ui.getPrefs().getWatches(chip);
                watches.remove(index);
                refreshMemoryWatches();
                if (!watches.isEmpty()) {
                    setSelectedWatchIndex(Math.min(index, watches.size() - 1));
                }
            }
        }
    }

    private void convertWatchToTrigger(int index) {
        if (index != -1) {
            MemoryWatch watch = ui.getPrefs().getWatches(chip).get(index);
            List<MemoryValueBreakCondition> memoryValueBreakConditions = new ArrayList<MemoryValueBreakCondition>();
            int currentValue = memory.load32(watch.getAddress());
            String triggerName = watch.getName() + " changes";

            CPUState values = (chip==Constants.CHIP_FR)?new FrCPUState():new TxCPUState();
            CPUState flags = (chip==Constants.CHIP_FR)?new FrCPUState():new TxCPUState();
            flags.setPc(0);
            if (chip==Constants.CHIP_FR) {
                ((FrCPUState)flags).setILM(0, false);
                flags.setReg(FrCPUState.TBR, 0);
            }
            else {
                flags.setReg(TxCPUState.Status, 0);
            }
            BreakTrigger trigger = new BreakTrigger(triggerName, values, flags, memoryValueBreakConditions);

            MemoryValueBreakCondition condition = new MemoryValueBreakCondition(trigger);
            condition.setAddress(watch.getAddress());
            condition.setMask(0xFFFFFFFF);
            condition.setValue(currentValue);
            memoryValueBreakConditions.add(condition);
            ui.getPrefs().getTriggers(chip).add(trigger);
            ui.onBreaktriggersChange(chip);
            JOptionPane.showMessageDialog(this, "Added trigger '" + triggerName + "'", "Trigger created", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setSelectedWatchIndex(int index) {
        watchTable.getSelectionModel().clearSelection();
        watchTable.getSelectionModel().setSelectionInterval(index, index);
    }

    private String findNewName() {
        int i = 1;
        String name;
        do {
            name = "Watch_" + i;
            i++;
        }
        while (isNameInUse(name));
        return name;
    }

    private boolean isNameInUse(String name) {
        for (MemoryWatch memoryWatch : watchList) {
            if (memoryWatch.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }



    private void refreshData() {
        if (ui.isEmulatorPlaying(chip)) {
            refreshMemoryPage();
            refreshMemoryWatcheValues();
        }
    }

    private void refreshMemoryPage() {
        if (currentPage != null) {
            try {
                hexEditor.open(new ByteArrayInputStream(currentPage));
                hexEditor.setColorMap(createColorMap());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private JPanel createGeneralEditorPanel(int baseAddress, boolean editable) {
        JPanel editorPanel = new JPanel(new BorderLayout());

        JPanel selectionPanel = new JPanel();

        fpButton = new JButton("Go to FP");
        selectionPanel.add(fpButton);
        fpButton.addActionListener(this);

//        selectionPanel.add(Box.createHorizontalGlue());

        spButton = new JButton("Go to SP");
        selectionPanel.add(spButton);
        spButton.addActionListener(this);

//        selectionPanel.add(Box.createHorizontalGlue());

        selectionPanel.add(new JLabel("Go to reg"));
        Vector<String> labels = new Vector<String>();
        labels.add("--");
        if (chip == Constants.CHIP_FR) {
            labels.addAll(Arrays.asList(FrCPUState.registerLabels));
        }
        else {
            labels.addAll(Arrays.asList(TxCPUState.registerLabels));
        }
        registerCombo = new JComboBox(labels);
        registerCombo.setMaximumRowCount(17);
        registerCombo.addActionListener(this);
        selectionPanel.add(registerCombo);

        
        Box.Filler largeFiller = new Box.Filler(new Dimension(0, 0), new Dimension(60, 0), new Dimension(60, 0));
        selectionPanel.add(largeFiller);

        
        leftButton = new JButton("<<");
        leftButton.setToolTipText("Previous page (-0x10000)");
        selectionPanel.add(leftButton);
        leftButton.addActionListener(this);
        
        selectionPanel.add(new JLabel("Go to  0x"));
        addressField = new JTextField(Format.asHex(baseAddress, 8), 8);
        selectionPanel.add(addressField);
        addressField.addActionListener(this);
        
        JButton goButton = new JButton("Go");
        selectionPanel.add(goButton);
        goButton.addActionListener(this);

        rightButton = new JButton(">>");
        rightButton.setToolTipText("Next page (+0x10000)");
        selectionPanel.add(rightButton);
        rightButton.addActionListener(this);

        
        selectionPanel.add(largeFiller);

        
        saveLoadButton = new JButton("Save/Load");
        saveLoadButton.setToolTipText("Save/Load selected area to/from file");
        selectionPanel.add(saveLoadButton);
        saveLoadButton.addActionListener(this);


        editorPanel.add(selectionPanel, BorderLayout.NORTH);

        hexEditor = new HexEditor();
        hexEditor.setRowHeaderOffset(baseAddress);
        hexEditor.setRowHeaderMinDigits(8);
        hexEditor.setCellEditable(editable);
        hexEditor.setAlternateRowBG(true);

        hexEditor.addHexEditorListener(this);

        editorPanel.add(hexEditor, BorderLayout.CENTER);
        
        return editorPanel;
    }

    private void loadPage(int baseAddress) {
        this.baseAddress = baseAddress;
        addressField.setBackground(Color.WHITE);

        tabbedPane.setTitleAt(0, "Page 0x" + Format.asHex(baseAddress, 8) + " - 0x" + Format.asHex(baseAddress + memory.getPageSize() - 1, 8)) ;

        try {
            currentPage = memory.getPageForAddress(baseAddress);
            if (currentPage == null) {
                memory.map(baseAddress, memory.getPageSize(), true, true, true);
                currentPage = memory.getPageForAddress(baseAddress);
            }
            hexEditor.open(new ByteArrayInputStream(currentPage));
            hexEditor.setColorMap(createColorMap());
            hexEditor.setRowHeaderOffset(baseAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Color[] createColorMap() {
        Color[] colorMap = null; // default if memory is not tracked
        TrackingMemoryActivityListener activityListener = ui.getTrackingMemoryActivityListener(chip);
        if (activityListener != null) {
            int[] cellActivityMap = activityListener.getCellActivityMap(baseAddress >>> 16);
            colorMap = new Color[0x10000];
            if (cellActivityMap == null) {
                // Memory is tracked, but this is page has never been accessed
                Arrays.fill(colorMap, Color.LIGHT_GRAY);
            }
            else {
                // Memory is tracked, set cell color according to access
                for (int i = 0; i < cellActivityMap.length; i++) {
                    int activity = cellActivityMap[i];
                    if (activity == 0 ) {
                        colorMap[i] = Color.LIGHT_GRAY;
                    }
                    else {
                        colorMap[i] = new Color((activity & 0xFF0000) == 0?0:0xFF, (activity & 0xFF00) == 0?0:0x7F, (activity & 0xFF) == 0?0:0xFF);
                    }
                }
            }
        }
        return colorMap;
    }

    public void dispose() {
        refreshTimer.stop();
        refreshTimer = null;
        super.dispose();
    }

    public void setEditable(boolean editable) {
        refreshData();
        this.editable = editable;
        hexEditor.setCellEditable(editable);
    }

    public void actionPerformed(ActionEvent e) {
        int address;
        int selectionLength = 1;
        // Handle "FP" button
        if (fpButton.equals(e.getSource())) {
            if (chip == Constants.CHIP_FR) {
                addressField.setText(Format.asHex(cpuState.getReg(FrCPUState.FP), 8));
            }
            else {
                addressField.setText(Format.asHex(cpuState.getReg(TxCPUState.FP), 8));
            }
            registerCombo.setSelectedIndex(0);
            selectionLength = 4;
        }
        // Handle "SP" button
        else if (spButton.equals(e.getSource())) {
            if (chip == Constants.CHIP_FR) {
                addressField.setText(Format.asHex(cpuState.getReg(FrCPUState.SP), 8));
            }
            else {
                addressField.setText(Format.asHex(cpuState.getReg(TxCPUState.SP), 8));
            }
            registerCombo.setSelectedIndex(0);
            selectionLength = 4;
        }
        // Handle register combo
        else if (registerCombo.equals(e.getSource())) {
            int selectedIndex = registerCombo.getSelectedIndex();
            if (selectedIndex == 0) {
                return;
            }
            addressField.setText(Format.asHex(cpuState.getReg(selectedIndex - 1), 8));
            selectionLength = 4;
        }
        // Handle "previous" button
        else if (leftButton.equals(e.getSource())) {
            long longAddress = Format.parseIntHexField(addressField) & 0xFFFFFFFFL;
            longAddress -= memory.getPageSize();
            if (longAddress >= 0) {
                addressField.setText(Format.asHex((int) longAddress, 8));
            }
            registerCombo.setSelectedIndex(0);
        }
        // Handle "next" button
        else if (rightButton.equals(e.getSource())) {
            address = Format.parseIntHexField(addressField);
            address += memory.getPageSize();
            if (address < 0x100000000L) {
                addressField.setText(Format.asHex(address, 8));
            }
            registerCombo.setSelectedIndex(0);
        }
        // Handle "save/load" button
        else if (saveLoadButton.equals(e.getSource())) {
            int start = hexEditor.getSmallestSelectionIndex();
            int end =  hexEditor.getLargestSelectionIndex();
            new SaveLoadMemoryDialog(getEmulatorUi(), memory, baseAddress + start, (start==end)?null:(baseAddress + end)).setVisible(true);
            return;
        }
        
        // other cases mean it was just the "GO" button or Return key in the text field
        jumpToAddressField(selectionLength);
    }

    private void jumpToAddressField(int selectionLength) {
        // read address in field and load corresponding page
        int address = Format.parseIntHexField(addressField);
        jumpToAddress(address, selectionLength);
    }

    public void jumpToAddress(int address, int selectionLength) {
        int baseAddress = address & 0xFFFF0000;
        loadPage(baseAddress);
        int offset = address & 0x0000FFFF;
        hexEditor.setSelectedRange(offset, offset + selectionLength - 1);
    }

    public void hexBytesChanged(HexEditorEvent event) {
        if (event.isModification()) {
            try {
                memory.store8((int) ((baseAddress & 0xFFFFFFFFL) + (long)event.getOffset()), hexEditor.getByte(event.getOffset()));
            }
            catch (ArrayIndexOutOfBoundsException exception) {
                JOptionPane.showMessageDialog(this, "Error writing to memory. This area is probably protected (see options)", "Write error", JOptionPane.ERROR_MESSAGE);
                // Reload to show unedited values
                jumpToAddress((int) ((baseAddress & 0xFFFFFFFFL) + (long)event.getOffset()), 1);
            }
        }
    }

    private class MemoryWatchTableFormat implements AdvancedTableFormat<MemoryWatch>, WritableTableFormat<MemoryWatch> {
        public boolean isEditable(MemoryWatch baseObject, int column) {
            //noinspection SimplifiableIfStatement
            if (column == 2) {
                return editable;
            }
            else {
                return true;
            }
        }

        public int getColumnCount() {
            return 3;
        }


        public MemoryWatch setColumnValue(MemoryWatch baseObject, Object editedValue, int column) {
            switch (column) {
                case 0:
                    baseObject.setName((String) editedValue);
                    break;
                case 1:
                    try {
                        int address = Format.parseUnsigned((((String)editedValue).startsWith("0x")?"":"0x") + editedValue);
                        baseObject.setAddress(address);
                    } catch (ParsingException e) {
                        // ignore the change
                    }
                    break;
                case 2:
                    if (editable) {
                        try {
                            int value = Format.parseUnsigned((((String)editedValue).startsWith("0x")?"":"0x") + editedValue);
                            memory.store32(baseObject.getAddress(), value);
                            refreshMemoryPage();
                        } catch (ParsingException e) {
                            // ignore the change
                        }
                    }
                    break;
            }
            return baseObject;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Address";
                case 2:
                    return "Value";
            }
            return null;
        }

        public Object getColumnValue(MemoryWatch baseObject, int column) {
            switch (column) {
                case 0:
                    return baseObject.getName();
                case 1:
                    return Format.asHex(baseObject.getAddress(), 8);
                case 2:
                    return Format.asHex(memory.load32(baseObject.getAddress()), 8);
            }
            return null;
        }

        public Class getColumnClass(int column) {
            switch (column) {
                case 0:
                    return String.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
            }
            return null;
        }

        public Comparator getColumnComparator(int column) {
            return null;
        }
    }
}
