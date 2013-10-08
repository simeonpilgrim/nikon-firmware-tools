package com.nikonhacker.emu.peripherials.frontPanel;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.ioPort.Pin;

/**
 * This is the model for a camera button. This has nothing to do with UI
 */
public class CameraButton {
    private String     key;
    private String[][] imageSuffixes;
    private boolean    isLeftClickTemp;

    private Pin[]   buttonPins;
    private int[][] statePinValues;
    private Prefs   prefs;
    private int     state;

    /**
     * Creates a new CameraButton that can be in 'n' distinct states.
     * @param key this is the symbolic name of the button. Also used as prefix for all images used for this button
     * @param imageSuffixes an array[2][n] of image filename suffixes.
     *                      The first dimension is 0 for the "non-hover" version, and 1 for the "hover" version.
     * @param isLeftClickTemp if true, a single left click presses and releases the button. If not, the left click toggles the button
     * @param statePinValues an array[p][n] of values that each pin should be returned for to each state
     * @param prefs the preference object to load/store values
     */
    public CameraButton(String key, String[][] imageSuffixes, boolean isLeftClickTemp, int[][] statePinValues, Prefs prefs) {
        // Sanity check on arrays
        int numberPins = statePinValues.length;
        int numberStates = statePinValues[0].length;
        for (int pinNumber = 0; pinNumber < numberPins; pinNumber++) {
            int[] pinValues = statePinValues[pinNumber];
            if (pinValues.length != numberStates) {
                throw new RuntimeException("Error initializing CameraButton for button key '" + key + "': " + numberStates + " states are defined for pin 0, but array for pin " + pinNumber + " has " + pinValues.length + " values !");
            }
        }
        for (int hoverState = 0; hoverState < 2; hoverState++) {
            String[] imageSuffix = imageSuffixes[hoverState];
            if (imageSuffix.length != numberStates) {
                throw new RuntimeException("Error initializing CameraButton for button key '" + key + "': " + numberStates + " states are defined, but array in hover state " + hoverState + " has " + imageSuffix.length + " pictures !");
            }
        }

        // OK, initialize fields
        this.key = key;
        this.imageSuffixes = imageSuffixes;
        this.isLeftClickTemp = isLeftClickTemp;
        this.statePinValues = statePinValues;
        this.prefs = prefs;
        this.buttonPins = new Pin[numberPins];

        // Initialize state according to last stored state
        Integer storedState = prefs.getButtonState(key);
        if (storedState == null) {
            state = 0;
        }
        else {
            state = storedState;
        }

        // Create pins, initialized according to stored state
        for (int i = 0; i < numberPins; i++) {
            buttonPins[i] = new ButtonOutputPin(key + " button" + (numberPins==1?"":(""+i)), statePinValues[i][state]);
        }
    }

    public String getKey() {
        return key;
    }

    public String[][] getImageSuffixes() {
        return imageSuffixes;
    }

    public boolean isLeftClickTemp() {
        return isLeftClickTemp;
    }

    public Pin getPin(int pinNumber) {
        return buttonPins[pinNumber];
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        prefs.setButtonState(key, state);
        for (int pinNumber = 0; pinNumber < statePinValues.length; pinNumber++) {
            buttonPins[pinNumber].setOutputValue(statePinValues[pinNumber][state]);
        }
    }
}
