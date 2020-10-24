package com.nikonhacker;

import java.awt.Color;

public interface Constants {
    /* These 2 constants are used to index everything pertaining to one chip or the other */
    int      CHIP_NONE             = -1;
    int      CHIP_FR               = 0;
    int      CHIP_TX               = 1;
    int      CHIP_ARM              = 2;
    String[] CHIP_LABEL            = {"FR80", "TX19"};
    Color[]  CHIP_BACKGROUND_COLOR = {new Color(240, 240, 255), new Color(248, 255, 248)};

    Color COLOR_HI  = Color.RED;
    Color COLOR_HIZ = Color.ORANGE;
    Color COLOR_LO  = Color.BLUE;
    Color COLOR_PULLUP = Color.GREEN;

    String LABEL_HI  = "VCC";
    String LABEL_HIZ = "---";
    String LABEL_LO  = "GND";


}
