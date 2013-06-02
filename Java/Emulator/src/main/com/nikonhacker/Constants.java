package com.nikonhacker;

import java.awt.*;

public interface Constants {
    /* These 2 constants are used to index everything pertaining to one chip or the other */
    int      CHIP_NONE  = -1;
    int      CHIP_FR    = 0;
    int      CHIP_TX    = 1;
    String[] CHIP_LABEL = {"FR", "TX"};

    Color COLOR_HI  = Color.RED;
    Color COLOR_HIZ = Color.ORANGE;
    Color COLOR_LO  = Color.BLUE;

    String LABEL_HI  = "VCC";
    String LABEL_HIZ = "---";
    String LABEL_LO  = "GND";
}
