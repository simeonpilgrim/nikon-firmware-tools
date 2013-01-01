package com.nikonhacker.gui.component.callStack;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.emu.CallStackItem;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    public CallStackFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, Emulator emulator, CPUState cpuState, CodeStructure codeStructure) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.emulator = emulator;
        this.cpuState = cpuState;

        setLayout(new BorderLayout());

        callStack = new LinkedList<CallStackItem>();

        emulator.setCallStack(callStack);

        callStackList = new JList();
        callStackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        callStackList.setLayoutOrientation(JList.VERTICAL);
        callStackList.setVisibleRowCount(10);
        callStackList.setCellRenderer(new CallStackItemRenderer(codeStructure));

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
        
        add(buttonPanel, BorderLayout.EAST);

        pack();

        // Prepare refresh timer
        refreshTimer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateList();
            }
        });
    }

    private void updateList() {
        synchronized (callStack) {
            DefaultListModel model = new DefaultListModel();
            // Pseudo stack element
            CallStackItem currentPositionItem = new CallStackItem(cpuState.pc, cpuState.pc, null, null);
            model.addElement(currentPositionItem);
            // Real stack
            for (CallStackItem callStackItem : callStack) {
                model.addElement(callStackItem);
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

    private class CallStackItemRenderer extends JLabel implements ListCellRenderer {
        private CodeStructure codeStructure;
        public CallStackItemRenderer(CodeStructure codeStructure) {
            this.codeStructure = codeStructure;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            CallStackItem item = (CallStackItem) value;
            String s = item.toString().trim();
            if (codeStructure != null && item.getTargetAddress() != null) {
                try {
                    int targetAddress = Format.parseUnsigned(item.getTargetAddress());
                    String label = codeStructure.getFunctionName(targetAddress);
                    if (label != null) {
                        s += " (" + label + ")";
                    }
                } catch (ParsingException e) {
                    // ignore
                }
            }
            setText(s);
            return this;
        }
    }
}
