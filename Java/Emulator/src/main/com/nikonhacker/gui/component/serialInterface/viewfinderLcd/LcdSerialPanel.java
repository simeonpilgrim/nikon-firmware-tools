package com.nikonhacker.gui.component.serialInterface.viewfinderLcd;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.emu.peripherials.serialInterface.lcd.LcdDriver;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.serialInterface.RxTxSerialPanel;
import com.nikonhacker.gui.component.serialInterface.SerialDevicePanel;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * This file is part of NikonEmulator, a NikonHacker.com project.
 */
public class LcdSerialPanel extends SerialDevicePanel {
    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    public static final String VIEWFINDER_LCD_IMG_BASE_PATH = "images/viewfinder_lcd";

    private final RxTxSerialPanel rxTxSerialPanel;
    private LcdDriver lcdDriver;
    private final ImagePanel imagePanel = new ImagePanel();
    private final JTextField lcdValuesTextField;
    private final JTextField manualValuesTextField;
    private final JPanel lcdPanel;

    private byte[] lcdValues;
    private byte[] manualValues;

    private Timer refreshTimer;

    /**
     * This array contains the black background [0][0]
     * as well as the image representing each segment [byte][bit]
     */
    private final BufferedImage segmentImages[][] = new BufferedImage[15][8];

    public LcdSerialPanel(final LcdDriver lcdDriver, EmulatorUI ui) {
        super();
        this.lcdDriver = lcdDriver;
        this.lcdValues = Arrays.copyOf(lcdDriver.getValues(), lcdDriver.getValues().length);
        this.manualValues = Arrays.copyOf(lcdDriver.getValues(), lcdDriver.getValues().length);

        final JTabbedPane tabbedPane = new JTabbedPane();

        final JCheckBox autoRefreshCheckBox = new JCheckBox("Auto-refresh");

        // LCD viewer
        lcdPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new MigLayout("fillx", "[][grow][]"));

        ActionListener refreshAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoRefreshCheckBox.setSelected(false);
                updateManualArrayFromTextField();
                renderImage();
            }
        };

        topPanel.add(new JLabel("Values received:"));

        lcdValuesTextField = new JTextField("");
        topPanel.add(lcdValuesTextField, "grow");
        lcdValuesTextField.setEnabled(false);
        updateTextFieldFromArray(lcdValuesTextField, lcdValues);

        autoRefreshCheckBox.setSelected(true);
        autoRefreshCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (autoRefreshCheckBox.isSelected()) {
                    manualValues = Arrays.copyOf(lcdValues, lcdValues.length);
                    updateTextFieldFromArray(manualValuesTextField, manualValues);
                    renderImage();
                }
            }
        });
        topPanel.add(autoRefreshCheckBox, "wrap");

        topPanel.add(new JLabel("Values rendered:"));
        manualValuesTextField = new JTextField("");
        topPanel.add(manualValuesTextField, "grow");
        manualValuesTextField.addActionListener(refreshAction);
        updateTextFieldFromArray(manualValuesTextField, manualValues);

        JButton refreshButton = new JButton("Render");
        topPanel.add(refreshButton, "wrap");
        refreshButton.addActionListener(refreshAction);

        JPanel bottomPanel = new JPanel();
        JButton saveButton = new JButton("Save image...");
        saveButton.setToolTipText("Save rendered image to file");
        bottomPanel.add(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });

        lcdPanel.add(topPanel, BorderLayout.NORTH);

        lcdPanel.add(imagePanel, BorderLayout.CENTER);

        lcdPanel.add(bottomPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Contents", lcdPanel);

        imagePanel.addComponentListener(new ComponentListener() {
            private Timer recalculateTimer = new Timer(40, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        prepareSegments(imagePanel.getWidth(), imagePanel.getHeight());
                        renderImage();
                    }
            });

            @Override
            public void componentResized(ComponentEvent e){
                if ( recalculateTimer.isRunning() ){
                    recalculateTimer.restart();
                } else {
                    recalculateTimer.setRepeats(false);
                    recalculateTimer.start();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        // Standard serial spy panel
        rxTxSerialPanel = new RxTxSerialPanel(lcdDriver);
        tabbedPane.add("Rx/Tx interface", rxTxSerialPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        // Start update timer
        refreshTimer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!Arrays.equals(lcdValues, lcdDriver.getValues())) {
                    // Values of the actual LCD driver are copied to local cache
                    lcdValues = Arrays.copyOf(lcdDriver.getValues(), lcdDriver.getValues().length);
                    // LCD Values are reflected in first TextField
                    updateTextFieldFromArray(lcdValuesTextField, lcdValues);
                    if (autoRefreshCheckBox.isSelected()) {
                        // Values of the actual LCD driver are also copied to manually editable cache
                        manualValues = Arrays.copyOf(lcdDriver.getValues(), lcdDriver.getValues().length);
                        // Manual Values are reflected in second TextField
                        updateTextFieldFromArray(manualValuesTextField, manualValues);
                        // LCD image is recomputed
                        renderImage();
                    }
                }
            }
        });
        refreshTimer.start();
    }

    private void updateTextFieldFromArray(JTextField textField, byte[] values) {
        String s ="";
        for (byte value : values) {
            s += "0x" + Format.asHex(value, 2) + " ";
        }
        textField.setText(s.trim());
    }

    private void updateManualArrayFromTextField() {
        String[] values = manualValuesTextField.getText().trim().split("[\\s,]+");
        int byteNumber = 0;
        for (String value : values) {
            try {
                manualValues[byteNumber] = (byte) (Format.parseUnsigned(value) & 0xFF);
            } catch (ParsingException e) {
                System.err.println("Cannot parse value: " + value);
            }
            byteNumber++;
            if (byteNumber == manualValues.length) {
                System.err.println("Too many values to render in LCD. Ignoring the " + manualValues.length + "th byte onwards");
                return;
            }
        }
    }

    /**
     * Render manualValues array to image
     */
    private void renderImage() {
        BufferedImage bgImage = segmentImages[0][0];

        // create the new image
        int w = bgImage.getWidth();
        int h = bgImage.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // paint bg image
        Graphics g = result.getGraphics();
        g.drawImage(bgImage, 0, 0, null);

        int byteNumber = 0;
        for (byte bValue : manualValues) {
            if (byteNumber > 0 && byteNumber < 14) { // Ignore bytes 0 and 14
                for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
                    if (Format.isBitSet(bValue, bitNumber)) {
                        // overlay image of corresponding segment, preserving the alpha channel
                        BufferedImage img = segmentImages[byteNumber][bitNumber];
                        if (img != null) {
                            g.drawImage(img, 0, 0, null);
                        }
                    }
                }
            }
            byteNumber += 1;
        }
        imagePanel.setImage(result);
        imagePanel.repaint();
    }

    private void prepareSegments(double windowWidth, double windowHeight) {
        // Background
        String bgImageFilename = VIEWFINDER_LCD_IMG_BASE_PATH + "/off.png";
        try {
            segmentImages[0][0] = ImageIO.read(EmulatorUI.class.getResource(bgImageFilename));
            int bgWidth = segmentImages[0][0].getWidth();
            int bgHeight = segmentImages[0][0].getHeight();

            double hRatio = windowWidth/bgWidth;
            double vRatio = windowHeight/bgHeight;
            double minRatio = Math.min(hRatio, vRatio);

            if (minRatio < 1) {
                // reduce
                segmentImages[0][0] = resizeImage(segmentImages[0][0], (int)(bgWidth * minRatio), (int)(bgHeight * minRatio));
            }

            for (int byteNumber = 1; byteNumber < 14; byteNumber++) {
                for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
                    String overlayImageFilename = VIEWFINDER_LCD_IMG_BASE_PATH + "/" + byteNumber + "_" + bitNumber + ".png";
                    try {
                        BufferedImage overlayImage = ImageIO.read(EmulatorUI.class.getResource(overlayImageFilename));
                        if (overlayImage.getWidth() != bgWidth || overlayImage.getHeight() != bgHeight) {
                            System.err.println("Wrong file size for " + overlayImageFilename + ": " + overlayImage.getWidth() + "x" + overlayImage.getHeight() + " while background is " + bgWidth + "x" + bgHeight);
                        }
                        else {
                            segmentImages[byteNumber][bitNumber] = overlayImage;
                            if (minRatio < 1) {
                                // reduce
                                segmentImages[byteNumber][bitNumber] = resizeImage(segmentImages[byteNumber][bitNumber], (int)(bgWidth * minRatio), (int)(bgHeight * minRatio));
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading file: " + overlayImageFilename);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load LCD background image " + bgImageFilename);
        }
    }

    private BufferedImage resizeImage(BufferedImage original, int newWidth, int newHeight) {
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, 0, 0, original.getWidth(), original.getHeight(), null);
        g.dispose();
        return resized;
    }

    private void save() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Image file (*.png)", "png"));
        fc.setCurrentDirectory(new File("."));

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fc.getSelectedFile();
                if (!file.exists() || JOptionPane.showConfirmDialog(this, "Do you want to overwrite\n" + file.getAbsolutePath() + " ?", "File exists", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    BufferedImage image = imagePanel.getImage();
                    if (image  == null) {
                        JOptionPane.showMessageDialog(this, "Error saving image to file: image is null", "Save error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        ImageIO.write(image, "PNG", file);
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving image to file: " + e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public void dispose() {
        rxTxSerialPanel.dispose();
    }


    static class ImagePanel extends JPanel {
        BufferedImage image;

        ImagePanel() {
        }

        ImagePanel(BufferedImage image) {
            this.image = image;
        }

        BufferedImage getImage() {
            return image;
        }

        void setImage(BufferedImage image) {
            this.image = image;
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            if(image != null){
                g.drawImage(image, 0, 0, this);
            }
        }
    }

}
