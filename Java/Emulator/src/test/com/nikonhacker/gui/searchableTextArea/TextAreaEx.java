package com.nikonhacker.gui.searchableTextArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

class TextAreaEx extends JFrame implements ActionListener, KeyListener {
    JButton button1;
    JTextArea tx = new JTextArea();

    int startFrom = 0;
    int offset = 0;
    String find = "";
    String text = "";

    TextAreaEx() {
        super("My Frame");
        FileInputStream fis = null;
        StringBuffer sb = new StringBuffer();
        try {
            fis = new FileInputStream("Test.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String h = "";
            while ((h = br.readLine()) != null) {
                sb.append(h + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        text = sb.toString();
        tx.setText(text);
        text = text.toLowerCase();
        button1 = new JButton("Find");
        button1.addActionListener(this);
        getContentPane().add(button1, BorderLayout.PAGE_START);
        button1.setFocusable(false);
        JScrollPane p1 = new JScrollPane(tx);
        getContentPane().add(p1);
        JFrame.setDefaultLookAndFeelDecorated(true);
        tx.addKeyListener(this);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(400, 300);
        setVisible(true);
    }

    public static void main(String s[]) {
        new TextAreaEx();
    }

    public void actionPerformed(ActionEvent e) {
        startFrom = 0;
        offset = 0;
        if (e.getSource() == button1) {
            find = (String) JOptionPane.showInputDialog(this, "FIND:\n", "Find", JOptionPane.INFORMATION_MESSAGE, null, null, null);
            find = find.toLowerCase();
            findWord();
        }
    }

    public void findWord() {
        offset = text.indexOf(find, startFrom);
        if (offset > -1) {
            tx.setFocusable(true);
            tx.select(offset, find.length() + offset);
            startFrom = find.length() + offset + 1;
        }
        else JOptionPane.showMessageDialog(this, "No (more) matches");
    }

    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_F3) {
            findWord();
        }
    }

    public void keyReleased(KeyEvent ke) {
    }

    public void keyTyped(KeyEvent ke) {
    }
}