package com.nikonhacker.gui.component.realos;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.realos.*;
import com.nikonhacker.realos.fr.FrSysCallEnvironment;
import com.nikonhacker.realos.tx.TxSysCallEnvironment;

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

    private JButton updateAllButton;
    private JCheckBox autoUpdateCheckbox;
    private final JPanel taskPanel, semaphorePanel, eventFlagPanel, mailboxPanel;
    private JScrollPane taskScroller, semaphoreScroller, eventFlagScroller, mailboxScroller;

    private final EventList<TaskInformation> taskInformationList;
    private final EventList<SemaphoreInformation> semaphoreInformationList;
    private final EventList<EventFlagInformation> eventFlagInformationList;
    private final EventList<MailboxInformation> mailboxInformationList;

    private final SysCallEnvironment sysCallEnvironment;

    public RealOsObjectFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui, Platform platform, CodeStructure codeStructure) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        sysCallEnvironment = (chip==Constants.CHIP_FR)?new FrSysCallEnvironment(platform):new TxSysCallEnvironment(platform, codeStructure);

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
        autoUpdateCheckbox.setSelected(ui.getPrefs().isAutoUpdateRealOsObjects(chip));
        autoUpdateCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ui.getPrefs().setAutoUpdateRealOsObjects(chip, autoUpdateCheckbox.isSelected());
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
        JTable taskTable = new JTable(new EventTableModel<TaskInformation>(sortedTaskInformationList, GlazedLists.tableFormat(sysCallEnvironment.getTaskInformationClass(),
                sysCallEnvironment.getTaskPropertyNames(),
                sysCallEnvironment.getTaskColumnLabels())));
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
                            ErrorCode errorCode = sysCallEnvironment.setFlagIdPattern(chip, flagId, value);
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
        TaskInformation taskInformation = sysCallEnvironment.getTaskInformation(chip, taskNumber);
        while (!EnumSet.of(ErrorCode.E_ID, ErrorCode.E_EMULATOR).contains(taskInformation.getErrorCode()) && taskNumber < 100) {
            taskInformationList.add(taskInformation);
            taskNumber++;
            taskInformation = sysCallEnvironment.getTaskInformation(chip, taskNumber);
        }

        taskPanel.removeAll();
        if (taskInformation.getErrorCode() == ErrorCode.E_EMULATOR) {
            taskPanel.add(getSyscallNotFountErrorLabel(chip), BorderLayout.CENTER);
        }
        else {
            taskPanel.add(taskScroller, BorderLayout.CENTER);
        }
        taskPanel.revalidate();
    }

    public void updateSemaphoreList(int chip) {
        semaphoreInformationList.clear();
        int semaphoreNumber = 1;
        SemaphoreInformation semaphoreInformation = sysCallEnvironment.getSemaphoreInformation(chip, semaphoreNumber);
        while (semaphoreInformation.getErrorCode() == ErrorCode.E_OK) {
            semaphoreInformationList.add(semaphoreInformation);
            semaphoreNumber++;
            semaphoreInformation = sysCallEnvironment.getSemaphoreInformation(chip, semaphoreNumber);
        }

        semaphorePanel.removeAll();
        if (semaphoreInformation.getErrorCode() == ErrorCode.E_EMULATOR) {
            semaphorePanel.add(getSyscallNotFountErrorLabel(chip), BorderLayout.CENTER);
        }
        else {
            semaphorePanel.add(semaphoreScroller, BorderLayout.CENTER);
        }
        semaphorePanel.revalidate();
    }


    public void updateEventFlagList(int chip) {
        eventFlagInformationList.clear();
        int eventFlagNumber = 1;
        EventFlagInformation eventFlagInformation = sysCallEnvironment.getEventFlagInformation(chip, eventFlagNumber);
        while (eventFlagInformation.getErrorCode() == ErrorCode.E_OK) {
            eventFlagInformationList.add(eventFlagInformation);
            eventFlagNumber++;
            eventFlagInformation = sysCallEnvironment.getEventFlagInformation(chip, eventFlagNumber);
        }

        eventFlagPanel.removeAll();
        if (eventFlagInformation.getErrorCode() == ErrorCode.E_EMULATOR) {
            eventFlagPanel.add(getSyscallNotFountErrorLabel(chip), BorderLayout.CENTER);
        }
        else {
            eventFlagPanel.add(eventFlagScroller, BorderLayout.CENTER);
        }
        eventFlagPanel.revalidate();
    }


    public void updateMailboxList(int chip) {
        mailboxInformationList.clear();
        int mailboxNumber = 1;
        MailboxInformation mailboxInformation = sysCallEnvironment.getMailboxInformation(chip, mailboxNumber);
        while (mailboxInformation.getErrorCode() == ErrorCode.E_OK) {
            mailboxInformationList.add(mailboxInformation);
            mailboxNumber++;
            mailboxInformation = sysCallEnvironment.getMailboxInformation(chip, mailboxNumber);
        }

        mailboxPanel.removeAll();
        if (mailboxInformation.getErrorCode() == ErrorCode.E_EMULATOR) {
            mailboxPanel.add(getSyscallNotFountErrorLabel(chip), BorderLayout.CENTER);
        }
        else {
            mailboxPanel.add(mailboxScroller, BorderLayout.CENTER);
        }
        mailboxPanel.revalidate();
    }

    private JLabel getSyscallNotFountErrorLabel(int chip) {
        JLabel comp = new JLabel("<html><center>Emulator Error<br/>" + ((chip == Constants.CHIP_FR)?"(syscall interrupt not initialized ?)":"(syscall not declared in dtx.txt file ?)") + "<br/>See console for more info</center></html>");
        comp.setHorizontalAlignment(SwingConstants.CENTER);
        return comp;
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
