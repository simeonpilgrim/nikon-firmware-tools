package com.nikonhacker.gui.component.itron;

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
import com.nikonhacker.gui.swing.DocumentFrame;
import com.nikonhacker.itron.*;
import com.nikonhacker.itron.fr.FrSysCallEnvironment;
import com.nikonhacker.itron.tx.TxSysCallEnvironment;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;

public class ITronObjectFrame extends DocumentFrame {

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

    public ITronObjectFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui, Platform platform, CodeStructure codeStructure) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        sysCallEnvironment = (chip==Constants.CHIP_FR)?new FrSysCallEnvironment(platform, codeStructure):new TxSysCallEnvironment(platform, codeStructure);

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
        autoUpdateCheckbox.setSelected(ui.getPrefs().isAutoUpdateITronObjects(chip));
        autoUpdateCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ui.getPrefs().setAutoUpdateITronObjects(chip, autoUpdateCheckbox.isSelected());
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
        final JTable taskTable = new JTable(new EventTableModel<TaskInformation>(
                sortedTaskInformationList,
                GlazedLists.tableFormat(sysCallEnvironment.getTaskInformationClass(),
                                        sysCallEnvironment.getTaskPropertyNames(),
                                        sysCallEnvironment.getTaskColumnLabels())
                                        )
        ){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                //  add coloring based on task status here

                if (!isRowSelected(row))
                {
                    int modelRow = convertRowIndexToModel(row);
                    TaskInformation.TaskState state = (TaskInformation.TaskState)getModel().getValueAt(modelRow, sysCallEnvironment.getTaskStateColumnNumber());
                    switch (state) {
                        case RUN:
                            c.setBackground(Color.YELLOW);
                            break;
                        case WAIT:
                        case WAIT_SUSPEND:
                            c.setBackground(Color.CYAN);
                            break;
                        case DORMANT:
                            c.setBackground(Color.LIGHT_GRAY);
                            break;
                        default:
                            c.setBackground(super.getBackground());
                            break;
                    }
                }

                return c;
            }
        };

        // Handle jump on double click
        taskTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getComponent().isEnabled() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    Point p = e.getPoint();
                    int row = taskTable.rowAtPoint(p);
                    int column = taskTable.columnAtPoint(p);
                    if ("nextPcHex".equals(sysCallEnvironment.getTaskPropertyNames()[column])) {
                        try {
                            ui.jumpToSource(chip, Format.parseUnsigned(taskTable.getValueAt(row, column).toString()));
                        } catch (ParsingException e1) {
                            // ignore
                        }
                    }
                    if ("addrContextHex".equals(sysCallEnvironment.getTaskPropertyNames()[column])) {
                        try {
                            ui.jumpToContext(chip, Format.parseUnsigned(taskTable.getValueAt(row, 0).toString()));
                        } catch (ParsingException e1) {
                            // ignore
                        }
                    }
                }
            }
        });

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
                        String newValue = JOptionPane.showInputDialog(ITronObjectFrame.this, "New value for Pattern",  eventFlagTable.getModel().getValueAt(eventFlagTable.rowAtPoint(e.getPoint()), 2));
                        try {
                            int value = Format.parseUnsigned(newValue);
                            ErrorCode errorCode = sysCallEnvironment.setFlagIdPattern(chip, flagId, value);
                            if (errorCode != ErrorCode.E_OK) {
                                JOptionPane.showMessageDialog(ITronObjectFrame.this, "Error: Setting flag returned " + errorCode);
                            }
                            updateAllLists(chip);
                        } catch (ParsingException e1) {
                            JOptionPane.showMessageDialog(ITronObjectFrame.this, "Error: Cannot parse new value " + newValue);
                        }
                    } catch (ParsingException e1) {
                        JOptionPane.showMessageDialog(ITronObjectFrame.this, "Error: Cannot parse flag ID " + strFlagId);
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
