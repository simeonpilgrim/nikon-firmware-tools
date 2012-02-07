package com.nikonhacker.gui.component;

/*
 * Inspired by SwingTextDocumentWriter.java
 * Original author: Herve AGNOUX, hagnoux@mail.club-internet.fr
 * License LGPL.
 */

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;

@Deprecated
public class PrintStreamArea extends JTextArea {
    private AttributeSet attributeSet = new SimpleAttributeSet();

    private Stream out = new Stream();
    private Vector<String> buffer = new Vector<String>();
    private boolean isOpen = true;

    public PrintStreamArea(int rows, int columns) {
        super(rows, columns);
        setEditable(false);
    }

    public void write(char cbuf[], int off, int len) throws IOException
    {
        String s = new String(cbuf, off, len);
        buffer.add(s);
    }

    private void flush() throws IOException {
        if (!isOpen) {
            throw new IOException("Writer closed");
        }
        try
        {
            String ss[];
            final StringBuffer sb;

            Vector v = (Vector)buffer.clone();
            buffer.clear();
            ss = new String[v.size()];
            v.copyInto(ss);
            sb = new StringBuffer();
            for (String s : ss) {
                sb.append(s);
            }
            getDocument().insertString(getDocument().getLength(), sb.toString(), attributeSet);
        }
        catch (BadLocationException ble)
        {
            throw new IOException(ble.toString());
        }
    }

    public void close() throws IOException
    {
        isOpen = false;
    }

    private class Stream extends OutputStream {

        char buf[] = new char[1];

        public void write(int b) throws IOException
        {
            buf[0] = (char)b;
            PrintStreamArea.this.write(buf, 0, 1);
            flush();
        }

        public void write(byte b[], int off, int len) throws IOException
        {
            char c[];

            c = new char[b.length];
            for (int i = off; i < off + len; i++)
                c[i] = (char)b[i];
            PrintStreamArea.this.write(c, off, len);
            flush();
        }

        public void flush() throws IOException
        {
            PrintStreamArea.this.flush();
        }
    }


    public PrintStream getPrintStream() {
        return new PrintStream(out);
    }

}
