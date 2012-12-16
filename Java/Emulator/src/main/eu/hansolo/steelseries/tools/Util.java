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
package eu.hansolo.steelseries.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A set of handy methods that will be used all over
 * the place.
 * @author hansolo
 */
public enum Util {

    INSTANCE;
    private final float INT_TO_FLOAT_CONST = 1f / 255f;
    private final Pattern NUMBERS_ONLY = Pattern.compile("^[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?$");
    private final Matcher MATCHES_NUMBERS = NUMBERS_ONLY.matcher("");
    private Font digitalFont = null;
    private final Font STANDARD_FONT = new Font("Verdana", 1, 24);
    private final Rectangle2D TEXT_BOUNDARY = new Rectangle2D.Double(0, 0, 10, 10);

    /**
     * A class that contains some useful methods related to the PointOfInterest class and
     * to general ui related things.
     */
    Util() {
        try {
            digitalFont = Font.createFont(0, this.getClass().getResourceAsStream("/eu/hansolo/steelseries/resources/digital.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(digitalFont);
        } catch (FontFormatException exception) {
        } catch (java.io.IOException exception) {
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Utility methods">
    //********************************** UI related utils **************************************************************
    /**
     * It will take the font from the given Graphics2D object and returns a shape of the given TEXT
     * that is rotated by the ROTATION_ANGLE around it's center which is defined
     * by TEXT_POSITION_X and TEXT_POSITION_Y. It will take the font's descent into account so that
     * the rotated text will be centered correctly even if it doesn't contain characters with descent.
     * @param G2
     * @param TEXT
     * @param TEXT_POSITION_X
     * @param TEXT_POSITION_Y
     * @param ROTATION_ANGLE
     * @return Glyph that is a shape of the given string rotated around it's center.
     */
    public Shape rotateTextAroundCenter(final Graphics2D G2, final String TEXT, final int TEXT_POSITION_X, final int TEXT_POSITION_Y, final double ROTATION_ANGLE) {
        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
        final TextLayout TEXT_LAYOUT = new TextLayout(TEXT, G2.getFont(), RENDER_CONTEXT);

        // Check if need to take the fonts descent into account
        final float DESCENT;
        MATCHES_NUMBERS.reset(TEXT);
        if (MATCHES_NUMBERS.matches()) {
            DESCENT = TEXT_LAYOUT.getDescent();
        } else {
            DESCENT = 0;
        }
        final Rectangle2D TEXT_BOUNDS = TEXT_LAYOUT.getBounds();
        TEXT_BOUNDARY.setRect(TEXT_BOUNDS.getMinX(), TEXT_BOUNDS.getMinY(), TEXT_BOUNDS.getWidth(), TEXT_BOUNDS.getHeight() + DESCENT / 2);

        final GlyphVector GLYPH_VECTOR = G2.getFont().createGlyphVector(RENDER_CONTEXT, TEXT);

        final java.awt.Shape GLYPH = GLYPH_VECTOR.getOutline((int) -TEXT_BOUNDARY.getCenterX(), 2 * (int) TEXT_BOUNDARY.getCenterY());

        final AffineTransform OLD_TRANSFORM = G2.getTransform();
        G2.translate(TEXT_POSITION_X, TEXT_POSITION_Y + TEXT_BOUNDARY.getHeight());

        G2.rotate(ROTATION_ANGLE, -TEXT_BOUNDARY.getCenterX() + TEXT_BOUNDARY.getWidth() / 2, TEXT_BOUNDARY.getCenterY() - (TEXT_BOUNDARY.getHeight() + DESCENT) / 2);
        G2.fill(GLYPH);

        G2.setTransform(OLD_TRANSFORM);

        return GLYPH;
    }

    /**
     * Calculates the centered position of the given text in the given boundary and
     * the given graphics2d object. This is really useful when centering text on buttons or other components.
     * @param G2
     * @param BOUNDARY
     * @param TEXT
     * @return a point2d that defines the position of the given text centered in the given rectangle
     */
    public Point2D getCenteredTextPosition(final Graphics2D G2, final Rectangle2D BOUNDARY, final String TEXT) {
        return getCenteredTextPosition(G2, BOUNDARY, G2.getFont(), TEXT);
    }

    /**
     * Calculates the centered position of the given text in the given boundary, with the given font and
     * the given graphics2d object. This is really useful when centering text on buttons or other components.
     * @param G2
     * @param BOUNDARY
     * @param FONT
     * @param TEXT
     * @return a point2d that defines the position of the given text centered in the given rectangle
     */
    public Point2D getCenteredTextPosition(final Graphics2D G2, final Rectangle2D BOUNDARY, final Font FONT, final String TEXT) {
        // Get the visual center of the component.
        final double CENTER_X = BOUNDARY.getWidth() / 2.0;
        final double CENTER_Y = BOUNDARY.getHeight() / 2.0;

        // Get the text boundary
        final FontRenderContext RENDER_CONTEXT = G2.getFontRenderContext();
        final TextLayout LAYOUT = new TextLayout(TEXT, FONT, RENDER_CONTEXT);
        final Rectangle2D TEXT_BOUNDS = LAYOUT.getBounds();

        // Calculate the text position
        final double TEXT_X = CENTER_X - TEXT_BOUNDS.getWidth() / 2.0;
        final double TEXT_Y = CENTER_Y - TEXT_BOUNDS.getHeight() / 2.0 + TEXT_BOUNDS.getHeight();

        return new Point2D.Double(TEXT_X, TEXT_Y);
    }

    /**
     * This method was taken from the great book "Filthy Rich Clients"
     * from Chet Haase and Romain Guy
     *
     * Convenience method that returns a scaled instance of the
     * provided BufferedImage.
     *
     * @param IMAGE the original image to be scaled
     * @param TARGET_WIDTH the desired width of the scaled instance,
     *    in pixels
     * @param TARGET_HEIGHT the desired height of the scaled instance,
     *    in pixels
     * @param HINT one of the rendering hints that corresponds to
     *    RenderingHints.KEY_INTERPOLATION (e.g.
     *    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
     *    RenderingHints.VALUE_INTERPOLATION_BILINEAR,
     *    RenderingHints.VALUE_INTERPOLATION_BICUBIC)
     * @return a scaled version of the original BufferedImage
     */
    public BufferedImage getScaledInstance(final BufferedImage IMAGE, final int TARGET_WIDTH, final int TARGET_HEIGHT, final Object HINT) {
        final int TYPE = (IMAGE.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = IMAGE;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;
        final int WIDTH = TARGET_WIDTH;
        final int HEIGHT = TARGET_HEIGHT;
        int previewWidth = ret.getWidth();
        int previewHeight = ret.getHeight();

        if (scratchImage == null) {
            scratchImage = new BufferedImage(WIDTH, HEIGHT, TYPE);
            g2 = scratchImage.createGraphics();
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, HINT);
        g2.drawImage(ret, 0, 0, WIDTH, HEIGHT, 0, 0, previewWidth, previewHeight, null);

        ret = scratchImage;

        g2.dispose();

        if (TARGET_WIDTH != ret.getWidth() || TARGET_HEIGHT != ret.getHeight()) {
            scratchImage = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, TYPE);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }

        return ret;
    }

    /**
     * Creates a image that contains the reflection of the given sourceimage.
     * This could be useful whereever you need some eyecandy. Here we use the good working
     * standard values for opacity = 0.5f and fade out height = 0.7f.
     * @param SOURCE_IMAGE
     * @return a new buffered image that contains the reflection of the original image
     */
    public BufferedImage createReflectionImage(final BufferedImage SOURCE_IMAGE) {
        return createReflectionImage(SOURCE_IMAGE, 0.5f, 0.7f);
    }

    /**
     * Creates a image that contains the reflection of the given sourceimage.
     * This could be useful whereever you need some eyecandy.
     * @param SOURCE_IMAGE
     * @param OPACITY a good standard value is 0.5f
     * @param FADE_OUT_HEIGHT a good standard value is 0.7f
     * @return a new buffered image that contains the reflection of the original image
     */
    public BufferedImage createReflectionImage(final BufferedImage SOURCE_IMAGE, final float OPACITY, final float FADE_OUT_HEIGHT) {
        final BufferedImage REFLECTION_IMAGE = new BufferedImage(SOURCE_IMAGE.getWidth(), SOURCE_IMAGE.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final BufferedImage BLURED_REFLECTION_IMAGE = new BufferedImage(SOURCE_IMAGE.getWidth(), SOURCE_IMAGE.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D G2 = REFLECTION_IMAGE.createGraphics();

        G2.translate(0, SOURCE_IMAGE.getHeight());
        G2.scale(1, -1);
        G2.drawRenderedImage(SOURCE_IMAGE, null);
        G2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.DST_IN));
        G2.setPaint(new java.awt.GradientPaint(0, SOURCE_IMAGE.getHeight() * FADE_OUT_HEIGHT, new Color(0.0f, 0.0f, 0.0f, 0.0f), 0, SOURCE_IMAGE.getHeight(), new Color(0.0f, 0.0f, 0.0f, OPACITY)));
        G2.fillRect(0, 0, SOURCE_IMAGE.getWidth(), SOURCE_IMAGE.getHeight());
        G2.dispose();

        // Blur the reflection to make it look more realistic
        float[] data = {
                0.0625f,
                0.125f,
                0.0625f,
                0.125f,
                0.25f,
                0.125f,
                0.0625f,
                0.125f,
                0.0625f
        };
        final java.awt.image.Kernel KERNEL = new java.awt.image.Kernel(3, 3, data);
        final java.awt.image.ConvolveOp CONVOLE = new java.awt.image.ConvolveOp(KERNEL, java.awt.image.ConvolveOp.EDGE_NO_OP, null);
        CONVOLE.filter(REFLECTION_IMAGE, BLURED_REFLECTION_IMAGE);

        return BLURED_REFLECTION_IMAGE;
    }

    /**
     * Creates a texture with a brushed metal look. The code originaly comes from Jerry Huxtable.
     * If you don't know his Java image related stuff you have to check out http://huxtable.com/
     * @param WIDTH
     * @param HEIGHT
     * @param COLOR
     * @return a buffered image that contains a brushed metal texture
     */
    public BufferedImage createBrushMetalTexture(final Color COLOR, final int WIDTH, final int HEIGHT) {
        return createBrushMetalTexture(COLOR, WIDTH, HEIGHT, 5, 0.1f, true, 0.3f);
    }

    /**
     * Creates a texture with a brushed metal look. The code originaly comes from Jerry Huxtable.
     * If you don't know his Java image related stuff you have to check out http://huxtable.com/
     * @param WIDTH
     * @param HEIGHT
     * @param COLOR
     * @param RADIUS
     * @param AMOUNT
     * @param MONOCHROME
     * @param SHINE
     * @return a buffered image that contains a brushed metal texture
     */
    public BufferedImage createBrushMetalTexture(final Color COLOR, final int WIDTH, final int HEIGHT, final int RADIUS, final float AMOUNT, final boolean MONOCHROME, final float SHINE) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        final BufferedImage IMAGE = createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        BrushedMetalFilter metalBrush = new BrushedMetalFilter();
        if (COLOR != null) {
            metalBrush.setColor(COLOR.getRGB());
        }
        metalBrush.setAmount(AMOUNT);
        metalBrush.setMonochrome(MONOCHROME);
        metalBrush.setShine(SHINE);
        metalBrush.setRadius(RADIUS);
        return metalBrush.filter(IMAGE, IMAGE);
    }

    /**
     * Creates a texture with a linen look. The code is derived from Jerry Huxtables BrushMetalTexture.
     * A linen texture is in principle the same as a brushed metal texture but instead of adding a motion blur to
     * the noise pattern in only one direction you add a second motion blur to the orthogonal direction.
     * @param COLOR
     * @param WIDTH
     * @param HEIGHT
     * @return a buffered image that contains a linen texture
     */
    public BufferedImage createLinenTexture(final Color COLOR, final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        final BufferedImage IMAGE1 = createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final BufferedImage IMAGE2 = createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        LinenFilter linenBrush = new LinenFilter();
        if (COLOR != null) {
            linenBrush.setColor(COLOR.getRGB());
        }
        final AlphaComposite COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        final BufferedImage RESULT = createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = RESULT.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        linenBrush.setShine(0.25f);
        linenBrush.setAmount(0.65f);
        linenBrush.setRadius(60);
        G2.drawImage(linenBrush.filter(IMAGE1, IMAGE1), 0, 0, null);
        G2.setComposite(COMPOSITE);
        linenBrush.setOrientation(Orientation.VERTICAL);
        linenBrush.setAmount(0.7f);
        linenBrush.setRadius(55);
        G2.drawImage(linenBrush.filter(IMAGE2, IMAGE2), 0, 0, null);
        G2.dispose();

        return RESULT;
    }

    /**
     * Returns a buffered image that contains a texture of a grinded stainless steel plate.
     * A good default value for the size is 100 px.
     * @param SIZE
     * @return a buffered image that contains a texture of a grinded stainless steel plate
     */
    public BufferedImage create_STAINLESS_STEEL_PLATE_Texture(final int SIZE) {
        if (SIZE <= 0) {
            return createImage(1, 1, Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = createImage(SIZE, SIZE, Transparency.TRANSLUCENT);

        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final Ellipse2D STEEL_CIRCLE = new Ellipse2D.Double(0, 0, SIZE / 2.0, SIZE / 2.0);
        final Point2D CENTER = new Point2D.Double(STEEL_CIRCLE.getCenterX(), STEEL_CIRCLE.getCenterY());
        final float[] FRACTIONS = {
                0f,
                0.03f,
                0.10f,
                0.14f,
                0.24f,
                0.33f,
                0.38f,
                0.5f,
                0.62f,
                0.67f,
                0.76f,
                0.81f,
                0.85f,
                0.97f,
                1.0f
        };

        final Color[] COLORS = {
                new Color(0xFDFDFD),
                new Color(0xFDFDFD),
                new Color(0xB2B2B4),
                new Color(0xACACAE),
                new Color(0xFDFDFD),
                new Color(0x6E6E70),
                new Color(0x6E6E70),
                new Color(0xFDFDFD),
                new Color(0x6E6E70),
                new Color(0x6E6E70),
                new Color(0xFDFDFD),
                new Color(0xACACAE),
                new Color(0xB2B2B4),
                new Color(0xFDFDFD),
                new Color(0xFDFDFD)
        };
        final ConicalGradientPaint GRADIENT = new ConicalGradientPaint(false, CENTER, -0.45f, FRACTIONS, COLORS);
        int amount;
        final double TRANSLATE_STEP = SIZE / 4.0;
        G2.translate(-TRANSLATE_STEP, -TRANSLATE_STEP);
        final AffineTransform OLD = G2.getTransform();
        for (int y = 0; y < 5; y++) {
            if (y % 2 == 0) {
                amount = 3;
                G2.translate(0, TRANSLATE_STEP * y);
            } else {
                amount = 2;
                G2.translate(TRANSLATE_STEP, TRANSLATE_STEP * y);
            }

            for (int x = 0; x < amount; x++) {
                G2.setPaint(GRADIENT);
                G2.fill(STEEL_CIRCLE);
                G2.translate(SIZE / 2.0, 0);
            }
            G2.setTransform(OLD);
        }
        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a buffered image that contains a texture of carbon fibre.
     * @param SIZE
     * @return a buffered image that contains a texture of carbon fibre.
     */
    public BufferedImage create_CARBON_Texture(final int SIZE) {
        if (SIZE <= 0) {
            return createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final BufferedImage IMAGE = createImage(SIZE, SIZE, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final Rectangle2D RULB = new Rectangle2D.Double(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final Point2D RULB_START = new Point2D.Double(0, RULB.getBounds2D().getMinY());
        final Point2D RULB_STOP = new Point2D.Double(0, RULB.getBounds2D().getMaxY());
        final float[] RULB_FRACTIONS = {
                0.0f,
                1.0f
        };
        final Color[] RULB_COLORS = {
                new Color(35, 35, 35, 255),
                new Color(23, 23, 23, 255)
        };
        final LinearGradientPaint RULB_GRADIENT = new LinearGradientPaint(RULB_START, RULB_STOP, RULB_FRACTIONS, RULB_COLORS);
        G2.setPaint(RULB_GRADIENT);
        G2.fill(RULB);

        final Rectangle2D RULF = new Rectangle2D.Double(IMAGE_WIDTH * 0.08333333333333333, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 0.3333333333333333, IMAGE_HEIGHT * 0.4166666666666667);
        final Point2D RULF_START = new Point2D.Double(0, RULF.getBounds2D().getMinY());
        final Point2D RULF_STOP = new Point2D.Double(0, RULF.getBounds2D().getMaxY());
        final float[] RULF_FRACTIONS = {
                0.0f,
                1.0f
        };
        final Color[] RULF_COLORS = {
                new Color(38, 38, 38, 255),
                new Color(30, 30, 30, 255)
        };
        final LinearGradientPaint RULF_GRADIENT = new LinearGradientPaint(RULF_START, RULF_STOP, RULF_FRACTIONS, RULF_COLORS);
        G2.setPaint(RULF_GRADIENT);
        G2.fill(RULF);

        final Rectangle2D RLRB = new Rectangle2D.Double(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final Point2D RLRB_START = new Point2D.Double(0, RLRB.getBounds2D().getMinY());
        final Point2D RLRB_STOP = new Point2D.Double(0, RLRB.getBounds2D().getMaxY());
        final float[] RLRB_FRACTIONS = {
                0.0f,
                1.0f
        };
        final Color[] RLRB_COLORS = {
                new Color(35, 35, 35, 255),
                new Color(23, 23, 23, 255)
        };
        final LinearGradientPaint RLRB_GRADIENT = new LinearGradientPaint(RLRB_START, RLRB_STOP, RLRB_FRACTIONS, RLRB_COLORS);
        G2.setPaint(RLRB_GRADIENT);
        G2.fill(RLRB);

        final Rectangle2D RLRF = new Rectangle2D.Double(IMAGE_WIDTH * 0.5833333333333334, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.3333333333333333, IMAGE_HEIGHT * 0.4166666666666667);
        final Point2D RLRF_START = new Point2D.Double(0, RLRF.getBounds2D().getMinY());
        final Point2D RLRF_STOP = new Point2D.Double(0, RLRF.getBounds2D().getMaxY());
        final float[] RLRF_FRACTIONS = {
                0.0f,
                1.0f
        };
        final Color[] RLRF_COLORS = {
                new Color(38, 38, 38, 255),
                new Color(30, 30, 30, 255)
        };
        final LinearGradientPaint RLRF_GRADIENT = new LinearGradientPaint(RLRF_START, RLRF_STOP, RLRF_FRACTIONS, RLRF_COLORS);
        G2.setPaint(RLRF_GRADIENT);
        G2.fill(RLRF);

        final Rectangle2D RURB = new Rectangle2D.Double(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final Point2D RURB_START = new Point2D.Double(0, RURB.getBounds2D().getMinY());
        final Point2D RURB_STOP = new Point2D.Double(0, RURB.getBounds2D().getMaxY());
        final float[] RURB_FRACTIONS = {
                0.0f,
                1.0f
        };
        final Color[] RURB_COLORS = {
                new Color(48, 48, 48, 255),
                new Color(40, 40, 40, 255)
        };
        final LinearGradientPaint RURB_GRADIENT = new LinearGradientPaint(RURB_START, RURB_STOP, RURB_FRACTIONS, RURB_COLORS);
        G2.setPaint(RURB_GRADIENT);
        G2.fill(RURB);

        final Rectangle2D RURF = new Rectangle2D.Double(IMAGE_WIDTH * 0.5833333333333334, IMAGE_HEIGHT * 0.08333333333333333, IMAGE_WIDTH * 0.3333333333333333, IMAGE_HEIGHT * 0.4166666666666667);
        final Point2D RURF_START = new Point2D.Double(0, RURF.getBounds2D().getMinY());
        final Point2D RURF_STOP = new Point2D.Double(0, RURF.getBounds2D().getMaxY());
        final float[] RURF_FRACTIONS = {
                0.0f,
                1.0f
        };
        final Color[] RURF_COLORS = {
                new Color(53, 53, 53, 255),
                new Color(45, 45, 45, 255)
        };
        final LinearGradientPaint RURF_GRADIENT = new LinearGradientPaint(RURF_START, RURF_STOP, RURF_FRACTIONS, RURF_COLORS);
        G2.setPaint(RURF_GRADIENT);
        G2.fill(RURF);

        final Rectangle2D RLLB = new Rectangle2D.Double(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final Point2D RLLB_START = new Point2D.Double(0, RLLB.getBounds2D().getMinY());
        final Point2D RLLB_STOP = new Point2D.Double(0, RLLB.getBounds2D().getMaxY());
        final float[] RLLB_FRACTIONS = {
                0.0f,
                1.0f
        };
        final Color[] RLLB_COLORS = {
                new Color(48, 48, 48, 255),
                new Color(40, 40, 40, 255)
        };
        final LinearGradientPaint RLLB_GRADIENT = new LinearGradientPaint(RLLB_START, RLLB_STOP, RLLB_FRACTIONS, RLLB_COLORS);
        G2.setPaint(RLLB_GRADIENT);
        G2.fill(RLLB);

        final Rectangle2D RLLF = new Rectangle2D.Double(IMAGE_WIDTH * 0.08333333333333333, IMAGE_HEIGHT * 0.5833333333333334, IMAGE_WIDTH * 0.3333333333333333, IMAGE_HEIGHT * 0.4166666666666667);
        final Point2D RLLF_START = new Point2D.Double(0, RLLF.getBounds2D().getMinY());
        final Point2D RLLF_STOP = new Point2D.Double(0, RLLF.getBounds2D().getMaxY());
        final float[] RLLF_FRACTIONS = {
                0.0f,
                1.0f
        };
        final Color[] RLLF_COLORS = {
                new Color(53, 53, 53, 255),
                new Color(45, 45, 45, 255)
        };
        final LinearGradientPaint RLLF_GRADIENT = new LinearGradientPaint(RLLF_START, RLLF_STOP, RLLF_FRACTIONS, RLLF_COLORS);
        G2.setPaint(RLLF_GRADIENT);
        G2.fill(RLLF);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a buffered image that contains a texture of dark punched sheet.
     * @param SIZE
     * @param TEXTURE_COLOR
     * @return a buffered image that contains a texture of dark punched sheet.
     */
    public BufferedImage create_PUNCHED_SHEET_Image(final int SIZE, final Color TEXTURE_COLOR) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        if (SIZE <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, Transparency.TRANSLUCENT);
        }

        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(SIZE, SIZE, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final Rectangle2D BACK = new Rectangle2D.Double(0.0, 0.0, IMAGE_WIDTH, IMAGE_HEIGHT);
        G2.setColor(TEXTURE_COLOR);
        G2.fill(BACK);

        //final Color DARK = new Color(0x050506);
        final Color DARK = TEXTURE_COLOR.darker().darker();
        final float[] FRACTIONS = {
                0.0f,
                1.0f
        };
        final Color[] COLORS = {
                new Color(0, 0, 0, 255),
                new Color(68, 68, 68, 255)
        };

        final Ellipse2D ULB = new Ellipse2D.Double(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.06666667014360428, IMAGE_WIDTH * 0.4000000059604645, IMAGE_HEIGHT * 0.4000000059604645);
        final Point2D ULB_START = new Point2D.Double(0, ULB.getBounds2D().getMinY());
        final Point2D ULB_STOP = new Point2D.Double(0, ULB.getBounds2D().getMaxY());
        final LinearGradientPaint ULB_GRADIENT = new LinearGradientPaint(ULB_START, ULB_STOP, FRACTIONS, COLORS);
        G2.setPaint(ULB_GRADIENT);
        G2.fill(ULB);

        final Ellipse2D ULF = new Ellipse2D.Double(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 0.4000000059604645, IMAGE_HEIGHT * 0.4000000059604645);
        G2.setColor(DARK);
        G2.fill(ULF);

        final Ellipse2D LRB = new Ellipse2D.Double(IMAGE_WIDTH * 0.46666666865348816, IMAGE_HEIGHT * 0.5333333611488342, IMAGE_WIDTH * 0.4000000059604645, IMAGE_HEIGHT * 0.3999999761581421);
        final Point2D LRB_START = new Point2D.Double(0, LRB.getBounds2D().getMinY());
        final Point2D LRB_STOP = new Point2D.Double(0, LRB.getBounds2D().getMaxY());
        final LinearGradientPaint LRB_GRADIENT = new LinearGradientPaint(LRB_START, LRB_STOP, FRACTIONS, COLORS);
        G2.setPaint(LRB_GRADIENT);
        G2.fill(LRB);

        final Ellipse2D LRF = new Ellipse2D.Double(IMAGE_WIDTH * 0.46666666865348816, IMAGE_HEIGHT * 0.46666666865348816, IMAGE_WIDTH * 0.4000000059604645, IMAGE_HEIGHT * 0.4000000059604645);
        G2.setColor(DARK);
        G2.fill(LRF);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a buffered image that contains a simple random noise
     * @param WIDTH
     * @param HEIGHT
     * @param COLOR
     * @return a buffered image that contains a simple random noise
     */
    public BufferedImage create_NOISE_Image(final int WIDTH, final int HEIGHT, final Color COLOR) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, Transparency.TRANSLUCENT);
        }

        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final Color DARK_NOISE = COLOR.darker();
        final Color BRIGHT_NOISE = COLOR.brighter();

        final Random BW_RND = new Random();
        final Random ALPHA_RND = new Random();
        Color noiseColor;
        int noiseAlpha;
        for (int y = 0; y < IMAGE_HEIGHT; y++) {
            for (int x = 0; x < IMAGE_WIDTH; x++) {
                if (BW_RND.nextBoolean()) {
                    noiseColor = BRIGHT_NOISE;
                } else {
                    noiseColor = DARK_NOISE;
                }
                noiseAlpha = 90 + ALPHA_RND.nextInt(90) - 45;
                G2.setColor(new Color(noiseColor.getRed(), noiseColor.getGreen(), noiseColor.getBlue(), noiseAlpha));
                G2.drawLine(x, y, x, y);
            }
        }
        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a compatible image of the given size and transparency
     * @param WIDTH
     * @param HEIGHT
     * @param TRANSPARENCY
     * @return a compatible image of the given size and transparency
     */
    public BufferedImage createImage(final int WIDTH, final int HEIGHT, final int TRANSPARENCY) {
        GraphicsConfiguration gfxConf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return gfxConf.createCompatibleImage(1, 1, TRANSPARENCY);
        }
        return gfxConf.createCompatibleImage(WIDTH, HEIGHT, TRANSPARENCY);
    }

    /**
     * Returns the given COLOR with the given ALPHA transparency
     * @param COLOR
     * @param ALPHA
     * @return Color with the given float transparency
     */
    public Color setAlpha(final Color COLOR, final float ALPHA) {
        if (ALPHA > 1) {
            return setAlpha(COLOR, 255);
        }
        if (ALPHA < 0) {
            return setAlpha(COLOR, 0);
        }
        return setAlpha(COLOR, (int) (Math.ceil(255 * ALPHA)));
    }

    /**
     * Return the given COLOR with the given ALPHA transparency
     * @param COLOR
     * @param ALPHA
     * @return Color with given integer transparency
     */
    public Color setAlpha(final Color COLOR, final int ALPHA) {
        return new Color(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), ALPHA);
    }

    /**
     * Returns the color that equals the value from CURRENT_FRACTION in a RANGE of values
     * where the start of the RANGE equals the SOURCE_COLOR and the end of the RANGE
     * equals the DESTINATION_COLOR. In other words you could get any color in a gradient
     * between to colors by a given value.
     * @param SOURCE_COLOR
     * @param DESTINATION_COLOR
     * @param RANGE
     * @param CURRENT_FRACTION
     * @return Color that was calculated by a fraction from a range of values.
     */
    public Color getColorFromFraction(final Color SOURCE_COLOR, final Color DESTINATION_COLOR, final int RANGE, final int CURRENT_FRACTION) {
        final float SOURCE_RED = SOURCE_COLOR.getRed() * INT_TO_FLOAT_CONST;
        final float SOURCE_GREEN = SOURCE_COLOR.getGreen() * INT_TO_FLOAT_CONST;
        final float SOURCE_BLUE = SOURCE_COLOR.getBlue() * INT_TO_FLOAT_CONST;
        final float SOURCE_ALPHA = SOURCE_COLOR.getAlpha() * INT_TO_FLOAT_CONST;

        final float DESTINATION_RED = DESTINATION_COLOR.getRed() * INT_TO_FLOAT_CONST;
        final float DESTINATION_GREEN = DESTINATION_COLOR.getGreen() * INT_TO_FLOAT_CONST;
        final float DESTINATION_BLUE = DESTINATION_COLOR.getBlue() * INT_TO_FLOAT_CONST;
        final float DESTINATION_ALPHA = DESTINATION_COLOR.getAlpha() * INT_TO_FLOAT_CONST;

        final float RED_DELTA = DESTINATION_RED - SOURCE_RED;
        final float GREEN_DELTA = DESTINATION_GREEN - SOURCE_GREEN;
        final float BLUE_DELTA = DESTINATION_BLUE - SOURCE_BLUE;
        final float ALPHA_DELTA = DESTINATION_ALPHA - SOURCE_ALPHA;

        final float RED_FRACTION = RED_DELTA / RANGE;
        final float GREEN_FRACTION = GREEN_DELTA / RANGE;
        final float BLUE_FRACTION = BLUE_DELTA / RANGE;
        final float ALPHA_FRACTION = ALPHA_DELTA / RANGE;

        return new Color(SOURCE_RED + RED_FRACTION * CURRENT_FRACTION, SOURCE_GREEN + GREEN_FRACTION * CURRENT_FRACTION, SOURCE_BLUE + BLUE_FRACTION * CURRENT_FRACTION, SOURCE_ALPHA + ALPHA_FRACTION * CURRENT_FRACTION);
    }

    /**
     * Returns the interpolated color that you get if you multiply the delta between
     * color2 and color1 with the given fraction (for each channel) and interpolation. The fraction should
     * be a value between 0 and 1.
     * @param COLOR1 The first color as integer in the hex format 0xALPHA RED GREEN BLUE, e.g. 0xFF00FF00 for a pure green
     * @param COLOR2 The second color as integer in the hex format 0xALPHA RED GREEN BLUE e.g. 0xFFFF0000 for a pure red
     * @param FRACTION The fraction between those two colors that we would like to get e.g. 0.5f will result in the color 0xFF808000
     * @return the interpolated color between color1 and color2 calculated by the given fraction and interpolation
     */
    public Color interpolateColor(final Color COLOR1, final Color COLOR2, final float FRACTION) {
        assert (Float.compare(FRACTION, 0f) >= 0 && Float.compare(FRACTION, 1f) <= 0);

        final float RED1 = COLOR1.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN1 = COLOR1.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE1 = COLOR1.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA1 = COLOR1.getAlpha() * INT_TO_FLOAT_CONST;

        final float RED2 = COLOR2.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN2 = COLOR2.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE2 = COLOR2.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA2 = COLOR2.getAlpha() * INT_TO_FLOAT_CONST;

        final float DELTA_RED = RED2 - RED1;
        final float DELTA_GREEN = GREEN2 - GREEN1;
        final float DELTA_BLUE = BLUE2 - BLUE1;
        final float DELTA_ALPHA = ALPHA2 - ALPHA1;

        float red = RED1 + (DELTA_RED * FRACTION);
        float green = GREEN1 + (DELTA_GREEN * FRACTION);
        float blue = BLUE1 + (DELTA_BLUE * FRACTION);
        float alpha = ALPHA1 + (DELTA_ALPHA * FRACTION);

        red = red < 0f ? 0f : (red > 1f ? 1f : red);
        green = green < 0f ? 0f : (green > 1f ? 1f : green);
        blue = blue < 0f ? 0f : (blue > 1f ? 1f : blue);
        alpha = alpha < 0f ? 0f : (alpha > 1f ? 1f : alpha);

        return new Color(red, green, blue, alpha);
    }

    /**
     * Returns the color calculated by a bilinear interpolation by the two fractions in x and y direction.
     * To get the color of the point defined by FRACTION_X and FRACTION_Y with in the rectangle defined by the
     * for given colors we first calculate the interpolated color between COLOR_00 and COLOR_10 (x-direction) with
     * the given FRACTION_X. After that we calculate the interpolated color between COLOR_01 and COLOR_11 (x-direction)
     * with the given FRACTION_X. Now we interpolate between the two results of the former calculations (y-direction)
     * with the given FRACTION_Y.
     * @param COLOR_UL The color on the lower left corner of the square
     * @param COLOR_UR The color on the lower right corner of the square
     * @param COLOR_LL The color on the upper left corner of the square
     * @param COLOR_LR The color on the upper right corner of the square
     * @param FRACTION_X The fraction of the point in x direction (between COLOR_00 and COLOR_10 or COLOR_01 and COLOR_11) range: 0.0f .. 1.0f
     * @param FRACTION_Y The fraction of the point in y direction (between COLOR_00 and COLOR_01 or COLOR_10 and COLOR_11) range: 0.0f .. 1.0f
     * @return the color of the point defined by fraction_x and fraction_y in the square defined by the for colors
     */
    public Color bilinearInterpolateColor(final Color COLOR_UL, final Color COLOR_UR, final Color COLOR_LL, final Color COLOR_LR, final float FRACTION_X, final float FRACTION_Y) {
        final Color INTERPOLATED_COLOR_X1 = interpolateColor(COLOR_UL, COLOR_UR, FRACTION_X);
        final Color INTERPOLATED_COLOR_X2 = interpolateColor(COLOR_LL, COLOR_LR, FRACTION_X);
        return interpolateColor(INTERPOLATED_COLOR_X1, INTERPOLATED_COLOR_X2, FRACTION_Y);
    }

    /**
     * Returns the given COLOR with the given HUE
     * @param COLOR
     * @param HUE
     * @return Color with a given hue
     */
    public Color setHue(final Color COLOR, final float HUE) {
        final float HSB_VALUES[] = Color.RGBtoHSB(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), null);
        return Color.getHSBColor(HUE, HSB_VALUES[1], HSB_VALUES[2]);
    }

    /**
     * Returns the given COLOR with the given SATURATION which is really useful
     * if you would like to receive a red tone that has the same brightness and hue
     * as a given blue tone.
     * @param COLOR
     * @param SATURATION
     * @return Color with a given saturation
     */
    public Color setSaturation(final Color COLOR, final float SATURATION) {
        final float HSB_VALUES[] = Color.RGBtoHSB(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), null);
        return Color.getHSBColor(HSB_VALUES[0], SATURATION, HSB_VALUES[2]);
    }

    /**
     * Returns the given COLOR with the given BRIGHTNESS
     * @param COLOR
     * @param BRIGHTNESS
     * @return Color with the given brightness
     */
    public Color setBrightness(final Color COLOR, final float BRIGHTNESS) {
        final float HSB_VALUES[] = Color.RGBtoHSB(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), null);
        return Color.getHSBColor(HSB_VALUES[0], HSB_VALUES[1], BRIGHTNESS);
    }

    /**
     * Returns the given COLOR with the given HUE an SATURATION
     * @param COLOR
     * @param HUE
     * @param SATURATION
     * @return the given COLOR with the given HUE and SATURATION
     */
    public Color setHueSaturation(final Color COLOR, final float HUE, final float SATURATION) {
        final float HSB_VALUES[] = Color.RGBtoHSB(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), null);
        return Color.getHSBColor(HUE, SATURATION, HSB_VALUES[2]);
    }

    /**
     * Returns the given COLOR with the given SATURATION and BRIGHTNESS
     * @param COLOR
     * @param SATURATION
     * @param BRIGHTNESS
     * @return the given COLOR with the given SATURATION and BRIGHTNESS
     */
    public Color setSaturationBrightness(final Color COLOR, final float SATURATION, final float BRIGHTNESS) {
        final float HSB_VALUES[] = Color.RGBtoHSB(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), null);
        return Color.getHSBColor(HSB_VALUES[0], SATURATION, BRIGHTNESS);
    }

    /**
     * Returns a darker version of the given color
     * @param COLOR
     * @param FRACTION
     * @return a darker version of the given color
     */
    public Color darker(final Color COLOR, final double FRACTION) {
        int red = (int) Math.round(COLOR.getRed() * (1.0 - FRACTION));
        int green = (int) Math.round(COLOR.getGreen() * (1.0 - FRACTION));
        int blue = (int) Math.round(COLOR.getBlue() * (1.0 - FRACTION));

        red = red < 0 ? 0 : (red > 255 ? 255 : red);
        green = green < 0 ? 0 : (green > 255 ? 255 : green);
        blue = blue < 0 ? 0 : (blue > 255 ? 255 : blue);

        return new Color(red, green, blue, COLOR.getAlpha());
    }

    /**
     * Returns a brighter version of the given color
     * @param COLOR
     * @param FRACTION
     * @return a brighter version of the given color
     */
    public Color lighter(final Color COLOR, final double FRACTION) {
        int red = (int) Math.round(COLOR.getRed() * (1.0 + FRACTION));
        int green = (int) Math.round(COLOR.getGreen() * (1.0 + FRACTION));
        int blue = (int) Math.round(COLOR.getBlue() * (1.0 + FRACTION));

        red = red < 0 ? 0 : (red > 255 ? 255 : red);
        green = green < 0 ? 0 : (green > 255 ? 255 : green);
        blue = blue < 0 ? 0 : (blue > 255 ? 255 : blue);

        return new Color(red, green, blue, COLOR.getAlpha());
    }

    /**
     * Return the "distance" between two colors where the rgb values are
     * are taken to be coordinates in a 3D space [0.0-1.0].
     * @param   COLOR1
     * @param   COLOR2
     * @return  Distance bwetween colors.
     */
    public double colorDistance(final Color COLOR1, final Color COLOR2) {
        final double FACTOR = 1.0 / 255.0;
        final double DELTA_R = (COLOR2.getRed() - COLOR1.getRed()) * FACTOR;
        final double DELTA_G = (COLOR2.getGreen() - COLOR1.getGreen()) * FACTOR;
        final double DELTA_B = (COLOR2.getBlue() - COLOR1.getBlue()) * FACTOR;

        return Math.sqrt(DELTA_R * DELTA_R + DELTA_G * DELTA_G + DELTA_B * DELTA_B);
    }

    /**
     * Returns true if the given color is closer to black than to white.
     * To get the result we calculate the colorDistance from the given color
     * to black and compare it with the colorDistance from the given color to
     * white.
     * @param COLOR
     * @return true if the given color is closer to black than white
     */
    public boolean isDark(final Color COLOR) {
        final double DISTANCE_TO_WHITE = colorDistance(COLOR, Color.WHITE);
        final double DISTANCE_TO_BLACK = colorDistance(COLOR, Color.BLACK);

        return DISTANCE_TO_BLACK < DISTANCE_TO_WHITE;
    }

    /**
     * Returns true if the red, green and blue value of the given color are equal
     * @param COLOR
     * @return true if the red, green and blue value of the given color are equal
     */
    public boolean isMonochrome(final Color COLOR) {
        return ((COLOR.getRed() == COLOR.getGreen()) && (COLOR.getGreen() == COLOR.getBlue()));
    }

    /**
     * Returns the seven segment font "lcd.ttf" if it is available.
     * Usualy it should be no problem because it will be delivered in the package but
     * if there is a problem it will return the standard font which is verdana.
     * @return Font with fontface from lcd.ttf (if available)
     */
    public Font getDigitalFont() {
        if (digitalFont == null) {
            digitalFont = STANDARD_FONT;
        }
        return this.digitalFont.deriveFont(24).deriveFont(Font.PLAIN);
    }

    /**
     * Returns the standard font which is verdana.
     * @return Font that is defined as standard
     */
    public Font getStandardFont() {
        return this.STANDARD_FONT;
    }

    /**
     * Saves the given buffered image as png image
     * @param IMAGE
     * @param FILE_NAME
     */
    public void savePngImage(final BufferedImage IMAGE, final String FILE_NAME) {
        try {
            ImageIO.write(IMAGE, "png", new File(FILE_NAME));
        } catch (final java.io.IOException EXCEPTION) {
        }
    }

    /**
     * Returns the logarithm of a given number and base
     * @param base
     * @param num
     * @return the logarithm of a given number and base
     */
    public double logOfBase(double base, double num) {
        return Math.log(num) / Math.log(base);
    }

    /**
     * Returns a list of 9 shades of the given color which will be calculated by the given
     * intensity. The 5 element of the list is the same as the igven color which means you will
     * get 4 darker colors and 4 brighter colors of the given color.
     * @param INTENSITY
     * @param COLOR
     * @return a list of 9 shades of the given color, 4 darker, the original and 4 brighter colors
     */
    public LinkedList<Color> createShades(final float INTENSITY, final Color COLOR) {
        final float[] HSB = Color.RGBtoHSB(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), null);
        final float SATURATION_STEPSIZE = HSB[1] / INTENSITY;
        final float BRIGHTNESS_STEPSIZE = HSB[2] / INTENSITY;
        LinkedList<Color> colorShades = new LinkedList<Color>();
        for (int i = 4; i > 0; i--) {
            colorShades.add(new HsbColor.Builder(COLOR).saturation(HSB[1] - i * SATURATION_STEPSIZE).build().getColor());
        }
        colorShades.add(COLOR);
        for (int i = 4; i > 0; i--) {
            colorShades.add(new HsbColor.Builder(COLOR).brightness(HSB[2] - i * BRIGHTNESS_STEPSIZE).build().getColor());
        }
        return colorShades;
    }

    public String valueToScientificText(final String UNIT, final int PRECISION, final double VALUE) {
        int          precision = PRECISION < 0 ? 0 : PRECISION;
        String       format    = "%." + precision + "f";
        String       formatAlt = "%." + precision + "e";
        double       divisor[] = { 100, 10, 1000, 100, 10 }; // (-2 -- +2)
        final String PREFIX[]  = { " y", " y", " y", " z", " z", " z", " a",
                " a", " a", " t", "  t", " t", " f", " f", " f", " n", " n",
                " n", " µ", " µ", " µ", " m", " m", " m", " ", " ", " ", " k",
                " k", " k", " M", " M", " M", " G", " G", " G", " T", " T",
                " T", " P", " P", " P", " E", " E", " E", " Z", " Z", " Z",
                " Y", " Y", " Y" };
        int    deca;
        int    decade;
        double m;
        double y;
        double mantisse;
        String formatedResult;

        if (VALUE != 0) {
            if (VALUE < 0) { y = Math.log10(-VALUE); } else { y = Math.log10(VALUE); }
            decade = (int) Math.round(y);
            m = y - decade;
            mantisse = 1000 * Math.pow(10, m);
            deca = decade % 3 + 2;
            mantisse /= divisor[deca];
            if (decade >= -24 && decade <= 24) {
                formatedResult = String.format(format, mantisse) + PREFIX[decade + 24] + UNIT;
            } else {
                formatedResult = String.format(formatAlt, mantisse);
            }
        } else {
            formatedResult = "0" + UNIT;
        }
        if (VALUE < 0) { formatedResult = '-' + formatedResult; }

        return formatedResult;
    }
    // </editor-fold>
}