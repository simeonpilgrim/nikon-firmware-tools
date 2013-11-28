package com.nikonhacker.gui.ioport;

import eu.hansolo.custom.SteelCheckBox;
import eu.hansolo.tools.ColorDef;

import javax.swing.*;
import java.awt.*;

/**
 * Original source was from http://harmoniccode.blogspot.be/2010/11/friday-fun-component-iii.html
 */
public class SteelCheckBoxTest {
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("SteelCheckBoxTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout());


        JButton btn1 = new JButton("200 x 200");
        btn1.setPreferredSize(new Dimension(200, 200));

        JButton btn2 = new JButton("200 x 200");
        btn2.setPreferredSize(new Dimension(200, 200));

        JButton btn3 = new JButton("200 x 200");
        btn3.setPreferredSize(new Dimension(200, 200));

        JButton btn4 = new JButton("200 x 200");
        btn4.setPreferredSize(new Dimension(200, 200));

        SteelCheckBox steelCheckBox1 = new SteelCheckBox();
        steelCheckBox1.setColored(true);
        steelCheckBox1.setSelectedColor(ColorDef.GREEN);
        steelCheckBox1.setRised(false);

        SteelCheckBox steelCheckBox2 = new SteelCheckBox();
        steelCheckBox2.setColored(true);
        steelCheckBox2.setSelectedColor(ColorDef.GREEN);
        steelCheckBox2.setRised(false);
        steelCheckBox2.setText("Horizontal");

        SteelCheckBox steelCheckBox3 = new SteelCheckBox(SwingConstants.VERTICAL);
        steelCheckBox3.setColored(true);
        steelCheckBox3.setSelectedColor(ColorDef.GREEN);
        steelCheckBox3.setRised(false);

        SteelCheckBox steelCheckBox4 = new SteelCheckBox(SwingConstants.VERTICAL);
        steelCheckBox4.setColored(true);
        steelCheckBox4.setSelectedColor(ColorDef.GREEN);
        steelCheckBox4.setRised(false);
        steelCheckBox4.setText("Vertical");

        panel.add(btn1, BorderLayout.NORTH);
        panel.add(btn2, BorderLayout.SOUTH);
        panel.add(btn3, BorderLayout.EAST);
        panel.add(btn4, BorderLayout.WEST);

//        panel.add(steelCheckBox1, BorderLayout.CENTER);
//        panel.add(steelCheckBox2, BorderLayout.CENTER);
        panel.add(steelCheckBox3, BorderLayout.CENTER);
//        panel.add(steelCheckBox4, BorderLayout.CENTER);

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
