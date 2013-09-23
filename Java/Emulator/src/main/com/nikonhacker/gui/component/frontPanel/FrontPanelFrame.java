package com.nikonhacker.gui.component.frontPanel;

import com.nikonhacker.emu.peripherials.frontPanel.CameraButton;
import com.nikonhacker.emu.peripherials.frontPanel.FrontPanel;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.BitmapToggleButton;
import com.nikonhacker.gui.swing.DocumentFrame;
import com.nikonhacker.gui.swing.ImagePanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FrontPanelFrame extends DocumentFrame {
    // TODO make this base path dynamic according to model
    public static final String IMG_DIR = "images/D5100buttons";

    public FrontPanelFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, final FrontPanel frontPanel) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        ImagePanel panel = new ImagePanel();

        try {
            BufferedImage background = ImageIO.read(EmulatorUI.class.getResource(IMG_DIR + "/background.png"));
            int bgWidth = background.getWidth();
            int bgHeight = background.getHeight();

            panel.setLayout(null);

            panel.setImage(background);

              // TODO add this help to Background image
//            System.out.println("Help:");
//            System.out.println("Standard buttons: left mouse button = click (or release already pressed), right mouse button = toggle");
//            System.out.println("Off/On buttons: any mouse button = toggle");
//            System.out.println("Dials: left mouse button = rotate clockwise, right mouse button = rotate anticlockwise");
//            System.out.println("Shutter: left mouse button = none > half > full, right mouse button = none < half < full");

            for (String key : frontPanel.getButtons().keySet()) {
                addButton(panel, IMG_DIR, bgWidth, bgHeight, frontPanel.getButtons().get(key));
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot find front panel's background image");
        }

        getContentPane().add(panel);
    }

    private void addButton(final JComponent panel, String imgDir, int width, int height, final CameraButton cameraButton) {
        BitmapToggleButton button = new BitmapToggleButton(panel, imgDir, cameraButton.getKey(), width, height, cameraButton.getImageSuffixes(), cameraButton.isLeftClickTemp());
        button.setBounds(0, 0, width, height);
        if (cameraButton.isReversed()) {
            button.setState(1 - cameraButton.getPin().getOutputValue(), false);
            button.addStateChangeListener(new StateChangeAdapter() {
                @Override
                public void onStateChange(int state) {
                    cameraButton.getPin().setOutputValue(1 - state);
                }
            });
        }
        else {
            button.setState(cameraButton.getPin().getOutputValue(), false);
            button.addStateChangeListener(new StateChangeAdapter() {
                @Override
                public void onStateChange(int state) {
                    cameraButton.getPin().setOutputValue(state);
                }
            });
        }
        panel.add(button);
    }
}
