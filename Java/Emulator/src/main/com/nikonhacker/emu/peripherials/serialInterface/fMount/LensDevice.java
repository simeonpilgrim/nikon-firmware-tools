package com.nikonhacker.emu.peripherials.serialInterface.fMount;

import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.fMount.FMountCircuit;

public abstract class LensDevice extends SerialDevice {

    FMountCircuit   fMountCircuit;
    
    protected LensDevice(FMountCircuit fMountCircuit) {
        this.fMountCircuit = fMountCircuit;
    }

    @Override
    public final void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        throw new RuntimeException("LensDevice.onBitNumberChange not possible");
    }

    public abstract String toString();

    public abstract void setPin2Value(int value);
    
    public void onTimer() {
    }

    public abstract Integer read();

    public void unplug() {
    }
}
