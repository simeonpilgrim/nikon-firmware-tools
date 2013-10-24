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


/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class CustomLedColor {

    public final Color COLOR;
    public final Color INNER_COLOR1_ON;
    public final Color INNER_COLOR2_ON;
    public final Color OUTER_COLOR_ON;
    public final Color CORONA_COLOR;
    public final Color INNER_COLOR1_OFF;
    public final Color INNER_COLOR2_OFF;
    public final Color OUTER_COLOR_OFF;

    public CustomLedColor(final Color COLOR) {
        this.COLOR = COLOR;
        final float HUE = Color.RGBtoHSB(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), null)[0];
        if (COLOR.getRed() == COLOR.getGreen() && COLOR.getRed() == COLOR.getBlue()) {
            INNER_COLOR1_ON = Color.getHSBColor(HUE, 0.0f, 1.0f);
            INNER_COLOR2_ON = Color.getHSBColor(HUE, 0.0f, 1.0f);
            OUTER_COLOR_ON = Color.getHSBColor(HUE, 0.0f, 0.99f);
            CORONA_COLOR = Color.getHSBColor(HUE, 0.0f, 1.00f);
            INNER_COLOR1_OFF = Color.getHSBColor(HUE, 0.0f, 0.35f);
            INNER_COLOR2_OFF = Color.getHSBColor(HUE, 0.0f, 0.35f);
            OUTER_COLOR_OFF = Color.getHSBColor(HUE, 0.0f, 0.26f);
        } else {
            INNER_COLOR1_ON = Color.getHSBColor(HUE, 0.75f, 1.0f);
            INNER_COLOR2_ON = Color.getHSBColor(HUE, 0.75f, 1.0f);
            OUTER_COLOR_ON = Color.getHSBColor(HUE, 1.0f, 0.99f);
            CORONA_COLOR = Color.getHSBColor(HUE, 0.75f, 1.00f);
            INNER_COLOR1_OFF = Color.getHSBColor(HUE, 1.0f, 0.35f);
            INNER_COLOR2_OFF = Color.getHSBColor(HUE, 1.0f, 0.35f);
            OUTER_COLOR_OFF = Color.getHSBColor(HUE, 1.0f, 0.26f);
        }
    }
}