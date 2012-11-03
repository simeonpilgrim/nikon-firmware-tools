package com.nikonhacker.gui.component.screenEmulator;

import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ScreenEmulatorFrame extends DocumentFrame implements ActionListener {

    private static final int UPDATE_INTERVAL_MS = 100; // 10fps

    private AffineTransform resizeTransform;
    private int previousW, previousH;

    private DebuggableMemory memory;
    private int yStart, uStart, vStart;
    int screenHeight, screenWidth;

    BufferedImage img;

    private Timer _timer;
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

    public void actionPerformed(ActionEvent e) {
        this.yStart = Format.parseIntHexField(yAddressField);
        this.uStart = Format.parseIntHexField(uAddressField);
        this.vStart = Format.parseIntHexField(vAddressField);
    }

    private class ScreenEmulatorComponent extends JComponent {
        Object pixel = null;

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
                resizeTransform.scale(scaleX, scaleY);
                previousW = w;
                previousH = h;
            }

            int yOffset, uvOffset;

            for (int yPos = 0; yPos < 480; yPos++) {
                for (int xPos = 0; xPos < screenWidth/2; xPos++) {
                    yOffset = yPos * screenWidth + xPos * 2;
                    uvOffset = yPos * screenWidth + xPos;

                    int u = memory.loadUnsigned8(uStart + uvOffset, false);
                    int v = memory.loadUnsigned8(vStart + uvOffset, false);

                    setPixelFromYUV(xPos * 2, yPos, memory.loadUnsigned8(yStart + yOffset, false) - 128, u - 128, v - 128);
                    setPixelFromYUV(xPos * 2 + 1, yPos, memory.loadUnsigned8(yStart + yOffset + 1, false) - 128, u - 128, v - 128);
                }
            }

            g2d.drawImage(img, resizeTransform, null);
        }

        private void setPixelFromYUV(int xPos, int yPos, int y, int u, int v) {
            int r = Math.max(0, Math.min(255, (int) (128 + y + 1.13983 * v)));
            int g = Math.max(0, Math.min(255, (int) (128 + y - 0.39465 * u - 0.58060 * v)));
            int b = Math.max(0, Math.min(255, (int) (128 + y + 2.03211 * u)));

            img.getRaster().setDataElements(xPos, yPos, img.getColorModel().getDataElements(r << 16 | g << 8 | b, pixel));
        }
    }
}
