package com.nikonhacker.gui.component.callStack;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.Instruction;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.emu.CallStackItem;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.VerticalLayout;
import com.nikonhacker.gui.swing.DocumentFrame;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.LinkedList;


public class CallStackFrame extends DocumentFrame {

    private static final int WINDOW_WIDTH = 250;
    private static final int WINDOW_HEIGHT = 300;

    private Emulator emulator;
    private CPUState cpuState;
    private final LinkedList<CallStackItem> callStack;

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private Timer refreshTimer;
    private final JList callStackList;

    private CodeStructure codeStructure;

    public CallStackFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, Emulator emulator, CPUState cpuState, CodeStructure codeStructure) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.emulator = emulator;
        this.cpuState = cpuState;
        this.codeStructure = codeStructure;

        setLayout(new BorderLayout());

        callStack = new LinkedList<CallStackItem>();

        emulator.setCallStack(callStack);

        callStackList = new JList();
        callStackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        callStackList.setLayoutOrientation(JList.VERTICAL);
        callStackList.setVisibleRowCount(10);
        callStackList.setCellRenderer(new CallStackItemRenderer());

        updateList();

        callStackList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = callStackList.locationToIndex(e.getPoint());
                    showSource(index);
                }
            }
        });


        JScrollPane listScroller = new JScrollPane(callStackList);
        listScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        add(listScroller, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new VerticalLayout());


        JButton showSourceButton = new JButton("Show source");
        showSourceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSource(callStackList.getSelectedIndex());
            }
        });
        
        showSourceButton.setEnabled(codeStructure != null);

        buttonPanel.add(showSourceButton);

        JButton showStackButton = new JButton("Show stack");
        showStackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showStack(callStackList.getSelectedIndex());
            }
        });

        buttonPanel.add(showStackButton);

        JButton copyToClipboardButton = new JButton("Copy to clipboard");
        copyToClipboardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyToClipboard();
            }
        });

        buttonPanel.add(copyToClipboardButton);

        if (chip == Constants.CHIP_TX) {
            // This distinction is not useful for FR which has separate CALL and JMP functions
            final JCheckBox hideJumps = new JCheckBox("Hide jumps");
            hideJumps.setSelected(ui.getPrefs().isCallStackHideJumps(chip));
            hideJumps.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onHideJumpsChange(hideJumps.isSelected());
                }
            });
            buttonPanel.add(hideJumps);
        }

        add(buttonPanel, BorderLayout.EAST);

        pack();

        // Prepare refresh timer
        refreshTimer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateList();
            }
        });
    }

    private void onHideJumpsChange(boolean selected) {
        ui.getPrefs().setCallStackHideJumps(chip, selected);
        updateList();
    }

    private void copyToClipboard() {
        String s = "0x" + Format.asHex(cpuState.pc, 8) + System.lineSeparator();
        boolean hideJumps = ui.getPrefs().isCallStackHideJumps(chip);
        for (CallStackItem callStackItem : callStack) {
            if (!(callStackItem.getInstruction().getFlowType() == Instruction.FlowType.JMP) || !hideJumps) {
                s += getFormattedElement(callStackItem) + System.lineSeparator();
            }
        }

        StringSelection selection = new StringSelection(s.trim());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    private void updateList() {
        synchronized (callStack) {
            DefaultListModel model = new DefaultListModel();
            // Pseudo stack element
            CallStackItem currentPositionItem = new CallStackItem(cpuState.pc, cpuState.pc, null, null, null);
            model.addElement(currentPositionItem);
            // Real stack
            boolean hideJumps = ui.getPrefs().isCallStackHideJumps(chip);
            for (CallStackItem callStackItem : callStack) {
                if (callStackItem.getInstruction() == null || !(callStackItem.getInstruction().getFlowType() == Instruction.FlowType.JMP) || !hideJumps) {
                    model.addElement(callStackItem);
                }
            }
            callStackList.setModel(model);
        }
    }

    private void showSource(int index) {
        if (index != -1) {
            if (index == 0) {
                // Pseudo stack element
                ui.jumpToSource(chip, cpuState.pc);
            }
            else {
                // Real stack
                ui.jumpToSource(chip, callStack.get(index - 1).getAddress());
            }
        }
    }

    private void showStack(int index) {
        if (index != -1) {
            if (index == 0) {
                // Pseudo stack element
                ui.jumpToMemory(chip, cpuState.getSp());
            }
            else {
                // Real stack
                ui.jumpToMemory(chip, callStack.get(index - 1).getSp());
            }
        }
    }

    public void setAutoRefresh(boolean refresh) {
        updateList();
        if (refresh) {
            if (!refreshTimer.isRunning()) {
                refreshTimer.start();
            }
        }
        else {
            if (refreshTimer.isRunning()) {
                refreshTimer.stop();
            }
        }
    }

    public void dispose() {
        refreshTimer.stop();
        refreshTimer = null;
        emulator.setCallStack(null);
        super.dispose();
    }

    private String getFormattedElement(CallStackItem item) {
        String s = item.toString().trim();
        if (codeStructure != null && item.getTargetAddress() != null) {
            try {
                int targetAddress = Format.parseUnsigned(item.getTargetAddress());
                // Find back function name, ignoring last bit (16-bit ISA indicator in Tx)
                String label = codeStructure.getFunctionName(targetAddress &0xFFFFFFFE);
                if (label != null) {
                    s += " (" + label + ")";
                }
            } catch (ParsingException e) {
                // ignore
            }
        }
        return s;
    }

    /**
     * Mostly a DefaultListCellRenderer, overriding getListCellRendererComponent to have a custom String formating
     */
    private class CallStackItemRenderer extends DefaultListCellRenderer implements ListCellRenderer<Object>, Serializable {
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus)
        {
            setComponentOrientation(list.getComponentOrientation());

            Color bg = null;
            Color fg = null;

            JList.DropLocation dropLocation = list.getDropLocation();
            if (dropLocation != null
                    && !dropLocation.isInsert()
                    && dropLocation.getIndex() == index) {

                bg = javax.swing.UIManager.getColor("List.dropCellBackground");
                fg = javax.swing.UIManager.getColor("List.dropCellForeground");

                isSelected = true;
            }

            if (isSelected) {
                setBackground(bg == null ? list.getSelectionBackground() : bg);
                setForeground(fg == null ? list.getSelectionForeground() : fg);
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (value instanceof Icon) {
                setIcon((Icon)value);
                setText("");
            }
            else {
                setIcon(null);
                setText((value == null) ? "" : getFormattedElement((CallStackItem) value));
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());

            Border border = null;
            if (cellHasFocus) {
                if (isSelected) {
                    border = javax.swing.UIManager.getBorder("List.focusSelectedCellHighlightBorder");
                }
                if (border == null) {
                    border = javax.swing.UIManager.getBorder("List.focusCellHighlightBorder");
                }
            } else {
                border = new EmptyBorder(1, 1, 1, 1);
            }
            setBorder(border);

            return this;
        }
    }
}
