package com.nikonhacker.gui.component.realos;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.realos.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EnumSet;


public class RealOsObjectFrame extends DocumentFrame {

    private static final int WINDOW_WIDTH = 250;
    private static final int WINDOW_HEIGHT = 300;

    int chip;

    private JButton updateAllButton;
    private JCheckBox autoUpdateCheckbox;
    private final EventList<TaskInformation> taskInformationList;
    private final EventList<SemaphoreInformation> semaphoreInformationList;
    private final EventList<EventFlagInformation> eventFlagInformationList;
    private final EventList<MailboxInformation> mailboxInformationList;
    private final JPanel taskPanel, semaphorePanel, eventFlagPanel, mailboxPanel;
    private JScrollPane taskScroller, semaphoreScroller, eventFlagScroller, mailboxScroller;

    public RealOsObjectFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.chip = chip;

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel();
        updateAllButton = new JButton("Update all");
        updateAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateAllLists(chip);
            }
        });
        topPanel.add(updateAllButton);

        autoUpdateCheckbox = new JCheckBox("Auto-update all");
        autoUpdateCheckbox.setSelected(ui.getPrefs().isAutoUpdateRealOsObjects());
        autoUpdateCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ui.getPrefs().setAutoUpdateRealOsObjects(autoUpdateCheckbox.isSelected());
                if (autoUpdateCheckbox.isSelected()) {
                    if (updateAllButton.isEnabled()) {
                        updateAllLists(chip);
                    }
                }
            }
        });
        topPanel.add(autoUpdateCheckbox);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tasks
        taskInformationList = GlazedLists.threadSafeList(new BasicEventList<TaskInformation>());
        taskPanel = new JPanel(new BorderLayout());
        SortedList<TaskInformation> sortedTaskInformationList = new SortedList<TaskInformation>(taskInformationList, null);
        JTable taskTable = new JTable(new EventTableModel<TaskInformation>(sortedTaskInformationList, GlazedLists.tableFormat(TaskInformation.class,
                new String[]{"objectIdHex", "taskState", "taskPriority", "extendedInformationHex"},
                new String[]{"Task Id", "State", "Priority", "Extended Information"})));
        TableComparatorChooser.install(taskTable, sortedTaskInformationList, AbstractTableComparatorChooser.SINGLE_COLUMN);

        taskScroller = new JScrollPane(taskTable);
        taskScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        taskPanel.add(taskScroller, BorderLayout.CENTER);

        tabbedPane.addTab("Tasks", null, taskPanel);


        // Semaphores
        semaphoreInformationList = GlazedLists.threadSafeList(new BasicEventList<SemaphoreInformation>());
        semaphorePanel = new JPanel(new BorderLayout());
        SortedList<SemaphoreInformation> sortedSemaphoreInformationList = new SortedList<SemaphoreInformation>(semaphoreInformationList, null);
        JTable semaphoreTable = new JTable(new EventTableModel<SemaphoreInformation>(sortedSemaphoreInformationList, GlazedLists.tableFormat(SemaphoreInformation.class,
                new String[]{"objectIdHex", "waitTaskInformationHex", "semaphoreCount", "extendedInformationHex"},
                new String[]{"Semaphore", "First Waiting Task", "Count", "Extended Information"})));
        TableComparatorChooser.install(semaphoreTable, sortedSemaphoreInformationList, AbstractTableComparatorChooser.SINGLE_COLUMN);

        semaphoreScroller = new JScrollPane(semaphoreTable);
        semaphoreScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        semaphorePanel.add(semaphoreScroller, BorderLayout.CENTER);

        tabbedPane.addTab("Semaphores", null, semaphorePanel);


        // EventFlags
        eventFlagInformationList = GlazedLists.threadSafeList(new BasicEventList<EventFlagInformation>());
        eventFlagPanel = new JPanel(new BorderLayout());
        SortedList<EventFlagInformation> sortedEventFlagInformationList = new SortedList<EventFlagInformation>(eventFlagInformationList, null);
        final JTable eventFlagTable = new JTable(new EventTableModel<EventFlagInformation>(sortedEventFlagInformationList, GlazedLists.tableFormat(EventFlagInformation.class,
                new String[]{"objectIdHex", "waitTaskInformationHex", "flagPatternHex", "extendedInformationHex"},
                new String[]{"EventFlag", "First Waiting Task", "Pattern", "Extended Information"})));
        TableComparatorChooser.install(eventFlagTable, sortedEventFlagInformationList, AbstractTableComparatorChooser.SINGLE_COLUMN);

        eventFlagTable.addMouseListener(new MouseInputAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2
                        && e.getButton() == MouseEvent.BUTTON1) {
                    String strFlagId = (String) eventFlagTable.getModel().getValueAt(eventFlagTable.rowAtPoint(e.getPoint()), 0);
                    try {
                        int flagId = Format.parseUnsigned(strFlagId);
                        String newValue = JOptionPane.showInputDialog(RealOsObjectFrame.this, "New value for Pattern",  eventFlagTable.getModel().getValueAt(eventFlagTable.rowAtPoint(e.getPoint()), 2));
                        try {
                            int value = Format.parseUnsigned(newValue);
                            ErrorCode errorCode = ui.setFlagIdPattern(flagId, value);
                            if (errorCode != ErrorCode.E_OK) {
                                JOptionPane.showMessageDialog(RealOsObjectFrame.this, "Error: Setting flag returned " + errorCode);
                            }
                            updateAllLists(chip);
                        } catch (ParsingException e1) {
                            JOptionPane.showMessageDialog(RealOsObjectFrame.this, "Error: Cannot parse new value " + newValue);
                        }
                    } catch (ParsingException e1) {
                        JOptionPane.showMessageDialog(RealOsObjectFrame.this, "Error: Cannot parse flag ID " + strFlagId);
                    }
                }
            }
        });

        eventFlagScroller = new JScrollPane(eventFlagTable);
        eventFlagScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        eventFlagPanel.add(eventFlagScroller, BorderLayout.CENTER);

        tabbedPane.addTab("EventFlags", null, eventFlagPanel);


        // Mailboxes
        mailboxInformationList = GlazedLists.threadSafeList(new BasicEventList<MailboxInformation>());
        mailboxPanel = new JPanel(new BorderLayout());
        SortedList<MailboxInformation> sortedMailboxInformationList = new SortedList<MailboxInformation>(mailboxInformationList, null);
        JTable mailboxTable = new JTable(new EventTableModel<MailboxInformation>(sortedMailboxInformationList, GlazedLists.tableFormat(MailboxInformation.class,
                new String[]{"objectIdHex", "waitTaskInformationHex", "pkMsgHex", "extendedInformationHex"},
                new String[]{"Mailbox", "First Waiting Task", "pkMsg", "Extended Information"})));
        TableComparatorChooser.install(mailboxTable, sortedMailboxInformationList, AbstractTableComparatorChooser.SINGLE_COLUMN);

        mailboxScroller = new JScrollPane(mailboxTable);
        mailboxScroller.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        mailboxPanel.add(mailboxScroller, BorderLayout.CENTER);

        tabbedPane.addTab("Mailboxes", null, mailboxPanel);


        // Add tab panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        getContentPane().add(mainPanel);

        pack();
    }

    public void updateTaskList(int chip) {
        taskInformationList.clear();
        int taskNumber = 1;
        TaskInformation taskInformation = ui.getTaskInformation(taskNumber);
        while (!EnumSet.of(ErrorCode.E_ID, ErrorCode.E_FREMU).contains(taskInformation.getErrorCode())) {
            taskInformationList.add(taskInformation);
            taskNumber++;
            taskInformation = ui.getTaskInformation(taskNumber);
        }

        taskPanel.removeAll();
        if (taskInformation.getErrorCode() == ErrorCode.E_FREMU) {
            JLabel comp = new JLabel("<html><center>Emulator Error<br/>(syscall interrupt not initialized ?)<br/>See console for more info</center></html>");
            comp.setHorizontalAlignment(SwingConstants.CENTER);
            taskPanel.add(comp, BorderLayout.CENTER);
        }
        else {
            taskPanel.add(taskScroller, BorderLayout.CENTER);
        }
        taskPanel.revalidate();
    }

    public void updateSemaphoreList(int chip) {
        semaphoreInformationList.clear();
        int semaphoreNumber = 1;
        SemaphoreInformation semaphoreInformation = ui.getSemaphoreInformation(semaphoreNumber);
        while (!EnumSet.of(ErrorCode.E_ID, ErrorCode.E_FREMU).contains(semaphoreInformation.getErrorCode())) {
            semaphoreInformationList.add(semaphoreInformation);
            semaphoreNumber++;
            semaphoreInformation = ui.getSemaphoreInformation(semaphoreNumber);
        }

        semaphorePanel.removeAll();
        if (semaphoreInformation.getErrorCode() == ErrorCode.E_FREMU) {
            JLabel comp = new JLabel("<html><center>Emulator Error<br/>(syscall interrupt not initialized ?)<br/>See console for more info</center></html>");
            comp.setHorizontalAlignment(SwingConstants.CENTER);
            semaphorePanel.add(comp, BorderLayout.CENTER);
        }
        else {
            semaphorePanel.add(semaphoreScroller, BorderLayout.CENTER);
        }
        semaphorePanel.revalidate();
    }


    public void updateEventFlagList(int chip) {
        eventFlagInformationList.clear();
        int eventFlagNumber = 1;
        EventFlagInformation eventFlagInformation = ui.getEventFlagInformation(eventFlagNumber);
        while (!EnumSet.of(ErrorCode.E_ID, ErrorCode.E_FREMU).contains(eventFlagInformation.getErrorCode())) {
            eventFlagInformationList.add(eventFlagInformation);
            eventFlagNumber++;
            eventFlagInformation = ui.getEventFlagInformation(eventFlagNumber);
        }

        eventFlagPanel.removeAll();
        if (eventFlagInformation.getErrorCode() == ErrorCode.E_FREMU) {
            JLabel comp = new JLabel("<html><center>Emulator Error<br/>(syscall interrupt not initialized ?)<br/>See console for more info</center></html>");
            comp.setHorizontalAlignment(SwingConstants.CENTER);
            eventFlagPanel.add(comp, BorderLayout.CENTER);
        }
        else {
            eventFlagPanel.add(eventFlagScroller, BorderLayout.CENTER);
        }
        eventFlagPanel.revalidate();
    }


    public void updateMailboxList(int chip) {
        mailboxInformationList.clear();
        int mailboxNumber = 1;
        MailboxInformation mailboxInformation = ui.getMailboxInformation(mailboxNumber);
        while (!EnumSet.of(ErrorCode.E_ID, ErrorCode.E_FREMU).contains(mailboxInformation.getErrorCode())) {
            mailboxInformationList.add(mailboxInformation);
            mailboxNumber++;
            mailboxInformation = ui.getMailboxInformation(mailboxNumber);
        }

        mailboxPanel.removeAll();
        if (mailboxInformation.getErrorCode() == ErrorCode.E_FREMU) {
            JLabel comp = new JLabel("<html><center>Emulator Error<br/>(syscall interrupt not initialized ?)<br/>See console for more info</center></html>");
            comp.setHorizontalAlignment(SwingConstants.CENTER);
            mailboxPanel.add(comp, BorderLayout.CENTER);
        }
        else {
            mailboxPanel.add(mailboxScroller, BorderLayout.CENTER);
        }
        mailboxPanel.revalidate();
    }

    public void updateAllLists(int chip) {
        updateTaskList(chip);
        updateSemaphoreList(chip);
        updateEventFlagList(chip);
        updateMailboxList(chip);
    }

    public void enableUpdate(boolean enable) {
        updateAllButton.setEnabled(enable);
    }

    public void onEmulatorStop(int chip) {
        if (autoUpdateCheckbox.isSelected()) {
            updateAllLists(chip);
        }
    }
}
