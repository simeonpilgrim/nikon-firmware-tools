package com.nikonhacker.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class BrowsePath extends JFrame implements ActionListener {

    JButton button;
    JTextField field;

    public BrowsePath() {
        this.setLayout(null);

        button = new JButton("browse");
        field = new JTextField();

        field.setBounds(30, 50, 200, 25);
        button.setBounds(240, 50, 100, 25);
        this.add(field);
        this.add(button);

        button.addActionListener(this);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {
        Chooser frame = new Chooser();
        field.setText(frame.fileName);
    }

    public static void main(String args[]) {

        BrowsePath frame = new BrowsePath();
        frame.setSize(400, 300);
        frame.setLocation(200, 100);
        frame.setVisible(true);
    }

    class Chooser extends JFrame {

        JFileChooser chooser;
        String fileName;

        public Chooser() {
            chooser = new JFileChooser();
            int r = chooser.showOpenDialog(new JFrame());
            if (r == JFileChooser.APPROVE_OPTION) {
                fileName = chooser.getSelectedFile().getPath();
            }
        }
    }
}

