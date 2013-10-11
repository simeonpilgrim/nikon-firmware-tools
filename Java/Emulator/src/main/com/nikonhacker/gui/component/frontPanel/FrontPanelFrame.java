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

/**
 * This is a generic camera front panel UI that renders a FrontPanel model
 */
public class FrontPanelFrame extends DocumentFrame {
    public String imageDir;

    public FrontPanelFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, final FrontPanel frontPanel, String imageSetName) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        ImagePanel panel = new ImagePanel();

        // default to "D5100_small" until a "default" version (with standard JButtons) is available
        if (imageSetName == null) imageSetName = "D5100_small";

        imageDir = "images/buttons/" + imageSetName;

        try {
            BufferedImage background = ImageIO.read(EmulatorUI.class.getResource(imageDir + "/background.png"));
            int bgWidth = background.getWidth();
            int bgHeight = background.getHeight();

            panel.setLayout(null);

            panel.setImage(background);

            for (String key : frontPanel.getButtons().keySet()) {
                addBitmapButton(panel, imageDir, bgWidth, bgHeight, frontPanel.getButtons().get(key));
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot find front panel's background image");
        }

        getContentPane().add(panel);
    }

    private void addBitmapButton(final JComponent panel, String imgDir, int width, int height, final CameraButton cameraButton) {
        BitmapToggleButton button = new BitmapToggleButton(panel, imgDir, cameraButton.getKey(), width, height, cameraButton.getImageSuffixes(), cameraButton.isLeftClickTemp(), cameraButton.getState());
        button.setBounds(0, 0, width, height);
        button.addStateChangeListener(new StateChangeAdapter() {
            @Override
            public void onStateChange(int state) {
                cameraButton.setState(state);
            }
        });
        panel.add(button);
    }
}
