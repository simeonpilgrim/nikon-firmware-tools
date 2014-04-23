package com.nikonhacker.emu.peripherials.frontPanel;

import com.nikonhacker.Prefs;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a the model of a generic camera front panel.
 * This has nothing to do with the UI
 */
public class FrontPanel {
    public static final String KEY_PLUS_MINUS = "+-";
    public static final String KEY_TIMER      = "timer";
    public static final String KEY_UP         = "up";
    public static final String KEY_POWER      = "power";
    public static final String KEY_FLASH      = "flash";
    public static final String KEY_DOWN       = "down";
    public static final String KEY_LEFT       = "left";
    public static final String KEY_REC        = "rec";
    public static final String KEY_DELETE     = "delete";
    public static final String KEY_INFO       = "info";
    public static final String KEY_LIVEVIEW   = "liveview";
    public static final String KEY_PLAY       = "play";
    public static final String KEY_MENU       = "menu";
    public static final String KEY_ZOOM_OUT   = "zoomout";
    public static final String KEY_ZOOM_IN    = "zoomin";
    public static final String KEY_OK         = "ok";
    public static final String KEY_I          = "i";
    public static final String KEY_RIGHT      = "right";
    public static final String KEY_AEL_AFL    = "aelafl";
    public static final String KEY_SHUTTER    = "shutter";
    public static final String KEY_DIAL       = "dial";
    public static final String KEY_MODEDIAL   = "modedial";

    public static final String KEY_CARDLED    = "led";

    Map<String, CameraButton> buttons = new HashMap<>();
    private Prefs prefs;
    CameraLed led;

    public FrontPanel(Prefs prefs) {
        this.prefs = prefs;
    }

    public Map<String, CameraButton> getButtons() {
        return buttons;
    }

    /**
     * Adds a button to the front panel
     * @param key the unique String identifying this button. Also used as filename prefix
     * @param imageSuffixes an array[2][n] of image suffixes. The first array is the "non-hover", the second one is the "hover". n is the number of states the button can be in.
     * @param isLeftClickTemp if true, a left click only pushes on the button until the mouse button is released.
     * @param statePinValues an array[p][n] of pin values, indicating for each pin, the value it should be in for each state. p is the number of pins to drive. n is the number of states the button can be in.
     */
    protected void addCameraButton(String key, String[][] imageSuffixes, boolean isLeftClickTemp, int[][] statePinValues) {
        buttons.put(key, new CameraButton(key, imageSuffixes, isLeftClickTemp, statePinValues, prefs));
    }

    protected void addCameraLed(String key, String[][] imageSuffixes) {
        if (led!=null)
            throw new RuntimeException("Only one LED is implemented");
        else
            led = new CameraLed(key, imageSuffixes);
    }

    public CameraButton getButton(String key) {
        return buttons.get(key);
    }

    public CameraLed getLed(String key) {
        return led;
    }
}
