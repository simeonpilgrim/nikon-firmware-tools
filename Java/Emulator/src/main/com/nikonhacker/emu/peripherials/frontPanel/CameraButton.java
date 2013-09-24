package com.nikonhacker.emu.peripherials.frontPanel;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.ioPort.Pin;

/**
 * This is the model for a camera button. This has nothing to do with UI
 */
public class CameraButton {
    private String   key;
    private String[][] imageSuffixes;
    private boolean  leftClickTemp;

    Pin buttonPin;
    private boolean reversed;

    /**
     * Creates a new CameraButton
     * @param key this is the symbolic name of the button. Also used as prefix for images
     * @param imageSuffixes an array[2][n] image filename suffixes.
     *                      The first array is the "non-hover" version, the second one is the "hover" version.
     *                      n is the number of states the button can be in.
     * @param isLeftClickTemp if true, a single left click presses and releases the button. If not, the left click toggles the button
     * @param isReversed if true, the button value is 1 when released and 0 when pressed (pull up)
     * @param prefs the preference object to load/store values
     */
    public CameraButton(String key, String[][] imageSuffixes, boolean isLeftClickTemp, boolean isReversed, Prefs prefs) {
        this.key = key;
        this.imageSuffixes = imageSuffixes;
        leftClickTemp = isLeftClickTemp;
        reversed = isReversed;
        buttonPin = new ButtonOutputPin(key, prefs, isReversed);
    }

    public String getKey() {
        return key;
    }

    public String[][] getImageSuffixes() {
        return imageSuffixes;
    }

    public boolean isLeftClickTemp() {
        return leftClickTemp;
    }

    public Pin getPin() {
        return buttonPin;
    }

    public boolean isReversed() {
        return reversed;
    }
}
