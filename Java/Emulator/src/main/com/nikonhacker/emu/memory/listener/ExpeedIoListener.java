package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.interruptController.InterruptController;

public class ExpeedIoListener implements IoActivityListener {

    private static final int REGISTER_DICR = 0x44;

    private static final int DELAY_INTERRUPT_REQUEST_NR = 0x3F;

    private final CPUState cpuState;
    private final InterruptController interruptController;

    public ExpeedIoListener(CPUState cpuState, InterruptController interruptController) {
        this.cpuState = cpuState;
        this.interruptController = interruptController;
    }

    public void onIoLoad8(byte[] ioPage, int offset, byte value) {

    }

    public void onIoStore8(byte[] ioPage, int offset, byte value) {
        switch (offset) {
            case REGISTER_DICR:
                if ((value & 0x1) == 0) {
                    interruptController.removeRequest(DELAY_INTERRUPT_REQUEST_NR);
                }
                else {
                    interruptController.request(DELAY_INTERRUPT_REQUEST_NR);
                }
                break;
        }
    }
}
