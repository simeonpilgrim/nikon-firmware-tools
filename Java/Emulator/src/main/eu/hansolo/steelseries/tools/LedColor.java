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
 * Definition of color combinations for different led colors.
 * @author hansolo
 */
public enum LedColor {

    RED_LED(new Color(16751241), new Color(16751241), new Color(16724736), new Color(16747888), new Color(8264704), new Color(8264704), new Color(6560512)),
    GREEN_LED(new Color(10157961), new Color(10157961), new Color(5898026), new Color(10878720), new Color(1867264), new Color(1867264), new Color(1795072)),
    BLUE_LED(new Color(9018111), new Color(9018111), new Color(13311), new Color(7376383), new Color(7294), new Color(7294), new Color(7012)),
    ORANGE_LED(new Color(0xFEA23F), new Color(0xFEA23F), new Color(0xFD6C00), new Color(0xFD6C00), new Color(0x592800), new Color(0x592800), new Color(0x421F00)),
    YELLOW_LED(new Color(0xFFFF62), new Color(0xFFFF62), new Color(0xFFFF00), new Color(0xFFFF00), new Color(0x6B6D00), new Color(0x6B6D00), new Color(0x515300)),
    CYAN_LED(new Color(0x00FFFF), new Color(0x00FFFF), new Color(0x1BC3C3), new Color(0x00FFFF), new Color(0x083B3B), new Color(0x083B3B), new Color(0x052727)),
    MAGENTA_LED(new Color(0xD300FF), new Color(0xD300FF), new Color(0x8600CB), new Color(0xC300FF), new Color(0x38004B), new Color(0x38004B), new Color(0x280035)),
    RED(ColorDef.RED.LIGHTER, ColorDef.RED.LIGHTER, ColorDef.RED.DARK, ColorDef.RED.LIGHTER, ColorDef.RED.DARK, ColorDef.RED.DARK, ColorDef.RED.VERY_DARK),
    GREEN(ColorDef.GREEN.LIGHTER, ColorDef.GREEN.LIGHTER, ColorDef.GREEN.DARK, ColorDef.GREEN.LIGHTER, ColorDef.GREEN.DARK, ColorDef.GREEN.DARK, ColorDef.GREEN.VERY_DARK),
    BLUE(ColorDef.BLUE.LIGHTER, ColorDef.BLUE.LIGHTER, ColorDef.BLUE.DARK, ColorDef.BLUE.LIGHTER, ColorDef.BLUE.DARK, ColorDef.BLUE.DARK, ColorDef.BLUE.VERY_DARK),
    ORANGE(ColorDef.ORANGE.LIGHTER, ColorDef.ORANGE.LIGHTER, ColorDef.ORANGE.DARK, ColorDef.ORANGE.LIGHTER, ColorDef.ORANGE.DARK, ColorDef.ORANGE.DARK, ColorDef.ORANGE.VERY_DARK),
    YELLOW(ColorDef.YELLOW.LIGHTER, ColorDef.YELLOW.LIGHTER, ColorDef.YELLOW.DARK, ColorDef.YELLOW.LIGHTER, ColorDef.YELLOW.DARK, ColorDef.YELLOW.DARK, ColorDef.YELLOW.VERY_DARK),
    CYAN(ColorDef.CYAN.LIGHTER, ColorDef.CYAN.LIGHTER, ColorDef.CYAN.DARK, ColorDef.CYAN.LIGHTER, ColorDef.CYAN.DARK, ColorDef.CYAN.DARK, ColorDef.CYAN.VERY_DARK),
    MAGENTA(ColorDef.MAGENTA.LIGHTER, ColorDef.MAGENTA.LIGHTER, ColorDef.MAGENTA.DARK, ColorDef.MAGENTA.LIGHTER, ColorDef.MAGENTA.DARK, ColorDef.MAGENTA.DARK, ColorDef.MAGENTA.VERY_DARK),
    CUSTOM(null, null, null, null, null, null, null);
    public final Color INNER_COLOR1_ON;
    public final Color INNER_COLOR2_ON;
    public final Color OUTER_COLOR_ON;
    public final Color CORONA_COLOR;
    public final Color INNER_COLOR1_OFF;
    public final Color INNER_COLOR2_OFF;
    public final Color OUTER_COLOR_OFF;

    LedColor(final Color INNER_COLOR1_ON, final Color INNER_COLOR2_ON,
             final Color OUTER_COLOR_ON, final Color CORONA_COLOR,
             final Color INNER_COLOR1_OFF, final Color INNER_COLOR2_OFF,
             final Color OUTER_COLOR_OFF) {
        this.INNER_COLOR1_ON = INNER_COLOR1_ON;
        this.INNER_COLOR2_ON = INNER_COLOR2_ON;
        this.OUTER_COLOR_ON = OUTER_COLOR_ON;
        this.CORONA_COLOR = CORONA_COLOR;
        this.INNER_COLOR1_OFF = INNER_COLOR1_OFF;
        this.INNER_COLOR2_OFF = INNER_COLOR2_OFF;
        this.OUTER_COLOR_OFF = OUTER_COLOR_OFF;
    }

    @Override
    public String toString() {
        return "{" + INNER_COLOR1_ON + "," + INNER_COLOR2_ON + "," + OUTER_COLOR_ON + "," + CORONA_COLOR + "," + INNER_COLOR1_OFF + "," + INNER_COLOR2_OFF + "," + OUTER_COLOR_OFF + "}";
    }
}