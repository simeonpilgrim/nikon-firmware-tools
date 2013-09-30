package com.nikonhacker.gui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JToggleButtonsTest {
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("JToggleButtonsTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new FlowLayout());

        final JButton button = new JButton("JButton");
        panel.add(button);

        final JToggleButton toggleButton = new JToggleButton("JToggleButton");
        panel.add(toggleButton);

        final JRightClickableButton rightClickableButton = new JRightClickableButton("JRightClickableButton");
        panel.add(rightClickableButton);

        final JToggleableButton toggleableButton = new JToggleableButton("best JToggleableButton");
        panel.add(toggleableButton);
        toggleableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e);
            }
        });

        frame.add(panel);

        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }


    public static void main(String args[]) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }


}
