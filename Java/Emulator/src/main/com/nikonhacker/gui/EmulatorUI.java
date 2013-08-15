package com.nikonhacker.gui;

/*
 * MDI Layout inspired by InternalFrameDemo from the Java Tutorial -
 * http://docs.oracle.com/javase/tutorial/uiswing/components/internalframe.html
 */

/* TODO : track executions in non CODE area */
/* TODO : memory viewer : add checkbox to toggle rotation, button to clear, ... */

import com.nikonhacker.*;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.disassembly.fr.Dfr;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.Dtx;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.*;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;
import com.nikonhacker.emu.memory.listener.fr.Expeed4006IoListener;
import com.nikonhacker.emu.memory.listener.fr.ExpeedIoListener;
import com.nikonhacker.emu.memory.listener.fr.ExpeedPinIoListener;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.adConverter.AdConverter;
import com.nikonhacker.emu.peripherials.adConverter.AdValueProvider;
import com.nikonhacker.emu.peripherials.adConverter.tx.TxAdConverter;
import com.nikonhacker.emu.peripherials.adConverter.tx.TxAdPrefsValueProvider;
import com.nikonhacker.emu.peripherials.clock.ClockGenerator;
import com.nikonhacker.emu.peripherials.clock.fr.FrClockGenerator;
import com.nikonhacker.emu.peripherials.clock.tx.TxClockGenerator;
import com.nikonhacker.emu.peripherials.dmaController.DmaController;
import com.nikonhacker.emu.peripherials.dmaController.tx.TxDmaController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.Pin;
import com.nikonhacker.emu.peripherials.ioPort.fr.FrIoPort;
import com.nikonhacker.emu.peripherials.ioPort.tx.TxIoPort;
import com.nikonhacker.emu.peripherials.ioPort.util.FixedSourceComponent;
import com.nikonhacker.emu.peripherials.keyCircuit.KeyCircuit;
import com.nikonhacker.emu.peripherials.keyCircuit.tx.TxKeyCircuit;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.fr.FrFreeRunTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.fr.FrReloadTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.tx.TxInputCaptureTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.tx.TxTimer;
import com.nikonhacker.emu.peripherials.realtimeClock.RealtimeClock;
import com.nikonhacker.emu.peripherials.realtimeClock.tx.TxRealtimeClock;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St95040;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St950x0;
import com.nikonhacker.emu.peripherials.serialInterface.fr.FrSerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.lcd.LcdDriver;
import com.nikonhacker.emu.peripherials.serialInterface.tx.TxHSerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.tx.TxSerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.util.SpiBus;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.*;
import com.nikonhacker.encoding.FirmwareDecoder;
import com.nikonhacker.encoding.FirmwareEncoder;
import com.nikonhacker.encoding.FirmwareFormatException;
import com.nikonhacker.gui.component.ad.AdConverterFrame;
import com.nikonhacker.gui.component.analyse.AnalyseProgressDialog;
import com.nikonhacker.gui.component.analyse.GenerateSysSymbolsDialog;
import com.nikonhacker.gui.component.breakTrigger.BreakTriggerListFrame;
import com.nikonhacker.gui.component.callStack.CallStackFrame;
import com.nikonhacker.gui.component.codeStructure.CodeStructureFrame;
import com.nikonhacker.gui.component.cpu.CPUStateEditorFrame;
import com.nikonhacker.gui.component.disassembly.DisassemblyFrame;
import com.nikonhacker.gui.component.image.BackgroundImagePanel;
import com.nikonhacker.gui.component.interruptController.FrInterruptControllerFrame;
import com.nikonhacker.gui.component.interruptController.InterruptControllerFrame;
import com.nikonhacker.gui.component.interruptController.TxInterruptControllerFrame;
import com.nikonhacker.gui.component.ioPort.IoPortsFrame;
import com.nikonhacker.gui.component.itron.ITronObjectFrame;
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
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
    private static final String[] COMMAND_LOAD_STATE                         = {"FR_LOAD_STATE", "TX_LOAD_STATE"};
    private static final String[] COMMAND_SAVE_STATE                         = {"FR_SAVE_STATE", "TX_SAVE_STATE"};
    private static final String[] COMMAND_SAVE_LOAD_MEMORY                   = {"FR_SAVE_LOAD_MEMORY", "TX_SAVE_LOAD_MEMORY"};
    private static final String[] COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW       = {"FR_TOGGLE_CODE_STRUCTURE_WINDOW", "TX_TOGGLE_CODE_STRUCTURE_WINDOW"};
    private static final String[] COMMAND_TOGGLE_SOURCE_CODE_WINDOW          = {"FR_TOGGLE_SOURCE_CODE_WINDOW", "TX_TOGGLE_SOURCE_CODE_WINDOW"};
    private static final String[] COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW  = {"FR_COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW", "TX_COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW"};
    private static final String[] COMMAND_TOGGLE_CALL_STACK_WINDOW           = {"FR_TOGGLE_CALL_STACK_WINDOW", "TX_TOGGLE_CALL_STACK_WINDOW"};
    private static final String[] COMMAND_TOGGLE_ITRON_OBJECT_WINDOW         = {"FR_TOGGLE_ITRON_OBJECT_WINDOW", "TX_TOGGLE_ITRON_OBJECT_WINDOW"};
    private static final String[] COMMAND_CHIP_OPTIONS                       = {"FR_OPTIONS", "TX_OPTIONS"};

    private static final String COMMAND_GENERATE_SYS_SYMBOLS         = "GENERATE_SYS_SYMBOLS";
    private static final String COMMAND_TOGGLE_COMPONENT_4006_WINDOW = "TOGGLE_COMPONENT_4006_WINDOW";
    private static final String COMMAND_TOGGLE_SCREEN_EMULATOR       = "TOGGLE_SCREEN_EMULATOR";
    private static final String COMMAND_UI_OPTIONS                   = "UI_OPTIONS";
    private static final String COMMAND_DECODE                       = "DECODE";
    private static final String COMMAND_ENCODE                       = "ENCODE";
    private static final String COMMAND_QUIT                         = "QUIT";
    private static final String COMMAND_ABOUT                        = "ABOUT";
    private static final String COMMAND_TEST                         = "TEST";

    private static final int[] CHIP_MODIFIER = new int[]{ActionEvent.CTRL_MASK, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK};

    public static final String BUTTON_SIZE_SMALL  = "SMALL";
    public static final String BUTTON_SIZE_MEDIUM = "MEDIUM";
    public static final String BUTTON_SIZE_LARGE  = "LARGE";

    private static final String BUTTON_PROPERTY_KEY_ICON = "icon";

    // Business constants

    private static final int BASE_ADDRESS_FUNCTION_CALL[] = {0xFFFFFFF0, 0x10001000};

    private static final int CAMERA_SCREEN_MEMORY_Y = 0xCE57DC60;
    private static final int CAMERA_SCREEN_MEMORY_U = CAMERA_SCREEN_MEMORY_Y + 0x64000;
    private static final int CAMERA_SCREEN_MEMORY_V = CAMERA_SCREEN_MEMORY_Y + 2 * 0x64000;
    private static final int CAMERA_SCREEN_WIDTH    = 640;
    private static final int CAMERA_SCREEN_HEIGHT   = 480;

    private static final String[] CPUSTATE_ENTRY_NAME = {"FrCPUState", "TxCPUState"};
    private static final String[] MEMORY_ENTRY_NAME   = {"FrMemory", "TxMemory"};

    private static final String STATUS_DEFAULT_TEXT   = "Ready";

    public static final Color STATUS_BGCOLOR_DEFAULT = Color.LIGHT_GRAY;
    public static final Color STATUS_BGCOLOR_RUN     = Color.GREEN;
    public static final Color STATUS_BGCOLOR_DEBUG   = Color.ORANGE;
    public static final Color STATUS_BGCOLOR_BREAK   = new Color(255, 127, 127);

    /** Type of run */
    public static enum RunMode {
        /** Run without any break */
        RUN("Run"),

        /** Run without all break triggers enabled */
        DEBUG("Debug"),

        /** Just execute one instruction, then break, */
        STEP("Step");
        private String label;

        RunMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

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

    private JCheckBoxMenuItem[] disassemblyMenuItem             = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] memoryActivityViewerMenuItem    = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] customMemoryRangeLoggerMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] callStackMenuItem               = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] iTronObjectMenuItem             = new JCheckBoxMenuItem[2];

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

    private ITronObjectFrame[] ITronObjectFrame = new ITronObjectFrame[2];

    // Misc UI related fields
    private String[]  statusText     = {STATUS_DEFAULT_TEXT, STATUS_DEFAULT_TEXT};
    private JLabel[]  statusBar      = new JLabel[2];
    private JSlider[] intervalSlider = new JSlider[2];

    private static ImageIcon[]       programmableTimersPauseButtonIcon           = new ImageIcon[2];
    private        int[]             programmableTimersPauseButtonAnimationIndex = new int[2];
    private        java.util.Timer[] programmableTimersPauseButtonAnimationTimer = new java.util.Timer[2];

    // Business fields
    private static File[] imageFile = new File[2];

    private final MasterClock masterClock = new MasterClock();
    private       Emulator[]  emulator    = new Emulator[2];
    private       Platform[]  platform    = new Platform[2];
    private       St950x0 eeprom;

    private boolean[] isImageLoaded     = {false, false};
    private boolean[] isEmulatorPlaying = {false, false};


    private CodeStructure[] codeStructure = new CodeStructure[2];

    private long lastUpdateCycles[] = {0, 0};
    private long lastUpdateTime[]   = {0, 0};

    private Prefs prefs = new Prefs();


    public static void main(String[] args) throws EmulationException, IOException, ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException {

        // Workaround for JDK bug - https://code.google.com/p/nikon-firmware-tools/issues/detail?id=17
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        if (args.length > 0) {
            if (new File(args[0]).exists()) {
                imageFile[Constants.CHIP_FR] = new File(args[0]);
                if (args.length > 1) {
                    if (new File(args[1]).exists()) {
                        imageFile[Constants.CHIP_TX] = new File(args[1]);
                    }
                }
            }
        }

        initProgrammableTimerAnimationIcons(BUTTON_SIZE_SMALL);

        // Using System L&F allows transparent window icon in the title bar on Windows, but causes a Sort exception in JDK 1.7 because of stricter sort - see http://www.java.net/node/700601
        // Use old less strict sort to avoid the Exception
        // Works, but ugly
        //System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // Tried to set Nimbus L&F to be able to reduce component size. Not successful till now.
//        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//            if ("Nimbus".equals(info.getName())) {
//                UIManager.setLookAndFeel(info.getClassName());
//                break;
//            }
//        }

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
        super(ApplicationInfo.getName() + " v" + ApplicationInfo.getVersion());

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        prefs = Prefs.load();
        // Apply register label prefs immediately
        FrCPUState.initRegisterLabels(prefs.getOutputOptions(Constants.CHIP_FR));
        TxCPUState.initRegisterLabels(prefs.getOutputOptions(Constants.CHIP_TX));

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
                loadImage(chip);
            }
        }

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

    public MasterClock getMasterClock() {
        return masterClock;
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
                if (emulator[chip] != null) {
                    long totalCycles = emulator[chip].getTotalCycles();
                    long now = System.currentTimeMillis();
                    long cps;
                    try {
                        cps = (1000 * (totalCycles - lastUpdateCycles[chip])) / (now - lastUpdateTime[chip]);
                    } catch (Exception e) {
                        cps = -1;
                    }

                    lastUpdateCycles[chip] = totalCycles;
                    lastUpdateTime[chip] = now;
                    statusBar[chip].setText(statusText[chip] + " (" + masterClock.getFormatedTotalElapsedTimeMs() + " or " + totalCycles + " cycles emulated. Current speed is " + (cps < 0 ? "?" : ("" + cps)) + "cps)");
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
        }

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        disassemblyButton[chip] = makeButton("disassembly_log", COMMAND_TOGGLE_DISASSEMBLY_WINDOW[chip], "Real time " + Constants.CHIP_LABEL[chip] + " disassembly log", "Disassembly");
        bar.add(disassemblyButton[chip]);
        memoryActivityViewerButton[chip] = makeButton("memory_activity", COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER[chip], Constants.CHIP_LABEL[chip] + " memory activity viewer", "Activity");
        bar.add(memoryActivityViewerButton[chip]);
        customMemoryRangeLoggerButton[chip] = makeButton("custom_logger", COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW[chip], "Custom " + Constants.CHIP_LABEL[chip] + " logger", "Custom logger");
        bar.add(customMemoryRangeLoggerButton[chip]);
        callStackButton[chip] = makeButton("call_stack", COMMAND_TOGGLE_CALL_STACK_WINDOW[chip], Constants.CHIP_LABEL[chip] + " call stack window", "CallStack");
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
            if (chip == Constants.CHIP_FR) loadMenuItem[chip].setMnemonic(KeyEvent.VK_L);
            loadMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, CHIP_MODIFIER[chip]));
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

        for (int chip = 0; chip < 2; chip++) {
            //Load state
            tmpMenuItem = new JMenuItem("Load " + Constants.CHIP_LABEL[chip] + "  state");
            tmpMenuItem.setActionCommand(COMMAND_LOAD_STATE[chip]);
            tmpMenuItem.addActionListener(this);
            fileMenu.add(tmpMenuItem);

            //Save state
            tmpMenuItem = new JMenuItem("Save " + Constants.CHIP_LABEL[chip] + " state");
            tmpMenuItem.setActionCommand(COMMAND_SAVE_STATE[chip]);
            tmpMenuItem.addActionListener(this);
            fileMenu.add(tmpMenuItem);

            fileMenu.add(new JSeparator());
        }

        //quit
        tmpMenuItem = new JMenuItem("Quit");
        tmpMenuItem.setMnemonic(KeyEvent.VK_Q);
        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
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
            //if (chip == Constants.CHIP_FR) playMenuItem[chip].setMnemonic(KeyEvent.VK_E);
            playMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, CHIP_MODIFIER[chip]));
            playMenuItem[chip].setActionCommand(COMMAND_EMULATOR_PLAY[chip]);
            playMenuItem[chip].addActionListener(this);
            runMenu.add(playMenuItem[chip]);

            //emulator debug
            debugMenuItem[chip] = new JMenuItem("Debug " + Constants.CHIP_LABEL[chip] + " emulator");
            //if (chip == Constants.CHIP_FR) debugMenuItem[chip].setMnemonic(KeyEvent.VK_G);
            debugMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, CHIP_MODIFIER[chip]));
            debugMenuItem[chip].setActionCommand(COMMAND_EMULATOR_DEBUG[chip]);
            debugMenuItem[chip].addActionListener(this);
            runMenu.add(debugMenuItem[chip]);

            //emulator pause
            pauseMenuItem[chip] = new JMenuItem("Pause " + Constants.CHIP_LABEL[chip] + " emulator");
            //if (chip == Constants.CHIP_FR) pauseMenuItem[chip].setMnemonic(KeyEvent.VK_P);
            pauseMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, CHIP_MODIFIER[chip]));
            pauseMenuItem[chip].setActionCommand(COMMAND_EMULATOR_PAUSE[chip]);
            pauseMenuItem[chip].addActionListener(this);
            runMenu.add(pauseMenuItem[chip]);

            //emulator step
            stepMenuItem[chip] = new JMenuItem("Step " + Constants.CHIP_LABEL[chip] + " emulator");
            //if (chip == Constants.CHIP_FR) stepMenuItem[chip].setMnemonic(KeyEvent.VK_T);
            stepMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, CHIP_MODIFIER[chip]));
            stepMenuItem[chip].setActionCommand(COMMAND_EMULATOR_STEP[chip]);
            stepMenuItem[chip].addActionListener(this);
            runMenu.add(stepMenuItem[chip]);

            //emulator stop
            stopMenuItem[chip] = new JMenuItem("Stop and reset " + Constants.CHIP_LABEL[chip] + " emulator");
            //if (chip == Constants.CHIP_FR) stopMenuItem[chip].setMnemonic(KeyEvent.VK_R);
            stopMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, CHIP_MODIFIER[chip]));
            stopMenuItem[chip].setActionCommand(COMMAND_EMULATOR_STOP[chip]);
            stopMenuItem[chip].addActionListener(this);
            runMenu.add(stopMenuItem[chip]);

            runMenu.add(new JSeparator());

            //setup breakpoints
            breakpointMenuItem[chip] = new JMenuItem("Setup " + Constants.CHIP_LABEL[chip] + " breakpoints");
            //if (chip == Constants.CHIP_FR) breakpointMenuItem[chip].setMnemonic(KeyEvent.VK_B);
            breakpointMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, CHIP_MODIFIER[chip]));
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
            if (chip == Constants.CHIP_FR) cpuStateMenuItem[chip].setMnemonic(KeyEvent.VK_C);
            cpuStateMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, CHIP_MODIFIER[chip]));
            cpuStateMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CPUSTATE_WINDOW[chip]);
            cpuStateMenuItem[chip].addActionListener(this);
            componentsMenu.add(cpuStateMenuItem[chip]);

            //memory hex editor
            memoryHexEditorMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " Memory hex editor");
            if (chip == Constants.CHIP_FR) memoryHexEditorMenuItem[chip].setMnemonic(KeyEvent.VK_H);
            memoryHexEditorMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, CHIP_MODIFIER[chip]));
            memoryHexEditorMenuItem[chip].setActionCommand(COMMAND_TOGGLE_MEMORY_HEX_EDITOR[chip]);
            memoryHexEditorMenuItem[chip].addActionListener(this);
            componentsMenu.add(memoryHexEditorMenuItem[chip]);

            //Interrupt controller
            interruptControllerMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " interrupt controller");
            if (chip == Constants.CHIP_FR) interruptControllerMenuItem[chip].setMnemonic(KeyEvent.VK_I);
            interruptControllerMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, CHIP_MODIFIER[chip]));
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

        //screen emulator
        screenEmulatorMenuItem = new JCheckBoxMenuItem("Screen emulator (FR only)");
        screenEmulatorMenuItem.setMnemonic(KeyEvent.VK_S);
        screenEmulatorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        screenEmulatorMenuItem.setActionCommand(COMMAND_TOGGLE_SCREEN_EMULATOR);
        screenEmulatorMenuItem.addActionListener(this);
        componentsMenu.add(screenEmulatorMenuItem);

        //Component 4006
        component4006MenuItem = new JCheckBoxMenuItem("Component 4006 window (FR only)");
        component4006MenuItem.setMnemonic(KeyEvent.VK_4);
        component4006MenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
        component4006MenuItem.setActionCommand(COMMAND_TOGGLE_COMPONENT_4006_WINDOW);
        component4006MenuItem.addActionListener(this);
        componentsMenu.add(component4006MenuItem);

        componentsMenu.add(new JSeparator());

        //Serial devices : TX only for now
        serialDevicesMenuItem[Constants.CHIP_TX] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[Constants.CHIP_TX] + " serial devices (TX only)");
        serialDevicesMenuItem[Constants.CHIP_TX].setActionCommand(COMMAND_TOGGLE_SERIAL_DEVICES[Constants.CHIP_TX]);
        serialDevicesMenuItem[Constants.CHIP_TX].addActionListener(this);
        componentsMenu.add(serialDevicesMenuItem[Constants.CHIP_TX]);

        //A/D converter : TX only for now
        adConverterMenuItem[Constants.CHIP_TX] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[Constants.CHIP_TX] + " A/D converter (TX only)");
        adConverterMenuItem[Constants.CHIP_TX].setActionCommand(COMMAND_TOGGLE_AD_CONVERTER[Constants.CHIP_TX]);
        adConverterMenuItem[Constants.CHIP_TX].addActionListener(this);
        componentsMenu.add(adConverterMenuItem[Constants.CHIP_TX]);

        //Set up the trace menu.
        JMenu traceMenu = new JMenu("Trace");
        traceMenu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(traceMenu);

        for (int chip = 0; chip < 2; chip++) {
            //memory activity viewer
            memoryActivityViewerMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " Memory activity viewer");
            if (chip == Constants.CHIP_FR) memoryActivityViewerMenuItem[chip].setMnemonic(KeyEvent.VK_M);
            memoryActivityViewerMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, CHIP_MODIFIER[chip]));
            memoryActivityViewerMenuItem[chip].setActionCommand(COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER[chip]);
            memoryActivityViewerMenuItem[chip].addActionListener(this);
            traceMenu.add(memoryActivityViewerMenuItem[chip]);

            //disassembly
            disassemblyMenuItem[chip] = new JCheckBoxMenuItem("Real-time " + Constants.CHIP_LABEL[chip] + " disassembly log");
            if (chip == Constants.CHIP_FR) disassemblyMenuItem[chip].setMnemonic(KeyEvent.VK_D);
            disassemblyMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, CHIP_MODIFIER[chip]));
            disassemblyMenuItem[chip].setActionCommand(COMMAND_TOGGLE_DISASSEMBLY_WINDOW[chip]);
            disassemblyMenuItem[chip].addActionListener(this);
            traceMenu.add(disassemblyMenuItem[chip]);

            //Custom logger
            customMemoryRangeLoggerMenuItem[chip] = new JCheckBoxMenuItem("Custom " + Constants.CHIP_LABEL[chip] + " logger window");
//        if (chip == CHIP_FR) customMemoryRangeLoggerMenuItem[chip].setMnemonic(KeyEvent.VK_4);
//        customMemoryRangeLoggerMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, CHIP_MODIFIER[chip]));
            customMemoryRangeLoggerMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW[chip]);
            customMemoryRangeLoggerMenuItem[chip].addActionListener(this);
            traceMenu.add(customMemoryRangeLoggerMenuItem[chip]);

            //Call Stack
            callStackMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " Call stack");
//        if (chip == CHIP_FR) callStackMenuItem[chip].setMnemonic(KeyEvent.VK_C);
//        callStackMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, CHIP_MODIFIER[chip]));
            callStackMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CALL_STACK_WINDOW[chip]);
            callStackMenuItem[chip].addActionListener(this);
            traceMenu.add(callStackMenuItem[chip]);

            //µITRON Object
            iTronObjectMenuItem[chip] = new JCheckBoxMenuItem("µITRON " + Constants.CHIP_LABEL[chip] + " Objects");
//        if (chip == CHIP_FR) iTronObjectMenuItem[chip].setMnemonic(KeyEvent.VK_C);
//        iTronObjectMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, CHIP_MODIFIER[chip]));
            iTronObjectMenuItem[chip].setActionCommand(COMMAND_TOGGLE_ITRON_OBJECT_WINDOW[chip]);
            iTronObjectMenuItem[chip].addActionListener(this);
            traceMenu.add(iTronObjectMenuItem[chip]);

            traceMenu.add(new JSeparator());
        }

        //Set up the tools menu.
        JMenu sourceMenu = new JMenu("Source");
        sourceMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(sourceMenu);

        // FR syscall symbols
        generateSysSymbolsMenuItem = new JMenuItem("Generate " + Constants.CHIP_LABEL[Constants.CHIP_FR] + " system call symbols");
        //        if (chip == CHIP_FR) generateSysSymbolsMenuItem.setMnemonic(KeyEvent.VK_A);
        //        generateSysSymbolsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, CHIP_MODIFIER[chip]));
        generateSysSymbolsMenuItem.setActionCommand(COMMAND_GENERATE_SYS_SYMBOLS);
        generateSysSymbolsMenuItem.addActionListener(this);
        sourceMenu.add(generateSysSymbolsMenuItem);

        for (int chip = 0; chip < 2; chip++) {

            sourceMenu.add(new JSeparator());

            //analyse / disassemble
            analyseMenuItem[chip] = new JMenuItem("Analyse / Disassemble " + Constants.CHIP_LABEL[chip] + " code");
            if (chip == Constants.CHIP_FR) analyseMenuItem[chip].setMnemonic(KeyEvent.VK_A);
            analyseMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, CHIP_MODIFIER[chip]));
            analyseMenuItem[chip].setActionCommand(COMMAND_ANALYSE_DISASSEMBLE[chip]);
            analyseMenuItem[chip].addActionListener(this);
            sourceMenu.add(analyseMenuItem[chip]);

            sourceMenu.add(new JSeparator());

            //code structure
            codeStructureMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " code structure");
            if (chip == Constants.CHIP_FR) codeStructureMenuItem[chip].setMnemonic(KeyEvent.VK_C);
            //        codeStructureMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, CHIP_MODIFIER[chip]));
            codeStructureMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW[chip]);
            codeStructureMenuItem[chip].addActionListener(this);
            sourceMenu.add(codeStructureMenuItem[chip]);

            //source code
            sourceCodeMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " source code");
            if (chip == Constants.CHIP_FR) sourceCodeMenuItem[chip].setMnemonic(KeyEvent.VK_S);
            //        sourceCodeMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, CHIP_MODIFIER[chip]));
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
            if (chip == Constants.CHIP_FR) saveLoadMemoryMenuItem[chip].setMnemonic(KeyEvent.VK_S);
            //        saveLoadMemoryMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, CHIP_MODIFIER[chip]));
            saveLoadMemoryMenuItem[chip].setActionCommand(COMMAND_SAVE_LOAD_MEMORY[chip]);
            saveLoadMemoryMenuItem[chip].addActionListener(this);
            toolsMenu.add(saveLoadMemoryMenuItem[chip]);

            //chip options
            chipOptionsMenuItem[chip] = new JMenuItem(Constants.CHIP_LABEL[chip] + " options");
            if (chip == Constants.CHIP_FR) chipOptionsMenuItem[chip].setMnemonic(KeyEvent.VK_O);
            chipOptionsMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, CHIP_MODIFIER[chip]));
            chipOptionsMenuItem[chip].setActionCommand(COMMAND_CHIP_OPTIONS[chip]);
            chipOptionsMenuItem[chip].addActionListener(this);
            toolsMenu.add(chipOptionsMenuItem[chip]);

            toolsMenu.add(new JSeparator());

        }

        //disassembly options
        uiOptionsMenuItem = new JMenuItem("UI Options");
//        uiOptionsMenuItem.setMnemonic(KeyEvent.VK_O);
//        uiOptionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
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
        masterClock.setSyncPlay(prefs.isSyncPlay());
        syncEmulators.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.setSyncPlay(syncEmulators.isSelected());
                masterClock.setSyncPlay(syncEmulators.isSelected());
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
            startEmulator(chip, RunMode.RUN, null);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_DEBUG)) != Constants.CHIP_NONE) {
            startEmulator(chip, RunMode.DEBUG, null);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_PAUSE)) != Constants.CHIP_NONE) {
            pauseEmulator(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_STEP)) != Constants.CHIP_NONE) {
            startEmulator(chip, RunMode.STEP, null);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_STOP)) != Constants.CHIP_NONE) {
            if (isEmulatorPlaying(chip)) {
                pauseEmulator(chip);
            }
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to reset the " + Constants.CHIP_LABEL[chip] + " emulator and lose the current state ?", "Reset ?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                stopEmulator(chip);
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
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_LOAD_STATE)) != Constants.CHIP_NONE) {
            loadState(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_SAVE_STATE)) != Constants.CHIP_NONE) {
            saveState(chip);
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
        else if (COMMAND_TOGGLE_SCREEN_EMULATOR.equals(e.getActionCommand())) {
            toggleScreenEmulator();
        }
        else if (COMMAND_TOGGLE_COMPONENT_4006_WINDOW.equals(e.getActionCommand())) {
            toggleComponent4006();
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
        new SaveLoadMemoryDialog(this, platform[chip].getMemory()).setVisible(true);
    }


    private void openDecodeDialog() {
        JTextField sourceFile = new JTextField();
        JTextField destinationDir = new JTextField();
        FileSelectionPanel sourceFileSelectionPanel = new FileSelectionPanel("Source file", sourceFile, false);
        sourceFileSelectionPanel.setFileFilter("*.bin", "Firmware file (*.bin)");
        final JComponent[] inputs = new JComponent[]{
                sourceFileSelectionPanel,
                new FileSelectionPanel("Destination dir", destinationDir, true)
        };
        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                inputs,
                "Choose decoding source and destination",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION)) {
            try {
                new FirmwareDecoder().decode(sourceFile.getText(), destinationDir.getText(), false);
                JOptionPane.showMessageDialog(this, "Decoding complete", "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (FirmwareFormatException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error decoding files", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openEncodeDialog() {
        JTextField destinationFile = new JTextField();
        JTextField sourceFile1 = new JTextField();
        JTextField sourceFile2 = new JTextField();
        FileSelectionPanel destinationFileSelectionPanel = new FileSelectionPanel("Destination file", destinationFile, false);
        destinationFileSelectionPanel.setFileFilter("*.bin", "Encoded firmware file (*.bin)");
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
                "Choose encoding destination and source files",
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
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error encoding files", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadState(int chip) {
        JTextField sourceFile = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new FileSelectionPanel("Source file", sourceFile, false),
        };
        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                inputs,
                "Choose state file",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION)) {
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(sourceFile.getText()));
                ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));

                // Read CPU State
                ZipEntry entry = zipInputStream.getNextEntry();
                if (entry == null || !CPUSTATE_ENTRY_NAME[chip].equals(entry.getName())) {
                    JOptionPane.showMessageDialog(this, "Error loading state file\nFirst file not called " + CPUSTATE_ENTRY_NAME[chip], "Error", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    platform[chip].setCpuState((CPUState) XStreamUtils.load(zipInputStream));

                    // Read memory
                    entry = zipInputStream.getNextEntry();
                    if (entry == null || !MEMORY_ENTRY_NAME[chip].equals(entry.getName())) {
                        JOptionPane.showMessageDialog(this, "Error loading state file\nSecond file not called " + MEMORY_ENTRY_NAME[chip], "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        platform[chip].getMemory().loadAllFromStream(zipInputStream);
                        JOptionPane.showMessageDialog(this, "State loading complete", "Done", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                zipInputStream.close();
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading state file\n" + e.getClass().getName()+ "\nSee console for more info", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveState(int chip) {
        JTextField destinationFile = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new FileSelectionPanel("Destination file", destinationFile, false),
        };
        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                inputs,
                "Choose destination file",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION)) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(destinationFile.getText()));
                ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

                StringWriter writer = new StringWriter();
                XStream xStream = new XStream(new StaxDriver());
                xStream.toXML(platform[chip].getCpuState(), writer);
                byte[] bytes = writer.toString().getBytes("UTF-8");

                ZipEntry zipEntry = new ZipEntry(CPUSTATE_ENTRY_NAME[chip]);
                zipEntry.setSize(bytes.length);
                zipOutputStream.putNextEntry(zipEntry);
                IOUtils.write(bytes, zipOutputStream);

                DebuggableMemory memory = (DebuggableMemory) platform[chip].getMemory();
                zipEntry = new ZipEntry(MEMORY_ENTRY_NAME[chip]);
                zipEntry.setSize(memory.getNumPages() + memory.getNumUsedPages() * memory.getPageSize());
                zipOutputStream.putNextEntry(zipEntry);
                memory.saveAllToStream(zipOutputStream);

                zipOutputStream.close();
                fileOutputStream.close();

                JOptionPane.showMessageDialog(this, "State saving complete", "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving state file\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openGenerateSysSymbolsDialog() {
        GenerateSysSymbolsDialog generateSysSymbolsDialog = new GenerateSysSymbolsDialog(this, platform[Constants.CHIP_FR].getMemory());
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
                AnalyseProgressDialog analyseProgressDialog = new AnalyseProgressDialog(this, platform[chip].getMemory());
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
            imageFile[chip] = fc.getSelectedFile();
            // Scratch any analysis that was previously done
            codeStructure[chip] = null;
            loadImage(chip);
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

        // Setup panel
        options.add(new JLabel("Button size :"));
        options.add(small);
        options.add(medium);
        options.add(large);
        options.add(closeAllWindowsOnStopCheckBox);

        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                options,
                "UI Options",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION))
        {
            // save
            prefs.setButtonSize(group.getSelection().getActionCommand());
            prefs.setCloseAllWindowsOnStop(closeAllWindowsOnStopCheckBox.isSelected());
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
                    int baseAddress = platform[chip].getCpuState().getResetAddress();
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
                        disassembler.processOptions(chip, new String[]{
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
                        disassembler.processOptions(chip, new String[]{
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
        Object[] altDebugMode = EnumSet.allOf(RunMode.class).toArray();
        final JComboBox altModeForDebugCombo = new JComboBox(new DefaultComboBoxModel(altDebugMode));
        for (int j = 0; j < altDebugMode.length; j++) {
            if (altDebugMode[j].equals(prefs.getAltModeForSyncedCpuUponDebug(chip))) {
                altModeForDebugCombo.setSelectedIndex(j);
            }
        }
        altDebugPanel.add(new JLabel(Constants.CHIP_LABEL[1 - chip] + " mode when " + Constants.CHIP_LABEL[chip] + " runs in sync Debug: "));
        altDebugPanel.add(altModeForDebugCombo);
        emulationOptionsPanel.add(altDebugPanel);
        emulationOptionsPanel.add(new JLabel("If 'sync mode' is selected, this is the mode the " + Constants.CHIP_LABEL[1 - chip] + " chip will run in when running the " + Constants.CHIP_LABEL[chip] + " in Debug mode"));

        // Alt mode upon Step
        JPanel altStepPanel = new JPanel(new FlowLayout());
        Object[] altStepMode = EnumSet.allOf(RunMode.class).toArray();
        final JComboBox altModeForStepCombo = new JComboBox(new DefaultComboBoxModel(altStepMode));
        for (int j = 0; j < altStepMode.length; j++) {
            if (altStepMode[j].equals(prefs.getAltModeForSyncedCpuUponStep(chip))) {
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
            JPanel eepromOptionsPanel = new JPanel(new VerticalLayout(5, VerticalLayout.LEFT));
            eepromOptionsPanel.add(new JLabel("Eeprom initialization mode:"));

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

            eepromOptionsPanel.add(blank);
            eepromOptionsPanel.add(persistent);
            eepromOptionsPanel.add(lastLoaded);

            tabbedPane.addTab("Eeprom Options", null, eepromOptionsPanel);
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
            prefs.setAltModeForSyncedCpuUponDebug(chip, (RunMode) altModeForDebugCombo.getSelectedItem());
            prefs.setAltModeForSyncedCpuUponStep(chip, (RunMode) altModeForStepCombo.getSelectedItem());
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
                + "<font size=\"+1\">" + ApplicationInfo.getName() + " v" + ApplicationInfo.getVersion() + "</font><br/>"
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
        emulator[chip].exitSleepLoop();
        switch (value) {
            case 0:
                emulator[chip].setSleepIntervalMs(0);
                break;
            case 1:
                emulator[chip].setSleepIntervalMs(1);
                break;
            case 2:
                emulator[chip].setSleepIntervalMs(10);
                break;
            case 3:
                emulator[chip].setSleepIntervalMs(100);
                break;
            case 4:
                emulator[chip].setSleepIntervalMs(1000);
                break;
            case 5:
                emulator[chip].setSleepIntervalMs(10000);
                break;
        }
    }


    private void loadImage(final int chip) {
        //System.err.println("Loading image for " + Constants.CHIP_LABEL[chip]);
        try {

            // 1. CLEANUP

            // Stop timers if active from a previous session (reset)
            if (platform[chip] != null && platform[chip].getProgrammableTimers()[0].isActive()) {
                for (ProgrammableTimer timer : platform[chip].getProgrammableTimers()) {
                    timer.setActive(false);
                }
                // Stop button animation
                setProgrammableTimerAnimationEnabled(chip, true);
            }

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

            // Remove old emulator from the list of clockable devices
            masterClock.remove(emulator[chip]);


            // 2. CREATE NEW

            // TODO We should not create a new platform, just reset it
            // TODO Otherwise, the cross-linkings risks memory leaks
            platform[chip] = new Platform(masterClock);

            // Create a brand new emulator
            emulator[chip] = (chip == Constants.CHIP_FR)?(new FrEmulator(platform[chip])):(new TxEmulator(platform[chip]));

            // Prepare all devices
            CPUState cpuState;
            DebuggableMemory memory = new DebuggableMemory(prefs.isLogMemoryMessages(chip));
            ProgrammableTimer[] programmableTimers;
            IoPort[] ioPorts;
            SerialInterface[] serialInterfaces;
            List<SerialDevice> serialDevices = new ArrayList<SerialDevice>();
            ClockGenerator clockGenerator;
            InterruptController interruptController;
            DmaController dmaController = null;
            RealtimeClock realtimeClock = null;
            KeyCircuit keyCircuit = null;
            AdConverter adConverter = null;

            if (chip == Constants.CHIP_FR) {
                cpuState = new FrCPUState();

                // Initializing only the platform's cpuState here is ugly, but is required
                // so that timers can hook to the cpu passed via the platform (at least on TX)...
                platform[chip].setCpuState(cpuState);

                programmableTimers = new ProgrammableTimer[ExpeedIoListener.NUM_TIMER + ExpeedIoListener.NUM_FREERUN_TIMER];
                serialInterfaces = new SerialInterface[ExpeedIoListener.NUM_SERIAL_IF];
                clockGenerator = new FrClockGenerator();
                interruptController = new FrInterruptController(platform[chip]);

                // Standard FR registers
                memory.addActivityListener(new ExpeedIoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
                // Unknown component 4006
                memory.addActivityListener(new Expeed4006IoListener(prefs.isLogRegisterMessages(chip)));
                // Specific Pin I/O register
                memory.addActivityListener(new ExpeedPinIoListener(platform[chip], prefs.isLogRegisterMessages(chip)));

                // Programmable timers
                for (int i = 0; i < ExpeedIoListener.NUM_TIMER; i++) {
                    programmableTimers[i] = new FrReloadTimer(i, platform[chip]);
                }
                for (int i = 0; i < ExpeedIoListener.NUM_FREERUN_TIMER; i++) {
                    programmableTimers[ExpeedIoListener.NUM_TIMER + i] = new FrFreeRunTimer(i, platform[chip]);
                }

                // I/O ports
                ioPorts = FrIoPort.setupPorts(interruptController, prefs.isLogPinMessages(chip));

                // Serial interfaces
                for (int i = 0; i < serialInterfaces.length; i++) {
                    serialInterfaces[i] = new FrSerialInterface(i, platform[chip], prefs.isLogSerialMessages(chip));
                }
            }
            else {
                cpuState = new TxCPUState();

                // Initializing only the platform's cpuState here is ugly, but is required
                // so that timers can hook to the cpu passed via the platform...
                platform[chip].setCpuState(cpuState);

                programmableTimers = new ProgrammableTimer[TxIoListener.NUM_16B_TIMER + TxIoListener.NUM_32B_TIMER];
                serialInterfaces = new SerialInterface[TxIoListener.NUM_SERIAL_IF + TxIoListener.NUM_HSERIAL_IF];
                clockGenerator = new TxClockGenerator();
                interruptController = new TxInterruptController(platform[chip]);

                memory.addActivityListener(new TxIoListener(platform[chip], prefs.isLogRegisterMessages(chip)));

                // Programmable timers
                // First put all 16-bit timers
                for (int i = 0; i < TxIoListener.NUM_16B_TIMER; i++) {
                    programmableTimers[i] = new TxTimer(i, platform[chip]);
                }
                // Then add the 32-bit input capture timer
                programmableTimers[TxIoListener.NUM_16B_TIMER] = new TxInputCaptureTimer(platform[chip]);

                // I/O ports
                ioPorts = TxIoPort.setupPorts(platform[chip], interruptController, programmableTimers, prefs.isLogPinMessages(chip));

                // Serial interfaces
                // Standard
                for (int i = 0; i < TxIoListener.NUM_SERIAL_IF; i++) {
                    serialInterfaces[i] = new TxSerialInterface(i, platform[chip], prefs.isLogSerialMessages(chip));
                }
                // Hi-speed
                for (int i = 0; i < TxIoListener.NUM_HSERIAL_IF; i++) {
                    serialInterfaces[TxIoListener.NUM_SERIAL_IF + i] = new TxHSerialInterface(i, platform[chip], prefs.isLogSerialMessages(chip));
                }

                ((TxCPUState) cpuState).setInterruptController((TxInterruptController) interruptController);

                dmaController = new TxDmaController(platform[chip], prefs);
                realtimeClock = new TxRealtimeClock(platform[chip], prefs);
                keyCircuit = new TxKeyCircuit(interruptController);

                // Devices to be linked to the Tx chip

                // Eeprom
                eeprom = new St95040("Eeprom");
                switch(prefs.getEepromInitMode()) {
                    case BLANK:
                        eeprom.clear();
                        break;
                    case PERSISTENT:
                        byte[] lastEepromContents = prefs.getLastEepromContents();
                        if (lastEepromContents != null) {
                            eeprom.loadArray(lastEepromContents);
                        }
                        else {
                            System.err.println("Attempt at loading previous eeprom values failed. No stored values...");
                            eeprom.clear();
                        }
                        break;
                    case LAST_LOADED:
                        String lastEepromFileName = prefs.getLastEepromFileName();
                        if (StringUtils.isNotBlank(lastEepromFileName)) {
                            try {
                                eeprom.loadBinary(new File(lastEepromFileName));
                            } catch (IOException e) {
                                System.err.println("Error reloading last eeprom contents from file '" + lastEepromFileName + "': " + e.getMessage());
                                eeprom.clear();
                            }
                        }
                        else {
                            System.err.println("Attempt at reloading last eeprom contents from file failed. Seems no eeprom was ever loaded...");
                            eeprom.clear();
                        }
                        break;
                }

                LcdDriver lcdDriver = new LcdDriver("ViewFinder LCD");
                serialDevices.add(eeprom);
                serialDevices.add(lcdDriver);

                connectTxSerialDevices(serialInterfaces, ioPorts, serialDevices);

                AdValueProvider provider = new TxAdPrefsValueProvider(prefs, Constants.CHIP_TX);
                adConverter = new TxAdConverter(emulator[Constants.CHIP_TX], (TxInterruptController) interruptController, provider);
            }

            // Set up input port overrides according to prefs
            for (int portNumber = 0; portNumber < ioPorts.length; portNumber++) {
                for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
                    Integer override = prefs.getPortInputValueOverride(chip, portNumber, bitNumber);
                    if (override != null) {
                        Pin pin = ioPorts[portNumber].getPin(bitNumber);
                        // Insert fixed source, if requested
                        switch (override) {
                            case 0:
                                // GND
                                new FixedSourceComponent(0, "Fixed " + Constants.LABEL_LO + " for " + pin.getName(), prefs.isLogPinMessages(chip)).insertAtPin(pin);
                                break;
                            case 1:
                                // VCC
                                new FixedSourceComponent(1, "Fixed " + Constants.LABEL_HI + " for " + pin.getName(), prefs.isLogPinMessages(chip)).insertAtPin(pin);
                                break;
                            default:
                                new FixedSourceComponent(override, "Fixed " + override + " for " + pin.getName(), prefs.isLogPinMessages(chip)).insertAtPin(pin);
                                break;
                        }
                    }
                }
            }


            platform[chip].setMemory(memory);
            platform[chip].setClockGenerator(clockGenerator);
            platform[chip].setInterruptController(interruptController);
            platform[chip].setProgrammableTimers(programmableTimers);
            platform[chip].setIoPorts(ioPorts);
            platform[chip].setSerialInterfaces(serialInterfaces);
            platform[chip].setDmaController(dmaController);
            platform[chip].setRealtimeClock(realtimeClock);
            platform[chip].setKeyCircuit(keyCircuit);
            platform[chip].setAdConverter(adConverter);
            platform[chip].setSerialDevices(serialDevices);

            clockGenerator.setPlatform(platform[chip]);

            // TODO is it the right way to create a context here ?
            // TODO passing cpu, memory and interrupt controller a second time although they're in the platform
            // TODO sounds weird...
            emulator[chip].setContext(memory, cpuState, interruptController);
            emulator[chip].clearCycleCounterListeners();

            setEmulatorSleepCode(chip, prefs.getSleepTick(chip));

            // TODO: let user choose whether he wants to load at reset address or refer to a dfr/dtx file's "-i" option
            // That would allow to load "relocatable" areas at their right place.
            // e.g. TX code @0xBFC0A000-0xBFC0ED69 is copied to RAM 0xFFFF4000-0xFFFF8D69 by code 0xBFC1C742-0xBFC1C76A
            memory.loadFile(imageFile[chip], cpuState.getResetAddress(), prefs.isFirmwareWriteProtected(chip));
            isImageLoaded[chip] = true;

            cpuState.reset();

            if (isImageLoaded[Constants.CHIP_FR] && isImageLoaded[Constants.CHIP_TX]) {
                // Two CPUs are ready.
                // Perform serial interconnection
                interconnectChipSerialPorts(platform[Constants.CHIP_FR].getSerialInterfaces(), platform[Constants.CHIP_TX].getSerialInterfaces());
                // Perform serial interconnection
                interconnectChipIoPorts(platform[Constants.CHIP_FR].getIoPorts(), platform[Constants.CHIP_TX].getIoPorts());
            }

            // 3. RESTORE

            if (prefs.isAutoEnableTimers(chip)) {
                toggleProgrammableTimers(chip);
            }

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

            // Finally, add the emulator to the list of clockable devices, in disabled state
            masterClock.add(emulator[chip], new ClockableCallbackHandler() {
                @Override
                public void onNormalExit(Object o) {
                    try {
                        isEmulatorPlaying[chip] = false;
                        emulator[chip].clearBreakConditions();
                        signalEmulatorStopped(chip);
                        if (o instanceof BreakCondition) {
                            if (((BreakCondition)o).getBreakTrigger() != null) {
                                setStatusText(chip, "Break trigger matched : " + ((BreakCondition) o).getBreakTrigger().getName());
                                statusBar[chip].setBackground(STATUS_BGCOLOR_BREAK);
                            }
                            else {
                                setStatusText(chip, "Emulation complete");
                                statusBar[chip].setBackground(STATUS_BGCOLOR_DEFAULT);
                            }
                        }
                        else {
                            setStatusText(chip, "Emulation complete" + ((o==null)?"":(": " + o.toString())));
                            statusBar[chip].setBackground(STATUS_BGCOLOR_DEFAULT);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateState(chip);
                }

                @Override
                public void onException(Exception e) {
                    isEmulatorPlaying[chip] = false;
                    emulator[chip].clearBreakConditions();
                    signalEmulatorStopped(chip);
                    e.printStackTrace();
                    String message = e.getMessage();
                    if (StringUtils.isEmpty(message)) {
                        message = e.getClass().getName();
                    }
                    JOptionPane.showMessageDialog(EmulatorUI.this, message + "\nSee console for more info", Constants.CHIP_LABEL[chip] + " Emulator error", JOptionPane.ERROR_MESSAGE);
                }
            }, false);

            updateState(chip);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Connect Tx serial interface HSC2 with the flash eeprom and the lcd driver via a SPI bus
     *
     * @param txSerialInterfaces
     * @param txIoPorts
     * @param txSerialDevices
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    private void connectTxSerialDevices(SerialInterface[] txSerialInterfaces, IoPort[] txIoPorts, List<SerialDevice> txSerialDevices) {
        // get components
        SerialInterface txSerialInterfaceH2 = txSerialInterfaces[TxIoListener.NUM_SERIAL_IF + 2]; // Master
        final St950x0 eeprom = (St950x0) txSerialDevices.get(0); // Slave 1
        final LcdDriver lcdDriver = (LcdDriver) txSerialDevices.get(1); // Slave 2

        // Create a bus with the CPU as master
        SpiBus bus = new SpiBus("bus", txSerialInterfaceH2) ;

        // Connect slaves
        bus.addSlaveDevice(eeprom);
        bus.addSlaveDevice(lcdDriver);
        bus.connect();

        // Connect CPU pins with eeprom and lcd driver ~SELECT pins
        Pin.interconnect(txIoPorts[IoPort.PORT_4].getPin(6), eeprom.getSelectPin());
        Pin.interconnect(txIoPorts[IoPort.PORT_E].getPin(6), lcdDriver.getSelectPin());
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    private void interconnectChipSerialPorts(SerialInterface[] frSerialInterfaces, SerialInterface[] txSerialInterfaces) {
        // Reconnect Fr Serial channel 5 with Tx serial interface HSC0
        SerialInterface frSerialInterface5 = frSerialInterfaces[5];
        SerialInterface txSerialInterfaceH0 = txSerialInterfaces[TxIoListener.NUM_SERIAL_IF + 0];
        frSerialInterface5.connectTargetDevice(txSerialInterfaceH0);
        txSerialInterfaceH0.connectTargetDevice(frSerialInterface5);
    }

    private void interconnectChipIoPorts(IoPort[] frIoPorts, IoPort[] txIoPorts) {
        // FR 0x50000100.bit5 => TX P53 (INTF), triggered (low) by FR at 001A8CBE and 001A8E24 and set back hi at 001A8E58
        Pin.interconnect(frIoPorts[IoPort.PORT_0].getPin(5), txIoPorts[IoPort.PORT_5].getPin(3));

        // TX PC3 => FR 0x50000107.bit6 (INT16) , tested by FR at 001A885C, 001A8896 (init) and 001A8976 (send header)
        Pin.interconnect(frIoPorts[IoPort.PORT_7].getPin(6), txIoPorts[IoPort.PORT_C].getPin(3));

        // Pin.interconnect(main power button, txIoPorts[IoPort.PORT_A].getPin(0));
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
        if (ITronObjectFrame[chip] != null) {
            ITronObjectFrame[chip].dispose();
            ITronObjectFrame[chip] = null;
            if (mustReOpen) toggleITronObject(chip);
        }
    }

    private void toggleBreakTriggerList(int chip) {
        if (breakTriggerListFrame[chip] == null) {
            breakTriggerListFrame[chip] = new BreakTriggerListFrame("Setup breakpoints and triggers", "breakpoint", true, true, true, true, chip, this, emulator[chip], prefs.getTriggers(chip), platform[chip].getMemory());
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
            disassemblyLogFrame[chip] = new DisassemblyFrame("Real-time disassembly log", "disassembly_log", true, true, true, true, chip, this, emulator[chip]);
            if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setInstructionPrintWriter(disassemblyLogFrame[chip].getInstructionPrintWriter());
            addDocumentFrame(chip, disassemblyLogFrame[chip]);
            disassemblyLogFrame[chip].display(true);
        }
        else {
            if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setInstructionPrintWriter(null);
            disassemblyLogFrame[chip].dispose();
            disassemblyLogFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleCPUState(int chip) {
        if (cpuStateEditorFrame[chip] == null) {
            cpuStateEditorFrame[chip] = new CPUStateEditorFrame("CPU State", "cpu", true, true, false, true, chip, this, platform[chip].getCpuState());
            cpuStateEditorFrame[chip].setEnabled(!isEmulatorPlaying[chip]);
            if (disassemblyLogFrame[chip] != null) cpuStateEditorFrame[chip].setInstructionPrintWriter(disassemblyLogFrame[chip].getInstructionPrintWriter());
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
            memoryActivityViewerFrame[chip] = new MemoryActivityViewerFrame("Base memory activity viewer (each cell=64k, click to zoom)", "memory_activity", true, true, true, true, chip, this, (DebuggableMemory) platform[chip].getMemory());
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
            memoryHexEditorFrame[chip] = new MemoryHexEditorFrame("Memory hex editor", "memory_editor", true, true, true, true, chip, this, (DebuggableMemory) platform[chip].getMemory(), platform[chip].getCpuState(), 0, !isEmulatorPlaying[chip]);
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
            screenEmulatorFrame = new ScreenEmulatorFrame("Screen emulator", "screen", true, true, true, true, Constants.CHIP_FR, this, (DebuggableMemory) platform[Constants.CHIP_FR].getMemory(), CAMERA_SCREEN_MEMORY_Y, CAMERA_SCREEN_MEMORY_U, CAMERA_SCREEN_MEMORY_V, CAMERA_SCREEN_WIDTH, CAMERA_SCREEN_HEIGHT);
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
            component4006Frame = new Component4006Frame("Component 4006", "4006", true, true, false, true, Constants.CHIP_FR, this, (DebuggableMemory) platform[Constants.CHIP_FR].getMemory(), 0x4006, platform[Constants.CHIP_FR].getCpuState());
            addDocumentFrame(Constants.CHIP_FR, component4006Frame);
            component4006Frame.display(true);
        }
        else {
            component4006Frame.dispose();
            component4006Frame = null;
        }
        updateState(Constants.CHIP_FR);
    }

    private void toggleCustomMemoryRangeLoggerComponentFrame(int chip) {
        if (customMemoryRangeLoggerFrame[chip] == null) {
            customMemoryRangeLoggerFrame[chip] = new CustomMemoryRangeLoggerFrame("Custom Logger", "custom_logger", true, true, false, true, chip, this, (DebuggableMemory) platform[chip].getMemory(), platform[chip].getCpuState());
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
            programmableTimersFrame[chip] = new ProgrammableTimersFrame("Programmable timers", "timer", true, true, false, true, chip, this, platform[chip].getProgrammableTimers());
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
                    ?new FrInterruptControllerFrame("Interrupt controller", "interrupt", true, true, false, true, chip, this, platform[chip].getInterruptController(), platform[chip].getMemory())
                    :new TxInterruptControllerFrame("Interrupt controller", "interrupt", true, true, false, true, chip, this, platform[chip].getInterruptController(), platform[chip].getMemory());
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
            serialInterfaceFrame[chip] = new SerialInterfaceFrame("Serial interfaces", "serial", true, true, false, true, chip, this, platform[chip].getSerialInterfaces());
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
            genericSerialFrame[chip] = new GenericSerialFrame("Serial devices", "serial_devices", true, true, false, true, chip, this, platform[chip].getSerialDevices());
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
            ioPortsFrame[chip] = new IoPortsFrame("I/O ports", "io", false, true, false, true, chip, this, platform[chip].getIoPorts());
            for (IoPort ioPort : platform[chip].getIoPorts()) {
                ioPort.addIoPortsListener(ioPortsFrame[chip]);
            }
            addDocumentFrame(chip, ioPortsFrame[chip]);
            ioPortsFrame[chip].display(true);
        }
        else {
            for (IoPort ioPort : platform[chip].getIoPorts()) {
                ioPort.removeIoPortsListener(ioPortsFrame[chip]);
            }
            ioPortsFrame[chip].dispose();
            ioPortsFrame[chip] = null;
        }
        updateState(chip);
    }

    private void toggleAdConverterFrame(int chip) {
        if (adConverterFrame[chip] == null) {
            adConverterFrame[chip] = new AdConverterFrame("A/D converter", "ad_converter", true, true, false, true, chip, this, platform[chip].getAdConverter());
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
            callStackFrame[chip] = new CallStackFrame("Call Stack", "call_stack", true, true, false, true, chip, this, emulator[chip], platform[chip].getCpuState(), codeStructure[chip]);
            callStackFrame[chip].setAutoRefresh(isEmulatorPlaying[chip]);
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
            ITronObjectFrame[chip] = new ITronObjectFrame("µITRON Object Status", "os", true, true, false, true, chip, this, platform[chip], codeStructure[chip]);
            ITronObjectFrame[chip].enableUpdate(!isEmulatorPlaying[chip]);
            if (!isEmulatorPlaying[chip]) {
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


    private void toggleCodeStructureWindow(int chip) {
        if (codeStructureFrame[chip] == null) {
            codeStructureFrame[chip] = new CodeStructureFrame("Code structure", "code_structure", true, true, true, true, chip, this, platform[chip].getCpuState(), codeStructure[chip]);
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
            sourceCodeFrame[chip] = new SourceCodeFrame("Source code", "source", true, true, true, true, chip, this, platform[chip].getCpuState(), codeStructure[chip]);
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

            component4006MenuItem.setEnabled(isImageLoaded[Constants.CHIP_FR]);
            component4006Button.setEnabled(isImageLoaded[Constants.CHIP_FR]);
            screenEmulatorMenuItem.setEnabled(isImageLoaded[Constants.CHIP_FR]);
            screenEmulatorButton.setEnabled(isImageLoaded[Constants.CHIP_FR]);

            generateSysSymbolsMenuItem.setEnabled(isImageLoaded[Constants.CHIP_FR]);
        }

        // Menus and buttons enabled or not
        codeStructureMenuItem[chip].setEnabled(codeStructure[chip] != null);
        codeStructureButton[chip].setEnabled(codeStructure[chip] != null);
        sourceCodeMenuItem[chip].setEnabled(codeStructure[chip] != null);
        sourceCodeButton[chip].setEnabled(codeStructure[chip] != null);

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

        analyseMenuItem[chip].setEnabled(isImageLoaded[chip]);
        analyseButton[chip].setEnabled(isImageLoaded[chip]);

        cpuStateMenuItem[chip].setEnabled(isImageLoaded[chip]);
        cpuStateButton[chip].setEnabled(isImageLoaded[chip]);
        disassemblyMenuItem[chip].setEnabled(isImageLoaded[chip]);
        disassemblyButton[chip].setEnabled(isImageLoaded[chip]);
        memoryActivityViewerMenuItem[chip].setEnabled(isImageLoaded[chip]);
        memoryActivityViewerButton[chip].setEnabled(isImageLoaded[chip]);
        memoryHexEditorMenuItem[chip].setEnabled(isImageLoaded[chip]);
        memoryHexEditorButton[chip].setEnabled(isImageLoaded[chip]);
        customMemoryRangeLoggerMenuItem[chip].setEnabled(isImageLoaded[chip]);
        customMemoryRangeLoggerButton[chip].setEnabled(isImageLoaded[chip]);
        programmableTimersMenuItem[chip].setEnabled(isImageLoaded[chip]);
        programmableTimersButton[chip].setEnabled(isImageLoaded[chip]);
        interruptControllerMenuItem[chip].setEnabled(isImageLoaded[chip]);
        interruptControllerButton[chip].setEnabled(isImageLoaded[chip]);
        serialInterfacesMenuItem[chip].setEnabled(isImageLoaded[chip]);
        serialInterfacesButton[chip].setEnabled(isImageLoaded[chip]);
        ioPortsMenuItem[chip].setEnabled(isImageLoaded[chip]);
        ioPortsButton[chip].setEnabled(isImageLoaded[chip]);
        if (chip == Constants.CHIP_TX) {
            serialDevicesMenuItem[chip].setEnabled(isImageLoaded[chip]); serialDevicesButton[chip].setEnabled(isImageLoaded[chip]);
            adConverterMenuItem[chip].setEnabled(isImageLoaded[chip]); adConverterButton[chip].setEnabled(isImageLoaded[chip]);
        }
        callStackMenuItem[chip].setEnabled(isImageLoaded[chip]);
        callStackButton[chip].setEnabled(isImageLoaded[chip]);
        iTronObjectMenuItem[chip].setEnabled(isImageLoaded[chip]);
        iTronObjectButton[chip].setEnabled(isImageLoaded[chip]);

        saveLoadMemoryMenuItem[chip].setEnabled(isImageLoaded[chip]);
        saveLoadMemoryButton[chip].setEnabled(isImageLoaded[chip]);

        stopMenuItem[chip].setEnabled(isImageLoaded[chip]);
        stopButton[chip].setEnabled(isImageLoaded[chip]);

        if (isImageLoaded[chip]) {
            // Depends whether emulator is playing or not
            loadMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); loadButton[chip].setEnabled(!isEmulatorPlaying[chip]);
            playMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); playButton[chip].setEnabled(!isEmulatorPlaying[chip]);
            debugMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); debugButton[chip].setEnabled(!isEmulatorPlaying[chip]);
            pauseMenuItem[chip].setEnabled(isEmulatorPlaying[chip]); pauseButton[chip].setEnabled(isEmulatorPlaying[chip]);
            stepMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); stepButton[chip].setEnabled(!isEmulatorPlaying[chip]);
            chipOptionsMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); chipOptionsButton[chip].setEnabled(!isEmulatorPlaying[chip]);

            // Editable components
            if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setEditable(!isEmulatorPlaying[chip]);
            if (memoryHexEditorFrame[chip] != null) memoryHexEditorFrame[chip].setEditable(!isEmulatorPlaying[chip]);
            if (callStackFrame[chip] != null) callStackFrame[chip].setAutoRefresh(isEmulatorPlaying[chip]);
            if (ITronObjectFrame[chip] != null) ITronObjectFrame[chip].enableUpdate(!isEmulatorPlaying[chip]);
            if (breakTriggerListFrame[chip] != null) breakTriggerListFrame[chip].setEditable(!isEmulatorPlaying[chip]);
            if (sourceCodeFrame[chip] != null) sourceCodeFrame[chip].setEditable(!isEmulatorPlaying[chip]);
            if (interruptControllerFrame[chip] != null) interruptControllerFrame[chip].setEditable(!isEmulatorPlaying[chip]);
        }
        else {
            loadMenuItem[chip].setEnabled(true); loadButton[chip].setEnabled(true);
            playMenuItem[chip].setEnabled(false); playButton[chip].setEnabled(false);
            debugMenuItem[chip].setEnabled(false); debugButton[chip].setEnabled(false);
            pauseMenuItem[chip].setEnabled(false); pauseButton[chip].setEnabled(false);
            stepMenuItem[chip].setEnabled(false); stepButton[chip].setEnabled(false);
            chipOptionsMenuItem[chip].setEnabled(false); chipOptionsButton[chip].setEnabled(false);

            // Editable components  TODO does it make sense ? And why true ?
            if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setEditable(true);
            if (memoryHexEditorFrame[chip] != null) memoryHexEditorFrame[chip].setEditable(true);
            if (callStackFrame[chip] != null) callStackFrame[chip].setAutoRefresh(false);
            if (ITronObjectFrame[chip] != null) ITronObjectFrame[chip].enableUpdate(true);
            if (breakTriggerListFrame[chip] != null) breakTriggerListFrame[chip].setEditable(true);
            if (sourceCodeFrame[chip] != null) sourceCodeFrame[chip].setEditable(true);
            if (interruptControllerFrame[chip] != null) interruptControllerFrame[chip].setEditable(true);
        }
    }


    public boolean isEmulatorReady(int chip) {
        return isImageLoaded[chip] && !isEmulatorPlaying[chip];
    }

    public boolean isEmulatorPlaying(int chip) {
        return isEmulatorPlaying[chip];
    }

    public void setCodeStructure(int chip, CodeStructure codeStructure) {
        this.codeStructure[chip] = codeStructure;
    }

    public void playToAddress(int chip, RunMode runMode, Integer endAddress) {
        prepareBreakTriggers(chip, runMode, endAddress);
        startEmulator(chip, runMode, endAddress);
    }


    /**
     * Prepares breakpoints for the given run mode on the given chip
     * @param chip
     * @param runMode mode to run in
     * @param endAddress if not null, stop when reaching this address
     */
    private void prepareBreakTriggers(int chip, RunMode runMode, Integer endAddress) {
        //System.err.println("Play request for " + Constants.CHIP_LABEL[chip]);
        if (!isImageLoaded[chip]) {
            throw new RuntimeException("No Image loaded !");
        }

        if (runMode == RunMode.STEP) {
            emulator[chip].addBreakCondition(new AlwaysBreakCondition());
        }
        else {
            if (runMode == RunMode.DEBUG) {
                for (BreakTrigger breakTrigger : prefs.getTriggers(chip)) {
                    if (breakTrigger.mustBreak() || breakTrigger.mustBeLogged() || breakTrigger.getInterruptToRequest() != null || breakTrigger.getPcToSet() != null) {
                        // Arm memory change detection triggers
                        for (MemoryValueBreakCondition memoryValueBreakCondition : breakTrigger.getMemoryValueBreakConditions()) {
                            if (memoryValueBreakCondition.isChangeDetection()) {
                                memoryValueBreakCondition.setValue(platform[chip].getMemory().load32(memoryValueBreakCondition.getAddress()));
                                memoryValueBreakCondition.setNegate(true);
                            }
                        }
                        emulator[chip].addBreakCondition(new AndCondition(breakTrigger.getBreakConditions(codeStructure[chip], platform[chip].getMemory()), breakTrigger));
                    }
                }
            }
            if (endAddress != null) {
                // Set a temporary break condition at given endAddress
                CPUState values = (chip==Constants.CHIP_FR)?new FrCPUState(endAddress):new TxCPUState(endAddress);
                CPUState flags = (chip==Constants.CHIP_FR)?new FrCPUState():new TxCPUState();
                flags.pc = 1;
                // TODO adapt this for Tx
                if (chip==Constants.CHIP_FR) {
                    ((FrCPUState)flags).setILM(0, false);
                    flags.setReg(FrCPUState.TBR, 0);
                }
                BreakTrigger breakTrigger = new BreakTrigger("Run to cursor at 0x" + Format.asHex(endAddress, 8), values, flags, new ArrayList<MemoryValueBreakCondition>());
                emulator[chip].addBreakCondition(new BreakPointCondition(endAddress, breakTrigger));
            }
        }
        setStatusText(chip, runMode.label + " session in progress...");
        statusBar[chip].setBackground(runMode==RunMode.DEBUG? STATUS_BGCOLOR_DEBUG : STATUS_BGCOLOR_RUN);
    }

    private void startEmulator(final int chip, RunMode runMode, Integer endAddress) {
        //System.err.println("Start request for " + Constants.CHIP_LABEL[chip]);
        prepareBreakTriggers(chip, runMode, endAddress);
        prepareEmulation(chip);
        if (prefs.isSyncPlay()) {
            // Start the other one too
            int otherChip = 1 - chip;
            setStatusText(otherChip, "Sync play...");
            statusBar[otherChip].setBackground(STATUS_BGCOLOR_RUN);
            RunMode altRunMode;
            switch (runMode) {
                case STEP:
                    altRunMode = prefs.getAltModeForSyncedCpuUponStep(chip);
                    break;
                case DEBUG:
                    altRunMode = prefs.getAltModeForSyncedCpuUponDebug(chip);
                    break;
                default:
                    altRunMode = RunMode.RUN;
            }
            prepareBreakTriggers(otherChip, altRunMode, null);
            prepareEmulation(otherChip);
        }
        //System.err.println("Requesting clock start");
        masterClock.start();
    }

    private void prepareEmulation(final int chip) {
        //System.err.println("Preparing emulation of " + Constants.CHIP_LABEL[chip]);
        isEmulatorPlaying[chip] = true;
        emulator[chip].setOutputOptions(prefs.getOutputOptions(chip));
        masterClock.setEnabled(emulator[chip], true);
        // TODO what's the use of this here ?
        platform[chip].getCpuState().setAllRegistersDefined();
        updateState(chip);
    }

    private void signalEmulatorStopped(int chip) {
        // TODO this should iterate on all windows. A NOP onEmulatorStop() should exist in DocumentFrame
        if (sourceCodeFrame[chip] != null) {
            sourceCodeFrame[chip].onEmulatorStop();
        }
        if (ITronObjectFrame[chip] != null) {
            ITronObjectFrame[chip].onEmulatorStop(chip);
        }
    }


    public void playOneFunction(int chip, int address, boolean debugMode) {
        if (chip == Constants.CHIP_TX) {
            System.err.println("Not implemented for TX");
            // TODO : implement the equivalent for TX
        }
        else {
            // TODO : make the call transparent by cloning CPUState
            // To execute one function only, we put a fake CALL at a conventional place, followed by an infinite loop
            platform[chip].getMemory().store16(BASE_ADDRESS_FUNCTION_CALL[chip], 0x9f8c);      // LD          ,R12
            platform[chip].getMemory().store32(BASE_ADDRESS_FUNCTION_CALL[chip] + 2, address); //     address
            platform[chip].getMemory().store16(BASE_ADDRESS_FUNCTION_CALL[chip] + 6, 0x971c);  // CALL @R12
            platform[chip].getMemory().store16(BASE_ADDRESS_FUNCTION_CALL[chip] + 8, 0xe0ff);  // HALT, infinite loop

            // And we put a breakpoint on the instruction after the call
            emulator[chip].clearBreakConditions();
            emulator[chip].addBreakCondition(new BreakPointCondition(BASE_ADDRESS_FUNCTION_CALL[chip] + 8, null));

            platform[chip].getCpuState().pc = BASE_ADDRESS_FUNCTION_CALL[chip];

            if (debugMode) {
                for (BreakTrigger breakTrigger : prefs.getTriggers(chip)) {
                    if (breakTrigger.mustBreak() || breakTrigger.mustBeLogged()) {
                        emulator[chip].addBreakCondition(new AndCondition(breakTrigger.getBreakConditions(codeStructure[chip], platform[chip].getMemory()), breakTrigger));
                    }
                }
            }

            prepareEmulation(chip);
            masterClock.start();
        }
    }

    private void pauseEmulator(int chip) {
        emulator[chip].addBreakCondition(new AlwaysBreakCondition());
        emulator[chip].exitSleepLoop();
    }

    private void stopEmulator(int chip) {
        prefs.setLastEepromContents(eeprom.getMemory());
        emulator[chip].addBreakCondition(new AlwaysBreakCondition());
        emulator[chip].exitSleepLoop();
        try {
            // Wait for emulator to stop
            Thread.sleep(120);
        } catch (InterruptedException e) {
            // nop
        }
        if (prefs.isSyncPlay()) {
            masterClock.resetTotalElapsedTimePs();
        }
        loadImage(chip);
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
        if (eeprom != null) {
            prefs.setLastEepromContents(eeprom.getMemory());
        }
        Prefs.save(prefs);
        System.exit(0);
    }

    public void jumpToSource(int chip, Function function) {
        if (codeStructure[chip] != null) {
            if (sourceCodeFrame[chip] == null) {
                toggleSourceCodeWindow(chip);
            }
            sourceCodeFrame[chip].writeFunction(function);
        }
    }

    public void jumpToSource(int chip, int address) {
        if (codeStructure[chip] != null) {
            if (sourceCodeFrame[chip] == null) {
                toggleSourceCodeWindow(chip);
            }
            if (!sourceCodeFrame[chip].exploreAddress(address)) {
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

    public void onBreaktriggersChange(int chip) {
        if (sourceCodeFrame[chip] != null) {
            sourceCodeFrame[chip].updateBreakTriggers();
        }
        if (breakTriggerListFrame[chip] != null) {
            breakTriggerListFrame[chip].updateBreaktriggers();
        }
    }

    public void toggleProgrammableTimers(int chip) {
        enableProgrammableTimers(chip, !platform[chip].getProgrammableTimers()[0].isActive());
    }

    private void enableProgrammableTimers(int chip, boolean active) {
        ProgrammableTimer[] timers = platform[chip].getProgrammableTimers();
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
