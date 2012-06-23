package com.nikonhacker.gui;

import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class MemoryMapComponent extends JComponent {

    private static final int UPDATE_INTERVAL_MS = 1000; // 1fps
    private static final int MAP_WIDTH = 256;
    private static final int MAP_HEIGHT = 256;
    private static final int PAGE_SIZE_BITS = 16;

    private static final int NO_SELECTION = -1;

    public static final int PAGE_MODE = -1;

    private AffineTransform resizeTransform;
    private int previousW, previousH;

    private int activityMap[];

    private Timer _timer; 

    private int selectedX = NO_SELECTION;
    private int selectedY = NO_SELECTION;

    BufferedImage img = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_RGB);
    private double scaleX;
    private double scaleY;

    private TrackingMemoryActivityListener activityListener;
    private int baseAddress = PAGE_MODE;
    private EmulatorUI emulatorUI;

    /**
     *
     * @param activityListener
     * @param baseAddress the base address this component displays. If displaying the page level, use PAGE_MODE
     * @param emulatorUI
     */
    public MemoryMapComponent(TrackingMemoryActivityListener activityListener, int baseAddress, EmulatorUI emulatorUI) {
        this.activityListener = activityListener;
        this.baseAddress = baseAddress;
        this.emulatorUI = emulatorUI;

        if (baseAddress == PAGE_MODE) {
            activityMap = activityListener.getPageActivityMap();
            setPreferredSize(new Dimension(MAP_WIDTH*2, MAP_HEIGHT*2));
        }
        else {
            activityMap = activityListener.getCellActivityMap(baseAddress >>> PAGE_SIZE_BITS);
            setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        }
        

        _timer = new javax.swing.Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        _timer.start();

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {/* noop */ }

            public void mouseMoved(MouseEvent e) {
                selectedX = e.getX();
                selectedY = e.getY();
            }
        });

        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                // Clear
                for(int i = 0; i < MAP_HEIGHT * MAP_WIDTH; i++) activityMap[i] = 0;
            }

            public void mousePressed(MouseEvent e) {/* noop */ }

            public void mouseReleased(MouseEvent e) {/* noop */ }

            public void mouseEntered(MouseEvent e) {/* noop */ }

            public void mouseExited(MouseEvent e) {
                selectedX = NO_SELECTION;
                selectedY = NO_SELECTION;
            }
        });
    }


    // This method is called whenever the contents needs to be painted
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        // Create the resizing transform upon first call or resize
        if (resizeTransform == null || previousW != w || previousH != h) {
            resizeTransform = new AffineTransform();
            scaleX = Math.max((double) w / MAP_WIDTH, 1);
            scaleY = Math.max((double) h / MAP_HEIGHT, 1);
            resizeTransform.scale(scaleX, scaleY);
            previousW = w;
            previousH = h;
        }

        img.setRGB(0, 0, MAP_WIDTH, MAP_HEIGHT, activityMap, 0, MAP_WIDTH);

        g2d.drawImage(img, resizeTransform, null);

        if (selectedX != NO_SELECTION) {
            int x = (int) (selectedX / scaleX);
            int y = (int) (selectedY / scaleY);
            FontMetrics fm = g.getFontMetrics();
            int value = activityMap[y * MAP_WIDTH + x];
            int reads = (value & 0xFF00) >>> 8;
            int writes = (value & 0xFF0000) >>> 16;
            int execs = (value & 0xFF);
            String message = "0x" + (Format.asHex(getAddressFromPosition(x, y), 8) + " : green="
                    + reads + ((reads==255)?"+":"") + " reads, red=" 
                    + writes + ((writes==255)?"+":"") + " writes, blue="
                    + execs + ((execs==255)?"+":"") + " execs");
            Rectangle2D stringBounds = fm.getStringBounds(message, g);
            g2d.setPaint(Color.WHITE);
            if (y < MAP_HEIGHT/2) {
                g2d.fillRect(0, (int)(h - stringBounds.getHeight()), fm.stringWidth(message), (int)(stringBounds.getHeight()));
                g2d.setPaint(Color.BLACK);
                g2d.drawString(message, 0, h - fm.getDescent());
            }
            else {
                g2d.fillRect(0, 0, fm.stringWidth(message), fm.getHeight());
                g2d.setPaint(Color.BLACK);
                g2d.drawString(message, 0, (int) stringBounds.getHeight() - fm.getDescent());
            }
        }
    }

    private int getAddressFromPosition(int x, int y) {
        if (baseAddress == PAGE_MODE) {
            return (y * MAP_WIDTH + x) << PAGE_SIZE_BITS;
        }
        else{
            return baseAddress + (y * MAP_WIDTH + x);
        }
    }
}
