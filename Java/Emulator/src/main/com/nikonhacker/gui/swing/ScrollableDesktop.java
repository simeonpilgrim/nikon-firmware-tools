package com.nikonhacker.gui.swing;

/**
 * A JDesktopPane that implements scrollable, and has a preferred size that auto-adapts dynamically
 * to the size and position of its internal frames
 * Just wrap it in a JScrollPane and it's done.
 *
 * This was inspired by:
 * - http://www.java2s.com/Tutorial/Java/0240__Swing/extendsJDesktopPaneimplementsScrollable.htm
 * Author: unknown
 * and
 * - http://tips4java.wordpress.com/2009/12/20/scrollable-panel
 * Author: Rob Camick
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

public class ScrollableDesktop extends JDesktopPane implements Scrollable, ContainerListener, ComponentListener {
    private int originalWidth;
    private int originalHeight;

    public ScrollableDesktop() {
        super();
        this.addContainerListener(this);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        this.originalWidth = (int) preferredSize.getWidth();
        this.originalHeight = (int) preferredSize.getHeight();
    }

    ////////////////////////////////////////////////////////////////////////////
    // From interface Scrollable

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int axis, int dir) {
        return 50;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int axis, int dir) {
        return 200;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////
    // From interface ContainerListener,
    // To hook a listener to each internal frame so that the preferred size of the panel will be updated
    // when internal frames are moved or resized
    //

    @Override
    public void componentAdded(ContainerEvent e) {
        e.getChild().addComponentListener(this);
        updatePreferredSize();
    }

    @Override
    public void componentRemoved(ContainerEvent e) {}


    ////////////////////////////////////////////////////////////////////////////
    // From interface ContainerListener,
    // which will be hooked to each internal frame so that the preferred size of the panel will be updated
    //

    @Override
    public void componentResized(ComponentEvent e) {
        updatePreferredSize();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        updatePreferredSize();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        updatePreferredSize();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        updatePreferredSize();
    }

    private void updatePreferredSize() {
        int maxRight = 0;
        int maxBottom = 0;
        for (Component component : getComponents()) {
            if (component.isVisible()) {
                Point location = component.getLocation();
                Dimension size = component.getSize();
                maxRight = Math.max(maxRight, location.x + size.width);
                maxBottom = Math.max(maxBottom, location.y + size.height);
            }
        }
        maxRight = Math.max(maxRight, originalWidth);
        maxBottom = Math.max(maxBottom, originalHeight);
        Dimension size = new Dimension(maxRight + 5, maxBottom + 5);
        super.setPreferredSize(size);
        setSize(size);
        revalidate();
    }
}

