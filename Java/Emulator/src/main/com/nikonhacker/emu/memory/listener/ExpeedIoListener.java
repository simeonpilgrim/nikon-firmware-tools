package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.peripherials.interruptController.FrInterruptController;
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

    /**
     *  Three serial registers have been spotted, configured at 0x60, 0x70 and 0xB0
     *  Let's assume the SerialInterface at 0x60 is the first in the Expeed, and that there are 6 serial interfaces
     *  (60, 70, 80, 90, A0, B0). This is pure speculation of course.
     */
    private static final int NB_SERIAL_IF = 6;
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


    private final FrCPUState cpuState;
    private final InterruptController interruptController;

    private final ReloadTimer[] reloadTimers;
    private SerialInterface[] serialInterfaces;

    public ExpeedIoListener(FrCPUState cpuState, InterruptController interruptController, ReloadTimer[] reloadTimers, SerialInterface[] serialInterfaces) {
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
        // Serial Interface configuration registers
        if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + NB_SERIAL_IF * 0x10) {
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> 4;
            switch (addr - (serialInterfaceNr << 4)) {
                case REGISTER_SCR_IBRC0:
                    return (byte)serialInterfaces[serialInterfaceNr].getScrIbcr();
                case REGISTER_SMR0:
                    return (byte)serialInterfaces[serialInterfaceNr].getSmr();
                case REGISTER_SSR0:
                    return (byte)serialInterfaces[serialInterfaceNr].getSsr();
                case REGISTER_ESCR_IBSR0:
                    return (byte)serialInterfaces[serialInterfaceNr].getEscrIbsr();
                case REGISTER_RDR_TDR0:   // written by 16-bit
                    throw new RuntimeException("Cannot read RDR register 8 bit at a time for now");
                case REGISTER_BGR10:      // read by 16-bit
                    return (byte)serialInterfaces[serialInterfaceNr].getBgr1();
                case REGISTER_BGR00:
                    return (byte)serialInterfaces[serialInterfaceNr].getBgr0();
                case REGISTER_ISMK0:
                    return (byte)serialInterfaces[serialInterfaceNr].getIsmk();
                case REGISTER_ISBA0:
                    return (byte)serialInterfaces[serialInterfaceNr].getIsba();
                case REGISTER_FCR10:
                    return (byte)serialInterfaces[serialInterfaceNr].getFcr1();
                case REGISTER_FCR00:
                    return (byte)serialInterfaces[serialInterfaceNr].getFcr0();
                case REGISTER_FBYTE20:    // read by 16-bit
                    return (byte)serialInterfaces[serialInterfaceNr].getFbyte2();
                case REGISTER_FBYTE10:
                    return (byte)serialInterfaces[serialInterfaceNr].getFbyte1();
            }
        }
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
        // Serial Interface configuration registers
        if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + NB_SERIAL_IF * 0x10) {
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> 4;
            switch (addr - (serialInterfaceNr << 4)) {
                case REGISTER_SCR_IBRC0:
                    return (serialInterfaces[serialInterfaceNr].getScrIbcr() << 8) | serialInterfaces[serialInterfaceNr].getSmr();
                case REGISTER_SSR0:
                    return (serialInterfaces[serialInterfaceNr].getSsr() << 8) | serialInterfaces[serialInterfaceNr].getEscrIbsr();
                case REGISTER_RDR_TDR0:
                    return serialInterfaces[serialInterfaceNr].getRdr();
                case REGISTER_BGR00:
                    return (serialInterfaces[serialInterfaceNr].getBgr1() << 8) | serialInterfaces[serialInterfaceNr].getBgr0();
                case REGISTER_ISMK0:
                    return (serialInterfaces[serialInterfaceNr].getIsmk() << 8) | serialInterfaces[serialInterfaceNr].getIsba();
                case REGISTER_FCR10:
                    return (serialInterfaces[serialInterfaceNr].getFcr1() << 8) | serialInterfaces[serialInterfaceNr].getFcr0();
                case REGISTER_FBYTE20:
                    return (serialInterfaces[serialInterfaceNr].getFbyte2() << 8) | serialInterfaces[serialInterfaceNr].getFbyte1();
                }
        }
        // Reload Timer configuration registers
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
            // Interrupt request level registers
            ((FrInterruptController)interruptController).updateRequestICR(addr - REGISTER_ICR00, value);
        }
        else if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + NB_SERIAL_IF * 0x10) {
            // Serial Interface configuration registers
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> 4;
            switch (addr - (serialInterfaceNr << 4)) {
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
                    throw new RuntimeException("Cannot write TDR register 8 bit at a time for now");
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
                // Delay interrupt register
                case REGISTER_DICR:
                    if ((value & 0x1) == 0) {
                        interruptController.removeRequest(FrInterruptController.DELAY_INTERRUPT_REQUEST_NR);
                    }
                    else {
                        interruptController.request(FrInterruptController.DELAY_INTERRUPT_REQUEST_NR);
                    }
                    break;

            }
        }
        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }

    public void onIoStore16(byte[] ioPage, int addr, int value) {
        // Serial Interface configuration registers
        if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + NB_SERIAL_IF * 0x10) {
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> 4;
            switch (addr - (serialInterfaceNr << 4)) {
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
            // Reload Timer configuration registers
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
