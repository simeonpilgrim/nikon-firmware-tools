package com.nikonhacker.gui.util;

import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * This is just a matte border whose color can be dynamically changed with setColor();
 */
public class DynamicMatteBorder extends MatteBorder {
    public DynamicMatteBorder(int top, int left, int bottom, int right) {
        super(top, left, bottom, right, (Color)null);
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
