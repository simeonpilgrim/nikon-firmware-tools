package com.nikonhacker.gui;

import com.nikonhacker.dfr.Format;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.TrackingMemoryActivityListener;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;


public class MemoryActivityViewerFrame extends DocumentFrame {

    private static final int UPDATE_INTERVAL_MS = 40; // 25fps
    private static final int MAP_WIDTH = 256;
    private static final int MAP_HEIGHT = 256;
    private static final int PAGE_SIZE_BITS = 16;

    private static final int NO_SELECTION = -1;

    private Timer _timer;

    BufferedImage img = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_RGB);
    private double scaleX;
    private double scaleY;

    private TrackingMemoryActivityListener memoryActivityListener;
    private DebuggableMemory memory;
    
    /** Indicate if this is a "master" view (1 cell = 1 memory page) or a "detail view" (1 cell = 1 memory byte) */ 
    private boolean isMaster;
    
    private int baseAddress;
    private MemoryActivityViewerFrame parentFrame;
    
    private List<MemoryActivityViewerFrame> children = new ArrayList<MemoryActivityViewerFrame>();

    /**
     *  Create a viewer frame in "master" mode (1 cell = 1 memory page) 
     */
    public MemoryActivityViewerFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, DebuggableMemory memory, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.isMaster = true;
        this.memory = memory;

        // Attach the listener
        this.memoryActivityListener = new TrackingMemoryActivityListener(memory.getNumPages(), memory.getPageSize());
        memory.addActivityListener(memoryActivityListener);

        getContentPane().add(new MemoryMapComponent(memoryActivityListener.getPageActivityMap()));

        startTimer();
    }

    /** 
     * Create a viewer frame in "detail" mode (1 cell = 1 memory byte) 
     */
    public MemoryActivityViewerFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int[] activityMap, int baseAddress, MemoryActivityViewerFrame parentFrame, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.isMaster = false;
        this.parentFrame = parentFrame;
        this.baseAddress = baseAddress;

        getContentPane().add(new MemoryMapComponent(activityMap));
        
        startTimer();
    }

    private void startTimer() {
        // Start update timer
        _timer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        _timer.start();
    }

    private void openSubWindow(int screenX, int screenY) {
        int x = (int) (screenX / scaleX);
        int y = (int) (screenY / scaleY);
        int address = getAddressFromPosition(x, y);
        if (isMaster && ui != null) {
            if (memoryActivityListener.getCellActivityMap(address >>> PAGE_SIZE_BITS) != null) {
                MemoryActivityViewerFrame subFrame = new MemoryActivityViewerFrame("Memory activity from 0x" + Format.asHex(address, 8), true, true, true, true, memoryActivityListener.getCellActivityMap(address >>> PAGE_SIZE_BITS), address, this, ui);
                ui.addDocumentFrame(subFrame);
                children.add(subFrame);
                subFrame.display(false);
            }
        }
        //         for(int i = 0; i < MAP_HEIGHT * MAP_WIDTH; i++) activityMap[i] = 0;

    }


    private int getAddressFromPosition(int x, int y) {
        if (isMaster) {
            return (y * MAP_WIDTH + x) << PAGE_SIZE_BITS;
        }
        else {
            return baseAddress + (y * MAP_WIDTH + x);
        }
    }

    /** overridden to only bubble up if master page is closing */
    public void internalFrameClosing(InternalFrameEvent e) {
        if (isMaster) {
            super.internalFrameClosing(e);
        }
        else {
            parentFrame.children.remove(this);
            dispose();
        }
    }


    public void dispose() {
        _timer.stop();
        _timer = null;
        if (isMaster) {
            for (MemoryActivityViewerFrame child : children) {
                child.dispose();
            }
            memory.removeActivityListener(memoryActivityListener);            
        }
        super.dispose();
    }


    private class MemoryMapComponent extends JComponent {
        private int activityMap[];

        private int selectedX = NO_SELECTION;
        private int selectedY = NO_SELECTION;

        private AffineTransform resizeTransform;
        private int previousW, previousH;

        private MemoryMapComponent(int activityMap[]) {
            this.activityMap = activityMap;
            if (isMaster) {
                setPreferredSize(new Dimension(MAP_WIDTH * 2, MAP_HEIGHT * 2));
            }
            else {                
                setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
            }

            addMouseMotionListener(new MouseMotionListener() {
                public void mouseDragged(MouseEvent e) {/* noop */ }

                public void mouseMoved(MouseEvent e) {
                    selectedX = e.getX();
                    selectedY = e.getY();
                }
            });

            addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    openSubWindow(e.getX(), e.getY());
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

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, w, h);

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
                        + reads + ((reads == 255) ? "+" : "") + " reads, red="
                        + writes + ((writes == 255) ? "+" : "") + " writes, blue="
                        + execs + ((execs == 255) ? "+" : "") + " execs");
                Rectangle2D stringBounds = fm.getStringBounds(message, g);
                if (y < MAP_HEIGHT / 2) {
                    g2d.fillRect(0, (int) (h - stringBounds.getHeight()), fm.stringWidth(message), (int) (stringBounds.getHeight()));
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
    }
}
