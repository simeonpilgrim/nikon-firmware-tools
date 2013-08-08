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
 * Definition of colors that will be used in gradients etc.
 * This is useful to assure that you use the same color combinations
 * in all the different components. Each color is defined in three
 * brightness levels.
 * @author hansolo
 */
public enum ColorDef {

    RED(new Color(82, 0, 0), new Color(158, 0, 19), new Color(213, 0, 25), new Color(213, 0, 0), new Color(240, 82, 88), new Color(255, 171, 173), new Color(255, 217, 218)),
    GREEN(new Color(8, 54, 4), new Color(0, 107, 14), new Color(15, 148, 0), new Color(0, 148, 0), new Color(121, 186, 37), new Color(190, 231, 141), new Color(234, 247, 218)),
    BLUE(new Color(0, 11, 68), new Color(0, 73, 135), new Color(0, 108, 201), new Color(0, 120, 220), new Color(0, 141, 242), new Color(122, 200, 255), new Color(204, 236, 255)),
    ORANGE(new Color(127, 34, 0), new Color(215, 67, 0), new Color(240, 117, 0), new Color(248, 142, 0), new Color(255, 166, 0), new Color(255, 200, 0), new Color(255, 225, 128)),
    YELLOW(new Color(41, 41, 0), new Color(102, 102, 0), new Color(177, 165, 0), new Color(210, 204, 0 ), new Color(255, 242, 0), new Color(255, 250, 153), new Color(255, 252, 204)),
    CYAN(new Color(15, 56, 80), new Color(0, 109, 144), new Color(0, 144, 191), new Color(0, 159, 215), new Color(0, 174, 239), new Color(153, 223, 249), new Color(204, 239, 252)),
    MAGENTA(new Color(98, 0, 114), new Color(128, 24, 72), new Color(191, 36, 107), new Color(223, 42, 125), new Color(255, 48, 143), new Color(255, 172, 210), new Color(255, 214, 23)),
    WHITE(new Color(210, 210, 210), new Color(220, 220, 220), new Color(235, 235, 235), new Color(245, 245, 245), Color.WHITE, Color.WHITE, Color.WHITE),
    GRAY(new Color(25, 25, 25), new Color(51, 51, 51), new Color(76, 76, 76), new Color(102, 102, 102), new Color(128, 128, 128), new Color(204, 204, 204), new Color(243, 243, 243)),
    BLACK(new Color(0, 0, 0), new Color(5, 5, 5), new Color(10, 10, 10), new Color(15, 15, 15), new Color(20, 20, 20), new Color(25, 25, 25), new Color(30, 30, 30)),
    RAITH(new Color(0, 32, 65), new Color(0, 65, 125), new Color(0, 106, 172), new Color(65, 143, 193), new Color(130, 180, 214), new Color(148, 203, 242), new Color(191, 229, 255)),
    GREEN_LCD(new Color(0, 55, 45), new Color(15, 109, 93), new Color(0, 185, 165), new Color(24, 220, 183), new Color(48, 255, 204), new Color(153, 255, 227), new Color(204, 255, 241)),
    JUG_GREEN(new Color(0, 56, 0), new Color(0x204524), new Color(0x32A100), new Color(0x5AB700), new Color(0x81CE00), new Color(190, 231, 141), new Color(234, 247, 218)),
    CUSTOM(null, null, null, null, null, null, null);
    public final Color VERY_DARK;
    public final Color DARK;
    public final Color MEDIUM;
    public final Color NORMAL;
    public final Color LIGHT;
    public final Color LIGHTER;
    public final Color VERY_LIGHT;

    ColorDef(final Color VERY_DARK_COLOR, final Color DARK_COLOR, final Color MEDIUM_COLOR, final Color NORMAL_COLOR,
             final Color LIGHT_COLOR, final Color LIGHTER_COLOR, final Color VERY_LIGHT_COLOR) {
        VERY_DARK = VERY_DARK_COLOR;
        DARK = DARK_COLOR;
        MEDIUM = MEDIUM_COLOR;
        NORMAL = NORMAL_COLOR;
        LIGHT = LIGHT_COLOR;
        LIGHTER = LIGHTER_COLOR;
        VERY_LIGHT = VERY_LIGHT_COLOR;
    }
}