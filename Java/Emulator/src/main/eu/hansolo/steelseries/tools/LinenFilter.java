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

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

/**
 * Based on the BrushedMetalFilter from Huxtable.com
 * @author hansolo
 */
public class LinenFilter implements BufferedImageOp {

    private int radius = 65;
    private float amount = 0.6f;
    private float shine = 0.3f;
    private Orientation orientation = Orientation.HORIZONTAL;
    private int color = 0xff686868;
    private java.util.Random randomNumbers;

    public LinenFilter() {
    }

    @Override
    public BufferedImage filter(final BufferedImage SOURCE, BufferedImage destination) {
        final int WIDTH = SOURCE.getWidth();
        final int HEIGHT = SOURCE.getHeight();

        if (destination == null) {
            destination = createCompatibleDestImage(SOURCE, null);
        }

        final int[] IN_PIXELS_HOR = new int[WIDTH];
        final int[] OUT_PIXELS_HOR = new int[WIDTH];

        randomNumbers = new java.util.Random(0);
        final int ALPHA = color & 0xFF000000;
        final int RED = (color >> 16) & 0xff;
        final int GREEN = (color >> 8) & 0xff;
        final int BLUE = color & 0xff;
        switch (orientation) {
            case VERTICAL:
                final int[] IN_PIXELS_VER = new int[HEIGHT];
                final int[] OUT_PIXELS_VER = new int[HEIGHT];
                randomNumbers = new java.util.Random(0);
                for (int x = 0; x < WIDTH; x++) {
                    for (int y = 0; y < HEIGHT; y++) {
                        int tr = RED;
                        int tg = GREEN;
                        int tb = BLUE;
                        if (shine != 0) {
                            int f = (int) (255 * shine * Math.sin((double) x / WIDTH * Math.PI));
                            tr += f;
                            tg += f;
                            tb += f;
                        }
                        int n = (int) (255 * (2 * randomNumbers.nextFloat() - 1) * amount);
                        IN_PIXELS_VER[y] = ALPHA | (clamp(tr + n) << 16) | (clamp(tg + n) << 8) | clamp(tb + n);
                    }

                    if (radius != 0) {
                        blurVertical(IN_PIXELS_VER, OUT_PIXELS_VER, HEIGHT, radius);
                        setRGB(destination, x, 0, 1, HEIGHT, OUT_PIXELS_VER);
                    } else {
                        setRGB(destination, x, 0, 1, HEIGHT, IN_PIXELS_VER);
                    }
                }
                break;

            case HORIZONTAL:

            default:
                for (int y = 0; y < HEIGHT; y++) {
                    for (int x = 0; x < WIDTH; x++) {
                        int tr = RED;
                        int tg = GREEN;
                        int tb = BLUE;
                        if (shine != 0) {
                            int f = (int) (255 * shine * Math.sin((double) x / WIDTH * Math.PI));
                            tr += f;
                            tg += f;
                            tb += f;
                        }
                        int n = (int) (255 * (2 * randomNumbers.nextFloat() - 1) * amount);
                        IN_PIXELS_HOR[x] = ALPHA | (clamp(tr + n) << 16) | (clamp(tg + n) << 8) | clamp(tb + n);
                    }

                    if (radius != 0) {
                        blurHorizontal(IN_PIXELS_HOR, OUT_PIXELS_HOR, WIDTH, radius);
                        setRGB(destination, 0, y, WIDTH, 1, OUT_PIXELS_HOR);
                    } else {
                        setRGB(destination, 0, y, WIDTH, 1, IN_PIXELS_HOR);
                    }
                }
                break;

        }

        return destination;
    }

    private int random(int x) {
        x += (int) (255 * (2 * randomNumbers.nextFloat() - 1) * amount);
        if (x < 0) {
            x = 0;
        } else if (x > 0xff) {
            x = 0xff;
        }
        return x;
    }

    private static int clamp(final int C) {

        int ret = C < 0 ? 0 : (C > 255 ? 255 : C);
        return ret;
        /*
        int ret = C;
        if (C < 0) {
            ret = 0;
        }
        if (C > 255) {
            ret = 255;
        }
        return ret;
        */
    }

    /**
     * Return a mod b. This differs from the % operator with respect to negative numbers.
     * @param a the dividend
     * @param B the divisor
     * @return a mod b
     */
    private static int mod(int a, final int B) {
        final int N = a / B;

        a -= N * B;
        if (a < 0) {
            return a + B;
        }
        return a;
    }

    public void blurHorizontal(final int[] IN, final int[] OUT, final int WIDTH, final int RADIUS) {
        final int WIDTH_MINUS_1 = WIDTH - 1;
        final int R2 = 2 * RADIUS + 1;
        int tr = 0, tg = 0, tb = 0;

        for (int i = -RADIUS; i <= RADIUS; i++) {
            int rgb = IN[mod(i, WIDTH)];
            tr += (rgb >> 16) & 0xff;
            tg += (rgb >> 8) & 0xff;
            tb += rgb & 0xff;
        }

        for (int x = 0; x < WIDTH; x++) {
            OUT[x] = 0xff000000 | ((tr / R2) << 16) | ((tg / R2) << 8) | (tb / R2);

            int i1 = x + RADIUS + 1;
            if (i1 > WIDTH_MINUS_1) {
                i1 = mod(i1, WIDTH);
            }
            int i2 = x - RADIUS;
            if (i2 < 0) {
                i2 = mod(i2, WIDTH);
            }
            int rgb1 = IN[i1];
            int rgb2 = IN[i2];

            tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
            tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
            tb += (rgb1 & 0xff) - (rgb2 & 0xff);
        }
    }

    public void blurVertical(final int[] IN, final int[] OUT, final int HEIGHT, final int RADIUS) {
        final int HEIGHT_MINUS_1 = HEIGHT - 1;
        final int R2 = 2 * RADIUS + 1;
        int tr = 0, tg = 0, tb = 0;

        for (int i = -RADIUS; i <= RADIUS; i++) {
            int rgb = IN[mod(i, HEIGHT)];
            tr += (rgb >> 16) & 0xff;
            tg += (rgb >> 8) & 0xff;
            tb += rgb & 0xff;
        }

        for (int y = 0; y < HEIGHT; y++) {
            OUT[y] = 0xff000000 | ((tr / R2) << 16) | ((tg / R2) << 8) | (tb / R2);

            int i1 = y + RADIUS + 1;
            if (i1 > HEIGHT_MINUS_1) {
                i1 = mod(i1, HEIGHT);
            }
            int i2 = y - RADIUS;
            if (i2 < 0) {
                i2 = mod(i2, HEIGHT);
            }
            int rgb1 = IN[i1];
            int rgb2 = IN[i2];

            tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
            tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
            tb += (rgb1 & 0xff) - (rgb2 & 0xff);
        }
    }

    public void setRadius(final int RADIUS) {
        this.radius = RADIUS;
    }

    public int getRadius() {
        return radius;
    }

    public void setAmount(final float AMOUNT) {
        this.amount = AMOUNT;
    }

    public float getAmount() {
        return amount;
    }

    public void setColor(final int COLOR) {
        this.color = COLOR;
    }

    public int getColor() {
        return color;
    }

    public void setShine(final float SHINE) {
        this.shine = SHINE;
    }

    public float getShine() {
        return shine;
    }

    public void setOrientation(final Orientation ORIENTATION) {
        orientation = ORIENTATION;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public BufferedImage createCompatibleDestImage(final BufferedImage SOURCE, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = SOURCE.getColorModel();
        }
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(SOURCE.getWidth(), SOURCE.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

    @Override
    public Rectangle2D getBounds2D(final BufferedImage SOURCE) {
        return new java.awt.Rectangle(0, 0, SOURCE.getWidth(), SOURCE.getHeight());
    }

    @Override
    public Point2D getPoint2D(final Point2D SOURCE_POINT, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Double();
        }
        dstPt.setLocation(SOURCE_POINT.getX(), SOURCE_POINT.getY());
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }

    /**
     * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
     * penalty of BufferedImage.setRGB unmanaging the image.
     * @param IMAGE
     * @param X
     * @param Y
     * @param WIDTH
     * @param HEIGHT
     * @param PIXELS
     */
    public void setRGB(final BufferedImage IMAGE, final int X, final int Y, final int WIDTH, final int HEIGHT, final int[] PIXELS) {
        int type = IMAGE.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
            IMAGE.getRaster().setDataElements(X, Y, WIDTH, HEIGHT, PIXELS);
        } else {
            IMAGE.setRGB(X, Y, WIDTH, HEIGHT, PIXELS, 0, WIDTH);
        }
    }

    @Override
    public String toString() {
        return "Texture/Brushed Metal...";
    }
}