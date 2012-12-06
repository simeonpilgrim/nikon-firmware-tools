package com.nikonhacker.gui.sourceFrameHighlighting;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

/**
 * A simple example showing how to use RSyntaxTextArea to add Java syntax
 * highlighting to a Swing application.<p>
 *
 * This example uses RSyntaxTextArea 2.0.1.<p>
 *
 * Project Home: http://fifesoft.com/rsyntaxtextarea<br>
 * Downloads: https://sourceforge.net/projects/rsyntaxtextarea
 */
public class TextEditorDemo extends JFrame {

    private static final long serialVersionUID = 1L;

    public TextEditorDemo() {

        JPanel cp = new JPanel(new BorderLayout());

        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        RSyntaxTextAreaHighlighter rSyntaxTextAreaHighlighter = new RSyntaxTextAreaHighlighter();
        textArea.setHighlighter(rSyntaxTextAreaHighlighter);
        textArea.setText("//test\nint main() {\n\tSystem.out.println();\n}\n");

        try {
            Object tag = textArea.addLineHighlight(3, Color.CYAN);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        sp.setIconRowHeaderEnabled(true);
        sp.setFoldIndicatorEnabled(false);
        Gutter gutter = sp.getGutter();
        gutter.setBookmarkIcon(new ImageIcon("bookmarkIcon.png"));
        gutter.setBookmarkingEnabled(true);

        try {
            gutter.addLineTrackingIcon(2, new ImageIcon("breakpointIcon.png"));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        cp.add(sp);

        setContentPane(cp);
        setTitle("Text Editor Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TextEditorDemo().setVisible(true);
            }
        });
    }

}