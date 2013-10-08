package com.nikonhacker.gui.swing;

import com.nikonhacker.emu.peripherials.frontPanel.FrontPanel;
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

        addButton(panel, FrontPanel.KEY_PLUS_MINUS, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_TIMER, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_UP, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_POWER, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight_off", "highlight_on"}}, false);
        addButton(panel, FrontPanel.KEY_FLASH, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_DOWN, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_LEFT, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_REC, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_DELETE, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_INFO, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_LIVEVIEW, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight_off", "highlight_on"}}, true);
        addButton(panel, FrontPanel.KEY_PLAY, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_MENU, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_ZOOM_OUT, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_ZOOM_IN, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_OK, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_I, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_RIGHT, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);
        addButton(panel, FrontPanel.KEY_AEL_AFL, bgWidth, bgHeight, new String[][]{{null, "on"}, {"highlight", "on"}}, true);

        addButton(panel, FrontPanel.KEY_SHUTTER, bgWidth, bgHeight, new String[][]{{null, "half", "on"}, {"highlight", "highlight_half", "on"}}, false);

        addButton(panel, FrontPanel.KEY_DIAL, bgWidth, bgHeight, new String[][]{{null, null}, {"highlight", "highlight"}}, true);
        addButton(panel, FrontPanel.KEY_MODEDIAL, bgWidth, bgHeight, new String[][]{{"0","1","2","3","4","5","6","7","8","9","10","11","12"}, {"0_highlight","1_highlight","2_highlight","3_highlight","4_highlight","5_highlight","6_highlight","7_highlight","8_highlight","9_highlight","10_highlight","11_highlight","12_highlight"}}, false);
//        addLed(panel, "led", bgWidth, bgHeight);

        frame.add(panel);
        frame.pack();

        frame.setLocation(0,100);
        frame.setVisible(true);
    }

    private static void addButton(JComponent parent, String imgPrefix, int width, int height, String[][] imageSuffixes, boolean isLeftClickTemp) {
        BitmapToggleButton button = new BitmapToggleButton(parent, IMG_DIR, imgPrefix, width, height, imageSuffixes, isLeftClickTemp, 0);
        parent.add(button);
        button.setBounds(0, 0, width, height);
    }

}
