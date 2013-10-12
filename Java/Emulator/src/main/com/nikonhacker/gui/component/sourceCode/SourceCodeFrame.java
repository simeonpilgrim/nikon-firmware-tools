package com.nikonhacker.gui.component.sourceCode;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.EmulationFramework;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SourceCodeFrame extends DocumentFrame implements ActionListener, KeyListener, PopupMenuListener {
    private static final int FRAME_WIDTH  = 400;
    private static final int FRAME_HEIGHT = 500;

    private final RSyntaxTextArea listingArea;
    private final ImageIcon icons[][][][][][][][][] = new ImageIcon[2][2][2][2][2][2][2][2][2];

    private Gutter gutter;
    private Object pcHighlightTag = null;
    private final JTextField searchField;
    private       JCheckBox  regexCB;
    private       JCheckBox  matchCaseCB;

    private CPUState      cpuState;
    private CodeStructure codeStructure;

    /** Contains, for each line number, the address of the instruction it contains, or null if it's not an instruction */
    private List<Integer> lineAddresses = new ArrayList<Integer>();

    private final JTextField        targetField;
    private       int               lastClickedTextPosition;
    private final JCheckBox         followPcCheckBox;
    private       JMenuItem         addBreakPointMenuItem;
    private       JMenuItem         removeBreakPointMenuItem;
    private       JMenuItem         toggleBreakPointMenuItem;
    private       JCheckBoxMenuItem breakCheckBoxMenuItem;
    private       JCheckBoxMenuItem logCheckBoxMenuItem;
    private       JMenuItem         runToHereMenuItem;
    private       JMenuItem         debugToHereMenuItem;

    private boolean enabled = true;

    private BufferedImage stopImg;
    private BufferedImage noStopImg;
    private BufferedImage logImg;
    private BufferedImage startLogImg;
    private BufferedImage endLogImg;
    private BufferedImage jumpImg;
    private BufferedImage interruptImg;
    private BufferedImage noInterruptImg;
    private BufferedImage registerImg;

    public SourceCodeFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui, final CPUState cpuState, final CodeStructure codeStructure) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        // Load icons
        try {
            stopImg = ImageIO.read(EmulatorUI.class.getResource("images/triggerStop.png"));
            noStopImg = ImageIO.read(EmulatorUI.class.getResource("images/triggerNoStop.png"));
            logImg = ImageIO.read(EmulatorUI.class.getResource("images/triggerLog.png"));
            startLogImg = ImageIO.read(EmulatorUI.class.getResource("images/triggerStartLog.png"));
            endLogImg = ImageIO.read(EmulatorUI.class.getResource("images/triggerEndLog.png"));
            jumpImg = ImageIO.read(EmulatorUI.class.getResource("images/triggerJump.png"));
            interruptImg = ImageIO.read(EmulatorUI.class.getResource("images/triggerInterrupt.png"));
            noInterruptImg = ImageIO.read(EmulatorUI.class.getResource("images/triggerNoInterrupt.png"));
            registerImg = ImageIO.read(EmulatorUI.class.getResource("images/triggerRegister.png"));
        } catch (IOException e) {
            System.err.println("Error initializing source code break trigger icons");
            e.printStackTrace();
        }

        this.cpuState = cpuState;
        this.codeStructure = codeStructure;

        setSize(FRAME_WIDTH, FRAME_HEIGHT);


        // Create top toolbar

        JPanel topToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topToolbar.add(new JLabel("Function name or address:"));
        targetField = new JTextField(7);
        topToolbar.add(targetField);
        final JButton exploreButton = new JButton("Explore");
        topToolbar.add(exploreButton);
        JButton goToPcButton = new JButton("Go to PC");
        topToolbar.add(goToPcButton);
        followPcCheckBox = new JCheckBox("Follow PC");
        followPcCheckBox.setSelected(ui.getPrefs().isSourceCodeFollowsPc(chip));
        topToolbar.add(followPcCheckBox);

        // Add listeners
        ActionListener exploreExecutor = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Integer address = codeStructure.getAddressFromString(targetField.getText());
                if (address == null) {
                    targetField.setBackground(Color.RED);
                }
                else {
                    targetField.setBackground(Color.WHITE);
                    if (!exploreAddress(address)) {
                        JOptionPane.showMessageDialog(SourceCodeFrame.this, "No function found matching address 0x" + Format.asHex(address, 8), "Cannot explore function", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        targetField.addActionListener(exploreExecutor);
        exploreButton.addActionListener(exploreExecutor);
        goToPcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!exploreAddress(cpuState.pc)) {
                    JOptionPane.showMessageDialog(SourceCodeFrame.this, "No function found at address 0x" + Format.asHex(cpuState.pc, 8), "Cannot explore function", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        targetField.addKeyListener(this);
        exploreButton.addKeyListener(this);
        followPcCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ui.getPrefs().setSourceCodeFollowsPc(chip, followPcCheckBox.isSelected());
                if (followPcCheckBox.isSelected()) {
                    reachAndHighlightPc();
                }
            }
        });
        

        // Create listing

        listingArea = new RSyntaxTextArea(50, 80);
        prepareAreaFormat(chip, listingArea);
        JComponent listingComponent = prepareListingPane();


        // Create a bottom toolbar with searching options.

        JPanel searchBar = new JPanel();
        searchField = new JTextField(30);
        searchField.addKeyListener(this);
        searchBar.add(searchField);
        final JButton nextButton = new JButton("Find Next");
        nextButton.setActionCommand("FindNext");
        nextButton.addActionListener(this);
        nextButton.addKeyListener(this);
        searchBar.add(nextButton);
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextButton.doClick(0);
            }
        });
        JButton prevButton = new JButton("Find Previous");
        prevButton.setActionCommand("FindPrev");
        prevButton.addActionListener(this);
        prevButton.addKeyListener(this);
        searchBar.add(prevButton);
        regexCB = new JCheckBox("Regex");
        regexCB.addKeyListener(this);
        searchBar.add(regexCB);
        matchCaseCB = new JCheckBox("Match Case");
        matchCaseCB.addKeyListener(this);
        searchBar.add(matchCaseCB);

        // Create and fill main panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(topToolbar, BorderLayout.NORTH);
        contentPanel.add(listingComponent, BorderLayout.CENTER);
        contentPanel.add(searchBar, BorderLayout.SOUTH);

        getContentPane().add(contentPanel);

        if (ui.getPrefs().isSourceCodeFollowsPc(chip)) {
            reachAndHighlightPc();
        }

        pack();
    }

    /**
     * Returns true if requested function was found
     * @param address
     * @return
     */
    public boolean exploreAddress(int address) {
        address = address & 0xFFFFFFFE; // ignore LSB (error in FR, ISA mode in TX)
        targetField.setText(Format.asHex(address, 8));
        Function function = codeStructure.getFunction(address);
        if (function == null) {
            function = codeStructure.findFunctionIncluding(address);
        }
        if (function != null) {
            writeFunction(function);
            Integer line = getLineFromAddress(address);
            if (line != null) {
                try {
                    listingArea.setCaretPosition(listingArea.getLineStartOffset(line));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Format as expected for Fr source display
     * @param chip
     * @param listingArea
     */
    public static void prepareAreaFormat(int chip, RSyntaxTextArea listingArea) {
        listingArea.setEditable(false);
        listingArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        listingArea.setCodeFoldingEnabled(true);
        listingArea.setAntiAliasingEnabled(true);

        listingArea.setMarkOccurrences(true);
        listingArea.setMarkOccurrencesColor(Color.GREEN);
//        // When one clicks on a term, highlight all occurrences (not only the ones within the same syntactic group)
//        MarkAllOccurrencesSupport support = new MarkAllOccurrencesSupport();
//        support.setColor(Color.GREEN);
//        support.install(listingArea);

        // Make current line transparent so PC line highlight passes through
        listingArea.setCurrentLineHighlightColor(new Color(255,255,0,64));

        // Register our assembly syntax highlighter
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        if (chip == Constants.CHIP_FR) {
            atmf.putMapping("text/frasm", "com.nikonhacker.gui.component.sourceCode.syntaxHighlighter.AssemblerFrTokenMaker");
        }
        else {
            atmf.putMapping("text/txasm", "com.nikonhacker.gui.component.sourceCode.syntaxHighlighter.AssemblerTxTokenMaker");
        }
        TokenMakerFactory.setDefaultInstance(atmf);

        SyntaxScheme ss = listingArea.getSyntaxScheme();
        Style functionStyle = ss.getStyle(Token.FUNCTION);

        Style addressStyle = (Style) functionStyle.clone();
        ss.setStyle(Token.LITERAL_NUMBER_HEXADECIMAL, addressStyle);
        addressStyle.foreground = Color.BLACK;

        Style instructionStyle = (Style) functionStyle.clone();
        ss.setStyle(Token.ANNOTATION, instructionStyle);
        instructionStyle.foreground = Color.LIGHT_GRAY;

        Style variableStyle = ss.getStyle(Token.VARIABLE);
        variableStyle.foreground = new Color(155, 22, 188);

        Style reservedWordStyle = ss.getStyle(Token.RESERVED_WORD);
        reservedWordStyle.foreground = new Color(0, 0, 255);

        Style reservedWord2Style = ss.getStyle(Token.RESERVED_WORD_2);
        reservedWord2Style.foreground = new Color(0, 150, 150);

        // Assign it to our area
        if (chip == Constants.CHIP_FR) {
            listingArea.setSyntaxEditingStyle("text/frasm");
        } else {
            listingArea.setSyntaxEditingStyle("text/txasm");
        }
        RSyntaxTextAreaHighlighter rSyntaxTextAreaHighlighter = new RSyntaxTextAreaHighlighter();
        listingArea.setHighlighter(rSyntaxTextAreaHighlighter);
    }

    /**
     * Add behaviours
     * @return
     */
    private JComponent prepareListingPane() {
        listingArea.addKeyListener(this); // For search keys

        // This is to make sure the right mouse button also moves the caret,
        // so that right-click + Toggle breakpoint acts on the clicked line
        listingArea.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {
                listingArea.requestFocusInWindow();
                lastClickedTextPosition = listingArea.viewToModel(e.getPoint());
            }
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        // Customize menu, copying over interesting preexisting entries
        JPopupMenu oldPopupMenu = listingArea.getPopupMenu();
        JPopupMenu newPopupMenu = new JPopupMenu();
        List<Integer> itemsToKeep = Arrays.asList(3, 6);
        MenuElement[] subElements = oldPopupMenu.getSubElements();
        for (int i = 0; i < subElements.length; i++) {
            MenuElement menuElement = subElements[i];
            JMenuItem item = (JMenuItem) menuElement;
            if (itemsToKeep.contains(i)) {
                newPopupMenu.add(item);
            }
        }
        newPopupMenu.addSeparator();

        newPopupMenu.add(new JMenuItem(new FindTextAction()));

        newPopupMenu.addSeparator();

        addBreakPointMenuItem = new JMenuItem("Add trigger");
        newPopupMenu.add(addBreakPointMenuItem);
        addBreakPointMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Integer address = getClickedAddress();
                    if (address != null) {
                        BreakTrigger matchedTrigger = getBreakTrigger(address);
                        if (matchedTrigger == null) {
                            // No match. Create a new one
                            CPUState values;
                            CPUState flags;
                            if (chip == Constants.CHIP_FR) {
                                values = new FrCPUState(address);
                                flags = new FrCPUState();
                                ((FrCPUState)flags).setILM(0, false);
                                flags.setReg(FrCPUState.TBR, 0);
                            }
                            else {
                                values = new TxCPUState(address);
                                flags = new TxCPUState();
                            }
                            flags.pc = 1;

                            String triggerName;
                            if (codeStructure.isFunction(address)) {
                                triggerName = codeStructure.getFunctionName(address) + "()";
                            }
                            else {
                                triggerName = "Breakpoint at 0x" + Format.asHex(address, 8);
                            }
                            ui.getPrefs().getTriggers(chip).add(new BreakTrigger(triggerName, values, flags, new ArrayList<MemoryValueBreakCondition>()));

                            ui.onBreaktriggersChange(chip);
                        }
                    }
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        });

        removeBreakPointMenuItem = new JMenuItem("Delete trigger");
        newPopupMenu.add(removeBreakPointMenuItem);
        removeBreakPointMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BreakTrigger trigger = getClickedTrigger();
                if (trigger != null) {
                    if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the trigger '" + trigger.getName() + "' ?", "Delete ?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        ui.getPrefs().getTriggers(chip).remove(trigger);
                        ui.onBreaktriggersChange(chip);
                    }
                }
            }
        });

        toggleBreakPointMenuItem = new JMenuItem("Toggle trigger");
        newPopupMenu.add(toggleBreakPointMenuItem);
        toggleBreakPointMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BreakTrigger trigger = getClickedTrigger();
                if (trigger != null) {
                    trigger.setEnabled(!trigger.isEnabled());
                    ui.onBreaktriggersChange(chip);
                }
            }
        });

        newPopupMenu.addSeparator();

        breakCheckBoxMenuItem = new JCheckBoxMenuItem("Break");
        newPopupMenu.add(breakCheckBoxMenuItem);
        breakCheckBoxMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BreakTrigger trigger = getClickedTrigger();
                if (trigger != null) {
                    trigger.setMustBreak(!trigger.mustBreak());
                    ui.onBreaktriggersChange(chip);
                }
            }
        });

        logCheckBoxMenuItem = new JCheckBoxMenuItem("Log");
        newPopupMenu.add(logCheckBoxMenuItem);
        logCheckBoxMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BreakTrigger trigger = getClickedTrigger();
                if (trigger != null) {
                    trigger.setMustBeLogged(!trigger.mustBeLogged());
                    ui.onBreaktriggersChange(chip);
                }
            }
        });

        newPopupMenu.addSeparator();

        debugToHereMenuItem = new JMenuItem(new RunToHereAction(EmulationFramework.ExecutionMode.DEBUG, true));
        newPopupMenu.add(debugToHereMenuItem);

        runToHereMenuItem = new JMenuItem(new RunToHereAction(EmulationFramework.ExecutionMode.RUN, false));
        newPopupMenu.add(runToHereMenuItem);


        newPopupMenu.addPopupMenuListener(this);

        listingArea.setPopupMenu(newPopupMenu);

        RTextScrollPane scrollPane = new RTextScrollPane(listingArea);
        scrollPane.setIconRowHeaderEnabled(true);
        scrollPane.setFoldIndicatorEnabled(false);

        gutter = scrollPane.getGutter();
        // disabling bookmark for now, because it is easier to clear all icons when updating bookmarks
        // they're not very useful until they are persisted anyway...
        gutter.setBookmarkingEnabled(false);
        // gutter.setBookmarkIcon(bookmarkIcon);
        gutter.setLineNumberColor(Color.LIGHT_GRAY);

        return scrollPane;
    }

    private BreakTrigger getClickedTrigger() {
        try {
            Integer address = getClickedAddress();
            if (address != null) {
                return getBreakTrigger(address);
            }
        } catch (BadLocationException ble) {
            // noop
        }
        return null;
    }


    public void reachAndHighlightPc() {
        if (pcHighlightTag != null) {
            listingArea.removeLineHighlight(pcHighlightTag);
        }
        try {
            Integer lineFromAddress = getLineFromAddress(cpuState.pc);
            if (lineFromAddress == null) {
                // PC is not found in current function. Try to find the correct function
                // ExploreAddress will take care of calling this function to highlight PC
                exploreAddress(cpuState.pc);
            }
            if (lineFromAddress != null) {
                pcHighlightTag = listingArea.addLineHighlight(lineFromAddress, Color.CYAN);
                // Force scrolling by putting caret on same line
                listingArea.setCaretPosition(listingArea.getLineStartOffset(lineFromAddress));
            }
        } catch (BadLocationException e) {
            pcHighlightTag = null;
            e.printStackTrace();
        }
    }

    public void setEditable(boolean enabled) {
        this.enabled = enabled;
    }

    public void highlightPc() {
        if (pcHighlightTag != null) {
            listingArea.removeLineHighlight(pcHighlightTag);
        }
        try {
            Integer lineFromAddress = getLineFromAddress(cpuState.pc);
            if (lineFromAddress != null) {
                pcHighlightTag = listingArea.addLineHighlight(lineFromAddress, Color.CYAN);
            }
        } catch (BadLocationException e) {
            pcHighlightTag = null;
            e.printStackTrace();
        }
    }

    public void onEmulatorStop() {
        if (followPcCheckBox.isSelected()) {
            reachAndHighlightPc();
        }
        else {
            highlightPc();
        }
    }


    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        addBreakPointMenuItem.setVisible(false);
        removeBreakPointMenuItem.setVisible(false);
        breakCheckBoxMenuItem.setVisible(false);
        logCheckBoxMenuItem.setVisible(false);
        debugToHereMenuItem.setEnabled(false);
        runToHereMenuItem.setEnabled(false);
        toggleBreakPointMenuItem.setVisible(false);
        if (enabled) {
            try {
                Integer address = getClickedAddress();
                if (address != null) {
                    BreakTrigger trigger = getBreakTrigger(address);
                    if (trigger != null) {
                        removeBreakPointMenuItem.setVisible(true);
                        toggleBreakPointMenuItem.setVisible(true);
                        toggleBreakPointMenuItem.setText(trigger.isEnabled() ? "Disable trigger" : "EnableTrigger");
                        breakCheckBoxMenuItem.setVisible(true);
                        breakCheckBoxMenuItem.setSelected(trigger.mustBreak());
                        logCheckBoxMenuItem.setVisible(true);
                        logCheckBoxMenuItem.setSelected(trigger.mustBeLogged());
                    }
                    else {
                        addBreakPointMenuItem.setVisible(true);
                    }

                    debugToHereMenuItem.setEnabled(true);
                    runToHereMenuItem.setEnabled(true);
                }
            } catch (BadLocationException ble) {
                // noop
            }
        }
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {}


    /**
     * An action that gets the text at the current caret position and searches it
     */
    private class FindTextAction extends TextAction {
        public FindTextAction() {
            super("Find");
        }

        public void actionPerformed(ActionEvent e) {
            findSelectedText();
        }
    }


    /**
     * An action that allows to run/debug code up to the click position
     */
    private class RunToHereAction extends TextAction {

        private EmulationFramework.ExecutionMode executionMode;
        private boolean                          debugMode;

        public RunToHereAction(EmulationFramework.ExecutionMode executionMode, boolean debugMode) {
            super(executionMode.getLabel() + " to this line");
            this.executionMode = executionMode;
            this.debugMode = debugMode;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                JTextComponent textComponent = getTextComponent(e);
                if (textComponent instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) textComponent;
                    Integer addressFromLine = getAddressFromLine(textArea.getLineOfOffset(lastClickedTextPosition));
                    if (addressFromLine != null) {
                        ui.playToAddress(chip, executionMode, addressFromLine);
                    }
                }
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }


    public void actionPerformed(ActionEvent e) {
        // "FindNext" => search forward, "FindPrev" => search backward
        String command = e.getActionCommand();
        performSearch("FindNext".equals(command));
    }

    private void performSearch(boolean isForward) {
        // Create an object defining our search parameters.
        SearchContext context = new SearchContext();
        String text = searchField.getText();
        if (text.length() == 0) {
            return;
        }
        context.setSearchFor(text);
        context.setMatchCase(matchCaseCB.isSelected());
        context.setRegularExpression(regexCB.isSelected());
        context.setSearchForward(isForward);
        context.setWholeWord(false);

        boolean found = SearchEngine.find(listingArea, context);
        if (!found) {
            JOptionPane.showMessageDialog(this, "Text not found");
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        // CTRL-F
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
            // TODO show if hidden
            findSelectedText();
        }
        else if (e.getKeyCode() == KeyEvent.VK_F3) {
            performSearch(!e.isShiftDown());
        }
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            // TODO hide if shown
        }
    }

    public void keyReleased(KeyEvent e) {
    }


    private void findSelectedText() {
        try {
            // Search implementation
            int selStart = listingArea.getSelectionStart();
            int selEnd = listingArea.getSelectionEnd();
            if (selStart != selEnd) {
                searchField.setText(listingArea.getText(selStart, selEnd - selStart));
            }
            performSearch(true);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }


    // Real source code handling methods


    public void writeFunction(Function function) {
        listingArea.setText("");
        lineAddresses.clear();
        List<CodeSegment> segments = function.getCodeSegments();
        if (segments.size() == 0) {
            listingArea.setText("; function at address 0x" + Format.asHex(function.getAddress(), 8) + " was not disassembled (not in CODE range)");
            lineAddresses.add(null);
        }
        else {
            for (int i = 0; i < segments.size(); i++) {
                CodeSegment codeSegment = segments.get(i);
                if (segments.size() > 1) {
                    listingArea.append("; Segment " + (i + 1) + "/" + segments.size() + "\n");
                    lineAddresses.add(null);
                }
                for (int address = codeSegment.getStart(); address <= codeSegment.getEnd(); address = codeStructure.getAddressOfStatementAfter(address)) {
                    Statement statement = codeStructure.getStatement(address);
                    try {
                        StringWriter writer = new StringWriter();
                        codeStructure.writeStatement(writer, address, statement, 0, ui.getPrefs().getOutputOptions(chip));
                        String str = writer.toString();
                        for (String line : str.split("\n")) {
                            if (line.length() > 0 && isCodeLine(line)) {
                                lineAddresses.add(address);
                            }
                            else {
                                lineAddresses.add(null);
                            }
                            listingArea.append(line + "\n");
                        }
                    } catch (IOException e) {
                        listingArea.append("# ERROR decoding instruction at address 0x" + Format.asHex(address, 8) + " : " + e.getMessage() + "\n");
                        lineAddresses.add(null);
                    }
                }
                listingArea.append("\n");
                lineAddresses.add(null);
            }
        }
        listingArea.setCaretPosition(0);
        highlightPc();
        updateBreakTriggers();
    }

    private boolean isCodeLine(String line) {
        char ch = line.charAt(0);
        return Character.isDigit(ch) || (ch >= 'A' && ch <= 'F');
    }


    public void updateBreakTriggers() {
        gutter.removeAllTrackingIcons();
        for (BreakTrigger breakTrigger : ui.getPrefs().getTriggers(chip)) {
            if (breakTrigger.getCpuStateFlags().pc != 0) {
                try {
                    Integer lineFromAddress = getLineFromAddress(breakTrigger.getCpuStateValues().pc);
                    if (lineFromAddress != null) {
                        gutter.addLineTrackingIcon(lineFromAddress, getIcon(breakTrigger));
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ImageIcon getIcon(BreakTrigger breakTrigger) {
        ImageIcon icon = icons[breakTrigger.mustBreak()?1:0]
                [breakTrigger.mustBeLogged()?1:0]
                [breakTrigger.getMustStartLogging()?1:0]
                [breakTrigger.getMustStopLogging()?1:0]
                [breakTrigger.getPcToSet()!=null?1:0]
                [breakTrigger.getInterruptToRequest()!=null?1:0]
                [breakTrigger.getInterruptToWithdraw()!=null?1:0]
                [!breakTrigger.getNewCpuStateFlags().hasAllRegistersZero()?1:0]
                [breakTrigger.isEnabled()?1:0];

        if (icon == null) {
            Image img = new BufferedImage(stopImg.getWidth(), stopImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = ((BufferedImage)img).createGraphics();
            g.drawImage(breakTrigger.mustBreak() ? stopImg : noStopImg, 0, 0, null);
            if (breakTrigger.mustBeLogged()) g.drawImage(logImg, 0, 0, null);
            if (breakTrigger.getMustStartLogging()) g.drawImage(startLogImg, 0, 0, null);
            if (breakTrigger.getMustStopLogging()) g.drawImage(endLogImg, 0, 0, null);
            if (breakTrigger.getPcToSet()!=null) g.drawImage(jumpImg, 0, 0, null);
            if (breakTrigger.getInterruptToRequest()!=null) g.drawImage(interruptImg, 0, 0, null);
            if (breakTrigger.getInterruptToWithdraw()!=null) g.drawImage(noInterruptImg, 0, 0, null);
            if (!breakTrigger.getNewCpuStateFlags().hasAllRegistersZero()) g.drawImage(registerImg, 0, 0, null);
            if (!breakTrigger.isEnabled()) {
                img = GrayFilter.createDisabledImage(img);
            }
            icon = new ImageIcon(img);

            icons[breakTrigger.mustBreak()?1:0]
                    [breakTrigger.mustBeLogged()?1:0]
                    [breakTrigger.getMustStartLogging()?1:0]
                    [breakTrigger.getMustStopLogging()?1:0]
                    [breakTrigger.getPcToSet()!=null?1:0]
                    [breakTrigger.getInterruptToRequest()!=null?1:0]
                    [breakTrigger.getInterruptToWithdraw()!=null?1:0]
                    [!breakTrigger.getNewCpuStateFlags().hasAllRegistersZero()?1:0]
                    [breakTrigger.isEnabled()?1:0] = icon;
        }
        return icon;
    }


    public Integer getLineFromAddress(int address) {
        for (int i = 0; i < lineAddresses.size(); i++) {
            if (lineAddresses.get(i) != null && lineAddresses.get(i) == address) {
                return i;
            }
        }

        return null;
    }


    private Integer getAddressFromLine(int line) {
        return lineAddresses.get(line);
    }

    private Integer getClickedAddress() throws BadLocationException {
        return getAddressFromLine(listingArea.getLineOfOffset(lastClickedTextPosition));
    }

    private BreakTrigger getBreakTrigger(int addressFromLine) {
        for (BreakTrigger breakTrigger : ui.getPrefs().getTriggers(chip)) {
            if (breakTrigger.getCpuStateFlags().pc != 0) {
                if (breakTrigger.getCpuStateValues().pc == addressFromLine) {
                    return breakTrigger;
                }
            }
        }
        return null;
    }
}
