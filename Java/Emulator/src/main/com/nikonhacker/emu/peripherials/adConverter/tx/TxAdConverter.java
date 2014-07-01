package com.nikonhacker.emu.peripherials.adConverter.tx;

import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.peripherials.adConverter.AdConverter;
import com.nikonhacker.emu.peripherials.adConverter.AdUnit;
import com.nikonhacker.emu.peripherials.adConverter.AdValueProvider;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;

public class TxAdConverter implements AdConverter {
    public TxAdUnit[] units;

    private AdValueProvider provider;

    public TxAdConverter(Emulator emulator, TxInterruptController interruptController, AdValueProvider provider) {
        this.provider = provider;
        units = new TxAdUnit[3];
        units[0] = new TxAdUnit('A', 4, emulator, interruptController, provider);
        units[1] = new TxAdUnit('B', 4, emulator, interruptController, provider);
        units[2] = new TxAdUnit('C', 8, emulator, interruptController, provider);
    }

    @Override
    public AdUnit[] getUnits() {
        return units;
    }

    public final AdValueProvider getProvider() {
        return provider;
    }
}
