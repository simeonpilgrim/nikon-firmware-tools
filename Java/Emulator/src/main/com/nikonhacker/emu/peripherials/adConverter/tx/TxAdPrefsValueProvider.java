package com.nikonhacker.emu.peripherials.adConverter.tx;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.adConverter.AdValueProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxAdPrefsValueProvider implements AdValueProvider {

    private Prefs prefs;
    private int chip;
    private Map<String, Integer> indices;

    public TxAdPrefsValueProvider(Prefs prefs, int chip) {
        this.prefs = prefs;
        this.chip = chip;
        indices = new HashMap<>();
    }


    @Override
    public int getAnalogValue(char unitName, int channel) {
        String key = "" + unitName + channel;
        if (prefs.isAdValueFromList(chip)) {
            List<Integer> values = prefs.getAdValueList(chip, key);
            if (values == null) {
                values = new ArrayList<Integer>();
                prefs.setAdValueList(chip, key, values);
            }
            Integer index = indices.get(key);
            if (index == null) {
                index = 0;
            }
            int value;
            if (index < values.size()) {
                value = values.get(index);
            }
            else {
                value = 0;
                values.add(0, value);
            }
            index++;
            if (index >= values.size()) {
                index = 0;
            }
            indices.put(key, index);
            return value;
        }
        else {
            return prefs.getAdValue(chip, key);
        }
    }

    public final void setPrefs(Prefs prefs) {
        this.prefs = prefs;
    }
}
