package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.interruptController.InterruptController;

public class ExpeedIoListener implements IoActivityListener {

    private static final int REGISTER_DICR    = 0x44;

    private static final int REGISTER_TMRLRA0 = 0x48;
    private static final int REGISTER_TMR0    = 0x4A;
    private static final int REGISTER_TMCSR0  = 0x4E;

    private static final int REGISTER_TMRLRA1 = REGISTER_TMRLRA0 + 8;
    private static final int REGISTER_TMR1 = REGISTER_TMR0 + 8;
    private static final int REGISTER_TMCSR1 = REGISTER_TMCSR0 + 8;

    private static final int REGISTER_TMRLRA2 = REGISTER_TMRLRA0 + 16;
    private static final int REGISTER_TMR2 = REGISTER_TMR0 + 16;
    private static final int REGISTER_TMCSR2 = REGISTER_TMCSR0 + 16;

    private final CPUState cpuState;
    private final InterruptController interruptController;

    private final ReloadTimer reloadTimers[];

    public ExpeedIoListener(CPUState cpuState, InterruptController interruptController, ReloadTimer[] reloadTimers) {
        this.cpuState = cpuState;
        this.interruptController = interruptController;
        this.reloadTimers = reloadTimers;
    }

    /**
     * Called when reading 8-bit value from register address range
     * @param ioPage
     * @param addr
     * @param value
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Byte onIoLoad8(byte[] ioPage, int addr, byte value) {
        return null;
    }

    /**
     * Called when reading 16-bit value from register address range
     *
     * @param ioPage
     * @param addr
     * @param value
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Integer onIoLoad16(byte[] ioPage, int addr, int value) {
        switch (addr) {
            case REGISTER_TMRLRA0:
                return reloadTimers[0].getReloadValue();
            case REGISTER_TMR0:
                return reloadTimers[0].getCurrentValue();
            case REGISTER_TMCSR0:
                return reloadTimers[0].getConfiguration();

            case REGISTER_TMRLRA1:
                return reloadTimers[1].getReloadValue();
            case REGISTER_TMR1:
                return reloadTimers[1].getCurrentValue();
            case REGISTER_TMCSR1:
                return reloadTimers[1].getConfiguration();

            case REGISTER_TMRLRA2:
                return reloadTimers[2].getReloadValue();
            case REGISTER_TMR2:
                return reloadTimers[2].getCurrentValue();
            case REGISTER_TMCSR2:
                return reloadTimers[2].getConfiguration();
        }
        return null;
    }

    /**
     * Called when reading 32-bit value from register address range
     *
     * @param ioPage
     * @param addr
     * @param value
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Integer onIoLoad32(byte[] ioPage, int addr, int value) {
        return null;
    }

    public void onIoStore8(byte[] ioPage, int addr, byte value) {
        if (addr >= InterruptController.ICR00_ADDRESS && addr <= InterruptController.ICR47_ADDRESS) {
            interruptController.updateRequestICR(addr - InterruptController.ICR00_ADDRESS, value);
        }
        else {
            switch (addr) {
                case REGISTER_DICR:
                    if ((value & 0x1) == 0) {
                        interruptController.removeRequest(InterruptController.DELAY_INTERRUPT_REQUEST_NR);
                    }
                    else {
                        interruptController.request(InterruptController.DELAY_INTERRUPT_REQUEST_NR);
                    }
                    break;
            }
        }
        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }

    public void onIoStore16(byte[] ioPage, int addr, int value) {
        switch (addr) {
            case REGISTER_TMRLRA0:
                reloadTimers[0].setReloadValue(value & 0x0000FFFF);
                break;
            case REGISTER_TMR0:
                System.out.println("Warning: ignoring attempt to write reloadTimer0 value. CPUState is " + cpuState);
                break;
            case REGISTER_TMCSR0:
                reloadTimers[0].setConfiguration(value & 0x0000FFFF);
                break;

            case REGISTER_TMRLRA1:
                reloadTimers[1].setReloadValue(value & 0x0000FFFF);
                break;
            case REGISTER_TMR1:
                System.out.println("Warning: ignoring attempt to write reloadTimer1 value. CPUState is " + cpuState);
                break;
            case REGISTER_TMCSR1:
                reloadTimers[1].setConfiguration(value & 0x0000FFFF);
                break;

            case REGISTER_TMRLRA2:
                reloadTimers[2].setReloadValue(value & 0x0000FFFF);
                break;
            case REGISTER_TMR2:
                System.out.println("Warning: ignoring attempt to write reloadTimer2 value. CPUState is " + cpuState);
                break;
            case REGISTER_TMCSR2:
                reloadTimers[2].setConfiguration(value & 0x0000FFFF);
                break;
        }
    }

    public void onIoStore32(byte[] ioPage, int addr, int value) {
        // noop
    }
}
