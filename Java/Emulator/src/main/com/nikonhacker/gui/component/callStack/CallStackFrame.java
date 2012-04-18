package com.nikonhacker.gui.component.callStack;

import com.nikonhacker.Format;
import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.dfr.CodeStructure;
import com.nikonhacker.dfr.DisassembledInstruction;
import com.nikonhacker.dfr.Function;
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
    private CodeStructure codeStructure;
    private final LinkedList<CallStackItem> callStack;

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private Timer _timer;
    private final JList callStackList;

    public CallStackFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, Emulator emulator, CPUState cpuState, CodeStructure codeStructure, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
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

        // Prepare update timer
        _timer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateList();
            }
        });
    }

    private void updateList() {
        synchronized (callStack) {
            DefaultListModel model = new DefaultListModel();
            // Pseudo stack element
            CallStackItem currentPositionItem = new CallStackItem(cpuState.pc, cpuState.pc);
            if (codeStructure != null) {
                computeCustomLabel(currentPositionItem);
            }
            model.addElement(currentPositionItem);
            // Real stack
            for (CallStackItem callStackItem : callStack) {
                if (codeStructure != null && callStackItem.getCustomLabel() == null) {
                    computeCustomLabel(callStackItem);
                }
                model.addElement(callStackItem);
            }
            callStackList.setModel(model);
        }
    }

    private void showSource(int index) {
        if (index != -1) {
            if (index == 0) {
                // Pseudo stack element
                ui.jumpToSource(cpuState.pc);
            }
            else {
                // Real stack
                ui.jumpToSource(callStack.get(index - 1).getAddress());
            }
        }
    }

    private void showStack(int index) {
        if (index != -1) {
            if (index == 0) {
                // Pseudo stack element
                ui.jumpToMemory(cpuState.getReg(CPUState.SP));
            }
            else {
                // Real stack
                ui.jumpToMemory(callStack.get(index - 1).getSp());
            }
        }
    }

    private void computeCustomLabel(CallStackItem callStackItem) {
        DisassembledInstruction instruction = codeStructure.getInstructions().get(callStackItem.getAddress());
        if (instruction != null) {
            Function function = codeStructure.findFunctionIncluding(callStackItem.getAddress());

            callStackItem.setCustomLabel("0x" + Format.asHex(callStackItem.getAddress(), 8)
                    + " - " + function.getName()
                    + "." + instruction.opcode.name
                    + " " + instruction.operands
                    + " - SP=0x" + Format.asHex(callStackItem.getSp(), 8));
        }
    }


    public void setAutoRefresh(boolean editable) {
        updateList();
        if (editable) {
            if (_timer.isRunning()) {
                _timer.stop();
            }
        }
        else {
            if (!_timer.isRunning()) {
                _timer.start();
            }
        }
    }

    public void dispose() {
        _timer.stop();
        _timer = null;
        emulator.setCallStack(null);
        super.dispose();
    }


}
