package eu.hansolo.custom;


import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * Original source was from http://harmoniccode.blogspot.be/2010/11/friday-fun-component-iii.html
 * @author hansolo
 * @author vicne for vertical version
 */
public class SteelCheckBoxUI extends javax.swing.plaf.basic.BasicCheckBoxUI implements java.beans.PropertyChangeListener, java.awt.event.ComponentListener, java.awt.event.MouseListener
{
    public static final int SLIDER_SIZE = 26;
    public static final int BUTTON_SIZE = 13;
    public static final int BTN_TEXT_HORIZONTAL_SPACING = 5;
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private final SteelCheckBox CHECKBOX;
    private boolean mouseOver = false;
    private boolean mousePressed = false;
    private java.awt.Dimension SIZE;
    private java.awt.image.BufferedImage backgroundImage;
    private java.awt.image.BufferedImage knobStandardImage;
    private java.awt.image.BufferedImage knobPressedImage;
    private java.awt.Point pos = new java.awt.Point(0,0);    
    private java.awt.geom.RoundRectangle2D foreground;
    private java.awt.geom.Point2D foregroundStart;
    private java.awt.geom.Point2D foregroundStop;
    private final float[] FOREGROUND_FRACTIONS =
    {
        0.0f,
        0.03f,
        0.94f,
        1.0f
    };
    private java.awt.Color[] foregroundColors =
    {
        new java.awt.Color(241, 242, 242, 255),
        new java.awt.Color(224, 225, 226, 255),
        new java.awt.Color(166, 169, 171, 255),
        new java.awt.Color(124, 124, 124, 255)
    };
    private java.awt.LinearGradientPaint foregroundGradient;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public SteelCheckBoxUI(final SteelCheckBox CHECKBOX)
    {
        this.CHECKBOX = CHECKBOX;
        this.CHECKBOX.addComponentListener(this);
        this.CHECKBOX.addMouseListener(this);
        this.CHECKBOX.addPropertyChangeListener(this);

        init();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    private void init()
    {
        int orientation = CHECKBOX.getOrientation();

        if (orientation == SwingConstants.HORIZONTAL) {
            SIZE = new java.awt.Dimension(SLIDER_SIZE, BUTTON_SIZE);
            backgroundImage = create_BACKGROUND_Image(orientation);
            knobStandardImage = create_KNOB_Image(false);
            knobPressedImage = create_KNOB_Image(true);
            foreground = new java.awt.geom.RoundRectangle2D.Double(
                    pos.x + backgroundImage.getWidth() * 0.03846153989434242, pos.y + backgroundImage.getHeight() * 0.0714285746216774,
                    backgroundImage.getWidth() * 0.923076868057251, backgroundImage.getHeight() * 0.8571428060531616,
                    backgroundImage.getHeight() * 0.8571428571, backgroundImage.getHeight() * 0.8571428571);
            foregroundStart = new java.awt.geom.Point2D.Double(pos.x, foreground.getBounds2D().getMinY() );
            foregroundStop = new java.awt.geom.Point2D.Double(pos.x, foreground.getBounds2D().getMaxY() );
        }
        else {
            SIZE = new java.awt.Dimension(BUTTON_SIZE, SLIDER_SIZE);
            backgroundImage = create_BACKGROUND_Image(orientation);
            knobStandardImage = create_KNOB_Image(false);
            knobPressedImage = create_KNOB_Image(true);
            foreground = new java.awt.geom.RoundRectangle2D.Double(
                    pos.x + backgroundImage.getWidth() * 0.0714285746216774, pos.y + backgroundImage.getHeight() * 0.03846153989434242,
                    backgroundImage.getWidth() * 0.8571428060531616, backgroundImage.getHeight() * 0.923076868057251,
                    backgroundImage.getWidth() * 0.8571428571, backgroundImage.getWidth() * 0.8571428571);
            foregroundStart = new java.awt.geom.Point2D.Double(foreground.getBounds2D().getMinX(), pos.y);
            foregroundStop = new java.awt.geom.Point2D.Double(foreground.getBounds2D().getMaxX(), pos.y);
        }
        foregroundGradient = new java.awt.LinearGradientPaint(foregroundStart, foregroundStop, FOREGROUND_FRACTIONS, foregroundColors);
    }

    @Override
    public void installUI(final javax.swing.JComponent COMPONENT)
    {
        super.installUI(COMPONENT);        
        this.CHECKBOX.addComponentListener(this);
    }

    @Override
    public void uninstallUI(final javax.swing.JComponent COMPONENT)
    {
        super.uninstallUI(COMPONENT);
        this.CHECKBOX.removeComponentListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Paint method">
    @Override
    public synchronized void paint(java.awt.Graphics g, javax.swing.JComponent component)
    {
        final java.awt.Graphics2D G2 = (java.awt.Graphics2D) g.create();

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        if (CHECKBOX.getOrientation() == SwingConstants.HORIZONTAL) {
            pos.setLocation(0, (this.CHECKBOX.getSize().height - SIZE.height) / 2.0);
        }
        else{
            pos.setLocation((this.CHECKBOX.getSize().width - SIZE.width) / 2.0, 0);
        }


        if (!CHECKBOX.isEnabled())
        {
            G2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
        }

        // Draw the background
        G2.drawImage(backgroundImage, pos.x, pos.y, null);

        BufferedImage knobImage = (mouseOver && mousePressed)?knobPressedImage:knobStandardImage;
        // Draw the foreground and knob
        if (CHECKBOX.isSelected())
        {
            if (CHECKBOX.isColored())
            {
                if (CHECKBOX.isRised())
                {
                    foregroundColors = new java.awt.Color[]
                    {

                        CHECKBOX.getSelectedColor().LIGHT.brighter(),
                        CHECKBOX.getSelectedColor().LIGHT,
                        CHECKBOX.getSelectedColor().MEDIUM,
                        CHECKBOX.getSelectedColor().DARK
                    };
                }
                else
                {
                    foregroundColors = new java.awt.Color[]
                    {
                        CHECKBOX.getSelectedColor().DARK,
                        CHECKBOX.getSelectedColor().DARK,
                        CHECKBOX.getSelectedColor().LIGHT,
                        CHECKBOX.getSelectedColor().MEDIUM
                    };
                }
            }
            else
            {
                foregroundColors = new java.awt.Color[]
                {
                    new java.awt.Color(241, 242, 242, 255),
                    new java.awt.Color(224, 225, 226, 255),
                    new java.awt.Color(166, 169, 171, 255),
                    new java.awt.Color(124, 124, 124, 255)
                };
            }
            foregroundGradient = new java.awt.LinearGradientPaint(foregroundStart, foregroundStop, FOREGROUND_FRACTIONS, foregroundColors);
            G2.setPaint(foregroundGradient);
            G2.fill(foreground);
            if (CHECKBOX.getOrientation() == SwingConstants.HORIZONTAL) {
                // Checked is right
                G2.drawImage(knobImage, pos.x + backgroundImage.getWidth() / 2, pos.y, null);
            }
            else {
                // Checked is up
                G2.drawImage(knobImage, pos.x, pos.y, null);
            }
        }
        else
        {
            if (CHECKBOX.getOrientation() == SwingConstants.HORIZONTAL) {
                // Unchecked is left
                G2.drawImage(knobImage, pos.x, pos.y, null);
            }
            else {
                // Unchecked is down
                G2.drawImage(knobImage, pos.x, pos.y + backgroundImage.getHeight() / 2, null);
            }
        }

        if (CHECKBOX.getText() != null && CHECKBOX.getText().length() > 0) {
            G2.setColor(CHECKBOX.getForeground());
            G2.setFont(CHECKBOX.getFont());
            final java.awt.font.FontRenderContext RENDER_CONTEXT = new java.awt.font.FontRenderContext(null, true, true);
            final java.awt.font.TextLayout TEXT_LAYOUT = new java.awt.font.TextLayout(CHECKBOX.getText(), G2.getFont(), RENDER_CONTEXT);
            final java.awt.geom.Rectangle2D TEXT_BOUNDS = TEXT_LAYOUT.getBounds();
            if (CHECKBOX.getOrientation() == SwingConstants.HORIZONTAL) {
                G2.drawString(CHECKBOX.getText(), backgroundImage.getWidth() + 5, (CHECKBOX.getBounds().height - TEXT_BOUNDS.getBounds().height) / 2 + TEXT_BOUNDS.getBounds().height);
            }
            else {
                G2.drawString(CHECKBOX.getText(), (this.CHECKBOX.getSize().width - TEXT_BOUNDS.getBounds().width) / 2, backgroundImage.getHeight() + 5 + TEXT_BOUNDS.getBounds().height);
            }
        }

        G2.dispose();        
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image creation methods">
    private java.awt.image.BufferedImage create_KNOB_Image(final boolean PRESSED)
    {
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(BUTTON_SIZE, BUTTON_SIZE, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.Ellipse2D KNOB_FRAME = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 1.0);
        final java.awt.geom.Point2D KNOB_FRAME_START = new java.awt.geom.Point2D.Double(0, KNOB_FRAME.getBounds2D().getMinY() );
        final java.awt.geom.Point2D KNOB_FRAME_STOP = new java.awt.geom.Point2D.Double(0, KNOB_FRAME.getBounds2D().getMaxY() );
        final float[] E_KNOB_FRAME_FRACTIONS =
        {
            0.0f,
            0.25f,
            0.51f,
            0.76f,
            1.0f
        };
        final java.awt.Color[] KNOB_FRAME_COLORS =
        {
            new java.awt.Color(90, 91, 92, 255),
            new java.awt.Color(127, 127, 128, 255),
            new java.awt.Color(81, 82, 83, 255),
            new java.awt.Color(104, 105, 105, 255),
            new java.awt.Color(63, 64, 65, 255)
        };
        if (KNOB_FRAME_START.distance(KNOB_FRAME_STOP) > 0)
        {
            final java.awt.LinearGradientPaint KNOB_FRAME_GRADIENT = new java.awt.LinearGradientPaint(KNOB_FRAME_START, KNOB_FRAME_STOP, E_KNOB_FRAME_FRACTIONS, KNOB_FRAME_COLORS);
            G2.setPaint(KNOB_FRAME_GRADIENT);
            G2.fill(KNOB_FRAME);
        }

        final java.awt.geom.Ellipse2D KNOB = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.07692307978868484, IMAGE_HEIGHT * 0.07692307978868484, IMAGE_WIDTH * 0.8461538553237915, IMAGE_HEIGHT * 0.8461538553237915);
        final java.awt.geom.Point2D KNOB_CENTER = new java.awt.geom.Point2D.Double(KNOB.getCenterX(), KNOB.getCenterY());
        final float[] KNOB_FRACTIONS =
        {
            0.0f,
            40.0f,
            90.0f,
            140.0f,
            220.0f,
            270.0f,
            320.0f
        };
        final java.awt.Color[] KNOB_COLORS;

        if (PRESSED)
        {
            KNOB_COLORS = new java.awt.Color[]
            {
                new java.awt.Color(0xC2C2C2),
                new java.awt.Color(0x727678),
                new java.awt.Color(0xC2C2C2),
                new java.awt.Color(0x727678),
                new java.awt.Color(0xC2C2C2),
                new java.awt.Color(0x727678),
                new java.awt.Color(0xC2C2C2)
            };
        }
        else
        {            
            KNOB_COLORS = new java.awt.Color[]
            {
                new java.awt.Color(0xF2F2F2),
                new java.awt.Color(0x8F9396),
                new java.awt.Color(0xF2F2F2),
                new java.awt.Color(0x8F9396),
                new java.awt.Color(0xF2F2F2),
                new java.awt.Color(0x8F9396),
                new java.awt.Color(0xF2F2F2)
            };
        }
        final eu.hansolo.tools.ConicalGradientPaint KNOB_GRADIENT = new eu.hansolo.tools.ConicalGradientPaint(true, KNOB_CENTER, 0f, KNOB_FRACTIONS, KNOB_COLORS);
        G2.setPaint(KNOB_GRADIENT);
        G2.fill(KNOB);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_BACKGROUND_Image(int orientation)
    {
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        java.awt.image.BufferedImage IMAGE;
        if (orientation == SwingConstants.HORIZONTAL) {
            IMAGE = GFX_CONF.createCompatibleImage(SLIDER_SIZE, (int) (0.5384615384615384 * SLIDER_SIZE), java.awt.Transparency.TRANSLUCENT);
        }
        else {
            IMAGE = GFX_CONF.createCompatibleImage((int) (0.5384615384615384 * SLIDER_SIZE), SLIDER_SIZE, java.awt.Transparency.TRANSLUCENT);
        }
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
//        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.RoundRectangle2D BACKGROUND_FRAME;
        final java.awt.geom.Point2D BACKGROUND_FRAME_START;
        final java.awt.geom.Point2D BACKGROUND_FRAME_STOP;
        if (orientation == SwingConstants.HORIZONTAL) {
            BACKGROUND_FRAME = new java.awt.geom.RoundRectangle2D.Double(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 1.0, IMAGE_HEIGHT, IMAGE_HEIGHT);
            BACKGROUND_FRAME_START = new java.awt.geom.Point2D.Double(0, BACKGROUND_FRAME.getBounds2D().getMinY() );
            BACKGROUND_FRAME_STOP = new java.awt.geom.Point2D.Double(0, BACKGROUND_FRAME.getBounds2D().getMaxY() );
        }
        else {
            BACKGROUND_FRAME = new java.awt.geom.RoundRectangle2D.Double(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 1.0, IMAGE_WIDTH, IMAGE_WIDTH);
            BACKGROUND_FRAME_START = new java.awt.geom.Point2D.Double(BACKGROUND_FRAME.getBounds2D().getMinX(), 0);
            BACKGROUND_FRAME_STOP = new java.awt.geom.Point2D.Double(BACKGROUND_FRAME.getBounds2D().getMaxX(), 0);
        }
        final float[] BACKGROUND_FRAME_FRACTIONS =
        {
            0.0f,
            0.51f,
            1.0f
        };
        final java.awt.Color[] BACKGROUND_FRAME_COLORS =
        {
            new java.awt.Color(68, 68, 68, 255),
            new java.awt.Color(105, 105, 106, 255),
            new java.awt.Color(216, 217, 218, 255)
        };
        final java.awt.LinearGradientPaint BACKGROUND_FRAME_GRADIENT = new java.awt.LinearGradientPaint(BACKGROUND_FRAME_START, BACKGROUND_FRAME_STOP, BACKGROUND_FRAME_FRACTIONS, BACKGROUND_FRAME_COLORS);
        G2.setPaint(BACKGROUND_FRAME_GRADIENT);
        G2.fill(BACKGROUND_FRAME);


        java.awt.geom.RoundRectangle2D BACKGROUND;
        final java.awt.geom.Point2D BACKGROUND_START;
        final java.awt.geom.Point2D BACKGROUND_STOP;
        if (orientation == SwingConstants.HORIZONTAL) {
            BACKGROUND = new java.awt.geom.RoundRectangle2D.Double(IMAGE_WIDTH * 0.03846153989434242, IMAGE_HEIGHT * 0.0714285746216774, IMAGE_WIDTH * 0.923076868057251, IMAGE_HEIGHT * 0.8571428060531616, IMAGE_HEIGHT, IMAGE_HEIGHT);
            BACKGROUND_START = new java.awt.geom.Point2D.Double(0, BACKGROUND.getBounds2D().getMinY() );
            BACKGROUND_STOP = new java.awt.geom.Point2D.Double(0, BACKGROUND.getBounds2D().getMaxY() );
        }
        else {
            BACKGROUND = new java.awt.geom.RoundRectangle2D.Double(IMAGE_WIDTH * 0.0714285746216774, IMAGE_HEIGHT * 0.03846153989434242, IMAGE_WIDTH * 0.8571428060531616, IMAGE_HEIGHT * 0.923076868057251, IMAGE_WIDTH, IMAGE_WIDTH);
            BACKGROUND_START = new java.awt.geom.Point2D.Double(BACKGROUND.getBounds2D().getMinX(), 0);
            BACKGROUND_STOP = new java.awt.geom.Point2D.Double(BACKGROUND.getBounds2D().getMaxX(), 0);
        }
        final float[] BACKGROUND_FRACTIONS =
        {
            0.0f,
            0.96f,
            1.0f
        };
        final java.awt.Color[] BACKGROUND_COLORS =
        {
            new java.awt.Color(91, 91, 91, 255),
            new java.awt.Color(138, 138, 138, 255),
            new java.awt.Color(124, 124, 124, 255)
        };
        if (BACKGROUND_START.distance(BACKGROUND_STOP) > 0)
        {
            final java.awt.LinearGradientPaint BACKGROUND_GRADIENT = new java.awt.LinearGradientPaint(BACKGROUND_START, BACKGROUND_STOP, BACKGROUND_FRACTIONS, BACKGROUND_COLORS);
            G2.setPaint(BACKGROUND_GRADIENT);
            G2.fill(BACKGROUND);
        }

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Utility methods">
    public java.awt.geom.Point2D getCenteredTextPosition(final java.awt.Graphics2D G2, final java.awt.geom.Rectangle2D BOUNDARY, final java.awt.Font FONT, final String TEXT, final int ORIENTATION)
    {
        // Get the visual center of the component.
        final double CENTER_X = BOUNDARY.getWidth() / 2.0;
        final double CENTER_Y = BOUNDARY.getHeight() / 2.0;

        // Get the text boundary
        final java.awt.font.FontRenderContext RENDER_CONTEXT = G2.getFontRenderContext();
        final java.awt.font.TextLayout LAYOUT = new java.awt.font.TextLayout(TEXT, FONT, RENDER_CONTEXT);
        final java.awt.geom.Rectangle2D TEXT_BOUNDARY = LAYOUT.getBounds();

        // Calculate the text position
        final double TEXT_X;
        final double TEXT_Y;
        switch (ORIENTATION)
        {
            case javax.swing.SwingConstants.CENTER:
                TEXT_X = CENTER_X - TEXT_BOUNDARY.getWidth() / 2.0;
                TEXT_Y = CENTER_Y - TEXT_BOUNDARY.getHeight() / 2.0 + TEXT_BOUNDARY.getHeight();
                break;

            case javax.swing.SwingConstants.LEFT:
                TEXT_X = BOUNDARY.getMinX();
                TEXT_Y = CENTER_Y - TEXT_BOUNDARY.getHeight() / 2.0 + TEXT_BOUNDARY.getHeight();
                break;

            case javax.swing.SwingConstants.RIGHT:
                TEXT_X = BOUNDARY.getMaxX() - TEXT_BOUNDARY.getWidth();
                TEXT_Y = CENTER_Y - TEXT_BOUNDARY.getHeight() / 2.0 + TEXT_BOUNDARY.getHeight();
                break;

            default:
                TEXT_X = CENTER_X - TEXT_BOUNDARY.getWidth() / 2.0;
                TEXT_Y = CENTER_Y - TEXT_BOUNDARY.getHeight() / 2.0 + TEXT_BOUNDARY.getHeight();
                break;
        }

        return new java.awt.geom.Point2D.Double(TEXT_X, TEXT_Y);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MouseListener methods">
    @Override
    public void mouseClicked(java.awt.event.MouseEvent event)
    {
        mousePressed = false;
        CHECKBOX.repaint();
    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent event)
    {
        mousePressed = true;
        CHECKBOX.repaint();
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent event)
    {
        mousePressed = false;
        CHECKBOX.repaint();
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent event)
    {
        mouseOver = true;
        CHECKBOX.repaint();
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent event)
    {
        mouseOver = false;
        CHECKBOX.repaint();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ComponentListener methods">
    @Override
    public void componentResized(final java.awt.event.ComponentEvent EVENT)
    {
        init();
        if (CHECKBOX.getOrientation() == SwingConstants.HORIZONTAL) {
            pos.setLocation(0, (EVENT.getComponent().getSize().height - SIZE.height) / 2.0);
        }
        else {
            pos.setLocation((EVENT.getComponent().getSize().width - SIZE.width) / 2.0, 0);
        }
    }

    @Override
    public void componentMoved(final java.awt.event.ComponentEvent EVENT)
    {

    }

    @Override
    public void componentShown(final java.awt.event.ComponentEvent EVENT)
    {

    }

    @Override
    public void componentHidden(final java.awt.event.ComponentEvent EVENT)
    {

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PropertyChangeListener method">
    @Override
    public void propertyChange(final java.beans.PropertyChangeEvent EVENT)
    {
        init();
        CHECKBOX.repaint();
    }
    // </editor-fold>

    @Override
    public String toString()
    {
        return "SteelCheckBoxUI";
    }
}
