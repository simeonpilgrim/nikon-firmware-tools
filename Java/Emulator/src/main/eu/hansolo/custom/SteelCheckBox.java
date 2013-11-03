package eu.hansolo.custom;

import javax.swing.*;
import java.awt.*;

/**
 * Original source was from http://harmoniccode.blogspot.be/2010/11/friday-fun-component-iii.html
 * @author hansolo
 * @author vicne for vertical version
 */
public class SteelCheckBox extends JCheckBox
{
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private boolean colored = false;
    private boolean rised = false;
    private eu.hansolo.tools.ColorDef selectedColor = eu.hansolo.tools.ColorDef.JUG_GREEN;
    protected static final String COLORED_PROPERTY = "colored";
    protected static final String COLOR_PROPERTY = "color";
    protected static final String RISED_PROPERTY = "rised";
    private int orientation = SwingConstants.HORIZONTAL;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Creates a horizontal SteelCheckBox
     */
    public SteelCheckBox()
    {
        super();
    }
    // </editor-fold>

    /**
     * Creates a SteelCheckBox in the chosen orientation
     * @param orientation one of SwingConstants.HORIZONTAl, SwingConstants.VERTICAL
     */
    public SteelCheckBox(int orientation)
    {
        super();
        this.orientation = orientation;
    }

    public int getOrientation() {
        return orientation;
    }

    public Dimension getPreferredSize() {
        int textWidth = 0;
        int textHeight = 0;
        if (getText() != null && getText().length() > 0) {
            // Delegate text rendering to a JLabel
            JLabel label = new JLabel(getText());
            label.setFont(getFont());
            Dimension labelSize = label.getPreferredSize();
            textWidth = (int) labelSize.getWidth();
            textHeight = (int) labelSize.getHeight();
        }
        if (getOrientation() == SwingConstants.HORIZONTAL) {
            return new java.awt.Dimension(26 + SteelCheckBoxUI.BTN_TEXT_HORIZONTAL_SPACING + textWidth, Math.max(15, textHeight));
        }
        else {
            return new java.awt.Dimension(Math.max(15, textWidth), 26 + textHeight);
        }
    }

    public Dimension getMinimumSize() {
        if (getOrientation() == SwingConstants.HORIZONTAL) {
            return new java.awt.Dimension(26, 15);
        }
        else {
            return new java.awt.Dimension(15, 26);
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Getter/Setter">
    public boolean isColored()
    {
        return this.colored;
    }

    public void setColored(final boolean COLORED)
    {
        final boolean OLD_STATE = this.colored;
        this.colored = COLORED;
        firePropertyChange(COLORED_PROPERTY, OLD_STATE, COLORED);
        repaint();
    }

    public boolean isRised()
    {
        return this.rised;
    }

    public void setRised(final boolean RISED)
    {
        final boolean OLD_VALUE = this.rised;
        this.rised = RISED;
        firePropertyChange(RISED_PROPERTY, OLD_VALUE, RISED);
    }

    public eu.hansolo.tools.ColorDef getSelectedColor()
    {
        return this.selectedColor;
    }

    public void setSelectedColor(final eu.hansolo.tools.ColorDef SELECTED_COLOR)
    {
        final eu.hansolo.tools.ColorDef OLD_COLOR = this.selectedColor;
        this.selectedColor = SELECTED_COLOR;
        firePropertyChange(COLOR_PROPERTY, OLD_COLOR, SELECTED_COLOR);
        repaint();
    }

    @Override
    public void setUI(final javax.swing.plaf.ButtonUI BUI)
    {
        super.setUI(new SteelCheckBoxUI(this));
    }

    public void setUi(final javax.swing.plaf.ComponentUI UI)
    {
        this.ui = new SteelCheckBoxUI(this);
    }

    @Override
    protected void setUI(final javax.swing.plaf.ComponentUI UI)
    {
        super.setUI(new SteelCheckBoxUI(this));
    }
    // </editor-fold>

    @Override
    public String toString()
    {
        return "SteelCheckBox";
    }
}
