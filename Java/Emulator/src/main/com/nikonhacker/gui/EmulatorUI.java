package com.nikonhacker.gui;

/*
 * MDI Layout inspired by InternalFrameDemo from the Java Tutorial -
 * http://docs.oracle.com/javase/tutorial/uiswing/components/internalframe.html
 */

/* TODO : track executions in non CODE area */
/* TODO : memory viewer : add checkbox to toggle rotation, button to clear, ... */

import com.nikonhacker.ApplicationInfo;
import com.nikonhacker.Format;
import com.nikonhacker.Prefs;
import com.nikonhacker.dfr.*;
import com.nikonhacker.emu.EmulationException;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.interruptController.InterruptController;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.memory.listener.ExpeedIoListener;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.*;
import com.nikonhacker.encoding.FirmwareDecoder;
import com.nikonhacker.encoding.FirmwareEncoder;
import com.nikonhacker.encoding.FirmwareFormatException;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.FileSelectionPanel;
import com.nikonhacker.gui.component.VerticalLayout;
import com.nikonhacker.gui.component.analyse.AnalyseProgressDialog;
import com.nikonhacker.gui.component.analyse.GenerateSysSymbolsDialog;
import com.nikonhacker.gui.component.breakTrigger.BreakTriggerListFrame;
import com.nikonhacker.gui.component.callStack.CallStackFrame;
import com.nikonhacker.gui.component.codeStructure.CodeStructureFrame;
import com.nikonhacker.gui.component.cpu.CPUStateEditorFrame;
import com.nikonhacker.gui.component.disassembly.DisassemblyFrame;
import com.nikonhacker.gui.component.interruptController.InterruptControllerFrame;
import com.nikonhacker.gui.component.memoryActivity.MemoryActivityViewerFrame;
import com.nikonhacker.gui.component.memoryHexEditor.MemoryHexEditorFrame;
import com.nikonhacker.gui.component.memoryMapped.Component4006Frame;
import com.nikonhacker.gui.component.saveLoadMemory.SaveLoadMemoryDialog;
import com.nikonhacker.gui.component.screenEmulator.ScreenEmulatorFrame;
import com.nikonhacker.gui.component.sourceCode.SourceCodeFrame;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.List;

public class EmulatorUI extends JFrame implements ActionListener, ChangeListener {

    private static final String COMMAND_IMAGE_LOAD = "IMAGE_LOAD";
    private static final String COMMAND_GENERATE_SYS_SYMBOLS = "GENERATE_SYS_SYMBOLS";
    private static final String COMMAND_ANALYSE_DISASSEMBLE = "ANALYSE_DISASSEMBLE";
    private static final String COMMAND_EMULATOR_PLAY = "EMULATOR_PLAY";
    private static final String COMMAND_EMULATOR_DEBUG = "EMULATOR_DEBUG";
    private static final String COMMAND_EMULATOR_PAUSE = "EMULATOR_PAUSE";
    private static final String COMMAND_EMULATOR_STEP = "EMULATOR_STEP";
    private static final String COMMAND_EMULATOR_STOP = "EMULATOR_STOP";
    private static final String COMMAND_SETUP_BREAKPOINTS = "SETUP_BREAKPOINTS";
    private static final String COMMAND_QUIT = "QUIT";
    private static final String COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER = "TOGGLE_MEMORY_ACTIVITY_VIEWER";
    private static final String COMMAND_TOGGLE_MEMORY_HEX_EDITOR = "TOGGLE_MEMORY_HEX_EDITOR";
    private static final String COMMAND_TOGGLE_SCREEN_EMULATOR = "TOGGLE_SCREEN_EMULATOR";
    private static final String COMMAND_TOGGLE_DISASSEMBLY_WINDOW = "TOGGLE_DISASSEMBLY_WINDOW";
    private static final String COMMAND_TOGGLE_CPUSTATE_WINDOW = "TOGGLE_CPUSTATE_WINDOW";
    private static final String COMMAND_TOGGLE_COMPONENT_4006_WINDOW = "TOGGLE_COMPONENT_4006_WINDOW";
    private static final String COMMAND_DECODE = "DECODE";
    private static final String COMMAND_ENCODE = "ENCODE";
    private static final String COMMAND_SAVE_LOAD_MEMORY = "SAVE_LOAD_MEMORY";
    private static final String COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW = "TOGGLE_CODE_STRUCTURE_WINDOW";
    private static final String COMMAND_TOGGLE_SOURCE_CODE_WINDOW = "TOGGLE_SOURCE_CODE_WINDOW";
    private static final String COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW = "TOGGLE_INTERRUPT_CONTROLLER_WINDOW";
    private static final String COMMAND_TOGGLE_CLOCK_TIMER = "TOGGLE CLOCK TIMER";
    private static final String COMMAND_TOGGLE_CALL_STACK_WINDOW = "TOGGLE_CALL_STACK_WINDOW";
    private static final String COMMAND_OPTIONS = "OPTIONS";
    private static final String COMMAND_ABOUT = "ABOUT";

    private static final int BASE_ADDRESS = 0x40000; // TODO de-hardcode this

    private static final int FUNCTION_CALL_BASE_ADDRESS = 0xFFFFFFF0;

    private static File imageFile;
    private static final int CAMERA_SCREEN_MEMORY_Y = 0xCE57DC60;
    private static final int CAMERA_SCREEN_MEMORY_U = CAMERA_SCREEN_MEMORY_Y + 0x64000;
    private static final int CAMERA_SCREEN_MEMORY_V = CAMERA_SCREEN_MEMORY_Y + 2 * 0x64000;
    private static final int CAMERA_SCREEN_WIDTH = 640;
    private static final int CAMERA_SCREEN_HEIGHT = 480;

    private static final int CLOCK_INTERRUPT_NUMBER = 0x18;


    private Emulator emulator;
    private CPUState cpuState;
    private DebuggableMemory memory;
    private InterruptController interruptController;
    private java.util.Timer clockTimer;
    private java.util.Timer clockAnimationTimer;

    private CodeStructure codeStructure = null;

    private boolean isImageLoaded = false;
    private boolean isEmulatorPlaying = false;

    long lastUpdateCycles = 0;
    long lastUpdateTime = 0;
    
    private Prefs prefs = new Prefs();

    private JDesktopPane mdiPane;

    private JMenuItem loadMenuItem;
    private JMenuItem playMenuItem;
    private JMenuItem debugMenuItem;
    private JMenuItem pauseMenuItem;
    private JMenuItem stepMenuItem;
    private JMenuItem stopMenuItem;
    private JMenuItem breakpointMenuItem;
    private JMenuItem generateSysSymbolsMenuItem;
    private JMenuItem analyseMenuItem;
    private JCheckBoxMenuItem disassemblyMenuItem;
    private JCheckBoxMenuItem cpuStateMenuItem;
    private JCheckBoxMenuItem memoryActivityViewerMenuItem;
    private JCheckBoxMenuItem memoryHexEditorMenuItem;
    private JCheckBoxMenuItem screenEmulatorMenuItem;
    private JCheckBoxMenuItem component4006MenuItem;
    private JCheckBoxMenuItem codeStructureMenuItem;
    private JCheckBoxMenuItem sourceCodeMenuItem;
    private JCheckBoxMenuItem interruptControllerMenuItem;
    private JCheckBoxMenuItem clockMenuItem;
    private JCheckBoxMenuItem callStackMenuItem;
    private JMenuItem saveLoadMemoryMenuItem;
    private JMenuItem optionsMenuItem;

    private JButton loadButton;
    private JButton analyseButton;
    private JButton playButton;
    private JButton debugButton;
    private JButton pauseButton;
    private JButton stepButton;
    private JButton stopButton;
    private JButton breakpointButton;
    private JButton disassemblyButton;
    private JButton cpuStateButton;
    private JButton memoryActivityViewerButton;
    private JButton memoryHexEditorButton;
    private JButton screenEmulatorButton;
    private JButton component4006Button;
    private JButton codeStructureButton;
    private JButton sourceCodeButton;
    private JButton interruptControllerButton;
    private JButton clockButton;
    private JButton callStackButton;
    private JButton saveLoadMemoryButton;
    private JButton optionsButton;

    private DocumentFrame disassemblyLogFrame;
    private CPUStateEditorFrame cpuStateEditorFrame;
    private DocumentFrame screenEmulatorFrame;
    private MemoryActivityViewerFrame memoryActivityViewerFrame;
    private BreakTriggerListFrame breakTriggerListFrame;
    private MemoryHexEditorFrame memoryHexEditorFrame;
    private Component4006Frame component4006Frame;
    private CodeStructureFrame codeStructureFrame;
    private SourceCodeFrame sourceCodeFrame;
    private InterruptControllerFrame interruptControllerFrame;
    private CallStackFrame callStackFrame;
    private final Insets toolbarButtonMargin;
    private final JPanel toolBar;
    private JLabel statusBar;
    private String statusText = "Ready";

    private static ImageIcon[] clockIcons;
    private int clockAnimationCounter = 0;


    public static void main(String[] args) throws EmulationException, IOException, ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException {
        if (args.length > 0) {
            if (new File(args[0]).exists()) {
                imageFile = new File(args[0]);
            }
        }

        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

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

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //Make the app window indented 50 pixels from each edge of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);

        //Set up the GUI.
        mdiPane = new JDesktopPane();

        setJMenuBar(createMenuBar());

        toolbarButtonMargin = new Insets(2, 14, 2, 14);
        toolBar = createToolBar();

        statusBar = new JLabel(statusText);

        JPanel mainContentPane = new JPanel(new BorderLayout());
        mainContentPane.add(toolBar, BorderLayout.PAGE_START);
        mainContentPane.add(mdiPane, BorderLayout.CENTER);
        mainContentPane.add(statusBar, BorderLayout.SOUTH);
        setContentPane(mainContentPane);

        applyPrefsToUI();


        if (imageFile != null) {
            loadImage();
        }

        updateStates();

        //Make dragging a little faster but perhaps uglier.
        // mdiPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        // Update title bar every seconds with emulator stats
        new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateStatusBar();
            }
        }).start();
    }

    private void applyPrefsToUI() {
        if (prefs.isLargeToolbarButtons()) {
            toolbarButtonMargin.set(2, 14, 2, 14);
        }
        else {
            toolbarButtonMargin.set(0, 0, 0, 0);
        }
        toolBar.revalidate();
    }

    public Prefs getPrefs() {
        return prefs;
    }

    public void setStatusText(String message) {
        statusText = message;
        updateStatusBar();
    }

    private void updateStatusBar() {
        if (emulator != null) {
            long totalCycles = emulator.getTotalCycles();
            long now = System.currentTimeMillis();
            long cps;
            try {
                cps = (1000 * (totalCycles - lastUpdateCycles))/(now - lastUpdateTime);
            } catch (Exception e) {
                cps = -1;
            }

            lastUpdateCycles = totalCycles;
            lastUpdateTime = now;
            statusBar.setText(statusText + " (" + totalCycles + " cycles emulated. Current speed is " + (cps<0?"?":(""+cps)) + "Hz)");
        }
        else {
            statusBar.setText(statusText);
        }
    }

    private JPanel createToolBar() {
        JPanel bar = new JPanel();

        bar.setLayout(new BoxLayout(bar, BoxLayout.LINE_AXIS));

        loadButton = makeButton("load", COMMAND_IMAGE_LOAD, "Load image", "Load");
        bar.add(loadButton);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        playButton = makeButton("play", COMMAND_EMULATOR_PLAY, "Start or resume emulator", "Play");
        bar.add(playButton);
        debugButton = makeButton("debug", COMMAND_EMULATOR_DEBUG, "Debug emulator", "Debug");
        bar.add(debugButton);
        pauseButton = makeButton("pause", COMMAND_EMULATOR_PAUSE, "Pause emulator", "Pause");
        bar.add(pauseButton);
        stepButton = makeButton("step", COMMAND_EMULATOR_STEP, "Step emulator", "Step");
        bar.add(stepButton);
        stopButton = makeButton("stop", COMMAND_EMULATOR_STOP, "Stop emulator and reset", "Stop");
        bar.add(stopButton);
 
        bar.add(Box.createRigidArea(new Dimension(10, 0)));
 
        breakpointButton = makeButton("breakpoint", COMMAND_SETUP_BREAKPOINTS, "Setup breakpoints", "Breakpoints");
        bar.add(breakpointButton);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));
        bar.add(new JLabel("Sleep :"));
        bar.add(Box.createRigidArea(new Dimension(10, 0)));
        bar.add(makeSlider());
        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        cpuStateButton = makeButton("cpu", COMMAND_TOGGLE_CPUSTATE_WINDOW, "CPU State window", "CPU");
        bar.add(cpuStateButton);
        memoryHexEditorButton = makeButton("memory_editor", COMMAND_TOGGLE_MEMORY_HEX_EDITOR, "Memory hex editor", "Hex Editor");
        bar.add(memoryHexEditorButton);
        screenEmulatorButton = makeButton("screen", COMMAND_TOGGLE_SCREEN_EMULATOR, "Screen emulator", "Screen");
        bar.add(screenEmulatorButton);
        interruptControllerButton = makeButton("interrupt", COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW, "Interrupt controller", "Interrupt");
        bar.add(interruptControllerButton);
        clockButton = makeButton("clock", COMMAND_TOGGLE_CLOCK_TIMER, "Toggle clock timer", "Clock");
        bar.add(clockButton);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        disassemblyButton = makeButton("disassembly_log", COMMAND_TOGGLE_DISASSEMBLY_WINDOW, "Real time disassembly log", "Disassembly");
        bar.add(disassemblyButton);
        memoryActivityViewerButton = makeButton("memory_activity", COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER, "Memory activity viewer", "Activity");
        bar.add(memoryActivityViewerButton);
        component4006Button = makeButton("4006", COMMAND_TOGGLE_COMPONENT_4006_WINDOW, "Component 4006", "Component 4006");
        bar.add(component4006Button);
        callStackButton = makeButton("call_stack", COMMAND_TOGGLE_CALL_STACK_WINDOW, "Call Stack window", "CallStack");
        bar.add(callStackButton);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        analyseButton = makeButton("analyse", COMMAND_ANALYSE_DISASSEMBLE, "Analyse/Disassemble", "Analyse");
        bar.add(analyseButton);
        codeStructureButton = makeButton("code_structure", COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW, "Code Structure", "Structure");
        bar.add(codeStructureButton);
        sourceCodeButton = makeButton("source", COMMAND_TOGGLE_SOURCE_CODE_WINDOW, "Source code", "Source");
        bar.add(sourceCodeButton);

        bar.add(Box.createHorizontalGlue());

        saveLoadMemoryButton = makeButton("save_load_memory", COMMAND_SAVE_LOAD_MEMORY, "Save/Load memory area", "Save/Load memory");
        bar.add(saveLoadMemoryButton);

        bar.add(Box.createRigidArea(new Dimension(10, 0)));

        optionsButton = makeButton("options", COMMAND_OPTIONS, "Options", "Options");
        bar.add(optionsButton);

        return bar;
    }

    private JSlider makeSlider() {
        JSlider intervalSlider = new JSlider(JSlider.HORIZONTAL, 0, 5, prefs.getSleepTick());

        intervalSlider.addChangeListener(this);

        Font font = new Font("Serif", Font.PLAIN, 10);
        intervalSlider.setFont(font);

        //Create the label table
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(0, new JLabel("0") );
        labelTable.put(1, new JLabel("1ms"));
        labelTable.put(2, new JLabel("10ms"));
        labelTable.put(3, new JLabel("0.1s"));
        labelTable.put(4, new JLabel("1s"));
        labelTable.put(5, new JLabel("10s"));
        intervalSlider.setLabelTable(labelTable);

        //Turn on labels at major tick marks.
        intervalSlider.setMajorTickSpacing(1);
        intervalSlider.setPaintLabels(true);
        intervalSlider.setPaintTicks(true);
        intervalSlider.setSnapToTicks(true);

        intervalSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        intervalSlider.setMaximumSize(new Dimension(400, 50));

        return intervalSlider;
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
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {
            button.setText(altText);
            System.err.println("Resource not found: " + imgLocation);
        }

        return button;
    }


    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenuItem tmpMenuItem;

        //Set up the file menu.
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        //load image
        loadMenuItem = new JMenuItem("Load firmware image");
        loadMenuItem.setMnemonic(KeyEvent.VK_L);
        loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
        loadMenuItem.setActionCommand(COMMAND_IMAGE_LOAD);
        loadMenuItem.addActionListener(this);
        fileMenu.add(loadMenuItem);

        fileMenu.add(new JSeparator());

        //decoder
        tmpMenuItem = new JMenuItem("Decode firmware");
        tmpMenuItem.setMnemonic(KeyEvent.VK_D);
//        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        tmpMenuItem.setActionCommand(COMMAND_DECODE);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);

        //encoder
        tmpMenuItem = new JMenuItem("Encode firmware (alpha)");
        tmpMenuItem.setMnemonic(KeyEvent.VK_E);
//        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
        tmpMenuItem.setActionCommand(COMMAND_ENCODE);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);

        fileMenu.add(new JSeparator());

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


        //emulator play
        playMenuItem = new JMenuItem("Start (or resume) emulator");
        playMenuItem.setMnemonic(KeyEvent.VK_E);
        playMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.CTRL_MASK));
        playMenuItem.setActionCommand(COMMAND_EMULATOR_PLAY);
        playMenuItem.addActionListener(this);
        runMenu.add(playMenuItem);

        //emulator debug
        debugMenuItem = new JMenuItem("Debug emulator");
        debugMenuItem.setMnemonic(KeyEvent.VK_G);
        debugMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        debugMenuItem.setActionCommand(COMMAND_EMULATOR_DEBUG);
        debugMenuItem.addActionListener(this);
        runMenu.add(debugMenuItem);

        //emulator pause
        pauseMenuItem = new JMenuItem("Pause emulator");
        pauseMenuItem.setMnemonic(KeyEvent.VK_P);
        pauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.ALT_MASK));
        pauseMenuItem.setActionCommand(COMMAND_EMULATOR_PAUSE);
        pauseMenuItem.addActionListener(this);
        runMenu.add(pauseMenuItem);

        //emulator step
        stepMenuItem = new JMenuItem("Step emulator");
        stepMenuItem.setMnemonic(KeyEvent.VK_T);
        stepMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        stepMenuItem.setActionCommand(COMMAND_EMULATOR_STEP);
        stepMenuItem.addActionListener(this);
        runMenu.add(stepMenuItem);

        //emulator stop
        stopMenuItem = new JMenuItem("Stop and reset emulator");
        stopMenuItem.setMnemonic(KeyEvent.VK_R);
        playMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.SHIFT_MASK));
        stopMenuItem.setActionCommand(COMMAND_EMULATOR_STOP);
        stopMenuItem.addActionListener(this);
        runMenu.add(stopMenuItem);

        runMenu.add(new JSeparator());

        //setup breakpoints
        breakpointMenuItem = new JMenuItem("Setup breakpoints");
        breakpointMenuItem.setMnemonic(KeyEvent.VK_B);
        breakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));
        breakpointMenuItem.setActionCommand(COMMAND_SETUP_BREAKPOINTS);
        breakpointMenuItem.addActionListener(this);
        runMenu.add(breakpointMenuItem);


        //Set up the components menu.
        JMenu componentsMenu = new JMenu("Components");
        componentsMenu.setMnemonic(KeyEvent.VK_O);
        menuBar.add(componentsMenu);

        //CPU state
        cpuStateMenuItem = new JCheckBoxMenuItem("CPU State window");
        cpuStateMenuItem.setMnemonic(KeyEvent.VK_C);
        cpuStateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
        cpuStateMenuItem.setActionCommand(COMMAND_TOGGLE_CPUSTATE_WINDOW);
        cpuStateMenuItem.addActionListener(this);
        componentsMenu.add(cpuStateMenuItem);

        //memory hex editor
        memoryHexEditorMenuItem = new JCheckBoxMenuItem("Memory hex editor");
        memoryHexEditorMenuItem.setMnemonic(KeyEvent.VK_H);
        memoryHexEditorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
        memoryHexEditorMenuItem.setActionCommand(COMMAND_TOGGLE_MEMORY_HEX_EDITOR);
        memoryHexEditorMenuItem.addActionListener(this);
        componentsMenu.add(memoryHexEditorMenuItem);

        //screen emulator
        screenEmulatorMenuItem = new JCheckBoxMenuItem("Screen emulator");
        screenEmulatorMenuItem.setMnemonic(KeyEvent.VK_S);
        screenEmulatorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        screenEmulatorMenuItem.setActionCommand(COMMAND_TOGGLE_SCREEN_EMULATOR);
        screenEmulatorMenuItem.addActionListener(this);
        componentsMenu.add(screenEmulatorMenuItem);

        //Interrupt controller
        interruptControllerMenuItem = new JCheckBoxMenuItem("Interrupt controller");
        interruptControllerMenuItem.setMnemonic(KeyEvent.VK_I);
        interruptControllerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
        interruptControllerMenuItem.setActionCommand(COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW);
        interruptControllerMenuItem.addActionListener(this);
        componentsMenu.add(interruptControllerMenuItem);

        //Timer interrupt
        clockMenuItem = new JCheckBoxMenuItem("Timer interrupt");
//        clockMenuItem.setMnemonic(KeyEvent.VK_T);
//        clockMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
        clockMenuItem.setActionCommand(COMMAND_TOGGLE_CLOCK_TIMER);
        clockMenuItem.addActionListener(this);
        componentsMenu.add(clockMenuItem);


        //Set up the trace menu.
        JMenu traceMenu = new JMenu("Trace");
        traceMenu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(traceMenu);

        //disassembly
        disassemblyMenuItem = new JCheckBoxMenuItem("Real-time disassembly log");
        disassemblyMenuItem.setMnemonic(KeyEvent.VK_D);
        disassemblyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        disassemblyMenuItem.setActionCommand(COMMAND_TOGGLE_DISASSEMBLY_WINDOW);
        disassemblyMenuItem.addActionListener(this);
        traceMenu.add(disassemblyMenuItem);

        //memory activity viewer
        memoryActivityViewerMenuItem = new JCheckBoxMenuItem("Memory activity viewer");
        memoryActivityViewerMenuItem.setMnemonic(KeyEvent.VK_M);
        memoryActivityViewerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
        memoryActivityViewerMenuItem.setActionCommand(COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER);
        memoryActivityViewerMenuItem.addActionListener(this);
        traceMenu.add(memoryActivityViewerMenuItem);

        //Component 4006
        component4006MenuItem = new JCheckBoxMenuItem("Component 4006 window");
        component4006MenuItem.setMnemonic(KeyEvent.VK_4);
        component4006MenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
        component4006MenuItem.setActionCommand(COMMAND_TOGGLE_COMPONENT_4006_WINDOW);
        component4006MenuItem.addActionListener(this);
        traceMenu.add(component4006MenuItem);

        //Call Stack
        callStackMenuItem = new JCheckBoxMenuItem("Call stack");
//        callStackMenuItem.setMnemonic(KeyEvent.VK_C);
//        callStackMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
        callStackMenuItem.setActionCommand(COMMAND_TOGGLE_CALL_STACK_WINDOW);
        callStackMenuItem.addActionListener(this);
        traceMenu.add(callStackMenuItem);


        //Set up the tools menu.
        JMenu sourceMenu = new JMenu("Source");
        sourceMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(sourceMenu);

        //analyse / disassemble
        generateSysSymbolsMenuItem = new JMenuItem("Generate system call symbols");
//        generateSysSymbolsMenuItem.setMnemonic(KeyEvent.VK_A);
//        generateSysSymbolsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        generateSysSymbolsMenuItem.setActionCommand(COMMAND_GENERATE_SYS_SYMBOLS);
        generateSysSymbolsMenuItem.addActionListener(this);
        sourceMenu.add(generateSysSymbolsMenuItem);

        sourceMenu.add(new JSeparator());

        //analyse / disassemble
        analyseMenuItem = new JMenuItem("Analyse / Disassemble");
        analyseMenuItem.setMnemonic(KeyEvent.VK_A);
        analyseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        analyseMenuItem.setActionCommand(COMMAND_ANALYSE_DISASSEMBLE);
        analyseMenuItem.addActionListener(this);
        sourceMenu.add(analyseMenuItem);

        sourceMenu.add(new JSeparator());

        //code structure
        codeStructureMenuItem = new JCheckBoxMenuItem("Code structure");
        codeStructureMenuItem.setMnemonic(KeyEvent.VK_C);
//        codeStructureMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        codeStructureMenuItem.setActionCommand(COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW);
        codeStructureMenuItem.addActionListener(this);
        sourceMenu.add(codeStructureMenuItem);

        //source code
        sourceCodeMenuItem = new JCheckBoxMenuItem("Source code");
        sourceCodeMenuItem.setMnemonic(KeyEvent.VK_S);
//        sourceCodeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        sourceCodeMenuItem.setActionCommand(COMMAND_TOGGLE_SOURCE_CODE_WINDOW);
        sourceCodeMenuItem.addActionListener(this);
        sourceMenu.add(sourceCodeMenuItem);


        //Set up the tools menu.
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(toolsMenu);

        // save/load memory area
        saveLoadMemoryMenuItem = new JMenuItem("Save/Load memory area");
        saveLoadMemoryMenuItem.setMnemonic(KeyEvent.VK_S);
//        saveLoadMemoryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        saveLoadMemoryMenuItem.setActionCommand(COMMAND_SAVE_LOAD_MEMORY);
        saveLoadMemoryMenuItem.addActionListener(this);
        toolsMenu.add(saveLoadMemoryMenuItem);

        toolsMenu.add(new JSeparator());

        //options
        optionsMenuItem = new JMenuItem("Options");
        optionsMenuItem.setMnemonic(KeyEvent.VK_O);
        optionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        optionsMenuItem.setActionCommand(COMMAND_OPTIONS);
        optionsMenuItem.addActionListener(this);
        toolsMenu.add(optionsMenuItem);

        //Set up the help menu.
        JMenu helpMenu = new JMenu("?");
        menuBar.add(helpMenu);

        //about
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setActionCommand(COMMAND_ABOUT);
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);

        return menuBar;
    }

    // Event listeners

    /**
     * React to menu selections and toggle buttons.
     *
     * @param e the event
     */
    public void actionPerformed(ActionEvent e) {
        if (COMMAND_IMAGE_LOAD.equals(e.getActionCommand())) {
            openLoadImageDialog();
        }
        else if (COMMAND_GENERATE_SYS_SYMBOLS.equals(e.getActionCommand())) {
            openGenerateSysSymbolsDialog();
        }
        else if (COMMAND_ANALYSE_DISASSEMBLE.equals(e.getActionCommand())) {
            openAnalyseDialog();
        }
        else if (COMMAND_EMULATOR_PLAY.equals(e.getActionCommand())) {
            playEmulator(false, false, null);
        }
        else if (COMMAND_EMULATOR_DEBUG.equals(e.getActionCommand())) {
            playEmulator(false, true, null);
        }
        else if (COMMAND_EMULATOR_PAUSE.equals(e.getActionCommand())) {
            pauseEmulator();
        }
        else if (COMMAND_EMULATOR_STEP.equals(e.getActionCommand())) {
            playEmulator(true, false, null);
        }
        else if (COMMAND_EMULATOR_STOP.equals(e.getActionCommand())) {
            stopEmulator();
        }
        else if (COMMAND_SETUP_BREAKPOINTS.equals(e.getActionCommand())) {
            toggleBreakTriggerList();
        }
        else if (COMMAND_QUIT.equals(e.getActionCommand())) {
            quit();
        }
        else if (COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER.equals(e.getActionCommand())) {
            toggleMemoryActivityViewer();
        }
        else if (COMMAND_TOGGLE_MEMORY_HEX_EDITOR.equals(e.getActionCommand())) {
            toggleMemoryHexEditor();
        }
        else if (COMMAND_TOGGLE_SCREEN_EMULATOR.equals(e.getActionCommand())) {
            toggleScreenEmulator();
        }
        else if (COMMAND_TOGGLE_DISASSEMBLY_WINDOW.equals(e.getActionCommand())) {
            toggleDisassemblyLog();
        }
        else if (COMMAND_TOGGLE_CPUSTATE_WINDOW.equals(e.getActionCommand())) {
            toggleCPUState();
        }
        else if (COMMAND_TOGGLE_COMPONENT_4006_WINDOW.equals(e.getActionCommand())) {
            toggleComponent4006();
        }
        else if (COMMAND_TOGGLE_INTERRUPT_CONTROLLER_WINDOW.equals(e.getActionCommand())) {
            toggleInterruptController();
        }
        else if (COMMAND_TOGGLE_CLOCK_TIMER.equals(e.getActionCommand())) {
            toggleClockTimer();
        }
        else if (COMMAND_TOGGLE_CALL_STACK_WINDOW.equals(e.getActionCommand())) {
            toggleCallStack();
        }
        else if (COMMAND_DECODE.equals(e.getActionCommand())) {
            openDecodeDialog();
        }
        else if (COMMAND_ENCODE.equals(e.getActionCommand())) {
            openEncodeDialog();
        }
        else if (COMMAND_SAVE_LOAD_MEMORY.equals(e.getActionCommand())) {
            openSaveLoadMemoryDialog();
        }
        else if (COMMAND_TOGGLE_CODE_STRUCTURE_WINDOW.equals(e.getActionCommand())) {
            toggleCodeStructureWindow();
        }
        else if (COMMAND_TOGGLE_SOURCE_CODE_WINDOW.equals(e.getActionCommand())) {
            toggleSourceCodeWindow();
        }
        else if (COMMAND_OPTIONS.equals(e.getActionCommand())) {
            openOptionsDialog();
        }
        else if (COMMAND_ABOUT.equals(e.getActionCommand())) {
            showAboutDialog();
        }
        else {
            System.err.println("Unknown menu command : " + e.getActionCommand());
        }
    }

    private void openSaveLoadMemoryDialog() {
        new SaveLoadMemoryDialog(this, memory).setVisible(true);
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

    private void openGenerateSysSymbolsDialog() {
        GenerateSysSymbolsDialog generateSysSymbolsDialog = new GenerateSysSymbolsDialog(this, memory);
        generateSysSymbolsDialog.startGeneration();
        generateSysSymbolsDialog.setVisible(true);
    }


    private void openAnalyseDialog() {
        JTextField dfrField = new JTextField();
        JTextField destinationField = new JTextField();

        // compute and try default names for options file.
        // In order : <firmware>.dfr.txt , <firmware>.txt , dfr.txt
        File optionsFile = new File(imageFile.getParentFile(), FilenameUtils.getBaseName(imageFile.getAbsolutePath()) + ".dfr.txt");
        if (!optionsFile.exists()) {
            optionsFile = new File(imageFile.getParentFile(), FilenameUtils.getBaseName(imageFile.getAbsolutePath()) + ".txt");
            if (!optionsFile.exists()) {
                optionsFile = new File(imageFile.getParentFile(), "dfr.txt");
                if (!optionsFile.exists()) {
                    optionsFile = null;
                }
            }
        }
        if (optionsFile != null) {
            dfrField.setText(optionsFile.getAbsolutePath());
        }

        // compute default name for output
        File outputFile = new File(imageFile.getParentFile(), FilenameUtils.getBaseName(imageFile.getAbsolutePath()) + ".asm");
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
                new FileSelectionPanel("Dfr options file", dfrField, false),
                writeOutputCheckbox,
                destinationFileSelectionPanel,
                makeOutputOptionCheckBox(OutputOption.STRUCTURE, prefs.getOutputOptions(), true),
                makeOutputOptionCheckBox(OutputOption.ORDINAL, prefs.getOutputOptions(), true),
                makeOutputOptionCheckBox(OutputOption.PARAMETERS, prefs.getOutputOptions(), true),
                makeOutputOptionCheckBox(OutputOption.INT40, prefs.getOutputOptions(), true),
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
                AnalyseProgressDialog analyseProgressDialog = new AnalyseProgressDialog(this, memory);
                analyseProgressDialog.startBackgroundAnalysis(dfrField.getText(), imageFile.getAbsolutePath(), outputFilename);
                analyseProgressDialog.setVisible(true);
            }
        }
    }


    private void openLoadImageDialog() {
        final JFileChooser fc = new JFileChooser();

        fc.setCurrentDirectory(new File("."));

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            imageFile = fc.getSelectedFile();
            // Scratch any analysis that was previously done
            codeStructure = null;
            loadImage();
        }
    }

    private void openOptionsDialog() {
        JTabbedPane optionsTabbedPane = new JTabbedPane();

        // Output options panel
        JPanel outputOptionsPanel = new JPanel(new VerticalLayout(5, VerticalLayout.LEFT));
        outputOptionsPanel.setName("Disassembler output");

        // Prepare sample code area
        final RSyntaxTextArea listingArea = new RSyntaxTextArea(15, 90);
        SourceCodeFrame.prepareAreaFormat(listingArea);

        final List<JCheckBox> outputOptionsCheckBoxes = new ArrayList<JCheckBox>();
        ActionListener areaRefresherListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Set<OutputOption> sampleOptions = EnumSet.noneOf(OutputOption.class);
                    dumpOptionCheckboxes(outputOptionsCheckBoxes, sampleOptions);
                    int baseAddress = 0x40000;
                    int lastAddress = baseAddress;
                    Memory sampleMemory = new DebuggableMemory();
                    sampleMemory.map(baseAddress, 0x100, true, true, true);
                    CPUState sampleCpuState = new CPUState();
                    sampleCpuState.setAllRegistersDefined();


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

                    StringWriter writer = new StringWriter();

                    Dfr disassembler = new Dfr();
                    disassembler.setDebugPrintWriter(new PrintWriter(new StringWriter())); // Ignore
                    disassembler.setOutputFileName(null);
                    disassembler.processOptions(new String[]{"-m", "" + baseAddress + "-" + lastAddress + "=CODE"});
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
        for (OutputOption outputOption : OutputOption.formatOptions) {
            JCheckBox checkBox = makeOutputOptionCheckBox(outputOption, prefs.getOutputOptions(), false);
            outputOptionsCheckBoxes.add(checkBox);
            outputOptionsPanel.add(checkBox);
            checkBox.addActionListener(areaRefresherListener);
        }

        // Force a refresh
        areaRefresherListener.actionPerformed(new ActionEvent(outputOptionsCheckBoxes.get(0), 0, ""));

        outputOptionsPanel.add(new JLabel("Sample output:"));
        outputOptionsPanel.add(new JScrollPane(listingArea));
        outputOptionsPanel.add(new JLabel("Tip: hover over the option checkboxes for help"));

        optionsTabbedPane.add(outputOptionsPanel);

        // UI options panel
        JPanel uiOptionsPanel = new JPanel(new GridLayout(0,1));
        uiOptionsPanel.setName("User Interface");
        final JCheckBox largeButtonsCheckBox = new JCheckBox("Use large buttons");
        //largeButtonsCheckBox.setToolTipText();
        largeButtonsCheckBox.setSelected(prefs.isLargeToolbarButtons());
        uiOptionsPanel.add(largeButtonsCheckBox);
        optionsTabbedPane.add(uiOptionsPanel);

        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
                optionsTabbedPane,
                "Options",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                JOptionPane.DEFAULT_OPTION)) 
        {
            // save
            dumpOptionCheckboxes(outputOptionsCheckBoxes, prefs.getOutputOptions());
            prefs.setLargeToolbarButtons(largeButtonsCheckBox.isSelected());
            applyPrefsToUI();
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
     * 
     * @param option one of the defined options 
     * @param outputOptions the options list to initialize field from (and to which change will be written if reflectChange is true)
     * @param reflectChange if true, changing the checkbox immediately changes the option in the given outputOptions
     * @return
     */
    private JCheckBox makeOutputOptionCheckBox(final OutputOption option, Set<OutputOption> outputOptions, boolean reflectChange) {
        final JCheckBox checkBox = new JCheckBox(option.getKey());
        checkBox.setToolTipText(option.getHelp());
        checkBox.setSelected(outputOptions.contains(option));
        if (reflectChange) {
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    prefs.setOutputOption(option, checkBox.isSelected());
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
        StringBuilder style = new StringBuilder("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");

        // html content
        JEditorPane editorPane = new JEditorPane("text/html", "<html><body style=\"" + style + "\">"
                + "<font size=\"+1\">" + ApplicationInfo.getName() + " v" + ApplicationInfo.getVersion() + "</font><br/>"
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
                + "<li>Samples from the Java Tutorial (c) Sun Microsystems / Oracle - " + makeLink("http://docs.oracle.com/javase/tutorial") + "</li>"
                + "</ul></body></html>");

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
        editorPane.setBackground(label.getBackground());

        // show
        JOptionPane.showMessageDialog(this, editorPane, "About", JOptionPane.PLAIN_MESSAGE);
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
        setEmulatorSleepCode(source.getValue());
        prefs.setSleepTick(source.getValue());
    }

    private void setEmulatorSleepCode(int value) {
        emulator.exitSleepLoop();
        switch (value) {
            case 0:
                emulator.setSleepIntervalMs(0);
                break;
            case 1:
                emulator.setSleepIntervalMs(1);
                break;
            case 2:
                emulator.setSleepIntervalMs(10);
                break;
            case 3:
                emulator.setSleepIntervalMs(100);
                break;
            case 4:
                emulator.setSleepIntervalMs(1000);
                break;
            case 5:
                emulator.setSleepIntervalMs(10000);
                break;
        }
    }


    private void loadImage() {
        try {
            memory = new DebuggableMemory();
            memory.loadFile(imageFile, BASE_ADDRESS);

            cpuState = new CPUState(BASE_ADDRESS);

            emulator = new Emulator();
            emulator.setMemory(memory);
            emulator.setCpuState(cpuState);

            interruptController = new InterruptController(memory);

            emulator.setInterruptController(interruptController);

            memory.addIoActivityListener(new ExpeedIoListener(cpuState, interruptController));

            setEmulatorSleepCode(prefs.getSleepTick());

            isImageLoaded = true;
            closeAllFrames();

            updateStates();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeAllFrames() {
        if (breakTriggerListFrame != null) {
            breakTriggerListFrame.dispose();
            breakTriggerListFrame = null;
        }

        if (disassemblyLogFrame != null) {
            disassemblyLogFrame.dispose();
            disassemblyLogFrame = null;
        }
        if (cpuStateEditorFrame != null) {
            cpuStateEditorFrame.dispose();
            cpuStateEditorFrame = null;
        }
        if (screenEmulatorFrame != null) {
            screenEmulatorFrame.dispose();
            screenEmulatorFrame = null;
        }
        if (memoryActivityViewerFrame != null) {
            memoryActivityViewerFrame.dispose();
            memoryActivityViewerFrame = null;
        }
        if (memoryHexEditorFrame != null) {
            memoryHexEditorFrame.dispose();
            memoryHexEditorFrame = null;
        }
        if (component4006Frame != null) {
            component4006Frame.dispose();
            component4006Frame = null;
        }
        if (interruptControllerFrame != null) {
            interruptControllerFrame.dispose();
            interruptControllerFrame = null;
        }
        if (callStackFrame != null) {
            callStackFrame.dispose();
            callStackFrame = null;
        }

        if (codeStructureFrame != null) {
            codeStructureFrame.dispose();
            codeStructureFrame = null;
        }
        if (sourceCodeFrame != null) {
            sourceCodeFrame.dispose();
            sourceCodeFrame = null;
        }

    }

    private void toggleBreakTriggerList() {
        if (breakTriggerListFrame == null) {
            breakTriggerListFrame = new BreakTriggerListFrame("Setup breakpoints and triggers", true, true, true, true, prefs.getTriggers(), this);
            addDocumentFrame(breakTriggerListFrame);
            breakTriggerListFrame.display(true);
        }
        else {
            breakTriggerListFrame.dispose();
            breakTriggerListFrame = null;
        }
        updateStates();
    }

    private void toggleDisassemblyLog() {
        if (disassemblyLogFrame == null) {
            disassemblyLogFrame = new DisassemblyFrame("Real-time disassembly log", true, true, true, true, emulator, this);
            addDocumentFrame(disassemblyLogFrame);
            disassemblyLogFrame.display(true);
        }
        else {
            disassemblyLogFrame.dispose();
            disassemblyLogFrame = null;
        }
        updateStates();
    }

    private void toggleCPUState() {
        if (cpuStateEditorFrame == null) {
            cpuStateEditorFrame = new CPUStateEditorFrame("CPU State", false, true, false, true, cpuState, this);
            cpuStateEditorFrame.setEnabled(!isEmulatorPlaying);
            addDocumentFrame(cpuStateEditorFrame);
            cpuStateEditorFrame.display(true);
        }
        else {
            cpuStateEditorFrame.dispose();
            cpuStateEditorFrame = null;
        }
        updateStates();
    }

    private void toggleMemoryActivityViewer() {
        if (memoryActivityViewerFrame == null) {
            memoryActivityViewerFrame = new MemoryActivityViewerFrame("Base memory activity viewer (each cell=64k, click to zoom)", true, true, true, true, memory, this);
            addDocumentFrame(memoryActivityViewerFrame);
            memoryActivityViewerFrame.display(true);
        }
        else {
            memoryActivityViewerFrame.dispose();
            memoryActivityViewerFrame = null;
        }
        updateStates();
    }

    private void toggleMemoryHexEditor() {
        if (memoryHexEditorFrame == null) {
            memoryHexEditorFrame = new MemoryHexEditorFrame("Memory hex editor", true, true, true, true, memory, cpuState, 0, !isEmulatorPlaying, this);
            addDocumentFrame(memoryHexEditorFrame);
            memoryHexEditorFrame.display(true);
        }
        else {
            memoryHexEditorFrame.dispose();
            memoryHexEditorFrame = null;
        }
        updateStates();
    }

    private void toggleScreenEmulator() {
        if (screenEmulatorFrame == null) {
            screenEmulatorFrame = new ScreenEmulatorFrame("Screen emulator", true, true, true, true, memory, CAMERA_SCREEN_MEMORY_Y, CAMERA_SCREEN_MEMORY_U, CAMERA_SCREEN_MEMORY_V, CAMERA_SCREEN_WIDTH, CAMERA_SCREEN_HEIGHT, this);
            addDocumentFrame(screenEmulatorFrame);
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
            component4006Frame = new Component4006Frame("Component 4006", true, true, false, true, memory, 0x4006, this);
            addDocumentFrame(component4006Frame);
            component4006Frame.display(true);
        }
        else {
            component4006Frame.dispose();
            component4006Frame = null;
        }
        updateStates();
    }


    private void toggleInterruptController() {
        if (interruptControllerFrame == null) {
            interruptControllerFrame = new InterruptControllerFrame("Interrupt controller", true, true, false, true, interruptController, memory, this);
            addDocumentFrame(interruptControllerFrame);
            interruptControllerFrame.display(true);
        }
        else {
            interruptControllerFrame.dispose();
            interruptControllerFrame = null;
        }
        updateStates();
    }

    private void toggleClockTimer() {
        if (clockTimer == null) {
            clockTimer = new java.util.Timer(false);
            clockTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    interruptController.request(CLOCK_INTERRUPT_NUMBER);
                }
            }, 0, 1 /*ms*/);
            setStatusText("Interrupt 0x" + Format.asHex(CLOCK_INTERRUPT_NUMBER, 2) + " will be requested every millisecond");

            clockAnimationTimer = new java.util.Timer(false);
            clockAnimationTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    clockAnimationCounter++;
                    if (clockAnimationCounter == clockIcons.length) {
                        clockAnimationCounter = 1;
                    }
                    clockButton.setIcon(clockIcons[clockAnimationCounter]);
                }
            }, 0, 300 /*ms*/);
        }
        else {
            clockTimer.cancel();
            clockTimer = null;
            clockAnimationTimer.cancel();
            clockAnimationTimer = null;
            clockButton.setIcon(clockIcons[0]);
        }
    }

    static {
        clockIcons = new ImageIcon[25];
        for (int i = 0; i < clockIcons.length; i++) {
            String imgLocation = "images/clock";
            String text;
            if (i == 0) {
                text = "Start timer";
            }
            else {
                imgLocation += "_" + i;
                text = "Stop timer";
            }
            clockIcons[i] = new ImageIcon(EmulatorUI.class.getResource(imgLocation + ".png"), text);
        }
    }

    private void toggleCallStack() {
        if (callStackFrame == null) {
            callStackFrame = new CallStackFrame("Call Stack", true, true, false, true, emulator, cpuState, codeStructure, this);
            callStackFrame.setAutoRefresh(!isEmulatorPlaying);
            addDocumentFrame(callStackFrame);
            callStackFrame.display(true);
        }
        else {
            callStackFrame.dispose();
            callStackFrame = null;
        }
        updateStates();
    }


    private void toggleCodeStructureWindow() {
        if (codeStructureFrame == null) {
            codeStructureFrame = new CodeStructureFrame("Code structure", true, true, true, true, cpuState, codeStructure, this);
            addDocumentFrame(codeStructureFrame);
            codeStructureFrame.display(true);
        }
        else {
            codeStructureFrame.dispose();
            codeStructureFrame = null;
        }
        updateStates();
    }

    private void toggleSourceCodeWindow() {
        if (sourceCodeFrame == null) {
            sourceCodeFrame = new SourceCodeFrame("Source code", true, true, true, true, cpuState, codeStructure, this);
            addDocumentFrame(sourceCodeFrame);
            sourceCodeFrame.display(true);
        }
        else {
            sourceCodeFrame.dispose();
            sourceCodeFrame = null;
        }
        updateStates();
    }


    public void addDocumentFrame(DocumentFrame frame) {
        mdiPane.add(frame);
    }


    /**
     * Called back by frames to inform UI that they are being closed
     * @param frame
     */
    public void frameClosing(DocumentFrame frame) {
        if (frame == breakTriggerListFrame) {
            toggleBreakTriggerList();
        }
        else if (frame == disassemblyLogFrame) {
            toggleDisassemblyLog();
        }
        else if (frame == cpuStateEditorFrame) {
            toggleCPUState();
        }
        else if (frame == screenEmulatorFrame) {
            toggleScreenEmulator();
        }
        else if (frame == memoryActivityViewerFrame) {
            toggleMemoryActivityViewer();
        }
        else if (frame == memoryHexEditorFrame) {
            toggleMemoryHexEditor();
        }
        else if (frame == component4006Frame) {
            toggleComponent4006();
        }
        else if (frame == interruptControllerFrame) {
            toggleInterruptController();
        }
        else if (frame == callStackFrame) {
            toggleCallStack();
        }
        else if (frame == codeStructureFrame) {
            toggleCodeStructureWindow();
        }
        else if (frame == sourceCodeFrame) {
            toggleSourceCodeWindow();
        }
        else {
            System.err.println("EmulatorUI.frameClosing : Unknown frame is being closed. Please add handler for " + frame.getClass().getSimpleName());
        }
    }

    public void updateStates() {
        // CheckboxMenuItem checked or not
        disassemblyMenuItem.setSelected(disassemblyLogFrame != null);
        cpuStateMenuItem.setSelected(cpuStateEditorFrame != null);
        screenEmulatorMenuItem.setSelected(screenEmulatorFrame != null);
        memoryActivityViewerMenuItem.setSelected(memoryActivityViewerFrame != null);
        memoryHexEditorMenuItem.setSelected(memoryHexEditorFrame != null);
        component4006MenuItem.setSelected(component4006Frame != null);
        interruptControllerMenuItem.setSelected(interruptControllerFrame != null);
        clockMenuItem.setSelected(clockTimer != null);

        codeStructureMenuItem.setSelected(codeStructureFrame != null);
        sourceCodeMenuItem.setSelected(sourceCodeFrame != null);

        // Menus and buttons enabled or not
        codeStructureMenuItem.setEnabled(codeStructure != null); codeStructureButton.setEnabled(codeStructure != null);
        sourceCodeMenuItem.setEnabled(codeStructure != null); sourceCodeButton.setEnabled(codeStructure != null);

        generateSysSymbolsMenuItem.setEnabled(isImageLoaded);
        analyseMenuItem.setEnabled(isImageLoaded); analyseButton.setEnabled(isImageLoaded);

        disassemblyMenuItem.setEnabled(isImageLoaded); disassemblyButton.setEnabled(isImageLoaded);
        cpuStateMenuItem.setEnabled(isImageLoaded); cpuStateButton.setEnabled(isImageLoaded);
        screenEmulatorMenuItem.setEnabled(isImageLoaded); screenEmulatorButton.setEnabled(isImageLoaded);
        memoryActivityViewerMenuItem.setEnabled(isImageLoaded); memoryActivityViewerButton.setEnabled(isImageLoaded);
        memoryHexEditorMenuItem.setEnabled(isImageLoaded); memoryHexEditorButton.setEnabled(isImageLoaded);
        component4006MenuItem.setEnabled(isImageLoaded); component4006Button.setEnabled(isImageLoaded);
        interruptControllerMenuItem.setEnabled(isImageLoaded); interruptControllerButton.setEnabled(isImageLoaded);
        clockMenuItem.setEnabled(isImageLoaded); clockButton.setEnabled(isImageLoaded);
        callStackMenuItem.setEnabled(isImageLoaded); callStackButton.setEnabled(isImageLoaded);

        saveLoadMemoryMenuItem.setEnabled(isImageLoaded); saveLoadMemoryButton.setEnabled(isImageLoaded);

        stopMenuItem.setEnabled(isImageLoaded); stopButton.setEnabled(isImageLoaded);

        if (isImageLoaded) {
            // Depends whether emulator is playing or not
            loadMenuItem.setEnabled(!isEmulatorPlaying); loadButton.setEnabled(!isEmulatorPlaying);
            playMenuItem.setEnabled(!isEmulatorPlaying); playButton.setEnabled(!isEmulatorPlaying);
            debugMenuItem.setEnabled(!isEmulatorPlaying); debugButton.setEnabled(!isEmulatorPlaying);
            pauseMenuItem.setEnabled(isEmulatorPlaying); pauseButton.setEnabled(isEmulatorPlaying);
            stepMenuItem.setEnabled(!isEmulatorPlaying); stepButton.setEnabled(!isEmulatorPlaying);
            breakpointMenuItem.setEnabled(!isEmulatorPlaying); breakpointButton.setEnabled(!isEmulatorPlaying);
            optionsMenuItem.setEnabled(!isEmulatorPlaying); optionsButton.setEnabled(!isEmulatorPlaying);
            // Editable components
            if (memoryHexEditorFrame != null) memoryHexEditorFrame.setEditable(!isEmulatorPlaying);
            if (cpuStateEditorFrame != null) cpuStateEditorFrame.setEditable(!isEmulatorPlaying);
            if (callStackFrame != null) callStackFrame.setAutoRefresh(!isEmulatorPlaying);
        }
        else {
            loadMenuItem.setEnabled(true); loadButton.setEnabled(true);
            playMenuItem.setEnabled(false); playButton.setEnabled(false);
            debugMenuItem.setEnabled(false); debugButton.setEnabled(false);
            pauseMenuItem.setEnabled(false); pauseButton.setEnabled(false);
            stepMenuItem.setEnabled(false); stepButton.setEnabled(false);
            breakpointMenuItem.setEnabled(false); breakpointButton.setEnabled(false);
            optionsMenuItem.setEnabled(true); optionsButton.setEnabled(true);
            // Editable components
            if (memoryHexEditorFrame != null) memoryHexEditorFrame.setEditable(true);
            if (cpuStateEditorFrame != null) cpuStateEditorFrame.setEditable(true);
            if (callStackFrame != null) callStackFrame.setAutoRefresh(true);
        }
    }


    public boolean isEmulatorReady() {
        return isImageLoaded && !isEmulatorPlaying;
    }

    public boolean isEmulatorPlaying() {
        return isEmulatorPlaying;
    }

    public CodeStructure getCodeStructure() {
        return codeStructure;
    }

    public void setCodeStructure(CodeStructure codeStructure) {
        this.codeStructure = codeStructure;
    }

    /**
     * Starts an emulating session
     * @param stepMode if true, only execute one exception
     * @param debugMode if true, defined user breakpoints are active
     * @param endAddress if not null, stop when reaching this address
     */
    private void playEmulator(boolean stepMode, boolean debugMode, Integer endAddress) {
        if (!isImageLoaded) {
            throw new RuntimeException("No Image loaded !");
        }

        emulator.clearBreakConditions();

        if (stepMode) {
            emulator.addBreakCondition(new AlwaysBreakCondition());
        }
        else {
            if (debugMode) {
                for (BreakTrigger breakTrigger : prefs.getTriggers()) {
                    if (breakTrigger.isEnabled()) {
                        emulator.addBreakCondition(new AndCondition(breakTrigger.getBreakConditions(), breakTrigger));
                    }
                }
            }
            if (endAddress != null) {
                // Set a temporary break condition at given endAddress
                CPUState values = new CPUState(endAddress);
                CPUState flags = new CPUState();
                flags.pc = 1;
                flags.setILM(0, false);
                flags.setReg(CPUState.TBR, 0);
                BreakTrigger breakTrigger = new BreakTrigger("Run to cursor at 0x" + Format.asHex(endAddress, 8), values, flags, new ArrayList<MemoryValueBreakCondition>());
                emulator.addBreakCondition(new BreakPointCondition(endAddress, breakTrigger));
            }
        }
        startEmulator();
    }


    public void playToAddress(Integer endAddress, boolean debugMode) {
        playEmulator(false, debugMode, endAddress);
    }

    private void startEmulator() {
        isEmulatorPlaying = true;
        updateStates();
        Thread emulatorThread = new Thread(new Runnable() {
            public void run() {
                emulator.setOutputOptions(prefs.getOutputOptions());
                setStatusText("Emulator is running...");
                BreakCondition stopCause = null;
                try {
                    stopCause = emulator.play();
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
                    isEmulatorPlaying = false;
                    if (sourceCodeFrame != null) {
                        sourceCodeFrame.onPcChanged();
                    }
                    if (stopCause != null && stopCause.getBreakTrigger() != null) {
                        setStatusText("Break trigger matched : " + stopCause.getBreakTrigger().getName());
                    }
                    else {
                        setStatusText("Emulation complete");
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


    public void playOneFunction(int address, boolean debugMode) {
        // To execute one function only, we put a fake CALL at a conventional place, followed by an infinite loop
        memory.store16(FUNCTION_CALL_BASE_ADDRESS, 0x9f8c);      // LD R12,
        memory.store32(FUNCTION_CALL_BASE_ADDRESS + 2, address); //         address
        memory.store16(FUNCTION_CALL_BASE_ADDRESS + 6, 0x971c);  // CALL @R12
        memory.store16(FUNCTION_CALL_BASE_ADDRESS + 8, 0xe0ff);  // HALT, infinite loop

        // And we put a breakpoint on the instruction after the call
        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(FUNCTION_CALL_BASE_ADDRESS + 8, null));

        cpuState.pc = FUNCTION_CALL_BASE_ADDRESS;

        if (debugMode) {
            for (BreakTrigger breakTrigger : prefs.getTriggers()) {
                if (breakTrigger.isEnabled()) {
                    emulator.addBreakCondition(new AndCondition(breakTrigger.getBreakConditions(), breakTrigger));
                }
            }
        }


        startEmulator();
    }


    private void pauseEmulator() {
        emulator.addBreakCondition(new AlwaysBreakCondition());
        emulator.exitSleepLoop();
    }

    private void stopEmulator() {
        emulator.addBreakCondition(new AlwaysBreakCondition());
        emulator.exitSleepLoop();
        try {
            // Wait for emulator to stop
            Thread.sleep(120);
        } catch (InterruptedException e) {}
        loadImage();
    }

    //Quit the application.
    protected void quit() {
        dispose();
    }

    public TrackingMemoryActivityListener getTrackingMemoryActivityListener() {
        if (memoryActivityViewerFrame != null) {
            return memoryActivityViewerFrame.getTrackingMemoryActivityListener();
        }
        else {
            return null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        closeAllFrames();
        Prefs.save(prefs);
        System.exit(0);
    }

    public void jumpToSource(Function function) {
        if (codeStructure != null) {
            if (sourceCodeFrame == null) {
                toggleSourceCodeWindow();
            }
            sourceCodeFrame.writeFunction(function);
        }
    }

    public void jumpToSource(int address) {
        if (codeStructure != null) {
            if (sourceCodeFrame == null) {
                toggleSourceCodeWindow();
            }
            if (!sourceCodeFrame.exploreAddress(address)) {
                JOptionPane.showMessageDialog(this, "No function found at address 0x" + Format.asHex(address, 8), "Cannot explore function", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void jumpToMemory(int address) {
        if (memoryHexEditorFrame == null) {
            toggleMemoryHexEditor();
        }
        memoryHexEditorFrame.jumpToAddress(address, 4);
    }

    public void onBreaktriggersChange() {
        if (sourceCodeFrame != null) {
            sourceCodeFrame.updateBreakTriggers();
        }
        if (breakTriggerListFrame != null) {
            breakTriggerListFrame.updateBreaktriggers();
        }
    }
}
