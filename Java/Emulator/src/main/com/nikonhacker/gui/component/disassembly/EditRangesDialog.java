package com.nikonhacker.gui.component.disassembly;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.emu.AddressRange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EditRangesDialog extends JDialog {
    private static final int WINDOW_WIDTH  = 300;
    private static final int WINDOW_HEIGHT = 400;

    private List<AddressRange> addressRanges;
    private final JTable                  rangeTable;
    private final EventList<AddressRange> rangeList;

    public EditRangesDialog(Frame frame, List<AddressRange> addressRanges) {
        super(frame, "Edit PC logging ranges", true);
        this.addressRanges = addressRanges;

        JPanel editPanel = new JPanel(new BorderLayout());

        rangeList = GlazedLists.threadSafeList(new BasicEventList<AddressRange>());
        updateAddressRanges();

        EventTableModel<AddressRange> etm = new EventTableModel<AddressRange>(rangeList, new AddressRangeTableFormat());
        rangeTable = new JTable(etm);
        rangeTable.getColumnModel().getColumn(0).setPreferredWidth(500);
        rangeTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        rangeTable.getColumnModel().getColumn(2).setPreferredWidth(200);

        JScrollPane listScroller = new JScrollPane(rangeTable);
        listScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        editPanel.add(listScroller, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(0, 1));

        JButton addButton = new JButton("Add");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addRange();
            }
        });
        rightPanel.add(addButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteRanges(rangeTable.getSelectedRows());
            }
        });
        rightPanel.add(deleteButton);

        JButton closeButton = new JButton("Close");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EditRangesDialog.this.dispose();
            }
        });
        rightPanel.add(closeButton);

        editPanel.add(rightPanel, BorderLayout.EAST);

        setContentPane(editPanel);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void addRange() {
        AddressRange range = new AddressRange(findNewName());

        addressRanges.add(range);

        updateAddressRanges();

        setSelectedIndex(addressRanges.size() - 1);
    }

    private String findNewName() {
        int i = 1;
        String name;
        do {
            name = "Range_" + i;
            i++;
        }
        while (isNameInUse(name));
        return name;
    }

    private boolean isNameInUse(String name) {
        for (AddressRange addressRange : addressRanges) {
            if (name.equals(addressRange.getName())) {
                return true;
            }
        }
        return false;
    }


    private void setSelectedIndex(int index) {
        rangeTable.getSelectionModel().clearSelection();
        rangeTable.getSelectionModel().addSelectionInterval(index, index);
    }

    private void deleteRanges(int[] indices) {
        if (indices.length > 0) {
            String message;
            if (indices.length ==1) {
                message = "Are you sure you want to delete the range '" + rangeList.get(indices[0]).getName() + "' ?";
            }
            else {
                message = "Are you sure you want to delete " + indices.length + " ranges ?";
            }
            if (JOptionPane.showConfirmDialog(this, message, "Delete ?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                java.util.List<Integer> indexList = new ArrayList<>();
                for (int index : indices) {
                    indexList.add(index);
                }
                // Make sure indices are sorted (the getSelectedRows() does not guarantee that)
                Collections.sort(indexList);
                // Reverse order because items at the end of the list will be shifted when we remove items
                Collections.reverse(indexList);
                // Remove, starting at the maximum index
                for (Integer index : indexList) {
                    addressRanges.remove(index.intValue());
                }
                if (!addressRanges.isEmpty()) {
                    setSelectedIndex(Math.min(indices[0], addressRanges.size() - 1));
                }
            }
            updateAddressRanges();
        }
    }


    public void updateAddressRanges() {
        rangeList.clear();
        for (AddressRange addressRange : addressRanges) {
            rangeList.add(addressRange);
        }
    }

    private class AddressRangeTableFormat implements AdvancedTableFormat<AddressRange>, WritableTableFormat<AddressRange> {

        public boolean isEditable(AddressRange baseObject, int column) {
            return true;
        }

        public int getColumnCount() {
            return 3;
        }


        public AddressRange setColumnValue(AddressRange baseObject, Object editedValue, int column) {
            switch (column) {
                case 0:
                    baseObject.setName((String) editedValue);
                    return baseObject;
                case 1:
                    if ("".equals(editedValue)) {
                        baseObject.setStartAddress(0);
                    }
                    else {
                        try {
                            int address = Format.parseUnsigned("0x" + editedValue);
                            baseObject.setStartAddress(address);
                        } catch (ParsingException e) {
                            // ignore the change
                        }
                    }
                    return baseObject;
                case 2:
                    if ("".equals(editedValue)) {
                        baseObject.setEndAddress(-1); // aka 0xFFFFFFFF
                    }
                    else {
                        try {
                            int address = Format.parseUnsigned("0x" + editedValue);
                            baseObject.setEndAddress(address);
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
                    return "Start address";
                case 2:
                    return "End address";
            }
            return null;
        }

        public Object getColumnValue(AddressRange baseObject, int column) {
            switch (column) {
                case 0:
                    return baseObject.getName();
                case 1:
                    return Format.asHex(baseObject.getStartAddress(), 8);
                case 2:
                    return Format.asHex(baseObject.getEndAddress(), 8);
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
