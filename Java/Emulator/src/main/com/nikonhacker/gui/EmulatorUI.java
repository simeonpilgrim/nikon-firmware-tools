package com.nikonhacker.gui;

/*
 * MDI Layout inspired by InternalFrameDemo from the Java Tutorial -
 * http://docs.oracle.com/javase/tutorial/uiswing/components/internalframe.html
 */

/* TODO : track executions in non CODE area */
/* TODO : memory viewer : add checkbox to toggle rotation, button to clear, ... */

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.EmulationException;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;
import com.nikonhacker.encoding.FirmwareDecoder;
import com.nikonhacker.encoding.FirmwareEncoder;
import com.nikonhacker.encoding.FirmwareFormatException;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.cpu.CPUStateEditorFrame;
import com.nikonhacker.gui.component.disassembly.DisassemblyFrame;
import com.nikonhacker.gui.component.memoryActivity.MemoryActivityViewerFrame;
import com.nikonhacker.gui.component.memoryHexEditor.MemoryHexEditorFrame;
import com.nikonhacker.gui.component.memoryMapped.Component4006Frame;
import com.nikonhacker.gui.component.screenEmulator.ScreenEmulatorFrame;

import javax.swing.*;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.prefs.Preferences;

public class EmulatorUI extends JFrame implements ActionListener, ChangeListener {

    private static final String COMMAND_EMULATOR_LOAD = "EMULATOR_LOAD";
    private static final String COMMAND_EMULATOR_PLAY = "EMULATOR_PLAY";
    private static final String COMMAND_EMULATOR_PAUSE = "EMULATOR_PAUSE";
    private static final String COMMAND_EMULATOR_STEP = "EMULATOR_STEP";
    private static final String COMMAND_EMULATOR_STOP = "EMULATOR_STOP";
    private static final String COMMAND_SETUP_BREAKPOINTS = "SETUP_BREAKPOINTS";
    private static final String COMMAND_TEST = "TEST";
    private static final String COMMAND_QUIT = "QUIT";
    private static final String COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER = "TOGGLE_MEMORY_ACTIVITY_VIEWER";
    private static final String COMMAND_TOGGLE_MEMORY_HEX_EDITOR = "TOGGLE_MEMORY_HEX_EDITOR";
    private static final String COMMAND_TOGGLE_SCREEN_EMULATOR = "TOGGLE_SCREEN_EMULATOR";
    private static final String COMMAND_TOGGLE_DISASSEMBLY_WINDOW = "TOGGLE_DISASSEMBLY_WINDOW";
    private static final String COMMAND_TOGGLE_CPUSTATE_WINDOW = "TOGGLE_CPUSTATE_WINDOW";
    private static final String COMMAND_TOGGLE_COMPONENT_4006_WINDOW = "TOGGLE_COMPONENT_4006_WINDOW";
    private static final String COMMAND_DECODE = "DECODE";
    private static final String COMMAND_ENCODE = "ENCODE";
    private static final String COMMAND_DISASSEMBLE_FILE = "DISASSEMBLE_FILE";
    private static final String COMMAND_ABOUT = "ABOUT";

    private static final int BASE_ADDRESS = 0x40000;

    private static File imagefile;

    public static final String PREFKEY_LAST_X = "_last_X";
    public static final String PREFKEY_LAST_Y = "_last_Y";
    public static final String PREFKEY_SLEEP = "sleep_setting";

    private DebuggableMemory memory;
    private CPUState cpuState;
    private Emulator emulator;

    private boolean isImageLoaded = false;
    private boolean isEmulatorPlaying = false;

    long lastUpdateCycles = 0;
    long lastUpdateTime = 0;

    private JDesktopPane mdiPane;

    private JMenuItem loadMenuItem;
    private JMenuItem playMenuItem;
    private JMenuItem pauseMenuItem;
    private JMenuItem stepMenuItem;
    private JMenuItem stopMenuItem;
    private JMenuItem breakpointMenuItem;
    private JCheckBoxMenuItem disassemblyMenuItem;
    private JCheckBoxMenuItem cpuStateMenuItem;
    private JCheckBoxMenuItem memoryActivityViewerMenuItem;
    private JCheckBoxMenuItem memoryHexEditorMenuItem;
    private JCheckBoxMenuItem screenEmulatorMenuItem;
    private JCheckBoxMenuItem component4006MenuItem;

    private JButton loadButton;
    private JButton playButton;
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

    private DocumentFrame disassemblyFrame;
    private CPUStateEditorFrame cpuStateEditorFrame;
    private DocumentFrame screenEmulatorFrame;
    private MemoryActivityViewerFrame memoryActivityViewerFrame;
    private MemoryHexEditorFrame memoryHexEditorFrame;
    private Component4006Frame component4006Frame;


    public static void main(String[] args) throws EmulationException, IOException, ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException {
        if (args.length > 0) {
            if (new File(args[0]).exists()) {
                imagefile = new File(args[0]);
            }
        }

        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        frame.setVisible(true);
    }

    public EmulatorUI() {
        super("Emulator UI");

        //Make the app window indented 50 pixels from each edge of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);

        //Set up the GUI.
        mdiPane = new JDesktopPane();

        setJMenuBar(createMenuBar());

        // setContentPane(mdiPane);

        JPanel mainContentPane = new JPanel(new BorderLayout());
        mainContentPane.add(createToolBar(), BorderLayout.PAGE_START);
        mainContentPane.add(mdiPane, BorderLayout.CENTER);
        setContentPane(mainContentPane);

        if (imagefile != null) {
            loadImage();
        }

        updateStates();

        //Make dragging a little faster but perhaps uglier.
        // mdiPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateTitleBar();
            }
        }).start();
    }

    private void updateTitleBar() {
        if (emulator != null) {
            long totalCycles = emulator.getTotalCycles();
            long now = System.currentTimeMillis();
            long cps = (1000 * (totalCycles - lastUpdateCycles))/(now - lastUpdateTime);

            lastUpdateCycles = totalCycles;
            lastUpdateTime = now;
            setTitle("Emulator UI (" + totalCycles + " cycles emulated. Current speed is "+ cps + "Hz)");
        }
        else {
            setTitle("Emulator UI");
        }
    }

    private JPanel createToolBar() {
        JPanel bar = new JPanel();

        bar.setLayout(new BoxLayout(bar, BoxLayout.LINE_AXIS));

        loadButton = makeButton("load", COMMAND_EMULATOR_LOAD, "Load image", "Load");
        bar.add(loadButton);
        playButton = makeButton("play", COMMAND_EMULATOR_PLAY, "Start or resume emulator", "Play");
        bar.add(playButton);
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

        disassemblyButton = makeButton("disassembly", COMMAND_TOGGLE_DISASSEMBLY_WINDOW, "Disassembly window", "Disassembly");
        bar.add(disassemblyButton);
        cpuStateButton = makeButton("cpu", COMMAND_TOGGLE_CPUSTATE_WINDOW, "CPU State window", "CPU");
        bar.add(cpuStateButton);
        memoryActivityViewerButton = makeButton("memory_activity", COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER, "Memory activity viewer", "Activity");
        bar.add(memoryActivityViewerButton);
        memoryHexEditorButton = makeButton("memory_editor", COMMAND_TOGGLE_MEMORY_HEX_EDITOR, "Memory hex editor", "Hex Editor");
        bar.add(memoryHexEditorButton);
        screenEmulatorButton = makeButton("screen", COMMAND_TOGGLE_SCREEN_EMULATOR, "Screen emulator", "Screen");
        bar.add(screenEmulatorButton);
        component4006Button = makeButton("4006", COMMAND_TOGGLE_COMPONENT_4006_WINDOW, "Component 4006", "Component 4006");
        bar.add(component4006Button);

        bar.add(Box.createHorizontalGlue());
        return bar;
    }

    private JSlider makeSlider() {
        Preferences prefs = Preferences.userNodeForPackage(DocumentFrame.class);
        int sleepValue = prefs.getInt(this.getClass().getSimpleName() + PREFKEY_SLEEP, 2);
        JSlider intervalSlider = new JSlider(JSlider.HORIZONTAL, 0, 5, sleepValue);

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
        loadMenuItem.setActionCommand(COMMAND_EMULATOR_LOAD);
        loadMenuItem.addActionListener(this);
        fileMenu.add(loadMenuItem);

        //emulator play
        playMenuItem = new JMenuItem("Start (or resume) emulator");
        playMenuItem.setMnemonic(KeyEvent.VK_E);
        playMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
        playMenuItem.setActionCommand(COMMAND_EMULATOR_PLAY);
        playMenuItem.addActionListener(this);
        fileMenu.add(playMenuItem);

        //emulator pause
        pauseMenuItem = new JMenuItem("Pause emulator");
        pauseMenuItem.setMnemonic(KeyEvent.VK_P);
        pauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
        pauseMenuItem.setActionCommand(COMMAND_EMULATOR_PAUSE);
        pauseMenuItem.addActionListener(this);
        fileMenu.add(pauseMenuItem);

        //emulator step
        stepMenuItem = new JMenuItem("Step emulator");
//        stepMenuItem.setMnemonic(KeyEvent.VK_P);
//        stepMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
        stepMenuItem.setActionCommand(COMMAND_EMULATOR_STEP);
        stepMenuItem.addActionListener(this);
        fileMenu.add(stepMenuItem);

        //emulator stop
        stopMenuItem = new JMenuItem("Stop emulator");
//        stopMenuItem.setMnemonic(KeyEvent.VK_P);
//        stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
        stopMenuItem.setActionCommand(COMMAND_EMULATOR_STOP);
        stopMenuItem.addActionListener(this);
        fileMenu.add(stopMenuItem);

        //setup breakpoints
        breakpointMenuItem = new JMenuItem("Setup breakpoints");
        breakpointMenuItem.setMnemonic(KeyEvent.VK_B);
        breakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));
        breakpointMenuItem.setActionCommand(COMMAND_SETUP_BREAKPOINTS);
        breakpointMenuItem.addActionListener(this);
        fileMenu.add(breakpointMenuItem);


//        //test
//        tmpMenuItem = new JMenuItem("Test");
//        tmpMenuItem.setMnemonic(KeyEvent.VK_T);
//        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
//        tmpMenuItem.setActionCommand(COMMAND_TEST);
//        tmpMenuItem.addActionListener(this);
//        fileMenu.add(tmpMenuItem);

        //quit
        tmpMenuItem = new JMenuItem("Quit");
        tmpMenuItem.setMnemonic(KeyEvent.VK_Q);
        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        tmpMenuItem.setActionCommand(COMMAND_QUIT);
        tmpMenuItem.addActionListener(this);
        fileMenu.add(tmpMenuItem);


        //Set up the view menu.
        JMenu viewMenu = new JMenu("View");
        fileMenu.setMnemonic(KeyEvent.VK_W);
        menuBar.add(viewMenu);

        //memory activity viewer
        memoryActivityViewerMenuItem = new JCheckBoxMenuItem("Memory activity viewer");
        memoryActivityViewerMenuItem.setMnemonic(KeyEvent.VK_M);
        memoryActivityViewerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
        memoryActivityViewerMenuItem.setActionCommand(COMMAND_TOGGLE_MEMORY_ACTIVITY_VIEWER);
        memoryActivityViewerMenuItem.addActionListener(this);
        viewMenu.add(memoryActivityViewerMenuItem);

        //memory hex editor
        memoryHexEditorMenuItem = new JCheckBoxMenuItem("Memory hex editor");
        memoryHexEditorMenuItem.setMnemonic(KeyEvent.VK_H);
        memoryHexEditorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
        memoryHexEditorMenuItem.setActionCommand(COMMAND_TOGGLE_MEMORY_HEX_EDITOR);
        memoryHexEditorMenuItem.addActionListener(this);
        viewMenu.add(memoryHexEditorMenuItem);

        //screen emulator
        screenEmulatorMenuItem = new JCheckBoxMenuItem("Screen emulator");
        screenEmulatorMenuItem.setMnemonic(KeyEvent.VK_S);
        screenEmulatorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        screenEmulatorMenuItem.setActionCommand(COMMAND_TOGGLE_SCREEN_EMULATOR);
        screenEmulatorMenuItem.addActionListener(this);
        viewMenu.add(screenEmulatorMenuItem);

        //disassembly
        disassemblyMenuItem = new JCheckBoxMenuItem("Disassembly window");
        disassemblyMenuItem.setMnemonic(KeyEvent.VK_D);
        disassemblyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        disassemblyMenuItem.setActionCommand(COMMAND_TOGGLE_DISASSEMBLY_WINDOW);
        disassemblyMenuItem.addActionListener(this);
        viewMenu.add(disassemblyMenuItem);

        //CPU state
        cpuStateMenuItem = new JCheckBoxMenuItem("CPU State window");
        cpuStateMenuItem.setMnemonic(KeyEvent.VK_C);
        cpuStateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
        cpuStateMenuItem.setActionCommand(COMMAND_TOGGLE_CPUSTATE_WINDOW);
        cpuStateMenuItem.addActionListener(this);
        viewMenu.add(cpuStateMenuItem);

        //Component 4006
        component4006MenuItem = new JCheckBoxMenuItem("Component 4006 window");
        component4006MenuItem.setMnemonic(KeyEvent.VK_4);
        component4006MenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
        component4006MenuItem.setActionCommand(COMMAND_TOGGLE_COMPONENT_4006_WINDOW);
        component4006MenuItem.addActionListener(this);
        viewMenu.add(component4006MenuItem);


        //Set up the tools menu.
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(toolsMenu);

        //decoder
        tmpMenuItem = new JMenuItem("Decode firmware");
//        tmpMenuItem.setMnemonic(KeyEvent.VK_M);
//        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
        tmpMenuItem.setActionCommand(COMMAND_DECODE);
        tmpMenuItem.addActionListener(this);
        toolsMenu.add(tmpMenuItem);

        //encoder
        tmpMenuItem = new JMenuItem("Encode firmware");
//        tmpMenuItem.setMnemonic(KeyEvent.VK_S);
//        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        tmpMenuItem.setActionCommand(COMMAND_ENCODE);
        tmpMenuItem.addActionListener(this);
        toolsMenu.add(tmpMenuItem);

        //disassembler
        tmpMenuItem = new JMenuItem("Disassemble firmware");
//        tmpMenuItem.setMnemonic(KeyEvent.VK_S);
//        tmpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        tmpMenuItem.setActionCommand(COMMAND_DISASSEMBLE_FILE);
        tmpMenuItem.addActionListener(this);
//        toolsMenu.add(tmpMenuItem);

        //Set up the help menu.
        JMenu helpMenu = new JMenu("?");
        menuBar.add(helpMenu);

        //about
        tmpMenuItem = new JCheckBoxMenuItem("About");
        tmpMenuItem.setActionCommand(COMMAND_ABOUT);
        tmpMenuItem.addActionListener(this);
        helpMenu.add(tmpMenuItem);

        return menuBar;
    }

    // Event listeners

    /**
     * React to menu selections and toggle buttons.
     *
     * @param e the event
     */
    public void actionPerformed(ActionEvent e) {
        if (COMMAND_EMULATOR_LOAD.equals(e.getActionCommand())) {
            selectAndLoadImage();
        }
        else if (COMMAND_EMULATOR_PLAY.equals(e.getActionCommand())) {
            playEmulator();
        }
        else if (COMMAND_EMULATOR_PAUSE.equals(e.getActionCommand())) {
            pauseEmulator();
        }
        else if (COMMAND_EMULATOR_STEP.equals(e.getActionCommand())) {
            stepEmulator();
        }
        else if (COMMAND_EMULATOR_STOP.equals(e.getActionCommand())) {
            stopEmulator();
        }
        else if (COMMAND_SETUP_BREAKPOINTS.equals(e.getActionCommand())) {
            setBreakpoints();
        }
        else if (COMMAND_TEST.equals(e.getActionCommand())) {

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
            toggleDisassembly();
        }
        else if (COMMAND_TOGGLE_CPUSTATE_WINDOW.equals(e.getActionCommand())) {
            toggleCPUState();
        }
        else if (COMMAND_TOGGLE_COMPONENT_4006_WINDOW.equals(e.getActionCommand())) {
            toggleComponent4006();
        }
        else if (COMMAND_DECODE.equals(e.getActionCommand())) {
            openDecodeDialog();
        }
        else if (COMMAND_ENCODE.equals(e.getActionCommand())) {
            openEncodeDialog();
        }
        else if (COMMAND_DISASSEMBLE_FILE.equals(e.getActionCommand())) {
            //openDisassembleDialog();
        }
        else if (COMMAND_ABOUT.equals(e.getActionCommand())) {
            showAbout();
        }
        else {
            System.err.println("Unknown menu command : " + e.getActionCommand());
        }
    }

    private void setBreakpoints() {
//        JTextField sourceFile = new JTextField();
//        JTextField destinationDir = new JTextField();
//        final JComponent[] inputs = new JComponent[]{
//                new FileSelectionPanel("Source file", sourceFile, false),
//                new FileSelectionPanel("Destination dir", destinationDir, true)
//        };
//        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
//                inputs,
//                "Choose decoding source and destination",
//                JOptionPane.OK_CANCEL_OPTION,
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                null,
//                JOptionPane.DEFAULT_OPTION)) {
//            try {
//                new FirmwareDecoder().decode(sourceFile.getText(), destinationDir.getText(), false);
                JOptionPane.showMessageDialog(this, "Decoding complete", "Done", JOptionPane.INFORMATION_MESSAGE);
//            } catch (FirmwareFormatException e) {
//                JOptionPane.showMessageDialog(this, e.getMessage(), "Error decoding files", JOptionPane.ERROR_MESSAGE);
//            }
//        }
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


    private void selectAndLoadImage() {
        final JFileChooser fc = new JFileChooser();

        fc.setCurrentDirectory(new java.io.File("."));

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            imagefile = fc.getSelectedFile();
            loadImage();
        }
    }

    private void showAbout() {
        // for copying style
        JLabel label = new JLabel();
        Font font = label.getFont();

        // create some css from the label's font
        StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");

        // html content
        JEditorPane editorPane = new JEditorPane("text/html", "<html><body style=\"" + style + "\">"
                + "<font size=\"+1\">Fujitsu FR emulator</font><br/><br/>"
                + "This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.<br/>"
                + "This software is provided under the GNU General Public License, version 3 - " + makeLink("http://www.gnu.org/licenses/gpl-3.0.txt") + "<br/>"
                + "This software is based on, or makes use of, the following works:<ul>\n"
                + "<li>Simeon Pilgrim's deciphering of firmware encoding and lots of information shared on his blog - " + makeLink("http://simeonpilgrim.com/blog/") + "</li>"
                + "<li>Dfr Fujitsu FR diassembler Copyright (c) Kevin Schoedel - " + makeLink("http://scratchpad.wikia.com/wiki/Disassemblers/DFR") + "<br/>and its port to C# by Simeon Pilgrim</li>"
                + "<li>The PearColator x86 emulator project - " + makeLink("http://apt.cs.man.ac.uk/projects/jamaica/tools/PearColator/") + "</li>"
                + "<li>The Jacksum checksum library Copyright (c) Dipl.-Inf. (FH) Johann Nepomuk L�fflmann  - " + makeLink("http://www.jonelo.de/java/jacksum/") + "</li>"
                + "<li>HexEditor swing component, Copyright (c) Robert Futrell - " + makeLink("http://fifesoft.com/hexeditor/") + "</li>"
                + "<li>Apache commons libraries, Copyright (c) The Apache Software Foundation - " + makeLink("http://commons.apache.org/") + "</li>"
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
        setEmulatorSleep(source.getValue());
        Preferences prefs = Preferences.userNodeForPackage(DocumentFrame.class);
        prefs.putInt(this.getClass().getSimpleName() + PREFKEY_SLEEP, source.getValue());
    }

    private void setEmulatorSleep(int value) {
        emulator.setSleepIntervalChanged();
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
            memory.loadFile(imagefile, BASE_ADDRESS);

            cpuState = new CPUState(BASE_ADDRESS);

            emulator = new Emulator(1); // We stop at each instruction to allow for pause and other debugging
            emulator.setMemory(memory);
            emulator.setCpuState(cpuState);

            Preferences prefs = Preferences.userNodeForPackage(DocumentFrame.class);
            int sleepValue = prefs.getInt(this.getClass().getSimpleName() + PREFKEY_SLEEP, 2);
            setEmulatorSleep(sleepValue);

            isImageLoaded = true;
            closeAllFrames();

            updateStates();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeAllFrames() {
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
        if (disassemblyFrame != null) {
            disassemblyFrame.dispose();
            disassemblyFrame = null;
        }
        if (cpuStateEditorFrame != null) {
            cpuStateEditorFrame.dispose();
            cpuStateEditorFrame = null;
        }
        if (component4006Frame != null) {
            component4006Frame.dispose();
            component4006Frame = null;
        }
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
            memoryHexEditorFrame = new MemoryHexEditorFrame("Memory hex editor", true, true, true, true, memory, 0, !isEmulatorPlaying, this);
            addDocumentFrame(memoryHexEditorFrame);
            memoryHexEditorFrame.display(true);
        }
        else {
            memoryHexEditorFrame.dispose();
            memoryHexEditorFrame = null;
        }
        updateStates();
    }

    private void toggleDisassembly() {
        if (disassemblyFrame == null) {
            disassemblyFrame = new DisassemblyFrame("Disassembly", true, true, true, true, emulator, this);
            addDocumentFrame(disassemblyFrame);
            disassemblyFrame.display(true);
        }
        else {
            disassemblyFrame.dispose();
            disassemblyFrame = null;
        }
        updateStates();
    }

    private void toggleCPUState() {
        if (cpuStateEditorFrame == null) {
            cpuStateEditorFrame = new CPUStateEditorFrame("CPU State", false, true, false, true, cpuState, this);
            cpuStateEditorFrame.setEnabled(!isEmulatorPlaying);
            //cpuStateFrame = new CPUStateFrame("CPU State", false, true, false, true, cpuState, this);
            addDocumentFrame(cpuStateEditorFrame);
            cpuStateEditorFrame.display(true);
        }
        else {
            cpuStateEditorFrame.dispose();
            cpuStateEditorFrame = null;
        }
        updateStates();
    }

    private void toggleScreenEmulator() {
        if (screenEmulatorFrame == null) {
            screenEmulatorFrame = new ScreenEmulatorFrame("Screen emulator", true, true, true, true, memory, 0x8F800A9C, 0x8F800A9C + 0x001C43E0, 640, this);
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


    public void addDocumentFrame(DocumentFrame frame) {
        mdiPane.add(frame);
    }


    /**
     * Called back by frames to inform UI that they are being closed
     * @param frame
     */
    public void frameClosing(DocumentFrame frame) {
        if (frame == disassemblyFrame) {
            toggleDisassembly();
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
        else {
            System.err.println("EmulatorUI.frameClosing : Unknown frame is being closed. Please add handler for " + frame.getClass().getSimpleName());
        }
    }

    public void updateStates() {
        disassemblyMenuItem.setSelected(disassemblyFrame != null);
        cpuStateMenuItem.setSelected(cpuStateEditorFrame != null);
        screenEmulatorMenuItem.setSelected(screenEmulatorFrame != null);
        memoryActivityViewerMenuItem.setSelected(memoryActivityViewerFrame != null);
        memoryHexEditorMenuItem.setSelected(memoryHexEditorFrame != null);
        component4006MenuItem.setSelected(component4006Frame != null);

        if (isImageLoaded) {
            disassemblyMenuItem.setEnabled(true); disassemblyButton.setEnabled(true);
            cpuStateMenuItem.setEnabled(true); cpuStateButton.setEnabled(true);
            screenEmulatorMenuItem.setEnabled(true); screenEmulatorButton.setEnabled(true);
            memoryActivityViewerMenuItem.setEnabled(true); memoryActivityViewerButton.setEnabled(true);
            memoryHexEditorMenuItem.setEnabled(true); memoryHexEditorButton.setEnabled(true);
            component4006MenuItem.setEnabled(true); component4006Button.setEnabled(true);

            stopMenuItem.setEnabled(true); stopButton.setEnabled(true);

            if (isEmulatorPlaying) {
                loadMenuItem.setEnabled(false); loadButton.setEnabled(false);
                playMenuItem.setEnabled(false); playButton.setEnabled(false);
                pauseMenuItem.setEnabled(true); pauseButton.setEnabled(true);
                stepMenuItem.setEnabled(false); stepButton.setEnabled(false);
                breakpointMenuItem.setEnabled(false); breakpointButton.setEnabled(false);
                if (memoryHexEditorFrame != null) memoryHexEditorFrame.setEditable(false);
                if (cpuStateEditorFrame != null) cpuStateEditorFrame.setEditable(false);
            }
            else {
                loadMenuItem.setEnabled(true); loadButton.setEnabled(true);
                playMenuItem.setEnabled(true); playButton.setEnabled(true);
                pauseMenuItem.setEnabled(false); pauseButton.setEnabled(false);
                stepMenuItem.setEnabled(true); stepButton.setEnabled(true);
                breakpointMenuItem.setEnabled(true); breakpointButton.setEnabled(true);
                if (memoryHexEditorFrame != null) memoryHexEditorFrame.setEditable(true);
                if (cpuStateEditorFrame != null) cpuStateEditorFrame.setEditable(true);
            }
        }
        else {
            disassemblyMenuItem.setEnabled(false); disassemblyButton.setEnabled(false);
            cpuStateMenuItem.setEnabled(false); cpuStateButton.setEnabled(false);
            screenEmulatorMenuItem.setEnabled(false); screenEmulatorButton.setEnabled(false);
            memoryActivityViewerMenuItem.setEnabled(false); memoryActivityViewerButton.setEnabled(false);
            memoryHexEditorMenuItem.setEnabled(false); memoryHexEditorButton.setEnabled(false);
            component4006MenuItem.setEnabled(false); component4006Button.setEnabled(false);

            loadMenuItem.setEnabled(true); loadButton.setEnabled(true);
            playMenuItem.setEnabled(false); playButton.setEnabled(false);
            pauseMenuItem.setEnabled(false); pauseButton.setEnabled(false);
            stepMenuItem.setEnabled(false); stepButton.setEnabled(false);
            breakpointMenuItem.setEnabled(false); breakpointButton.setEnabled(false);

            stopMenuItem.setEnabled(false); stopButton.setEnabled(false);

            if (memoryHexEditorFrame != null) memoryHexEditorFrame.setEditable(true);
            if (cpuStateEditorFrame != null) cpuStateEditorFrame.setEditable(true);
        }
    }


    private void playEmulator() {
        if (!isImageLoaded) {
            throw new RuntimeException("No Image loaded !");
        }
        emulator.setExitRequired(false);
        Thread emulatorThread = new Thread(new Runnable() {
            public void run() {
                try {
                    emulator.play();
                } catch (EmulationException e) {
                    e.printStackTrace();
                }
            }
        });
        isEmulatorPlaying = true;
        updateStates();
        emulatorThread.start();
    }

    private void pauseEmulator() {
        emulator.setExitRequired(true);
        isEmulatorPlaying = false;
        updateStates();
    }

    private void stepEmulator() {
        if (!isImageLoaded) {
            throw new RuntimeException("No Image loaded !");
        }
        emulator.setExitRequired(true); // To exit immediately
        isEmulatorPlaying = true;
        updateStates();
        Thread emulatorThread = new Thread(new Runnable() {
            public void run() {
                try {
                    emulator.play();
                    isEmulatorPlaying = false;
                    updateStates();
                } catch (EmulationException e) {
                    e.printStackTrace();
                }
            }
        });
        emulatorThread.start();
    }

    private void stopEmulator() {
        emulator.setExitRequired(true);
        isEmulatorPlaying = false;
        updateStates();
        try {
            // Wait for emulator to stop
            Thread.sleep(10);
        } catch (InterruptedException e) {}
        loadImage();
    }

    //Quit the application.
    protected void quit() {
        System.exit(0);
    }

    public TrackingMemoryActivityListener getTrackingMemoryActivityListener() {
        if (memoryActivityViewerFrame != null) {
            return memoryActivityViewerFrame.getTrackingMemoryActivityListener();
        }
        else {
            return null;
        }
    }

    public class FileSelectionPanel extends JPanel implements ActionListener {
        String label;
        JButton button;
        JTextField textField;
        boolean directoryMode;

        public FileSelectionPanel(String label, JTextField textField, boolean directoryMode) {
            super();
            this.label = label;
            this.textField = textField;
            this.directoryMode = directoryMode;

            this.setLayout(new FlowLayout(FlowLayout.RIGHT));

            JLabel jlabel = new JLabel(label);
            this.add(jlabel);

            textField.setPreferredSize(new Dimension(400, (int) textField.getPreferredSize().getHeight()));
            this.add(textField);

            button = new JButton("...");
            this.add(button);

            button.addActionListener(this);
            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        }

        public void actionPerformed(ActionEvent e) {
            final JFileChooser fc = new JFileChooser();

            fc.setDialogTitle("Select " + label);
            fc.setCurrentDirectory(new java.io.File("."));

            if (directoryMode) {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);
            }
            else {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(true);
            }

            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                textField.setText(fc.getSelectedFile().getPath());
            }
        }
    }

}