package com.nikonhacker.gui.component.sourceCode;

import com.nikonhacker.Format;
import com.nikonhacker.dfr.*;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SourceCodeFrame extends DocumentFrame implements ActionListener, KeyListener {
    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 500;

    private final RSyntaxTextArea listingArea;
    private final ImageIcon enabledBreakPointIcon = new ImageIcon(EmulatorUI.class.getResource("images/enabledBreakpointIcon.png"));
    private final ImageIcon disabledBreakPointIcon = new ImageIcon(EmulatorUI.class.getResource("images/disabledBreakpointIcon.png"));
//    private final ImageIcon bookmarkIcon = new ImageIcon(EmulatorUI.class.getResource("images/bookmarkIcon.png"));
    
    private Gutter gutter;
    Object pcHighlightTag = null;
    private final JTextField searchField;
    private JCheckBox regexCB;
    private JCheckBox matchCaseCB;

    private CPUState cpuState;
    private CodeStructure codeStructure;
    /** Contains, for each line number, the address of the instruction it contains, or null if it's not an instruction */
    List<Integer> lineAddresses = new ArrayList<Integer>();
    private final JTextField targetAddressField;
    private int lastClickedTextPosition;
    private final JCheckBox followPcCheckBox;


    public SourceCodeFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final CPUState cpuState, final CodeStructure codeStructure, final EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.cpuState = cpuState;
        this.codeStructure = codeStructure;

        setSize(FRAME_WIDTH, FRAME_HEIGHT);


        // Create top toolbar

        JPanel topToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topToolbar.add(new JLabel("0x"));
        targetAddressField = new JTextField(7);
        topToolbar.add(targetAddressField);
        final JButton exploreButton = new JButton("Explore");
        topToolbar.add(exploreButton);
        JButton goToPcButton = new JButton("Go to PC");
        topToolbar.add(goToPcButton);
        followPcCheckBox = new JCheckBox("Follow PC");
        followPcCheckBox.setSelected(ui.getPrefs().isFollowPc());
        topToolbar.add(followPcCheckBox);

        // Add listeners
        ActionListener exploreExecutor = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int address = Format.parseIntHexField(targetAddressField);
                    if (!exploreAddress(address)) {
                        JOptionPane.showMessageDialog(SourceCodeFrame.this, "No function found at address 0x" + Format.asHex(address, 8), "Cannot explore function", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    // do nothing. Field is highlighted
                }
            }
        };
        targetAddressField.addActionListener(exploreExecutor);
        exploreButton.addActionListener(exploreExecutor);
        goToPcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!exploreAddress(cpuState.pc)) {
                    JOptionPane.showMessageDialog(SourceCodeFrame.this, "No function found at address 0x" + Format.asHex(cpuState.pc, 8), "Cannot explore function", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        targetAddressField.addKeyListener(this);
        exploreButton.addKeyListener(this);
        followPcCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ui.getPrefs().setFollowPc(followPcCheckBox.isSelected());
                if (followPcCheckBox.isSelected()) {
                    reachAndHighlightPc();
                }
            }
        });
        

        // Create listing

        listingArea = new RSyntaxTextArea(50, 80);
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

        pack();
    }

    /**
     * Returns true if requested function was found
     * @param address
     * @return
     */
    public boolean exploreAddress(int address) {
        targetAddressField.setText(Format.asHex(address, 8));
        Function function = codeStructure.getFunctions().get(address);
        if (function == null) {
            function = codeStructure.findFunctionIncluding(address);
        }
        if (function != null) {
            writeFunction(function);
            Integer line = getLineFromAddress(address);
            if (line != null) {
                try {
                    listingArea.setCaretPosition(listingArea.getLineStartOffset(line) + 30); // adding 30 to set the caret in the blanks, avoiding automatic highlight of all occurrences
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

    private JComponent prepareListingPane() {
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


        // Register our FR assembly syntax highlighter
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/frasm", "com.nikonhacker.gui.component.sourceCode.syntaxHighlighter.AssemblerFrTokenMaker");
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
        listingArea.setSyntaxEditingStyle("text/frasm");
        RSyntaxTextAreaHighlighter rSyntaxTextAreaHighlighter = new RSyntaxTextAreaHighlighter();
        listingArea.setHighlighter(rSyntaxTextAreaHighlighter);

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
        newPopupMenu.add(new JMenuItem(new ToggleBreakpointAction()));
        newPopupMenu.addSeparator();
        newPopupMenu.add(new JMenuItem(new RunToHereAction(true)));
        newPopupMenu.addSeparator();
        newPopupMenu.add(new JMenuItem(new RunToHereAction(false)));
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
                listingArea.setCaretPosition(listingArea.getLineStartOffset(lineFromAddress) + 30); // adding 30 to set the caret in the blanks, avoiding automatic highlight of all occurrences
            }
        } catch (BadLocationException e) {
            pcHighlightTag = null;
            e.printStackTrace();
        }
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

    public void onPcChanged() {
        if (followPcCheckBox.isSelected()) {
            reachAndHighlightPc();
        }
        else {
            highlightPc();
        }
    }


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
     * An action that toggles breakpoint at clicked position
     */
    private class ToggleBreakpointAction extends TextAction {

        public ToggleBreakpointAction() {
            super("Toggle Breakpoint");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                JTextComponent textComponent = getTextComponent(e);
                if (textComponent instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) textComponent;
                    Integer addressFromLine = getAddressFromLine(textArea.getLineOfOffset(lastClickedTextPosition));
                    if (addressFromLine != null) {
                        toggleBreakpoint(addressFromLine);
                    }
                }
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }

    /**
     * An action that allows to run/debug code up to the click position
     */
    private class RunToHereAction extends TextAction {

        private boolean debugMode;

        public RunToHereAction(boolean debugMode) {
            super((debugMode?"Debug":"Run") + " to this line");
            this.debugMode = debugMode;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                JTextComponent textComponent = getTextComponent(e);
                if (textComponent instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) textComponent;
                    Integer addressFromLine = getAddressFromLine(textArea.getLineOfOffset(lastClickedTextPosition));
                    if (addressFromLine != null) {
                        ui.playToAddress(addressFromLine, debugMode);
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

    public void keyTyped(KeyEvent e) {}

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

    public void keyReleased(KeyEvent e) {}


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
                for (int address = codeSegment.getStart(); address <= codeSegment.getEnd(); address = codeStructure.getInstructions().higherKey(address)) {
                    DisassembledInstruction instruction = codeStructure.getInstructions().get(address);
                    try {
                        StringWriter writer = new StringWriter();
                        codeStructure.writeInstruction(writer, address, instruction, 0);
                        String str = writer.toString();
                        for (String line : str.split("\n")) {
                            if (line.length() > 0 && Character.isDigit(line.charAt(0))) {
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


    public void updateBreakTriggers() {
        gutter.removeAllTrackingIcons();
        for (BreakTrigger breakTrigger : ui.getPrefs().getTriggers()) {
            if (breakTrigger.getCpuStateFlags().pc != 0) {
                try {
                    Integer lineFromAddress = getLineFromAddress(breakTrigger.getCpuStateValues().pc);
                    if (lineFromAddress != null) {
                        gutter.addLineTrackingIcon(lineFromAddress, breakTrigger.isEnabled() ? enabledBreakPointIcon : disabledBreakPointIcon);
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public Integer getLineFromAddress(int address) {
        for (int i = 0; i < lineAddresses.size(); i++) {
            if (lineAddresses.get(i) != null && lineAddresses.get(i) == address){
                return i;
            }
        }
        
        return null;
    }


    private Integer getAddressFromLine(int line) {
        return lineAddresses.get(line);
    }


    private void toggleBreakpoint(int addressFromLine) {
        BreakTrigger matchedTrigger = null;
        for (BreakTrigger breakTrigger : ui.getPrefs().getTriggers()) {
            if (breakTrigger.getCpuStateFlags().pc != 0) {
                if (breakTrigger.getCpuStateValues().pc == addressFromLine) {
                    // We found a matching breakpoint, toggle it
                    breakTrigger.setEnabled(!breakTrigger.isEnabled());
                    matchedTrigger = breakTrigger;
                    break;
                }
            }
        }
        if (matchedTrigger == null) {
            // No match. Create a new one
            CPUState values = new CPUState(addressFromLine);
            CPUState flags = new CPUState();
            flags.pc = 1;
            flags.setILM(0);
            flags.setReg(CPUState.TBR, 0);
            ui.getPrefs().getTriggers().add(new BreakTrigger("Breakpoint at 0x" + Format.asHex(addressFromLine, 8), values, flags, new ArrayList<MemoryValueBreakCondition>()));
        }

        ui.onBreaktriggersChange();
    }
}
