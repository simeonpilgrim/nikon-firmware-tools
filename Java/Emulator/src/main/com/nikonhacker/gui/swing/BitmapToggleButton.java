package com.nikonhacker.gui.swing;

import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.frontPanel.StateChangeListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BitmapToggleButton extends JComponent implements MouseMotionListener, MouseListener {

    /**
     * The required dimension for all images
     */
    private final Dimension dimension;
    private final int  numberStates;
    private int        stateNumber;
    private String     imgDir;
    private String     imgPrefix;
    private String[][] imageSuffixes;
    private boolean isLeftClickTemp;

    private final BufferedImage mask;
    // image cache, filled on first request
    private BufferedImage[][] imageCache;

    private BufferedImage currentImage;

    private List<StateChangeListener> stateChangeListeners = new ArrayList<>();

    /**
     * Creates a new BitmapToggleButton
     * @param component the component this button belongs to
     * @param imgDir
     * @param imgPrefix image filename prefix
     * @param requiredWidth required image width
     * @param requiredHeight required image height
     * @param imageSuffixes an array[2][n] image suffixes. The first array is the "non-hover", the second one is the "hover". n is the number of states the button can be in.
     * @param isLeftClickTemp if true, a single left click presses and releases the button. If not, the left click toggles the button
     * @param initialStateNumber the state the button should be in upon creation
     */
    public BitmapToggleButton(JComponent component, String imgDir, String imgPrefix, int requiredWidth, int requiredHeight, String[][] imageSuffixes, boolean isLeftClickTemp, int initialStateNumber) {
        super();
        this.imgDir = imgDir;
        this.imgPrefix = imgPrefix;
        this.imageSuffixes = imageSuffixes;
        this.isLeftClickTemp = isLeftClickTemp;
        dimension = new Dimension(requiredWidth, requiredHeight);

        setOpaque(false);

        numberStates = imageSuffixes[0].length;

        // Prepare image cache. Actual loading is delayed. Will be done on demand.
        imageCache = new BufferedImage[2][numberStates];

        mask = getImage(1, 0);
        if (mask == null) {
            throw new RuntimeException("Could not initialize create BitmapToggleButton: hover image for state #0 must be specified as it serves as mask");
        }

        component.addMouseMotionListener(this);
        component.addMouseListener(this);

        // Set state and refresh
        stateNumber = initialStateNumber;
        updateCurrentImage(false);
    }

    /**
     * Returns the image for the given state, or null if no image is to be overlayed in this state.
     * This uses lazy loading.
     * @param hover 0 for non-hover, 1 for hover
     * @param stateNumber the button state
     * @return
     */
    private BufferedImage getImage(int hover, int stateNumber) {
        if (imageCache[hover][stateNumber] == null) {
            // see if one should be loaded
            if (imageSuffixes[hover][stateNumber] != null) {
                try {
                    imageCache[hover][stateNumber] = getBufferedImage(imgDir + "/" + imgPrefix + "_" + imageSuffixes[hover][stateNumber] + ".png");
                } catch (IOException e) {
                    System.err.println("Error initializing images #" + stateNumber + " for BitmapToggleButton: " + imgPrefix);
                    e.printStackTrace();
                }
            }
        }
        return imageCache[hover][stateNumber];
    }

    private void updateCurrentImage(boolean isHover) {
        currentImage = getImage(isHover?1:0, stateNumber);
        repaint();
    }

    public void setState(int stateNumber, boolean isHover) {
        this.stateNumber = stateNumber;
        for (StateChangeListener stateChangeListener : stateChangeListeners) {
            stateChangeListener.onStateChange(stateNumber);
        }
        updateCurrentImage(isHover);
    }

    private boolean isOnOpaquePixel(MouseEvent e) {
        try {
            return (mask.getRGB(e.getX(), e.getY()) & 0xff000000) != 0;
        }
        catch (ArrayIndexOutOfBoundsException ex){
            // out of the image. Ignore
            return false;
        }
    }

    private BufferedImage getBufferedImage(String filename) throws IOException {
        URL resource = EmulatorUI.class.getResource(filename);
        if (resource == null) {
            System.err.println("Resource not found: " + filename);
            return null;
        }
        BufferedImage selectedImage = ImageIO.read(resource);
        if (selectedImage.getWidth() != dimension.getWidth() || selectedImage.getHeight() != dimension.getHeight()) {
            System.err.println("Wrong file size for " + filename + ": " + selectedImage.getWidth() + "x" + selectedImage.getHeight() + " while it should be " + dimension.getWidth() + "x" + dimension.getHeight());
        }
        return selectedImage;
    }

    @Override
    public void paintComponent(Graphics g){
        if(currentImage != null){
            g.drawImage(currentImage, 0, 0, this);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return dimension;
    }

    // Handled mouse events

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        updateCurrentImage(isOnOpaquePixel(e));
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (isOnOpaquePixel(e)) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                // state = 1;
                if (stateNumber < numberStates - 1) {
                    setState(stateNumber + 1, true);
                }
                else {
                    setState(0, true);
                }
                for (StateChangeListener stateChangeListener : stateChangeListeners) {
                    stateChangeListener.onStateRotateUp();
                }
            }
            else if (SwingUtilities.isRightMouseButton(e)) {
                // state = 1 - state;
                if (stateNumber > 0) {
                    setState(stateNumber - 1, true);
                }
                else {
                    setState(numberStates - 1, true);
                }
                for (StateChangeListener stateChangeListener : stateChangeListeners) {
                    stateChangeListener.onStateRotateDown();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isOnOpaquePixel(e)) {
            if (SwingUtilities.isLeftMouseButton(e) && isLeftClickTemp) {
                setState(0, true);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public void addStateChangeListener(StateChangeListener stateChangeListener) {
        this.stateChangeListeners.add(stateChangeListener);
    }
}
