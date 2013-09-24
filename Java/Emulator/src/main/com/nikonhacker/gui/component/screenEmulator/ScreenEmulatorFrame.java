package com.nikonhacker.gui.component.screenEmulator;

import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ScreenEmulatorFrame extends DocumentFrame implements ActionListener {

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private AffineTransform resizeTransform;
    private int previousW, previousH;

    private DebuggableMemory memory;
    private int yStart, uStart, vStart;
    private int screenHeight, screenWidth;

    private BufferedImage img;

    private Timer refreshTimer;
    private final JTextField yAddressField, uAddressField, vAddressField;

    public ScreenEmulatorFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, DebuggableMemory memory, int yStart, int uStart, int vStart, int screenWidth, int screenHeight) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.memory = memory;
        this.yStart = yStart;
        this.uStart = uStart;
        this.vStart = vStart;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        img = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        
        JPanel selectionPanel = new JPanel();
        selectionPanel.add(new JLabel("Start addresses:    Y = 0x"));
        yAddressField = new JTextField(Format.asHex(yStart, 8), 8);
        selectionPanel.add(yAddressField);
        yAddressField.addActionListener(this);

        selectionPanel.add(new JLabel("  U = 0x"));
        uAddressField = new JTextField(Format.asHex(uStart, 8), 8);
        selectionPanel.add(uAddressField);
        uAddressField.addActionListener(this);

        selectionPanel.add(new JLabel("  V = 0x"));
        vAddressField = new JTextField(Format.asHex(vStart, 8), 8);
        selectionPanel.add(vAddressField);
        vAddressField.addActionListener(this);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(selectionPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(new ScreenEmulatorComponent(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        
        getContentPane().add(contentPanel);

        setPreferredSize(new Dimension(screenWidth, 600));

        // Start update timer
        refreshTimer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        refreshTimer.start();
    }

    public void dispose() {
        refreshTimer.stop();
        refreshTimer = null;
        super.dispose();
    }

    public void actionPerformed(ActionEvent e) {
        this.yStart = Format.parseIntHexField(yAddressField);
        this.uStart = Format.parseIntHexField(uAddressField);
        this.vStart = Format.parseIntHexField(vAddressField);
    }

    private class ScreenEmulatorComponent extends JComponent {

        private ScreenEmulatorComponent() {
            super();
            setPreferredSize(new Dimension(screenWidth, screenHeight));
        }

        // This method is called whenever the contents needs to be painted
        public void paintComponent(Graphics graphics) {
            Graphics2D g2d = (Graphics2D) graphics;

            // Get size of JScrollPane
            int w = getParent().getWidth();
            int h = getParent().getHeight();

            // Create the resizing transform upon first call or resize
            if (resizeTransform == null || previousW != w || previousH != h) {
                resizeTransform = new AffineTransform();
                double scaleX = Math.max(0.5, (2 * w / screenWidth) / 2.0);
                double scaleY = Math.max(0.5, (2 * h / screenHeight) / 2.0);
                double scale = Math.min(scaleX, scaleY);
                resizeTransform.scale(scale, scale);
                previousW = w;
                previousH = h;
            }

            int yOffset=0, uAddr=uStart, vAddr=vStart;
            int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
            final int halfWidth = screenWidth>>1;

            // optimisation for buffered image TYPE_INT_RGB
            // coderat: this optimized code is 2x faster as before
            for (int yPos = 0; yPos < screenHeight; yPos++) {
                for (int xPos = 0; xPos < halfWidth; xPos++,yOffset+=2) {
                    final int y = memory.loadUnsigned16(yStart+yOffset, null);
                    setPixelsFromYCbCr422(pixels, yOffset, 
                                          y>>8,
                                          y&0xFF, 
                                          memory.loadUnsigned8(uAddr++, null), 
                                          memory.loadUnsigned8(vAddr++, null));
                }
                uAddr += halfWidth;
                vAddr += halfWidth;
            }

            g2d.drawImage(img, resizeTransform, null);
        }

        private final int clamp(int x) {
            return (x<=255 ? (x>=0 ? x : 0 ) : 255);
        }

        private final void setPixelsFromYCbCr422(int[] pixels,int pos, int y, int y1, int u, int v) {
            // full range YCbCr to RGB conversion
            final int factorR = Math.round(1.4f * (v-128) );
            final int factorG = Math.round(-0.343f * (u-128) - 0.711f * (v-128));
            final int factorB = Math.round(1.765f * (u-128) );
            
            // coderat: conversion YCbCr->RGB clamp is needed, because RGB do not include complete YCbCr space
            pixels[pos]   = (clamp(y+factorR) << 16) | (clamp(y+factorG) << 8) | clamp(y+factorB);
            pixels[pos+1] = (clamp(y1+factorR) << 16) | (clamp(y1+factorG) << 8) | clamp(y1+factorB);
        }
    }

}
