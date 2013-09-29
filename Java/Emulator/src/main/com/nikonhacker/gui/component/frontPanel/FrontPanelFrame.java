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
    public String imageDir;

    public FrontPanelFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, final FrontPanel frontPanel, String imageSetName) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        ImagePanel panel = new ImagePanel();

        if (imageSetName == null) imageSetName = "D5100_small"; // temp until a "null" version is available

        imageDir = "images/buttons/" + imageSetName;

        try {
            BufferedImage background = ImageIO.read(EmulatorUI.class.getResource(imageDir + "/background.png"));
            int bgWidth = background.getWidth();
            int bgHeight = background.getHeight();

            panel.setLayout(null);

            panel.setImage(background);

            for (String key : frontPanel.getButtons().keySet()) {
                addButton(panel, imageDir, bgWidth, bgHeight, frontPanel.getButtons().get(key));
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
