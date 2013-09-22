package com.nikonhacker.emu.peripherials.lcd.fr;

public class FrLcd {
    public static final int CAMERA_SCREEN_MEMORY_Y = 0xCE57DC60;
    public static final int CAMERA_SCREEN_MEMORY_U = CAMERA_SCREEN_MEMORY_Y + 0x64000;
    public static final int CAMERA_SCREEN_MEMORY_V = CAMERA_SCREEN_MEMORY_Y + 2 * 0x64000;
    public static final int CAMERA_SCREEN_WIDTH    = 640;
    public static final int CAMERA_SCREEN_HEIGHT   = 480;

}
