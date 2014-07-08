package com.nikonhacker.gui;

/*
 * Main Emulator class
 *
 * MDI Layout inspired by InternalFrameDemo from the Java Tutorial -
 * http://docs.oracle.com/javase/tutorial/uiswing/components/internalframe.html
 */

import com.nikonhacker.ApplicationInfo;
import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.Prefs;
import com.nikonhacker.disassembly.Disassembler;
import com.nikonhacker.disassembly.Function;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.disassembly.fr.Dfr;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.Dtx;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.ClockableCallbackHandler;
import com.nikonhacker.emu.EmulationException;
import com.nikonhacker.emu.EmulationFramework;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.lcd.fr.FrLcd;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.trigger.condition.BreakCondition;
import com.nikonhacker.encoding.FirmwareDecoder;
import com.nikonhacker.encoding.FirmwareEncoder;
import com.nikonhacker.encoding.FirmwareFormatException;
import com.nikonhacker.encoding.NkldDecoder;
import com.nikonhacker.gui.component.ad.AdConverterFrame;
import com.nikonhacker.gui.component.analyse.AnalyseProgressDialog;
import com.nikonhacker.gui.component.analyse.GenerateSysSymbolsDialog;
import com.nikonhacker.gui.component.breakTrigger.BreakTriggerListFrame;
import com.nikonhacker.gui.component.callStack.CallStackFrame;
import com.nikonhacker.gui.component.codeStructure.CodeStructureFrame;
import com.nikonhacker.gui.component.cpu.CPUStateEditorFrame;
import com.nikonhacker.gui.component.disassembly.DisassemblyFrame;
import com.nikonhacker.gui.component.frontPanel.FrontPanelFrame;
import com.nikonhacker.gui.component.interruptController.FrInterruptControllerFrame;
import com.nikonhacker.gui.component.interruptController.InterruptControllerFrame;
import com.nikonhacker.gui.component.interruptController.TxInterruptControllerFrame;
import com.nikonhacker.gui.component.ioPort.IoPortsFrame;
import com.nikonhacker.gui.component.itron.ITronObjectFrame;
import com.nikonhacker.gui.component.itron.ITronReturnStackFrame;
import com.nikonhacker.gui.component.memoryActivity.MemoryActivityViewerFrame;
import com.nikonhacker.gui.component.memoryHexEditor.MemoryHexEditorFrame;
import com.nikonhacker.gui.component.memoryMapped.Component4006Frame;
import com.nikonhacker.gui.component.memoryMapped.CustomMemoryRangeLoggerFrame;
import com.nikonhacker.gui.component.saveLoadMemory.SaveLoadMemoryDialog;
import com.nikonhacker.gui.component.screenEmulator.ScreenEmulatorFrame;
import com.nikonhacker.gui.component.serialInterface.GenericSerialFrame;
import com.nikonhacker.gui.component.serialInterface.SerialInterfaceFrame;
import com.nikonhacker.gui.component.sourceCode.SourceCodeFrame;
import com.nikonhacker.gui.component.timer.ProgrammableTimersFrame;
import com.nikonhacker.gui.swing.*;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.List;

public class EmulatorUI extends JFrame implements ActionListener {

    // Constants for commands
    private static final String[] COMMAND_IMAGE_LOAD                         = {"FR_IMAGE_LOAD", "TX_IMAGE_LOAD"};
    private static final String[] COMMAND_ANALYSE_DISASSEMBLE                = {"FR_ANALYSE_DISASSEMBLE", "TX_ANALYSE_DISASSEMBLE"};
    private static final String[] COMMAND_EMULATOR_PLAY                      = {"FR_EMULATOR_PLAY", "TX_EMULATOR_PLAY"};
    private static final String[] COMMAND_EMULATOR_DEBUG                     = {"FR_EMULATOR_DEBUG", "TX_EMULATOR_DEBUG"};
    private static final String[] COMMAND_EMULATOR_PAUSE                     = {"FR_EMULATOR_PAUSE", "TX_EMULATOR_PAUSE"};
    private static final String[] COMMAND_EMULATOR_STEP                      = {"FR_EMULATOR_STEP", "TX_EMULATOR_STEP"};
    private static final String[] COMMAND_EMULATOR_STOP                      = {"FR_EMULATOR_STOP", "TX_EMULATOR_STOP"};
    private static final String[] COMMAND_SETUP_BREAKPOINTS                  = {"FR_SETUP_BREAKPOINTS", "TX_SETUP_BREAKPOINTS"};
    private static final String[] COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER      = {"FR_TOGGLE_MEMORY_ACTIVITY_VIEWER", "TX_TOGGLE_MEMORY_ACTIVITY_VIEWER"};
    private static final String[] COMMAND_TOGGLE_MEMORY_HEX_EDITOR           = {"FR_TOGGLE_MEMORY_HEX_EDITOR", "TX_TOGGLE_MEMORY_HEX_EDITOR"};
    private static final String[] COMMAND_TOGGLE_DISASSEMBLY_WINDOW          = {"FR_TOGGLE_DISASSEMBLY_WINDOW", "TX_TOGGLE_DISASSEMBLY_WINDOW"};
    private static final String[] COMMAND_TOGGLE_CPUSTATE_WINDOW             = {"FR_TOGGLE_CPUSTATE_WINDOW", "TX_TOGGLE_CPUSTATE_WINDOW"};
    private static final String[] COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW        = {"FR_COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW", "TX_COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW"};
    private static final String[] COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW = {"FR_TOGGLE_INTERRUPT_CONTROLLER_WINDOW", "TX_TOGGLE_INTERRUPT_CONTROLLER_WINDOW"};
    private static final String[] COMMAND_TOGGLE_SERIAL_INTERFACES           = {"FR_COMMAND_TOGGLE_SERIAL_INTERFACES", "TX_COMMAND_TOGGLE_SERIAL_INTERFACES"};
    private static final String[] COMMAND_TOGGLE_SERIAL_DEVICES              = {"FR_COMMAND_TOGGLE_SERIAL_DEVICES", "TX_COMMAND_TOGGLE_SERIAL_DEVICES"};
    private static final String[] COMMAND_TOGGLE_IO_PORTS_WINDOW             = {"FR_COMMAND_TOGGLE_IO_PORTS_WINDOW", "TX_COMMAND_TOGGLE_IO_PORTS_WINDOW"};
    private static final String[] COMMAND_TOGGLE_AD_CONVERTER                = {"FR_COMMAND_TOGGLE_AD_CONVERTER", "TX_COMMAND_TOGGLE_AD_CONVERTER"};
    private static final String[] COMMAND_SAVE_LOAD_MEMORY                   = {"FR_SAVE_LOAD_MEMORY", "TX_SAVE_LOAD_MEMORY"};
    private static final String[] COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW       = {"FR_TOGGLE_CODE_STRUCTURE_WINDOW", "TX_TOGGLE_CODE_STRUCTURE_WINDOW"};
    private static final String[] COMMAND_TOGGLE_SOURCE_CODE_WINDOW          = {"FR_TOGGLE_SOURCE_CODE_WINDOW", "TX_TOGGLE_SOURCE_CODE_WINDOW"};
    private static final String[] COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW  = {"FR_COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW", "TX_COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW"};
    private static final String[] COMMAND_TOGGLE_CALL_STACK_WINDOW           = {"FR_TOGGLE_CALL_STACK_WINDOW", "TX_TOGGLE_CALL_STACK_WINDOW"};
    private static final String[] COMMAND_TOGGLE_ITRON_OBJECT_WINDOW         = {"FR_TOGGLE_ITRON_OBJECT_WINDOW", "TX_TOGGLE_ITRON_OBJECT_WINDOW"};
    private static final String[] COMMAND_TOGGLE_ITRON_RETURN_STACK_WINDOW   = {"FR_TOGGLE_ITRON_RETURN_STACK_WINDOW", "TX_TOGGLE_ITRON_RETURN_STACK_WINDOW"};
    private static final String[] COMMAND_CHIP_OPTIONS                       = {"FR_OPTIONS", "TX_OPTIONS"};

    private static final String COMMAND_GENERATE_SYS_SYMBOLS         = "GENERATE_SYS_SYMBOLS";
    private static final String COMMAND_TOGGLE_COMPONENT_4006_WINDOW = "TOGGLE_COMPONENT_4006_WINDOW";
    private static final String COMMAND_TOGGLE_SCREEN_EMULATOR       = "TOGGLE_SCREEN_EMULATOR";
    private static final String COMMAND_TOGGLE_FRONT_PANEL           = "TOGGLE_FRONT_PANEL";
    private static final String COMMAND_UI_OPTIONS                   = "UI_OPTIONS";
    private static final String COMMAND_DECODE                       = "DECODE";
    private static final String COMMAND_ENCODE                       = "ENCODE";
    private static final String COMMAND_DECODE_NKLD                  = "DECODE_NKLD";
    private static final String COMMAND_LOAD_STATE                   = "LOAD_STATE";
    private static final String COMMAND_SAVE_STATE                   = "SAVE_STATE";
    private static final String COMMAND_QUIT                         = "QUIT";
    private static final String COMMAND_ABOUT                        = "ABOUT";
    private static final String COMMAND_TEST                         = "TEST";

    /* Tried to set key bindings that avoid conflicts:

     1 - See http://msdn.microsoft.com/en-us/library/windows/desktop/bb545460.aspx
     According to that, the following are safe for windows :
        Ctrl+G, J, K, L, M, Q, R, or T
        Ctrl+any number
        F7, F9, or F12
        Shift+F2, F3, F4, F5, F7, F8, F9, F11, or F12
        Alt+any function key except F4
        Ctrl+any function key except F1, F4, F6, F8
        Ctrl+Shift+any letter or number

        Note: Ctrl+Shift+0 is used by Windows to switch the input language !

    2 - See http://docs.oracle.com/javase/1.3/docs/api/javax/swing/doc-files/Key-Metal.html#JApplet
    According to that, the following keys are absolutely forbidden:
        Ctrl+A, C, X, V, F (select / copy / cut / paste)
        Alt+letters or Alt+numbers, used for menus
        Ctrl or Alt+F4,F5,F7,F8,F9 used by JInternalFrame
        F6 and F8, used by JSplitPane
    3 - Moreover, we want to keep CTRL-F for find

    Still free : Ctrl+J
    */

    private static final int KEY_EVENT_RUN[]   = {KeyEvent.VK_F5, KeyEvent.VK_F9};
    private static final int KEY_EVENT_DEBUG[] = {KeyEvent.VK_F6, KeyEvent.VK_F10};
    private static final int KEY_EVENT_PAUSE[] = {KeyEvent.VK_F7, KeyEvent.VK_F11};
    private static final int KEY_EVENT_STEP[]  = {KeyEvent.VK_F8, KeyEvent.VK_F12};

    private static final int[] KEY_CHIP_MODIFIER = new int[]{ActionEvent.CTRL_MASK, ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK};

    private static final int KEY_EVENT_LOAD = KeyEvent.VK_L; // Standard
    private static final int KEY_EVENT_QUIT = KeyEvent.VK_Q; // Standard

    private static final int KEY_EVENT_CPUSTATE             = KeyEvent.VK_T;
    private static final int KEY_EVENT_MEMORY               = KeyEvent.VK_M;
    private static final int KEY_EVENT_SCREEN               = KeyEvent.VK_S; // Not recommended
    private static final int KEY_EVENT_REALTIME_DISASSEMBLY = KeyEvent.VK_R;
    private static final int KEY_EVENT_SOURCE               = KeyEvent.VK_G; // Meaningless, but well...


    public static final String BUTTON_SIZE_SMALL  = "SMALL";
    public static final String BUTTON_SIZE_MEDIUM = "MEDIUM";
    public static final String BUTTON_SIZE_LARGE  = "LARGE";

    private static final String BUTTON_PROPERTY_KEY_ICON = "icon";

    // Business constants

    private static final String STATUS_DEFAULT_TEXT = "Ready";

    public static final Color STATUS_BGCOLOR_DEFAULT = Color.LIGHT_GRAY;
    public static final Color STATUS_BGCOLOR_RUN     = Color.GREEN;
    public static final Color STATUS_BGCOLOR_DEBUG   = Color.ORANGE;
    public static final Color STATUS_BGCOLOR_BREAK   = new Color(255, 127, 127);

    private EmulationFramework framework;

    // UI

    private final Insets toolbarButtonMargin;

    private final JSplitPane splitPane;
    private final JDesktopPane[] mdiPane = new JDesktopPane[2];
    private final JPanel[]       toolBar = new JPanel[2];

    // Menu items
    private JMenuItem[] loadMenuItem       = new JMenuItem[2];
    private JMenuItem[] playMenuItem       = new JMenuItem[2];
    private JMenuItem[] debugMenuItem      = new JMenuItem[2];
    private JMenuItem[] pauseMenuItem      = new JMenuItem[2];
    private JMenuItem[] stepMenuItem       = new JMenuItem[2];
    private JMenuItem[] stopMenuItem       = new JMenuItem[2];
    private JMenuItem[] breakpointMenuItem = new JMenuItem[2];

    private JMenuItem generateSysSymbolsMenuItem;

    private JCheckBoxMenuItem[] cpuStateMenuItem            = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] memoryHexEditorMenuItem     = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] interruptControllerMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] programmableTimersMenuItem  = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] serialInterfacesMenuItem    = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] serialDevicesMenuItem       = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] ioPortsMenuItem             = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] adConverterMenuItem         = new JCheckBoxMenuItem[2];

    private JCheckBoxMenuItem component4006MenuItem;
    private JCheckBoxMenuItem screenEmulatorMenuItem;

    private JCheckBoxMenuItem frontPanelMenuItem;

    private JCheckBoxMenuItem[] disassemblyMenuItem             = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] memoryActivityViewerMenuItem    = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] customMemoryRangeLoggerMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] callStackMenuItem               = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] iTronObjectMenuItem             = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] iTronReturnStackMenuItem        = new JCheckBoxMenuItem[2];

    private JMenuItem[]         analyseMenuItem        = new JMenuItem[2];
    private JCheckBoxMenuItem[] codeStructureMenuItem  = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] sourceCodeMenuItem     = new JCheckBoxMenuItem[2];
    private JMenuItem[]         saveLoadMemoryMenuItem = new JMenuItem[2];
    private JMenuItem[]         chipOptionsMenuItem    = new JMenuItem[2];

    @SuppressWarnings("FieldCanBeLocal")
    private JMenuItem uiOptionsMenuItem;

    // Buttons
    private JButton[] loadButton       = new JButton[2];
    private JButton[] playButton       = new JButton[2];
    private JButton[] debugButton      = new JButton[2];
    private JButton[] pauseButton      = new JButton[2];
    private JButton[] stepButton       = new JButton[2];
    private JButton[] stopButton       = new JButton[2];
    private JButton[] breakpointButton = new JButton[2];

    private JButton[] disassemblyButton             = new JButton[2];
    private JButton[] cpuStateButton                = new JButton[2];
    private JButton[] memoryActivityViewerButton    = new JButton[2];
    private JButton[] memoryHexEditorButton         = new JButton[2];
    private JButton[] customMemoryRangeLoggerButton = new JButton[2];
    private JButton[] codeStructureButton           = new JButton[2];
    private JButton[] sourceCodeButton              = new JButton[2];
    private JButton[] interruptControllerButton     = new JButton[2];
    private JButton[] programmableTimersButton      = new JButton[2];
    private JButton[] serialInterfacesButton        = new JButton[2];
    private JButton[] serialDevicesButton           = new JButton[2];
    private JButton[] ioPortsButton                 = new JButton[2];
    private JButton[] adConverterButton             = new JButton[2];
    private JButton[] callStackButton               = new JButton[2];
    private JButton[] iTronObjectButton             = new JButton[2];

    private JButton component4006Button;
    private JButton screenEmulatorButton;

    private JButton frontPanelButton;

    private JButton[] analyseButton        = new JButton[2];
    private JButton[] saveLoadMemoryButton = new JButton[2];
    private JButton[] chipOptionsButton    = new JButton[2];

    // Frames
    private CPUStateEditorFrame[]          cpuStateEditorFrame          = new CPUStateEditorFrame[2];
    private DisassemblyFrame[]             disassemblyLogFrame          = new DisassemblyFrame[2];
    private BreakTriggerListFrame[]        breakTriggerListFrame        = new BreakTriggerListFrame[2];
    private MemoryActivityViewerFrame[]    memoryActivityViewerFrame    = new MemoryActivityViewerFrame[2];
    private MemoryHexEditorFrame[]         memoryHexEditorFrame         = new MemoryHexEditorFrame[2];
    private CustomMemoryRangeLoggerFrame[] customMemoryRangeLoggerFrame = new CustomMemoryRangeLoggerFrame[2];
    private CodeStructureFrame[]           codeStructureFrame           = new CodeStructureFrame[2];
    private SourceCodeFrame[]              sourceCodeFrame              = new SourceCodeFrame[2];
    private ProgrammableTimersFrame[]      programmableTimersFrame      = new ProgrammableTimersFrame[2];
    private InterruptControllerFrame[]     interruptControllerFrame     = new InterruptControllerFrame[2];
    private SerialInterfaceFrame[]         serialInterfaceFrame         = new SerialInterfaceFrame[2];
    private GenericSerialFrame[]           genericSerialFrame           = new GenericSerialFrame[2];
    private IoPortsFrame[]                 ioPortsFrame                 = new IoPortsFrame[2];
    private AdConverterFrame[]             adConverterFrame             = new AdConverterFrame[2];
    private CallStackFrame[]               callStackFrame               = new CallStackFrame[2];

    private Component4006Frame component4006Frame;
    private DocumentFrame      screenEmulatorFrame;

    private FrontPanelFrame frontPanelFrame;

    private ITronObjectFrame[] ITronObjectFrame = new ITronObjectFrame[2];
    private ITronReturnStackFrame[] iTronReturnStackFrame = new ITronReturnStackFrame[2];

    // Misc UI related fields
    private String[]  statusText     = {STATUS_DEFAULT_TEXT, STATUS_DEFAULT_TEXT};
    private JLabel[]  statusBar      = new JLabel[2];
    private JSlider[] intervalSlider = new JSlider[2];

    private static ImageIcon[]       programmableTimersPauseButtonIcon           = new ImageIcon[2];
    private        int[]             programmableTimersPauseButtonAnimationIndex = new int[2];
    private        java.util.Timer[] programmableTimersPauseButtonAnimationTimer = new java.util.Timer[2];

    private static File[] imageFile = new File[2];

    private long lastUpdateCycles[] = {0, 0};
    private long lastUpdateTime[]   = {0, 0};

    private Prefs prefs = new Prefs();


    public static void main(String[] args) throws EmulationException, IOException, ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException {

        // Workaround for JDK bug - https://code.google.com/p/nikon-firmware-tools/issues/detail?id=17
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        if (args.length > 0) {
            imageFile[Constants.CHIP_FR] = new File(args[0]);
            if (args.length > 1) {
                imageFile[Constants.CHIP_TX] = new File(args[1]);
            }
        }

        initProgrammableTimerAnimationIcons(BUTTON_SIZE_SMALL);

        // a lot of calls are made from GUI in AWT thread that exits fast with no error code
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                System.exit(1);
            }
        });

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }


    /**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Choose to keep Java-style for main window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        EmulatorUI frame = new EmulatorUI();

        // Display the window.
        frame.setVisible(true);
    }

    public EmulatorUI() {
        super(ApplicationInfo.getNameVersion() + " - (none) / (none)");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        prefs = Prefs.load();
        // Apply register label prefs immediately
        FrCPUState.initRegisterLabels(prefs.getOutputOptions(Constants.CHIP_FR));
        TxCPUState.initRegisterLabels(prefs.getOutputOptions(Constants.CHIP_TX));

        // Create and set up the Emulation Framework
        framework = new EmulationFramework(prefs);

        //Set up the GUI.
        setJMenuBar(createMenuBar());

        toolbarButtonMargin = new Insets(2, 14, 2, 14);

        JPanel[] contentPane = new JPanel[2];
        for (int chip = 0; chip < 2; chip++) {
            // This is the contentPane that will make one side of the JSplitPane
            contentPane[chip] = new JPanel(new BorderLayout());

            // First we create a toolbar and put it on top
            toolBar[chip] = createToolBar(chip);
            contentPane[chip].add(toolBar[chip], BorderLayout.NORTH);


            // Then we prepare a "desktop" panel and put it at the center
            // This subclass of JDesktopPane implements Scrollable and has a size which auto-adapts dynamically
            // to the size and position of its internal frames
            mdiPane[chip] = new ScrollableDesktop();
            mdiPane[chip].setBackground(Constants.CHIP_BACKGROUND_COLOR[chip]);
            mdiPane[chip].setOpaque(true);

            // We wrap it inside a panel to force a stretch to the size of the JSplitPane panel
            // Otherwise, as the size of the panel auto-adapts, it starts at 0,0 if no component is present,
            // so the background color would only be only visible in the bounding box surrounding the internal frames
            JPanel forcedStretchPanel = new JPanel(new BorderLayout());
            forcedStretchPanel.add(mdiPane[chip], BorderLayout.CENTER);

            // And we wrap the result in a JScrollPane to take care of the actual scrolling,
            // before adding it at the center
            contentPane[chip].add(new JScrollPane(forcedStretchPanel), BorderLayout.CENTER);

            // Finally, we prepare the status bar and add it at the bottom
            statusBar[chip] = new JLabel(statusText[chip]);
            statusBar[chip].setOpaque(true);
            statusBar[chip].setBackground(STATUS_BGCOLOR_DEFAULT);
            statusBar[chip].setMinimumSize(new Dimension(0,0));
            contentPane[chip].add(statusBar[chip], BorderLayout.SOUTH);
        }

        setIconImages(Arrays.asList(
                Toolkit.getDefaultToolkit().getImage(EmulatorUI.class.getResource("images/nh_16x16.png")),
                Toolkit.getDefaultToolkit().getImage(EmulatorUI.class.getResource("images/nh_20x20.png")),
                Toolkit.getDefaultToolkit().getImage(EmulatorUI.class.getResource("images/nh_24x24.png")),
                Toolkit.getDefaultToolkit().getImage(EmulatorUI.class.getResource("images/nh_32x32.png")),
                Toolkit.getDefaultToolkit().getImage(EmulatorUI.class.getResource("images/nh_64x64.png"))
        ));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, contentPane[Constants.CHIP_FR], contentPane[Constants.CHIP_TX]);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.5);

        setContentPane(splitPane);

        applyPrefsToUI();

        for (int chip = 0; chip < 2; chip++) {
            if (imageFile[chip] != null) {
                // There was a command line argument.
                if (imageFile[chip].exists()) {
                    initialize(chip);
                }
                else {
                    JOptionPane.showMessageDialog(this, "Given " + Constants.CHIP_LABEL[chip] + " firmware file does not exist:\n" + imageFile[chip].getAbsolutePath(), "File not found", JOptionPane.WARNING_MESSAGE);
                }
            }
            else {
                // Check if a FW file is stored in the prefs
                String firmwareFilename = prefs.getFirmwareFilename(chip);
                if (firmwareFilename != null) {
                    File firmwareFile = new File(firmwareFilename);
                    if (firmwareFile.exists()) {
                        imageFile[chip] = firmwareFile;
                        initialize(chip);
                    }
                    else {
                        JOptionPane.showMessageDialog(this, Constants.CHIP_LABEL[chip] + " firmware file stored in preference file cannot be found:\n" + firmwareFile.getAbsolutePath(), "File not found", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
        framework.setupCallbacks(getCallbackHandler(0), getCallbackHandler(1));

        restoreMainWindowSettings();

        pack();

        updateStates();

        //Make dragging a little faster but perhaps uglier.
        // mdiPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        // Update title bars with emulator statistics every second
        new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateStatusBar(Constants.CHIP_FR);
                updateStatusBar(Constants.CHIP_TX);
            }
        }).start();
    }


    private void saveMainWindowSettings() {
        prefs.setMainWindowPosition(getX(), getY());
        prefs.setMainWindowSize(getWidth(), getHeight());

        prefs.setDividerLocation(splitPane.getDividerLocation());
        prefs.setLastDividerLocation(splitPane.getLastDividerLocation());
        prefs.setDividerKeepHidden(getKeepHidden(splitPane));
    }

    private void restoreMainWindowSettings() {
        if (prefs.getMainWindowSizeX() == 0) {
            //Settings were never saved. Make the app window indented 50 pixels from each edge of the screen.
            int inset = 50;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = screenSize.width - inset * 2;
            int height = screenSize.height - inset * 2;
            setLocation(inset, inset);
            setPreferredSize(new Dimension(width, height));
        }
        else {
            setLocation(prefs.getMainWindowPositionX(), prefs.getMainWindowPositionY());
            setPreferredSize(new Dimension(prefs.getMainWindowSizeX(), prefs.getMainWindowSizeY()));

            splitPane.setDividerLocation(prefs.getDividerLocation());
            splitPane.setLastDividerLocation(prefs.getLastDividerLocation());
            setKeepHidden(splitPane, prefs.isDividerKeepHidden());
        }
    }

    // TODO instead of having the next two methods here, use a custom JFoldableSplitPane extends JSplitPane, and give it two methods void setFolded(boolean folded) and boolean isFolded()

    /**
     * Method circumventing package access to setKeepHidden() method of BasicSplitPaneUI
     * @param splitPane
     * @param keepHidden
     * @author taken from http://java-swing-tips.googlecode.com/svn/trunk/OneTouchExpandable/src/java/example/MainPanel.java
     */
    private void setKeepHidden(JSplitPane splitPane, boolean keepHidden) {
        if (splitPane.getUI() instanceof BasicSplitPaneUI) {
            try {
                Method setKeepHidden = BasicSplitPaneUI.class.getDeclaredMethod("setKeepHidden", new Class<?>[]{Boolean.TYPE}); //boolean.class });
                setKeepHidden.setAccessible(true);
                setKeepHidden.invoke(splitPane.getUI(), new Object[]{keepHidden});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method circumventing package access to getKeepHidden() method of BasicSplitPaneUI
     * @param splitPane
     * @return true if one panel is hidden
     * @author inspired by http://java-swing-tips.googlecode.com/svn/trunk/OneTouchExpandable/src/java/example/MainPanel.java
     */
    private boolean getKeepHidden(JSplitPane splitPane) {
        if (splitPane.getUI() instanceof BasicSplitPaneUI) {
            try {
                //noinspection RedundantArrayCreation
                Method getKeepHidden = BasicSplitPaneUI.class.getDeclaredMethod("getKeepHidden", new Class<?>[]{});
                getKeepHidden.setAccessible(true);
                return (Boolean) (getKeepHidden.invoke(splitPane.getUI()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void applyPrefsToUI() {
        if (BUTTON_SIZE_LARGE.equals(prefs.getButtonSize())) {
            toolbarButtonMargin.set(2, 14, 2, 14);
        }
        else {
            toolbarButtonMargin.set(0, 0, 0, 0);
        }
        if (BUTTON_SIZE_SMALL.equals(prefs.getButtonSize())) {
            //iterate on buttons and resize them to 16x16
            for (int chip = 0; chip < 2; chip++) {
                for (Component component : toolBar[chip].getComponents()) {
                    if (component instanceof JButton) {
                        JButton button = (JButton) component;
                        ImageIcon icon = (ImageIcon) button.getClientProperty(BUTTON_PROPERTY_KEY_ICON);
                        if (icon != null) {
                            Image newImg = icon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
                            button.setIcon(new ImageIcon(newImg));
                        }
                    }
                }
            }
        }
        else {
            //iterate on buttons and revert to original icon
            for (int chip = 0; chip < 2; chip++) {
                for (Component component : toolBar[chip].getComponents()) {
                    if (component instanceof JButton) {
                        JButton button = (JButton) component;
                        ImageIcon icon = (ImageIcon) button.getClientProperty(BUTTON_PROPERTY_KEY_ICON);
                        if (icon != null) {
                            button.setIcon(icon);
                        }
                    }
                }
            }
        }
        initProgrammableTimerAnimationIcons(prefs.getButtonSize());
        for (int chip = 0; chip < 2; chip++) {
            toolBar[chip].revalidate();
        }
    }

    public Prefs getPrefs() {
        return prefs;
    }

    public EmulationFramework getFramework() {
        return framework;
    }

    public void setStatusText(int chip, String message) {
        statusText[chip] = message;
        updateStatusBar(chip);
    }

    private void updateStatusBar(final int chip) {
        // Updates UI. Make sure it runs in the event dispatch thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (framework.getEmulator(chip) != null) {
                    long totalCycles = framework.getEmulator(chip).getTotalCycles();
                    long now = System.currentTimeMillis();
                    long cps;
                    try {
                        cps = (1000 * (totalCycles - lastUpdateCycles[chip])) / (now - lastUpdateTime[chip]);
                    } catch (Exception e) {
                        cps = -1;
                    }

                    lastUpdateCycles[chip] = totalCycles;
                    lastUpdateTime[chip] = now;
                    statusBar[chip].setText(statusText[chip] + " (" + framework.getMasterClock().getFormatedTotalElapsedTimeMs() + " or " + totalCycles + " cycles emulated. Current speed is " + (cps < 0 ? "?" : ("" + cps)) + "cps)");
                }
                else {
                    statusBar[chip].setText(statusText[chip]);
                }
            }
        });
    }

    private JPanel createToolBar(int chip) {
        JPanel bar = new JPanel();

        bar.setLayout(new ModifiedFlowLayout(FlowLayout.LEFT, 0, 0));

        loadButton[chip] = makeButton("load", COMMAND_IMAGE_LOAD[chip], "Load " + Constants.CHIP_LABEL[chip] + " image", "Load");
        bar.add(loadButton[chip]);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        playButton[chip] = makeButton("play", COMMAND_EMULATOR_PLAY[chip], "Start or resume " + Constants.CHIP_LABEL[chip] + " emulator", "Play");
        bar.add(playButton[chip]);
        debugButton[chip] = makeButton("debug", COMMAND_EMULATOR_DEBUG[chip], "Debug " + Constants.CHIP_LABEL[chip] + " emulator", "Debug");
        bar.add(debugButton[chip]);
        pauseButton[chip] = makeButton("pause", COMMAND_EMULATOR_PAUSE[chip], "Pause " + Constants.CHIP_LABEL[chip] + " emulator", "Pause");
        bar.add(pauseButton[chip]);
        stepButton[chip] = makeButton("step", COMMAND_EMULATOR_STEP[chip], "Step " + Constants.CHIP_LABEL[chip] + " emulator", "Step");
        bar.add(stepButton[chip]);
        stopButton[chip] = makeButton("stop", COMMAND_EMULATOR_STOP[chip], "Stop " + Constants.CHIP_LABEL[chip] + " emulator and reset", "Stop");
        bar.add(stopButton[chip]);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        breakpointButton[chip] = makeButton("breakpoint", COMMAND_SETUP_BREAKPOINTS[chip], "Setup " + Constants.CHIP_LABEL[chip] + " breakpoints", "Breakpoints");
        bar.add(breakpointButton[chip]);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));
        bar.add(new JLabel("Sleep :"));
        bar.add(Box.createRigidArea(new Dimension(10, 0)));
        bar.add(makeSlider(chip));
        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        cpuStateButton[chip] = makeButton("cpu", COMMAND_TOGGLE_CPUSTATE_WINDOW[chip], Constants.CHIP_LABEL[chip] + " CPU state window", "CPU");
        bar.add(cpuStateButton[chip]);
        memoryHexEditorButton[chip] = makeButton("memory_editor", COMMAND_TOGGLE_MEMORY_HEX_EDITOR[chip], Constants.CHIP_LABEL[chip] + " memory hex editor", "Hex Editor");
        bar.add(memoryHexEditorButton[chip]);
        interruptControllerButton[chip] = makeButton("interrupt", COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW[chip], Constants.CHIP_LABEL[chip] + " interrupt controller", "Interrupt");
        bar.add(interruptControllerButton[chip]);
        programmableTimersButton[chip] = makeButton("timer", COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW[chip], Constants.CHIP_LABEL[chip] + " programmable timers", "Programmable timers");
        bar.add(programmableTimersButton[chip]);
        serialInterfacesButton[chip] = makeButton("serial", COMMAND_TOGGLE_SERIAL_INTERFACES[chip], Constants.CHIP_LABEL[chip] + " serial interfaces", "Serial interfaces");
        bar.add(serialInterfacesButton[chip]);
        ioPortsButton[chip] = makeButton("io", COMMAND_TOGGLE_IO_PORTS_WINDOW[chip], Constants.CHIP_LABEL[chip] + " I/O Ports", "I/O Ports");
        bar.add(ioPortsButton[chip]);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        if (chip == Constants.CHIP_FR) {
            screenEmulatorButton = makeButton("screen", COMMAND_TOGGLE_SCREEN_EMULATOR, "Screen emulator", "Screen");
            bar.add(screenEmulatorButton);
            component4006Button = makeButton("4006", COMMAND_TOGGLE_COMPONENT_4006_WINDOW, "Component 4006", "Component 4006");
            bar.add(component4006Button);
        }
        else {
            serialDevicesButton[Constants.CHIP_TX] = makeButton("serial_devices", COMMAND_TOGGLE_SERIAL_DEVICES[Constants.CHIP_TX], Constants.CHIP_LABEL[Constants.CHIP_TX] + " serial devices", "Serial devices");
            bar.add(serialDevicesButton[Constants.CHIP_TX]);
            adConverterButton[Constants.CHIP_TX] = makeButton("ad_converter", COMMAND_TOGGLE_AD_CONVERTER[Constants.CHIP_TX], Constants.CHIP_LABEL[Constants.CHIP_TX] + " A/D converter", "A/D converter");
            bar.add(adConverterButton[Constants.CHIP_TX]);
            frontPanelButton = makeButton("front_panel", COMMAND_TOGGLE_FRONT_PANEL, "Front Panel", "Front Panel");
            bar.add(frontPanelButton);
        }

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        disassemblyButton[chip] = makeButton("disassembly_log", COMMAND_TOGGLE_DISASSEMBLY_WINDOW[chip], "Real time " + Constants.CHIP_LABEL[chip] + " disassembly log", "Disassembly");
        bar.add(disassemblyButton[chip]);
        memoryActivityViewerButton[chip] = makeButton("memory_activity", COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER[chip], Constants.CHIP_LABEL[chip] + " memory activity viewer", "Activity");
        bar.add(memoryActivityViewerButton[chip]);
        customMemoryRangeLoggerButton[chip] = makeButton("custom_logger", COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW[chip], "Custom " + Constants.CHIP_LABEL[chip] + " logger", "Custom logger");
        bar.add(customMemoryRangeLoggerButton[chip]);
        callStackButton[chip] = makeButton("call_stack", COMMAND_TOGGLE_CALL_STACK_WINDOW[chip], Constants.CHIP_LABEL[chip] + " call stack logger window", "CallStack");
        bar.add(callStackButton[chip]);
        iTronObjectButton[chip] = makeButton("os", COMMAND_TOGGLE_ITRON_OBJECT_WINDOW[chip], Constants.CHIP_LABEL[chip] + " µITRON object window", "µITRON Object");
        bar.add(iTronObjectButton[chip]);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        analyseButton[chip] = makeButton("analyse", COMMAND_ANALYSE_DISASSEMBLE[chip], Constants.CHIP_LABEL[chip] + " Analyse/Disassemble", "Analyse");
        bar.add(analyseButton[chip]);
        codeStructureButton[chip] = makeButton("code_structure", COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW[chip], Constants.CHIP_LABEL[chip] + " Code Structure", "Structure");
        bar.add(codeStructureButton[chip]);
        sourceCodeButton[chip] = makeButton("source", COMMAND_TOGGLE_SOURCE_CODE_WINDOW[chip], Constants.CHIP_LABEL[chip] + " Source code", "Source");
        bar.add(sourceCodeButton[chip]);

        bar.add(Box.createHorizontalGlue());

        saveLoadMemoryButton[chip] = makeButton("save_load_memory", COMMAND_SAVE_LOAD_MEMORY[chip], "Save/Load " + Constants.CHIP_LABEL[chip] + " memory area", "Save/Load memory");
        bar.add(saveLoadMemoryButton[chip]);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        chipOptionsButton[chip] = makeButton("options", COMMAND_CHIP_OPTIONS[chip], Constants.CHIP_LABEL[chip] + " options", "Options");
        bar.add(chipOptionsButton[chip]);

        return bar;
    }

    private JSlider makeSlider(int chip) {
        intervalSlider[chip] = new JSlider(JSlider.HORIZONTAL, 0, 5, prefs.getSleepTick(chip));

        intervalSlider[chip].addChangeListener(new ChangeListener() {
            /**
             * React to slider moves
             * @param e
             */
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                // if (!source.getValueIsAdjusting())
                for (int chip = 0; chip < 2; chip++) {
                    if (source == intervalSlider[chip]) {
                        setEmulatorSleepCode(chip, source.getValue());
                        prefs.setSleepTick( chip, source.getValue());
                    }
                }
            }
        });

        intervalSlider[chip].putClientProperty("JComponent.sizeVariant", "large");

        //Create the label table
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(0, getSmallLabel("0"));
        labelTable.put(1, getSmallLabel("1ms"));
        labelTable.put(2, getSmallLabel("10ms"));
        labelTable.put(3, getSmallLabel("0.1s"));
        labelTable.put(4, getSmallLabel("1s"));
        labelTable.put(5, getSmallLabel("10s"));
        intervalSlider[chip].setLabelTable(labelTable);

        //Turn on labels at major tick marks.
        intervalSlider[chip].setMajorTickSpacing(1);
        intervalSlider[chip].setPaintLabels(true);
        intervalSlider[chip].setPaintTicks(true);
        intervalSlider[chip].setSnapToTicks(true);

        intervalSlider[chip].setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        intervalSlider[chip].setMaximumSize(new Dimension(400, 50));

        return intervalSlider[chip];
    }

    private JLabel getSmallLabel(String s) {
        JLabel jLabel = new JLabel(s);
        jLabel.setFont(new Font("Arial", Font.PLAIN, 8));
        return jLabel;
    }

    protected JButton makeButton(String imageName, String actionCommand, String toolTipText, String altText) {
        //Look for the image.
        String imgLocation = "images/" + imageName + ".png";
        URL imageURL = EmulatorUI.class.getResource(imgLocation);

        //Create and initialize the button.
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        button.setMargin(toolbarButtonMargin);

        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL, altText);
            button.putClientProperty(BUTTON_PROPERTY_KEY_ICON, icon);
            button.setIcon(icon);
        } else {
            button.setText(altText);
            System.err.println("Resource not found: " + imgLocation);
        }

        return button;
    }


    @SuppressWarnings("MagicConstant")
    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenuItem tmpMenuItem;

        //Set up the file menu.
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        //load image
        for (int chip = 0; chip < 2; chip++) {
            loadMenuItem[chip] = new JMenuItem("Load " + Constants.CHIP_LABEL[chip] + " firmware image");
            if (chip == Constants.CHIP_FR) loadMenuItem[chip].setMnemonic(KEY_EVENT_LOAD);
            loadMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_LOAD, KEY_CHIP_MODIFIER[chip]));
            loadMenuItem[chip].setActionCommand(COMMAND_IMAGE_LOAD[chip]);
            loadMenuItem[chip].addActionListener(this);
            fileMenu.add(loadMenuItem[chip]);
        }

        fileMenu.add(new JSeparator());

        //decoder
        tmpMenuItem = new JMenuItem("Decode firmware");
        tmpMenuItem.setMnemonic(KeyEvent.VK_D);
        tmpMenuItem.setActionCommand(COMMAND_DECODE);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);

        //encoder
        tmpMenuItem = new JMenuItem("Encode firmware (alpha)");
        tmpMenuItem.setMnemonic(KeyEvent.VK_E);
        tmpMenuItem.setActionCommand(COMMAND_ENCODE);
        tmpMenuItem.addActionListener(this);
//        fileMenu.add(tmpMenuItem);

        fileMenu.add(new JSeparator());

        //decoder
        tmpMenuItem = new JMenuItem("Decode lens correction data");
        //tmpMenuItem.setMnemonic(KeyEvent.VK_D);
        tmpMenuItem.setActionCommand(COMMAND_DECODE_NKLD);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);

        fileMenu.add(new JSeparator());

        //Save state
        tmpMenuItem = new JMenuItem("Save state");
        tmpMenuItem.setActionCommand(COMMAND_SAVE_STATE);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);

        //Load state
        tmpMenuItem = new JMenuItem("Load state");
        tmpMenuItem.setActionCommand(COMMAND_LOAD_STATE);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);

        fileMenu.add(new JSeparator());

        //quit
        tmpMenuItem = new JMenuItem("Quit");
        tmpMenuItem.setMnemonic(KEY_EVENT_QUIT);
        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_QUIT, ActionEvent.ALT_MASK));
        tmpMenuItem.setActionCommand(COMMAND_QUIT);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);


        //Set up the run menu.
        JMenu runMenu = new JMenu("Run");
        runMenu.setMnemonic(KeyEvent.VK_R);
        menuBar.add(runMenu);


        for (int chip = 0; chip < 2; chip++) {
            //emulator play
            playMenuItem[chip] = new JMenuItem("Start (or resume) " + Constants.CHIP_LABEL[chip] + " emulator");
            playMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_RUN[chip], ActionEvent.ALT_MASK));
            playMenuItem[chip].setActionCommand(COMMAND_EMULATOR_PLAY[chip]);
            playMenuItem[chip].addActionListener(this);
            runMenu.add(playMenuItem[chip]);

            //emulator debug
            debugMenuItem[chip] = new JMenuItem("Debug " + Constants.CHIP_LABEL[chip] + " emulator");
            debugMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_DEBUG[chip], ActionEvent.ALT_MASK));
            debugMenuItem[chip].setActionCommand(COMMAND_EMULATOR_DEBUG[chip]);
            debugMenuItem[chip].addActionListener(this);
            runMenu.add(debugMenuItem[chip]);

            //emulator pause
            pauseMenuItem[chip] = new JMenuItem("Pause " + Constants.CHIP_LABEL[chip] + " emulator");
            pauseMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_PAUSE[chip], ActionEvent.ALT_MASK));
            pauseMenuItem[chip].setActionCommand(COMMAND_EMULATOR_PAUSE[chip]);
            pauseMenuItem[chip].addActionListener(this);
            runMenu.add(pauseMenuItem[chip]);

            //emulator step
            stepMenuItem[chip] = new JMenuItem("Step " + Constants.CHIP_LABEL[chip] + " emulator");
            stepMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_STEP[chip], ActionEvent.ALT_MASK));
            stepMenuItem[chip].setActionCommand(COMMAND_EMULATOR_STEP[chip]);
            stepMenuItem[chip].addActionListener(this);
            runMenu.add(stepMenuItem[chip]);

            //emulator stop
            stopMenuItem[chip] = new JMenuItem("Stop and reset " + Constants.CHIP_LABEL[chip] + " emulator");
            stopMenuItem[chip].setActionCommand(COMMAND_EMULATOR_STOP[chip]);
            stopMenuItem[chip].addActionListener(this);
            runMenu.add(stopMenuItem[chip]);

            runMenu.add(new JSeparator());

            //setup breakpoints
            breakpointMenuItem[chip] = new JMenuItem("Setup " + Constants.CHIP_LABEL[chip] + " breakpoints");
            breakpointMenuItem[chip].setActionCommand(COMMAND_SETUP_BREAKPOINTS[chip]);
            breakpointMenuItem[chip].addActionListener(this);
            runMenu.add(breakpointMenuItem[chip]);

            if (chip == Constants.CHIP_FR) {
                runMenu.add(new JSeparator());
            }
        }

        //Set up the components menu.
        JMenu componentsMenu = new JMenu("Components");
        componentsMenu.setMnemonic(KeyEvent.VK_O);
        menuBar.add(componentsMenu);

        for (int chip = 0; chip < 2; chip++) {
            //CPU state
            cpuStateMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " CPU State window");
            if (chip == Constants.CHIP_FR) cpuStateMenuItem[chip].setMnemonic(KEY_EVENT_CPUSTATE);
            cpuStateMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_CPUSTATE, KEY_CHIP_MODIFIER[chip]));
            cpuStateMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CPUSTATE_WINDOW[chip]);
            cpuStateMenuItem[chip].addActionListener(this);
            componentsMenu.add(cpuStateMenuItem[chip]);

            //memory hex editor
            memoryHexEditorMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " Memory hex editor");
            if (chip == Constants.CHIP_FR) memoryHexEditorMenuItem[chip].setMnemonic(KEY_EVENT_MEMORY);
            memoryHexEditorMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_MEMORY, KEY_CHIP_MODIFIER[chip]));
            memoryHexEditorMenuItem[chip].setActionCommand(COMMAND_TOGGLE_MEMORY_HEX_EDITOR[chip]);
            memoryHexEditorMenuItem[chip].addActionListener(this);
            componentsMenu.add(memoryHexEditorMenuItem[chip]);

            //Interrupt controller
            interruptControllerMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " interrupt controller");
            interruptControllerMenuItem[chip].setActionCommand(COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW[chip]);
            interruptControllerMenuItem[chip].addActionListener(this);
            componentsMenu.add(interruptControllerMenuItem[chip]);

            //Programmble timers
            programmableTimersMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " programmable timers");
            programmableTimersMenuItem[chip].setActionCommand(COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW[chip]);
            programmableTimersMenuItem[chip].addActionListener(this);
            componentsMenu.add(programmableTimersMenuItem[chip]);

            //Serial interface
            serialInterfacesMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " serial interfaces");
            serialInterfacesMenuItem[chip].setActionCommand(COMMAND_TOGGLE_SERIAL_INTERFACES[chip]);
            serialInterfacesMenuItem[chip].addActionListener(this);
            componentsMenu.add(serialInterfacesMenuItem[chip]);

            // I/O
            ioPortsMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " I/O ports");
            ioPortsMenuItem[chip].setActionCommand(COMMAND_TOGGLE_IO_PORTS_WINDOW[chip]);
            ioPortsMenuItem[chip].addActionListener(this);
            componentsMenu.add(ioPortsMenuItem[chip]);

            componentsMenu.add(new JSeparator());
        }

        //screen emulator: FR80 only
        screenEmulatorMenuItem = new JCheckBoxMenuItem("Screen emulator (FR only)");
        screenEmulatorMenuItem.setMnemonic(KEY_EVENT_SCREEN);
        screenEmulatorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_SCREEN, ActionEvent.ALT_MASK));
        screenEmulatorMenuItem.setActionCommand(COMMAND_TOGGLE_SCREEN_EMULATOR);
        screenEmulatorMenuItem.addActionListener(this);
        componentsMenu.add(screenEmulatorMenuItem);

        //Component 4006: FR80 only
        component4006MenuItem = new JCheckBoxMenuItem("Component 4006 window (FR only)");
        component4006MenuItem.setMnemonic(KeyEvent.VK_4);
        component4006MenuItem.setActionCommand(COMMAND_TOGGLE_COMPONENT_4006_WINDOW);
        component4006MenuItem.addActionListener(this);
        componentsMenu.add(component4006MenuItem);

        componentsMenu.add(new JSeparator());

        //Serial devices: TX19 only for now
        serialDevicesMenuItem[Constants.CHIP_TX] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[Constants.CHIP_TX] + " serial devices (TX only)");
        serialDevicesMenuItem[Constants.CHIP_TX].setActionCommand(COMMAND_TOGGLE_SERIAL_DEVICES[Constants.CHIP_TX]);
        serialDevicesMenuItem[Constants.CHIP_TX].addActionListener(this);
        componentsMenu.add(serialDevicesMenuItem[Constants.CHIP_TX]);

        //A/D converter: TX19 only for now
        adConverterMenuItem[Constants.CHIP_TX] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[Constants.CHIP_TX] + " A/D converter (TX only)");
        adConverterMenuItem[Constants.CHIP_TX].setActionCommand(COMMAND_TOGGLE_AD_CONVERTER[Constants.CHIP_TX]);
        adConverterMenuItem[Constants.CHIP_TX].addActionListener(this);
        componentsMenu.add(adConverterMenuItem[Constants.CHIP_TX]);

        //Front panel: TX19 only
        frontPanelMenuItem = new JCheckBoxMenuItem("Front panel (TX only)");
        frontPanelMenuItem.setActionCommand(COMMAND_TOGGLE_FRONT_PANEL);
        frontPanelMenuItem.addActionListener(this);
        componentsMenu.add(frontPanelMenuItem);

        //Set up the trace menu.
        JMenu traceMenu = new JMenu("Trace");
        traceMenu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(traceMenu);

        for (int chip = 0; chip < 2; chip++) {
            //memory activity viewer
            memoryActivityViewerMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " Memory activity viewer");
            memoryActivityViewerMenuItem[chip].setActionCommand(COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER[chip]);
            memoryActivityViewerMenuItem[chip].addActionListener(this);
            traceMenu.add(memoryActivityViewerMenuItem[chip]);

            //disassembly
            disassemblyMenuItem[chip] = new JCheckBoxMenuItem("Real-time " + Constants.CHIP_LABEL[chip] + " disassembly log");
            if (chip == Constants.CHIP_FR) disassemblyMenuItem[chip].setMnemonic(KEY_EVENT_REALTIME_DISASSEMBLY);
            disassemblyMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_REALTIME_DISASSEMBLY, KEY_CHIP_MODIFIER[chip]));
            disassemblyMenuItem[chip].setActionCommand(COMMAND_TOGGLE_DISASSEMBLY_WINDOW[chip]);
            disassemblyMenuItem[chip].addActionListener(this);
            traceMenu.add(disassemblyMenuItem[chip]);

            //Custom logger
            customMemoryRangeLoggerMenuItem[chip] = new JCheckBoxMenuItem("Custom " + Constants.CHIP_LABEL[chip] + " logger window");
            customMemoryRangeLoggerMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW[chip]);
            customMemoryRangeLoggerMenuItem[chip].addActionListener(this);
            traceMenu.add(customMemoryRangeLoggerMenuItem[chip]);

            //Call Stack logger
            callStackMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " Call stack logger");
            callStackMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CALL_STACK_WINDOW[chip]);
            callStackMenuItem[chip].addActionListener(this);
            traceMenu.add(callStackMenuItem[chip]);

            //µITRON Object
            iTronObjectMenuItem[chip] = new JCheckBoxMenuItem("µITRON " + Constants.CHIP_LABEL[chip] + " Objects");
            iTronObjectMenuItem[chip].setActionCommand(COMMAND_TOGGLE_ITRON_OBJECT_WINDOW[chip]);
            iTronObjectMenuItem[chip].addActionListener(this);
            traceMenu.add(iTronObjectMenuItem[chip]);

            //µITRON Return Stack
            iTronReturnStackMenuItem[chip] = new JCheckBoxMenuItem("µITRON " + Constants.CHIP_LABEL[chip] + " Return stack");
            iTronReturnStackMenuItem[chip].setActionCommand(COMMAND_TOGGLE_ITRON_RETURN_STACK_WINDOW[chip]);
            iTronReturnStackMenuItem[chip].addActionListener(this);
            traceMenu.add(iTronReturnStackMenuItem[chip]);

            traceMenu.add(new JSeparator());
        }

        //Set up the source menu.
        JMenu sourceMenu = new JMenu("Source");
        sourceMenu.setMnemonic(KEY_EVENT_SCREEN);
        menuBar.add(sourceMenu);

        // FR syscall symbols
        generateSysSymbolsMenuItem = new JMenuItem("Generate " + Constants.CHIP_LABEL[Constants.CHIP_FR] + " system call symbols");
        generateSysSymbolsMenuItem.setActionCommand(COMMAND_GENERATE_SYS_SYMBOLS);
        generateSysSymbolsMenuItem.addActionListener(this);
        sourceMenu.add(generateSysSymbolsMenuItem);

        for (int chip = 0; chip < 2; chip++) {

            sourceMenu.add(new JSeparator());

            //analyse / disassemble
            analyseMenuItem[chip] = new JMenuItem("Analyse / Disassemble " + Constants.CHIP_LABEL[chip] + " code");
            analyseMenuItem[chip].setActionCommand(COMMAND_ANALYSE_DISASSEMBLE[chip]);
            analyseMenuItem[chip].addActionListener(this);
            sourceMenu.add(analyseMenuItem[chip]);

            sourceMenu.add(new JSeparator());

            //code structure
            codeStructureMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " code structure");
            codeStructureMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW[chip]);
            codeStructureMenuItem[chip].addActionListener(this);
            sourceMenu.add(codeStructureMenuItem[chip]);

            //source code
            sourceCodeMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " source code");
            if (chip == Constants.CHIP_FR) sourceCodeMenuItem[chip].setMnemonic(KEY_EVENT_SOURCE);
            sourceCodeMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KEY_EVENT_SOURCE, KEY_CHIP_MODIFIER[chip]));
            sourceCodeMenuItem[chip].setActionCommand(COMMAND_TOGGLE_SOURCE_CODE_WINDOW[chip]);
            sourceCodeMenuItem[chip].addActionListener(this);
            sourceMenu.add(sourceCodeMenuItem[chip]);

            if (chip == Constants.CHIP_FR) {
                sourceMenu.add(new JSeparator());
            }
        }


        //Set up the tools menu.
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(toolsMenu);

        for (int chip = 0; chip < 2; chip++) {
            // save/load memory area
            saveLoadMemoryMenuItem[chip] = new JMenuItem("Save/Load " + Constants.CHIP_LABEL[chip] + " memory area");
            saveLoadMemoryMenuItem[chip].setActionCommand(COMMAND_SAVE_LOAD_MEMORY[chip]);
            saveLoadMemoryMenuItem[chip].addActionListener(this);
            toolsMenu.add(saveLoadMemoryMenuItem[chip]);

            //chip options
            chipOptionsMenuItem[chip] = new JMenuItem(Constants.CHIP_LABEL[chip] + " options");
            chipOptionsMenuItem[chip].setActionCommand(COMMAND_CHIP_OPTIONS[chip]);
            chipOptionsMenuItem[chip].addActionListener(this);
            toolsMenu.add(chipOptionsMenuItem[chip]);

            toolsMenu.add(new JSeparator());

        }

        //disassembly options
        uiOptionsMenuItem = new JMenuItem("Preferences");
        uiOptionsMenuItem.setActionCommand(COMMAND_UI_OPTIONS);
        uiOptionsMenuItem.addActionListener(this);
        toolsMenu.add(uiOptionsMenuItem);

        //Set up the help menu.
        JMenu helpMenu = new JMenu("?");
        menuBar.add(helpMenu);

        //about
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setActionCommand(COMMAND_ABOUT);
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);

//        JMenuItem testMenuItem = new JMenuItem("Test");
//        testMenuItem.setActionCommand(COMMAND_TEST);
//        testMenuItem.addActionListener(this);
//        helpMenu.add(testMenuItem);

        // Global "Keep in sync" setting
        menuBar.add(Box.createHorizontalGlue());
        final JCheckBox syncEmulators = new JCheckBox("Keep emulators in sync");
        syncEmulators.setSelected(prefs.isSyncPlay());
        framework.getMasterClock().setSyncPlay(prefs.isSyncPlay());
        syncEmulators.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.setSyncPlay(syncEmulators.isSelected());
                framework.getMasterClock().setSyncPlay(syncEmulators.isSelected());
            }
        });
        menuBar.add(syncEmulators);
        return menuBar;
    }

    // Event listeners

    /**
     * React to menu selections and toggle buttons.
     *
     * @param e the event
     */
    public void actionPerformed(ActionEvent e) {
        int chip;

        if ((chip = getChipCommandMatchingAction(e, COMMAND_IMAGE_LOAD)) != Constants.CHIP_NONE) {
            openLoadImageDialog(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_ANALYSE_DISASSEMBLE)) != Constants.CHIP_NONE) {
            openAnalyseDialog(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_PLAY)) != Constants.CHIP_NONE) {
            startEmulator(chip, EmulationFramework.ExecutionMode.RUN, null);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_DEBUG)) != Constants.CHIP_NONE) {
            startEmulator(chip, EmulationFramework.ExecutionMode.DEBUG, null);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_PAUSE)) != Constants.CHIP_NONE) {
            framework.pauseEmulator(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_STEP)) != Constants.CHIP_NONE) {
            startEmulator(chip, EmulationFramework.ExecutionMode.STEP, null);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_STOP)) != Constants.CHIP_NONE) {
            if (framework.isEmulatorPlaying(chip)) {
                framework.pauseEmulator(chip);
            }
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to reset the " + Constants.CHIP_LABEL[chip] + " emulator and lose the current state ?", "Reset ?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                framework.stopEmulator(chip);
                reset(chip);
            }
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_SETUP_BREAKPOINTS)) != Constants.CHIP_NONE) {
            toggleBreakTriggerList(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_MEMORY_HEX_EDITOR)) != Constants.CHIP_NONE) {
            toggleMemoryHexEditor(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER)) != Constants.CHIP_NONE) {
            toggleMemoryActivityViewer(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_DISASSEMBLY_WINDOW)) != Constants.CHIP_NONE) {
            toggleDisassemblyLog(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_CPUSTATE_WINDOW)) != Constants.CHIP_NONE) {
            toggleCPUState(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW)) != Constants.CHIP_NONE) {
            toggleCustomMemoryRangeLoggerComponentFrame(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW)) != Constants.CHIP_NONE) {
            toggleProgrammableTimersWindow(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_CALL_STACK_WINDOW)) != Constants.CHIP_NONE) {
            toggleCallStack(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_ITRON_OBJECT_WINDOW)) != Constants.CHIP_NONE) {
            toggleITronObject(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_ITRON_RETURN_STACK_WINDOW)) != Constants.CHIP_NONE) {
            toggleITronReturnStack(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_SAVE_LOAD_MEMORY)) != Constants.CHIP_NONE) {
            openSaveLoadMemoryDialog(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW)) != Constants.CHIP_NONE) {
            toggleCodeStructureWindow(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_SOURCE_CODE_WINDOW)) != Constants.CHIP_NONE) {
            toggleSourceCodeWindow(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW)) != Constants.CHIP_NONE) {
            toggleInterruptController(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_SERIAL_INTERFACES)) != Constants.CHIP_NONE) {
            toggleSerialInterfaces(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_SERIAL_DEVICES)) != Constants.CHIP_NONE) {
            toggleGenericSerialFrame(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_IO_PORTS_WINDOW)) != Constants.CHIP_NONE) {
            toggleIoPortsWindow(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_AD_CONVERTER)) != Constants.CHIP_NONE) {
            toggleAdConverterFrame(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_CHIP_OPTIONS)) != Constants.CHIP_NONE) {
            openChipOptionsDialog(chip);
        }
        else if (COMMAND_GENERATE_SYS_SYMBOLS.equals(e.getActionCommand())) {
            openGenerateSysSymbolsDialog();
        }
        else if (COMMAND_UI_OPTIONS.equals(e.getActionCommand())) {
            openUIOptionsDialog();
        }
        else if (COMMAND_DECODE.equals(e.getActionCommand())) {
            openDecodeDialog();
        }
        else if (COMMAND_ENCODE.equals(e.getActionCommand())) {
            openEncodeDialog();
        }
        else if (COMMAND_DECODE_NKLD.equals(e.getActionCommand())) {
            openDecodeNkldDialog();
        }
        else if (COMMAND_LOAD_STATE.equals(e.getActionCommand())) {
            loadState();
        }
        else if (COMMAND_SAVE_STATE.equals(e.getActionCommand())) {
            saveState();
        }
        else if (COMMAND_TOGGLE_SCREEN_EMULATOR.equals(e.getActionCommand())) {
            toggleScreenEmulator();
        }
        else if (COMMAND_TOGGLE_COMPONENT_4006_WINDOW.equals(e.getActionCommand())) {
            toggleComponent4006();
        }

        else if (COMMAND_TOGGLE_FRONT_PANEL.equals(e.getActionCommand())) {
            toggleFrontPanel();
        }

        else if (COMMAND_QUIT.equals(e.getActionCommand())) {
            quit();
        }
        else if (COMMAND_ABOUT.equals(e.getActionCommand())) {
            showAboutDialog();
        }
        else if (COMMAND_TEST.equals(e.getActionCommand())) {
            // noop
        }
        else {
            System.err.println("Unknown menu command : " + e.getActionCommand());
        }
    }

    private int getChipCommandMatchingAction(ActionEvent e, String[] commands) {
        for (int chip = 0; chip < 2; chip++) {
            if (commands[chip].equals(e.getActionCommand())) {
                return chip;
            }
        }
        return -1;
    }

    private void openSaveLoadMemoryDialog(int chip) {
        new SaveLoadMemoryDialog(this, framework.getPlatform(chip).getMemory()).setVisible(true);
    }


    private void openDecodeDialog() {
        JTextField sourceFile = new JTextField();
        JTextField destinationDir = new JTextField();
        FileSelectionPanel sourceFileSelectionPanel = new FileSelectionPanel("Source file", sourceFile, false);
        sourceFileSelectionPanel.setFileFilter(".bin", "Firmware file (*.bin)");
        final JComponent[] inputs = new JComponent[]{
                sourceFileSelectionPanel,
                new FileSelectionPanel("Destination dir", destinationDir, true)
        };
        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                inputs,
                "Choose source file and destination dir",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION)) {
            try {
                new FirmwareDecoder().decode(sourceFile.getText(), destinationDir.getText(), false);
                JOptionPane.showMessageDialog(this, "Decoding complete", "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (FirmwareFormatException e) {
                JOptionPane.showMessageDialog(this, e.getMessage() + "\nPlease see console for full stack trace", "Error decoding files", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void openEncodeDialog() {
        JTextField destinationFile = new JTextField();
        JTextField sourceFile1 = new JTextField();
        JTextField sourceFile2 = new JTextField();
        FileSelectionPanel destinationFileSelectionPanel = new FileSelectionPanel("Destination file", destinationFile, false);
        destinationFileSelectionPanel.setFileFilter(".bin", "Encoded firmware file (*.bin)");
        FileSelectionPanel sourceFile1SelectionPanel = new FileSelectionPanel("Source file 1", sourceFile1, false);
        destinationFileSelectionPanel.setFileFilter("a*.bin", "Decoded A firmware file (a*.bin)");
        FileSelectionPanel sourceFile2SelectionPanel = new FileSelectionPanel("Source file 2", sourceFile2, false);
        destinationFileSelectionPanel.setFileFilter("b*.bin", "Decoded B firmware file (b*.bin)");
        final JComponent[] inputs = new JComponent[]{
                destinationFileSelectionPanel,
                sourceFile1SelectionPanel,
                sourceFile2SelectionPanel
        };
        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                inputs,
                "Choose target encoded file and source files",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION)) {
            try {
                ArrayList<String> inputFilenames = new ArrayList<String>();
                inputFilenames.add(sourceFile1.getText());
                inputFilenames.add(sourceFile2.getText());
                new FirmwareEncoder().encode(inputFilenames, destinationFile.getText());
                JOptionPane.showMessageDialog(this, "Encoding complete", "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (FirmwareFormatException e) {
                JOptionPane.showMessageDialog(this, e.getMessage() + "\nPlease see console for full stack trace", "Error encoding files", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void openDecodeNkldDialog() {
        JTextField sourceFile = new JTextField();
        JTextField destinationFile = new JTextField();
        FileSelectionPanel sourceFileSelectionPanel = new FileSelectionPanel("Source file", sourceFile, false);
        sourceFileSelectionPanel.setFileFilter("nkld*.bin", "Lens correction data file (nkld*.bin)");
        final JComponent[] inputs = new JComponent[]{
                sourceFileSelectionPanel,
                new FileSelectionPanel("Destination file", destinationFile, true)
        };
        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                inputs,
                "Choose source file and destination dir",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION)) {
            try {
                new NkldDecoder().decode(sourceFile.getText(), destinationFile.getText(), false);
                JOptionPane.showMessageDialog(this, "Decoding complete.\nFile '" + new File(destinationFile.getText()).getAbsolutePath() + "' was created.", "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (FirmwareFormatException e) {
                JOptionPane.showMessageDialog(this, e.getMessage() + "\nPlease see console for full stack trace", "Error decoding files", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }


    private static final String STATE_EXTENSION = ".xstate";

    private void loadState() {
        final JFileChooser fc = new JFileChooser();

        fc.setDialogTitle("Select source file");
        fc.setCurrentDirectory(new java.io.File("."));

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        /* TODO add chip name to triggers extension */
        fc.setFileFilter(Format.createFilter(STATE_EXTENSION, "Emulator state (*" + STATE_EXTENSION + ")"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File sourceFile = fc.getSelectedFile();
            if (!sourceFile.exists()) {
                JOptionPane.showMessageDialog(this, "Could not find file '" + sourceFile.getAbsolutePath() + "'.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {

                // Problem: some UI components install listeners and links will be lost
                closeAllFrames();
                try {
                    final String source = sourceFile.getAbsolutePath();
                    setTitle(ApplicationInfo.getNameVersion() + " - Loading...");

                    framework = EmulationFramework.load(source, prefs);
                    framework.setupCallbacks(getCallbackHandler(0), getCallbackHandler(1));
                    framework.getMasterClock().setSyncPlay(prefs.isSyncPlay());
                    setTitle(ApplicationInfo.getNameVersion() + " - Loaded " + source);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, e.getMessage() + "\nSee console for more info", "Error", JOptionPane.ERROR_MESSAGE);
                }
                // some menu items may get disabled
                updateStates();
            }
        }
    }

    private void saveState() {
        final JFileChooser fc = new JFileChooser();

        fc.setDialogTitle("Select destination file");
        fc.setCurrentDirectory(new java.io.File("."));

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        /* TODO add chip name to triggers extension */
        fc.setFileFilter(Format.createFilter(STATE_EXTENSION, "Emulator state (*" + STATE_EXTENSION + ")"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destinationFile = fc.getSelectedFile();
            if (!(destinationFile.getAbsolutePath().toLowerCase().endsWith(STATE_EXTENSION))) {
                destinationFile = new File(destinationFile.getAbsolutePath() + STATE_EXTENSION);
            }
            if (!destinationFile.exists() || JOptionPane.showConfirmDialog(this, "Are you sure you want to overwrite " + destinationFile.getName(), "Confirm overwrite", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                // spying frames insert IO forwarding pins that should not be saved
                closeAllSpyFrames();
                try {
                    EmulationFramework.saveStateToFile(framework, destinationFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "State saving complete", "Done", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error saving state file\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void openGenerateSysSymbolsDialog() {
        GenerateSysSymbolsDialog generateSysSymbolsDialog = new GenerateSysSymbolsDialog(this, framework.getPlatform(Constants.CHIP_FR).getMemory());
        generateSysSymbolsDialog.startGeneration();
        generateSysSymbolsDialog.setVisible(true);
    }


    private void openAnalyseDialog(final int chip) {
        JTextField optionsField = new JTextField();
        JTextField destinationField = new JTextField();

        // compute and try default names for options file.
        // In order : <firmware>.dfr.txt , <firmware>.txt , dfr.txt (or the same for dtx)
        File optionsFile = new File(imageFile[chip].getParentFile(), FilenameUtils.getBaseName(imageFile[chip].getAbsolutePath()) + ((chip == Constants.CHIP_FR)?".dfr.txt":".dtx.txt"));
        if (!optionsFile.exists()) {
            optionsFile = new File(imageFile[chip].getParentFile(), FilenameUtils.getBaseName(imageFile[chip].getAbsolutePath()) + ".txt");
            if (!optionsFile.exists()) {
                optionsFile = new File(imageFile[chip].getParentFile(), ((chip == Constants.CHIP_FR)?"dfr.txt":"dtx.txt"));
                if (!optionsFile.exists()) {
                    optionsFile = null;
                }
            }
        }
        if (optionsFile != null) {
            optionsField.setText(optionsFile.getAbsolutePath());
        }

        // compute default name for output
        File outputFile = new File(imageFile[chip].getParentFile(), FilenameUtils.getBaseName(imageFile[chip].getAbsolutePath()) + ".asm");
        destinationField.setText(outputFile.getAbsolutePath());

        final JCheckBox writeOutputCheckbox = new JCheckBox("Write disassembly to file");

        final FileSelectionPanel destinationFileSelectionPanel = new FileSelectionPanel("Destination file", destinationField, false);
        destinationFileSelectionPanel.setFileFilter(".asm", "Assembly language file (*.asm)");
        writeOutputCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean writeToFile = writeOutputCheckbox.isSelected();
                destinationFileSelectionPanel.setEnabled(writeToFile);
                prefs.setWriteDisassemblyToFile(chip, writeToFile);
            }
        });

        writeOutputCheckbox.setSelected(prefs.isWriteDisassemblyToFile(chip));
        destinationFileSelectionPanel.setEnabled(prefs.isWriteDisassemblyToFile(chip));

        FileSelectionPanel fileSelectionPanel = new FileSelectionPanel((chip == Constants.CHIP_FR) ? "Dfr options file" : "Dtx options file", optionsField, false);
        fileSelectionPanel.setFileFilter((chip == Constants.CHIP_FR)?".dfr.txt":".dtx.txt", (chip == Constants.CHIP_FR)?"Dfr options file (*.dfr.txt)":"Dtx options file (*.dtx.txt)");
        final JComponent[] inputs = new JComponent[]{
                //new FileSelectionPanel("Source file", sourceFile, false, dependencies),
                fileSelectionPanel,
                writeOutputCheckbox,
                destinationFileSelectionPanel,
                makeOutputOptionCheckBox(chip, OutputOption.STRUCTURE, prefs.getOutputOptions(chip), true),
                makeOutputOptionCheckBox(chip, OutputOption.ORDINAL, prefs.getOutputOptions(chip), true),
                makeOutputOptionCheckBox(chip, OutputOption.PARAMETERS, prefs.getOutputOptions(chip), true),
                makeOutputOptionCheckBox(chip, OutputOption.INT40, prefs.getOutputOptions(chip), true),
                makeOutputOptionCheckBox(chip, OutputOption.MEMORY, prefs.getOutputOptions(chip), true),
                new JLabel("(hover over the options for help. See also 'Tools/Options/Disassembler output')", SwingConstants.CENTER)
        };

        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                inputs,
                "Choose analyse options",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION)) {
            String outputFilename = writeOutputCheckbox.isSelected() ? destinationField.getText() : null;
            boolean cancel = false;
            if (outputFilename != null) {
                if (new File(outputFilename).exists()) {
                    if (JOptionPane.showConfirmDialog(this, "File '" + outputFilename + "' already exists.\nDo you really want to overwrite it ?", "File exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                        cancel = true;
                    }
                }
            }
            if (!cancel) {
                AnalyseProgressDialog analyseProgressDialog = new AnalyseProgressDialog(this, framework.getPlatform(chip).getMemory());
                analyseProgressDialog.startBackgroundAnalysis(chip, optionsField.getText(), outputFilename);
                analyseProgressDialog.setVisible(true);
            }
        }
    }


    private void openLoadImageDialog(final int chip) {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f != null) {
                    //noinspection SimplifiableIfStatement
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().toLowerCase().startsWith((chip == Constants.CHIP_FR) ? "b" : "a") && f.getName().toLowerCase().endsWith(".bin");
                }
                return false;
            }

            @Override
            public String getDescription() {
                return Constants.CHIP_LABEL[chip] + " firmware file (" + ((chip == Constants.CHIP_FR)?"b":"a") + "*.bin)";
            }
        });
        fc.setCurrentDirectory(new File("."));

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File firmwareFile = fc.getSelectedFile();
            if (firmwareFile.exists()) {
                imageFile[chip] = firmwareFile;
                reset(chip);
            } else {
                JOptionPane.showMessageDialog(this, "Given " + Constants.CHIP_LABEL[chip] + " firmware file does not exist:\n" + firmwareFile.getAbsolutePath(), "File not found", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void reset(int chip) {
        // As serial and I/O are interconnected, make sure all spying windows are closed before
        // reinstanciating serial interfaces and i/o ports, otherwise we keep references to dead objects
        boolean wasSerialInterfaceFrameOpen[] = {false, false};
        boolean wasGenericSerialFrameOpen[] = {false, false};
        boolean wasIoPortsFrameOpen[] = {false, false};
        for (int c = 0; c < 2; c++) {
            if (serialInterfaceFrame[c] != null) {
                wasSerialInterfaceFrameOpen[c] = true;
                serialInterfaceFrame[c].dispose();
                serialInterfaceFrame[c] = null;
            }
            if (genericSerialFrame[c] != null) {
                wasGenericSerialFrameOpen[c] = true;
                genericSerialFrame[c].dispose();
                genericSerialFrame[c] = null;
            }
            if (ioPortsFrame[c] != null) {
                wasIoPortsFrameOpen[c] = true;
                ioPortsFrame[c].dispose();
                ioPortsFrame[c] = null;
            }
        }

        initialize(chip);

        if (prefs.isCloseAllWindowsOnStop()) {
            closeAllFrames(chip, false);
        }
        else {
            closeAllFrames(chip, true);
            // Restore serial and I/O chips spy windows if they were open
            for (int c = 0; c < 2; c++) {
                if (wasSerialInterfaceFrameOpen[c]) toggleSerialInterfaces(c);
                if (wasGenericSerialFrameOpen[c]) toggleGenericSerialFrame(c);
                if (wasIoPortsFrameOpen[c]) toggleIoPortsWindow(c);
            }
        }

        updateState(chip);
    }

    private ClockableCallbackHandler getCallbackHandler(final int chip) {
        return new ClockableCallbackHandler() {
            @Override
            public void onNormalExit(final Object o) {
                Runnable  runnable = new Runnable() {
                    public void run(){
                        try {
                            signalEmulatorStopped(chip);
                            if (o instanceof BreakCondition) {
                                if (((BreakCondition) o).getBreakTrigger() != null) {

                                    setStatusText(chip, "Break trigger matched : " + ((BreakCondition) o).getBreakTrigger().getName());
                                    statusBar[chip].setBackground(STATUS_BGCOLOR_BREAK);
                                }
                                else {
                                    setStatusText(chip, "Emulation complete");
                                    statusBar[chip].setBackground(STATUS_BGCOLOR_DEFAULT);
                                }
                            }
                            else {
                                setStatusText(chip, "Emulation complete" + ((o == null) ? "" : (": " + o.toString())));
                                statusBar[chip].setBackground(STATUS_BGCOLOR_DEFAULT);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateState(chip);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }

            @Override
            public void onException(final Exception e) {
                Runnable  runnable = new Runnable() {
                    public void run(){
                        signalEmulatorStopped(chip);
                        String message = e.getMessage();
                        if (StringUtils.isEmpty(message)) {
                            message = e.getClass().getName();
                        }
                        JOptionPane.showMessageDialog(EmulatorUI.this, message + "\nSee console for more info", Constants.CHIP_LABEL[chip] + " Emulator error", JOptionPane.ERROR_MESSAGE);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        };
    }

    private void initialize(final int chip) {
        // Stop timers if active from a previous session (reset)
        if (framework.getPlatform(chip) != null && framework.getPlatform(chip).getProgrammableTimers()[0].isActive()) {
            for (ProgrammableTimer timer : framework.getPlatform(chip).getProgrammableTimers()) {
                timer.setActive(false);
            }
            // Stop button animation
            setProgrammableTimerAnimationEnabled(chip, true);
        }

        framework.initialize(chip, imageFile[chip]);

        setTitle(ApplicationInfo.getNameVersion() + " - " + (imageFile[Constants.CHIP_FR]==null?"(none)":imageFile[Constants.CHIP_FR].getName()) + " / " + (imageFile[Constants.CHIP_TX]==null?"(none)":imageFile[Constants.CHIP_TX].getName()));

        setEmulatorSleepCode(chip, prefs.getSleepTick(chip));

        // Reenable Timers if requested
        if (prefs.isAutoEnableTimers(chip)) {
            toggleProgrammableTimers(chip);
        }
    }

    private void openUIOptionsDialog() {
        JPanel options = new JPanel(new GridLayout(0,1));
        options.setName("User Interface");
        // Button size
        ActionListener buttonSizeRadioListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.setButtonSize(e.getActionCommand());
            }
        };
        JRadioButton small = new JRadioButton("Small");
        small.setActionCommand(BUTTON_SIZE_SMALL);
        small.addActionListener(buttonSizeRadioListener);
        if (BUTTON_SIZE_SMALL.equals(prefs.getButtonSize())) small.setSelected(true);
        JRadioButton medium = new JRadioButton("Medium");
        medium.setActionCommand(BUTTON_SIZE_MEDIUM);
        medium.addActionListener(buttonSizeRadioListener);
        if (BUTTON_SIZE_MEDIUM.equals(prefs.getButtonSize())) medium.setSelected(true);
        JRadioButton large = new JRadioButton("Large");
        large.setActionCommand(BUTTON_SIZE_LARGE);
        large.addActionListener(buttonSizeRadioListener);
        if (BUTTON_SIZE_LARGE.equals(prefs.getButtonSize())) large.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(small);
        group.add(medium);
        group.add(large);

        // Close windows on stop
        final JCheckBox closeAllWindowsOnStopCheckBox = new JCheckBox("Close all windows on Stop");
        closeAllWindowsOnStopCheckBox.setSelected(prefs.isCloseAllWindowsOnStop());

        // Refresh interval
        JPanel refreshIntervalPanel = new JPanel();
        final JTextField refreshIntervalField = new JTextField(5);
        refreshIntervalPanel.add(new JLabel("Refresh interval for cpu, screen, etc. (ms):"));

        refreshIntervalField.setText("" + prefs.getRefreshIntervalMs());
        refreshIntervalPanel.add(refreshIntervalField);

        // Setup panel
        options.add(new JLabel("Button size :"));
        options.add(small);
        options.add(medium);
        options.add(large);
        options.add(closeAllWindowsOnStopCheckBox);
        options.add(refreshIntervalPanel);
        options.add(new JLabel("Larger value greatly increases emulation speed"));

        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                options,
                "Preferences",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION))
        {
            // save
            prefs.setButtonSize(group.getSelection().getActionCommand());
            prefs.setCloseAllWindowsOnStop(closeAllWindowsOnStopCheckBox.isSelected());
            int refreshIntervalMs = 0;
            try {
                refreshIntervalMs = Integer.parseInt(refreshIntervalField.getText());
            } catch (NumberFormatException e) {
                // noop
            }
            refreshIntervalMs = Math.max(Math.min(refreshIntervalMs, 10000), 10);
            prefs.setRefreshIntervalMs(refreshIntervalMs);
            applyPrefsToUI();
        }
    }

    private void openChipOptionsDialog(final int chip) {

        // ------------------------ Disassembly options

        JPanel disassemblyOptionsPanel = new JPanel(new MigLayout("", "[left,grow][left,grow]"));

        // Prepare sample code area
        final RSyntaxTextArea listingArea = new RSyntaxTextArea(20, 90);
        SourceCodeFrame.prepareAreaFormat(chip, listingArea);

        final List<JCheckBox> outputOptionsCheckBoxes = new ArrayList<JCheckBox>();
        ActionListener areaRefresherListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Set<OutputOption> sampleOptions = EnumSet.noneOf(OutputOption.class);
                    dumpOptionCheckboxes(outputOptionsCheckBoxes, sampleOptions);
                    int baseAddress = framework.getPlatform(chip).getCpuState().getResetAddress();
                    int lastAddress = baseAddress;
                    Memory sampleMemory = new DebuggableMemory(false);
                    sampleMemory.map(baseAddress, 0x100, true, true, true);
                    StringWriter writer = new StringWriter();
                    Disassembler disassembler;
                    if (chip == Constants.CHIP_FR) {
                        sampleMemory.store16(lastAddress, 0x1781); // PUSH    RP
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x8FFE); // PUSH    (FP,AC,R12,R11,R10,R9,R8)
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x83EF); // ANDCCR  #0xEF
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x9F80); // LDI:32  #0x68000000,R0
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x6800);
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x0000);
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x2031); // LD      @(FP,0x00C),R1
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0xB581); // LSL     #24,R1
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x1A40); // DMOVB   R13,@0x40
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x9310); // ORCCR   #0x10
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x8D7F); // POP     (R8,R9,R10,R11,R12,AC,FP)
                        lastAddress += 2;
                        sampleMemory.store16(lastAddress, 0x0781); // POP    RP
                        lastAddress += 2;

                        disassembler = new Dfr();
                        disassembler.setDebugPrintWriter(new PrintWriter(new StringWriter())); // Ignore
                        disassembler.setOutputFileName(null);
                        disassembler.processOptions(new String[]{
                                "-m",
                                "0x" + Format.asHex(baseAddress, 8) + "-0x" + Format.asHex(lastAddress, 8) + "=CODE"
                        });
                    }
                    else {
                        sampleMemory.store32(lastAddress, 0x340B0001);   // li      $t3, 0x0001
                        lastAddress += 4;
                        sampleMemory.store32(lastAddress, 0x17600006);   // bnez    $k1, 0xBFC00020
                        lastAddress += 4;
                        sampleMemory.store32(lastAddress, 0x00000000);   //  nop
                        lastAddress += 4;
                        sampleMemory.store32(lastAddress, 0x54400006);   // bnezl   $t4, 0xBFC00028
                        lastAddress += 4;
                        sampleMemory.store32(lastAddress, 0x3C0C0000);   //  ?lui   $t4, 0x0000
                        lastAddress += 4;

                        int baseAddress16 = lastAddress;
                        int lastAddress16 = baseAddress16;
                        sampleMemory.store32(lastAddress16, 0xF70064F6); // save    $ra,$s0,$s1,$s2-$s7,$fp, 0x30
                        lastAddress16 += 4;
                        sampleMemory.store16(lastAddress16, 0x6500);     // nop
                        lastAddress16 += 2;
                        sampleMemory.store32(lastAddress16, 0xF7006476); // restore $ra,$s0,$s1,$s2-$s7,$fp, 0x30
                        lastAddress16 += 4;
                        sampleMemory.store16(lastAddress16, 0xE8A0);     // ret
                        lastAddress16 += 2;

                        disassembler = new Dtx();
                        disassembler.setDebugPrintWriter(new PrintWriter(new StringWriter())); // Ignore
                        disassembler.setOutputFileName(null);
                        disassembler.processOptions(new String[]{
                                "-m",
                                "0x" + Format.asHex(baseAddress, 8) + "-0x" + Format.asHex(lastAddress, 8) + "=CODE:32",
                                "-m",
                                "0x" + Format.asHex(baseAddress16, 8) + "-0x" + Format.asHex(lastAddress16, 8) + "=CODE:16"
                        });
                    }
                    disassembler.setOutputOptions(sampleOptions);
                    disassembler.setMemory(sampleMemory);
                    disassembler.initialize();
                    disassembler.setOutWriter(writer);
                    disassembler.disassembleMemRanges();
                    disassembler.cleanup();
                    listingArea.setText("");
                    listingArea.append(writer.toString());
                    listingArea.setCaretPosition(0);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        int i = 1;
        for (OutputOption outputOption : OutputOption.allFormatOptions) {
            JCheckBox checkBox = makeOutputOptionCheckBox(chip, outputOption, prefs.getOutputOptions(chip), false);
            if (checkBox != null) {
                outputOptionsCheckBoxes.add(checkBox);
                disassemblyOptionsPanel.add(checkBox, (i % 2 == 0)?"wrap":"");
                checkBox.addActionListener(areaRefresherListener);
                i++;
            }
        }
        if (i % 2 == 0) {
            disassemblyOptionsPanel.add(new JLabel(), "wrap");
        }

        // Force a refresh
        areaRefresherListener.actionPerformed(new ActionEvent(outputOptionsCheckBoxes.get(0), 0, ""));

//        disassemblyOptionsPanel.add(new JLabel("Sample output:", SwingConstants.LEADING), "gapbottom 1, span, split 2, aligny center");
//        disassemblyOptionsPanel.add(new JSeparator(), "span 2,wrap");
        disassemblyOptionsPanel.add(new JSeparator(), "span 2, gapleft rel, growx, wrap");
        disassemblyOptionsPanel.add(new JLabel("Sample output:"), "span 2,wrap");
        disassemblyOptionsPanel.add(new JScrollPane(listingArea), "span 2,wrap");
        disassemblyOptionsPanel.add(new JLabel("Tip: hover over the option checkboxes for help"), "span 2, center, wrap");

        // ------------------------ Emulation options

        JPanel emulationOptionsPanel = new JPanel(new VerticalLayout(5, VerticalLayout.LEFT));
        emulationOptionsPanel.add(new JLabel());
        JLabel warningLabel = new JLabel("NOTE: these options only take effect after reloading the firmware (or performing a 'Stop and reset')");
        warningLabel.setBackground(Color.RED);
        warningLabel.setOpaque(true);
        warningLabel.setForeground(Color.WHITE);
        warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emulationOptionsPanel.add(warningLabel);
        emulationOptionsPanel.add(new JLabel());

        final JCheckBox writeProtectFirmwareCheckBox = new JCheckBox("Write-protect firmware");
        writeProtectFirmwareCheckBox.setSelected(prefs.isFirmwareWriteProtected(chip));
        emulationOptionsPanel.add(writeProtectFirmwareCheckBox);
        emulationOptionsPanel.add(new JLabel("If checked, any attempt to write to the loaded firmware area will result in an Emulator error. This can help trap spurious writes"));

        final JCheckBox dmaSynchronousCheckBox = new JCheckBox("Make DMA synchronous");
        dmaSynchronousCheckBox.setSelected(prefs.isDmaSynchronous(chip));
        emulationOptionsPanel.add(dmaSynchronousCheckBox);
        emulationOptionsPanel.add(new JLabel("If checked, DMA operations will be performed immediately, pausing the CPU. Otherwise they are performed in a separate thread."));

        final JCheckBox autoEnableTimersCheckBox = new JCheckBox("Auto enable timers");
        autoEnableTimersCheckBox.setSelected(prefs.isAutoEnableTimers(chip));
        emulationOptionsPanel.add(autoEnableTimersCheckBox);
        emulationOptionsPanel.add(new JLabel("If checked, timers will be automatically enabled upon reset or firmware load."));

        // Log memory messages
        final JCheckBox logMemoryMessagesCheckBox = new JCheckBox("Log memory messages");
        logMemoryMessagesCheckBox.setSelected(prefs.isLogMemoryMessages(chip));
        emulationOptionsPanel.add(logMemoryMessagesCheckBox);
        emulationOptionsPanel.add(new JLabel("If checked, messages related to memory will be logged to the console."));

        // Log serial messages
        final JCheckBox logSerialMessagesCheckBox = new JCheckBox("Log serial messages");
        logSerialMessagesCheckBox.setSelected(prefs.isLogSerialMessages(chip));
        emulationOptionsPanel.add(logSerialMessagesCheckBox);
        emulationOptionsPanel.add(new JLabel("If checked, messages related to serial interfaces will be logged to the console."));

        // Log register messages
        final JCheckBox logRegisterMessagesCheckBox = new JCheckBox("Log register messages");
        logRegisterMessagesCheckBox.setSelected(prefs.isLogRegisterMessages(chip));
        emulationOptionsPanel.add(logRegisterMessagesCheckBox);
        emulationOptionsPanel.add(new JLabel("If checked, warnings related to unimplemented register addresses will be logged to the console."));

        // Log pin messages
        final JCheckBox logPinMessagesCheckBox = new JCheckBox("Log pin messages");
        logPinMessagesCheckBox.setSelected(prefs.isLogPinMessages(chip));
        emulationOptionsPanel.add(logPinMessagesCheckBox);
        emulationOptionsPanel.add(new JLabel("If checked, warnings related to unimplemented I/O pins will be logged to the console."));

        emulationOptionsPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        // Alt mode upon Debug
        JPanel altDebugPanel = new JPanel(new FlowLayout());
        Object[] altDebugMode = EnumSet.allOf(EmulationFramework.ExecutionMode.class).toArray();
        final JComboBox altModeForDebugCombo = new JComboBox(new DefaultComboBoxModel(altDebugMode));
        for (int j = 0; j < altDebugMode.length; j++) {
            if (altDebugMode[j].equals(prefs.getAltExecutionModeForSyncedCpuUponDebug(chip))) {
                altModeForDebugCombo.setSelectedIndex(j);
            }
        }
        altDebugPanel.add(new JLabel(Constants.CHIP_LABEL[1 - chip] + " mode when " + Constants.CHIP_LABEL[chip] + " runs in sync Debug: "));
        altDebugPanel.add(altModeForDebugCombo);
        emulationOptionsPanel.add(altDebugPanel);
        emulationOptionsPanel.add(new JLabel("If 'sync mode' is selected, this is the mode the " + Constants.CHIP_LABEL[1 - chip] + " chip will run in when running the " + Constants.CHIP_LABEL[chip] + " in Debug mode"));

        // Alt mode upon Step
        JPanel altStepPanel = new JPanel(new FlowLayout());
        Object[] altStepMode = EnumSet.allOf(EmulationFramework.ExecutionMode.class).toArray();
        final JComboBox altModeForStepCombo = new JComboBox(new DefaultComboBoxModel(altStepMode));
        for (int j = 0; j < altStepMode.length; j++) {
            if (altStepMode[j].equals(prefs.getAltExecutionModeForSyncedCpuUponStep(chip))) {
                altModeForStepCombo.setSelectedIndex(j);
            }
        }
        altStepPanel.add(new JLabel(Constants.CHIP_LABEL[1 - chip] + " mode when " + Constants.CHIP_LABEL[chip] + " runs in sync Step: "));
        altStepPanel.add(altModeForStepCombo);
        emulationOptionsPanel.add(altStepPanel);
        emulationOptionsPanel.add(new JLabel("If 'sync mode' is selected, this is the mode the " + Constants.CHIP_LABEL[1 - chip] + " chip will run in when running the " + Constants.CHIP_LABEL[chip] + " in Step mode"));


        // ------------------------ Prepare tabbed pane

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Constants.CHIP_LABEL[chip] + " Disassembly Options", null, disassemblyOptionsPanel);
        tabbedPane.addTab(Constants.CHIP_LABEL[chip] + " Emulation Options", null, emulationOptionsPanel);

        if (chip == Constants.CHIP_TX) {
            JPanel chipSpecificOptionsPanel = new JPanel(new VerticalLayout(5, VerticalLayout.LEFT));

            chipSpecificOptionsPanel.add(new JLabel("Eeprom initialization mode:"));

            ActionListener eepromInitializationRadioActionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    prefs.setEepromInitMode(Prefs.EepromInitMode.valueOf(e.getActionCommand()));
                }
            };
            JRadioButton blank = new JRadioButton("Blank");
            blank.setActionCommand(Prefs.EepromInitMode.BLANK.name());
            blank.addActionListener(eepromInitializationRadioActionListener);
            if (Prefs.EepromInitMode.BLANK.equals(prefs.getEepromInitMode())) blank.setSelected(true);
            JRadioButton persistent = new JRadioButton("Persistent across sessions");
            persistent.setActionCommand(Prefs.EepromInitMode.PERSISTENT.name());
            persistent.addActionListener(eepromInitializationRadioActionListener);
            if (Prefs.EepromInitMode.PERSISTENT.equals(prefs.getEepromInitMode())) persistent.setSelected(true);
            JRadioButton lastLoaded = new JRadioButton("Last Loaded");
            lastLoaded.setActionCommand(Prefs.EepromInitMode.LAST_LOADED.name());
            lastLoaded.addActionListener(eepromInitializationRadioActionListener);
            if (Prefs.EepromInitMode.LAST_LOADED.equals(prefs.getEepromInitMode())) lastLoaded.setSelected(true);

            ButtonGroup group = new ButtonGroup();
            group.add(blank);
            group.add(persistent);
            group.add(lastLoaded);

            chipSpecificOptionsPanel.add(blank);
            chipSpecificOptionsPanel.add(persistent);
            chipSpecificOptionsPanel.add(lastLoaded);


            chipSpecificOptionsPanel.add(new JLabel("Front panel type:"));
            final JComboBox frontPanelNameCombo = new JComboBox(new String[]{"D5100_small", "D5100_large"});
            if (prefs.getFrontPanelName() != null) {
                frontPanelNameCombo.setSelectedItem(prefs.getFrontPanelName());
            }
            frontPanelNameCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    prefs.setFrontPanelName((String) frontPanelNameCombo.getSelectedItem());
                }
            });
            chipSpecificOptionsPanel.add(frontPanelNameCombo);

            emulationOptionsPanel.add(new JSeparator(JSeparator.HORIZONTAL));

            tabbedPane.addTab(Constants.CHIP_LABEL[chip] + " specific options", null, chipSpecificOptionsPanel);
        }

        // ------------------------ Show it

        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                tabbedPane,
                Constants.CHIP_LABEL[chip] + " options",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION))
        {
            // save output options
            dumpOptionCheckboxes(outputOptionsCheckBoxes, prefs.getOutputOptions(chip));
            // apply
            TxCPUState.initRegisterLabels(prefs.getOutputOptions(chip));

            // save other prefs
            prefs.setFirmwareWriteProtected(chip, writeProtectFirmwareCheckBox.isSelected());
            prefs.setDmaSynchronous(chip, dmaSynchronousCheckBox.isSelected());
            prefs.setAutoEnableTimers(chip, autoEnableTimersCheckBox.isSelected());
            prefs.setLogRegisterMessages(chip, logRegisterMessagesCheckBox.isSelected());
            prefs.setLogSerialMessages(chip, logSerialMessagesCheckBox.isSelected());
            prefs.setLogPinMessages(chip, logPinMessagesCheckBox.isSelected());
            prefs.setLogMemoryMessages(chip, logMemoryMessagesCheckBox.isSelected());
            prefs.setAltExecutionModeForSyncedCpuUponDebug(chip, (EmulationFramework.ExecutionMode) altModeForDebugCombo.getSelectedItem());
            prefs.setAltExecutionModeForSyncedCpuUponStep(chip, (EmulationFramework.ExecutionMode) altModeForStepCombo.getSelectedItem());

        }
    }

    private void dumpOptionCheckboxes(List<JCheckBox> outputOptionsCheckBoxes, Set<OutputOption> outputOptions) {
        for (JCheckBox checkBox : outputOptionsCheckBoxes) {
            try {
                OutputOption.setOption(outputOptions, checkBox.getText(), checkBox.isSelected());
            } catch (ParsingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a JCheckbox corresponding to the given Output Option, reflecting its current state in prefs and changing it when modified
     * @param chip
     * @param option one of the defined options
     * @param outputOptions the options list to initialize field from (and to which change will be written if reflectChange is true)
     * @param reflectChange if true, changing the checkbox immediately changes the option in the given outputOptions
     * @return
     */
    private JCheckBox makeOutputOptionCheckBox(final int chip, final OutputOption option, Set<OutputOption> outputOptions, boolean reflectChange) {
        final JCheckBox checkBox = new JCheckBox(option.getKey());
        String help = (chip == Constants.CHIP_FR)?option.getFrHelp():option.getTxHelp();

        if (help == null) return null;

        checkBox.setToolTipText(help);
        checkBox.setSelected(outputOptions.contains(option));
        if (reflectChange) {
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    prefs.setOutputOption(chip, option, checkBox.isSelected());
                }
            });
        }
        return checkBox;
    }

    private void showAboutDialog() {
        // for copying style
        JLabel label = new JLabel();
        Font font = label.getFont();

        // create some css from the label's font
        String style = "font-family:" + font.getFamily() + ";"
                + "font-weight:" + (font.isBold() ? "bold" : "normal") + ";"
                + "font-size:" + font.getSize() + "pt;";

        // html content
        JEditorPane editorPane = new JEditorPane("text/html", "<html><body style=\"" + style + "\">"
                + "<font size=\"+1\">" + ApplicationInfo.getNameVersion() + "</font><br/>"
                + "<i>A dual (Fujitsu FR + Toshiba TX) microcontroller emulator in Java, aimed at mimicking the behaviour of Nikon DSLRs</i><br/>"
                + "<font size=\"-2\">Built on " + ApplicationInfo.getBuildTime() + "</font><br/><br/>"
                + "This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.<br/>"
                + "This software is provided under the GNU General Public License, version 3 - " + makeLink("http://www.gnu.org/licenses/gpl-3.0.txt") + "<br/>"
                + "This software is based on, or makes use of, the following works:<ul>\n"
                + "<li>Simeon Pilgrim's deciphering of firmware encoding and lots of information shared on his blog - " + makeLink("http://simeonpilgrim.com/blog/") + "</li>"
                + "<li>Dfr Fujitsu FR diassembler Copyright (c) Kevin Schoedel - " + makeLink("http://scratchpad.wikia.com/wiki/Disassemblers/DFR") + "<br/>and its port to C# by Simeon Pilgrim</li>"
                + "<li>\"How To Write a Computer Emulator\" article by Marat Fayzullin - " + makeLink("http://fms.komkon.org/EMUL8/HOWTO.html") + "</li>"
                + "<li>The PearColator x86 emulator project - " + makeLink("http://apt.cs.man.ac.uk/projects/jamaica/tools/PearColator/") + "</li>"
                + "<li>The Jacksum checksum library Copyright (c) Dipl.-Inf. (FH) Johann Nepomuk Löfflmann  - " + makeLink("http://www.jonelo.de/java/jacksum/") + "</li>"
                + "<li>HexEditor & RSyntaxTextArea swing components, Copyright (c) Robert Futrell - " + makeLink("http://fifesoft.com/hexeditor/") + "</li>"
                + "<li>JGraphX graph drawing library, Copyright (c) JGraph Ltd - " + makeLink("http://www.jgraph.com/jgraph.html") + "</li>"
                + "<li>Apache commons libraries, Copyright (c) The Apache Software Foundation - " + makeLink("http://commons.apache.org/") + "</li>"
                + "<li>VerticalLayout, Copyright (c) Cellspark - " + makeLink("http://www.cellspark.com/vl.html") + "</li>"
                + "<li>MigLayout, Copyright (c) MigInfoCom - " + makeLink("http://www.miginfocom.com/") + "</li>"
                + "<li>Glazed Lists, Copyright (c) 2003-2006, publicobject.com, O'Dell Engineering Ltd - " + makeLink("http://www.glazedlists.com/") + "</li>"
                + "<li>Samples from the Java Tutorial (c) Sun Microsystems / Oracle - " + makeLink("http://docs.oracle.com/javase/tutorial") + "</li>"
                + "<li>MARS, MIPS Assembler and Runtime Simulator (c) 2003-2011, Pete Sanderson and Kenneth Vollmar - " + makeLink("http://courses.missouristate.edu/KenVollmar/MARS") + "</li>"
                + "<li>SteelSeries (and SteelCheckBox) Swing components (c) 2010, Gerrit Grunwald - " + makeLink("http://harmoniccode.blogspot.be/search/label/steelseries") + "</li>"
                + "</ul>"
                + "License terms for all included code are available in the 'licenses' folder of the distribution."
                + "<p>For more information, help or ideas, please join us at " + makeLink("http://nikonhacker.com") + "</p></body></html>");

        // handle link events
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception e1) {
                        // noop
                    }
                }
            }
        });
        editorPane.setEditable(false);
        Color greyLayer = new Color(label.getBackground().getRed(), label.getBackground().getGreen(), label.getBackground().getBlue(), 192);
        editorPane.setBackground(greyLayer);
        //editorPane.setOpaque(false);

        // show
//        JOptionPane.showMessageDialog(this, editorPane, "About", JOptionPane.PLAIN_MESSAGE);

        final JDialog dialog = new JDialog(this, "About", true);

        JPanel contentPane = new BackgroundImagePanel(new BorderLayout(), Toolkit.getDefaultToolkit().getImage(EmulatorUI.class.getResource("images/nh_full.jpg")));
        contentPane.add(editorPane, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(okButton);
        bottomPanel.setBackground(greyLayer);
        // bottomPanel.setOpaque(false);

        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setContentPane(contentPane);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private String makeLink(String link) {
        return "<a href=" + link + ">" + link + "</a>";
    }

    private void setEmulatorSleepCode(int chip, int value) {
        framework.getEmulator(chip).exitSleepLoop();
        switch (value) {
            case 0:
                framework.getEmulator(chip).setSleepIntervalMs(0);
                break;
            case 1:
                framework.getEmulator(chip).setSleepIntervalMs(1);
                break;
            case 2:
                framework.getEmulator(chip).setSleepIntervalMs(10);
                break;
            case 3:
                framework.getEmulator(chip).setSleepIntervalMs(100);
                break;
            case 4:
                framework.getEmulator(chip).setSleepIntervalMs(1000);
                break;
            case 5:
                framework.getEmulator(chip).setSleepIntervalMs(10000);
                break;
        }
    }


    private void closeAllFrames() {
        closeAllFrames(Constants.CHIP_FR, false);
        closeAllFrames(Constants.CHIP_TX, false);
    }

    private void closeAllFrames(int chip, boolean mustReOpen) {
        if (chip == Constants.CHIP_FR) {
            if (component4006Frame != null) {
                component4006Frame.dispose();
                component4006Frame = null;
                if (mustReOpen) toggleComponent4006();
            }
            if (screenEmulatorFrame != null) {
                screenEmulatorFrame.dispose();
                screenEmulatorFrame = null;
                if (mustReOpen) toggleScreenEmulator();
            }
        }

        if (chip == Constants.CHIP_TX) {
            if (frontPanelFrame != null) {
                frontPanelFrame.dispose();
                frontPanelFrame = null;
                if (mustReOpen) toggleFrontPanel();
            }
        }

        if (cpuStateEditorFrame[chip] != null) {
            cpuStateEditorFrame[chip].dispose();
            cpuStateEditorFrame[chip] = null;
            if (mustReOpen) toggleCPUState(chip);
        }
        if (disassemblyLogFrame[chip] != null) {
            disassemblyLogFrame[chip].dispose();
            disassemblyLogFrame[chip] = null;
            if (mustReOpen) toggleDisassemblyLog(chip);
        }
        // TODO why close Break Trigger ?
        if (breakTriggerListFrame[chip] != null) {
            breakTriggerListFrame[chip].dispose();
            breakTriggerListFrame[chip] = null;
            if (mustReOpen) toggleBreakTriggerList(chip);
        }
        if (memoryActivityViewerFrame[chip] != null) {
            memoryActivityViewerFrame[chip].dispose();
            memoryActivityViewerFrame[chip] = null;
            if (mustReOpen) toggleMemoryActivityViewer(chip);
        }
        // TODO reopen on current tab ?
        if (memoryHexEditorFrame[chip] != null) {
            memoryHexEditorFrame[chip].dispose();
            memoryHexEditorFrame[chip] = null;
            if (mustReOpen) toggleMemoryHexEditor(chip);
        }
        if (customMemoryRangeLoggerFrame[chip] != null) {
            customMemoryRangeLoggerFrame[chip].dispose();
            customMemoryRangeLoggerFrame[chip] = null;
            if (mustReOpen) toggleCustomMemoryRangeLoggerComponentFrame(chip);
        }
        if (codeStructureFrame[chip] != null) {
            codeStructureFrame[chip].dispose();
            codeStructureFrame[chip] = null;
            if (mustReOpen) toggleCustomMemoryRangeLoggerComponentFrame(chip);
        }
        if (sourceCodeFrame[chip] != null) {
            sourceCodeFrame[chip].dispose();
            sourceCodeFrame[chip] = null;
            if (mustReOpen) toggleSourceCodeWindow(chip);
        }
        if (callStackFrame[chip] != null) {
            callStackFrame[chip].dispose();
            callStackFrame[chip] = null;
            if (mustReOpen) toggleCallStack(chip);
        }
        if (programmableTimersFrame[chip] != null) {
            programmableTimersFrame[chip].dispose();
            programmableTimersFrame[chip] = null;
            if (mustReOpen) toggleProgrammableTimersWindow(chip);
        }
        if (interruptControllerFrame[chip] != null) {
            interruptControllerFrame[chip].dispose();
            interruptControllerFrame[chip] = null;
            if (mustReOpen) toggleInterruptController(chip);
        }
        closeSpyFrames(chip, mustReOpen);
        if (ITronObjectFrame[chip] != null) {
            ITronObjectFrame[chip].dispose();
            ITronObjectFrame[chip] = null;
            if (mustReOpen) toggleITronObject(chip);
        }
        if (iTronReturnStackFrame[chip] != null) {
            iTronReturnStackFrame[chip].dispose();
            iTronReturnStackFrame[chip] = null;
            if (mustReOpen) toggleITronReturnStack(chip);
        }
    }

    private final void closeSpyFrames(int chip, boolean mustReOpen) {
        if (serialInterfaceFrame[chip] != null) {
            serialInterfaceFrame[chip].dispose();
            serialInterfaceFrame[chip] = null;
            if (mustReOpen) toggleSerialInterfaces(chip);
        }
        if (genericSerialFrame[chip] != null) {
            genericSerialFrame[chip].dispose();
            genericSerialFrame[chip] = null;
            if (mustReOpen) toggleGenericSerialFrame(chip);
        }
        if (ioPortsFrame[chip] != null) {
            ioPortsFrame[chip].dispose();
            ioPortsFrame[chip] = null;
            if (mustReOpen) toggleIoPortsWindow(chip);
        }
    }

    private final void closeAllSpyFrames() {
        closeSpyFrames(Constants.CHIP_FR, false);
        closeSpyFrames(Constants.CHIP_TX, false);
    }

    private void toggleBreakTriggerList(int chip) {
        if (breakTriggerListFrame[chip] == null) {
            breakTriggerListFrame[chip] = new BreakTriggerListFrame("Setup breakpoints and triggers", "breakpoint", true, true, true, true, chip, this, framework.getEmulator(chip), prefs.getTriggers(chip), framework.getPlatform(chip).getMemory());
            addDocumentFrame(chip, breakTriggerListFrame[chip]);
            breakTriggerListFrame[chip].display(true);
        }
        else {
            breakTriggerListFrame[chip].dispose();
            breakTriggerListFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleDisassemblyLog(int chip) {
        if (disassemblyLogFrame[chip] == null) {
            disassemblyLogFrame[chip] = new DisassemblyFrame("Real-time disassembly log", "disassembly_log", true, true, true, true, chip, this, framework.getEmulator(chip), prefs.getDisassemblyAddressRange(chip));
            if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setLogger(disassemblyLogFrame[chip].getLogger());
            addDocumentFrame(chip, disassemblyLogFrame[chip]);
            disassemblyLogFrame[chip].display(true);
        }
        else {
            if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setLogger(null);
            disassemblyLogFrame[chip].dispose();
            disassemblyLogFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleCPUState(int chip) {
        if (cpuStateEditorFrame[chip] == null) {
            cpuStateEditorFrame[chip] = new CPUStateEditorFrame("CPU State", "cpu", true, true, false, true, chip, this, framework.getPlatform(chip).getCpuState(), prefs.getRefreshIntervalMs());
            cpuStateEditorFrame[chip].setEnabled(!framework.isEmulatorPlaying(chip));
            if (disassemblyLogFrame[chip] != null) cpuStateEditorFrame[chip].setLogger(disassemblyLogFrame[chip].getLogger());
            addDocumentFrame(chip, cpuStateEditorFrame[chip]);
            cpuStateEditorFrame[chip].display(true);
        }
        else {
            cpuStateEditorFrame[chip].dispose();
            cpuStateEditorFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleMemoryActivityViewer(int chip) {
        if (memoryActivityViewerFrame[chip] == null) {
            memoryActivityViewerFrame[chip] = new MemoryActivityViewerFrame("Base memory activity viewer (each cell=64k, click to zoom)", "memory_activity", true, true, true, true, chip, this, framework.getPlatform(chip).getMemory());
            addDocumentFrame(chip, memoryActivityViewerFrame[chip]);
            memoryActivityViewerFrame[chip].display(true);
        }
        else {
            memoryActivityViewerFrame[chip].dispose();
            memoryActivityViewerFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleMemoryHexEditor(int chip) {
        if (memoryHexEditorFrame[chip] == null) {
            memoryHexEditorFrame[chip] = new MemoryHexEditorFrame("Memory hex editor", "memory_editor", true, true, true, true, chip, this, framework.getPlatform(chip).getMemory(), framework.getPlatform(chip).getCpuState(), 0, !framework.isEmulatorPlaying(chip));
            addDocumentFrame(chip, memoryHexEditorFrame[chip]);
            memoryHexEditorFrame[chip].display(true);
        }
        else {
            memoryHexEditorFrame[chip].dispose();
            memoryHexEditorFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleScreenEmulator() {
        if (screenEmulatorFrame == null) {
            screenEmulatorFrame = new ScreenEmulatorFrame("Screen emulator", "screen", true, true, true, true, Constants.CHIP_FR, this, ((FrLcd)framework.getPlatform(Constants.CHIP_FR).getLcd()), FrLcd.CAMERA_SCREEN_MEMORY_Y, FrLcd.CAMERA_SCREEN_MEMORY_U, FrLcd.CAMERA_SCREEN_MEMORY_V, FrLcd.CAMERA_SCREEN_WIDTH, FrLcd.CAMERA_SCREEN_HEIGHT, prefs.getRefreshIntervalMs());
            addDocumentFrame(Constants.CHIP_FR, screenEmulatorFrame);
            screenEmulatorFrame.display(true);
        }
        else {
            screenEmulatorFrame.dispose();
            screenEmulatorFrame = null;
        }
        updateState(Constants.CHIP_FR);
    }


    private void toggleComponent4006() {
        if (component4006Frame == null) {
            component4006Frame = new Component4006Frame("Component 4006", "4006", true, true, false, true, Constants.CHIP_FR, this, framework.getPlatform(Constants.CHIP_FR).getMemory(), 0x4006, framework.getPlatform(Constants.CHIP_FR).getCpuState());
            addDocumentFrame(Constants.CHIP_FR, component4006Frame);
            component4006Frame.display(true);
        }
        else {
            component4006Frame.dispose();
            component4006Frame = null;
        }
        updateState(Constants.CHIP_FR);
    }

    private void toggleFrontPanel() {
        if (frontPanelFrame == null) {
            frontPanelFrame = new FrontPanelFrame("Front Panel", "front_panel", true, true, true, true, Constants.CHIP_TX, this, framework.getPlatform(Constants.CHIP_TX).getFrontPanel(), prefs.getFrontPanelName());
            addDocumentFrame(Constants.CHIP_TX, frontPanelFrame);
            frontPanelFrame.display(true);
        }
        else {
            frontPanelFrame.dispose();
            frontPanelFrame = null;
        }
        updateState(Constants.CHIP_FR);
    }


    private void toggleCustomMemoryRangeLoggerComponentFrame(int chip) {
        if (customMemoryRangeLoggerFrame[chip] == null) {
            customMemoryRangeLoggerFrame[chip] = new CustomMemoryRangeLoggerFrame("Custom Logger", "custom_logger", true, true, false, true, chip, this, framework.getPlatform(chip).getMemory(), framework.getPlatform(chip).getCpuState());
            addDocumentFrame(chip, customMemoryRangeLoggerFrame[chip]);
            customMemoryRangeLoggerFrame[chip].display(true);
        }
        else {
            customMemoryRangeLoggerFrame[chip].dispose();
            customMemoryRangeLoggerFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleProgrammableTimersWindow(int chip) {
        if (programmableTimersFrame[chip] == null) {
            programmableTimersFrame[chip] = new ProgrammableTimersFrame("Programmable timers", "timer", true, true, false, true, chip, this, framework.getPlatform(chip).getProgrammableTimers());
            addDocumentFrame(chip, programmableTimersFrame[chip]);
            programmableTimersFrame[chip].display(true);
        }
        else {
            programmableTimersFrame[chip].dispose();
            programmableTimersFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleInterruptController(int chip) {
        if (interruptControllerFrame[chip] == null) {
            interruptControllerFrame[chip] = (chip == Constants.CHIP_FR)
                    ?new FrInterruptControllerFrame("Interrupt controller", "interrupt", true, true, false, true, chip, this, framework.getPlatform(chip).getInterruptController(), framework.getPlatform(chip).getMemory())
                    :new TxInterruptControllerFrame("Interrupt controller", "interrupt", true, true, false, true, chip, this, framework.getPlatform(chip).getInterruptController(), framework.getPlatform(chip).getMemory());
            addDocumentFrame(chip, interruptControllerFrame[chip]);
            interruptControllerFrame[chip].display(true);
        }
        else {
            interruptControllerFrame[chip].dispose();
            interruptControllerFrame[chip] = null;
        }
        updateState(chip);
    }


    private void toggleSerialInterfaces(int chip) {
        if (serialInterfaceFrame[chip] == null) {
            serialInterfaceFrame[chip] = new SerialInterfaceFrame("Serial interfaces", "serial", true, true, false, true, chip, this, framework.getPlatform(chip).getSerialInterfaces());
            addDocumentFrame(chip, serialInterfaceFrame[chip]);
            serialInterfaceFrame[chip].display(true);
        }
        else {
            serialInterfaceFrame[chip].dispose();
            serialInterfaceFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleGenericSerialFrame(int chip) {
        if (genericSerialFrame[chip] == null) {
            genericSerialFrame[chip] = new GenericSerialFrame("Serial devices", "serial_devices", true, true, false, true, chip, this, framework.getPlatform(chip).getSerialDevices());
            addDocumentFrame(chip, genericSerialFrame[chip]);
            genericSerialFrame[chip].display(true);
        }
        else {
            genericSerialFrame[chip].dispose();
            genericSerialFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleIoPortsWindow(int chip) {
        if (ioPortsFrame[chip] == null) {
            ioPortsFrame[chip] = new IoPortsFrame("I/O ports", "io", false, true, false, true, chip, this, framework.getPlatform(chip).getIoPorts());
            addDocumentFrame(chip, ioPortsFrame[chip]);
            ioPortsFrame[chip].display(true);
        }
        else {
            ioPortsFrame[chip].dispose();
            ioPortsFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleAdConverterFrame(int chip) {
        if (adConverterFrame[chip] == null) {
            adConverterFrame[chip] = new AdConverterFrame("A/D converter", "ad_converter", true, true, false, true, chip, this, framework.getPlatform(chip).getAdConverter());
            addDocumentFrame(chip, adConverterFrame[chip]);
            adConverterFrame[chip].display(true);
        }
        else {
            adConverterFrame[chip].dispose();
            adConverterFrame[chip] = null;
        }
        updateState(chip);
    }


    private static void initProgrammableTimerAnimationIcons(String buttonSize) {
        programmableTimersPauseButtonIcon[0] = new ImageIcon(EmulatorUI.class.getResource("images/timer.png"), "Start programmable timer");
        programmableTimersPauseButtonIcon[1] = new ImageIcon(EmulatorUI.class.getResource("images/timer_pause.png"), "Start programmable timer");
        if (BUTTON_SIZE_SMALL.equals(buttonSize)) {
            programmableTimersPauseButtonIcon[0] = new ImageIcon(programmableTimersPauseButtonIcon[0].getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
            programmableTimersPauseButtonIcon[1] = new ImageIcon(programmableTimersPauseButtonIcon[1].getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
        }
    }


    public void setProgrammableTimerAnimationEnabled(final int chip, boolean enabled) {
        if (enabled) {
            // Animate button
            programmableTimersPauseButtonAnimationTimer[chip] = new java.util.Timer(false);
            programmableTimersPauseButtonAnimationTimer[chip].scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    programmableTimersPauseButtonAnimationIndex[chip] = 1 - programmableTimersPauseButtonAnimationIndex[chip];
                    programmableTimersButton[chip].setIcon(programmableTimersPauseButtonIcon[programmableTimersPauseButtonAnimationIndex[chip]]);
                }
            }, 0, 800 /*ms*/);
        }
        else {
            // Stop button animation if active
            if (programmableTimersPauseButtonAnimationTimer[chip] != null)  {
                programmableTimersPauseButtonAnimationTimer[chip].cancel();
                programmableTimersPauseButtonAnimationTimer[chip] = null;
                programmableTimersButton[chip].setIcon(programmableTimersPauseButtonIcon[0]);
            }
        }
    }


    private void toggleCallStack(int chip) {
        if (callStackFrame[chip] == null) {
            callStackFrame[chip] = new CallStackFrame("Call Stack logger", "call_stack", true, true, false, true, chip, this, framework.getEmulator(chip), framework.getPlatform(chip).getCpuState(), framework.getCodeStructure(chip));
            callStackFrame[chip].setAutoRefresh(framework.isEmulatorPlaying(chip));
            addDocumentFrame(chip, callStackFrame[chip]);
            callStackFrame[chip].display(true);
        }
        else {
            callStackFrame[chip].dispose();
            callStackFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleITronObject(int chip) {
        if (ITronObjectFrame[chip] == null) {
            ITronObjectFrame[chip] = new ITronObjectFrame("µITRON Object Status", "os", true, true, false, true, chip, this, framework.getPlatform(chip), framework.getCodeStructure(chip));
            ITronObjectFrame[chip].enableUpdate(!framework.isEmulatorPlaying(chip));
            if (!framework.isEmulatorPlaying(chip)) {
                ITronObjectFrame[chip].updateAllLists(chip);
            }
            addDocumentFrame(chip, ITronObjectFrame[chip]);
            ITronObjectFrame[chip].display(true);
        }
        else {
            ITronObjectFrame[chip].dispose();
            ITronObjectFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleITronReturnStack(int chip) {
        if (iTronReturnStackFrame[chip] == null) {
            iTronReturnStackFrame[chip] = new ITronReturnStackFrame("µITRON Return Stack", "os", true, true, false, true, chip, this, framework.getPlatform(chip), framework.getCodeStructure(chip));
            iTronReturnStackFrame[chip].enableUpdate(!framework.isEmulatorPlaying(chip));
            if (!framework.isEmulatorPlaying(chip)) {
                iTronReturnStackFrame[chip].updateAll();
            }
            addDocumentFrame(chip, iTronReturnStackFrame[chip]);
            iTronReturnStackFrame[chip].display(true);
        }
        else {
            iTronReturnStackFrame[chip].dispose();
            iTronReturnStackFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleCodeStructureWindow(int chip) {
        if (codeStructureFrame[chip] == null) {
            codeStructureFrame[chip] = new CodeStructureFrame("Code structure", "code_structure", true, true, true, true, chip, this, framework.getPlatform(chip).getCpuState(), framework.getCodeStructure(chip));
            addDocumentFrame(chip, codeStructureFrame[chip]);
            codeStructureFrame[chip].display(true);
        }
        else {
            codeStructureFrame[chip].dispose();
            codeStructureFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleSourceCodeWindow(int chip) {
        if (sourceCodeFrame[chip] == null) {
            sourceCodeFrame[chip] = new SourceCodeFrame("Source code", "source", true, true, true, true, chip, this, framework.getPlatform(chip).getCpuState(), framework.getCodeStructure(chip));
            addDocumentFrame(chip, sourceCodeFrame[chip]);
            sourceCodeFrame[chip].display(true);
        }
        else {
            sourceCodeFrame[chip].dispose();
            sourceCodeFrame[chip] = null;
        }
        updateState(chip);
    }

    /**
     * Add a new window to the desktop
     * @param chip determines if we speak about FR or TX sides
     * @param frame
     */
    public void addDocumentFrame(int chip, DocumentFrame frame) {
        mdiPane[chip].add(frame);
    }


    /**
     * Called back by frames to inform UI that they are being closed
     * @param frame
     */
    public void frameClosing(DocumentFrame frame) {
        if (frame == component4006Frame) {
            toggleComponent4006(); return;
        }
        else if (frame == screenEmulatorFrame) {
            toggleScreenEmulator(); return;
        }
        else if (frame == frontPanelFrame) {
            toggleFrontPanel(); return;
        }
        else for (int chip = 0; chip < 2; chip++) {
                if (frame == cpuStateEditorFrame[chip]) {
                    toggleCPUState(chip); return;
                }
                else if (frame == disassemblyLogFrame[chip]) {
                    toggleDisassemblyLog(chip); return;
                }
                else if (frame == breakTriggerListFrame[chip]) {
                    toggleBreakTriggerList(chip); return;
                }
                else if (frame == memoryActivityViewerFrame[chip]) {
                    toggleMemoryActivityViewer(chip); return;
                }
                else if (frame == memoryHexEditorFrame[chip]) {
                    toggleMemoryHexEditor(chip); return;
                }
                else if (frame == customMemoryRangeLoggerFrame[chip]) {
                    toggleCustomMemoryRangeLoggerComponentFrame(chip); return;
                }
                else if (frame == codeStructureFrame[chip]) {
                    toggleCodeStructureWindow(chip); return;
                }
                else if (frame == sourceCodeFrame[chip]) {
                    toggleSourceCodeWindow(chip); return;
                }
                else if (frame == callStackFrame[chip]) {
                    toggleCallStack(chip); return;
                }
                else if (frame == programmableTimersFrame[chip]) {
                    toggleProgrammableTimersWindow(chip) ; return;
                }
                else if (frame == interruptControllerFrame[chip]) {
                    toggleInterruptController(chip); return;
                }
                else if (frame == serialInterfaceFrame[chip]) {
                    toggleSerialInterfaces(chip); return;
                }
                else if (frame == genericSerialFrame[chip]) {
                    toggleGenericSerialFrame(chip); return;
                }
                else if (frame == ioPortsFrame[chip]) {
                    toggleIoPortsWindow(chip); return;
                }
                else if (frame == adConverterFrame[chip]) {
                    toggleAdConverterFrame(chip); return;
                }
                else if (frame == ITronObjectFrame[chip]) {
                    toggleITronObject(chip); return;
                }
                else if (frame == iTronReturnStackFrame[chip]) {
                    toggleITronReturnStack(chip); return;
                }
            }
        System.err.println("EmulatorUI.frameClosing : Unknown frame is being closed. Please add handler for " + frame.getClass().getSimpleName());
    }

    public void updateStates() {
        updateState(Constants.CHIP_FR);
        updateState(Constants.CHIP_TX);
    }

    public void updateState(int chip) {
        if (chip == Constants.CHIP_FR) {
            component4006MenuItem.setSelected(component4006Frame != null);
            screenEmulatorMenuItem.setSelected(screenEmulatorFrame != null);

            component4006MenuItem.setEnabled(framework.isImageLoaded(Constants.CHIP_FR));
            component4006Button.setEnabled(framework.isImageLoaded(Constants.CHIP_FR));
            screenEmulatorMenuItem.setEnabled(framework.isImageLoaded(Constants.CHIP_FR));
            screenEmulatorButton.setEnabled(framework.isImageLoaded(Constants.CHIP_FR));

            generateSysSymbolsMenuItem.setEnabled(framework.isImageLoaded(Constants.CHIP_FR));
        }

        if (chip == Constants.CHIP_FR) {
            frontPanelMenuItem.setSelected(frontPanelFrame != null);

            frontPanelMenuItem.setEnabled(framework.isImageLoaded(Constants.CHIP_TX));
        }

        // Menus and buttons enabled or not
        codeStructureMenuItem[chip].setEnabled(framework.getCodeStructure(chip) != null);
        codeStructureButton[chip].setEnabled(framework.getCodeStructure(chip) != null);
        sourceCodeMenuItem[chip].setEnabled(framework.getCodeStructure(chip) != null);
        sourceCodeButton[chip].setEnabled(framework.getCodeStructure(chip) != null);

        // CheckboxMenuItem checked or not
        cpuStateMenuItem[chip].setSelected(cpuStateEditorFrame[chip] != null);
        disassemblyMenuItem[chip].setSelected(disassemblyLogFrame[chip] != null);
        memoryActivityViewerMenuItem[chip].setSelected(memoryActivityViewerFrame[chip] != null);
        memoryHexEditorMenuItem[chip].setSelected(memoryHexEditorFrame[chip] != null);
        customMemoryRangeLoggerMenuItem[chip].setSelected(customMemoryRangeLoggerFrame[chip] != null);
        codeStructureMenuItem[chip].setSelected(codeStructureFrame[chip] != null);
        sourceCodeMenuItem[chip].setSelected(sourceCodeFrame[chip] != null);
        programmableTimersMenuItem[chip].setSelected(programmableTimersFrame[chip] != null);
        interruptControllerMenuItem[chip].setSelected(interruptControllerFrame[chip] != null);
        serialInterfacesMenuItem[chip].setSelected(serialInterfaceFrame[chip] != null);
        ioPortsMenuItem[chip].setSelected(ioPortsFrame[chip] != null);
        if (chip == Constants.CHIP_TX) {
            serialDevicesMenuItem[chip].setSelected(genericSerialFrame[chip] != null);
            adConverterMenuItem[chip].setSelected(adConverterFrame[chip] != null);
        }

        analyseMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        analyseButton[chip].setEnabled(framework.isImageLoaded(chip));

        cpuStateMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        cpuStateButton[chip].setEnabled(framework.isImageLoaded(chip));
        disassemblyMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        disassemblyButton[chip].setEnabled(framework.isImageLoaded(chip));
        memoryActivityViewerMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        memoryActivityViewerButton[chip].setEnabled(framework.isImageLoaded(chip));
        memoryHexEditorMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        memoryHexEditorButton[chip].setEnabled(framework.isImageLoaded(chip));
        customMemoryRangeLoggerMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        customMemoryRangeLoggerButton[chip].setEnabled(framework.isImageLoaded(chip));
        programmableTimersMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        programmableTimersButton[chip].setEnabled(framework.isImageLoaded(chip));
        interruptControllerMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        interruptControllerButton[chip].setEnabled(framework.isImageLoaded(chip));
        if (chip == Constants.CHIP_TX) {
            adConverterMenuItem[chip].setEnabled(framework.isImageLoaded(chip)); adConverterButton[chip].setEnabled(framework.isImageLoaded(chip));
            frontPanelMenuItem.setEnabled(framework.isImageLoaded(chip)); frontPanelButton.setEnabled(framework.isImageLoaded(chip));
        }
        callStackMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        callStackButton[chip].setEnabled(framework.isImageLoaded(chip));
        iTronObjectMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        iTronObjectButton[chip].setEnabled(framework.isImageLoaded(chip));
        iTronReturnStackMenuItem[chip].setEnabled(framework.isImageLoaded(chip));

        saveLoadMemoryMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        saveLoadMemoryButton[chip].setEnabled(framework.isImageLoaded(chip));

        stopMenuItem[chip].setEnabled(framework.isImageLoaded(chip));
        stopButton[chip].setEnabled(framework.isImageLoaded(chip));

        if (framework.isImageLoaded(chip)) {
            // Depends whether emulator is playing or not
            loadMenuItem[chip].setEnabled(!framework.isEmulatorPlaying(chip)); loadButton[chip].setEnabled(!framework.isEmulatorPlaying(chip));
            playMenuItem[chip].setEnabled(!framework.isEmulatorPlaying(chip)); playButton[chip].setEnabled(!framework.isEmulatorPlaying(chip));
            debugMenuItem[chip].setEnabled(!framework.isEmulatorPlaying(chip)); debugButton[chip].setEnabled(!framework.isEmulatorPlaying(chip));
            pauseMenuItem[chip].setEnabled(framework.isEmulatorPlaying(chip)); pauseButton[chip].setEnabled(framework.isEmulatorPlaying(chip));
            stepMenuItem[chip].setEnabled(!framework.isEmulatorPlaying(chip)); stepButton[chip].setEnabled(!framework.isEmulatorPlaying(chip));
            chipOptionsMenuItem[chip].setEnabled(!framework.isEmulatorPlaying(chip)); chipOptionsButton[chip].setEnabled(!framework.isEmulatorPlaying(chip));
            // coderat: opening of IO Ports window or other spying window with runing emulation may cause unpredictable results,
            // because it runs asynchronously. In constructor a pin will be inserted in the middle of connection by 2
            // consequent calls that may fail to transfer value correctly in another thread !
            if (chip == Constants.CHIP_TX) {
                serialDevicesMenuItem[chip].setEnabled(!framework.isEmulatorPlaying(chip)); serialDevicesButton[chip].setEnabled(!framework.isEmulatorPlaying(chip));
            }
            serialInterfacesMenuItem[chip].setEnabled(!framework.isEmulatorPlaying(chip)); serialInterfacesButton[chip].setEnabled(!framework.isEmulatorPlaying(chip));
            ioPortsMenuItem[chip].setEnabled(!framework.isEmulatorPlaying(chip)); ioPortsButton[chip].setEnabled(!framework.isEmulatorPlaying(chip));

            // Editable components
            if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setEditable(!framework.isEmulatorPlaying(chip));
            if (memoryHexEditorFrame[chip] != null) memoryHexEditorFrame[chip].setEditable(!framework.isEmulatorPlaying(chip));
            if (callStackFrame[chip] != null) callStackFrame[chip].setAutoRefresh(framework.isEmulatorPlaying(chip));
            if (ITronObjectFrame[chip] != null) ITronObjectFrame[chip].enableUpdate(!framework.isEmulatorPlaying(chip));
            if (iTronReturnStackFrame[chip] != null) iTronReturnStackFrame[chip].enableUpdate(!framework.isEmulatorPlaying(chip));
            if (breakTriggerListFrame[chip] != null) breakTriggerListFrame[chip].setEditable(!framework.isEmulatorPlaying(chip));
            if (sourceCodeFrame[chip] != null) sourceCodeFrame[chip].setEditable(!framework.isEmulatorPlaying(chip));
            if (interruptControllerFrame[chip] != null) interruptControllerFrame[chip].setEditable(!framework.isEmulatorPlaying(chip));
            if (disassemblyLogFrame[chip] != null) disassemblyLogFrame[chip].setEditable(!framework.isEmulatorPlaying(chip));
        }
        else {
            loadMenuItem[chip].setEnabled(true); loadButton[chip].setEnabled(true);
            playMenuItem[chip].setEnabled(false); playButton[chip].setEnabled(false);
            debugMenuItem[chip].setEnabled(false); debugButton[chip].setEnabled(false);
            pauseMenuItem[chip].setEnabled(false); pauseButton[chip].setEnabled(false);
            stepMenuItem[chip].setEnabled(false); stepButton[chip].setEnabled(false);
            chipOptionsMenuItem[chip].setEnabled(false); chipOptionsButton[chip].setEnabled(false);
            if (chip == Constants.CHIP_TX) {
                serialDevicesMenuItem[chip].setEnabled(false); serialDevicesButton[chip].setEnabled(false);
            }
            serialInterfacesMenuItem[chip].setEnabled(false); serialInterfacesButton[chip].setEnabled(false);
            ioPortsMenuItem[chip].setEnabled(false); ioPortsButton[chip].setEnabled(false);

            // Editable components  TODO does it make sense ? And why true ?
            if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setEditable(true);
            if (memoryHexEditorFrame[chip] != null) memoryHexEditorFrame[chip].setEditable(true);
            if (callStackFrame[chip] != null) callStackFrame[chip].setAutoRefresh(false);
            if (ITronObjectFrame[chip] != null) ITronObjectFrame[chip].enableUpdate(true);
            if (iTronReturnStackFrame[chip] != null) iTronReturnStackFrame[chip].enableUpdate(true);
            if (breakTriggerListFrame[chip] != null) breakTriggerListFrame[chip].setEditable(true);
            if (sourceCodeFrame[chip] != null) sourceCodeFrame[chip].setEditable(true);
            if (interruptControllerFrame[chip] != null) interruptControllerFrame[chip].setEditable(true);
            if (disassemblyLogFrame[chip] != null) disassemblyLogFrame[chip].setEditable(true);
        }
    }

    public void startEmulator(final int chip, EmulationFramework.ExecutionMode executionMode, Integer endAddress) {
        //System.err.println("Start request for " + Constants.CHIP_LABEL[chip]);
        framework.prepareBreakTriggers(chip, executionMode, endAddress);
        setStatusText(chip, executionMode.getLabel() + " session in progress...");
        statusBar[chip].setBackground(executionMode == EmulationFramework.ExecutionMode.DEBUG ? STATUS_BGCOLOR_DEBUG : STATUS_BGCOLOR_RUN);
        framework.prepareEmulation(chip);
        updateState(chip);
        if (prefs.isSyncPlay()) {
            // Start the other one too
            int otherChip = 1 - chip;
            if (framework.isImageLoaded(otherChip)) {

                setStatusText(otherChip, "Sync play...");
                statusBar[otherChip].setBackground(STATUS_BGCOLOR_RUN);
                EmulationFramework.ExecutionMode altExecutionMode;
                switch (executionMode) {
                    case STEP:
                        altExecutionMode = prefs.getAltExecutionModeForSyncedCpuUponStep(chip);
                        break;
                    case DEBUG:
                        altExecutionMode = prefs.getAltExecutionModeForSyncedCpuUponDebug(chip);
                        break;
                    default:
                        altExecutionMode = EmulationFramework.ExecutionMode.RUN;
                }
                framework.prepareBreakTriggers(otherChip, altExecutionMode, null);
                setStatusText(otherChip, altExecutionMode.getLabel() + " session in progress...");
                statusBar[otherChip].setBackground(altExecutionMode == EmulationFramework.ExecutionMode.DEBUG ? STATUS_BGCOLOR_DEBUG : STATUS_BGCOLOR_RUN);
                framework.prepareEmulation(otherChip);
                updateState(otherChip);
            }
            else {
                JOptionPane.showMessageDialog(this, "Cannot start " + Constants.CHIP_LABEL[otherChip] + " in sync because no image is loaded.\nOnly " + Constants.CHIP_LABEL[chip] + " will run.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
        //System.err.println("Requesting clock start");
        framework.getMasterClock().start();
    }


    public void playToAddress(int chip, EmulationFramework.ExecutionMode executionMode, Integer endAddress) {
        framework.prepareBreakTriggers(chip, executionMode, endAddress);
        setStatusText(chip, executionMode.getLabel() + " session in progress...");
        statusBar[chip].setBackground(executionMode == EmulationFramework.ExecutionMode.DEBUG? STATUS_BGCOLOR_DEBUG : STATUS_BGCOLOR_RUN);
        startEmulator(chip, executionMode, endAddress);
    }

    private void signalEmulatorStopped(int chip) {
        // TODO this should iterate on all windows. A NOP onEmulatorStop() should exist in DocumentFrame
        // coderat: do not agree, because function updateState(chip)->setEnabled() is called for example
        //          in onNormalExit() after call this one. So not all windows need call from here.
        if (sourceCodeFrame[chip] != null) {
            sourceCodeFrame[chip].onEmulatorStop();
        }
        if (ITronObjectFrame[chip] != null) {
            ITronObjectFrame[chip].onEmulatorStop(chip);
        }
        if (iTronReturnStackFrame[chip] != null) {
            iTronReturnStackFrame[chip].onEmulatorStop(chip);
        }
    }


    //Quit the application.
    protected void quit() {
        dispose();
    }

    public TrackingMemoryActivityListener getTrackingMemoryActivityListener(int chip) {
        if (memoryActivityViewerFrame[chip] != null) {
            return memoryActivityViewerFrame[chip].getTrackingMemoryActivityListener();
        }
        else {
            return null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        closeAllFrames();
        saveMainWindowSettings();
        framework.dispose();
        Prefs.save(prefs);
        System.exit(0);
    }

    public void jumpToSource(int chip, Function function) {
        if (framework.getCodeStructure(chip) != null) {
            if (sourceCodeFrame[chip] == null) {
                toggleSourceCodeWindow(chip);
            }
            sourceCodeFrame[chip].writeFunction(function);
        }
    }

    public void jumpToSource(int chip, int address) {
        if (framework.getCodeStructure(chip) != null) {
            if (sourceCodeFrame[chip] == null) {
                toggleSourceCodeWindow(chip);
            }
            if (!sourceCodeFrame[chip].exploreAddress(address, true)) {
                JOptionPane.showMessageDialog(this, "No function found at address 0x" + Format.asHex(address, 8), "Cannot explore function", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void jumpToMemory(int chip, int address) {
        if (memoryHexEditorFrame[chip] == null) {
            toggleMemoryHexEditor(chip);
        }
        memoryHexEditorFrame[chip].jumpToAddress(address, 4);
    }

    public void jumpToContext(int chip, int taskId) {
        if (framework.getCodeStructure(chip) != null) {
            if (iTronReturnStackFrame[chip] == null) {
                toggleITronReturnStack(chip);
            }
            iTronReturnStackFrame[chip].exploreTask(taskId);
        }
    }

    public void onBreaktriggersChange(int chip) {
        if (sourceCodeFrame[chip] != null) {
            sourceCodeFrame[chip].updateBreakTriggers();
        }
        if (breakTriggerListFrame[chip] != null) {
            breakTriggerListFrame[chip].updateBreaktriggers();
        }
    }

    // TODO Move to EmulatorPlatform ?
    public void toggleProgrammableTimers(int chip) {
        enableProgrammableTimers(chip, !framework.getPlatform(chip).getProgrammableTimers()[0].isActive());
    }

    private void enableProgrammableTimers(int chip, boolean active) {
        ProgrammableTimer[] timers = framework.getPlatform(chip).getProgrammableTimers();
        for (ProgrammableTimer timer : timers) {
            timer.setActive(active);
        }
        // Start/stop button animation
        setProgrammableTimerAnimationEnabled(chip, !active);
        // Update window status, if open
        if (programmableTimersFrame[chip] != null) {
            programmableTimersFrame[chip].updateState(active);
        }
    }
}
