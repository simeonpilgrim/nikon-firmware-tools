/*
 * Copyright (c) 2012, Gerrit Grunwald
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * The names of its contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.tools.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hansolo
 */
public class Led extends JComponent implements ActionListener {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private static final    Util                       UTIL  = Util.INSTANCE;
    private static final    Map<String, BufferedImage> CACHE = new HashMap<String, BufferedImage>();
    private final           Rectangle                  INNER_BOUNDS;
    private final           Timer                      LED_BLINKING_TIMER;
    private final transient ComponentListener          COMPONENT_LISTENER;
    private                 LedColor                   ledColor;
    private                 CustomLedColor             customLedColor;
    private                 BufferedImage              ledImageOff;
    private                 BufferedImage              ledImageOn;
    private                 BufferedImage              currentLedImage;
    private                 boolean                    ledBlinking;
    private                 boolean                    ledOn;
    private                 LedType                    ledType;
    private                 boolean                    initialized;
    // Alignment related
    private                 int horizontalAlignment;
    private                 int verticalAlignment;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Led() {
        super();
        INNER_BOUNDS        = new Rectangle(16, 16);
        ledColor            = LedColor.RED_LED;
        customLedColor      = new CustomLedColor(Color.RED);
        ledImageOff         = create_LED_Image(16, 0, ledColor, LedType.ROUND);
        ledImageOn          = create_LED_Image(16, 1, ledColor, LedType.ROUND);
        currentLedImage     = ledImageOff;
        LED_BLINKING_TIMER  = new javax.swing.Timer(500, this);
        ledBlinking         = false;
        ledOn               = false;
        ledType             = LedType.ROUND;
        initialized         = false;
        COMPONENT_LISTENER  = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                final int SIZE = getWidth() <= getHeight() ? getWidth() : getHeight();
                java.awt.Container parent = getParent();
                if ((parent != null) && (parent.getLayout() == null)) {
                    if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                        setSize(getMinimumSize().width, getMinimumSize().height);
                    } else {
                        setSize(SIZE, SIZE);
                    }
                } else {
                    if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                        setPreferredSize(getMinimumSize());
                    } else {
                        setPreferredSize(new java.awt.Dimension(SIZE, SIZE));
                    }
                }

                calcInnerBounds();

                init(INNER_BOUNDS.width);
                //revalidate();
                //repaint(INNER_BOUNDS);

            }
        };
        horizontalAlignment = SwingConstants.CENTER;
        verticalAlignment   = SwingConstants.CENTER;
        init(INNER_BOUNDS.width);
        addComponentListener(COMPONENT_LISTENER);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    private void init(final int WIDTH) {
        if (WIDTH <= 1) {
            return;
        }

        if (ledImageOff != null) {
            ledImageOff.flush();
        }
        ledImageOff = create_LED_Image(WIDTH, 0, ledColor, ledType);

        if (ledImageOn != null) {
            ledImageOn.flush();
        }
        ledImageOn = create_LED_Image(WIDTH, 1, ledColor, ledType);

        if (ledOn) {
            setCurrentLedImage(ledImageOn);
        } else {
            setCurrentLedImage(ledImageOff);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(Graphics g) {
        if (!initialized) {
            return;
        }

        final Graphics2D G2 = (Graphics2D) g.create();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        G2.translate(INNER_BOUNDS.x, INNER_BOUNDS.y);

        G2.drawImage(getCurrentLedImage(), 0, 0, null);

        G2.translate(-INNER_BOUNDS.x, -INNER_BOUNDS.y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getter and setter">
    /**
     * Returns the type of LED. Possible values are ROUND, RECT_VERTICAL and RECT_HORIZONTAL
     * @return the type of LED. Possible values are ROUND, RECT_VERTICAL and RECT_HORIZONTAL
     */
    public LedType getLedType() {
        return ledType;
    }

    /**
     * Sets the type of LED.
     * @param LED_TYPE Possible values are ROUND, RECT_VERTICAL and RECT_HORIZONTAL
     */
    public void setLedType(final LedType LED_TYPE) {
        if (ledType == LED_TYPE) {return;}

        ledType = LED_TYPE;
        final boolean LED_WAS_ON = currentLedImage.equals(ledImageOn) ? true : false;
        flushImages();
        ledImageOff = create_LED_Image(getWidth(), 0, ledColor, ledType);
        ledImageOn = create_LED_Image(getWidth(), 1, ledColor, ledType);

        currentLedImage = LED_WAS_ON == true ? ledImageOn : ledImageOff;

        repaint();
    }

    /**
     * Returns the color of led.
     * The LedColor is not a standard color but defines a
     * color scheme for the led. The default ledcolor is RED
     * @return the selected the color for the led
     */
    public LedColor getLedColor() {
        return ledColor;
    }

    /**
     * Sets the color of the threshold led.
     * The LedColor is not a standard color but defines a
     * color scheme for the led. The default ledcolor is RED
     * @param LED_COLOR
     */
    public void setLedColor(final LedColor LED_COLOR) {
        if (ledColor == LED_COLOR) {return;}

        if (LED_COLOR == null) {
            ledColor = LedColor.RED_LED;
        } else {
            ledColor = LED_COLOR;
        }
        final boolean LED_WAS_ON = currentLedImage.equals(ledImageOn) ? true : false;
        flushImages();
        ledImageOff = create_LED_Image(getWidth(), 0, LED_COLOR, ledType);
        ledImageOn  = create_LED_Image(getWidth(), 1, LED_COLOR, ledType);

        currentLedImage = LED_WAS_ON == true ? ledImageOn : ledImageOff;

        repaint();
    }

    /**
     * Returns the color that will be used to calculate the custom led color
     * @return the color that will be used to calculate the custom led color
     */
    public Color getCustomLedColor() {
        return customLedColor.COLOR;
    }

    /**
     * Sets the color that will be used to calculate the custom led color
     * @param COLOR
     */
    public void setCustomLedColor(final Color COLOR) {
        if (customLedColor.COLOR.equals(COLOR)) {return;}

        customLedColor = new CustomLedColor(COLOR);
        final boolean LED_WAS_ON = currentLedImage.equals(ledImageOn) ? true : false;
        flushImages();

        ledImageOff = create_LED_Image(getWidth(), 0, ledColor, ledType);
        ledImageOn  = create_LED_Image(getWidth(), 1, ledColor, ledType);

        currentLedImage = LED_WAS_ON == true ? ledImageOn : ledImageOff;

        repaint();
    }

    /**
     * Returns the object that represents the custom led color
     * @return the object that represents the custom led color
     */
    public CustomLedColor getCustomLedColorObject() {
        return customLedColor;
    }

    /**
     * Returns true if the led is on
     * @return true if the led is on
     */
    public boolean isLedOn() {
        return ledOn;
    }

    /**
     * Sets the state of the led
     * @param LED_ON
     */
    public void setLedOn(final boolean LED_ON) {
        if (ledOn == LED_ON) {return;}
        ledOn = LED_ON;
        init(getWidth());
        repaint();
    }

    /**
     * Returns the state of the threshold led.
     * The led could blink which will be triggered by a javax.swing.Timer
     * that triggers every 500 ms. The blinking will be done by switching
     * between two images.
     * @return true if the led is blinking
     */
    public boolean isLedBlinking() {
        return ledBlinking;
    }

    /**
     * Sets the state of the threshold led.
     * The led could blink which will be triggered by a javax.swing.Timer
     * that triggers every 500 ms. The blinking will be done by switching
     * between two images.
     * @param LED_BLINKING
     */
    public void setLedBlinking(final boolean LED_BLINKING) {
        if (ledBlinking == LED_BLINKING) {return;}
        ledBlinking = LED_BLINKING;
        if (LED_BLINKING) {
            LED_BLINKING_TIMER.start();
        } else {
            setCurrentLedImage(getLedImageOff());
            LED_BLINKING_TIMER.stop();
        }
    }

    /**
     * Returns the current component as buffered image.
     * To save this buffered image as png you could use for example:
     * File file = new File("image.png");
     * ImageIO.write(Image, "png", file);
     * @return the current component as buffered image
     */
    public BufferedImage getAsImage() {
        final BufferedImage IMAGE = UTIL.createImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        paintAll(G2);
        G2.dispose();
        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private void flushImages() {
        ledImageOff.flush();
        ledImageOn.flush();
    }

    /**
     * Returns the image of the switched on threshold led
     * with the currently active ledcolor.
     * @return the image of the led with the state active
     * and the selected led color
     */
    private BufferedImage getLedImageOn() {
        return ledImageOn;
    }

    /**
     * Returns the image of the switched off threshold led
     * with the currently active ledcolor.
     * @return the image of the led with the state inactive
     * and the selected led color
     */
    private BufferedImage getLedImageOff() {
        return ledImageOff;
    }

    /**
     * Returns the image of the currently used led image.
     * @return the led image at the moment (depends on blinking)
     */
    private BufferedImage getCurrentLedImage() {
        return currentLedImage;
    }

    /**
     * Sets the image of the currently used led image.
     * @param CURRENT_LED_IMAGE
     */
    private void setCurrentLedImage(final BufferedImage CURRENT_LED_IMAGE) {
        currentLedImage = CURRENT_LED_IMAGE;
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns a buffered image that represents a led with the given size, state and color
     * @param SIZE
     * @param STATE
     * @param LED_COLOR
     * @return a buffered image that represents a led with the given size, state and color
     */
    public final BufferedImage create_LED_Image(final int SIZE, final int STATE,
                                                final LedColor LED_COLOR,
                                                final LedType LED_TYPE) {
        if (SIZE <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final StringBuilder KEY = new StringBuilder(32);
        KEY.append(Integer.toString(SIZE));
        KEY.append("_");
        KEY.append(Integer.toString(STATE));
        KEY.append("_");
        KEY.append(LED_COLOR);
        KEY.append("_");
        KEY.append(LED_TYPE);

        if (LED_COLOR == LedColor.CUSTOM) {
            StringBuilder sb = new StringBuilder();
            sb.append("#");
            final String RED_HEX   = Integer.toHexString(customLedColor.COLOR.getRed());
            final String BLUE_HEX  = Integer.toHexString(customLedColor.COLOR.getBlue());
            final String GREEN_HEX = Integer.toHexString(customLedColor.COLOR.getGreen());
            if (RED_HEX.length() == 1) {
                sb.append("0");
            }
            sb.append(RED_HEX);
            if (GREEN_HEX.length() == 1) {
                sb.append("0");
            }
            sb.append(GREEN_HEX);
            if (BLUE_HEX.length() == 1) {
                sb.append("0");
            }
            sb.append(BLUE_HEX);
            if (customLedColor.COLOR.getAlpha() != 255) {
                final String ALPHA_HEX = Integer.toHexString(customLedColor.COLOR.getAlpha());
                if (ALPHA_HEX.length() == 1) {
                    sb.append("0");
                }
                sb.append(ALPHA_HEX);
            }
            KEY.append("_").append(sb.toString());
        }

        if (CACHE.containsKey(KEY.toString())) {
            return CACHE.get(KEY.toString());
        }

        final BufferedImage IMAGE = UTIL.createImage(SIZE, SIZE, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Define led data
        final java.awt.Shape LED;
        switch (LED_TYPE) {
            case RECT_VERTICAL:
                //LED = new Rectangle2D.Double(IMAGE_WIDTH * 0.39473684210526316, IMAGE_HEIGHT * 0.23684210526315788, IMAGE_WIDTH * 0.18421052631578946, IMAGE_HEIGHT * 0.5);
                LED = new Rectangle2D.Double(IMAGE_WIDTH * 0.3421052632, IMAGE_HEIGHT * 0.1052631579, IMAGE_WIDTH * 0.3157894737, IMAGE_HEIGHT * 0.7894736842);
                break;

            case RECT_HORIZONTAL:
                LED = new Rectangle2D.Double(IMAGE_WIDTH * 0.1052631579, IMAGE_HEIGHT * 0.3421052632, IMAGE_WIDTH * 0.7894736842, IMAGE_HEIGHT * 0.3157894737);
                break;

            case ROUND:

            default:
                LED = new Ellipse2D.Double(0.25 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
                break;
        }

        final Ellipse2D LED_CORONA = new Ellipse2D.Double(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        final Point2D LED_CENTER = new Point2D.Double(LED.getBounds2D().getCenterX(), LED.getBounds2D().getCenterY());

        final float[] LED_FRACTIONS = {
                0.0f,
                0.2f,
                1.0f
        };

        final Color[] LED_OFF_COLORS;
        final Color[] LED_ON_COLORS;
        final Color[] LED_ON_CORONA_COLORS;

        if (LED_COLOR != LedColor.CUSTOM) {
            LED_OFF_COLORS = new Color[]{
                    LED_COLOR.INNER_COLOR1_OFF,
                    LED_COLOR.INNER_COLOR2_OFF,
                    LED_COLOR.OUTER_COLOR_OFF
            };

            LED_ON_COLORS = new Color[]{
                    LED_COLOR.INNER_COLOR1_ON,
                    LED_COLOR.INNER_COLOR2_ON,
                    LED_COLOR.OUTER_COLOR_ON
            };

            LED_ON_CORONA_COLORS = new Color[]{
                    UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.4f),
                    UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.4f),
                    UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.25f),
                    UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.15f),
                    UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.05f),
                    UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.0f)
            };
        } else {
            LED_OFF_COLORS = new Color[]{
                    customLedColor.INNER_COLOR1_OFF,
                    customLedColor.INNER_COLOR2_OFF,
                    customLedColor.OUTER_COLOR_OFF
            };

            LED_ON_COLORS = new Color[]{
                    customLedColor.INNER_COLOR1_ON,
                    customLedColor.INNER_COLOR2_ON,
                    customLedColor.OUTER_COLOR_ON
            };

            LED_ON_CORONA_COLORS = new Color[]{
                    UTIL.setAlpha(customLedColor.CORONA_COLOR, 0.4f),
                    UTIL.setAlpha(customLedColor.CORONA_COLOR, 0.4f),
                    UTIL.setAlpha(customLedColor.CORONA_COLOR, 0.25f),
                    UTIL.setAlpha(customLedColor.CORONA_COLOR, 0.15f),
                    UTIL.setAlpha(customLedColor.CORONA_COLOR, 0.05f),
                    UTIL.setAlpha(customLedColor.CORONA_COLOR, 0.0f)
            };
        }

        final float[] LED_INNER_SHADOW_FRACTIONS = {
                0.0f,
                0.8f,
                1.0f
        };

        final Color[] LED_INNER_SHADOW_COLORS = {
                new Color(0.0f, 0.0f, 0.0f, 0.0f),
                new Color(0.0f, 0.0f, 0.0f, 0.0f),
                new Color(0.0f, 0.0f, 0.0f, 0.4f),};

        final float[] LED_ON_CORONA_FRACTIONS = {
                0.0f,
                0.6f,
                0.7f,
                0.8f,
                0.85f,
                1.0f
        };

        // Define gradients for the led
        final RadialGradientPaint LED_OFF_GRADIENT = new RadialGradientPaint(LED_CENTER, 0.25f * IMAGE_WIDTH, LED_FRACTIONS, LED_OFF_COLORS);
        final RadialGradientPaint LED_ON_GRADIENT = new RadialGradientPaint(LED_CENTER, 0.25f * IMAGE_WIDTH, LED_FRACTIONS, LED_ON_COLORS);
        final RadialGradientPaint LED_INNER_SHADOW_GRADIENT = new RadialGradientPaint(LED_CENTER, 0.25f * IMAGE_WIDTH, LED_INNER_SHADOW_FRACTIONS, LED_INNER_SHADOW_COLORS);
        final RadialGradientPaint LED_ON_CORONA_GRADIENT = new RadialGradientPaint(LED_CENTER, 0.5f * IMAGE_WIDTH, LED_ON_CORONA_FRACTIONS, LED_ON_CORONA_COLORS);


        // Define light reflex data
        final java.awt.Shape LED_LIGHTREFLEX;
        final Point2D LED_LIGHTREFLEX_START;
        final Point2D LED_LIGHTREFLEX_STOP;

        switch (LED_TYPE) {
            case RECT_VERTICAL:
                final GeneralPath VERTICAL_HL = new GeneralPath();
                VERTICAL_HL.setWindingRule(Path2D.WIND_EVEN_ODD);
                VERTICAL_HL.moveTo(IMAGE_WIDTH * 0.34210526315789475, IMAGE_HEIGHT * 0.10526315789473684);
                VERTICAL_HL.lineTo(IMAGE_WIDTH * 0.6578947368421053, IMAGE_HEIGHT * 0.10526315789473684);
                VERTICAL_HL.lineTo(IMAGE_WIDTH * 0.6578947368421053, IMAGE_HEIGHT * 0.3684210526315789);
                VERTICAL_HL.curveTo(IMAGE_WIDTH * 0.6578947368421053, IMAGE_HEIGHT * 0.3684210526315789, IMAGE_WIDTH * 0.631578947368421, IMAGE_HEIGHT * 0.42105263157894735, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.42105263157894735);
                VERTICAL_HL.curveTo(IMAGE_WIDTH * 0.3684210526315789, IMAGE_HEIGHT * 0.42105263157894735, IMAGE_WIDTH * 0.34210526315789475, IMAGE_HEIGHT * 0.3684210526315789, IMAGE_WIDTH * 0.34210526315789475, IMAGE_HEIGHT * 0.3684210526315789);
                VERTICAL_HL.lineTo(IMAGE_WIDTH * 0.34210526315789475, IMAGE_HEIGHT * 0.10526315789473684);
                VERTICAL_HL.closePath();
                LED_LIGHTREFLEX = VERTICAL_HL;
                LED_LIGHTREFLEX_START = new Point2D.Double(0, VERTICAL_HL.getBounds2D().getMinY());
                LED_LIGHTREFLEX_STOP = new Point2D.Double(0, VERTICAL_HL.getBounds2D().getMaxY());
                break;

            case RECT_HORIZONTAL:
                final GeneralPath HORIZONTAL_HL = new GeneralPath();
                HORIZONTAL_HL.setWindingRule(Path2D.WIND_EVEN_ODD);
                HORIZONTAL_HL.moveTo(IMAGE_WIDTH * 0.10526315789473684, IMAGE_HEIGHT * 0.34210526315789475);
                HORIZONTAL_HL.lineTo(IMAGE_WIDTH * 0.8947368421052632, IMAGE_HEIGHT * 0.34210526315789475);
                HORIZONTAL_HL.lineTo(IMAGE_WIDTH * 0.8947368421052632, IMAGE_HEIGHT * 0.42105263157894735);
                HORIZONTAL_HL.curveTo(IMAGE_WIDTH * 0.8947368421052632, IMAGE_HEIGHT * 0.42105263157894735, IMAGE_WIDTH * 0.7894736842105263, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
                HORIZONTAL_HL.curveTo(IMAGE_WIDTH * 0.21052631578947367, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.10526315789473684, IMAGE_HEIGHT * 0.42105263157894735, IMAGE_WIDTH * 0.10526315789473684, IMAGE_HEIGHT * 0.42105263157894735);
                HORIZONTAL_HL.lineTo(IMAGE_WIDTH * 0.10526315789473684, IMAGE_HEIGHT * 0.34210526315789475);
                HORIZONTAL_HL.closePath();
                LED_LIGHTREFLEX = HORIZONTAL_HL;
                LED_LIGHTREFLEX_START = new Point2D.Double(0, LED_LIGHTREFLEX.getBounds2D().getMinY());
                LED_LIGHTREFLEX_STOP = new Point2D.Double(0, LED_LIGHTREFLEX.getBounds2D().getMaxY());
                break;

            case ROUND:

            default:
                LED_LIGHTREFLEX = new Ellipse2D.Double(0.4 * IMAGE_WIDTH, 0.35 * IMAGE_WIDTH, 0.2 * IMAGE_WIDTH, 0.15 * IMAGE_WIDTH);
                LED_LIGHTREFLEX_START = new Point2D.Double(0, LED_LIGHTREFLEX.getBounds2D().getMinY());
                LED_LIGHTREFLEX_STOP = new Point2D.Double(0, LED_LIGHTREFLEX.getBounds2D().getMaxY());
                break;
        }

        final float[] LIGHT_REFLEX_FRACTIONS = {
                0.0f,
                1.0f
        };

        final Color[] LIGHTREFLEX_COLORS = {
                new Color(1.0f, 1.0f, 1.0f, 0.4f),
                new Color(1.0f, 1.0f, 1.0f, 0.0f)
        };

        // Define light reflex gradients
        final LinearGradientPaint LED_LIGHTREFLEX_GRADIENT = new LinearGradientPaint(LED_LIGHTREFLEX_START, LED_LIGHTREFLEX_STOP, LIGHT_REFLEX_FRACTIONS, LIGHTREFLEX_COLORS);

        switch (STATE) {
            case 1:
                // LED ON
                G2.setPaint(LED_ON_CORONA_GRADIENT);
                G2.fill(LED_CORONA);
                switch (LED_TYPE) {
                    case ROUND:
                        G2.setPaint(LED_ON_GRADIENT);
                        G2.fill(LED);
                        G2.setPaint(LED_INNER_SHADOW_GRADIENT);
                        G2.fill(LED);
                        break;

                    case RECT_VERTICAL:
                        G2.drawImage(Shadow.INSTANCE.createInnerShadow(LED, LED_ON_GRADIENT, 0, 0.65f, Color.BLACK, 20, 315), (int) (IMAGE_WIDTH * 0.3421052632), (int) (IMAGE_HEIGHT * 0.1052631579), null);
                        break;

                    case RECT_HORIZONTAL:
                        G2.drawImage(Shadow.INSTANCE.createInnerShadow(LED, LED_ON_GRADIENT, 0, 0.65f, Color.BLACK, 20, 315), (int) (IMAGE_WIDTH * 0.1052631579), (int) (IMAGE_HEIGHT * 0.3421052632), null);
                        break;
                }
                G2.setPaint(LED_LIGHTREFLEX_GRADIENT);
                G2.fill(LED_LIGHTREFLEX);
                break;

            case 0:

            default:
                // LED OFF
                switch (LED_TYPE) {
                    case ROUND:
                        G2.setPaint(LED_OFF_GRADIENT);
                        G2.fill(LED);
                        G2.setPaint(LED_INNER_SHADOW_GRADIENT);
                        G2.fill(LED);
                        break;

                    case RECT_VERTICAL:
                        G2.drawImage(Shadow.INSTANCE.createInnerShadow(LED, LED_OFF_GRADIENT, 0, 0.65f, Color.BLACK, 20, 315), (int) (IMAGE_WIDTH * 0.3421052632), (int) (IMAGE_HEIGHT * 0.1052631579), null);
                        break;

                    case RECT_HORIZONTAL:
                        G2.drawImage(Shadow.INSTANCE.createInnerShadow(LED, LED_OFF_GRADIENT, 0, 0.65f, Color.BLACK, 20, 315), (int) (IMAGE_WIDTH * 0.1052631579), (int) (IMAGE_HEIGHT * 0.3421052632), null);
                        break;
                }
                G2.setPaint(LED_LIGHTREFLEX_GRADIENT);
                G2.fill(LED_LIGHTREFLEX);
                break;
        }

        G2.dispose();

        CACHE.put(KEY.toString(), IMAGE);

        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related">
    /**
     * Calculates the rectangle that specifies the area that is available
     * for painting the gauge. This means that if the component has insets
     * that are larger than 0, these will be taken into account.
     */
    private void calcInnerBounds() {
//        final java.awt.Insets INSETS = getInsets();
//        if (getWidth() - INSETS.left - INSETS.right < getHeight() - INSETS.top - INSETS.bottom) {
//            INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, getWidth() - INSETS.left - INSETS.right, getHeight() - INSETS.top - INSETS.bottom);
//        } else {
//            INNER_BOUNDS.setBounds(INSETS.left + (int) (((double) (getWidth() - INSETS.left - INSETS.right) - (double) (getHeight() - INSETS.top - INSETS.bottom)) / 2.0), INSETS.top, getHeight() - INSETS.top - INSETS.bottom, getHeight() - INSETS.top - INSETS.bottom);
//        }

        final Insets INSETS = getInsets();
        final int SIZE = (getWidth() - INSETS.left - INSETS.right) <= (getHeight() - INSETS.top - INSETS.bottom) ? (getWidth() - INSETS.left - INSETS.right) : (getHeight() - INSETS.top - INSETS.bottom);
        //INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, getWidth() - INSETS.left - INSETS.right, getHeight() - INSETS.top - INSETS.bottom);
        INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, SIZE, SIZE);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if (dim.width < 16 || dim.height < 16) {
            dim = new Dimension(16, 16);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int  width = DIM.width < 16 ? 16 : DIM.width;
        int height = DIM.height < 16 ? 16 : DIM.height;
        final int SIZE = width <= height ? width : height;
        super.setMinimumSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(INNER_BOUNDS.width);
        initialized = true;
        invalidate();
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        if (dim.width > 1080 || dim.height > 1080) {
            dim = new Dimension(1080, 1080);
        }
        return dim;
    }

    @Override
    public void setMaximumSize(final Dimension DIM) {
        int  width = DIM.width > 1080 ? 1080 : DIM.width;
        int height = DIM.height > 1080 ? 1080 : DIM.height;
        final int SIZE = width <= height ? width : height;
        super.setMaximumSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(INNER_BOUNDS.width);
        initialized = true;
        invalidate();
        repaint();
    }

    @Override
    public void setPreferredSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setPreferredSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(INNER_BOUNDS.width);
        initialized = true;
        invalidate();
        repaint();
    }

    @Override
    public void setSize(final int WIDTH, final int HEIGHT) {
        final int SIZE = WIDTH <= HEIGHT ? WIDTH : HEIGHT;
        super.setSize(SIZE, SIZE);
        calcInnerBounds();
        init(INNER_BOUNDS.width);
        initialized = true;
    }

    @Override
    public void setSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(INNER_BOUNDS.width);
        initialized = true;
    }

    @Override
    public void setBounds(final Rectangle BOUNDS) {
        if (BOUNDS.width <= BOUNDS.height) {
            // vertical
            int yNew;
            switch(verticalAlignment) {
                case SwingConstants.TOP:
                    yNew = BOUNDS.y;
                    break;
                case SwingConstants.BOTTOM:
                    yNew = BOUNDS.y + (BOUNDS.height - BOUNDS.width);
                    break;
                case SwingConstants.CENTER:
                default:
                    yNew = BOUNDS.y + ((BOUNDS.height - BOUNDS.width) / 2);
                    break;
            }
            super.setBounds(BOUNDS.x, yNew, BOUNDS.width, BOUNDS.width);
        } else {
            // horizontal
            int xNew;
            switch(horizontalAlignment) {
                case SwingConstants.LEFT:
                    xNew = BOUNDS.x;
                    break;
                case SwingConstants.RIGHT:
                    xNew = BOUNDS.x + (BOUNDS.width - BOUNDS.height);
                    break;
                case SwingConstants.CENTER:
                default:
                    xNew = BOUNDS.x + ((BOUNDS.width - BOUNDS.height) / 2);
                    break;
            }
            super.setBounds(xNew, BOUNDS.y, BOUNDS.height, BOUNDS.height);
        }
        calcInnerBounds();
        init(INNER_BOUNDS.width);
        initialized = true;
    }

    @Override
    public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
        if (WIDTH <= HEIGHT) {
            // vertical
            int yNew;
            switch(verticalAlignment) {
                case SwingConstants.TOP:
                    yNew = Y;
                    break;
                case SwingConstants.BOTTOM:
                    yNew = Y + (HEIGHT - WIDTH);
                    break;
                case SwingConstants.CENTER:
                default:
                    yNew = Y + ((HEIGHT - WIDTH) / 2);
                    break;
            }
            super.setBounds(X, yNew, WIDTH, WIDTH);
        } else {
            // horizontal
            int xNew;
            switch(horizontalAlignment) {
                case SwingConstants.LEFT:
                    xNew = X;
                    break;
                case SwingConstants.RIGHT:
                    xNew = X + (WIDTH - HEIGHT);
                    break;
                case SwingConstants.CENTER:
                default:
                    xNew = X + ((WIDTH - HEIGHT) / 2);
                    break;
            }
            super.setBounds(xNew, Y, HEIGHT, HEIGHT);
        }
        calcInnerBounds();
        init(INNER_BOUNDS.width);
        initialized = true;
    }

    @Override
    public void setBorder(Border BORDER) {
        super.setBorder(BORDER);
        calcInnerBounds();
        init(INNER_BOUNDS.width);
    }

    /**
     * Returns the alignment of the radial gauge along the X axis.
     * @return the alignment of the radial gauge along the X axis.
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the X axis.
     * @param HORIZONTAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    public void setHorizontalAlignment(final int HORIZONTAL_ALIGNMENT) {
        horizontalAlignment = HORIZONTAL_ALIGNMENT;
    }

    /**
     * Returns the alignment of the radial gauge along the Y axis.
     * @return the alignment of the radial gauge along the Y axis.
     */
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the Y axis.
     * @param VERTICAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    public void setVerticalAlignment(final int VERTICAL_ALIGNMENT) {
        verticalAlignment = VERTICAL_ALIGNMENT;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Miscellaneous">
    public void dispose() {
        LED_BLINKING_TIMER.removeActionListener(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Action listener method">
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource().equals(LED_BLINKING_TIMER)) {
            currentLedImage = ledOn == true ? getLedImageOn() : getLedImageOff();
            ledOn ^= true;

            repaint();
        }
    }
    // </editor-fold>

    @Override public String toString() {
        return "LED";
    }
}