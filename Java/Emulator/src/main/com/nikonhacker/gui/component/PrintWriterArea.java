package com.nikonhacker.gui.component;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * A JTextArea exposing a Writer to be used for any output or logging
 */
public class PrintWriterArea extends JTextArea {
    private AttributeSet attributeSet = new SimpleAttributeSet();

    private InternalWriter writer = new InternalWriter();

    /**
     * Constructs a new empty PrintWriterArea with the specified number of
     * rows and columns.
     * By default, the area is not editable
     *
     * @param rows the number of rows >= 0
     * @param columns the number of columns >= 0
     * @exception IllegalArgumentException if the rows or columns
     *  arguments are negative.
     */
    public PrintWriterArea(int rows, int columns) {
        super(rows, columns);
        setEditable(false);
    }

    /**
     * Returns a PrintWriter to write to the area
     * @return
     */
    public PrintWriter getPrintWriter() {
        return new PrintWriter(writer);
    }

    public void setAutoScroll(boolean isAutoScroll) {
        DefaultCaret caret = (DefaultCaret)this.getCaret();
        caret.setUpdatePolicy(isAutoScroll?DefaultCaret.ALWAYS_UPDATE:DefaultCaret.NEVER_UPDATE);
    }


    private void write(char cbuf[], int off, int len) throws IOException {
        try {
            getDocument().insertString(getDocument().getLength(), new String(cbuf, off, len), attributeSet);
        } catch (BadLocationException e) {
            throw new IOException(e);
        }
    }


    private void flush() throws IOException {
        // noop
    }


    public void close() throws IOException
    {
        // noop
    }


    public void clear() throws IOException
    {
        try {
            getDocument().remove(0, getDocument().getLength());
        } catch (BadLocationException e) {
            throw new IOException(e);
        }
    }


    private class InternalWriter extends Writer {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            PrintWriterArea.this.write(cbuf, off, len);
        }
        public void flush() throws IOException {
            PrintWriterArea.this.flush();
        }
        @Override
        public void close() throws IOException {
            PrintWriterArea.this.close();
        }
    }
}
