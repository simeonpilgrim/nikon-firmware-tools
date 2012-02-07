package com.nikonhacker.gui.component;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import java.io.IOException;
import java.io.Writer;

public class PrintWriterArea extends JTextArea {
    private AttributeSet attributeSet = new SimpleAttributeSet();

    private InternalWriter writer = new InternalWriter();

    public PrintWriterArea(int rows, int columns) {
        super(rows, columns);
        setEditable(false);
    }


    public Writer getWriter() {
        return writer;
    }


    public void write(char cbuf[], int off, int len) throws IOException {
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
