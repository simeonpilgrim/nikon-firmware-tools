package com.nikonhacker.gui.swing;

import com.nikonhacker.gui.EmulatorUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class BitmapToggleButtonTest {
    private static final String IMG_DIR = "images/buttons/D5100_large";

    public static void main(String[] args) throws Exception {
        javax.swing.JFrame frame = new javax.swing.JFrame("Custom Shape Button");
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        ImagePanel panel = new ImagePanel();
        BufferedImage background = ImageIO.read(EmulatorUI.class.getResource(IMG_DIR + "/background.png"));
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();

        panel.setLayout(null);

        panel.setImage(background);

        System.out.println("Help:");
        System.out.println("Standard buttons: left mouse button = click (or release already pressed), right mouse button = toggle");
        System.out.println("Off/On buttons: any mouse button = toggle");
        System.out.println("Dials: left mouse button = rotate clockwise, right mouse button = rotate anticlockwise");
        System.out.println("Shutter: left mouse button = none > half > full, right mouse button = none < half < full");

        addButton(panel, "+-", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "timer", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "up", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "power", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight_off", "highlight_on"}}, false);
        addButton(panel, "flash", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "down", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "left", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "rec", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "delete", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "info", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "liveview", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight_off", "highlight_on"}}, true);
        addButton(panel, "play", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "menu", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "zoomout", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "zoomin", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "ok", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "i", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "right", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, "aelafl", bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);

        addButton(panel, "shutter", bgWidth, bgHeight, new String[][]{{null, "half", "on"}, {"highlight", "highlight_half", "on"}}, false);

        addButton(panel, "dial", bgWidth, bgHeight, new String[][]{{null, null}, {"highlight", "highlight"}}, true);
        addButton(panel, "modedial", bgWidth, bgHeight, new String[][]{{"0","1","2","3","4","5","6","7","8","9","10","11","12"}, {"0_highlight","1_highlight","2_highlight","3_highlight","4_highlight","5_highlight","6_highlight","7_highlight","8_highlight","9_highlight","10_highlight","11_highlight","12_highlight"}}, false);
//        addLed(panel, "led", bgWidth, bgHeight);

        frame.add(panel);
        frame.pack();

        frame.setLocation(0,100);
        frame.setVisible(true);
    }

    private static void addButton(JComponent parent, String imgPrefix, int width, int height, String[][] imageSuffixes, boolean isLeftClickTemp) {
        BitmapToggleButton button = new BitmapToggleButton(parent, IMG_DIR, imgPrefix, width, height, imageSuffixes, isLeftClickTemp);
        parent.add(button);
        button.setBounds(0, 0, width, height);
    }

}
