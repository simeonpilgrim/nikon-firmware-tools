package com.nikonhacker.emu.peripherials.frontPanel;

import com.nikonhacker.Prefs;

import java.util.HashMap;
import java.util.Map;

public class FrontPanel {
    Map<String, CameraButton> buttons = new HashMap<>();
    private Prefs prefs;

    public FrontPanel(Prefs prefs) {
        this.prefs = prefs;
    }

    public Map<String, CameraButton> getButtons() {
        return buttons;
    }

    /**
     *
     * @param key
     * @param imageSuffixes  an array[2][n] image suffixes. The first array is the "non-hover", the second one is the "hover". n is the number of states the button can be in.
     * @param isLeftClickTemp
     * @param isReversed
     */
    protected void addButton(String key, String[][] imageSuffixes, boolean isLeftClickTemp, boolean isReversed) {
        getButtons().put(key, new CameraButton(key, imageSuffixes, isLeftClickTemp, isReversed, prefs));
    }

    public CameraButton getButton(String key) {
        return buttons.get(key);
    }
}
