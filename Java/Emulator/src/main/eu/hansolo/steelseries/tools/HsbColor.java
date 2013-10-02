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


public class HsbColor {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private final float mHue;
    private final float mSaturation;
    private final float mBrightness;
    private final Color mColor;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    private HsbColor(Builder builder) {
        // private Constructor can only be called from Builder
        mHue = builder.getHue();
        mSaturation = builder.getSaturation();
        mBrightness = builder.getBrightness();
        final Color TMP_COLOR = new Color(Color.HSBtoRGB(mHue, mSaturation, mBrightness));
        mColor = new Color(TMP_COLOR.getRed(), TMP_COLOR.getGreen(), TMP_COLOR.getBlue(), builder.getAlpha());
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public float getHue() {
        return mHue;
    }

    public float getSaturation() {
        return mSaturation;
    }

    public float getBrightness() {
        return mBrightness;
    }

    public Color getColor() {
        return mColor;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder">
    public static class Builder {
        // mandatory parameter
        private float hue = 0;
        private float saturation = 0;
        private float brightness = 0;
        private int alpha = 255;

        public Builder(Color color) {
            final float[] HSB_VALUES = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            hue = HSB_VALUES[0];
            saturation = HSB_VALUES[1];
            brightness = HSB_VALUES[2];
            alpha = color.getAlpha();
        }

        public Builder hue(final float HUE) {
            hue = HUE < 0 ? 0 : (HUE > 1 ? 1 : HUE);
            return this;
        }

        public Builder relHue(final float REL_HUE) {
            hue = (hue + REL_HUE) < 0 ? 0 : (hue + REL_HUE > 360 ? 360 : (hue + REL_HUE));
            return this;
        }

        public float getHue() {
            return hue;
        }

        public Builder saturation(final float SATURATION) {
            saturation = SATURATION < 0 ? 0 : (SATURATION > 1 ? 1 : SATURATION);
            return this;
        }

        public Builder relSaturation(final float REL_SATURATION) {
            saturation = (saturation + REL_SATURATION) < 0 ? 0 : (saturation + REL_SATURATION > 1 ? 1 : (saturation + REL_SATURATION));
            return this;
        }

        public float getSaturation() {
            return saturation;
        }

        public Builder brightness(final float BRIGHTNESS) {
            brightness = BRIGHTNESS < 0 ? 0 : (BRIGHTNESS > 1 ? 1 : BRIGHTNESS);
            return this;
        }

        public Builder relBrightness(final float REL_BRIGHTNESS) {
            brightness = (brightness + REL_BRIGHTNESS) < 0 ? 0 : (brightness + REL_BRIGHTNESS > 1 ? 1 : (brightness + REL_BRIGHTNESS));
            return this;
        }

        public float getBrightness() {
            return brightness;
        }

        public Builder alpha(final int ALPHA) {
            alpha = ALPHA < 0 ? 0 : (ALPHA > 255 ? 255 : ALPHA);
            return this;
        }

        public int getAlpha() {
            return alpha;
        }

        public HsbColor build() {
            return new HsbColor(this);
        }
    }
    // </editor-fold>
}