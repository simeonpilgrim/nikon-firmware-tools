package com.nikonhacker.emu.peripherials.adConverter.tx;

import com.nikonhacker.emu.peripherials.adConverter.AdConverter;

public class TxAdConverter implements AdConverter {
    public TxAdUnit[] units;

    public TxAdConverter() {
        units = new TxAdUnit[3];
        units[0] = new TxAdUnit('A', 4);
        units[1] = new TxAdUnit('B', 4);
        units[2] = new TxAdUnit('C', 8);
    }
}
