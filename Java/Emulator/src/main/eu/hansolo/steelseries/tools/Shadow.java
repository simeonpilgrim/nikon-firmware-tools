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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;


/**
 * Definition of methods to create inner shadows and
 * drop shadows for shapes. The syntax of the methods
 * uses the same parameters as Adobe Fireworks.
 * @author hansolo
 */
public enum Shadow {

    INSTANCE;
    private final Util UTIL = Util.INSTANCE;

    /**
     * <p>Return a new compatible image that contains the given shape
     * with the paint, color, stroke etc. that is specified.</p>
     * @param SHAPE the shape to create the image fromf
     * @param PAINT the paint of the shape
     * @param COLOR the color of the shape
     * @param FILLED indicator if the shape is filled or not
     * @param STROKE the stroke of the shape
     * @param STROKE_COLOR color of the stroke
     * @return a new compatible image that contains the given shape
     */
    public BufferedImage createImageFromShape(final Shape SHAPE, final Paint PAINT, final Color COLOR, final boolean FILLED, final Stroke STROKE, final Color STROKE_COLOR) {
        final BufferedImage IMAGE = UTIL.createImage(SHAPE.getBounds().width, SHAPE.getBounds().height, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        G2.translate(-SHAPE.getBounds2D().getX(), -SHAPE.getBounds2D().getY());

        if (PAINT != null) {
            G2.setPaint(PAINT);
            if (FILLED) {
                G2.fill(SHAPE);
            } else {
                G2.draw(SHAPE);
            }
        }

        if (COLOR != null) {
            G2.setColor(COLOR);
            if (FILLED) {
                G2.fill(SHAPE);
            } else {
                G2.draw(SHAPE);
            }
        }

        if (STROKE != null) {
            if (STROKE_COLOR != null) {
                G2.setColor(STROKE_COLOR);
            }

            G2.setStroke(STROKE);

            if (!FILLED) {
                G2.draw(SHAPE);
            }
        }

        G2.dispose();

        return IMAGE;

    }

    public BufferedImage createDropShadow(final BufferedImage SRC_IMAGE, final int DISTANCE, final float ALPHA, final int SOFTNESS, final int ANGLE, final Color SHADOW_COLOR) {
        final float TRANSLATE_X = (float) (DISTANCE * Math.cos(Math.toRadians(360 - ANGLE)));
        final float TRANSLATE_Y = (float) (DISTANCE * Math.sin(Math.toRadians(360 - ANGLE)));

        final BufferedImage SHADOW_IMAGE = renderDropShadow(SRC_IMAGE, SOFTNESS, ALPHA, SHADOW_COLOR);
        final BufferedImage RESULT = new BufferedImage(SHADOW_IMAGE.getWidth(), SHADOW_IMAGE.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D G2 = RESULT.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.translate(TRANSLATE_X, TRANSLATE_Y);
        G2.drawImage(SHADOW_IMAGE, 0, 0, null);
        G2.translate(-TRANSLATE_X, -TRANSLATE_Y);
        G2.translate(SOFTNESS, SOFTNESS);
        G2.drawImage(SRC_IMAGE, 0, 0, null);

        G2.dispose();

        return RESULT;
    }

    public BufferedImage createDropShadow(final Shape SHAPE, final Paint PAINT, final Color COLOR, final boolean FILLED, final Stroke STROKE, final Color STROKE_COLOR, final int DISTANCE, final float ALPHA, final int SOFTNESS, final int ANGLE, final Color SHADOW_COLOR) {
        final float TRANSLATE_X = (float) (DISTANCE * Math.cos(Math.toRadians(360 - ANGLE)));
        final float TRANSLATE_Y = (float) (DISTANCE * Math.sin(Math.toRadians(360 - ANGLE)));

        final BufferedImage SHAPE_IMAGE = createImageFromShape(SHAPE, PAINT, COLOR, FILLED, STROKE, STROKE_COLOR);
        final BufferedImage SHADOW_IMAGE = renderDropShadow(SHAPE_IMAGE, SOFTNESS, ALPHA, SHADOW_COLOR);

        final BufferedImage RESULT = new BufferedImage(SHADOW_IMAGE.getWidth(), SHADOW_IMAGE.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D G2 = RESULT.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.translate(TRANSLATE_X, TRANSLATE_Y);
        G2.drawImage(SHADOW_IMAGE, 0, 0, null);
        G2.translate(-TRANSLATE_X, -TRANSLATE_Y);
        G2.translate(SOFTNESS, SOFTNESS);
        G2.drawImage(SHAPE_IMAGE, 0, 0, null);

        G2.dispose();

        return RESULT;
    }

    /**
     * <p>Method to create a inner shadow on a given shape</p>
     * @param SOFT_CLIP_IMAGE softclipimage create by method createSoftClipImage()
     * @param SHAPE shape that should get the inner shadow
     * @param DISTANCE distance of the shadow
     * @param ALPHA alpha value of the shadow
     * @param SHADOW_COLOR color of the shadow
     * @param SOFTNESS softness/fuzzyness of the shadow
     * @param ANGLE angle under which the shadow should appear
     * @return IMAGE buffered image that contains the shape including the inner shadow
     */
    public BufferedImage createInnerShadow(final BufferedImage SOFT_CLIP_IMAGE, final Shape SHAPE, final int DISTANCE, final float ALPHA, final Color SHADOW_COLOR, final int SOFTNESS, final int ANGLE) {
        final float COLOR_CONSTANT = 1f / 255f;
        final float RED = COLOR_CONSTANT * SHADOW_COLOR.getRed();
        final float GREEN = COLOR_CONSTANT * SHADOW_COLOR.getGreen();
        final float BLUE = COLOR_CONSTANT * SHADOW_COLOR.getBlue();
        final float MAX_STROKE_WIDTH = SOFTNESS * 2;
        final float ALPHA_STEP = 1f / (2 * SOFTNESS + 2) * ALPHA;
        final float TRANSLATE_X = (float) (DISTANCE * Math.cos(Math.toRadians(ANGLE)));
        final float TRANSLATE_Y = (float) (DISTANCE * Math.sin(Math.toRadians(ANGLE)));

        final Graphics2D G2 = SOFT_CLIP_IMAGE.createGraphics();

        // Enable Antialiasing
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Translate the coordinate system to 0,0
        G2.translate(-SHAPE.getBounds2D().getX(), -SHAPE.getBounds2D().getY());

        // Set the color
        G2.setColor(new Color(RED, GREEN, BLUE, ALPHA_STEP));

        // Set the alpha transparency of the whole image
        G2.setComposite(AlphaComposite.SrcAtop);

        // Translate the coordinate system related to the given distance and angle
        G2.translate(TRANSLATE_X, -TRANSLATE_Y);

        // Draw the inner shadow
        for (float strokeWidth = SOFTNESS; strokeWidth >= 1; strokeWidth -= 1) {
            G2.setStroke(new BasicStroke((float) (MAX_STROKE_WIDTH * Math.pow(0.85, strokeWidth))));
            G2.draw(SHAPE);
        }

        G2.dispose();

        return SOFT_CLIP_IMAGE;
    }

    /**
     * <p>Method to create a inner shadow on a given shape</p>
     * @param SHAPE shape that should get the inner shadow
     * @param SHAPE_PAINT paint of the given shape
     * @param DISTANCE distance of the shadow
     * @param ALPHA alpha value of the shadow
     * @param SHADOW_COLOR color of the shadow
     * @param SOFTNESS softness/fuzzyness of the shadow
     * @param ANGLE angle under which the shadow should appear
     * @return IMAGE buffered image that contains the shape including the inner shadow
     */
    public BufferedImage createInnerShadow(final Shape SHAPE, final Paint SHAPE_PAINT, final int DISTANCE, final float ALPHA, final Color SHADOW_COLOR, final int SOFTNESS, final int ANGLE) {
        final float COLOR_CONSTANT = 1f / 255f;
        final float RED = COLOR_CONSTANT * SHADOW_COLOR.getRed();
        final float GREEN = COLOR_CONSTANT * SHADOW_COLOR.getGreen();
        final float BLUE = COLOR_CONSTANT * SHADOW_COLOR.getBlue();
        final float MAX_STROKE_WIDTH = SOFTNESS * 2;
        final float ALPHA_STEP = 1f / (2 * SOFTNESS + 2) * ALPHA;
        final float TRANSLATE_X = (float) (DISTANCE * Math.cos(Math.toRadians(ANGLE)));
        final float TRANSLATE_Y = (float) (DISTANCE * Math.sin(Math.toRadians(ANGLE)));

        final BufferedImage IMAGE = createSoftClipImage(SHAPE, SHAPE_PAINT, 0, 0, 0);

        final Graphics2D G2 = IMAGE.createGraphics();

        // Enable Antialiasing
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Translate the coordinate system to 0,0
        G2.translate(-SHAPE.getBounds2D().getX(), -SHAPE.getBounds2D().getY());

        // Set the color
        G2.setColor(new Color(RED, GREEN, BLUE, ALPHA_STEP));

        // Set the alpha transparency of the whole image
        G2.setComposite(AlphaComposite.SrcAtop);

        // Translate the coordinate system related to the given distance and angle
        G2.translate(TRANSLATE_X, -TRANSLATE_Y);

        // Draw the inner shadow
        for (float strokeWidth = SOFTNESS; strokeWidth >= 1; strokeWidth -= 1) {
            G2.setStroke(new BasicStroke((float) (MAX_STROKE_WIDTH * Math.pow(0.85, strokeWidth))));
            G2.draw(SHAPE);
        }

        G2.dispose();

        return IMAGE;
    }

    /**
     * Method that creates a intermediate image to enable soft clipping functionality
     * This code was taken from Chris Campbells blog http://weblogs.java.net/blog/campbell/archive/2006/07/java_2d_tricker.html
     * @param SHAPE
     * @param SHAPE_PAINT
     * @return IMAGE buffered image that will be used for soft clipping
     */
    public BufferedImage createSoftClipImage(final Shape SHAPE, final Paint SHAPE_PAINT) {
        final BufferedImage IMAGE = UTIL.createImage(SHAPE.getBounds().width, SHAPE.getBounds().height, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();

        // Clear the image so all pixels have zero alpha
        G2.setComposite(AlphaComposite.Clear);
        G2.fillRect(0, 0, IMAGE.getWidth(), IMAGE.getHeight());

        // Render our clip shape into the image.  Note that we enable
        // antialiasing to achieve the soft clipping effect.  Try
        // commenting out the line that enables antialiasing, and
        // you will see that you end up with the usual hard clipping.
        G2.setComposite(AlphaComposite.Src);
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set Color or Gradient here
        if (SHAPE_PAINT != null) {
            G2.setPaint(SHAPE_PAINT);
        }

        // Translate the coordinate system to 0,0
        G2.translate(-SHAPE.getBounds2D().getX(), -SHAPE.getBounds2D().getY());
        G2.fill(SHAPE);

        return IMAGE;
    }

    /**
     * <p>Method that creates a intermediate image to enable soft clipping functionality
     * This code was taken from Chris Campbells blog http://weblogs.java.net/blog/campbell/archive/2006/07/java_2d_tricker.html</p>
     * @param SHAPE
     * @param SHAPE_PAINT
     * @param SOFTNESS
     * @param TRANSLATE_X
     * @param TRANSLATE_Y
     * @return IMAGE buffered image that will be used for soft clipping
     */
    public BufferedImage createSoftClipImage(final Shape SHAPE, final Paint SHAPE_PAINT, final int SOFTNESS, final int TRANSLATE_X, final int TRANSLATE_Y) {
        final BufferedImage IMAGE = UTIL.createImage(SHAPE.getBounds().width + 2 * SOFTNESS + TRANSLATE_X, SHAPE.getBounds().height + 2 * SOFTNESS + TRANSLATE_Y, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();

        G2.translate(SOFTNESS / 2.0, SOFTNESS / 2.0);

        // Clear the image so all pixels have zero alpha
        G2.setComposite(AlphaComposite.Clear);
        G2.fillRect(0, 0, IMAGE.getWidth(), IMAGE.getHeight());

        // Render our clip shape into the image.  Note that we enable
        // antialiasing to achieve the soft clipping effect.  Try
        // commenting out the line that enables antialiasing, and
        // you will see that you end up with the usual hard clipping.
        G2.setComposite(AlphaComposite.Src);
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set Color or Gradient here
        if (SHAPE_PAINT != null) {
            G2.setPaint(SHAPE_PAINT);
        }

        // Translate the coordinate system to 0,0
        G2.translate(-SHAPE.getBounds2D().getX(), -SHAPE.getBounds2D().getY());
        G2.fill(SHAPE);

        return IMAGE;
    }

    /**
     * <p>Generates the shadow for a given picture and the current properties
     * of the renderer.</p>
     * <p>The generated image dimensions are computed as following</p>
     * <pre>
     * width  = imageWidth  + 2 * shadowSize
     * height = imageHeight + 2 * shadowSize
     * </pre>
     * @param IMAGE image the picture from which the shadow must be cast
     * @param SOFTNESS
     * @param ALPHA
     * @param SHADOW_COLOR
     * @return the picture containing the shadow of <code>image</code>
     */
    public BufferedImage renderDropShadow(final BufferedImage IMAGE, final int SOFTNESS, final float ALPHA, final Color SHADOW_COLOR) {
        // Written by Sesbastien Petrucci
        final int SHADOW_SIZE = SOFTNESS * 2;

        final int SRC_WIDTH = IMAGE.getWidth();
        final int SRC_HEIGHT = IMAGE.getHeight();

        final int DST_WIDTH = SRC_WIDTH + SHADOW_SIZE;
        final int DST_HEIGHT = SRC_HEIGHT + SHADOW_SIZE;

        final int LEFT = SOFTNESS;
        final int RIGHT = SHADOW_SIZE - LEFT;

        final int Y_STOP = DST_HEIGHT - RIGHT;

        final int SHADOW_RGB = SHADOW_COLOR.getRGB() & 0x00FFFFFF;
        int[] aHistory = new int[SHADOW_SIZE];
        int historyIdx;

        int aSum;

        final BufferedImage DST = new BufferedImage(DST_WIDTH, DST_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        int[] dstBuffer = new int[DST_WIDTH * DST_HEIGHT];
        int[] srcBuffer = new int[SRC_WIDTH * SRC_HEIGHT];

        getPixels(IMAGE, 0, 0, SRC_WIDTH, SRC_HEIGHT, srcBuffer);

        final int LAST_PIXEL_OFFSET = RIGHT * DST_WIDTH;
        final float H_SUM_DIVIDER = 1.0f / SHADOW_SIZE;
        final float V_SUM_DIVIDER = ALPHA / SHADOW_SIZE;

        int max;

        int[] hSumLookup = new int[256 * SHADOW_SIZE];
        max = hSumLookup.length;

        for (int i = 0; i < max; i++) {
            hSumLookup[i] = (int) (i * H_SUM_DIVIDER);
        }

        int[] vSumLookup = new int[256 * SHADOW_SIZE];
        max = vSumLookup.length;
        for (int i = 0; i < max; i++) {
            vSumLookup[i] = (int) (i * V_SUM_DIVIDER);
        }

        int srcOffset;

        // horizontal pass  extract the alpha mask from the source picture and
        // blur it into the destination picture
        for (int srcY = 0, dstOffset = LEFT * DST_WIDTH; srcY < SRC_HEIGHT; srcY++) {
            // first pixels are empty
            for (historyIdx = 0; historyIdx < SHADOW_SIZE;) {
                aHistory[historyIdx++] = 0;
            }

            aSum = 0;
            historyIdx = 0;
            srcOffset = srcY * SRC_WIDTH;

            // compute the blur average with pixels from the source image
            for (int srcX = 0; srcX < SRC_WIDTH; srcX++) {
                int a = hSumLookup[aSum];
                dstBuffer[dstOffset++] = a << 24; // store the alpha value only
                // the shadow color will be added in the next pass

                aSum -= aHistory[historyIdx]; // substract the oldest pixel from the sum

                // extract the new pixel ...
                a = srcBuffer[srcOffset + srcX] >>> 24;
                aHistory[historyIdx] = a; // ... and store its value into history
                aSum += a; // ... and add its value to the sum

                if (++historyIdx >= SHADOW_SIZE) {
                    historyIdx -= SHADOW_SIZE;
                }
            }

            // blur the end of the row - no new pixels to grab
            for (int i = 0; i < SHADOW_SIZE; i++) {
                final int A = hSumLookup[aSum];
                dstBuffer[dstOffset++] = A << 24;

                // substract the oldest pixel from the sum ... and nothing new to add !
                aSum -= aHistory[historyIdx];

                if (++historyIdx >= SHADOW_SIZE) {
                    historyIdx -= SHADOW_SIZE;
                }
            }
        }

        // vertical pass
        for (int x = 0, bufferOffset = 0; x < DST_WIDTH; x++, bufferOffset = x) {
            aSum = 0;

            // first pixels are empty
            for (historyIdx = 0; historyIdx < LEFT;) {
                aHistory[historyIdx++] = 0;
            }

            // and then they come from the dstBuffer
            for (int y = 0; y < RIGHT; y++, bufferOffset += DST_WIDTH) {
                final int A = dstBuffer[bufferOffset] >>> 24; // extract alpha
                aHistory[historyIdx++] = A; // store into history
                aSum += A; // and add to sum
            }

            bufferOffset = x;
            historyIdx = 0;

            // compute the blur avera`ge with pixels from the previous pass
            for (int y = 0; y < Y_STOP; y++, bufferOffset += DST_WIDTH) {

                int a = vSumLookup[aSum];
                dstBuffer[bufferOffset] = a << 24 | SHADOW_RGB; // store alpha value + shadow color

                aSum -= aHistory[historyIdx]; // substract the oldest pixel from the sum

                a = dstBuffer[bufferOffset + LAST_PIXEL_OFFSET] >>> 24; // extract the new pixel ...
                aHistory[historyIdx] = a; // ... and store its value into history
                aSum += a; // ... and add its value to the sum

                if (++historyIdx >= SHADOW_SIZE) {
                    historyIdx -= SHADOW_SIZE;
                }
            }

            // blur the end of the column - no pixels to grab anymore
            for (int y = Y_STOP; y < DST_HEIGHT; y++, bufferOffset += DST_WIDTH) {

                final int A = vSumLookup[aSum];
                dstBuffer[bufferOffset] = A << 24 | SHADOW_RGB;

                aSum -= aHistory[historyIdx]; // substract the oldest pixel from the sum

                if (++historyIdx >= SHADOW_SIZE) {
                    historyIdx -= SHADOW_SIZE;
                }
            }
        }

        setPixels(DST, 0, 0, DST_WIDTH, DST_HEIGHT, dstBuffer);

        return DST;
    }

    /**
     * <p>Returns an array of pixels, stored as integers, from a
     * <code>BufferedImage</code>. The pixels are grabbed from a rectangular
     * area defined by a location and two dimensions. Calling this method on
     * an image of type different from <code>BufferedImage.TYPE_INT_ARGB</code>
     * and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.</p>
     *
     * @param IMAGE the source image
     * @param X the x location at which to start grabbing pixels
     * @param Y the y location at which to start grabbing pixels
     * @param W the width of the rectangle of pixels to grab
     * @param H the height of the rectangle of pixels to grab
     * @param pixels a pre-allocated array of pixels of size w*h; can be null
     * @return <code>pixels</code> if non-null, a new array of integers
     *   otherwise
     * @throws IllegalArgumentException is <code>pixels</code> is non-null and
     *   of length &lt; w*h
     */
    public int[] getPixels(final BufferedImage IMAGE, final int X, final int Y, final int W, final int H, int[] pixels) {
        if (W == 0 || H == 0) {
            return new int[0];
        }

        if (pixels == null) {
            pixels = new int[W * H];
        } else if (pixels.length < W * H) {
            throw new IllegalArgumentException("pixels array must have a length " + " >= w*h");
        }

        int imageType = IMAGE.getType();
        if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB) {
            Raster raster = IMAGE.getRaster();
            return (int[]) raster.getDataElements(X, Y, W, H, pixels);
        }

        // Unmanages the image
        return IMAGE.getRGB(X, Y, W, H, pixels, 0, W);
    }

    /**
     * <p>Writes a rectangular area of pixels in the destination
     * <code>BufferedImage</code>. Calling this method on
     * an image of type different from <code>BufferedImage.TYPE_INT_ARGB</code>
     * and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.</p>
     *
     * @param IMAGE the destination image
     * @param X the x location at which to start storing pixels
     * @param Y the y location at which to start storing pixels
     * @param W the width of the rectangle of pixels to store
     * @param H the height of the rectangle of pixels to store
     * @param pixels an array of pixels, stored as integers
     * @throws IllegalArgumentException is <code>pixels</code> is non-null and
     *   of length &lt; w*h
     */
    public void setPixels(final BufferedImage IMAGE, final int X, final int Y, final int W, final int H, int[] pixels) {
        if (pixels == null || W == 0 || H == 0) {
            return;
        } else if (pixels.length < W * H) {
            throw new IllegalArgumentException("pixels array must have a length" + " >= w*h");
        }

        int imageType = IMAGE.getType();
        if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB) {
            WritableRaster raster = IMAGE.getRaster();
            raster.setDataElements(X, Y, W, H, pixels);
        } else {
            // Unmanages the image
            IMAGE.setRGB(X, Y, W, H, pixels, 0, W);
        }
    }

    /**
     * <p>Method to create a inner shadow on a given shape</p>
     * @param G2 graphics2d object that contains the shape which will get the inner shadow
     * @param SHAPE shape that should get the inner shadow
     * @param DISTANCE distance of the shadow
     * @param ALPHA alpha value of the shadow
     * @param SHADOW_COLOR color of the shadow
     * @param SOFTNESS softness/fuzzyness of the shadow
     * @param ANGLE angle under which the shadow should appear
     */
    public void addInnerShadow(final Graphics2D G2, final Shape SHAPE, final Color SHADOW_COLOR, final int DISTANCE, final float ALPHA, final int SOFTNESS, final int ANGLE) {
        final float COLOR_CONSTANT = 1f / 255f;
        final float RED = COLOR_CONSTANT * SHADOW_COLOR.getRed();
        final float GREEN = COLOR_CONSTANT * SHADOW_COLOR.getGreen();
        final float BLUE = COLOR_CONSTANT * SHADOW_COLOR.getBlue();
        final float MAX_STROKE_WIDTH = SOFTNESS * 2;
        final float ALPHA_STEP = 1f / (2 * SOFTNESS + 2) * ALPHA;
        final float TRANSLATE_X = (float) (DISTANCE * Math.cos(Math.toRadians(ANGLE)));
        final float TRANSLATE_Y = (float) (DISTANCE * Math.sin(Math.toRadians(ANGLE)));

        // Enable Antialiasing
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Store existing parameters
        final Shape OLD_CLIP = G2.getClip();
        final AffineTransform OLD_TRANSFORM = G2.getTransform();
        final Stroke OLD_STROKE = G2.getStroke();
        final Paint OLD_PAINT = G2.getPaint();

        // Set the color
        G2.setColor(new Color(RED, GREEN, BLUE, ALPHA_STEP));

        // Set the alpha transparency of the whole image
        G2.setComposite(AlphaComposite.SrcAtop);

        // Translate the coordinate system related to the given distance and angle
        G2.translate(TRANSLATE_X, -TRANSLATE_Y);

        G2.setClip(SHAPE);

        // Draw the inner shadow
        for (float strokeWidth = SOFTNESS; strokeWidth >= 1; strokeWidth -= 1) {
            G2.setStroke(new BasicStroke((float) (MAX_STROKE_WIDTH * Math.pow(0.85, strokeWidth))));
            G2.draw(SHAPE);
        }

        // Restore old parameters
        G2.setTransform(OLD_TRANSFORM);
        G2.setClip(OLD_CLIP);
        G2.setStroke(OLD_STROKE);
        G2.setPaint(OLD_PAINT);
    }

    /**
     * Adds a simple glow around the given shape
     * @param G2
     * @param CANVAS
     * @param SHAPE
     * @param SHAPE_PAINT
     * @param GLOW_COLOR
     * @param ALPHA
     * @param SOFTNESS
     */
    public void addGlow(final Graphics2D G2, final Rectangle CANVAS, final Shape SHAPE, final Paint SHAPE_PAINT, final Color GLOW_COLOR, final float ALPHA, final int SOFTNESS) {
        final float COLOR_CONSTANT = 1f / 255f;
        final float RED = COLOR_CONSTANT * GLOW_COLOR.getRed();
        final float GREEN = COLOR_CONSTANT * GLOW_COLOR.getGreen();
        final float BLUE = COLOR_CONSTANT * GLOW_COLOR.getBlue();
        final float MAX_STROKE_WIDTH = SOFTNESS * 2;
        final float ALPHA_STEP = 1f / (2 * SOFTNESS + 2) * ALPHA;

        // Enable Antialiasing
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Store existing parameters
        final Shape OLD_CLIP = G2.getClip();
        final AffineTransform OLD_TRANSFORM = G2.getTransform();
        final Stroke OLD_STROKE = G2.getStroke();
        final Paint OLD_PAINT = G2.getPaint();

        // Set the color
        G2.setColor(new Color(RED, GREEN, BLUE, ALPHA_STEP));

        // Draw the inner shadow
        for (float strokeWidth = SOFTNESS; strokeWidth >= 1; strokeWidth -= 1) {
            G2.setStroke(new BasicStroke((float) (MAX_STROKE_WIDTH * Math.pow(0.85, strokeWidth))));
            G2.draw(SHAPE);
        }

        // Restore old parameters
        G2.setTransform(OLD_TRANSFORM);
        G2.setClip(OLD_CLIP);
        G2.setStroke(OLD_STROKE);

        // Draw the original shape on top
        G2.setPaint(SHAPE_PAINT);
        G2.fill(SHAPE);

        G2.setPaint(OLD_PAINT);
    }
}