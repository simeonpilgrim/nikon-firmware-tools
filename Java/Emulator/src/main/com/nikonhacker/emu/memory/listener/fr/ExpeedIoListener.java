package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.clock.fr.FrClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.programmableTimer.fr.FrReloadTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.fr.FrReloadTimer32;
import com.nikonhacker.emu.peripherials.serialInterface.fr.FrSerialInterface;

public class ExpeedIoListener extends IoActivityListener {

    // Interrupt controller
    private static final int REGISTER_EIRR0 = 0x40;
    private static final int REGISTER_ENIR0 = 0x41;
    private static final int REGISTER_ELVR0 = 0x42;

    private static final int REGISTER_EIRR1 = 0xF0;
    private static final int REGISTER_ENIR1 = 0xF1;
    private static final int REGISTER_ELVR1 = 0xF2;
    // Delay interrupt
    private static final int REGISTER_DICR  = 0x44;

    // Timer
    public static final  int NUM_TIMER        = 3;
    private static final int REGISTER_TMRLRA0 = 0x48;
    private static final int REGISTER_TMR0    = 0x4A;
    private static final int REGISTER_TMCSR0  = 0x4E;

    private static final int REGISTER_TMRLRA1 = REGISTER_TMRLRA0 + 0x8;
    private static final int REGISTER_TMR1    = REGISTER_TMR0 + 0x8;
    private static final int REGISTER_TMCSR1  = REGISTER_TMCSR0 + 0x8;

    private static final int REGISTER_TMRLRA2 = REGISTER_TMRLRA0 + 0x10;
    private static final int REGISTER_TMR2    = REGISTER_TMR0 + 0x10;
    private static final int REGISTER_TMCSR2  = REGISTER_TMCSR0 + 0x10;

    // 32-bit Timer
    public static final  int NUM_TIMER32 = 12;
    private static final int TIMER32_OFFSET = 0x10;
    private static final int REGISTER_TMRLRA0_32 = 0x100;
    private static final int REGISTER_TMR0_32    = REGISTER_TMRLRA0_32 + 8;
    private static final int REGISTER_TMCSR0_32  = REGISTER_TMRLRA0_32 + 0xE;

    private static final int REGISTER_DIVR0 = 0x488;
    private static final int REGISTER_DIVR1 = 0x489;
    private static final int REGISTER_DIVR2 = 0x48A;

    // Serial ports
    /**
     *  Three serial registers have been spotted, configured at 0x60, 0x70 and 0xB0
     *  Let's assume the SerialInterface at 0x60 is the first in the Expeed, and that there are 6 serial interfaces
     *  (60, 70, 80, 90, A0, B0). This is pure speculation of course.
     */
    public static final  int NUM_SERIAL_IF         = 6;
    private static final int SERIAL_IF_OFFSET_BITS = 4;
    private static final int SERIAL_IF_OFFSET      = 1 << SERIAL_IF_OFFSET_BITS;
    private static final int REGISTER_SCR_IBRC0    = 0x60;
    private static final int REGISTER_SMR0         = 0x61;
    private static final int REGISTER_SSR0         = 0x62;
    private static final int REGISTER_ESCR_IBSR0   = 0x63;
    private static final int REGISTER_RDR_TDR0     = 0x64;
    private static final int REGISTER_BGR10        = 0x66;
    private static final int REGISTER_BGR00        = 0x67;
    private static final int REGISTER_ISMK0        = 0x68;
    private static final int REGISTER_ISBA0        = 0x69;
    private static final int REGISTER_FCR10        = 0x6C;
    private static final int REGISTER_FCR00        = 0x6D;
    private static final int REGISTER_FBYTE20      = 0x6E;
    private static final int REGISTER_FBYTE10      = 0x6F;

    // Interrupt controller
    public static final int REGISTER_ICR00 = 0x440;

    public ExpeedIoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);
    }

    @Override
    public boolean matches(int address) {
        return address >>> 16 == 0x0000;
    }

    /**
     * Called when reading 8-bit value from register address range
     *
     * @param ioPage
     * @param addr
     * @param value
     * @param accessSource
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Byte onLoadData8(byte[] ioPage, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_ICR00 && addr < REGISTER_ICR00 + 48) {
            // Interrupt request level registers
            // Standard memory is used
            return null;
        }
        // Serial Interface configuration registers
        if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + NUM_SERIAL_IF * SERIAL_IF_OFFSET) {
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> SERIAL_IF_OFFSET_BITS;
            FrSerialInterface serialInterface = (FrSerialInterface) platform.getSerialInterfaces()[serialInterfaceNr];
            switch (addr - (serialInterfaceNr << SERIAL_IF_OFFSET_BITS)) {
                case REGISTER_SCR_IBRC0:
                    return (byte)serialInterface.getScrIbcr();
                case REGISTER_SMR0:
                    return (byte)serialInterface.getSmr();
                case REGISTER_SSR0:
                    return (byte)serialInterface.getSsr();
                case REGISTER_ESCR_IBSR0:
                    return (byte)serialInterface.getEscrIbsr();
                case REGISTER_RDR_TDR0:   // written by 16-bit
                    stop("Cannot read RDR register 8 bit at a time for now");
                case REGISTER_BGR10:      // read by 16-bit
                    return (byte)serialInterface.getBgr1();
                case REGISTER_BGR00:
                    return (byte)serialInterface.getBgr0();
                case REGISTER_ISMK0:
                    return (byte)serialInterface.getIsmk();
                case REGISTER_ISBA0:
                    return (byte)serialInterface.getIsba();
                case REGISTER_FCR10:
                    return (byte)serialInterface.getFcr1();
                case REGISTER_FCR00:
                    return (byte)serialInterface.getFcr0();
                case REGISTER_FBYTE20:    // read by 16-bit
                    return (byte)serialInterface.getFbyte2();
                case REGISTER_FBYTE10:
                    return (byte)serialInterface.getFbyte1();
            }
        }
        else if (addr >= REGISTER_TMRLRA0_32 && addr < (REGISTER_TMRLRA0_32+NUM_TIMER32*TIMER32_OFFSET)) {
            // 32-bit timer
            stop("32-bit timer registers cannot be accessed by 8-bit for now");
        }
        else if ( ((addr >= REGISTER_EIRR0) && (addr < (REGISTER_ELVR0 + 2))) ||
                  ((addr >= REGISTER_EIRR1) && (addr < (REGISTER_ELVR1 + 2)))) {
            FrInterruptController interruptController = (FrInterruptController)platform.getInterruptController();
            int unit = 0;
            
            if (addr>=REGISTER_EIRR1) {
                unit = 1;
                addr -= (REGISTER_EIRR1-REGISTER_EIRR0);
            }
            switch (addr) {
                case REGISTER_EIRR0:
                    return (byte)interruptController.getEirr(unit);
                case REGISTER_ENIR0:
                    return (byte)interruptController.getEnir(unit);
                case REGISTER_ELVR0:
                    return (byte)(interruptController.getElvr(unit) >> 8);
                case REGISTER_ELVR0+1:
                    return (byte)(interruptController.getElvr(unit) & 0xFF);
            }
        }
        else {
            switch (addr) {
                // Delay interrupt register
                case REGISTER_DICR:
                    // Seems the code often writes to AC to DICR, then immediately rereads it to AC (!) and moves AC into AC (!!).
                    // Maybe to defeat the pipeline and give a little delay for the interrupt to occur ? Anyway...
                    // Spec says highest 7 bits are read as 1. No precision for bit 0.
                    // Assume it is zero...
                    return (byte)0b11111110;

                // Clock division registers
                case REGISTER_DIVR0:
                    return (byte)((FrClockGenerator)platform.getClockGenerator()).getDivr0();
                case REGISTER_DIVR1:
                    return (byte)((FrClockGenerator)platform.getClockGenerator()).getDivr1();
                case REGISTER_DIVR2:
                    return (byte)((FrClockGenerator)platform.getClockGenerator()).getDivr2();
            }
        }

        if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Load8 is not supported yet");

        return null;
    }

    /**
     * Called when reading 16-bit value from register address range
     *
     *
     * @param ioPage
     * @param addr
     * @param value
     * @param accessSource
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Integer onLoadData16(byte[] ioPage, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        // Serial Interface configuration registers
        if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + NUM_SERIAL_IF * SERIAL_IF_OFFSET) {
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> SERIAL_IF_OFFSET_BITS;
            FrSerialInterface serialInterface = (FrSerialInterface) platform.getSerialInterfaces()[serialInterfaceNr];
            switch (addr - (serialInterfaceNr << SERIAL_IF_OFFSET_BITS)) {
                case REGISTER_SCR_IBRC0:
                    return (serialInterface.getScrIbcr() << 8) | serialInterface.getSmr();
                case REGISTER_SSR0:
                    return (serialInterface.getSsr() << 8) | serialInterface.getEscrIbsr();
                case REGISTER_RDR_TDR0:
                    return serialInterface.getRdr();
                case REGISTER_BGR10:
                    return (serialInterface.getBgr1() << 8) | serialInterface.getBgr0();
                case REGISTER_ISMK0:
                    return (serialInterface.getIsmk() << 8) | serialInterface.getIsba();
                case REGISTER_FCR10:
                    return (serialInterface.getFcr1() << 8) | serialInterface.getFcr0();
                case REGISTER_FBYTE20:
                    return (serialInterface.getFbyte2() << 8) | serialInterface.getFbyte1();
            }
        }
        else if (addr >= REGISTER_TMRLRA0_32 && addr < (REGISTER_TMRLRA0_32 + NUM_TIMER32 * TIMER32_OFFSET)) {
            // 32-bit timer
            int channel;

            channel = (addr - REGISTER_TMRLRA0_32) / TIMER32_OFFSET;
            addr -= (channel * TIMER32_OFFSET);
            
            // correction because 32-bit timers are at the end of 16-bit timers
            channel += NUM_TIMER;
            switch (addr) {
                case REGISTER_TMRLRA0_32:
                    return (((FrReloadTimer32)platform.getProgrammableTimers()[channel]).getTmrlra() >> 16);
                case REGISTER_TMRLRA0_32 + 2:
                    return (((FrReloadTimer32)platform.getProgrammableTimers()[channel]).getTmrlra() & 0xFFFF);
                case REGISTER_TMR0_32:
                    return (((FrReloadTimer32)platform.getProgrammableTimers()[channel]).getTmr() >> 16);
                case REGISTER_TMR0_32 + 2:
                    return (((FrReloadTimer32)platform.getProgrammableTimers()[channel]).getTmr() & 0xFFFF);
                case REGISTER_TMCSR0_32:
                    return ((FrReloadTimer32)platform.getProgrammableTimers()[channel]).getTmcsr();
                default:
                    stop("Warning: ignoring attempt to read 16-bit register in 32-bit Timer");
            }
        }
        else if ( (addr >= REGISTER_EIRR0 && addr < (REGISTER_ELVR0 + 2)) ||
                  (addr >= REGISTER_EIRR1 && addr < (REGISTER_ELVR1 + 2)) ) {
            FrInterruptController interruptController = (FrInterruptController)platform.getInterruptController();
            int unit = 0;
            
            if (addr >= REGISTER_EIRR1) {
                unit = 1;
                addr -= (REGISTER_EIRR1 - REGISTER_EIRR0);
            }
            switch (addr) {
                case REGISTER_EIRR0:
                    return ((interruptController.getEirr(unit) << 8) | interruptController.getEnir(unit));
                case REGISTER_ELVR0:
                    return interruptController.getElvr(unit);
            }
        }
        else {
            switch (addr) {
                // Reload Timer configuration registers
                case REGISTER_TMRLRA0:
                    return ((FrReloadTimer)platform.getProgrammableTimers()[0]).getTmrlra();
                case REGISTER_TMR0:
                    return ((FrReloadTimer)platform.getProgrammableTimers()[0]).getTmr();
                case REGISTER_TMCSR0:
                    return ((FrReloadTimer)platform.getProgrammableTimers()[0]).getTmcsr();

                case REGISTER_TMRLRA1:
                    return ((FrReloadTimer)platform.getProgrammableTimers()[1]).getTmrlra();
                case REGISTER_TMR1:
                    return ((FrReloadTimer)platform.getProgrammableTimers()[1]).getTmr();
                case REGISTER_TMCSR1:
                    return ((FrReloadTimer)platform.getProgrammableTimers()[1]).getTmcsr();

                case REGISTER_TMRLRA2:
                    return ((FrReloadTimer)platform.getProgrammableTimers()[2]).getTmrlra();
                case REGISTER_TMR2:
                    return ((FrReloadTimer)platform.getProgrammableTimers()[2]).getTmr();
                case REGISTER_TMCSR2:
                    return ((FrReloadTimer)platform.getProgrammableTimers()[2]).getTmcsr();

                case REGISTER_DIVR0:
                case REGISTER_DIVR1:
                case REGISTER_DIVR2:
                    stop("Warning: reading DIVR registers by 16bit is not supported");
            }
        }

        if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Load16 is not supported yet");

        return null;
    }

    /**
     * Called when reading 32-bit value from register address range
     *
     *
     * @param ioPage
     * @param addr
     * @param value
     * @param accessSource
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Integer onLoadData32(byte[] ioPage, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_TMRLRA0_32 && addr < (REGISTER_TMRLRA0_32 + NUM_TIMER32 * TIMER32_OFFSET)) {
            // 32-bit timer
            int channel;

            channel = (addr - REGISTER_TMRLRA0_32) / TIMER32_OFFSET;
            addr -= (channel * TIMER32_OFFSET);
            
            // correction because 32-bit timers are at the end of 16-bit timers
            channel += NUM_TIMER;
            switch (addr) {
                case REGISTER_TMRLRA0_32:
                    return ((FrReloadTimer32)platform.getProgrammableTimers()[channel]).getTmrlra();
                case REGISTER_TMR0_32:
                    return ((FrReloadTimer32)platform.getProgrammableTimers()[channel]).getTmr();
                default:
                    stop("Warning: ignoring attempt to write 32-bit register in 32-bit Timer");
            }
        }
        else if (addr == REGISTER_EIRR0 || addr == REGISTER_EIRR1) {
            FrInterruptController interruptController = (FrInterruptController)platform.getInterruptController();
            int unit = (addr==REGISTER_EIRR1 ? 1 : 0);
            return ((interruptController.getEirr(unit) << 24) | (interruptController.getEnir(unit) << 16) | interruptController.getElvr(unit));
        }
        else {
            switch (addr) {
                case REGISTER_DIVR0:
                case REGISTER_DIVR1:
                case REGISTER_DIVR2:
                    stop("Warning: reading DIVR registers by 32bit is not supported");
            }
        }

        if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Load32 is not supported yet");

        return null;
    }

    public void onStore8(byte[] ioPage, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_ICR00 && addr < REGISTER_ICR00 + 48) {
            // Interrupt request level registers
            ((FrInterruptController)platform.getInterruptController()).updateRequestICR(addr - REGISTER_ICR00, value);
        }
        else if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + NUM_SERIAL_IF * SERIAL_IF_OFFSET) {
            // Serial Interface configuration registers
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> SERIAL_IF_OFFSET_BITS;
            FrSerialInterface serialInterface = (FrSerialInterface) platform.getSerialInterfaces()[serialInterfaceNr];
            switch (addr - (serialInterfaceNr << SERIAL_IF_OFFSET_BITS)) {
                case REGISTER_SCR_IBRC0:   // written by 8-bit
                    serialInterface.setScrIbcr(value & 0xFF);
                    break;
                case REGISTER_SMR0:       // written by 8-bit
                    serialInterface.setSmr(value& 0xFF);
                    break;
                case REGISTER_SSR0:
                    serialInterface.setSsr(value & 0xFF);
                    break;
                case REGISTER_ESCR_IBSR0: // written by 8-bit
                    serialInterface.setEscrIbsr(value & 0xFF);
                    break;
                case REGISTER_RDR_TDR0:   // written by 16-bit
                    stop("Cannot write TDR register 8 bit at a time for now");
                case REGISTER_BGR10:      // written by 16-bit
                    serialInterface.setBgr1(value & 0xFF);
                    break;
                case REGISTER_BGR00:
                    serialInterface.setBgr0(value & 0xFF);
                    break;
                case REGISTER_ISMK0:
                    serialInterface.setIsmk(value & 0xFF);
                    break;
                case REGISTER_ISBA0:
                    serialInterface.setIsba(value & 0xFF);
                    break;
                case REGISTER_FCR10:
                    serialInterface.setFcr1(value & 0xFF);
                    break;
                case REGISTER_FCR00:      // written by 8-bit
                    serialInterface.setFcr0(value & 0xFF);
                    break;
                case REGISTER_FBYTE20:    // written by 16-bit
                    serialInterface.setFbyte2(value & 0xFF);
                    break;
                case REGISTER_FBYTE10:
                    serialInterface.setFbyte1(value & 0xFF);
                    break;
            }
        }
        else if (addr >= REGISTER_TMRLRA0_32 && addr < (REGISTER_TMRLRA0_32 + NUM_TIMER32 * TIMER32_OFFSET)) {
            // 32-bit timer
            stop("32-bit timer registers cannot be accessed by 8-bit for now");
        }
        else if ( (addr >= REGISTER_EIRR0 && addr < (REGISTER_ELVR0 + 2)) ||
                  (addr >= REGISTER_EIRR1 && addr < (REGISTER_ELVR1 + 2)) ) {
            FrInterruptController interruptController = (FrInterruptController)platform.getInterruptController();
            int unit = 0;
            
            if (addr >= REGISTER_EIRR1) {
                unit = 1;
                addr -= (REGISTER_EIRR1 - REGISTER_EIRR0);
            }
            switch (addr) {
                case REGISTER_EIRR0:
                    interruptController.setEirr(unit, value); break;
                case REGISTER_ENIR0:
                    interruptController.setEnir(unit, value); break;
                case REGISTER_ELVR0:
                    interruptController.setElvrHi(unit, value); break;
                case REGISTER_ELVR0+1:
                    interruptController.setElvrLo(unit, value); break;
            }
        }
        else {
            switch (addr) {
                // Delay interrupt register
                case REGISTER_DICR:
                    if ((value & 0x1) == 0) {
                        platform.getInterruptController().removeRequest(FrInterruptController.DELAY_INTERRUPT_REQUEST_NR);
                    }
                    else {
                        platform.getInterruptController().request(FrInterruptController.DELAY_INTERRUPT_REQUEST_NR);
                    }
                    break;


                case REGISTER_DIVR0:
                    ((FrClockGenerator)platform.getClockGenerator()).setDivr0(value & 0xFF);
                    break;
                case REGISTER_DIVR1:
                    ((FrClockGenerator)platform.getClockGenerator()).setDivr1(value & 0xFF);
                    break;
                case REGISTER_DIVR2:
                    ((FrClockGenerator)platform.getClockGenerator()).setDivr2(value & 0xFF);
                    break;

                default:
                    if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Store8 value 0x" + Format.asHex(value, 2) + " is not supported yet");
            }
        }
    }

    public void onStore16(byte[] ioPage, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        // Serial Interface configuration registers
        if (addr >= REGISTER_SCR_IBRC0 && addr < REGISTER_SCR_IBRC0 + NUM_SERIAL_IF * SERIAL_IF_OFFSET) {
            int serialInterfaceNr = (addr - REGISTER_SCR_IBRC0) >> SERIAL_IF_OFFSET_BITS;
            FrSerialInterface serialInterface = (FrSerialInterface) platform.getSerialInterfaces()[serialInterfaceNr];
            switch (addr - (serialInterfaceNr << SERIAL_IF_OFFSET_BITS)) {
                case REGISTER_SCR_IBRC0:   // normally written by 8-bit
                    serialInterface.setScrIbcr((value >> 8) & 0xFF);
                    serialInterface.setSmr(value & 0xFF);
                    break;
                case REGISTER_SSR0:       // normally written by 8-bit
                    serialInterface.setSsr((value >> 8) & 0xFF);
                    serialInterface.setEscrIbsr(value & 0xFF);
                    break;
                case REGISTER_RDR_TDR0:   // 16-bit register
                    serialInterface.setTdr(value & 0xFFFF);
                    break;
                case REGISTER_BGR10:      // written by 16-bit
                    serialInterface.setBgr1((value >> 8) & 0xFF);
                    serialInterface.setBgr0(value & 0xFF);
                    break;
                case REGISTER_ISMK0:      // normally written by 8-bit
                    serialInterface.setIsmk((value >> 8) & 0xFF);
                    serialInterface.setIsba(value & 0xFF);
                    break;
                case REGISTER_FCR10:      // normally written by 8-bit
                    serialInterface.setFcr1((value >> 8) & 0xFF);
                    serialInterface.setFcr0(value & 0xFF);
                    break;
                case REGISTER_FBYTE20:    // written by 16-bit
                    serialInterface.setFbyte2((value >> 8) & 0xFF);
                    serialInterface.setFbyte1(value & 0xFF);
                    break;
            }
        }
        else if (addr >= REGISTER_TMRLRA0_32 && addr < (REGISTER_TMRLRA0_32 + NUM_TIMER32 * TIMER32_OFFSET)) {
            // 32-bit timer
            int channel;

            channel = (addr - REGISTER_TMRLRA0_32) / TIMER32_OFFSET;
            addr -= (channel * TIMER32_OFFSET);
            
            // correction because 32-bit timers are at the end of 16-bit timers
            channel += NUM_TIMER;
            switch (addr) {
                case REGISTER_TMCSR0_32:
                    ((FrReloadTimer32)platform.getProgrammableTimers()[channel]).setTmcsr(value & 0xFFFF); break;
                default:
                    stop("Warning: ignoring attempt to write 16-bit register in 32-bit Timer");
            }
        }
        else if ( (addr >= REGISTER_EIRR0 && addr < (REGISTER_ELVR0 + 2)) ||
                  (addr >= REGISTER_EIRR1 && addr < (REGISTER_ELVR1 + 2)) ) {
            FrInterruptController interruptController = (FrInterruptController)platform.getInterruptController();
            int unit = 0;
            
            if (addr >= REGISTER_EIRR1) {
                unit = 1;
                addr -= (REGISTER_EIRR1 - REGISTER_EIRR0);
            }
            switch (addr) {
                case REGISTER_EIRR0:
                    interruptController.setEirr(unit, value >> 8);
                    interruptController.setEnir(unit, value & 0xFF);
                    break;
                case REGISTER_ELVR0:
                    interruptController.setElvr(unit, value);
                    break;
            }
        }
        else {
            // TODO remove copy/paste by using the same logic as for Serial Ports
            // Reload Timer configuration registers
            switch (addr) {
                case REGISTER_TMRLRA0:
                    ((FrReloadTimer)platform.getProgrammableTimers()[0]).setTmrlra(value & 0xFFFF);
                    break;
                case REGISTER_TMR0:
                    stop("Warning: ignoring attempt to write reloadTimer0 value");
                case REGISTER_TMCSR0:
                    ((FrReloadTimer)platform.getProgrammableTimers()[0]).setTmcsr(value & 0xFFFF);
                    break;

                case REGISTER_TMRLRA1:
                    ((FrReloadTimer)platform.getProgrammableTimers()[1]).setTmrlra(value & 0xFFFF);
                    break;
                case REGISTER_TMR1:
                    stop("Warning: ignoring attempt to write reloadTimer1 value 0x" + Format.asHex(platform.getCpuState().pc, 8));
                case REGISTER_TMCSR1:
                    ((FrReloadTimer)platform.getProgrammableTimers()[1]).setTmcsr(value & 0xFFFF);
                    break;

                case REGISTER_TMRLRA2:
                    ((FrReloadTimer)platform.getProgrammableTimers()[2]).setTmrlra(value & 0xFFFF);
                    break;
                case REGISTER_TMR2:
                    stop("Warning: ignoring attempt to write reloadTimer2 value");
                case REGISTER_TMCSR2:
                    ((FrReloadTimer)platform.getProgrammableTimers()[2]).setTmcsr(value & 0xFFFF);
                    break;

                case REGISTER_DIVR0:
                case REGISTER_DIVR1:
                case REGISTER_DIVR2:
                    stop("Warning: writing DIVR registers by 16bit is not supported");

                default:
                    if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Store16 value 0x" + Format.asHex(value, 4) + " is not supported yet");
            }
        }
    }

    public void onStore32(byte[] ioPage, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_TMRLRA0_32 && addr < (REGISTER_TMRLRA0_32 + NUM_TIMER32 * TIMER32_OFFSET)) {
            // 32-bit timer
            int channel;

            channel = (addr - REGISTER_TMRLRA0_32) / TIMER32_OFFSET;
            addr -= (channel * TIMER32_OFFSET);
            
            // correction because 32-bit timers are at the end of 16-bit timers
            channel += NUM_TIMER;
            switch (addr) {
                case REGISTER_TMRLRA0_32:
                    ((FrReloadTimer32)platform.getProgrammableTimers()[channel]).setTmrlra(value); break;
                default:
                    stop("Warning: ignoring attempt to write 32-bit register in 32-bit Timer");
            }
        }
        else switch(addr) {
            case REGISTER_EIRR0:
            case REGISTER_EIRR1:
                int unit = (addr == REGISTER_EIRR1 ? 1 : 0);

                FrInterruptController interruptController = (FrInterruptController)platform.getInterruptController();
                interruptController.setEirr(unit, (value >> 24) & 0xFF);
                interruptController.setElvr(unit, value & 0xFFFF);
                interruptController.setEnir(unit, (value >> 16) & 0xFF);
                break;

            case REGISTER_DIVR0:
            case REGISTER_DIVR1:
            case REGISTER_DIVR2:
                stop("Warning: writing DIVR registers by 32bit is not supported");

            default:
                if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Store32 value 0x" + Format.asHex(value, 8) + " is not supported yet");
        }
    }

}

/*

TODO: Unimplemented registers accessed by the code

Register 0x000000B6: Load16 is not supported yet => ???

Register 0x000000F0: Store8 value 0x00 is not supported yet => Remote control ???
Register 0x000000F1: Load8 is not supported yet
Register 0x000000F1: Store8 value 0x00 is not supported yet
Register 0x000000F1: Store8 value 0x20 is not supported yet
Register 0x000000F2: Load16 is not supported yet
Register 0x000000F2: Store16 value 0x000C is not supported yet
Register 0x000000F2: Store16 value 0x002C is not supported yet
Register 0x000000F2: Store16 value 0x00EC is not supported yet
Register 0x000000F2: Store16 value 0x02EC is not supported yet
Register 0x000000F2: Store16 value 0x0AEC is not supported yet

Register 0x00000150: Store32 value 0x000009C3 is not supported yet => Other freerun timer ?
Register 0x00000150: Store32 value 0x0000C92B is not supported yet
Register 0x00000150: Store32 value 0x0000DABF is not supported yet
Register 0x00000150: Store32 value 0x00011557 is not supported yet
Register 0x00000150: Store32 value 0x0001869F is not supported yet
Register 0x00000150: Store32 value 0x000249EF is not supported yet
Register 0x0000015E: Store16 value 0x0000 is not supported yet
Register 0x0000015E: Store16 value 0x000B is not supported yet

Register 0x000003E0: Store32 value 0x47000000 is not supported yet => CARR ??(accessed by 32bits ?)
Register 0x000003E3: Store8 value 0x03 is not supported yet        => DCHCR ??
Register 0x000003E7: Store8 value 0x03 is not supported yet        => ICHCR ??

Register 0x00000481: Store8 value 0x00 is not supported yet
Register 0x00000482: Store8 value 0x03 is not supported yet

Register 0x00000600: Store32 value 0x00040185 is not supported yet
Register 0x00000604: Store32 value 0x7000008D is not supported yet
Register 0x00000608: Store32 value 0x03000105 is not supported yet
Register 0x0000060C: Store32 value 0x400001CD is not supported yet
Register 0x00000610: Store32 value 0x7F00008D is not supported yet
Register 0x00000614: Store32 value 0x7F01009D is not supported yet

Register 0x00000640: Store32 value 0x00000040 is not supported yet
Register 0x00000644: Store32 value 0x00000040 is not supported yet
Register 0x00000648: Store32 value 0x00000040 is not supported yet
Register 0x0000064C: Store32 value 0x00000040 is not supported yet
Register 0x00000650: Store32 value 0x00000080 is not supported yet
Register 0x00000654: Store32 value 0x00000080 is not supported yet

Register 0x00000680: Store32 value 0x799F9910 is not supported yet
Register 0x00000684: Store32 value 0x333A5500 is not supported yet
Register 0x00000688: Store32 value 0x02250501 is not supported yet
Register 0x0000068C: Store32 value 0x01105502 is not supported yet
Register 0x00000690: Store32 value 0x01105500 is not supported yet
Register 0x00000694: Store32 value 0x02105500 is not supported yet

Register 0x000006F8: Store32 value 0x0000001C is not supported yet

Register 0x000007EF: Store8 value 0x10 is not supported yet

*/