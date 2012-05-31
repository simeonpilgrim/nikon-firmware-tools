package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.dfr.CPUState;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.reloadTimer.ReloadTimer;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

public class ExpeedIoListener implements IoActivityListener {

    private static final int REGISTER_DICR    = 0x44;

    private static final int REGISTER_TMRLRA0 = 0x48;
    private static final int REGISTER_TMR0    = 0x4A;
    private static final int REGISTER_TMCSR0  = 0x4E;

    private static final int REGISTER_TMRLRA1 = REGISTER_TMRLRA0 + 0x8;
    private static final int REGISTER_TMR1    = REGISTER_TMR0 + 0x8;
    private static final int REGISTER_TMCSR1  = REGISTER_TMCSR0 + 0x8;

    private static final int REGISTER_TMRLRA2 = REGISTER_TMRLRA0 + 0x10;
    private static final int REGISTER_TMR2    = REGISTER_TMR0 + 0x10;
    private static final int REGISTER_TMCSR2  = REGISTER_TMCSR0 + 0x10;

    private static final int REGISTER_SCR_IBRC0  = 0x60;
    private static final int REGISTER_SMR0       = 0x61;
    private static final int REGISTER_SSR0       = 0x62;
    private static final int REGISTER_ESCR_IBSR0 = 0x63;
    private static final int REGISTER_RDR_TDR0   = 0x64;
    private static final int REGISTER_BGR10      = 0x66;
    private static final int REGISTER_BGR00      = 0x67;
    private static final int REGISTER_ISMK0      = 0x68;
    private static final int REGISTER_ISBA0      = 0x69;
    private static final int REGISTER_FCR10      = 0x6C;
    private static final int REGISTER_FCR00      = 0x6D;
    private static final int REGISTER_FBYTE20    = 0x6E;
    private static final int REGISTER_FBYTE10    = 0x6F;

    public static final int REGISTER_ICR00 = 0x440;


    private final CPUState cpuState;
    private final InterruptController interruptController;

    private final ReloadTimer[] reloadTimers;
    private SerialInterface[] serialInterfaces;

    public ExpeedIoListener(CPUState cpuState, InterruptController interruptController, ReloadTimer[] reloadTimers, SerialInterface[] serialInterfaces) {
        this.cpuState = cpuState;
        this.interruptController = interruptController;
        this.reloadTimers = reloadTimers;
        this.serialInterfaces = serialInterfaces;
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
        if (addr >= REGISTER_ICR00 && addr < REGISTER_ICR00 + 48 * 4) {
            interruptController.updateRequestICR(addr - REGISTER_ICR00, value);
        }
        else if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + 0x90) {
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> 4;
            switch (addr & 0xF) {
                case REGISTER_SCR_IBRC0:   // written by 8-bit
                    serialInterfaces[serialInterfaceNr].setScrIbcr(value & 0xFF);
                    break;
                case REGISTER_SMR0:       // written by 8-bit
                    serialInterfaces[serialInterfaceNr].setSmr(value& 0xFF);
                    break;
                case REGISTER_SSR0:
                    serialInterfaces[serialInterfaceNr].setSsr(value & 0xFF);
                    break;
                case REGISTER_ESCR_IBSR0: // written by 8-bit
                    serialInterfaces[serialInterfaceNr].setEscrIbsr(value & 0xFF);
                    break;
                case REGISTER_RDR_TDR0:   // written by 16-bit
                    throw new RuntimeException("Cannot write register RDR/TDR 8 bit at a time");
                case REGISTER_BGR10:      // written by 16-bit
                    serialInterfaces[serialInterfaceNr].setBgr1(value & 0xFF);
                    break;
                case REGISTER_BGR00:
                    serialInterfaces[serialInterfaceNr].setBgr0(value & 0xFF);
                    break;
                case REGISTER_ISMK0:
                    serialInterfaces[serialInterfaceNr].setIsmk(value & 0xFF);
                    break;
                case REGISTER_ISBA0:
                    serialInterfaces[serialInterfaceNr].setIsba(value & 0xFF);
                    break;
                case REGISTER_FCR10:
                    serialInterfaces[serialInterfaceNr].setFcr1(value & 0xFF);
                    break;
                case REGISTER_FCR00:      // written by 8-bit
                    serialInterfaces[serialInterfaceNr].setFcr0(value & 0xFF);
                    break;
                case REGISTER_FBYTE20:    // written by 16-bit
                    serialInterfaces[serialInterfaceNr].setFbyte2(value & 0xFF);
                    break;
                case REGISTER_FBYTE10:
                    serialInterfaces[serialInterfaceNr].setFbyte1(value & 0xFF);
                    break;
            }
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
        if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + 0x90) {
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> 4;
            switch (addr & 0xF) {
                case REGISTER_SCR_IBRC0:   // normally written by 8-bit
                    serialInterfaces[serialInterfaceNr].setScrIbcr((value >> 8) & 0xFF);
                    serialInterfaces[serialInterfaceNr].setSmr(value & 0xFF);
                    break;
                case REGISTER_SSR0:       // normally written by 8-bit
                    serialInterfaces[serialInterfaceNr].setSsr((value >> 8) & 0xFF);
                    serialInterfaces[serialInterfaceNr].setEscrIbsr(value & 0xFF);
                    break;
                case REGISTER_RDR_TDR0:   // 16-bit register
                    serialInterfaces[serialInterfaceNr].setTdr(value & 0xFFFF);
                    break;
                case REGISTER_BGR10:      // written by 16-bit
                    serialInterfaces[serialInterfaceNr].setBgr1((value >> 8) & 0xFF);
                    serialInterfaces[serialInterfaceNr].setBgr0(value & 0xFF);
                    break;
                case REGISTER_ISMK0:      // normally written by 8-bit
                    serialInterfaces[serialInterfaceNr].setIsmk((value >> 8) & 0xFF);
                    serialInterfaces[serialInterfaceNr].setIsba(value & 0xFF);
                    break;
                case REGISTER_FCR10:      // normally written by 8-bit
                    serialInterfaces[serialInterfaceNr].setFcr1((value >> 8) & 0xFF);
                    serialInterfaces[serialInterfaceNr].setFcr0(value & 0xFF);
                    break;
                case REGISTER_FBYTE20:    // written by 16-bit
                    serialInterfaces[serialInterfaceNr].setFbyte2((value >> 8) & 0xFF);
                    serialInterfaces[serialInterfaceNr].setFbyte1(value & 0xFF);
                    break;
            }
        }
        else {
            switch (addr) {
                case REGISTER_TMRLRA0:
                    reloadTimers[0].setReloadValue(value & 0xFFFF);
                    break;
                case REGISTER_TMR0:
                    System.out.println("Warning: ignoring attempt to write reloadTimer0 value. CPUState is " + cpuState);
                    break;
                case REGISTER_TMCSR0:
                    reloadTimers[0].setConfiguration(value & 0xFFFF);
                    break;

                case REGISTER_TMRLRA1:
                    reloadTimers[1].setReloadValue(value & 0xFFFF);
                    break;
                case REGISTER_TMR1:
                    System.out.println("Warning: ignoring attempt to write reloadTimer1 value. CPUState is " + cpuState);
                    break;
                case REGISTER_TMCSR1:
                    reloadTimers[1].setConfiguration(value & 0xFFFF);
                    break;

                case REGISTER_TMRLRA2:
                    reloadTimers[2].setReloadValue(value & 0xFFFF);
                    break;
                case REGISTER_TMR2:
                    System.out.println("Warning: ignoring attempt to write reloadTimer2 value. CPUState is " + cpuState);
                    break;
                case REGISTER_TMCSR2:
                    reloadTimers[2].setConfiguration(value & 0xFFFF);
                    break;
            }
        }
    }

    public void onIoStore32(byte[] ioPage, int addr, int value) {
        // noop
    }
}
