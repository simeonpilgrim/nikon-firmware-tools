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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.List;


/**
 * A paint class that creates conical gradients around a given center point
 * It could be used in the same way as LinearGradientPaint and RadialGradientPaint
 * and follows the same syntax.
 * You could use floats from 0.0 to 1.0 for the fractions which is standard but it's
 * also possible to use angles from 0.0 to 360 degrees which is most of the times
 * much easier to handle.
 * Gradients always start at the top with a clockwise direction and you could
 * rotate the gradient around the center by given offset.
 * The offset could also be defined from -0.5 to +0.5 or -180 to +180 degrees.
 * If you would like to use degrees instead of values from 0 to 1 you have to use
 * the full constructor and set the USE_DEGREES variable to true
 * @version 1.0
 * @author hansolo
 */
public final class ConicalGradientPaint implements Paint {

    private final Point2D CENTER;
    private final float[] FRACTION_ANGLES;
    private final float[] RED_STEP_LOOKUP;
    private final float[] GREEN_STEP_LOOKUP;
    private final float[] BLUE_STEP_LOOKUP;
    private final float[] ALPHA_STEP_LOOKUP;
    private final Color[] COLORS;
    private static final float INT_TO_FLOAT_CONST = 1f / 255f;

    /**
     * Standard constructor which takes the FRACTIONS in values from 0.0f to 1.0f
     * @param CENTER
     * @param GIVEN_FRACTIONS
     * @param GIVEN_COLORS
     * @throws IllegalArgumentException
     */
    public ConicalGradientPaint(final Point2D CENTER, final float[] GIVEN_FRACTIONS, final Color[] GIVEN_COLORS) throws IllegalArgumentException {
        this(false, CENTER, 0.0f, GIVEN_FRACTIONS, GIVEN_COLORS);
    }

    /**
     * Enhanced constructor which takes the FRACTIONS in degress from 0.0f to 360.0f and
     * also an GIVEN_OFFSET in degrees around the rotation CENTER
     * @param USE_DEGREES
     * @param CENTER
     * @param GIVEN_OFFSET
     * @param GIVEN_FRACTIONS
     * @param GIVEN_COLORS
     * @throws IllegalArgumentException
     */
    public ConicalGradientPaint(final boolean USE_DEGREES, final Point2D CENTER, final float GIVEN_OFFSET, final float[] GIVEN_FRACTIONS, final Color[] GIVEN_COLORS) throws IllegalArgumentException {
        // Check that fractions and colors are of the same size
        if (GIVEN_FRACTIONS.length != GIVEN_COLORS.length) {
            throw new IllegalArgumentException("Fractions and colors must be equal in size");
        }

        final java.util.List<Float> fractionList = new java.util.ArrayList<Float>(GIVEN_FRACTIONS.length);
        final float OFFSET;
        if (USE_DEGREES) {
            final float DEG_FRACTION = 1f / 360f;
            if (Float.compare((GIVEN_OFFSET * DEG_FRACTION), -0.5f) == 0) {
                OFFSET = -0.5f;
            } else if (Float.compare((GIVEN_OFFSET * DEG_FRACTION), 0.5f) == 0) {
                OFFSET = 0.5f;
            } else {
                OFFSET = (GIVEN_OFFSET * DEG_FRACTION);
            }
            for (final float fraction : GIVEN_FRACTIONS) {
                fractionList.add((fraction * DEG_FRACTION));
            }
        } else {
            // Now it seems to work with rotation of 0.5f, below is the old code to correct the problem
//            if (GIVEN_OFFSET == -0.5)
//            {
//                // This is needed because of problems in the creation of the Raster
//                // with a angle offset of exactly -0.5
//                OFFSET = -0.49999f;
//            }
//            else if (GIVEN_OFFSET == 0.5)
//            {
//                // This is needed because of problems in the creation of the Raster
//                // with a angle offset of exactly +0.5
//                OFFSET = 0.499999f;
//            }
//            else
            {
                OFFSET = GIVEN_OFFSET;
            }
            for (final float fraction : GIVEN_FRACTIONS) {
                fractionList.add(fraction);
            }
        }

        // Check for valid offset
        if (OFFSET > 0.5f || OFFSET < -0.5f) {
            throw new IllegalArgumentException("Offset has to be in the range of -0.5 to 0.5");
        }

        // Adjust fractions and colors array in the case where startvalue != 0.0f and/or endvalue != 1.0f
        final java.util.List<Color> colorList = new java.util.ArrayList<Color>(GIVEN_COLORS.length);
        colorList.addAll(java.util.Arrays.asList(GIVEN_COLORS));

        // Assure that fractions start with 0.0f
        if (fractionList.get(0) != 0.0f) {
            fractionList.add(0, 0.0f);
            final Color TMP_COLOR = colorList.get(0);
            colorList.add(0, TMP_COLOR);
        }

        // Assure that fractions end with 1.0f
        if (fractionList.get(fractionList.size() - 1) != 1.0f) {
            fractionList.add(1.0f);
            colorList.add(GIVEN_COLORS[0]);
        }

        // Recalculate the fractions and colors with the given offset
        final java.util.Map<Float, Color> fractionColors = recalculate(fractionList, colorList, OFFSET);

        // Clear the original FRACTION_LIST and COLOR_LIST
        fractionList.clear();
        colorList.clear();

        // Sort the hashmap by fraction and add the values to the FRACION_LIST and COLOR_LIST
        final java.util.SortedSet<Float> sortedFractions = new java.util.TreeSet<Float>(fractionColors.keySet());
        for (final Float CURRENT_FRACTION : sortedFractions) {
            fractionList.add(CURRENT_FRACTION);
            colorList.add(fractionColors.get(CURRENT_FRACTION));
        }

        // Set the values
        this.CENTER = CENTER;
        COLORS = colorList.toArray(new Color[colorList.size()]);

        // Prepare lookup table for the angles of each fraction
        final int MAX_FRACTIONS = fractionList.size();
        this.FRACTION_ANGLES = new float[MAX_FRACTIONS];
        for (int i = 0; i < MAX_FRACTIONS; i++) {
            FRACTION_ANGLES[i] = fractionList.get(i) * 360f;
        }

        // Prepare lookup tables for the color stepsize of each color
        RED_STEP_LOOKUP = new float[COLORS.length];
        GREEN_STEP_LOOKUP = new float[COLORS.length];
        BLUE_STEP_LOOKUP = new float[COLORS.length];
        ALPHA_STEP_LOOKUP = new float[COLORS.length];

        for (int i = 0; i < (COLORS.length - 1); i++) {
            RED_STEP_LOOKUP[i] = ((COLORS[i + 1].getRed() - COLORS[i].getRed()) * INT_TO_FLOAT_CONST) / (FRACTION_ANGLES[i + 1] - FRACTION_ANGLES[i]);
            GREEN_STEP_LOOKUP[i] = ((COLORS[i + 1].getGreen() - COLORS[i].getGreen()) * INT_TO_FLOAT_CONST) / (FRACTION_ANGLES[i + 1] - FRACTION_ANGLES[i]);
            BLUE_STEP_LOOKUP[i] = ((COLORS[i + 1].getBlue() - COLORS[i].getBlue()) * INT_TO_FLOAT_CONST) / (FRACTION_ANGLES[i + 1] - FRACTION_ANGLES[i]);
            ALPHA_STEP_LOOKUP[i] = ((COLORS[i + 1].getAlpha() - COLORS[i].getAlpha()) * INT_TO_FLOAT_CONST) / (FRACTION_ANGLES[i + 1] - FRACTION_ANGLES[i]);
        }
    }

    /**
     * Recalculates the fractions in the FRACTION_LIST and their associated colors in the COLOR_LIST with a given OFFSET.
     * Because the conical gradients always starts with 0 at the top and clockwise direction
     * you could rotate the defined conical gradient from -180 to 180 degrees which equals values from -0.5 to +0.5
     * @param fractionList
     * @param colorList
     * @param OFFSET
     * @return Hashmap that contains the recalculated fractions and colors after a given rotation
     */
    private java.util.HashMap<Float, Color> recalculate(final List<Float> fractionList, final List<Color> colorList, final float OFFSET) {
        // Recalculate the fractions and colors with the given offset
        final int MAX_FRACTIONS = fractionList.size();
        final HashMap<Float, Color> fractionColors = new HashMap<Float, Color>(MAX_FRACTIONS);
        for (int i = 0; i < MAX_FRACTIONS; i++) {
            // Add offset to fraction
            final float TMP_FRACTION = fractionList.get(i) + OFFSET;

            // Color related to current fraction
            final Color TMP_COLOR = colorList.get(i);

            // Check each fraction for limits (0...1)
            if (TMP_FRACTION <= 0) {
                fractionColors.put(1.0f + TMP_FRACTION + 0.0001f, TMP_COLOR);

                final float NEXT_FRACTION;
                final Color NEXT_COLOR;
                if (i < MAX_FRACTIONS - 1) {
                    NEXT_FRACTION = fractionList.get(i + 1) + OFFSET;
                    NEXT_COLOR = colorList.get(i + 1);
                } else {
                    NEXT_FRACTION = 1 - fractionList.get(0) + OFFSET;
                    NEXT_COLOR = colorList.get(0);
                }
                if (NEXT_FRACTION > 0) {
                    final Color NEW_FRACTION_COLOR = getColorFromFraction(TMP_COLOR, NEXT_COLOR, (int) ((NEXT_FRACTION - TMP_FRACTION) * 10000), (int) ((-TMP_FRACTION) * 10000));
                    fractionColors.put(0.0f, NEW_FRACTION_COLOR);
                    fractionColors.put(1.0f, NEW_FRACTION_COLOR);
                }
            } else if (TMP_FRACTION >= 1) {
                fractionColors.put(TMP_FRACTION - 1.0f - 0.0001f, TMP_COLOR);

                final float PREVIOUS_FRACTION;
                final Color PREVIOUS_COLOR;
                if (i > 0) {
                    PREVIOUS_FRACTION = fractionList.get(i - 1) + OFFSET;
                    PREVIOUS_COLOR = colorList.get(i - 1);
                } else {
                    PREVIOUS_FRACTION = fractionList.get(MAX_FRACTIONS - 1) + OFFSET;
                    PREVIOUS_COLOR = colorList.get(MAX_FRACTIONS - 1);
                }
                if (PREVIOUS_FRACTION < 1) {
                    final Color NEW_FRACTION_COLOR = getColorFromFraction(TMP_COLOR, PREVIOUS_COLOR, (int) ((TMP_FRACTION - PREVIOUS_FRACTION) * 10000), (int) (TMP_FRACTION - 1.0f) * 10000);
                    fractionColors.put(1.0f, NEW_FRACTION_COLOR);
                    fractionColors.put(0.0f, NEW_FRACTION_COLOR);
                }
            } else {
                fractionColors.put(TMP_FRACTION, TMP_COLOR);
            }
        }

        // Clear the original FRACTION_LIST and COLOR_LIST
        fractionList.clear();
        colorList.clear();

        return fractionColors;
    }

    /**
     * With the START_COLOR at the beginning and the DESTINATION_COLOR at the end of the given RANGE the method will calculate
     * and return the color that equals the given VALUE.
     * e.g. a START_COLOR of BLACK (R:0, G:0, B:0, A:255) and a DESTINATION_COLOR of WHITE(R:255, G:255, B:255, A:255)
     * with a given RANGE of 100 and a given VALUE of 50 will return the color that is exactly in the middle of the
     * gradient between black and white which is gray(R:128, G:128, B:128, A:255)
     * So this method is really useful to calculate colors in gradients between two given colors.
     * @param START_COLOR
     * @param DESTINATION_COLOR
     * @param RANGE
     * @param VALUE
     * @return Color calculated from a range of values by given value
     */
    public static Color getColorFromFraction(final Color START_COLOR, final Color DESTINATION_COLOR, final int RANGE, final int VALUE) {
        final float SOURCE_RED = START_COLOR.getRed() * INT_TO_FLOAT_CONST;
        final float SOURCE_GREEN = START_COLOR.getGreen() * INT_TO_FLOAT_CONST;
        final float SOURCE_BLUE = START_COLOR.getBlue() * INT_TO_FLOAT_CONST;
        final float SOURCE_ALPHA = START_COLOR.getAlpha() * INT_TO_FLOAT_CONST;

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

        float red = SOURCE_RED + RED_FRACTION * VALUE;
        float green = SOURCE_GREEN + GREEN_FRACTION * VALUE;
        float blue = SOURCE_BLUE + BLUE_FRACTION * VALUE;
        float alpha = SOURCE_ALPHA + ALPHA_FRACTION * VALUE;

        red = red < 0f ? 0f : (red > 1f ? 1f : red);
        green = green < 0f ? 0f : (green > 1f ? 1f : green);
        blue = blue < 0f ? 0f : (blue > 1f ? 1f : blue);
        alpha = alpha < 0f ? 0f : (alpha > 1f ? 1f : alpha);

        return new Color(red, green, blue, alpha);
    }

    @Override
    public java.awt.PaintContext createContext(final ColorModel COLOR_MODEL,
                                               final Rectangle DEVICE_BOUNDS,
                                               final Rectangle2D USER_BOUNDS,
                                               final AffineTransform TRANSFORM,
                                               final RenderingHints HINTS) {
        final Point2D TRANSFORMED_CENTER = TRANSFORM.transform(CENTER, null);
        return new ConicalGradientPaintContext(TRANSFORMED_CENTER);
    }

    @Override
    public int getTransparency() {
        return Transparency.TRANSLUCENT;
    }

    private final class ConicalGradientPaintContext implements PaintContext {

        final private Point2D CENTER;

        public ConicalGradientPaintContext(final Point2D CENTER) {
            this.CENTER = new Point2D.Double(CENTER.getX(), CENTER.getY());
        }

        @Override
        public void dispose() {
        }

        @Override
        public java.awt.image.ColorModel getColorModel() {
            return ColorModel.getRGBdefault();
        }

        @Override
        public Raster getRaster(final int X, final int Y, final int TILE_WIDTH, final int TILE_HEIGHT) {
            final double ROTATION_CENTER_X = -X + CENTER.getX();
            final double ROTATION_CENTER_Y = -Y + CENTER.getY();

            final int MAX = FRACTION_ANGLES.length - 1;

            // Create raster for given colormodel
            final WritableRaster RASTER = getColorModel().createCompatibleWritableRaster(TILE_WIDTH, TILE_HEIGHT);

            // Create data array with place for red, green, blue and alpha values
            final int[] data = new int[(TILE_WIDTH * TILE_HEIGHT * 4)];

            double dx;
            double dy;
            double distance;
            double angle;
            double currentRed = 0;
            double currentGreen = 0;
            double currentBlue = 0;
            double currentAlpha = 0;

            for (int tileY = 0; tileY < TILE_HEIGHT; tileY++) {
                for (int tileX = 0; tileX < TILE_WIDTH; tileX++) {

                    // Calculate the distance between the current position and the rotation angle
                    dx = tileX - ROTATION_CENTER_X;
                    dy = tileY - ROTATION_CENTER_Y;
                    distance = Math.sqrt(dx * dx + dy * dy);

                    // Avoid division by zero
                    if (distance == 0) {
                        distance = 1;
                    }

                    // 0 degree on top
                    angle = Math.abs(Math.toDegrees(Math.acos(dx / distance)));

                    if (dx >= 0 && dy <= 0) {
                        angle = 90.0 - angle;
                    } else if (dx >= 0 && dy >= 0) {
                        angle += 90.0;
                    } else if (dx <= 0 && dy >= 0) {
                        angle += 90.0;
                    } else if (dx <= 0 && dy <= 0) {
                        angle = 450.0 - angle;
                    }

                    // Check for each angle in fractionAngles array
                    for (int i = 0; i < MAX; i++) {
                        if ((angle >= FRACTION_ANGLES[i])) {
                            currentRed = COLORS[i].getRed() * INT_TO_FLOAT_CONST + (angle - FRACTION_ANGLES[i]) * RED_STEP_LOOKUP[i];
                            currentGreen = COLORS[i].getGreen() * INT_TO_FLOAT_CONST + (angle - FRACTION_ANGLES[i]) * GREEN_STEP_LOOKUP[i];
                            currentBlue = COLORS[i].getBlue() * INT_TO_FLOAT_CONST + (angle - FRACTION_ANGLES[i]) * BLUE_STEP_LOOKUP[i];
                            currentAlpha = COLORS[i].getAlpha() * INT_TO_FLOAT_CONST + (angle - FRACTION_ANGLES[i]) * ALPHA_STEP_LOOKUP[i];
                        }
                    }

                    // Fill data array with calculated color values
                    final int BASE = (tileY * TILE_WIDTH + tileX) * 4;
                    data[BASE] = (int) (currentRed * 255);
                    data[BASE + 1] = (int) (currentGreen * 255);
                    data[BASE + 2] = (int) (currentBlue * 255);
                    data[BASE + 3] = (int) (currentAlpha * 255);
                }
            }

            // Fill the raster with the data
            RASTER.setPixels(0, 0, TILE_WIDTH, TILE_HEIGHT, data);

            return RASTER;
        }
    }

}