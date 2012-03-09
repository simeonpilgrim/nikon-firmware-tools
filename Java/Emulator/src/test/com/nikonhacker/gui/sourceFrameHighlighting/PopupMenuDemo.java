package com.nikonhacker.gui.sourceFrameHighlighting;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A simple example showing how to use RSyntaxTextArea to add Java syntax
 * highlighting to a Swing application.<p>
 *
 * This example uses RSyntaxTextArea 2.0.1.<p>
 *
 * Project Home: http://fifesoft.com/rsyntaxtextarea<br>
 * Downloads: https://sourceforge.net/projects/rsyntaxtextarea
 */
public class PopupMenuDemo extends JFrame {

    private static final long serialVersionUID = 1L;

    private RSyntaxTextArea textArea;

    public PopupMenuDemo() {

        JPanel cp = new JPanel(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        textArea.setAntiAliasingEnabled(true);

        // Add an item to the popup menu that opens the file whose name is
        // specified at the current caret position.
        JPopupMenu popup = textArea.getPopupMenu();
        popup.addSeparator();
        popup.add(new JMenuItem(new OpenFileAction()));
        RTextScrollPane sp = new RTextScrollPane(textArea);
        cp.add(sp);

        setContentPane(cp);
        setTitle("Popup Menu Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

    }

    /**
     * Loads a file's contents into the text area, or displays an error message
     * if the file does not exist.
     *
     * @param file
     *           The file to load.
     */
    public void loadFile(File file) {

        System.out.println("DEBUG: " + file.getAbsolutePath());
        if (file.isDirectory()) { // Clicking on a space character
            JOptionPane.showMessageDialog(this, file.getAbsolutePath()
                    + " is a directory", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } else if (!file.isFile()) {
            JOptionPane.showMessageDialog(this, "No such file: "
                    + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            textArea.read(r, null);
            r.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            UIManager.getLookAndFeel().provideErrorFeedback(textArea);
        }

    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PopupMenuDemo().setVisible(true);
            }
        });
    }

    /**
     * An action that gets the filename at the current caret position and tries
     * to open that file. If there is a selection, it uses the selected text as
     * the filename.
     */
    private class OpenFileAction extends TextAction {

        public OpenFileAction() {
            super("Open File");
        }

        public void actionPerformed(ActionEvent e) {

            JTextComponent tc = getTextComponent(e);
            String filename = null;

            // Get the name of the file to load. If there is a selection, use
            // that as the file name, otherwise, scan for a filename around
            // the caret.
            try {
                int selStart = tc.getSelectionStart();
                int selEnd = tc.getSelectionEnd();
                if (selStart != selEnd) {
                    filename = tc.getText(selStart, selEnd - selStart);
                } else {
                    filename = getFilenameAtCaret(tc);
                }
            } catch (BadLocationException ble) {
                ble.printStackTrace();
                UIManager.getLookAndFeel().provideErrorFeedback(tc);
                return;
            }

            loadFile(new File(filename));

        }

        /**
         * Gets the filename that the caret is sitting on. Note that this is a
         * somewhat naive implementation and assumes filenames do not contain
         * whitespace or other "funny" characters, but it will catch most common
         * filenames.
         *
         * @param tc
         *           The text component to look at.
         * @return The filename at the caret position.
         * @throws BadLocationException
         *            Shouldn't actually happen.
         */
        public String getFilenameAtCaret(JTextComponent tc) throws BadLocationException {
            int caret = tc.getCaretPosition();
            int start = caret;
            Document doc = tc.getDocument();
            while (start > 0) {
                char ch = doc.getText(start - 1, 1).charAt(0);
                if (isFilenameChar(ch)) {
                    start--;
                } else {
                    break;
                }
            }
            int end = caret;
            while (end < doc.getLength()) {
                char ch = doc.getText(end, 1).charAt(0);
                if (isFilenameChar(ch)) {
                    end++;
                } else {
                    break;
                }
            }
            return doc.getText(start, end - start);
        }

        public boolean isFilenameChar(char ch) {
            return Character.isLetterOrDigit(ch) || ch == ':' || ch == '.'
                    || ch == File.separatorChar;
        }

    }

}
