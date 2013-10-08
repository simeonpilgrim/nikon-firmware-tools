package com.nikonhacker.gui.component.image;

import com.nikonhacker.gui.swing.BackgroundImagePanel;

import javax.swing.*;
import java.awt.*;

public class TestBackgroundImage extends JFrame {
    TestBackgroundImage() {
        BackgroundImagePanel backgroundImagePanel = new BackgroundImagePanel(Toolkit.getDefaultToolkit().getImage(TestBackgroundImage.class.getResource("nh_full.jpg")));
        add(backgroundImagePanel);
        setSize(500, 300);
    }

    public static void main(String[] args) {
        TestBackgroundImage jrFrame = new TestBackgroundImage();
        jrFrame.setVisible(true);
    }
}

