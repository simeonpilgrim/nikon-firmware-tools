package com.nikonhacker.emu.peripherials.frontPanel;

import com.nikonhacker.emu.peripherials.ioPort.Pin;

public class CameraLed {
    private String     key;
    private String[][] imageSuffixes;

    private LedPin ledPin;
    private LedStateChangeListener  listener;

    public CameraLed(String key, String[][] imageSuffixes) {

        for (int hoverState = 0; hoverState < 2; hoverState++) {
            String[] imageSuffix = imageSuffixes[hoverState];
            if (imageSuffix.length != 2) {
                throw new RuntimeException("Error initializing CameraLed '" + key + "': 2 states are defined, but array in hover state " + hoverState + " has " + imageSuffix.length + " pictures !");
            }
        }

        // OK, initialize fields
        this.key = key;
        this.imageSuffixes = imageSuffixes;
        ledPin = new LedPin(key.toUpperCase() + " LED");
    }

    public String getKey() {
        return key;
    }

    public String[][] getImageSuffixes() {
        return imageSuffixes;
    }


    public Pin getPin() {
        return ledPin;
    }

    public void registerListener(LedStateChangeListener listener) {
        this.listener = listener;
    }

    public void unregisterListener() {
        this.listener = null;
    }

    private class LedPin extends Pin {

        public LedPin(String name) {
            super(name);
        }

        @Override
        public void setInputValue(int value) {
            if (listener!=null)
                listener.onValueChange(value);
        }

    }
}
