package com.nikonhacker.gui.component.itron;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;
import com.nikonhacker.gui.swing.VerticalLayout;
import com.nikonhacker.itron.tx.TxItronTaskTable;
import com.nikonhacker.itron.ReturnStackEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.LinkedList;


public class ITronReturnStackFrame extends DocumentFrame {

    private static final int WINDOW_WIDTH = 250;
    private static final int WINDOW_HEIGHT = 300;

    private Emulator emulator;
    private CodeStructure codeStructure;
    private final Platform platform;

    private final TxItronTaskTable taskTable;
    private int tasks;
    LinkedList<ReturnStackEntry> returnStack;

    private final JList returnStackList;
    private JButton updateButton;
    private JCheckBox autoUpdateCheckbox;
    private JComboBox taskNumberComboBox;

    public ITronReturnStackFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui, Platform platform, CodeStructure codeStructure) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.emulator = emulator;
        this.codeStructure = codeStructure;
        this.platform = platform;
        taskTable = new TxItronTaskTable(platform.getMemory());

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Task 0x"));
        taskNumberComboBox = new JComboBox();
        taskNumberComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateReturnList();
            }
        });
        topPanel.add(taskNumberComboBox);

        updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateAll();
            }
        });
        topPanel.add(updateButton);

        autoUpdateCheckbox = new JCheckBox("Auto-update");
        autoUpdateCheckbox.setSelected(ui.getPrefs().isAutoUpdateITronObjects(chip));
        autoUpdateCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ui.getPrefs().setAutoUpdateITronObjects(chip, autoUpdateCheckbox.isSelected());
                if (autoUpdateCheckbox.isSelected()) {
                    if (updateButton.isEnabled()) {
                        updateAll();
                    }
                }
            }
        });
        topPanel.add(autoUpdateCheckbox);

        add(topPanel, BorderLayout.NORTH);

        returnStackList = new JList();
        returnStackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        returnStackList.setLayoutOrientation(JList.VERTICAL);
        returnStackList.setVisibleRowCount(10);
        add(returnStackList, BorderLayout.CENTER);

        returnStackList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final int index = returnStackList.locationToIndex(e.getPoint());
                    if (index!=-1)
                        ui.jumpToSource(chip, returnStack.get(index).returnAddress);
                }
            }
        });

        JScrollPane listScroller = new JScrollPane(returnStackList);
        listScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        add(listScroller, BorderLayout.CENTER);

        pack();

    }

    private final void updateReturnList() {
        returnStack = null;

        if (tasks>0) {
            TxCPUState st=taskTable.getUserCpuState(taskNumberComboBox.getSelectedIndex()+1);
            if (st!=null) {
                returnStack = taskTable.getUserReturnStack(st);
            } else {
                st = ((TxCPUState)platform.getCpuState());
                if ( st.is16bitIsaMode) {
                    // attempt trace current task
                    returnStack = taskTable.getUserReturnStack(st);
                }
            }
        }

        DefaultListModel model = new DefaultListModel();
        if (returnStack==null) {
            model.addElement("Not available");
            returnStackList.setModel(model);
            returnStackList.setEnabled(false);
        } else {
            // update task data and list
            for (ReturnStackEntry entry : returnStack) {
                model.addElement(entry.function.getName() + ": 0x" + Format.asHex(entry.returnAddress,8));
            }
            returnStackList.setModel(model);
            returnStackList.setEnabled(true);
            final int currentPos = Math.max(returnStack.size()-1,0);
            returnStackList.setSelectedIndex(currentPos);
            returnStackList.ensureIndexIsVisible(currentPos);
        }
    }

    public final void updateAll() {
        int i = taskTable.read(codeStructure);

        // get tasks and update combobox
        if (i != tasks) {
            tasks = i;
            taskNumberComboBox.removeAllItems();
            for (i=1; i<= tasks; i++) {
                taskNumberComboBox.addItem(Integer.toHexString(i).toUpperCase());
            }
            // do not call updateReturnList() here, because changed task selection do it anyway
        } else {
            updateReturnList();
        }
    }

    public void enableUpdate(boolean enable) {
        updateButton.setEnabled(enable);
    }

    public void onEmulatorStop(int chip) {
        if (autoUpdateCheckbox.isSelected()) {
            updateAll();
        }
    }

    public void dispose() {
        super.dispose();
    }
}
