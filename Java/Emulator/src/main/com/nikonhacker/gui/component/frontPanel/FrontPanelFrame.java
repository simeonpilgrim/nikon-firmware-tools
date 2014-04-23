package com.nikonhacker.gui.component.frontPanel;

import com.nikonhacker.emu.peripherials.frontPanel.CameraButton;
import com.nikonhacker.emu.peripherials.frontPanel.CameraLed;
import com.nikonhacker.emu.peripherials.frontPanel.FrontPanel;
import com.nikonhacker.emu.peripherials.frontPanel.LedStateChangeListener;
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

    private final FrontPanel frontPanel;

    public FrontPanelFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, final FrontPanel frontPanel, String imageSetName) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        ImagePanel panel = new ImagePanel();

        // default to "D5100_small" until a "default" version (with standard JButtons) is available
        if (imageSetName == null) imageSetName = "D5100_small";

        imageDir = "images/buttons/" + imageSetName;
        this.frontPanel = frontPanel;

        try {
            BufferedImage background = ImageIO.read(EmulatorUI.class.getResource(imageDir + "/background.png"));
            int bgWidth = background.getWidth();
            int bgHeight = background.getHeight();

            panel.setLayout(null);

            panel.setImage(background);

            for (String key : frontPanel.getButtons().keySet()) {
                addBitmapButton(panel, imageDir, bgWidth, bgHeight, frontPanel.getButtons().get(key));
            }

            addLed(panel, imageDir, bgWidth, bgHeight, frontPanel.getLed(FrontPanel.KEY_CARDLED));

        } catch (IOException e) {
            throw new RuntimeException("Cannot find image " + e.getMessage());
        }

        getContentPane().add(panel);
    }

    private void addBitmapButton(final JComponent panel, String imgDir, int width, int height, final CameraButton cameraButton) {
        BitmapToggleButton button = new BitmapToggleButton(panel, imgDir, cameraButton.getKey(), width, height, cameraButton.getImageSuffixes(), cameraButton.isLeftClickTemp(), cameraButton.getState());
        button.setBounds(0, 0, width, height);
        button.addStateChangeListener(new StateChangeAdapter () {
            @Override
            public void onStateChange(int state) {
                cameraButton.setState(state);
            }
        });
        panel.add(button);
    }

    private void addLed(final JComponent panel, String imgDir, int width, int height, final CameraLed cameraLed) throws IOException {
        // LED doesn't react on mouse, so enough static JLabel
        final ImageIcon imageIcon = new ImageIcon(EmulatorUI.class.getResource(imageDir + "/" + cameraLed.getKey() + "_" + cameraLed.getImageSuffixes()[0][1] + ".png"));
        final JLabel label = new JLabel();
        label.setOpaque(false);
        label.setBounds(0, 0, width, height);
        cameraLed.registerListener(new LedStateChangeListener() {
            @Override
            public void onValueChange(int newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run(){
                        Integer value = cameraLed.getPin().getInputValue();
                        if (value==null) {
                            value = 0;
                        }
                        if (value==0) {
                            label.setIcon(null);
                        } else {
                            label.setIcon(imageIcon);
                        }
                    }
                });
            }
        });
        panel.add(label);
    }

    public void dispose() {
        frontPanel.getLed(FrontPanel.KEY_CARDLED).unregisterListener();
        super.dispose();
    }
}
