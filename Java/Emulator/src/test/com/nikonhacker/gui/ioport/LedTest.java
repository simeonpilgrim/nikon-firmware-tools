package com.nikonhacker.gui.ioport;

import eu.hansolo.custom.SteelCheckBox;
import eu.hansolo.steelseries.extras.Led;
import eu.hansolo.steelseries.tools.LedColor;
import eu.hansolo.steelseries.tools.LedType;
import eu.hansolo.tools.ColorDef;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Original source was from http://harmoniccode.blogspot.be/2010/11/friday-fun-component-iii.html
 */
public class LedTest {
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("SteelCheckBoxTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout());

        final Led led1 = new Led();
        led1.setLedColor(LedColor.RED);
        led1.setLedType(LedType.ROUND);

        final Led led2 = new Led();
        led2.setLedColor(LedColor.RED_LED);
        led2.setLedType(LedType.ROUND);

        final Led led3 = new Led();
        led3.setLedColor(LedColor.ORANGE_LED);
        led3.setLedType(LedType.RECT_VERTICAL);

        final Led led4 = new Led();
        led4.setLedColor(LedColor.GREEN);
        led4.setLedType(LedType.ROUND);


        final SteelCheckBox steelCheckBox = new SteelCheckBox(SwingConstants.VERTICAL);
        steelCheckBox.setColored(true);
        steelCheckBox.setSelectedColor(ColorDef.GREEN);
        steelCheckBox.setRised(false);
        steelCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = steelCheckBox.isSelected();
                led1.setLedOn(selected);
                led2.setLedOn(selected);
                led3.setLedOn(selected);
                led4.setLedOn(selected);
            }
        });

        panel.add(led1, BorderLayout.NORTH);
        panel.add(led2, BorderLayout.SOUTH);
        panel.add(led3, BorderLayout.EAST);
        panel.add(led4, BorderLayout.WEST);

        panel.add(steelCheckBox, BorderLayout.CENTER);

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
