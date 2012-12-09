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
import com.nikonhacker.emu.EmulationException;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.FrEmulator;
import com.nikonhacker.emu.TxEmulator;
import com.nikonhacker.emu.clock.ClockGenerator;
import com.nikonhacker.emu.clock.FrClockGenerator;
import com.nikonhacker.emu.clock.TxClockGenerator;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;
import com.nikonhacker.emu.memory.listener.fr.ExpeedIoListener;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.interruptController.FrInterruptController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.TxIoPort;
import com.nikonhacker.emu.peripherials.programmableTimer.FrReloadTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.TxTimer;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.*;
import com.nikonhacker.encoding.FirmwareDecoder;
import com.nikonhacker.encoding.FirmwareEncoder;
import com.nikonhacker.encoding.FirmwareFormatException;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.FileSelectionPanel;
import com.nikonhacker.gui.component.ModifiedFlowLayout;
import com.nikonhacker.gui.component.VerticalLayout;
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
import com.nikonhacker.gui.component.memoryActivity.MemoryActivityViewerFrame;
import com.nikonhacker.gui.component.memoryHexEditor.MemoryHexEditorFrame;
import com.nikonhacker.gui.component.memoryMapped.Component4006Frame;
import com.nikonhacker.gui.component.memoryMapped.CustomMemoryRangeLoggerFrame;
import com.nikonhacker.gui.component.realos.RealOsObjectFrame;
import com.nikonhacker.gui.component.saveLoadMemory.SaveLoadMemoryDialog;
import com.nikonhacker.gui.component.screenEmulator.ScreenEmulatorFrame;
import com.nikonhacker.gui.component.serialInterface.SerialInterfaceFrame;
import com.nikonhacker.gui.component.sourceCode.SourceCodeFrame;
import com.nikonhacker.gui.component.timer.ProgrammableTimersFrame;
import com.nikonhacker.realos.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
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

public class EmulatorUI extends JFrame implements ActionListener, ChangeListener {

    // Constants
    private static final String[] COMMAND_IMAGE_LOAD = {"FR_IMAGE_LOAD", "TX_IMAGE_LOAD"};
    private static final String[] COMMAND_GENERATE_SYS_SYMBOLS = {"FR_GENERATE_SYS_SYMBOLS", "TX_GENERATE_SYS_SYMBOLS"};
    private static final String[] COMMAND_ANALYSE_DISASSEMBLE = {"FR_ANALYSE_DISASSEMBLE", "TX_ANALYSE_DISASSEMBLE"};
    private static final String[] COMMAND_EMULATOR_PLAY = {"FR_EMULATOR_PLAY", "TX_EMULATOR_PLAY"};
    private static final String[] COMMAND_EMULATOR_DEBUG = {"FR_EMULATOR_DEBUG", "TX_EMULATOR_DEBUG"};
    private static final String[] COMMAND_EMULATOR_PAUSE = {"FR_EMULATOR_PAUSE", "TX_EMULATOR_PAUSE"};
    private static final String[] COMMAND_EMULATOR_STEP = {"FR_EMULATOR_STEP", "TX_EMULATOR_STEP"};
    private static final String[] COMMAND_EMULATOR_STOP = {"FR_EMULATOR_STOP", "TX_EMULATOR_STOP"};
    private static final String[] COMMAND_SETUP_BREAKPOINTS = {"FR_SETUP_BREAKPOINTS", "TX_SETUP_BREAKPOINTS"};
    private static final String[] COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER = {"FR_TOGGLE_MEMORY_ACTIVITY_VIEWER", "TX_TOGGLE_MEMORY_ACTIVITY_VIEWER"};
    private static final String[] COMMAND_TOGGLE_MEMORY_HEX_EDITOR = {"FR_TOGGLE_MEMORY_HEX_EDITOR", "TX_TOGGLE_MEMORY_HEX_EDITOR"};
    private static final String[] COMMAND_TOGGLE_DISASSEMBLY_WINDOW = {"FR_TOGGLE_DISASSEMBLY_WINDOW", "TX_TOGGLE_DISASSEMBLY_WINDOW"};
    private static final String[] COMMAND_TOGGLE_CPUSTATE_WINDOW = {"FR_TOGGLE_CPUSTATE_WINDOW", "TX_TOGGLE_CPUSTATE_WINDOW"};
    private static final String[] COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW = {"FR_COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW", "TX_COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW"};
    private static final String[] COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW = {"FR_TOGGLE_INTERRUPT_CONTROLLER_WINDOW", "TX_TOGGLE_INTERRUPT_CONTROLLER_WINDOW"};
    private static final String[] COMMAND_TOGGLE_SERIAL_INTERFACES = {"FR_COMMAND_TOGGLE_SERIAL_INTERFACES", "TX_COMMAND_TOGGLE_SERIAL_INTERFACES"};
    private static final String[] COMMAND_LOAD_STATE = {"FR_LOAD_STATE", "TX_LOAD_STATE"};
    private static final String[] COMMAND_SAVE_STATE = {"FR_SAVE_STATE", "TX_SAVE_STATE"};
    private static final String[] COMMAND_SAVE_LOAD_MEMORY = {"FR_SAVE_LOAD_MEMORY", "TX_SAVE_LOAD_MEMORY"};
    private static final String[] COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW = {"FR_TOGGLE_CODE_STRUCTURE_WINDOW", "TX_TOGGLE_CODE_STRUCTURE_WINDOW"};
    private static final String[] COMMAND_TOGGLE_SOURCE_CODE_WINDOW = {"FR_TOGGLE_SOURCE_CODE_WINDOW", "TX_TOGGLE_SOURCE_CODE_WINDOW"};
    private static final String[] COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW = {"FR_COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW", "TX_COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW"};
    private static final String[] COMMAND_TOGGLE_IO_PORTS_WINDOW = {"FR_COMMAND_TOGGLE_io_PORTS_WINDOW", "TX_COMMAND_TOGGLE_IO_PORTS_WINDOW"};
    private static final String[] COMMAND_TOGGLE_CALL_STACK_WINDOW = {"FR_TOGGLE_CALL_STACK_WINDOW", "TX_TOGGLE_CALL_STACK_WINDOW"};
    private static final String[] COMMAND_TOGGLE_REALOS_OBJECT_WINDOW = {"FR_TOGGLE_REALOS_OBJECT_WINDOW", "TX_TOGGLE_REALOS_OBJECT_WINDOW"};
    private static final String[] COMMAND_DISASSEMBLY_OPTIONS = {"FR_DISASSEMBLY_OPTIONS", "TX_DISASSEMBLY_OPTIONS"};

    private static final String COMMAND_TOGGLE_COMPONENT_4006_WINDOW = "TOGGLE_COMPONENT_4006_WINDOW";
    private static final String COMMAND_TOGGLE_SCREEN_EMULATOR = "TOGGLE_SCREEN_EMULATOR";

    private static final String COMMAND_UI_OPTIONS = "UI_OPTIONS";

    private static final String COMMAND_DECODE = "DECODE";
    private static final String COMMAND_ENCODE = "ENCODE";
    private static final String COMMAND_QUIT = "QUIT";
    private static final String COMMAND_ABOUT = "ABOUT";
    private static final String COMMAND_TEST = "TEST";

    public static final String BUTTON_SIZE_SMALL = "SMALL";
    public static final String BUTTON_SIZE_MEDIUM = "MEDIUM";
    public static final String BUTTON_SIZE_LARGE = "LARGE";

    private static final int BASE_ADDRESS_FUNCTION_CALL = 0xFFFFFFF0;
    private static final int BASE_ADDRESS_SYSCALL = 0xFFFFFF00;

    private static final int CAMERA_SCREEN_MEMORY_Y = 0xCE57DC60;
    private static final int CAMERA_SCREEN_MEMORY_U = CAMERA_SCREEN_MEMORY_Y + 0x64000;
    private static final int CAMERA_SCREEN_MEMORY_V = CAMERA_SCREEN_MEMORY_Y + 2 * 0x64000;
    private static final int CAMERA_SCREEN_WIDTH = 640;
    private static final int CAMERA_SCREEN_HEIGHT = 480;

    private static final String[] CPUSTATE_ENTRY_NAME = {"FrCPUState", "TxCPUState"};
    private static final String[] MEMORY_ENTRY_NAME = {"FrMemory", "TxMemory"};

    private static final String DEFAULT_STATUS_TEXT = "Ready";
    private static final String BUTTON_PROPERTY_KEY_ICON = "icon";


    // UI

    private final Insets toolbarButtonMargin;

    private JDesktopPane[] mdiPane = new JDesktopPane[2];
    private final JPanel[] toolBar = new JPanel[2];

    // Menu items
    private JMenuItem[] loadMenuItem = new JMenuItem[2];
    private JMenuItem[] playMenuItem = new JMenuItem[2];
    private JMenuItem[] debugMenuItem = new JMenuItem[2];
    private JMenuItem[] pauseMenuItem = new JMenuItem[2];
    private JMenuItem[] stepMenuItem = new JMenuItem[2];
    private JMenuItem[] stopMenuItem = new JMenuItem[2];
    private JMenuItem[] breakpointMenuItem = new JMenuItem[2];
    private JMenuItem[] generateSysSymbolsMenuItem = new JMenuItem[2];

    private JCheckBoxMenuItem component4006MenuItem;
    private JCheckBoxMenuItem screenEmulatorMenuItem;

    private JCheckBoxMenuItem[] disassemblyMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] cpuStateMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] memoryActivityViewerMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] memoryHexEditorMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] customMemoryRangeLoggerMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] codeStructureMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] sourceCodeMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] interruptControllerMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] programmableTimersMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] ioPortsMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] serialInterfacesMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] callStackMenuItem = new JCheckBoxMenuItem[2];
    private JCheckBoxMenuItem[] realosObjectMenuItem = new JCheckBoxMenuItem[2];

    private JMenuItem[] analyseMenuItem = new JMenuItem[2];
    private JMenuItem[] saveLoadMemoryMenuItem = new JMenuItem[2];
    private JMenuItem[] disassemblyOptionsMenuItem = new JMenuItem[2];

    @SuppressWarnings("FieldCanBeLocal")
    private JMenuItem uiOptionsMenuItem;

    // Buttons
    private JButton[] loadButton = new JButton[2];
    private JButton[] playButton = new JButton[2];
    private JButton[] debugButton = new JButton[2];
    private JButton[] pauseButton = new JButton[2];
    private JButton[] stepButton = new JButton[2];
    private JButton[] stopButton = new JButton[2];
    private JButton[] breakpointButton = new JButton[2];

    private JButton component4006Button;
    private JButton screenEmulatorButton;

    private JButton[] disassemblyButton = new JButton[2];
    private JButton[] cpuStateButton = new JButton[2];
    private JButton[] memoryActivityViewerButton = new JButton[2];
    private JButton[] memoryHexEditorButton = new JButton[2];
    private JButton[] customMemoryRangeLoggerButton = new JButton[2];
    private JButton[] codeStructureButton = new JButton[2];
    private JButton[] sourceCodeButton = new JButton[2];
    private JButton[] interruptControllerButton = new JButton[2];
    private JButton[] programmableTimersButton = new JButton[2];
    private JButton[] ioPortsButton = new JButton[2];
    private JButton[] serialInterfacesButton = new JButton[2];
    private JButton[] callStackButton = new JButton[2];
    private JButton[] realosObjectButton = new JButton[2];

    private JButton[] analyseButton = new JButton[2];
    private JButton[] saveLoadMemoryButton = new JButton[2];
    private JButton[] disassemblyOptionsButton = new JButton[2];

    // Frames
    private Component4006Frame component4006Frame;
    private DocumentFrame screenEmulatorFrame;

    private CPUStateEditorFrame[] cpuStateEditorFrame = new CPUStateEditorFrame[2];
    private DisassemblyFrame[] disassemblyLogFrame = new DisassemblyFrame[2];
    private BreakTriggerListFrame[] breakTriggerListFrame = new BreakTriggerListFrame[2];
    private MemoryActivityViewerFrame[] memoryActivityViewerFrame = new MemoryActivityViewerFrame[2];
    private MemoryHexEditorFrame[] memoryHexEditorFrame = new MemoryHexEditorFrame[2];
    private CustomMemoryRangeLoggerFrame[] customMemoryRangeLoggerFrame = new CustomMemoryRangeLoggerFrame[2];
    private CodeStructureFrame[] codeStructureFrame = new CodeStructureFrame[2];
    private SourceCodeFrame[] sourceCodeFrame = new SourceCodeFrame[2];
    private ProgrammableTimersFrame[] programmableTimersFrame = new ProgrammableTimersFrame[2];
    private IoPortsFrame[] ioPortsFrame = new IoPortsFrame[2];
    private InterruptControllerFrame[] interruptControllerFrame = new InterruptControllerFrame[2];
    private SerialInterfaceFrame[] serialInterfaceFrame = new SerialInterfaceFrame[2];
    private CallStackFrame[] callStackFrame = new CallStackFrame[2];

    private RealOsObjectFrame[] realOsObjectFrame = new RealOsObjectFrame[2];

    private String[] statusText = {DEFAULT_STATUS_TEXT, DEFAULT_STATUS_TEXT};
    private JLabel[] statusBar = new JLabel[2];
    private JSlider[] intervalSlider = new JSlider[2];

    private static ImageIcon[] programmableTimerButtonIcons;
    private int programmableTimerButtonAnimationCounter = 0;


    // Business fields
    private static File[] imageFile = new File[2];

    private Emulator[] emulator = new Emulator[2];
    private CPUState[] cpuState = new CPUState[]{new FrCPUState(), new TxCPUState()};
    private DebuggableMemory[] memory = new DebuggableMemory[2];
    private ClockGenerator[] clockGenerator = new ClockGenerator[2];
    private InterruptController[] interruptController = new InterruptController[2];
    private java.util.Timer[] programmableTimerButtonAnimationTimer = new java.util.Timer[2];
    private ProgrammableTimer[][] programmableTimers = new ProgrammableTimer[2][];
    private IoPort[][] ioPorts = new IoPort[2][];
    private SerialInterface[][] serialInterfaces = new SerialInterface[2][];

    private CodeStructure[] codeStructure = new CodeStructure[2];

    private boolean[] isImageLoaded = {false, false};
    private boolean[] isEmulatorPlaying = {false, false};

    long lastUpdateCycles[] = {0, 0};
    long lastUpdateTime[] = {0, 0};

    private Prefs prefs = new Prefs();
    private static final int[] CHIP_MODIFIER = new int[]{0, ActionEvent.SHIFT_MASK};
    private final JSplitPane splitPane;


    public static void main(String[] args) throws EmulationException, IOException, ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException {
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
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        EmulatorUI frame = new EmulatorUI();

        //Display the window.
        frame.setVisible(true);
    }

    public EmulatorUI() {
        super(ApplicationInfo.getName() + " v" + ApplicationInfo.getVersion());

        prefs = Prefs.load();
        // Apply register label prefs immediately
        FrCPUState.initRegisterLabels(prefs.getOutputOptions(Constants.CHIP_FR));
        TxCPUState.initRegisterLabels(prefs.getOutputOptions(Constants.CHIP_TX));

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //Set up the GUI.
        setJMenuBar(createMenuBar());

        toolbarButtonMargin = new Insets(2, 14, 2, 14);

        JPanel[] contentPane = new JPanel[2];
        for(int chip=0; chip < 2; chip++) {
            mdiPane[chip] = new JDesktopPane();
            statusBar[chip] = new JLabel(statusText[chip]);
            toolBar[chip] = createToolBar(chip);

            contentPane[chip] = new JPanel(new BorderLayout());
            contentPane[chip].add(toolBar[chip], BorderLayout.PAGE_START);
            contentPane[chip].add(mdiPane[chip], BorderLayout.CENTER);
            contentPane[chip].add(statusBar[chip], BorderLayout.SOUTH);
        }

        mdiPane[Constants.CHIP_FR].setBackground(new Color(240,240,255));
        mdiPane[Constants.CHIP_TX].setBackground(new Color(248,255,248));

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

        //togglePane(false);

        setContentPane(splitPane);

        applyPrefsToUI();


        for (int chip = 0; chip < 2; chip++) {
            if (imageFile[chip] != null) {
                loadImage(chip);
            }
        }

        restoreMainWindowSettings();

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
            setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);
        }
        else {
            setBounds(prefs.getMainWindowPositionX(), prefs.getMainWindowPositionY(), prefs.getMainWindowSizeX(), prefs.getMainWindowSizeY());

            splitPane.setDividerLocation(prefs.getDividerLocation());
            splitPane.setLastDividerLocation(prefs.getLastDividerLocation());
            setKeepHidden(splitPane, prefs.isDividerKeepHidden());
        }
    }

    /**
     * Method circumventing package access to setKeepHidden() method of BasicSplitPaneUI
     * @param splitPane
     * @param keepHidden
     * @author taken from http://java-swing-tips.googlecode.com/svn/trunk/OneTouchExpandable/src/java/example/MainPanel.java
     */
    private void setKeepHidden(JSplitPane splitPane, boolean keepHidden) {
        if(splitPane.getUI() instanceof BasicSplitPaneUI) {
            try{
                Method setKeepHidden = BasicSplitPaneUI.class.getDeclaredMethod("setKeepHidden", new Class<?>[] { Boolean.TYPE }); //boolean.class });
                setKeepHidden.setAccessible(true);
                setKeepHidden.invoke(splitPane.getUI(), new Object[] { keepHidden });
            }catch(Exception e) {
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
        if(splitPane.getUI() instanceof BasicSplitPaneUI) {
            try{
                Method getKeepHidden = BasicSplitPaneUI.class.getDeclaredMethod("getKeepHidden", new Class<?>[]{});
                getKeepHidden.setAccessible(true);
                return (Boolean)(getKeepHidden.invoke(splitPane.getUI()));
            }catch(Exception e) {
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
                            Image newimg = icon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH) ;
                            button.setIcon(new ImageIcon(newimg));
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

    public void setStatusText(int chip, String message) {
        statusText[chip] = message;
        updateStatusBar(chip);
    }

    private void updateStatusBar(int chip) {
        if (emulator[chip] != null) {
            long totalCycles = emulator[chip].getTotalCycles();
            long now = System.currentTimeMillis();
            long cps;
            try {
                cps = (1000 * (totalCycles - lastUpdateCycles[chip]))/(now - lastUpdateTime[chip]);
            } catch (Exception e) {
                cps = -1;
            }

            lastUpdateCycles[chip] = totalCycles;
            lastUpdateTime[chip] = now;
            statusBar[chip].setText(statusText[chip] + " (" + totalCycles + " cycles emulated. Current speed is " + (cps<0?"?":(""+cps)) + "Hz)");
        }
        else {
            statusBar[chip].setText(statusText[chip]);
        }
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

        cpuStateButton[chip] = makeButton("cpu", COMMAND_TOGGLE_CPUSTATE_WINDOW[chip], Constants.CHIP_LABEL[chip] + " CPU State window", "CPU");
        bar.add(cpuStateButton[chip]);
        memoryActivityViewerButton[chip] = makeButton("memory_activity", COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER[chip], Constants.CHIP_LABEL[chip] + " Memory activity viewer", "Activity");
        bar.add(memoryActivityViewerButton[chip]);
        memoryHexEditorButton[chip] = makeButton("memory_editor", COMMAND_TOGGLE_MEMORY_HEX_EDITOR[chip], Constants.CHIP_LABEL[chip] + " Memory hex editor", "Hex Editor");
        bar.add(memoryHexEditorButton[chip]);
        if (chip == Constants.CHIP_FR) {
            screenEmulatorButton = makeButton("screen", COMMAND_TOGGLE_SCREEN_EMULATOR, "Screen emulator", "Screen");
            bar.add(screenEmulatorButton);
        }
        interruptControllerButton[chip] = makeButton("interrupt", COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW[chip], Constants.CHIP_LABEL[chip] + " Interrupt controller", "Interrupt");
        bar.add(interruptControllerButton[chip]);
        programmableTimersButton[chip] = makeButton("reload", COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW[chip], "Toggle " + Constants.CHIP_LABEL[chip] + " programmable timers", "Programmable timers");
        bar.add(programmableTimersButton[chip]);

        if (chip == Constants.CHIP_TX) {
            ioPortsButton[chip] = makeButton("io", COMMAND_TOGGLE_IO_PORTS_WINDOW[chip], "Toggle " + Constants.CHIP_LABEL[chip] + " I/O Ports", "I/O Ports");
            bar.add(ioPortsButton[chip]);
        }

        serialInterfacesButton[chip] = makeButton("serial", COMMAND_TOGGLE_SERIAL_INTERFACES[chip], "Toggle " + Constants.CHIP_LABEL[chip] + " Serial interfaces", "Serial interfaces");
        bar.add(serialInterfacesButton[chip]);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        disassemblyButton[chip] = makeButton("disassembly_log", COMMAND_TOGGLE_DISASSEMBLY_WINDOW[chip], "Real time " + Constants.CHIP_LABEL[chip] + " disassembly log", "Disassembly");
        bar.add(disassemblyButton[chip]);
        if (chip == Constants.CHIP_FR) {
            component4006Button = makeButton("4006", COMMAND_TOGGLE_COMPONENT_4006_WINDOW, "Component 4006", "Component 4006");
            bar.add(component4006Button);
        }
        customMemoryRangeLoggerButton[chip] = makeButton("custom_logger", COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW[chip], "Custom " + Constants.CHIP_LABEL[chip] + " logger", "Custom logger");
        bar.add(customMemoryRangeLoggerButton[chip]);
        callStackButton[chip] = makeButton("call_stack", COMMAND_TOGGLE_CALL_STACK_WINDOW[chip], Constants.CHIP_LABEL[chip] + " Call Stack window", "CallStack");
        bar.add(callStackButton[chip]);
        realosObjectButton[chip] = makeButton("os", COMMAND_TOGGLE_REALOS_OBJECT_WINDOW[chip], Constants.CHIP_LABEL[chip] + " RealOS Object window", "RealOS Object");
        bar.add(realosObjectButton[chip]);

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

        disassemblyOptionsButton[chip] = makeButton("options", COMMAND_DISASSEMBLY_OPTIONS[chip], Constants.CHIP_LABEL[chip] + " disassembly options", "Options");
        bar.add(disassemblyOptionsButton[chip]);

        return bar;
    }

    private JSlider makeSlider(int chip) {
        intervalSlider[chip] = new JSlider(JSlider.HORIZONTAL, 0, 5, prefs.getSleepTick());

        intervalSlider[chip].addChangeListener(this);
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
            loadMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            loadMenuItem[chip].setActionCommand(COMMAND_IMAGE_LOAD[chip]);
            loadMenuItem[chip].addActionListener(this);
            fileMenu.add(loadMenuItem[chip]);
        }

        fileMenu.add(new JSeparator());

        //decoder
        tmpMenuItem = new JMenuItem("Decode firmware");
        tmpMenuItem.setMnemonic(KeyEvent.VK_D);
//        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
        tmpMenuItem.setActionCommand(COMMAND_DECODE);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);

        //encoder
        tmpMenuItem = new JMenuItem("Encode firmware (alpha)");
        tmpMenuItem.setMnemonic(KeyEvent.VK_E);
//        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
        tmpMenuItem.setActionCommand(COMMAND_ENCODE);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);

        fileMenu.add(new JSeparator());

        for (int chip = 0; chip < 2; chip++) {
            //decoder
            tmpMenuItem = new JMenuItem("Load " + Constants.CHIP_LABEL[chip] + "  state");
//            tmpMenuItem.setMnemonic(KeyEvent.VK_D);
//            tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            tmpMenuItem.setActionCommand(COMMAND_LOAD_STATE[chip]);
            tmpMenuItem.addActionListener(this);
            fileMenu.add(tmpMenuItem);

            //encoder
            tmpMenuItem = new JMenuItem("Save " + Constants.CHIP_LABEL[chip] + " state");
//            tmpMenuItem.setMnemonic(KeyEvent.VK_E);
//            tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
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
            if (chip == Constants.CHIP_FR) playMenuItem[chip].setMnemonic(KeyEvent.VK_E);
            playMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.CTRL_MASK | CHIP_MODIFIER[chip]));
            playMenuItem[chip].setActionCommand(COMMAND_EMULATOR_PLAY[chip]);
            playMenuItem[chip].addActionListener(this);
            runMenu.add(playMenuItem[chip]);

            //emulator debug
            debugMenuItem[chip] = new JMenuItem("Debug " + Constants.CHIP_LABEL[chip] + " emulator");
            if (chip == Constants.CHIP_FR) debugMenuItem[chip].setMnemonic(KeyEvent.VK_G);
            debugMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            debugMenuItem[chip].setActionCommand(COMMAND_EMULATOR_DEBUG[chip]);
            debugMenuItem[chip].addActionListener(this);
            runMenu.add(debugMenuItem[chip]);

            //emulator pause
            pauseMenuItem[chip] = new JMenuItem("Pause " + Constants.CHIP_LABEL[chip] + " emulator");
            if (chip == Constants.CHIP_FR) pauseMenuItem[chip].setMnemonic(KeyEvent.VK_P);
            pauseMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            pauseMenuItem[chip].setActionCommand(COMMAND_EMULATOR_PAUSE[chip]);
            pauseMenuItem[chip].addActionListener(this);
            runMenu.add(pauseMenuItem[chip]);

            //emulator step
            stepMenuItem[chip] = new JMenuItem("Step " + Constants.CHIP_LABEL[chip] + " emulator");
            if (chip == Constants.CHIP_FR) stepMenuItem[chip].setMnemonic(KeyEvent.VK_T);
            stepMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
            stepMenuItem[chip].setActionCommand(COMMAND_EMULATOR_STEP[chip]);
            stepMenuItem[chip].addActionListener(this);
            runMenu.add(stepMenuItem[chip]);

            //emulator stop
            stopMenuItem[chip] = new JMenuItem("Stop and reset " + Constants.CHIP_LABEL[chip] + " emulator");
            if (chip == Constants.CHIP_FR) stopMenuItem[chip].setMnemonic(KeyEvent.VK_R);
            playMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.SHIFT_MASK | CHIP_MODIFIER[chip]));
            stopMenuItem[chip].setActionCommand(COMMAND_EMULATOR_STOP[chip]);
            stopMenuItem[chip].addActionListener(this);
            runMenu.add(stopMenuItem[chip]);

            runMenu.add(new JSeparator());

            //setup breakpoints
            breakpointMenuItem[chip] = new JMenuItem("Setup " + Constants.CHIP_LABEL[chip] + " breakpoints");
            if (chip == Constants.CHIP_FR) breakpointMenuItem[chip].setMnemonic(KeyEvent.VK_B);
            breakpointMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
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
            cpuStateMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            cpuStateMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CPUSTATE_WINDOW[chip]);
            cpuStateMenuItem[chip].addActionListener(this);
            componentsMenu.add(cpuStateMenuItem[chip]);

            //memory hex editor
            memoryHexEditorMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " Memory hex editor");
            if (chip == Constants.CHIP_FR) memoryHexEditorMenuItem[chip].setMnemonic(KeyEvent.VK_H);
            memoryHexEditorMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            memoryHexEditorMenuItem[chip].setActionCommand(COMMAND_TOGGLE_MEMORY_HEX_EDITOR[chip]);
            memoryHexEditorMenuItem[chip].addActionListener(this);
            componentsMenu.add(memoryHexEditorMenuItem[chip]);

            //Interrupt controller
            interruptControllerMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " interrupt controller");
            if (chip == Constants.CHIP_FR) interruptControllerMenuItem[chip].setMnemonic(KeyEvent.VK_I);
            interruptControllerMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            interruptControllerMenuItem[chip].setActionCommand(COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW[chip]);
            interruptControllerMenuItem[chip].addActionListener(this);
            componentsMenu.add(interruptControllerMenuItem[chip]);

            //Programmble timers
            programmableTimersMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " programmable timers");
            //if (chip == CHIP_FR) programmableTimersMenuItem[chip].setMnemonic(KeyEvent.VK_I);
            //programmableTimersMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            programmableTimersMenuItem[chip].setActionCommand(COMMAND_TOGGLE_PROGRAMMABLE_TIMERS_WINDOW[chip]);
            programmableTimersMenuItem[chip].addActionListener(this);
            componentsMenu.add(programmableTimersMenuItem[chip]);

            //Serial interface
            serialInterfacesMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " serial interfaces");
//        if (chip == Constants.CHIP_FR) serialInterfacesMenuItem[chip].setMnemonic(KeyEvent.VK_I);
//        serialInterfacesMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            serialInterfacesMenuItem[chip].setActionCommand(COMMAND_TOGGLE_SERIAL_INTERFACES[chip]);
            serialInterfacesMenuItem[chip].addActionListener(this);
            componentsMenu.add(serialInterfacesMenuItem[chip]);

            componentsMenu.add(new JSeparator());

            if (chip == Constants.CHIP_TX) {
                //i/o ports
                ioPortsMenuItem[chip] = new JCheckBoxMenuItem("I/O ports");
//                ioPortsMenuItem[chip].setMnemonic(KeyEvent.VK_S);
//                ioPortsMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
                ioPortsMenuItem[chip].setActionCommand(COMMAND_TOGGLE_IO_PORTS_WINDOW[chip]);
                ioPortsMenuItem[chip].addActionListener(this);
                componentsMenu.add(ioPortsMenuItem[chip]);
            }

        }

        //screen emulator
        screenEmulatorMenuItem = new JCheckBoxMenuItem("Screen emulator");
        screenEmulatorMenuItem.setMnemonic(KeyEvent.VK_S);
        screenEmulatorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        screenEmulatorMenuItem.setActionCommand(COMMAND_TOGGLE_SCREEN_EMULATOR);
        screenEmulatorMenuItem.addActionListener(this);
        componentsMenu.add(screenEmulatorMenuItem);

        //Set up the trace menu.
        JMenu traceMenu = new JMenu("Trace");
        traceMenu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(traceMenu);

        for (int chip = 0; chip < 2; chip++) {
            //memory activity viewer
            memoryActivityViewerMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " Memory activity viewer");
            if (chip == Constants.CHIP_FR) memoryActivityViewerMenuItem[chip].setMnemonic(KeyEvent.VK_M);
            memoryActivityViewerMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            memoryActivityViewerMenuItem[chip].setActionCommand(COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER[chip]);
            memoryActivityViewerMenuItem[chip].addActionListener(this);
            traceMenu.add(memoryActivityViewerMenuItem[chip]);

            //disassembly
            disassemblyMenuItem[chip] = new JCheckBoxMenuItem("Real-time " + Constants.CHIP_LABEL[chip] + " disassembly log");
            if (chip == Constants.CHIP_FR) disassemblyMenuItem[chip].setMnemonic(KeyEvent.VK_D);
            disassemblyMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            disassemblyMenuItem[chip].setActionCommand(COMMAND_TOGGLE_DISASSEMBLY_WINDOW[chip]);
            disassemblyMenuItem[chip].addActionListener(this);
            traceMenu.add(disassemblyMenuItem[chip]);

            //Custom logger
            customMemoryRangeLoggerMenuItem[chip] = new JCheckBoxMenuItem("Custom " + Constants.CHIP_LABEL[chip] + " logger window");
//        if (chip == CHIP_FR) customMemoryRangeLoggerMenuItem[chip].setMnemonic(KeyEvent.VK_4);
//        customMemoryRangeLoggerMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            customMemoryRangeLoggerMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CUSTOM_LOGGER_WINDOW[chip]);
            customMemoryRangeLoggerMenuItem[chip].addActionListener(this);
            traceMenu.add(customMemoryRangeLoggerMenuItem[chip]);

            //Call Stack
            callStackMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " Call stack");
//        if (chip == CHIP_FR) callStackMenuItem[chip].setMnemonic(KeyEvent.VK_C);
//        callStackMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            callStackMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CALL_STACK_WINDOW[chip]);
            callStackMenuItem[chip].addActionListener(this);
            traceMenu.add(callStackMenuItem[chip]);

            //RealOS Object
            realosObjectMenuItem[chip] = new JCheckBoxMenuItem("RealOS " + Constants.CHIP_LABEL[chip] + " Objects");
//        if (chip == CHIP_FR) realosObjectMenuItem[chip].setMnemonic(KeyEvent.VK_C);
//        realosObjectMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            realosObjectMenuItem[chip].setActionCommand(COMMAND_TOGGLE_REALOS_OBJECT_WINDOW[chip]);
            realosObjectMenuItem[chip].addActionListener(this);
            traceMenu.add(realosObjectMenuItem[chip]);

            traceMenu.add(new JSeparator());
        }

        //Component 4006
        component4006MenuItem = new JCheckBoxMenuItem("Component 4006 window");
        component4006MenuItem.setMnemonic(KeyEvent.VK_4);
        component4006MenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
        component4006MenuItem.setActionCommand(COMMAND_TOGGLE_COMPONENT_4006_WINDOW);
        component4006MenuItem.addActionListener(this);
        traceMenu.add(component4006MenuItem);


        //Set up the tools menu.
        JMenu sourceMenu = new JMenu("Source");
        sourceMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(sourceMenu);

        for (int chip = 0; chip < 2; chip++) {
            //analyse / disassemble
            generateSysSymbolsMenuItem[chip] = new JMenuItem("Generate " + Constants.CHIP_LABEL[chip] + " system call symbols");
    //        if (chip == CHIP_FR) generateSysSymbolsMenuItem[chip].setMnemonic(KeyEvent.VK_A);
    //        generateSysSymbolsMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            generateSysSymbolsMenuItem[chip].setActionCommand(COMMAND_GENERATE_SYS_SYMBOLS[chip]);
            generateSysSymbolsMenuItem[chip].addActionListener(this);
            sourceMenu.add(generateSysSymbolsMenuItem[chip]);

            sourceMenu.add(new JSeparator());

            //analyse / disassemble
            analyseMenuItem[chip] = new JMenuItem("Analyse / Disassemble " + Constants.CHIP_LABEL[chip] + " code");
            if (chip == Constants.CHIP_FR) analyseMenuItem[chip].setMnemonic(KeyEvent.VK_A);
            analyseMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            analyseMenuItem[chip].setActionCommand(COMMAND_ANALYSE_DISASSEMBLE[chip]);
            analyseMenuItem[chip].addActionListener(this);
            sourceMenu.add(analyseMenuItem[chip]);

            sourceMenu.add(new JSeparator());

            //code structure
            codeStructureMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " code structure");
            if (chip == Constants.CHIP_FR) codeStructureMenuItem[chip].setMnemonic(KeyEvent.VK_C);
    //        codeStructureMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            codeStructureMenuItem[chip].setActionCommand(COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW[chip]);
            codeStructureMenuItem[chip].addActionListener(this);
            sourceMenu.add(codeStructureMenuItem[chip]);

            //source code
            sourceCodeMenuItem[chip] = new JCheckBoxMenuItem(Constants.CHIP_LABEL[chip] + " source code");
            if (chip == Constants.CHIP_FR) sourceCodeMenuItem[chip].setMnemonic(KeyEvent.VK_S);
    //        sourceCodeMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
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
    //        saveLoadMemoryMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            saveLoadMemoryMenuItem[chip].setActionCommand(COMMAND_SAVE_LOAD_MEMORY[chip]);
            saveLoadMemoryMenuItem[chip].addActionListener(this);
            toolsMenu.add(saveLoadMemoryMenuItem[chip]);

            //disassembly options
            disassemblyOptionsMenuItem[chip] = new JMenuItem(Constants.CHIP_LABEL[chip] + " disassembly options");
            if (chip == Constants.CHIP_FR) disassemblyOptionsMenuItem[chip].setMnemonic(KeyEvent.VK_O);
            disassemblyOptionsMenuItem[chip].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK | CHIP_MODIFIER[chip]));
            disassemblyOptionsMenuItem[chip].setActionCommand(COMMAND_DISASSEMBLY_OPTIONS[chip]);
            disassemblyOptionsMenuItem[chip].addActionListener(this);
            toolsMenu.add(disassemblyOptionsMenuItem[chip]);

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
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_GENERATE_SYS_SYMBOLS)) != Constants.CHIP_NONE) {
            openGenerateSysSymbolsDialog(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_ANALYSE_DISASSEMBLE)) != Constants.CHIP_NONE) {
            openAnalyseDialog(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_PLAY)) != Constants.CHIP_NONE) {
            playEmulator(chip, false, false, null);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_DEBUG)) != Constants.CHIP_NONE) {
            playEmulator(chip, false, true, null);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_PAUSE)) != Constants.CHIP_NONE) {
            pauseEmulator(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_STEP)) != Constants.CHIP_NONE) {
            playEmulator(chip, true, false, null);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_EMULATOR_STOP)) != Constants.CHIP_NONE) {
            stopEmulator(chip);
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
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_IO_PORTS_WINDOW)) != Constants.CHIP_NONE) {
            toggleIoPortsWindow(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_CALL_STACK_WINDOW)) != Constants.CHIP_NONE) {
            toggleCallStack(chip);
        }
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_TOGGLE_REALOS_OBJECT_WINDOW)) != Constants.CHIP_NONE) {
            toggleRealOsObject(chip);
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
        else if ((chip = getChipCommandMatchingAction(e, COMMAND_DISASSEMBLY_OPTIONS)) != Constants.CHIP_NONE) {
            openDisassemblyOptionsDialog(chip);
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
        new SaveLoadMemoryDialog(this, memory[chip]).setVisible(true);
    }


    private void openDecodeDialog() {
        JTextField sourceFile = new JTextField();
        JTextField destinationDir = new JTextField();
        final JComponent[] inputs = new JComponent[]{
                new FileSelectionPanel("Source file", sourceFile, false),
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
        final JComponent[] inputs = new JComponent[]{
                new FileSelectionPanel("Destination file", destinationFile, false),
                new FileSelectionPanel("Source file 1", sourceFile1, false),
                new FileSelectionPanel("Source file 2", sourceFile2, false)
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
                    cpuState[chip] = (CPUState) XStreamUtils.load(zipInputStream);

                    // Read memory
                    entry = zipInputStream.getNextEntry();
                    if (entry == null || !MEMORY_ENTRY_NAME[chip].equals(entry.getName())) {
                        JOptionPane.showMessageDialog(this, "Error loading state file\nSecond file not called " + MEMORY_ENTRY_NAME[chip], "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        memory[chip].loadAllFromStream(zipInputStream);
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
                xStream.toXML(cpuState[chip], writer);
                byte[] bytes = writer.toString().getBytes("UTF-8");

                ZipEntry zipEntry = new ZipEntry(CPUSTATE_ENTRY_NAME[chip]);
                zipEntry.setSize(bytes.length);
                zipOutputStream.putNextEntry(zipEntry);
                IOUtils.write(bytes, zipOutputStream);

                zipEntry = new ZipEntry(MEMORY_ENTRY_NAME[chip]);
                zipEntry.setSize(memory[chip].getNumPages() + memory[chip].getNumUsedPages() * memory[chip].getPageSize());
                zipOutputStream.putNextEntry(zipEntry);
                memory[chip].saveAllToStream(zipOutputStream);

                zipOutputStream.close();
                fileOutputStream.close();

                JOptionPane.showMessageDialog(this, "State saving complete", "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving state file\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openGenerateSysSymbolsDialog(int chip) {
        GenerateSysSymbolsDialog generateSysSymbolsDialog = new GenerateSysSymbolsDialog(this, memory[chip]);
        generateSysSymbolsDialog.startGeneration();
        generateSysSymbolsDialog.setVisible(true);
    }


    private void openAnalyseDialog(int chip) {
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
        writeOutputCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean writeToFile = writeOutputCheckbox.isSelected();
                destinationFileSelectionPanel.setEnabled(writeToFile);
                prefs.setWriteDisassemblyToFile(writeToFile);
            }
        });

        writeOutputCheckbox.setSelected(prefs.isWriteDisassemblyToFile());
        destinationFileSelectionPanel.setEnabled(prefs.isWriteDisassemblyToFile());

        final JComponent[] inputs = new JComponent[]{
                //new FileSelectionPanel("Source file", sourceFile, false, dependencies),
                new FileSelectionPanel((chip == Constants.CHIP_FR)?"Dfr options file":"Dtx options file", optionsField, false),
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
                AnalyseProgressDialog analyseProgressDialog = new AnalyseProgressDialog(this, memory[chip]);
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
                return f.getName().startsWith((chip == Constants.CHIP_FR)?"b":"a") && f.getName().endsWith(".bin");
            }

            @Override
            public String getDescription() {
                return Constants.CHIP_LABEL[chip] + " firmware file";
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

    private void openDisassemblyOptionsDialog(final int chip) {
        JPanel panel = new JPanel(new VerticalLayout(5, VerticalLayout.LEFT));
        panel.setName("Disassembler output");

        // Prepare sample code area
        final RSyntaxTextArea listingArea = new RSyntaxTextArea(15, 90);
        SourceCodeFrame.prepareAreaFormat(chip, listingArea);

        final List<JCheckBox> outputOptionsCheckBoxes = new ArrayList<JCheckBox>();
        ActionListener areaRefresherListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Set<OutputOption> sampleOptions = EnumSet.noneOf(OutputOption.class);
                    dumpOptionCheckboxes(outputOptionsCheckBoxes, sampleOptions);
                    int baseAddress = cpuState[chip].getResetAddress();
                    int lastAddress = baseAddress;
                    Memory sampleMemory = new DebuggableMemory();
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
                        disassembler.processOptions(chip, new String[]{"-m", "0x" + Format.asHex(baseAddress, 8) + "-0x" + Format.asHex(lastAddress, 8) + "=CODE"});
                    }
                    else {
                        // TODO
                        /*
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
                         */
                        disassembler = new Dtx();
                        disassembler.setDebugPrintWriter(new PrintWriter(new StringWriter())); // Ignore
                        disassembler.setOutputFileName(null);
                        disassembler.processOptions(chip, new String[]{"-m", "0x" + Format.asHex(baseAddress, 8) + "-0x" + Format.asHex(lastAddress, 8) + "=CODE"});
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
        for (OutputOption outputOption : OutputOption.allFormatOptions) {
            JCheckBox checkBox = makeOutputOptionCheckBox(chip, outputOption, prefs.getOutputOptions(chip), false);
            if (checkBox != null) {
                outputOptionsCheckBoxes.add(checkBox);
                panel.add(checkBox);
                checkBox.addActionListener(areaRefresherListener);
            }
        }

        // Force a refresh
        areaRefresherListener.actionPerformed(new ActionEvent(outputOptionsCheckBoxes.get(0), 0, ""));

        panel.add(new JLabel("Sample output:"));
        panel.add(new JScrollPane(listingArea));
        panel.add(new JLabel("Tip: hover over the option checkboxes for help"));

        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                panel,
                Constants.CHIP_LABEL[chip] + " disassembly options",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION))
        {
            // save
            dumpOptionCheckboxes(outputOptionsCheckBoxes, prefs.getOutputOptions(chip));
            // apply
            TxCPUState.initRegisterLabels(prefs.getOutputOptions(chip));
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
                + "</ul>"
                + "For more information, help or ideas, please join us at " + makeLink("http://nikonhacker.com") + "</body></html>");

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

    /**
     * React to slider moves
     * @param e
     */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        // if (!source.getValueIsAdjusting())
        for (int chip = 0; chip < 2; chip++) {
            if (source == intervalSlider[chip]) {
                setEmulatorSleepCode(chip, source.getValue());
                prefs.setSleepTick(/*todo chip, */source.getValue());
            }
        }
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


    private void loadImage(int chip) {
        try {
            cpuState[chip].reset();

            memory[chip] = new DebuggableMemory();
            memory[chip].loadFile(imageFile[chip], cpuState[chip].getResetAddress());

            emulator[chip] = (chip == Constants.CHIP_FR)?(new FrEmulator()):(new TxEmulator());
            emulator[chip].setMemory(memory[chip]);


            switch (chip) {
                case Constants.CHIP_FR :
                    clockGenerator[chip] = new FrClockGenerator();

                    interruptController[chip] = new FrInterruptController(memory[chip]);

                    programmableTimers[chip] = new FrReloadTimer[]{
                            new FrReloadTimer(0, interruptController[chip]),
                            new FrReloadTimer(1, interruptController[chip]),
                            new FrReloadTimer(2, interruptController[chip])
                    };

                    ioPorts[chip] = new IoPort[]{
//                            new FrIoPort(0x0, (FrInterruptController)interruptController[chip])
                    };

                    serialInterfaces[chip] = new SerialInterface[]{
                            /** The number of actual serial interfaces is pure speculation. See ExpeedIoListener for more info */
                            new SerialInterface(0, interruptController[chip], 0x1B),
                            new SerialInterface(1, interruptController[chip], 0x1B),
                            new SerialInterface(2, interruptController[chip], 0x1B),
                            new SerialInterface(3, interruptController[chip], 0x1B),
                            new SerialInterface(4, interruptController[chip], 0x1B),
                            new SerialInterface(5, interruptController[chip], 0x1B)
                    };

                    memory[chip].setIoActivityListener(
                            new ExpeedIoListener(
                                    (FrInterruptController) interruptController[chip],
                                    (FrReloadTimer[]) programmableTimers[chip],
                                    serialInterfaces[chip]
                            )
                    );
                    break;


                case Constants.CHIP_TX:
                    clockGenerator[chip] = new TxClockGenerator();

                    interruptController[chip] = new TxInterruptController((TxCPUState)cpuState[chip], memory[chip]);

                    programmableTimers[chip] = new TxTimer[]{
                            new TxTimer(0x0, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x1, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x2, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x3, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x4, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x5, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x6, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x7, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x8, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x9, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0xA, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0xB, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0xC, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0xD, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0xE, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0xF, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x10, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip]),
                            new TxTimer(0x11, (TxCPUState) cpuState[chip], (TxClockGenerator)clockGenerator[chip], (TxInterruptController)interruptController[chip])
                    };

                    ioPorts[chip] = new TxIoPort[]{
                            new TxIoPort(0x0, (TxInterruptController)interruptController[chip])
                    };

                    serialInterfaces[chip] = new SerialInterface[]{
    /*
                            new SerialInterface(0, interruptController[chip], 0x1B), //TODO
                            new SerialInterface(1, interruptController[chip], 0x1B),
                            new SerialInterface(2, interruptController[chip], 0x1B),
                            new SerialInterface(3, interruptController[chip], 0x1B),
                            new SerialInterface(4, interruptController[chip], 0x1B),
                            new SerialInterface(5, interruptController[chip], 0x1B)
    */
                    };

                    ((TxCPUState)cpuState[chip]).setInterruptController((TxInterruptController) interruptController[chip]);

                    memory[chip].setIoActivityListener(
                            new TxIoListener(
                                    (TxClockGenerator) clockGenerator[chip],
                                    (TxInterruptController) interruptController[chip],
                                    (TxTimer[]) programmableTimers[chip],
                                    (TxIoPort[]) ioPorts[chip],
                                    serialInterfaces[chip]
                            )
                    );
                    break;
                default:
                    throw new RuntimeException("Unknown chip : " + chip);
            }

            emulator[chip].setInterruptController(interruptController[chip]);

            setEmulatorSleepCode(chip, prefs.getSleepTick());

            isImageLoaded[chip] = true;
            if (prefs.isCloseAllWindowsOnStop()) {
                closeAllFrames();
            }

            updateStates();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeAllFrames() {
        if (component4006Frame != null) {
            component4006Frame.dispose();
            component4006Frame = null;
        }
        if (screenEmulatorFrame != null) {
            screenEmulatorFrame.dispose();
            screenEmulatorFrame = null;
        }
        for (int chip = 0; chip < 2; chip++) {
            if (cpuStateEditorFrame[chip] != null) {
                cpuStateEditorFrame[chip].dispose();
                cpuStateEditorFrame[chip] = null;
            }
            if (disassemblyLogFrame[chip] != null) {
                disassemblyLogFrame[chip].dispose();
                disassemblyLogFrame[chip] = null;
            }
            if (breakTriggerListFrame[chip] != null) {
                breakTriggerListFrame[chip].dispose();
                breakTriggerListFrame[chip] = null;
            }
            if (memoryActivityViewerFrame[chip] != null) {
                memoryActivityViewerFrame[chip].dispose();
                memoryActivityViewerFrame[chip] = null;
            }
            if (memoryHexEditorFrame[chip] != null) {
                memoryHexEditorFrame[chip].dispose();
                memoryHexEditorFrame[chip] = null;
            }
            if (customMemoryRangeLoggerFrame[chip] != null) {
                customMemoryRangeLoggerFrame[chip].dispose();
                customMemoryRangeLoggerFrame[chip] = null;
            }
            if (codeStructureFrame[chip] != null) {
                codeStructureFrame[chip].dispose();
                codeStructureFrame[chip] = null;
            }
            if (sourceCodeFrame[chip] != null) {
                sourceCodeFrame[chip].dispose();
                sourceCodeFrame[chip] = null;
            }
            if (callStackFrame[chip] != null) {
                callStackFrame[chip].dispose();
                callStackFrame[chip] = null;
            }
            if (programmableTimersFrame[chip] != null) {
                programmableTimersFrame[chip].dispose();
                programmableTimersFrame[chip] = null;
            }
            if (ioPortsFrame[chip] != null) {
                ioPortsFrame[chip].dispose();
                ioPortsFrame[chip] = null;
            }
            if (interruptControllerFrame[chip] != null) {
                interruptControllerFrame[chip].dispose();
                interruptControllerFrame[chip] = null;
            }
            if (serialInterfaceFrame[chip] != null) {

                serialInterfaceFrame[chip].dispose();
                serialInterfaceFrame[chip] = null;
            }
        }
    }

    private void toggleBreakTriggerList(int chip) {
        if (breakTriggerListFrame[chip] == null) {
            breakTriggerListFrame[chip] = new BreakTriggerListFrame("Setup breakpoints and triggers", "breakpoint", true, true, true, true, chip, this, emulator[chip], prefs.getTriggers(chip), memory[chip]);
            addDocumentFrame(chip, breakTriggerListFrame[chip]);
            breakTriggerListFrame[chip].display(true);
        }
        else {
            breakTriggerListFrame[chip].dispose();
            breakTriggerListFrame[chip] = null;
        }
        updateStates();
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
        updateStates();
    }

    private void toggleCPUState(int chip) {
        if (cpuStateEditorFrame[chip] == null) {
            cpuStateEditorFrame[chip] = new CPUStateEditorFrame("CPU State", "cpu", false, true, false, true, chip, this, cpuState[chip]);
            cpuStateEditorFrame[chip].setEnabled(!isEmulatorPlaying[chip]);
            if (disassemblyLogFrame[chip] != null) cpuStateEditorFrame[chip].setInstructionPrintWriter(disassemblyLogFrame[chip].getInstructionPrintWriter());
            addDocumentFrame(chip, cpuStateEditorFrame[chip]);
            cpuStateEditorFrame[chip].display(true);
        }
        else {
            cpuStateEditorFrame[chip].dispose();
            cpuStateEditorFrame[chip] = null;
        }
        updateStates();
    }

    private void toggleMemoryActivityViewer(int chip) {
        if (memoryActivityViewerFrame[chip] == null) {
            memoryActivityViewerFrame[chip] = new MemoryActivityViewerFrame("Base memory activity viewer (each cell=64k, click to zoom)", "memory_activity", true, true, true, true, chip, this, memory[chip]);
            addDocumentFrame(chip, memoryActivityViewerFrame[chip]);
            memoryActivityViewerFrame[chip].display(true);
        }
        else {
            memoryActivityViewerFrame[chip].dispose();
            memoryActivityViewerFrame[chip] = null;
        }
        updateStates();
    }

    private void toggleMemoryHexEditor(int chip) {
        if (memoryHexEditorFrame[chip] == null) {
            memoryHexEditorFrame[chip] = new MemoryHexEditorFrame("Memory hex editor", "memory_editor", true, true, true, true, chip, this, memory[chip], cpuState[chip], 0, !isEmulatorPlaying[chip]);
            addDocumentFrame(chip, memoryHexEditorFrame[chip]);
            memoryHexEditorFrame[chip].display(true);
        }
        else {
            memoryHexEditorFrame[chip].dispose();
            memoryHexEditorFrame[chip] = null;
        }
        updateStates();
    }

    private void toggleScreenEmulator() {
        if (screenEmulatorFrame == null) {
            screenEmulatorFrame = new ScreenEmulatorFrame("Screen emulator", "screen", true, true, true, true, Constants.CHIP_FR, this, memory[Constants.CHIP_FR], CAMERA_SCREEN_MEMORY_Y, CAMERA_SCREEN_MEMORY_U, CAMERA_SCREEN_MEMORY_V, CAMERA_SCREEN_WIDTH, CAMERA_SCREEN_HEIGHT);
            addDocumentFrame(Constants.CHIP_FR, screenEmulatorFrame);
            screenEmulatorFrame.display(true);
        }
        else {
            screenEmulatorFrame.dispose();
            screenEmulatorFrame = null;
        }
        updateStates();
    }


    private void toggleComponent4006() {
        if (component4006Frame == null) {
            component4006Frame = new Component4006Frame("Component 4006", "4006", true, true, false, true, Constants.CHIP_FR, this, memory[Constants.CHIP_FR], 0x4006, cpuState[Constants.CHIP_FR]);
            addDocumentFrame(Constants.CHIP_FR, component4006Frame);
            component4006Frame.display(true);
        }
        else {
            component4006Frame.dispose();
            component4006Frame = null;
        }
        updateStates();
    }

    private void toggleCustomMemoryRangeLoggerComponentFrame(int chip) {
        if (customMemoryRangeLoggerFrame[chip] == null) {
            customMemoryRangeLoggerFrame[chip] = new CustomMemoryRangeLoggerFrame("Custom Logger", "custom_logger", true, true, false, true, chip, this, memory[chip], cpuState[chip]);
            addDocumentFrame(chip, customMemoryRangeLoggerFrame[chip]);
            customMemoryRangeLoggerFrame[chip].display(true);
        }
        else {
            customMemoryRangeLoggerFrame[chip].dispose();
            customMemoryRangeLoggerFrame[chip] = null;
        }
        updateStates();
    }

    private void toggleProgrammableTimersWindow(int chip) {
        if (programmableTimersFrame[chip] == null) {
            programmableTimersFrame[chip] = new ProgrammableTimersFrame("Programmable timers", "reload", true, true, false, true, chip, this, programmableTimers[chip]);
            addDocumentFrame(chip, programmableTimersFrame[chip]);
            programmableTimersFrame[chip].display(true);
        }
        else {
            programmableTimersFrame[chip].dispose();
            programmableTimersFrame[chip] = null;
        }
        updateStates();
    }

    private void toggleIoPortsWindow(int chip) {
        if (ioPortsFrame[chip] == null) {
            ioPortsFrame[chip] = new IoPortsFrame("I/O ports", "io", true, true, false, true, chip, this, ioPorts[chip]);
            addDocumentFrame(chip, ioPortsFrame[chip]);
            ioPortsFrame[chip].display(true);
        }
        else {
            ioPortsFrame[chip].dispose();
            ioPortsFrame[chip] = null;
        }
        updateStates();
    }


    private void toggleInterruptController(int chip) {
        if (interruptControllerFrame[chip] == null) {
            interruptControllerFrame[chip] = (chip == Constants.CHIP_FR)
                    ?new FrInterruptControllerFrame("Interrupt controller", "interrupt", true, true, false, true, chip, this, interruptController[chip], memory[chip])
                    :new TxInterruptControllerFrame("Interrupt controller", "interrupt", true, true, false, true, chip, this, interruptController[chip], memory[chip]);
            addDocumentFrame(chip, interruptControllerFrame[chip]);
            interruptControllerFrame[chip].display(true);
        }
        else {
            interruptControllerFrame[chip].dispose();
            interruptControllerFrame[chip] = null;
        }
        updateStates();
    }


    private void toggleSerialInterfaces(int chip) {
        if (serialInterfaceFrame[chip] == null) {
            serialInterfaceFrame[chip] = new SerialInterfaceFrame("Serial interfaces", "serial", true, true, false, true, chip, this, serialInterfaces[chip]);
            addDocumentFrame(chip, serialInterfaceFrame[chip]);
            serialInterfaceFrame[chip].display(true);
        }
        else {
            serialInterfaceFrame[chip].dispose();
            serialInterfaceFrame[chip] = null;
        }
        updateStates();
    }


    static {
        initProgrammableTimerAnimationIcons(BUTTON_SIZE_SMALL);
    }


    private static void initProgrammableTimerAnimationIcons(String buttonSize) {
        programmableTimerButtonIcons = new ImageIcon[17];
        for (int i = 0; i < programmableTimerButtonIcons.length; i++) {
            String imgLocation = "images/reload";
            String text;
            if (i == 0) {
                text = "Start programmable timer";
            }
            else {
                imgLocation += "_" + i;
                text = "Stop programmable timer";
            }
            programmableTimerButtonIcons[i] = new ImageIcon(EmulatorUI.class.getResource(imgLocation + ".png"), text);
            if (BUTTON_SIZE_SMALL.equals(buttonSize)) {
                programmableTimerButtonIcons[i] = new ImageIcon(programmableTimerButtonIcons[i].getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
            }
        }
    }


    public void setProgrammableTimerAnimationEnabled(final int chip, boolean enabled) {
        if (enabled) {
            // Animate button
            programmableTimerButtonAnimationTimer[chip] = new java.util.Timer(false);
            programmableTimerButtonAnimationTimer[chip].scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    programmableTimerButtonAnimationCounter++;
                    if (programmableTimerButtonAnimationCounter == programmableTimerButtonIcons.length) {
                        programmableTimerButtonAnimationCounter = 1;
                    }
                    programmableTimersButton[chip].setIcon(programmableTimerButtonIcons[programmableTimerButtonAnimationCounter]);
                }
            }, 0, 300 /*ms*/);
        }
        else {
            // Stop button animation
            programmableTimerButtonAnimationTimer[chip].cancel();
            programmableTimerButtonAnimationTimer[chip] = null;
            programmableTimersButton[chip].setIcon(programmableTimerButtonIcons[0]);
        }
    }


    private void toggleCallStack(int chip) {
        if (callStackFrame[chip] == null) {
            callStackFrame[chip] = new CallStackFrame("Call Stack", "call_stack", true, true, false, true, chip, this, emulator[chip], cpuState[chip], codeStructure[chip]);
            callStackFrame[chip].setAutoRefresh(isEmulatorPlaying[chip]);
            addDocumentFrame(chip, callStackFrame[chip]);
            callStackFrame[chip].display(true);
        }
        else {
            callStackFrame[chip].dispose();
            callStackFrame[chip] = null;
        }
        updateStates();
    }

    private void toggleRealOsObject(int chip) {
        if (realOsObjectFrame[chip] == null) {
            realOsObjectFrame[chip] = new RealOsObjectFrame("µITRON Object Status", "os", true, true, false, true, chip, this);
            realOsObjectFrame[chip].enableUpdate(!isEmulatorPlaying[chip]);
            if (!isEmulatorPlaying[chip]) {
                realOsObjectFrame[chip].updateAllLists(chip);
            }
            addDocumentFrame(chip, realOsObjectFrame[chip]);
            realOsObjectFrame[chip].display(true);
        }
        else {
            realOsObjectFrame[chip].dispose();
            realOsObjectFrame[chip] = null;
        }
        updateStates();
    }


    private void toggleCodeStructureWindow(int chip) {
        if (codeStructureFrame[chip] == null) {
            codeStructureFrame[chip] = new CodeStructureFrame("Code structure", "code_structure", true, true, true, true, chip, this, cpuState[chip], codeStructure[chip]);
            addDocumentFrame(chip, codeStructureFrame[chip]);
            codeStructureFrame[chip].display(true);
        }
        else {
            codeStructureFrame[chip].dispose();
            codeStructureFrame[chip] = null;
        }
        updateStates();
    }

    private void toggleSourceCodeWindow(int chip) {
        if (sourceCodeFrame[chip] == null) {
            sourceCodeFrame[chip] = new SourceCodeFrame("Source code", "source", true, true, true, true, chip, this, cpuState[chip], codeStructure[chip]);
            addDocumentFrame(chip, sourceCodeFrame[chip]);
            sourceCodeFrame[chip].display(true);
        }
        else {
            sourceCodeFrame[chip].dispose();
            sourceCodeFrame[chip] = null;
        }
        updateStates();
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
        for (int chip = 0; chip < 2; chip++) {
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
            else if (frame == ioPortsFrame[chip]) {
                toggleIoPortsWindow(chip) ; return;
            }
            else if (frame == interruptControllerFrame[chip]) {
                toggleInterruptController(chip); return;
            }
            else if (frame == serialInterfaceFrame[chip]) {
                toggleSerialInterfaces(chip); return;
            }
            else if (frame == realOsObjectFrame[chip]) {
                toggleRealOsObject(chip); return;
            }
        }
        System.err.println("EmulatorUI.frameClosing : Unknown frame is being closed. Please add handler for " + frame.getClass().getSimpleName());
    }

    public void updateStates() {
        component4006MenuItem.setSelected(component4006Frame != null);
        screenEmulatorMenuItem.setSelected(screenEmulatorFrame != null);

        component4006MenuItem.setEnabled(isImageLoaded[Constants.CHIP_FR]); component4006Button.setEnabled(isImageLoaded[Constants.CHIP_FR]);
        screenEmulatorMenuItem.setEnabled(isImageLoaded[Constants.CHIP_FR]); screenEmulatorButton.setEnabled(isImageLoaded[Constants.CHIP_FR]);

        for (int chip = 0; chip < 2; chip++) {
            // Menus and buttons enabled or not
            codeStructureMenuItem[chip].setEnabled(codeStructure[chip] != null); codeStructureButton[chip].setEnabled(codeStructure[chip] != null);
            sourceCodeMenuItem[chip].setEnabled(codeStructure[chip] != null); sourceCodeButton[chip].setEnabled(codeStructure[chip] != null);

            // CheckboxMenuItem checked or not
            cpuStateMenuItem[chip].setSelected(cpuStateEditorFrame[chip] != null);
            disassemblyMenuItem[chip].setSelected(disassemblyLogFrame[chip] != null);
            memoryActivityViewerMenuItem[chip].setSelected(memoryActivityViewerFrame[chip] != null);
            memoryHexEditorMenuItem[chip].setSelected(memoryHexEditorFrame[chip] != null);
            customMemoryRangeLoggerMenuItem[chip].setSelected(customMemoryRangeLoggerFrame[chip] != null);
            codeStructureMenuItem[chip].setSelected(codeStructureFrame[chip] != null);
            sourceCodeMenuItem[chip].setSelected(sourceCodeFrame[chip] != null);
            programmableTimersMenuItem[chip].setSelected(programmableTimersFrame[chip] != null);
            if (chip == Constants.CHIP_TX) ioPortsMenuItem[chip].setSelected(ioPortsFrame[chip] != null);
            interruptControllerMenuItem[chip].setSelected(interruptControllerFrame[chip] != null);
            serialInterfacesMenuItem[chip].setSelected(serialInterfaceFrame[chip] != null);

            generateSysSymbolsMenuItem[chip].setEnabled(isImageLoaded[chip]);
            analyseMenuItem[chip].setEnabled(isImageLoaded[chip]); analyseButton[chip].setEnabled(isImageLoaded[chip]);

            cpuStateMenuItem[chip].setEnabled(isImageLoaded[chip]); cpuStateButton[chip].setEnabled(isImageLoaded[chip]);
            disassemblyMenuItem[chip].setEnabled(isImageLoaded[chip]); disassemblyButton[chip].setEnabled(isImageLoaded[chip]);
            memoryActivityViewerMenuItem[chip].setEnabled(isImageLoaded[chip]); memoryActivityViewerButton[chip].setEnabled(isImageLoaded[chip]);
            memoryHexEditorMenuItem[chip].setEnabled(isImageLoaded[chip]); memoryHexEditorButton[chip].setEnabled(isImageLoaded[chip]);
            customMemoryRangeLoggerMenuItem[chip].setEnabled(isImageLoaded[chip]); customMemoryRangeLoggerButton[chip].setEnabled(isImageLoaded[chip]);
            programmableTimersMenuItem[chip].setEnabled(isImageLoaded[chip]); programmableTimersButton[chip].setEnabled(isImageLoaded[chip]);
            if (chip == Constants.CHIP_TX) {
                ioPortsMenuItem[chip].setEnabled(isImageLoaded[chip]); ioPortsButton[chip].setEnabled(isImageLoaded[chip]);
            }
            interruptControllerMenuItem[chip].setEnabled(isImageLoaded[Constants.CHIP_FR]); interruptControllerButton[chip].setEnabled(isImageLoaded[chip]);
            serialInterfacesMenuItem[chip].setEnabled(isImageLoaded[Constants.CHIP_FR]); serialInterfacesButton[chip].setEnabled(isImageLoaded[chip]);
            callStackMenuItem[chip].setEnabled(isImageLoaded[chip]); callStackButton[chip].setEnabled(isImageLoaded[chip]);
            realosObjectMenuItem[chip].setEnabled(isImageLoaded[chip]); realosObjectButton[chip].setEnabled(isImageLoaded[chip]);

            saveLoadMemoryMenuItem[chip].setEnabled(isImageLoaded[chip]); saveLoadMemoryButton[chip].setEnabled(isImageLoaded[chip]);

            stopMenuItem[chip].setEnabled(isImageLoaded[chip]); stopButton[chip].setEnabled(isImageLoaded[chip]);

            disassemblyOptionsMenuItem[chip].setEnabled(!isAtLeastOneEmulatorPlaying()); disassemblyOptionsButton[chip].setEnabled(!isAtLeastOneEmulatorPlaying());

            if (isImageLoaded[chip]) {
                // Depends whether emulator is playing or not
                loadMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); loadButton[chip].setEnabled(!isEmulatorPlaying[chip]);
                playMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); playButton[chip].setEnabled(!isEmulatorPlaying[chip]);
                debugMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); debugButton[chip].setEnabled(!isEmulatorPlaying[chip]);
                pauseMenuItem[chip].setEnabled(isEmulatorPlaying[chip]); pauseButton[chip].setEnabled(isEmulatorPlaying[chip]);
                stepMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); stepButton[chip].setEnabled(!isEmulatorPlaying[chip]);
                breakpointMenuItem[chip].setEnabled(!isEmulatorPlaying[chip]); breakpointButton[chip].setEnabled(!isEmulatorPlaying[chip]);
                // Editable components
                if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setEditable(!isEmulatorPlaying[chip]);
                if (memoryHexEditorFrame[chip] != null) memoryHexEditorFrame[chip].setEditable(!isEmulatorPlaying[chip]);
                if (callStackFrame[chip] != null) callStackFrame[chip].setAutoRefresh(isEmulatorPlaying[chip]);
                if (realOsObjectFrame[chip] != null) realOsObjectFrame[chip].enableUpdate(!isEmulatorPlaying[chip]);
            }
            else {
                loadMenuItem[chip].setEnabled(true); loadButton[chip].setEnabled(true);
                playMenuItem[chip].setEnabled(false); playButton[chip].setEnabled(false);
                debugMenuItem[chip].setEnabled(false); debugButton[chip].setEnabled(false);
                pauseMenuItem[chip].setEnabled(false); pauseButton[chip].setEnabled(false);
                stepMenuItem[chip].setEnabled(false); stepButton[chip].setEnabled(false);
                breakpointMenuItem[chip].setEnabled(false); breakpointButton[chip].setEnabled(false);
                // Editable components  TODO does it make sense ? And why true ?
                if (cpuStateEditorFrame[chip] != null) cpuStateEditorFrame[chip].setEditable(true);
                if (memoryHexEditorFrame[chip] != null) memoryHexEditorFrame[chip].setEditable(true);
                if (callStackFrame[chip] != null) callStackFrame[chip].setAutoRefresh(false);
                if (realOsObjectFrame[chip] != null) realOsObjectFrame[chip].enableUpdate(true);
            }
        }

    }

    private boolean isAtLeastOneEmulatorPlaying() {
        return (isEmulatorPlaying[Constants.CHIP_FR] || isEmulatorPlaying[Constants.CHIP_TX]);
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

    /**
     * Starts an emulating session
     * @param chip
     * @param stepMode if true, only execute one exception
     * @param debugMode if true, defined user breakpoints are active
     * @param endAddress if not null, stop when reaching this address
     */
    private void playEmulator(int chip, boolean stepMode, boolean debugMode, Integer endAddress) {
        if (!isImageLoaded[chip]) {
            throw new RuntimeException("No Image loaded !");
        }

        emulator[chip].clearBreakConditions();

        if (stepMode) {
            emulator[chip].addBreakCondition(new AlwaysBreakCondition());
        }
        else {
            if (debugMode) {
                for (BreakTrigger breakTrigger : prefs.getTriggers(chip)) {
                    if (breakTrigger.mustBreak() || breakTrigger.mustBeLogged() || breakTrigger.getInterruptToRequest() != null || breakTrigger.getPcToSet() != null) {
                        emulator[chip].addBreakCondition(new AndCondition(breakTrigger.getBreakConditions(codeStructure[chip], memory[chip]), breakTrigger));
                    }
                }
            }
            if (endAddress != null) {
                // Set a temporary break condition at given endAddress
                FrCPUState values = new FrCPUState(endAddress);
                FrCPUState flags = new FrCPUState();
                flags.pc = 1;
                flags.setILM(0, false);
                flags.setReg(FrCPUState.TBR, 0);
                BreakTrigger breakTrigger = new BreakTrigger("Run to cursor at 0x" + Format.asHex(endAddress, 8), values, flags, new ArrayList<MemoryValueBreakCondition>());
                emulator[chip].addBreakCondition(new BreakPointCondition(endAddress, breakTrigger));
            }
        }
        startEmulator(chip);
    }


    public void playToAddress(int chip, Integer endAddress, boolean debugMode) {
        playEmulator(chip, false, debugMode, endAddress);
    }

    private void startEmulator(final int chip) {
        isEmulatorPlaying[chip] = true;
        updateStates();
        Thread emulatorThread = new Thread(new Runnable() {
            public void run() {
                emulator[chip].setCpuState(cpuState[chip]);
                emulator[chip].setOutputOptions(prefs.getOutputOptions(chip));
                setStatusText(chip, "Emulator is running...");
                BreakCondition stopCause = null;
                try {
                    stopCause = emulator[chip].play();
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    String message = t.getMessage();
                    if (StringUtils.isEmpty(message)) {
                        message = t.getClass().getName();
                    }
                    JOptionPane.showMessageDialog(EmulatorUI.this, message + "\nSee console for more info", "Emulator error", JOptionPane.ERROR_MESSAGE);
                }
                try {
                    isEmulatorPlaying[chip] = false;
                    signalEmulatorStopped(chip);
                    if (stopCause != null && stopCause.getBreakTrigger() != null) {
                        setStatusText(chip, "Break trigger matched : " + stopCause.getBreakTrigger().getName());
                    }
                    else {
                        setStatusText(chip, "Emulation complete");
                    }
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
                updateStates();
            }
        });
        emulatorThread.start();
    }

    private void signalEmulatorStopped(int chip) {
        // TODO this should iterate on all windows. A NOP onEmulatorStop() should exist in DocumentFrame
        if (sourceCodeFrame[chip] != null) {
            sourceCodeFrame[chip].onEmulatorStop();
        }
        if (realOsObjectFrame[chip] != null) {
            realOsObjectFrame[chip].onEmulatorStop(chip);
        }
    }


    public void playOneFunction(int chip, int address, boolean debugMode) {
        // TODO : make the call transparent by cloning CPUState
        // To execute one function only, we put a fake CALL at a conventional place, followed by an infinite loop
        memory[chip].store16(BASE_ADDRESS_FUNCTION_CALL, 0x9f8c);      // LD          ,R12
        memory[chip].store32(BASE_ADDRESS_FUNCTION_CALL + 2, address); //     address
        memory[chip].store16(BASE_ADDRESS_FUNCTION_CALL + 6, 0x971c);  // CALL @R12
        memory[chip].store16(BASE_ADDRESS_FUNCTION_CALL + 8, 0xe0ff);  // HALT, infinite loop

        // And we put a breakpoint on the instruction after the call
        emulator[chip].clearBreakConditions();
        emulator[chip].addBreakCondition(new BreakPointCondition(BASE_ADDRESS_FUNCTION_CALL + 8, null));

        cpuState[chip].pc = BASE_ADDRESS_FUNCTION_CALL;

        if (debugMode) {
            for (BreakTrigger breakTrigger : prefs.getTriggers(chip)) {
                if (breakTrigger.mustBreak() || breakTrigger.mustBeLogged()) {
                    emulator[chip].addBreakCondition(new AndCondition(breakTrigger.getBreakConditions(codeStructure[chip], memory[chip]), breakTrigger));
                }
            }
        }

        startEmulator(chip);
    }

    public TaskInformation getTaskInformation(int objId) {
        // todo chip
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall(0xEC, pk_robj, objId);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new TaskInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            return new TaskInformation(objId, errorCode, memory[Constants.CHIP_FR].load32(pk_robj), memory[Constants.CHIP_FR].load32(pk_robj + 4), memory[Constants.CHIP_FR].load32(pk_robj + 8));
        }
    }

    public SemaphoreInformation getSemaphoreInformation(int objId) {
        // todo chip
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall(0xCC, pk_robj, objId);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new SemaphoreInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            return new SemaphoreInformation(objId, errorCode, memory[Constants.CHIP_FR].load32(pk_robj), memory[Constants.CHIP_FR].load32(pk_robj + 4), memory[Constants.CHIP_FR].load32(pk_robj + 8));
        }
    }

    public EventFlagInformation getEventFlagInformation(int objId) {
        // todo chip
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall(0xD4, pk_robj, objId);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new EventFlagInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            return new EventFlagInformation(objId, errorCode, memory[Constants.CHIP_FR].load32(pk_robj), memory[Constants.CHIP_FR].load32(pk_robj + 4), memory[Constants.CHIP_FR].load32(pk_robj + 8));
        }
    }

    public MailboxInformation getMailboxInformation(int objId) {
        // todo chip
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall(0xC4, pk_robj, objId);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new MailboxInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            return new MailboxInformation(objId, errorCode, memory[Constants.CHIP_FR].load32(pk_robj), memory[Constants.CHIP_FR].load32(pk_robj + 4), memory[Constants.CHIP_FR].load32(pk_robj + 8));
        }
    }


    public ErrorCode setFlagIdPattern(int flagId, int pattern) {
        // todo chip
        ErrorCode errorCode;
        // Set
        errorCode = runSysCall(0xD0, flagId, pattern);
        if (errorCode == ErrorCode.E_OK) {
            // Clr
            errorCode = runSysCall(0xD1, flagId, pattern);
        }
        return errorCode;
    }

    private ErrorCode runSysCall(int syscallNumber, int r4, int r5) {
        // todo chip
        // Create alternate cpuState
        FrCPUState tmpCpuState = ((FrCPUState)cpuState[Constants.CHIP_FR]).clone();

        // Tweak alt cpuState
        tmpCpuState.I = 0; // prevent interrupts
        tmpCpuState.setILM(0, false);
        tmpCpuState.pc = BASE_ADDRESS_SYSCALL; // point to the new code

        // Set params for call
        tmpCpuState.setReg(4, r4);
        tmpCpuState.setReg(5, r5);
        tmpCpuState.setReg(12, BinaryArithmetics.signExtend(8, syscallNumber));

        emulator[Constants.CHIP_FR].setCpuState(tmpCpuState);

        // Prepare code
        memory[Constants.CHIP_FR].store16(BASE_ADDRESS_SYSCALL, 0x1F40);                      // 1F40    INT     #0x40; R12=sys_xxx_xxx(r4=R4, r5=R5)
        memory[Constants.CHIP_FR].store16(BASE_ADDRESS_SYSCALL + 2, 0xE0FF);                  // HALT, infinite loop

        // Put a breakpoint on the instruction after the call
        emulator[Constants.CHIP_FR].clearBreakConditions();
        emulator[Constants.CHIP_FR].addBreakCondition(new BreakPointCondition(BASE_ADDRESS_SYSCALL + 2, null));

        // Start emulator synchronously
        try {
            emulator[Constants.CHIP_FR].play();

            // Read error code
            return ErrorCode.fromValue(tmpCpuState.getReg(12));
        }
        catch (Throwable t) {
            t.printStackTrace();
            return ErrorCode.E_FREMU;
        }
    }


    private void pauseEmulator(int chip) {
        emulator[chip].addBreakCondition(new AlwaysBreakCondition());
        emulator[chip].exitSleepLoop();
    }

    private void stopEmulator(int chip) {
        emulator[chip].addBreakCondition(new AlwaysBreakCondition());
        emulator[chip].exitSleepLoop();
        try {
            // Wait for emulator to stop
            Thread.sleep(120);
        } catch (InterruptedException e) {
            // nop
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
}
