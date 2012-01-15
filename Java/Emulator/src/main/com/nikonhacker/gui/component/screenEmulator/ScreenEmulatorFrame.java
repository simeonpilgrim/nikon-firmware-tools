package com.nikonhacker.gui.component.screenEmulator;

import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ScreenEmulatorFrame extends DocumentFrame {

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private AffineTransform resizeTransform;
    private int previousW, previousH;

    private DebuggableMemory memory;
    private int start;
    int screenHeight, screenWidth;

    BufferedImage img;

    private Timer _timer;

    public ScreenEmulatorFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, DebuggableMemory memory, int start, int end, int screenWidth, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.memory = memory;
        this.start = start;
        this.screenWidth = screenWidth;
        this.screenHeight = (end - start)/ (3 * screenWidth);

        img = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_3BYTE_BGR);

        getContentPane().add(new ScreenEmulatorComponent());

        // Start update timer
        _timer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        _timer.start();
    }

    public void dispose() {
        _timer.stop();
        _timer = null;
        super.dispose();
    }

    private class ScreenEmulatorComponent extends JComponent {
        private ScreenEmulatorComponent() {
            super();
            setPreferredSize(new Dimension(screenWidth, screenHeight));
        }

        // This method is called whenever the contents needs to be painted
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            int w = getWidth();
            int h = getHeight();

            // Create the resizing transform upon first call or resize
            if (resizeTransform == null || previousW != w || previousH != h) {
                resizeTransform = new AffineTransform();
                double scaleX = Math.max((double) w / screenWidth, 1);
                double scaleY = Math.max((double) h / screenHeight, 1);
                resizeTransform.scale(scaleX, scaleY);
                previousW = w;
                previousH = h;
            }

            int yOffset = start;
            int off;
            Object pixel = null;

            for (int y = 0; y < screenHeight; y++, yOffset+= screenWidth) {
                off = yOffset;
                for (int x = 0; x < screenWidth; x++) {
                    pixel = img.getColorModel().getDataElements(memory.loadUnsigned16(off++, false), pixel);
                    img.getRaster().setDataElements(x, y, pixel);
                }
            }

            g2d.drawImage(img, resizeTransform, null);
        }
    }
}
