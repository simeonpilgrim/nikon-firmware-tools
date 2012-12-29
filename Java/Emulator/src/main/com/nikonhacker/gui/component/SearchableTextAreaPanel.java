package com.nikonhacker.gui.component;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <p>This class wraps the given JTextArea inside a scrollable panel, along with a Firefox-like search bar at the bottom
 * The search bar can be made visible or hidden at start, and features a "search term" box, forward and back buttons,
 * and options to be case sensitive or not, and to hightlight all matches at once.</p>
 * <p>CTRL-F opens the bar (if hidden) and performs a search based on the currently selected text of the JTextArea</p>
 * <p>F3 searches for the next occurrence, Shift-F3 searched for the previous one</p>
 * <p>ESC hides the bar</p>
 * <p>Based on http://coding.derkeiler.com/Archive/Java/comp.lang.java.programmer/2008-04/msg01467.html</p>
 * TODO cosmetic : don't select anything if searchText is empty
 * TODO add "cross icon" to close the search bar
 */
public class SearchableTextAreaPanel extends JPanel implements DocumentListener, KeyListener {

    // Global flag to allow fiddling with fields without triggering multiple searches
    private boolean searchEnabled = true;

    public enum SearchDirection {
        BACKWARDS,
        NONE,
        FORWARD
    }

    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JPanel searchPanel;
    private JTextField searchTextField;
    private JCheckBox highlightAllCheckBox;
    private JCheckBox matchCaseCheckBox;
    private int selectedHighlightPosition = -1;
    private Map<Integer,Object> highlights = new HashMap<Integer, Object>();
    private Color defaultSearchFieldBgColor = null;

    /**
     * Create a new SearchableTextAreaPanel encapsulating the given JTextArea, with the search bar hidden
     * @param textArea the textArea to wrap
     */
    public SearchableTextAreaPanel(JTextArea textArea) {
        this(textArea, false);
    }

    /**
     * Create a new SearchableTextAreaPanel encapsulating the given JTextArea, with the search bar optionally shown
     * @param textArea the textArea to wrap
     */
    public SearchableTextAreaPanel(JTextArea textArea, boolean showBarByDefault) {
        super(new BorderLayout());
        this.textArea = textArea;
        createAndShowGUI(showBarByDefault);
    }

    private void createAndShowGUI(boolean showBar) {
        textArea.addKeyListener(this);
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                clearHighlights();
            }

            public void removeUpdate(DocumentEvent e) {
                clearHighlights();
            }

            public void changedUpdate(DocumentEvent e) {
                clearHighlights();
            }
        });

        scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchTextField = new JTextField(20);
        searchTextField.addKeyListener(this);
        searchTextField.getDocument().addDocumentListener(this);
        defaultSearchFieldBgColor = searchTextField.getBackground();
        searchPanel.add(searchTextField);

        JButton nextButton = new JButton("Next");
        nextButton.addKeyListener(this);
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSearch(searchTextField.getText(), textArea.getSelectionStart(), SearchDirection.FORWARD, false);
            }
        });
        searchPanel.add(nextButton);

        JButton previousButton = new JButton("Previous");
        previousButton.addKeyListener(this);
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSearch(searchTextField.getText(), textArea.getSelectionStart(), SearchDirection.BACKWARDS, false);
            }
        });
        searchPanel.add(previousButton);

        highlightAllCheckBox = new JCheckBox("Highlight all");
        highlightAllCheckBox.addKeyListener(this);
        highlightAllCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSearch(searchTextField.getText(), textArea.getSelectionStart(), SearchDirection.NONE, true);
            }
        });
        searchPanel.add(highlightAllCheckBox);

        matchCaseCheckBox = new JCheckBox("Match case");
        matchCaseCheckBox.addKeyListener(this);
        matchCaseCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSearch(searchTextField.getText(), textArea.getSelectionStart(), SearchDirection.NONE, true);
            }
        });
        searchPanel.add(matchCaseCheckBox);

        searchPanel.setVisible(showBar); // if false, it remains hidden until CTRL-F is pressed
        add(searchPanel, BorderLayout.SOUTH);
    }

    /**
     * Searches the given text with default options (case-insensitive, no highlights)
     * @param text the text to search
     * @return
     */
    public int search(String text) {
        return search(text, false);
    }

    /**
     * Searches the given text, optionally matching case, with the default options (no highlights)
     * @param text the text to search
     * @param matchCase if true, a case-sensitive search is performed
     * @return
     */
    public int search(String text, boolean matchCase) {
        return search(text, matchCase, false);
    }

    /**
     * Searches the given text, optionally matching case, and optionally highlighting all the matches
     * @param text the text to search
     * @param matchCase if true, a case-sensitive search is performed
     * @param highlightAll if true, all matches are highlighted
     * @return
     */
    public int search(String text, boolean matchCase, boolean highlightAll) {
        searchPanel.setVisible(true);
        searchEnabled = false; // to avoid that events on field trigger lots of searches
        searchTextField.setText(text);
        matchCaseCheckBox.setSelected(matchCase);
        highlightAllCheckBox.setSelected(highlightAll);
        searchEnabled = true; // restore normal behaviour

        // Perform search 
        return performSearch(text, textArea.getSelectionStart(), SearchDirection.FORWARD, true);
    }

    public void clearHighlights() {
        if (selectedHighlightPosition != -1) {
            textArea.getHighlighter().removeAllHighlights();
            selectedHighlightPosition = -1;
        }
    }

    private int performSearch(String searchText, int startPosition, SearchDirection direction, boolean searchTextChanged) {
        if (searchEnabled) {
            searchTextField.setBackground(defaultSearchFieldBgColor);
            DefaultHighlighter.DefaultHighlightPainter nonSelectedHighlighter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
            DefaultHighlighter.DefaultHighlightPainter selectedHighlighter = new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);

            Cursor previousCursor = textArea.getCursor();
            boolean previousEditableStatus = textArea.isEditable();

            textArea.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            textArea.setEditable(false);

            boolean highlightAll = highlightAllCheckBox.isSelected();
            boolean matchCase = matchCaseCheckBox.isSelected();
            String searchString = matchCase?searchText:searchText.toLowerCase();

            Highlighter highlighter = textArea.getHighlighter();

            // HighlightAll (if requested)
            if (searchTextChanged) {
                highlighter.removeAllHighlights();
                if (highlightAll) {
                    if (searchString.length() > 0) {
                        SearchHighlighter searchHighlighter = new SearchHighlighter(textArea.getDocument(), searchString, matchCase);

                        ExecutorService service = Executors.newSingleThreadExecutor();
                        Future<java.util.List<Integer>> offsets = service.submit(searchHighlighter);

                        try {
                            for (Integer start : offsets.get()) {
                                highlights.put(start, highlighter.addHighlight(start, start + searchString.length(), nonSelectedHighlighter));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // Highlight & select "current" match
            String text = matchCase?textArea.getText():textArea.getText().toLowerCase();
            int matchOffset = -1;
            switch (direction) {
                case NONE :
                    // No move was requested (e.g. searchText was changed. Cheek if search still matches
                    if (matches(startPosition, searchString, text)) {
                        matchOffset = startPosition;
                    }
                    else {
                        // if check fails (text changed or match case changed), perform forward search
                        matchOffset = text.indexOf(searchString, startPosition);
                        if (matchOffset == -1 && startPosition != 0) {
                            matchOffset = text.indexOf(searchString, 0);
                        }
                    }
                    break;
                case FORWARD:
                    matchOffset = text.indexOf(searchString, (startPosition != -1)?startPosition+1:0);
                    if (matchOffset == -1 && startPosition != 0) {
                        matchOffset = text.indexOf(searchString, 0);
                    }
                    break;
                case BACKWARDS:
                    matchOffset = text.lastIndexOf(searchString, (startPosition != -1)?startPosition-1:text.length());
                    if (matchOffset == -1 && startPosition != text.length()) {
                        matchOffset = text.lastIndexOf(searchString, text.length());
                    }
                    break;
            }

            // Remove previous highlight
            Object currentHighlight = highlights.get(selectedHighlightPosition);
            if (currentHighlight != null) {
                highlighter.removeHighlight(currentHighlight);
                if (highlightAll) {
                    // put back a "non-current" highlight if still valid
                    try {
                        if (matches(selectedHighlightPosition, searchString, text)) {
                            highlights.put(selectedHighlightPosition, highlighter.addHighlight(selectedHighlightPosition, selectedHighlightPosition + searchString.length(), nonSelectedHighlighter));
                        }
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
            // Create new highlight (if match found)
            if (matchOffset > -1) {
                setSelection(matchOffset, searchString.length());
                try {
                    Object oldHighlight = highlights.get(matchOffset);
                    if (oldHighlight != null) {
                        highlighter.removeHighlight(oldHighlight);
                    }
                    highlights.put(matchOffset, highlighter.addHighlight(matchOffset, matchOffset + searchString.length(), selectedHighlighter));
                } catch (BadLocationException e) {
                    // ignore
                }
            }
            else {
                searchTextField.setBackground(Color.PINK);
            }
            selectedHighlightPosition = matchOffset;


            textArea.setEditable(previousEditableStatus);
            textArea.setCursor(previousCursor);

            return matchOffset;
        }
        return selectedHighlightPosition;
    }

    private void setSelection(int offset, int length) {
        textArea.setCaretPosition(offset);
        textArea.moveCaretPosition(offset + length);
    }

    private boolean matches(int position, String searchString, String text) {
        if (position >= 0 && position + searchString.length() <= text.length()) {
            if (matchCaseCheckBox.isSelected()) {
                return text.substring(position, position + searchString.length()).equals((searchString));
            }
            else {
                return text.substring(position, position + searchString.length()).equalsIgnoreCase((searchString));
            }
        }
        return false;
    }


    // Event handlers for keypress on any component of the SearchablePanel

    public void keyTyped(KeyEvent e) { /* do nothing */ }

    public void keyPressed(KeyEvent e) {
        // CTRL-F
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
            searchPanel.setVisible(true); // in case it was hidden
            searchTextField.requestFocusInWindow();
            searchTextField.selectAll();
            if (textArea.getSelectedText() != null && textArea.getSelectedText().length() > 0) {
                performSearch(textArea.getSelectedText(), textArea.getSelectionStart(), SearchDirection.NONE, true);
                searchTextField.setText(textArea.getSelectedText());
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_F3) {
            if (e.isShiftDown()) {
                performSearch(searchTextField.getText(), textArea.getSelectionStart(), SearchDirection.BACKWARDS, false);
            }
            else {
                performSearch(searchTextField.getText(), textArea.getSelectionStart(), SearchDirection.FORWARD, false);
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (searchPanel.isVisible()) {
                searchPanel.setVisible(false);
                textArea.requestFocusInWindow();
            }
            else {
                clearHighlights();
            }
        }
    }

    public void keyReleased(KeyEvent e) { /* do nothing */ }


    //  Event handlers for changes to the text search field
    
    public void insertUpdate(DocumentEvent e) {
        performSearch(searchTextField.getText(), textArea.getSelectionStart(), SearchDirection.NONE, true);
    }

    public void removeUpdate(DocumentEvent e) {
        performSearch(searchTextField.getText(), textArea.getSelectionStart(), SearchDirection.NONE, true);
    }

    public void changedUpdate(DocumentEvent e) {
        performSearch(searchTextField.getText(), textArea.getSelectionStart(), SearchDirection.NONE, true);
    }

    private static class SearchHighlighter implements Callable<java.util.List<Integer>> {
        private Document document;
        java.util.List<Integer> dataOffsets;
        String searchString;
        private boolean matchCase;

        public SearchHighlighter(Document document, String searchString, boolean matchCase) {
            this.document = document;
            this.searchString = searchString;
            this.matchCase = matchCase;
        }

        public java.util.List<Integer> call() throws Exception {
            highlightSearch();
            return dataOffsets;
        }

        private void highlightSearch() {
            List<Integer> lineOffsets = new ArrayList<Integer>();
            dataOffsets = new ArrayList<Integer>();
            Element element = document.getDefaultRootElement();
            int elementCount = element.getElementCount();

            for (int i = 0; i < elementCount; i++) {
                lineOffsets.add(element.getElement(i).getStartOffset());
            }
            lineOffsets.add(element.getElement(element.getElementCount() - 1).getEndOffset());

            int count = 0;
            int lsOffset;
            int leOffset;

            while (count < (lineOffsets.size() - 1)) {
                lsOffset = lineOffsets.get(count);
                leOffset = lineOffsets.get(count + 1);
                count++;
                Segment seg = new Segment();

                try {
                    document.getText(lsOffset, leOffset - lsOffset, seg);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }

                String line = matchCase?seg.toString():seg.toString().toLowerCase();
                int mark = 0;

                while ((mark = line.indexOf(searchString, mark)) > -1) {
                    dataOffsets.add(lsOffset + mark);
                    mark += searchString.length();
                }
            }
        }
    }
}
