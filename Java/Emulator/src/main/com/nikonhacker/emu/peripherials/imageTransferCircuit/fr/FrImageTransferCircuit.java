package com.nikonhacker.emu.peripherials.imageTransferCircuit.fr;

import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.imageTransferCircuit.ImageTransferCircuit;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;

public class FrImageTransferCircuit implements ImageTransferCircuit {

    public FrImageTransferChannel channels[] = new FrImageTransferChannel[2];
    
    private Platform platform;
    private int interruptStatus, interruptMask;
    
    private boolean enabled;

    public FrImageTransferCircuit(Platform platform) {
        this.platform = platform;
        for (int i = 0; i < channels.length; i++) {
            channels[i] = new FrImageTransferChannel(i, this, platform);
        }
    }

    public int getEnabled() {
        return (enabled ? 0 : 0x8000);
    }
    
    public void setEnabled(int value) {
        enabled = ((value & 0x8000)==0);
    }

    public int getInterruptStatus() {
        return interruptStatus;
    }
    
    public void setInterruptStatus(int value) {
        synchronized (this) {
            interruptStatus &= (~value);
            if ((interruptStatus & interruptMask)==0)
                platform.getSharedInterruptCircuit().removeRequest(FrInterruptController.IMAGE_28_SHARED_REQUEST_NR, 30);
        }
    }
    
    public int getInterruptMask() {
        return interruptMask;
    }
    
    public void setInterruptMask(int value) {
        synchronized (this) {
            interruptMask = value;
        }
    }

    public void requestInterrupt (int channel) {
        synchronized (this) {
            interruptStatus |= (1<<(channel+4));
            if ((interruptStatus & interruptMask)!=0)
                platform.getSharedInterruptCircuit().request(FrInterruptController.IMAGE_28_SHARED_REQUEST_NR, 30);
        }
    }

}
