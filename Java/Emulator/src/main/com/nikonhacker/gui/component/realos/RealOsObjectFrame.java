package com.nikonhacker.gui.component.realos;

import com.nikonhacker.Format;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.realos.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;


public class RealOsObjectFrame extends DocumentFrame {

    private static final int WINDOW_WIDTH = 250;
    private static final int WINDOW_HEIGHT = 300;

    private JList taskList, semaphoreList, eventFlagList, mailboxList;
    private JButton updateAllButton;
    private JCheckBox autoUpdateCheckbox;

    public RealOsObjectFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel();
        updateAllButton = new JButton("Update all");
        updateAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateAllLists();
            }
        });
        topPanel.add(updateAllButton);

        autoUpdateCheckbox = new JCheckBox("Auto-update all");
        autoUpdateCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (autoUpdateCheckbox.isSelected()) {
                    if (updateAllButton.isEnabled()) {
                        updateAllLists();
                    }
                }
            }
        });
        topPanel.add(autoUpdateCheckbox);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tasks
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskList = new JList();
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setLayoutOrientation(JList.VERTICAL);
        taskList.setVisibleRowCount(10);

        JScrollPane listScroller = new JScrollPane(taskList);
        listScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        taskPanel.add(listScroller, BorderLayout.CENTER);

        tabbedPane.addTab("Tasks", null, taskPanel);


        // Semaphores
        JPanel semaphorePanel = new JPanel(new BorderLayout());
        semaphoreList = new JList();
        semaphoreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        semaphoreList.setLayoutOrientation(JList.VERTICAL);
        semaphoreList.setVisibleRowCount(10);

        listScroller = new JScrollPane(semaphoreList);
        listScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        semaphorePanel.add(listScroller, BorderLayout.CENTER);

        tabbedPane.addTab("Semaphores", null, semaphorePanel);


        // EventFlag
        JPanel eventFlagPanel = new JPanel(new BorderLayout());
        eventFlagList = new JList();
        eventFlagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventFlagList.setLayoutOrientation(JList.VERTICAL);
        eventFlagList.setVisibleRowCount(10);

        listScroller = new JScrollPane(eventFlagList);
        listScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        eventFlagPanel.add(listScroller, BorderLayout.CENTER);

        tabbedPane.addTab("EventFlags", null, eventFlagPanel);


        // Mailbox
        JPanel mailboxPanel = new JPanel(new BorderLayout());
        mailboxList = new JList();
        mailboxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mailboxList.setLayoutOrientation(JList.VERTICAL);
        mailboxList.setVisibleRowCount(10);

        listScroller = new JScrollPane(mailboxList);
        listScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        mailboxPanel.add(listScroller, BorderLayout.CENTER);

        tabbedPane.addTab("Mailboxes", null, mailboxPanel);


        // Add tab panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        getContentPane().add(mainPanel);

        pack();
    }

    public void updateTaskList() {
        DefaultListModel model = new DefaultListModel();
        int taskNumber = 1;
        TaskInformation taskInformation = ui.getTaskInformation(taskNumber);
        while (!EnumSet.of(ErrorCode.E_ID, ErrorCode.E_FREMU).contains(taskInformation.getErrorCode())) {
            model.addElement("Task 0x" + Format.asHex(taskNumber, 2) + ": " + taskInformation.toString());
            taskNumber++;
            taskInformation = ui.getTaskInformation(taskNumber);
        }
        if (taskInformation.getErrorCode() == ErrorCode.E_FREMU) {
            JOptionPane.showMessageDialog(this, "Error\nSee console for more info", "Emulator error", JOptionPane.ERROR_MESSAGE);
        }
        taskList.setModel(model);
    }

    public void updateSemaphoreList() {
        DefaultListModel model = new DefaultListModel();
        int semaphoreNumber = 1;
        SemaphoreInformation semaphoreInformation = ui.getSemaphoreInformation(semaphoreNumber);
        while (!EnumSet.of(ErrorCode.E_ID, ErrorCode.E_FREMU).contains(semaphoreInformation.getErrorCode())) {
            model.addElement("Task 0x" + Format.asHex(semaphoreNumber, 2) + ": " + semaphoreInformation.toString());
            semaphoreNumber++;
            semaphoreInformation = ui.getSemaphoreInformation(semaphoreNumber);
        }
        if (semaphoreInformation.getErrorCode() == ErrorCode.E_FREMU) {
            JOptionPane.showMessageDialog(this, "Error\nSee console for more info", "Emulator error", JOptionPane.ERROR_MESSAGE);
        }
        semaphoreList.setModel(model);
    }

    public void updateEventFlagList() {
        DefaultListModel model = new DefaultListModel();
        int eventFlagNumber = 1;
        EventFlagInformation eventFlagInformation = ui.getEventFlagInformation(eventFlagNumber);
        while (!EnumSet.of(ErrorCode.E_ID, ErrorCode.E_FREMU).contains(eventFlagInformation.getErrorCode())) {
            model.addElement("Task 0x" + Format.asHex(eventFlagNumber, 2) + ": " + eventFlagInformation.toString());
            eventFlagNumber++;
            eventFlagInformation = ui.getEventFlagInformation(eventFlagNumber);
        }
        if (eventFlagInformation.getErrorCode() == ErrorCode.E_FREMU) {
            JOptionPane.showMessageDialog(this, "Error\nSee console for more info", "Emulator error", JOptionPane.ERROR_MESSAGE);
        }
        eventFlagList.setModel(model);
    }

    public void updateMailboxList() {
        DefaultListModel model = new DefaultListModel();
        int mailboxNumber = 1;
        MailboxInformation mailboxInformation = ui.getMailboxInformation(mailboxNumber);
        while (!EnumSet.of(ErrorCode.E_ID, ErrorCode.E_FREMU).contains(mailboxInformation.getErrorCode())) {
            model.addElement("Task 0x" + Format.asHex(mailboxNumber, 2) + ": " + mailboxInformation.toString());
            mailboxNumber++;
            mailboxInformation = ui.getMailboxInformation(mailboxNumber);
        }
        if (mailboxInformation.getErrorCode() == ErrorCode.E_FREMU) {
            JOptionPane.showMessageDialog(this, "Error\nSee console for more info", "Emulator error", JOptionPane.ERROR_MESSAGE);
        }
        mailboxList.setModel(model);
    }

    public void updateAllLists() {
        updateTaskList();
        updateSemaphoreList();
        updateEventFlagList();
        updateMailboxList();
    }

    public void enableUpdate(boolean enable) {
        updateAllButton.setEnabled(enable);
    }

    public void onEmulatorStop() {
        if (autoUpdateCheckbox.isSelected()) {
            updateAllLists();
        }
    }
}
